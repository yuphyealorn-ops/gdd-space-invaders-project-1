package gdd.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

abstract public class Sprite {

    protected boolean visible;
    protected Image image;
    protected boolean dying;
    protected int visibleFrames = 10;

    protected int x;
    protected int y;
    protected int dx;

    private static final Map<String, BufferedImage> SHEET_CACHE = new HashMap<>();

    public Sprite() {
        visible = true;
    }

    protected Image loadScaledImage(String path, int scaleFactor) {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            int width = source.getWidth() * scaleFactor;
            int height = source.getHeight() * scaleFactor;
            return scaleImage(source, width, height);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load image: " + path, e);
        }
    }

    // Loads (and caches) a sprite sheet so we can clip frames from it.
    protected static BufferedImage loadSheet(String path) {
        return SHEET_CACHE.computeIfAbsent(path, p -> {
            try {
                return ImageIO.read(new File(p));
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load sheet: " + p, e);
            }
        });
    }

    // Clip a (x,y,w,h) rectangle out of a sheet, scale it, optionally flip horizontally.
    protected static Image clip(BufferedImage sheet, int sx, int sy, int sw, int sh, int scale, boolean flipH) {
        sx = Math.max(0, Math.min(sx, sheet.getWidth() - 1));
        sy = Math.max(0, Math.min(sy, sheet.getHeight() - 1));
        sw = Math.max(1, Math.min(sw, sheet.getWidth() - sx));
        sh = Math.max(1, Math.min(sh, sheet.getHeight() - sy));
        BufferedImage frame = sheet.getSubimage(sx, sy, sw, sh);
        if (flipH) {
            frame = flipHorizontal(frame);
        }
        return scaleImage(frame, sw * scale, sh * scale);
    }

    protected static BufferedImage flipHorizontal(BufferedImage src) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-src.getWidth(), 0);
        return new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
                .filter(src, null);
    }

    protected static BufferedImage rotate(BufferedImage src, double degrees) {
        double rad = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.translate((newW - w) / 2, (newH - h) / 2);
        g.rotate(rad, w / 2.0, h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    protected static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(Math.max(1, width), Math.max(1, height),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(source, 0, 0, Math.max(1, width), Math.max(1, height), null);
        g2d.dispose();
        return scaled;
    }

    // Default empty action. Subclasses can override it when needed.
    public void act() {
    }

    // Required by the assignment: collision with another Sprite.
    public boolean collideWithOther(Sprite other) {
        if (other == null || !this.isVisible() || !other.isVisible()) {
            return false;
        }

        Image thisImage = this.getImage();
        Image otherImage = other.getImage();
        if (thisImage == null || otherImage == null) {
            return false;
        }

        int thisWidth = thisImage.getWidth(null);
        int thisHeight = thisImage.getHeight(null);
        int otherWidth = otherImage.getWidth(null);
        int otherHeight = otherImage.getHeight(null);

        return this.getX() < other.getX() + otherWidth
                && this.getX() + thisWidth > other.getX()
                && this.getY() < other.getY() + otherHeight
                && this.getY() + thisHeight > other.getY();
    }

    // Old method name kept so previous code still works.
    public boolean collidesWith(Sprite other) {
        return collideWithOther(other);
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void visibleCountDown() {
        if (visibleFrames > 0) {
            visibleFrames--;
        } else {
            visible = false;
        }
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public boolean isDying() {
        return this.dying;
    }
}
