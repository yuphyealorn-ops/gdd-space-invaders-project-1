package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Animated explosion clipped from enemy_explosion.png (7 expanding frames),
// centered on the point it is spawned at.
public class Explosion extends Sprite {

    private static Image[] frames;

    private final int centerX;
    private final int centerY;
    private int frameIndex;
    private int tick;

    public Explosion(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.visibleFrames = 100;
        setImage(frames()[0]);
    }

    private static Image[] frames() {
        if (frames == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_EXPLOSION);
            int[][] r = {
                {62, 70, 70, 69}, {223, 36, 129, 112}, {397, 19, 167, 152},
                {580, 0, 182, 182}, {768, 0, 177, 176}, {11, 208, 173, 160},
                {210, 216, 162, 152}
            };
            frames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                frames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, false);
            }
        }
        return frames;
    }

    @Override
    public int getX() {
        return centerX - getImage().getWidth(null) / 2;
    }

    @Override
    public int getY() {
        return centerY - getImage().getHeight(null) / 2;
    }

    @Override
    public void visibleCountDown() {
        tick++;
        if (tick % 4 == 0) {
            frameIndex++;
            if (frameIndex >= frames().length) {
                visible = false;
                frameIndex = frames().length - 1;
            } else {
                setImage(frames()[frameIndex]);
            }
        }
    }
}
