package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Player laser (from sprites.png). Points along its travel direction. The
// piercing variant is the Ultimate: a large accelerating beam (ultimate_shot
// frames) that passes through every enemy.
public class Shot extends Sprite {

    private static final int SPEED = 12;
    private static final int NORMAL_FRAME_HOLD_TICKS = 3;
    private static final int[][] NORMAL_RECTS = {
        {290, 38, 4, 4},
        {280, 37, 8, 6},
        {266, 37, 12, 6}
    };
    private static BufferedImage[] shotFrames;
    private static Image[] ultFrames;
    private static final Map<String, Image> ROT = new HashMap<>();

    private int velocityX;
    private final int velocityY;
    private final boolean piercing;
    private final int rotDeg;
    private int animTick;
    private int normalFrameIndex;
    private boolean bossHit;

    public Shot(int x, int y) {
        this(x, y, 0, false);
    }

    public Shot(int x, int y, int velocityY) {
        this(x, y, velocityY, false);
    }

    public Shot(int x, int y, int velocityY, boolean piercing) {
        this.velocityY = velocityY;
        this.piercing = piercing;
        this.velocityX = piercing ? 6 : SPEED; // ultimate starts slow then accelerates
        int deg = (int) Math.round(Math.toDegrees(Math.atan2(velocityY, this.velocityX)));
        this.rotDeg = ((deg % 360) + 360) % 360;
        setImage(piercing ? ultFrames()[0] : rotatedNormalFrame(0));
        setX(x);
        setY(y - getImage().getHeight(null) / 2);
    }

    public boolean isPiercing() {
        return piercing;
    }

    public boolean hasHitBoss() {
        return bossHit;
    }

    public void markBossHit() {
        bossHit = true;
    }

    private static BufferedImage[] shotFrames() {
        if (shotFrames == null) {
            BufferedImage sheet = loadSheet(IMG_PLAYER_SHEET);
            shotFrames = new BufferedImage[NORMAL_RECTS.length];
            for (int i = 0; i < NORMAL_RECTS.length; i++) {
                int[] r = NORMAL_RECTS[i];
                shotFrames[i] = scaleImage(sheet.getSubimage(r[0], r[1], r[2], r[3]),
                        r[2] * PLAYER_SCALE, r[3] * PLAYER_SCALE);
            }
        }
        return shotFrames;
    }

    private static Image[] ultFrames() {
        if (ultFrames == null) {
            BufferedImage sheet = loadSheet(IMG_PLAYER_SHEET);
            // order: before-leaving (2), accelerate (1), travel (3)
            int[][] r = {{328, 11, 24, 10}, {354, 11, 23, 10}, {377, 11, 23, 12}};
            ultFrames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                ultFrames[i] = scaleImage(sheet.getSubimage(r[i][0], r[i][1], r[i][2], r[i][3]),
                        r[i][2] * 5, r[i][3] * 5); // large beam
            }
        }
        return ultFrames;
    }

    private Image rotatedNormalFrame(int frameIndex) {
        BufferedImage base = shotFrames()[frameIndex];
        if (rotDeg == 0) {
            return base;
        }
        String key = frameIndex + ":" + rotDeg;
        return ROT.computeIfAbsent(key, k -> rotate(base, rotDeg));
    }

    private void applyNormalFrame(int frameIndex) {
        Image nextImage = rotatedNormalFrame(frameIndex);
        Image previousImage = getImage();
        if (previousImage != null) {
            x += previousImage.getWidth(null) / 2 - nextImage.getWidth(null) / 2;
            y += previousImage.getHeight(null) / 2 - nextImage.getHeight(null) / 2;
        }
        setImage(nextImage);
    }

    int getNormalFrameIndex() {
        return normalFrameIndex;
    }

    @Override
    public void act() {
        x += velocityX;
        y += velocityY;
        animTick++;
        if (piercing) {
            velocityX = Math.min(30, velocityX + 1); // accelerate slow -> fast
            setImage(ultFrames()[Math.min(2, animTick / 6)]);
        } else {
            normalFrameIndex = Math.min(shotFrames().length - 1,
                    animTick / NORMAL_FRAME_HOLD_TICKS);
            applyNormalFrame(normalFrameIndex);
        }
    }
}
