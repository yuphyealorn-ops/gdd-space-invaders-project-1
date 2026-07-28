package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Boss Attack #2: a flame aimed at a random point in the player's half of the
// board. The complete travel -> ignite -> fade sequence is paced to reach that
// point, then the flame keeps travelling until it has left the board.
public class BossFlame extends Sprite {

    public static final int DAMAGE_PER_TICK = 2;
    public static final int DAMAGE_INTERVAL_FRAMES = 30;

    private static final double SPEED = 4.0;
    private static final int OFFSCREEN_MARGIN = 20;
    private static Image[] frames;

    private final double velocityX;
    private final double velocityY;
    private final int animationTravelTicks;
    private double centerX;
    private double centerY;
    private int animTick;
    private int frameIndex;

    public BossFlame(int startCenterX, int startCenterY, int targetCenterX, int targetCenterY) {
        centerX = startCenterX;
        centerY = startCenterY;

        double dx = targetCenterX - startCenterX;
        double dy = targetCenterY - startCenterY;
        double distance = Math.max(1.0, Math.hypot(dx, dy));
        velocityX = dx / distance * SPEED;
        velocityY = dy / distance * SPEED;
        animationTravelTicks = Math.max(frames().length,
                (int) Math.ceil(distance / SPEED));

        applyFrame(0);
    }

    private static Image[] frames() {
        if (frames == null) {
            BufferedImage sheet = loadSheet(IMG_BOSS_ATTACK);
            int[][] rectangles = {
                {42, 193, 7, 8},
                {59, 189, 18, 12}, {86, 189, 29, 14}, {126, 189, 44, 12},
                {186, 189, 34, 9}, {243, 189, 20, 9},
                {281, 178, 23, 27}, {324, 172, 25, 26}, {371, 168, 35, 41},
                {430, 166, 40, 48}, {493, 167, 41, 51}, {555, 167, 42, 52},
                {616, 170, 39, 48}
            };
            frames = new Image[rectangles.length];
            for (int i = 0; i < rectangles.length; i++) {
                int[] r = rectangles[i];
                frames[i] = clip(sheet, r[0], r[1], r[2], r[3], 2, false);
            }
        }
        return frames;
    }

    public int getDamage() {
        return DAMAGE_PER_TICK;
    }

    @Override
    public void act() {
        centerX += velocityX;
        centerY += velocityY;
        animTick++;

        int nextFrame = Math.min(frames().length - 1,
                animTick * frames().length / animationTravelTicks);
        applyFrame(nextFrame);

        if (x + getImage().getWidth(null) < -OFFSCREEN_MARGIN
                || x > BOARD_WIDTH + OFFSCREEN_MARGIN
                || y + getImage().getHeight(null) < -OFFSCREEN_MARGIN
                || y > BOARD_HEIGHT + OFFSCREEN_MARGIN) {
            die();
        }
    }

    private void applyFrame(int index) {
        frameIndex = index;
        setImage(frames()[index]);
        x = (int) Math.round(centerX - getImage().getWidth(null) / 2.0);
        y = (int) Math.round(centerY - getImage().getHeight(null) / 2.0);
    }

    int getFrameIndex() {
        return frameIndex;
    }

    double getCenterX() {
        return centerX;
    }

    double getCenterY() {
        return centerY;
    }
}
