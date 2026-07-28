package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        setImage(icon());
    }

    // Procedural 48x48 icon (green speed badge).
    private static BufferedImage icon() {
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20, 60, 40));
        g.fillOval(2, 2, 44, 44);
        g.setColor(new Color(60, 240, 140));
        g.setStroke(new java.awt.BasicStroke(3f));
        g.drawOval(3, 3, 42, 42);
        g.setColor(new Color(120, 255, 180));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString(">>", 8, 33);
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
        player.setSpeed(player.getSpeed() + 2);
        this.die();
    }
}
