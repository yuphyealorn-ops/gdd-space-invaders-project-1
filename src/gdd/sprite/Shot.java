package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Player laser. Travels right, with an optional vertical component for
// multi-shot. A piercing variant is the Ultimate: a big beam that passes
// through every enemy.
public class Shot extends Sprite {

    private static final int SPEED = 13;
    private static Image laser;
    private static Image beam;

    private final int velocityX;
    private final int velocityY;
    private final boolean piercing;

    public Shot(int x, int y) {
        this(x, y, 0, false);
    }

    public Shot(int x, int y, int velocityY) {
        this(x, y, velocityY, false);
    }

    public Shot(int x, int y, int velocityY, boolean piercing) {
        this.velocityX = piercing ? SPEED + 3 : SPEED;
        this.velocityY = velocityY;
        this.piercing = piercing;
        setImage(piercing ? beam() : laser());
        setX(x);
        setY(y - (piercing ? getImage().getHeight(null) / 2 : 0));
    }

    public boolean isPiercing() {
        return piercing;
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

    private static Image beam() {
        if (beam == null) {
            BufferedImage img = new BufferedImage(120, 46, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 210, 90, 70));
            g.fillRoundRect(0, 4, 120, 38, 20, 20);
            g.setColor(new Color(255, 235, 150, 180));
            g.fillRoundRect(0, 13, 120, 20, 14, 14);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 19, 120, 8, 6, 6);
            g.dispose();
            beam = img;
        }
        return beam;
    }

    @Override
    public void act() {
        x += velocityX;
        y += velocityY;
    }
}
