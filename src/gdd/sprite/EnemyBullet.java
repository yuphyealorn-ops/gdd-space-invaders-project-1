package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class EnemyBullet extends Sprite {
    private final double velocityX;
    private final double velocityY;
    private double preciseX;
    private double preciseY;

    public EnemyBullet(int x, int y, double velocityX, double velocityY) {
        this.preciseX = x;
        this.preciseY = y;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;

        BufferedImage bullet = new BufferedImage(9, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bullet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 50, 155, 70));
        g.fillOval(0, 0, 9, 18);
        g.setColor(new Color(255, 90, 190));
        g.fillRoundRect(3, 2, 3, 14, 3, 3);
        g.setColor(Color.WHITE);
        g.fillRect(4, 4, 1, 7);
        g.dispose();
        setImage(bullet);
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
    }
}
