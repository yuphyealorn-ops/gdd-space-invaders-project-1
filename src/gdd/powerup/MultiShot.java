package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y);
        setImage(icon());
    }

    // Procedural 48x48 icon (magenta multi-shot badge).
    private static BufferedImage icon() {
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(60, 20, 50));
        g.fillOval(2, 2, 44, 44);
        g.setColor(new Color(255, 90, 200));
        g.setStroke(new java.awt.BasicStroke(3f));
        g.drawOval(3, 3, 42, 42);
        g.setColor(new Color(255, 150, 220));
        g.fillRoundRect(12, 12, 16, 5, 4, 4);
        g.fillRoundRect(12, 21, 22, 5, 4, 4);
        g.fillRoundRect(12, 30, 16, 5, 4, 4);
        g.dispose();
        return img;
    }

    @Override
    public void act() {
        this.x -= 2;
        if (this.x < -50) {
            die();
        }
    }

    @Override
    public void upgrade(Player player) {
        player.enableMultiShot();
        this.die();
    }
}
