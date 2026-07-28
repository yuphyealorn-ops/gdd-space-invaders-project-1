package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Stage-3 boss from boss_sprites.png (2048x448, four frames in a row). Enters
// from the right to a hold position, then drifts vertically. (Boss projectiles
// are left for the user to implement.)
public class Boss extends Sprite {

    private final Image[] frames = new Image[4];
    private final int maxHp;
    private int hp;
    private double preciseX;
    private double preciseY;
    private final double holdX;
    private double phase;
    private int tick;
    private boolean entering = true;

    public Boss() {
        BufferedImage sheet = loadSheet(IMG_BOSS);
        int fw = sheet.getWidth() / 4; // 512
        int fh = sheet.getHeight();    // 448
        int targetW = 205;
        int targetH = Math.max(1, fh * targetW / fw);
        for (int i = 0; i < 4; i++) {
            frames[i] = scaleImage(sheet.getSubimage(i * fw, 0, fw, fh), targetW, targetH);
        }
        maxHp = 90;
        hp = maxHp;
        setImage(frames[0]);
        preciseX = BOARD_WIDTH + 120;      // 836
        preciseY = BOARD_HEIGHT / 2.0 - 55; // 295
        holdX = BOARD_WIDTH - 205;          // 511
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
            preciseY = 330 + Math.sin(phase) * 215; // drift between y=115 and y=545
        }
        x = (int) preciseX;
        y = (int) preciseY;
        setImage(frames[(tick / 12) % 4]);
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
