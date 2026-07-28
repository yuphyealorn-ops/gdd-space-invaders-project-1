package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Player laser. Side-scroll: travels right, with an optional vertical
// component for multi-shot / 3-way spreads.
public class Shot extends Sprite {

    private static final int SPEED = 13;
    private static Image laser;

    private final int velocityX;
    private final int velocityY;

    public Shot(int x, int y) {
        this(x, y, 0);
    }

    public Shot(int x, int y, int velocityY) {
        this.velocityX = SPEED;
        this.velocityY = velocityY;
        setImage(laser());
        setX(x);
        setY(y);
    }

    private static Image laser() {
        if (laser == null) {
            BufferedImage img = new BufferedImage(22, 6, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(40, 220, 255, 90));
            g.fillRoundRect(0, 0, 22, 6, 6, 6);
            g.setColor(new Color(150, 245, 255));
            g.fillRoundRect(2, 1, 18, 4, 4, 4);
            g.setColor(Color.WHITE);
            g.fillRect(4, 2, 12, 2);
            g.dispose();
            laser = img;
        }
        return laser;
    }

    @Override
    public void act() {
        x += velocityX;
        y += velocityY;
    }
}
