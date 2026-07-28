package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Stage-3 boss, clipped from boss_sprite.png (6 forms) and flipped to face the
// player. Enters from the right, strafes vertically, evolves its form as its
// health drops. (Boss projectiles are left for the user to implement.)
public class Boss extends Sprite {

    private final Image[] frames = new Image[6];
    private final int maxHp;
    private int hp;
    private double preciseX;
    private double preciseY;
    private double holdX;
    private double phase;
    private int tick;
    private boolean entering = true;

    public Boss() {
        BufferedImage sheet = loadSheet(IMG_BOSS);
        int[][] r = {
            {137, 193, 244, 214}, {560, 215, 268, 164}, {989, 231, 296, 165},
            {271, 419, 297, 189}, {855, 414, 278, 201}, {358, 664, 288, 201}
        };
        for (int i = 0; i < r.length; i++) {
            frames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, true); // flip to face left
        }
        maxHp = 90;
        hp = maxHp;
        setImage(frames[0]);
        int w = frames[0].getWidth(null);
        preciseX = BOARD_WIDTH + 60;
        holdX = BOARD_WIDTH - w - 30;
        preciseY = BOARD_HEIGHT / 2.0 - frames[0].getHeight(null) / 2.0;
        x = (int) preciseX;
        y = (int) preciseY;
    }

    @Override
    public void act() {
        tick++;
        phase += 0.03;
        if (entering) {
            preciseX -= 2.4;
            if (preciseX <= holdX) {
                preciseX = holdX;
                entering = false;
            }
        } else {
            preciseX = holdX + Math.sin(phase * 0.7) * 24;
        }
        preciseY = (BOARD_HEIGHT / 2.0 - getImage().getHeight(null) / 2.0)
                + Math.sin(phase) * (BOARD_HEIGHT * 0.28);
        x = (int) preciseX;
        y = (int) preciseY;
        setImage(currentFrame());
    }

    private Image currentFrame() {
        if (hp <= maxHp * 0.2) {
            return frames[5]; // enraged
        }
        if (hp <= maxHp * 0.5) {
            return frames[4]; // armored
        }
        return frames[(tick / 14) % 3]; // idle / weapon-deploy cycle
    }

    public boolean hit(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            die();
            return true;
        }
        return false;
    }

    public boolean isEntering() {
        return entering;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getWidth() {
        return getImage().getWidth(null);
    }

    public int getHeight() {
        return getImage().getHeight(null);
    }
}
