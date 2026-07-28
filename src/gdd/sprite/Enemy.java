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
    private static final Map<String, Image> ROT_CACHE = new HashMap<>();

    private static final int HOLD_MIN_X = (int) (BOARD_WIDTH * 0.42);
    private static final int HOLD_MAX_X = BOARD_WIDTH - 90;

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
        int bucket = (int) (Math.round(facingAngle / 10.0) * 10) % 360;
        if (bucket < 0) bucket += 360;
        final int b = bucket;
        return ROT_CACHE.computeIfAbsent(kind + (health < maxHealth ? "c" : "n") + b,
                key -> rotate(base, b));
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
