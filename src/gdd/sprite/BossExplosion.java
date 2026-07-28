package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Boss death explosion: 14 incremental frames across three sheets, played fast,
// centred on the boss.
public class BossExplosion extends Sprite {

    private final Image[] frames;
    private final int centerX;
    private final int centerY;
    private int frameIndex;
    private int tick;

    public BossExplosion(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.frames = build();
        setImage(frames[0]);
    }

    private static Image[] build() {
        BufferedImage s1 = loadSheet(IMG_BOSS_EXPLOSION);
        BufferedImage s2 = loadSheet(IMG_BOSS_EXPLOSION2);
        BufferedImage s3 = loadSheet(IMG_BOSS_EXPLOSION3);
        int[][] a = {
            {304, 80, 32, 35}, {408, 57, 74, 79}, {35, 10, 112, 118}, {104, 98, 168, 158},
            {497, 147, 56, 58}, {599, 130, 154, 142}, {30, 304, 186, 168},
            {255, 296, 218, 190}, {512, 281, 241, 219}
        };
        int[][] b = {{406, 3, 233, 212}, {40, 211, 245, 235}, {387, 229, 262, 236}};
        int[][] c = {{80, 136, 308, 288}, {416, 134, 296, 298}};
        Image[] f = new Image[a.length + b.length + c.length];
        int i = 0;
        for (int[] r : a) f[i++] = clip(s1, r[0], r[1], r[2], r[3], 1, false);
        for (int[] r : b) f[i++] = clip(s2, r[0], r[1], r[2], r[3], 1, false);
        for (int[] r : c) f[i++] = clip(s3, r[0], r[1], r[2], r[3], 1, false);
        return f;
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
    public void act() {
        tick++;
        if (tick % 3 == 0) { // fast
            frameIndex++;
            if (frameIndex >= frames.length) {
                die();
                frameIndex = frames.length - 1;
            } else {
                setImage(frames[frameIndex]);
            }
        }
    }
}
