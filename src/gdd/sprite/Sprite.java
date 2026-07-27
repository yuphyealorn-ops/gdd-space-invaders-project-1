package gdd.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

abstract public class Sprite {

    protected boolean visible;
    protected Image image;
    protected boolean dying;
    protected int visibleFrames = 10;

    protected int x;
    protected int y;
    protected int dx;

    public Sprite() {
        visible = true;
    }

    protected Image loadScaledImage(String path, int scaleFactor) {
        try {
            BufferedImage source = ImageIO.read(new File(path));
            int width = source.getWidth() * scaleFactor;
            int height = source.getHeight() * scaleFactor;
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(source, 0, 0, width, height, null);
            g2d.dispose();
            return scaled;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load image: " + path, e);
        }
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
