package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Enemy projectiles (both travel left toward the player, kept in Scene1's
// separate enemyBullets list):
//  - BULLET: fast, 20% damage. Uses EnemySpriteEffect.png (start flash -> travel).
//  - BOMB:   slow, 30% damage. Uses enemy_projectile.png frames played IN ORDER
//            once, then fades out after the final frame.
public class EnemyBullet extends Sprite {

    private static Image[] bombFrames;
    private static Image bulletStart;
    private static Image bulletTravel;

    private final double velocityX;
    private final double velocityY;
    private final boolean bomb;
    private double preciseX;
    private double preciseY;
    private int animTick;
    private float alpha = 1f;

    public EnemyBullet(int x, int y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, false);
    }

    public EnemyBullet(int x, int y, double velocityX, double velocityY, boolean bomb) {
        this.preciseX = x;
        this.preciseY = y;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.bomb = bomb;
        setImage(bomb ? bombFrames()[0] : bulletStart());
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
                bombFrames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, false);
            }
        }
        return bombFrames;
    }

    private static Image effect(int x, int y, int w, int h, int targetH) {
        BufferedImage sheet = loadSheet(IMG_ENEMY_EFFECT);
        BufferedImage sub = sheet.getSubimage(
                Math.max(0, Math.min(x, sheet.getWidth() - 1)),
                Math.max(0, Math.min(y, sheet.getHeight() - 1)),
                Math.max(1, Math.min(w, sheet.getWidth() - x)),
                Math.max(1, Math.min(h, sheet.getHeight() - y)));
        int tw = Math.max(1, sub.getWidth() * targetH / sub.getHeight());
        return scaleImage(sub, tw, targetH);
    }

    private static Image bulletStart() {
        if (bulletStart == null) {
            bulletStart = effect(311, 508, 194, 83, 40); // bullet starting flash
        }
        return bulletStart;
    }

    private static Image bulletTravel() {
        if (bulletTravel == null) {
            bulletTravel = effect(580, 536, 129, 27, 20); // bullet travel
        }
        return bulletTravel;
    }

    public boolean isPlasma() {
        return bomb;
    }

    public int getDamage() {
        return bomb ? DMG_PLASMA : DMG_BULLET;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
        animTick++;
        if (bomb) {
            int idx = animTick / 4;
            if (idx < bombFrames().length) {
                setImage(bombFrames()[idx]);
            } else {
                setImage(bombFrames()[bombFrames().length - 1]);
                alpha -= 0.03f; // start fading once the sequence finishes
                if (alpha <= 0f) {
                    die();
                }
            }
        } else {
            setImage(animTick < 7 ? bulletStart() : bulletTravel());
        }
        if (x < -70 || y < -70 || y > BOARD_HEIGHT + 70) {
            die();
        }
    }
}
