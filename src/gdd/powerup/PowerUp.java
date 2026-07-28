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

    private static Image speedBar;
    private static Image multiBar;
    private static Image ultBar;

    // Clip an icon out of powerups.png and scale it to pickup size.
    protected static Image iconFrom(int x, int y, int w, int h) {
        return iconAt(x, y, w, h, 46);
    }

    private static Image iconAt(int x, int y, int w, int h, int th) {
        BufferedImage sheet = loadSheet(IMG_POWERUPS);
        x = Math.max(0, Math.min(x, sheet.getWidth() - 1));
        y = Math.max(0, Math.min(y, sheet.getHeight() - 1));
        w = Math.max(1, Math.min(w, sheet.getWidth() - x));
        h = Math.max(1, Math.min(h, sheet.getHeight() - y));
        int tw = Math.max(1, w * th / h);
        return scaleImage(sheet.getSubimage(x, y, w, h), tw, th);
    }

    // Small icons for the bottom power-up bar.
    public static Image speedBarIcon() {
        if (speedBar == null) speedBar = iconAt(213, 242, 130, 80, 34);
        return speedBar;
    }

    public static Image multiBarIcon() {
        if (multiBar == null) multiBar = iconAt(360, 148, 127, 79, 34);
        return multiBar;
    }

    public static Image ultBarIcon() {
        if (ultBar == null) ultBar = iconAt(363, 333, 128, 81, 34);
        return ultBar;
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
