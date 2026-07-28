package gdd.sprite;

import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Enemy projectile. Two kinds:
//  - BULLET: small, fast-ish, 20% damage.
//  - PLASMA: animated ball from enemy_projectile.png, slow, 30% damage, fades out over time.
// Both travel left toward the player and live in Scene1's separate enemyBullets list.
public class EnemyBullet extends Sprite {

    private static Image[] plasmaFrames;

    private final double velocityX;
    private final double velocityY;
    private final boolean plasma;
    private double preciseX;
    private double preciseY;
    private int life;
    private final int maxLife;
    private int animTick;

    public EnemyBullet(int x, int y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, false);
    }

    public EnemyBullet(int x, int y, double velocityX, double velocityY, boolean plasma) {
        this.preciseX = x;
        this.preciseY = y;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.plasma = plasma;
        this.maxLife = plasma ? 210 : 600;
        this.life = maxLife;
        if (plasma) {
            setImage(plasmaFrames()[0]);
        } else {
            setImage(makeBullet());
        }
    }

    private static Image[] plasmaFrames() {
        if (plasmaFrames == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_PROJECTILE);
            int[][] r = {
                {18, 10, 67, 64}, {124, 12, 65, 62}, {230, 12, 61, 62},
                {23, 119, 59, 55}, {129, 119, 54, 54}, {237, 121, 47, 49},
                {30, 228, 41, 44}, {136, 230, 38, 38}, {243, 233, 35, 34}
            };
            plasmaFrames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                plasmaFrames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, false);
            }
        }
        return plasmaFrames;
    }

    private static Image makeBullet() {
        BufferedImage bullet = new BufferedImage(16, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bullet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 50, 155, 80));
        g.fillOval(0, 0, 16, 8);
        g.setColor(new Color(255, 110, 200));
        g.fillRoundRect(2, 2, 12, 4, 3, 3);
        g.setColor(Color.WHITE);
        g.fillRect(3, 3, 6, 2);
        g.dispose();
        return bullet;
    }

    public boolean isPlasma() {
        return plasma;
    }

    public int getDamage() {
        return plasma ? DMG_PLASMA : DMG_BULLET;
    }

    public float getAlpha() {
        return Math.max(0f, Math.min(1f, life / (float) maxLife));
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
        life--;
        if (plasma) {
            animTick++;
            setImage(plasmaFrames()[(animTick / 4) % plasmaFrames().length]);
        }
        if (life <= 0) {
            die();
        }
    }
}
