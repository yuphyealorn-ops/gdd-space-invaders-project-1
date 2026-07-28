package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import gdd.sprite.Sprite;
import java.awt.Image;
import java.awt.image.BufferedImage;

abstract public class PowerUp extends Sprite {

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Clip an icon out of powerups.png and scale it to pickup size.
    protected static Image iconFrom(int x, int y, int w, int h) {
        BufferedImage sheet = loadSheet(IMG_POWERUPS);
        x = Math.max(0, Math.min(x, sheet.getWidth() - 1));
        y = Math.max(0, Math.min(y, sheet.getHeight() - 1));
        w = Math.max(1, Math.min(w, sheet.getWidth() - x));
        h = Math.max(1, Math.min(h, sheet.getHeight() - y));
        BufferedImage sub = sheet.getSubimage(x, y, w, h);
        int th = 46;
        int tw = Math.max(1, w * th / h);
        return scaleImage(sub, tw, th);
    }

    @Override
    public void act() {
        this.x -= 2; // drift left toward the player
        if (this.x < -70) {
            die();
        }
    }

    abstract public void upgrade(Player player);
}
