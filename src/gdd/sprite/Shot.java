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
    private static BufferedImage shotBase;
    private static Image[] ultFrames;
    private static final Map<String, Image> ROT = new HashMap<>();

    private int velocityX;
    private final int velocityY;
    private final boolean piercing;
    private final int rotDeg;
    private int animTick;
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
        setImage(piercing ? ultFrames()[0] : rotated(shotBase()));
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

    private static BufferedImage shotBase() {
        if (shotBase == null) {
            BufferedImage sheet = loadSheet(IMG_PLAYER_SHEET);
            shotBase = scaleImage(sheet.getSubimage(280, 15, 8, 3), 20, 7); // player_shot, small
        }
        return shotBase;
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

    private Image rotated(BufferedImage base) {
        if (rotDeg == 0) {
            return base;
        }
        return ROT.computeIfAbsent(String.valueOf(rotDeg), k -> rotate(base, rotDeg));
    }

    @Override
    public void act() {
        x += velocityX;
        y += velocityY;
        animTick++;
        if (piercing) {
            velocityX = Math.min(30, velocityX + 1); // accelerate slow -> fast
            setImage(ultFrames()[Math.min(2, animTick / 6)]);
        }
    }
}
