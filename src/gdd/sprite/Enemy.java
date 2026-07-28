package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Side-scroll enemy with two types (from EnemySprite.png) and cracked states.
// Enemies enter from the right, then STRAFE around the right side (never crossing
// the left border), and slowly turn to face the player's direction with a lag.
public class Enemy extends Sprite {

    private static final BufferedImage[] BASE_NORMAL = new BufferedImage[2];
    private static final BufferedImage[] BASE_CRACKED = new BufferedImage[2];
    private static final BufferedImage[] EXHAUST_UP = new BufferedImage[5];
    private static final Map<String, Image> ROT_CACHE = new HashMap<>();
    private static final Map<String, Image> EXHAUST_ROT_CACHE = new HashMap<>();

    private static final int HOLD_MIN_X = (int) (BOARD_WIDTH * 0.42);
    private static final int HOLD_MAX_X = BOARD_WIDTH - 90;
    private static final int EXHAUST_FRAME_TICKS = 5;
    private static final double EXHAUST_SCALE = 0.32;
    private static final double FORWARD_MOVEMENT_THRESHOLD = 0.12;

    protected int kind = 1; // 1 or 2
    protected double preciseX;
    protected double preciseY;
    protected double targetX;
    protected double fallSpeed = 1.0;
    protected double driftSpeed;
    protected double weavePhase;
    protected double facingAngle = 270; // display rotation of the up-facing art; 270 = facing left
    protected int strafeTimer;
    protected int health = 1;
    protected int maxHealth = 1;
    protected int scoreValue = 100;
    private boolean exhaustActive;
    private int exhaustTick;
    private int exhaustFrameIndex;

    public Enemy(int x, int y) {
        this(x, y, 1);
    }

    public Enemy(int x, int y, int kind) {
        this.kind = kind == 2 ? 2 : 1;
        initEnemy(x, y);
    }

    private static void loadFrames() {
        if (BASE_NORMAL[0] != null) {
            return;
        }
        BufferedImage sheet = loadSheet(IMG_ENEMY_SHEET);
        BASE_NORMAL[0] = baseUp(sheet, 45, 227, 203, 191, 66);
        BASE_NORMAL[1] = baseUp(sheet, 268, 227, 144, 191, 60);
        BASE_CRACKED[0] = baseUp(sheet, 45, 590, 203, 191, 66);
        BASE_CRACKED[1] = baseUp(sheet, 268, 590, 144, 191, 60);

        BufferedImage effects = loadSheet(IMG_ENEMY_EFFECT);
        int[][] exhaustRects = {
            {384, 140, 61, 176},
            {595, 127, 101, 203},
            {829, 130, 100, 197},
            {1060, 133, 100, 191},
            {1297, 154, 96, 149}
        };
        for (int i = 0; i < exhaustRects.length; i++) {
            int[] r = exhaustRects[i];
            // Flip the sheet frame so its curved leading edge faces back toward
            // the ship instead of pointing away from it. The exhaust is still
            // positioned behind the enemy below; this only corrects the art's
            // visual thrust direction.
            BufferedImage source = flipHorizontal(
                    effects.getSubimage(r[0], r[1], r[2], r[3]));
            // Rotate into the same up-facing base orientation as the enemy so
            // both continue to use the identical runtime facing angle.
            BufferedImage up = rotate(source, 90);
            EXHAUST_UP[i] = scaleImage(up,
                    Math.max(1, (int) Math.round(up.getWidth() * EXHAUST_SCALE)),
                    Math.max(1, (int) Math.round(up.getHeight() * EXHAUST_SCALE)));
        }
    }

    // Scaled but still facing up; rotation is applied dynamically per frame.
    private static BufferedImage baseUp(BufferedImage sheet, int x, int y, int w, int h, int targetH) {
        BufferedImage sub = sheet.getSubimage(
                Math.max(0, Math.min(x, sheet.getWidth() - 1)),
                Math.max(0, Math.min(y, sheet.getHeight() - 1)),
                Math.max(1, Math.min(w, sheet.getWidth() - x)),
                Math.max(1, Math.min(h, sheet.getHeight() - y)));
        int tw = Math.max(1, sub.getWidth() * targetH / sub.getHeight());
        return scaleImage(sub, tw, targetH);
    }

    private void initEnemy(int x, int y) {
        loadFrames();
        this.x = x;
        this.y = y;
        this.preciseX = x;
        this.preciseY = y;
        this.weavePhase = Math.random() * Math.PI * 2;
        this.targetX = HOLD_MIN_X + Math.random() * (HOLD_MAX_X - HOLD_MIN_X);
        this.strafeTimer = 80 + (int) (Math.random() * 120);
        setImage(frameForAngle());
    }

