package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Enemy projectiles (travel left toward the player, in Scene1's separate list):
//  - BULLET: fast, 20% dmg. Small travel sprite from EnemySpriteEffect.png,
//            rotated to point along its travel direction (fixed at fire time).
//  - BOMB:   slow, 30% dmg. enemy_projectile.png frames (full size) played in
//            order over its trip across the board. The sheet frames contain
//            their own fade; animation completion does not end the projectile.
public class EnemyBullet extends Sprite {

    private static final int BOMB_ANIMATION_END_X = -40;
    private static Image[] bombFrames;
    private static BufferedImage travelBase;
    private static final Map<String, Image> ROT = new HashMap<>();

    private final double velocityX;
    private final double velocityY;
    private final boolean bomb;
    private final int rotDeg;
    private final double startX;
    private double preciseX;
    private double preciseY;
    private int frameIndex;

    public EnemyBullet(int x, int y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, false);
    }

    public EnemyBullet(int x, int y, double velocityX, double velocityY, boolean bomb) {
        this.preciseX = x;
        this.preciseY = y;
        this.startX = x;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.bomb = bomb;
        int deg = (int) Math.round(Math.toDegrees(Math.atan2(velocityY, velocityX)) - 180);
        this.rotDeg = ((deg % 360) + 360) % 360;
        setImage(bomb ? bombFrames()[0] : rotatedTravel());
    }

    private static Image[] bombFrames() {
        if (bombFrames == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_PROJECTILE);
            int[][] r = {
                {18, 10, 67, 64}, {124, 12, 65, 62}, {230, 12, 61, 62},
                {23, 119, 59, 55}, {129, 119, 54, 54}, {237, 121, 47, 49},
                {30, 228, 41, 44}, {136, 230, 38, 38}, {243, 233, 35, 34}
            };
            bombFrames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                bombFrames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, false); // full size
            }
        }
        return bombFrames;
    }

    private static BufferedImage travelBase() {
        if (travelBase == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_EFFECT);
            travelBase = scaleImage(sheet.getSubimage(580, 536, 129, 27), 24, 5); // small bullet
        }
        return travelBase;
    }

    private Image rotatedTravel() {
        if (rotDeg == 0) {
            return travelBase();
        }
        return ROT.computeIfAbsent(String.valueOf(rotDeg), k -> rotate(travelBase(), rotDeg));
    }

    public boolean isPlasma() {
        return bomb;
    }

    public int getDamage() {
        return bomb ? DMG_PLASMA : DMG_BULLET;
    }

    public float getAlpha() {
        return 1f;
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
        if (bomb) {
            double travelDistance = Math.max(1.0, startX - BOMB_ANIMATION_END_X);
            double progress = Math.max(0.0,
                    Math.min(1.0, (startX - preciseX) / travelDistance));
            frameIndex = Math.min(bombFrames().length - 1,
                    (int) (progress * bombFrames().length));
            setImage(bombFrames()[frameIndex]);
        } else {
            setImage(rotatedTravel());
        }
        if (x < -80 || y < -80 || y > BOARD_HEIGHT + 80) {
            die();
        }
    }

    int getFrameIndex() {
        return frameIndex;
    }
}
