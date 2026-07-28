package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Boss Attack #1: a plasma bomb aimed straight at the player when fired. It
// animates travel -> growing ball -> fade from boss_attack.png, moves faster
// than a normal enemy bomb, and deals high damage.
public class BossBullet extends Sprite {

    private static Image[] frames;

    private final double velocityX;
    private final double velocityY;
    private double preciseX;
    private double preciseY;
    private int animTick;

    public BossBullet(int x, int y, int targetX, int targetY) {
        this.preciseX = x;
        this.preciseY = y;
        this.x = x;
        this.y = y;
        double dx = targetX - x;
        double dy = targetY - y;
        double len = Math.max(1.0, Math.hypot(dx, dy));
        double speed = 5.5; // faster than the enemy bomb
        this.velocityX = dx / len * speed;
        this.velocityY = dy / len * speed;
        setImage(frames()[0]);
    }

    private static Image[] frames() {
        if (frames == null) {
            BufferedImage sheet = loadSheet(IMG_BOSS_ATTACK);
            int[][] r = {
                {66, 87, 13, 12}, {88, 87, 18, 14}, {118, 89, 17, 11}, {152, 91, 13, 7}, // travel
                {182, 91, 7, 7}, {205, 90, 11, 10}, {229, 86, 16, 17}, {261, 82, 26, 26},
                {309, 80, 31, 30}, {360, 76, 37, 38}, {415, 74, 42, 41}, {476, 71, 46, 47}, // grow
                {538, 70, 50, 49}, {603, 68, 53, 54} // fade
            };
            frames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                frames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 2, false);
            }
        }
        return frames;
    }

    public int getDamage() {
        return 35; // a little stronger than the enemy bomb (30)
    }

    @Override
    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
        animTick++;
        int idx = Math.min(frames().length - 1, animTick / 5);
        setImage(frames()[idx]);
        if (idx >= frames().length - 1 || x < -90 || x > BOARD_WIDTH + 90
                || y < -90 || y > BOARD_HEIGHT + 90) {
            die();
        }
    }
}