    // px,py = the point (player centre) the enemy should turn toward.
    public void act(int px, int py) {
        double previousX = preciseX;
        double previousY = preciseY;
        weavePhase += 0.04;
        double toTarget = targetX - preciseX;
        preciseX += Math.max(-fallSpeed, Math.min(fallSpeed, toTarget));
        preciseY += Math.sin(weavePhase) * 1.3 + driftSpeed;
        if (preciseX < HOLD_MIN_X) preciseX = HOLD_MIN_X;
        if (preciseX > BOARD_WIDTH + 50) preciseX = BOARD_WIDTH + 50;
        if (preciseY < 30) preciseY = 30;
        if (preciseY > BOARD_HEIGHT - 80) preciseY = BOARD_HEIGHT - 80;
        if (--strafeTimer <= 0) {
            targetX = HOLD_MIN_X + Math.random() * (HOLD_MAX_X - HOLD_MIN_X);
            driftSpeed = (Math.random() - 0.5) * 2.0;
            strafeTimer = 80 + (int) (Math.random() * 120);
        }
        this.x = (int) preciseX;
        this.y = (int) preciseY;

        // aim toward the player, but turn slowly (lag) instead of snapping
        double dx = px - (preciseX + 34);
        double dy = py - (preciseY + 34);
        double target = Math.toDegrees(Math.atan2(dy, dx)) + 90; // up-facing art -> point at player
        facingAngle = approachAngle(facingAngle, target, 3.5);
        setImage(frameForAngle());
        updateExhaust(preciseX - previousX, preciseY - previousY);
    }

    private void updateExhaust(double movementX, double movementY) {
        double radians = Math.toRadians(facingAngle);
        double forwardX = Math.sin(radians);
        double forwardY = -Math.cos(radians);
        double forwardMovement = movementX * forwardX + movementY * forwardY;
        exhaustActive = Math.hypot(movementX, movementY) > FORWARD_MOVEMENT_THRESHOLD
                && forwardMovement > FORWARD_MOVEMENT_THRESHOLD;
        if (exhaustActive) {
            exhaustFrameIndex = (exhaustTick / EXHAUST_FRAME_TICKS) % EXHAUST_UP.length;
            exhaustTick++;
        } else {
            exhaustTick = 0;
            exhaustFrameIndex = 0;
        }
    }

    private static double approachAngle(double current, double target, double step) {
        double diff = ((target - current + 540) % 360) - 180; // shortest signed difference
        if (Math.abs(diff) <= step) {
            return (target % 360 + 360) % 360;
        }
        double next = current + Math.signum(diff) * step;
        return (next % 360 + 360) % 360;
    }

    private Image frameForAngle() {
        BufferedImage base = (health < maxHealth ? BASE_CRACKED : BASE_NORMAL)[kind - 1];
        int bucket = angleBucket();
        final int b = bucket;
        return ROT_CACHE.computeIfAbsent(kind + (health < maxHealth ? "c" : "n") + b,
                key -> rotate(base, b));
    }

    private int angleBucket() {
        int bucket = (int) (Math.round(facingAngle / 10.0) * 10) % 360;
        return bucket < 0 ? bucket + 360 : bucket;
    }

    public boolean isExhaustActive() {
        return exhaustActive;
    }

    public Image getExhaustImage() {
        if (!exhaustActive) {
            return null;
        }
        int bucket = angleBucket();
        String key = exhaustFrameIndex + ":" + bucket;
        return EXHAUST_ROT_CACHE.computeIfAbsent(key,
                ignored -> rotate(EXHAUST_UP[exhaustFrameIndex], bucket));
    }

    public int getExhaustX() {
        Image exhaust = getExhaustImage();
        if (exhaust == null) {
            return x;
        }
        double radians = Math.toRadians(facingAngle);
        double forwardX = Math.sin(radians);
        double distanceBehind = Math.max(getImage().getWidth(null), getImage().getHeight(null)) * 0.48;
        double centerX = x + getImage().getWidth(null) / 2.0 - forwardX * distanceBehind;
        return (int) Math.round(centerX - exhaust.getWidth(null) / 2.0);
    }

    public int getExhaustY() {
        Image exhaust = getExhaustImage();
        if (exhaust == null) {
            return y;
        }
        double radians = Math.toRadians(facingAngle);
        double forwardY = -Math.cos(radians);
        double distanceBehind = Math.max(getImage().getWidth(null), getImage().getHeight(null)) * 0.48;
        double centerY = y + getImage().getHeight(null) / 2.0 - forwardY * distanceBehind;
        return (int) Math.round(centerY - exhaust.getHeight(null) / 2.0);
    }

    int getExhaustFrameIndex() {
        return exhaustFrameIndex;
    }

    public void configure(double speed, double drift, int hitPoints, int points) {
        fallSpeed = speed;
        driftSpeed = drift;
        health = Math.max(1, hitPoints);
        maxHealth = health;
        scoreValue = points;
    }

    public boolean hit() {
        health--;
        return health <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
