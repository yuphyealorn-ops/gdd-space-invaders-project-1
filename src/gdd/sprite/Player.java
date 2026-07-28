package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

// Side-scroll hero ship: starts on the left, faces right, animates from
// sprites.png (idle flicker + up/down bank + death frames).
public class Player extends Sprite {

    private static final int START_X = 60;
    private static final int START_Y = BOARD_HEIGHT / 2 - 14;
    private static final int ULT_CHARGE_PER_DAMAGE = 5;

    private final Image idle1;
    private final Image idle2;
    private final Image bankUp;
    private final Image bankDown;
    private final Image[] death = new Image[3];

    private int currentSpeed = PLAYER_START_SPEED;
    private boolean multiShot = false;
    private int shotLevel = 1;
    private int verticalSpeed;
    private int animTick;
    private int ultCharge;

    public Player() {
        BufferedImage sheet = loadSheet(IMG_PLAYER_SHEET);
        idle1 = clip(sheet, 24, 10, 24, 14, PLAYER_SCALE, false);
        idle2 = clip(sheet, 56, 10, 24, 14, PLAYER_SCALE, false);
        bankUp = clip(sheet, 88, 10, 24, 14, PLAYER_SCALE, false);
        bankDown = clip(sheet, 120, 10, 24, 14, PLAYER_SCALE, false);
        death[0] = clip(sheet, 24, 58, 24, 17, PLAYER_SCALE, false);
        death[1] = clip(sheet, 56, 57, 24, 18, PLAYER_SCALE, false);
        death[2] = clip(sheet, 88, 58, 16, 17, PLAYER_SCALE, false);
        setImage(idle1);
        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {
        this.currentSpeed = Math.max(1, Math.min(PLAYER_MAX_SPEED, speed));
        return currentSpeed;
    }

    public void enableMultiShot() {
        multiShot = true;
        shotLevel = Math.min(4, shotLevel + 1);
    }

    public int getShotLevel() {
        return shotLevel;
    }

    public void gainDamageCharge(int damage) {
        if (damage <= 0) {
            return;
        }
        long chargedAmount = (long) damage * ULT_CHARGE_PER_DAMAGE;
        ultCharge = (int) Math.max(0L, Math.min(100L, ultCharge + chargedAmount));
    }

    public void gainBossDamageCharge(int damage) {
        gainDamageCharge(damage);
    }

    public boolean ultReady() {
        return ultCharge >= 100;
    }

    public void resetUlt() {
        ultCharge = 0;
    }

    public int getUltCharge() {
        return ultCharge;
    }

    public void resetPosition() {
        setX(START_X);
        setY(START_Y);
        dx = 0;
        verticalSpeed = 0;
        setDying(false);
        setVisible(true);
    }

    public boolean hasMultiShot() {
        return multiShot;
    }

    public Image getDeathFrame(int i) {
        return death[Math.max(0, Math.min(2, i))];
    }

    public int getWidth() {
        return getImage() == null ? 48 : getImage().getWidth(null);
    }

    public int getHeight() {
        return getImage() == null ? 28 : getImage().getHeight(null);
    }

    @Override
    public void act() {
        x += dx;
        y += verticalSpeed;

        if (x < 6) {
            x = 6;
        }
        if (x > BOARD_WIDTH / 2) {
            x = BOARD_WIDTH / 2;
        }
        if (y < 40) {
            y = 40;
        }
        if (y > BOARD_HEIGHT - getHeight() - 12) {
            y = BOARD_HEIGHT - getHeight() - 12;
        }

        animTick++;
        if (verticalSpeed < 0) {
            setImage(bankUp);
        } else if (verticalSpeed > 0) {
            setImage(bankDown);
        } else {
            setImage(animTick % 16 < 8 ? idle1 : idle2);
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            dx = -currentSpeed;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            dx = currentSpeed;
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            verticalSpeed = -currentSpeed;
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            verticalSpeed = currentSpeed;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT
                || key == KeyEvent.VK_A || key == KeyEvent.VK_D) {
            dx = 0;
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN
                || key == KeyEvent.VK_W || key == KeyEvent.VK_S) {
            verticalSpeed = 0;
        }
    }
}
