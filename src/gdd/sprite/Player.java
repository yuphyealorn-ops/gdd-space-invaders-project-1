package gdd.sprite;

import static gdd.Global.*;
import java.awt.event.KeyEvent;

public class Player extends Sprite {

    private static final int START_X = 335;
    private static final int START_Y = 595;

    private int currentSpeed = PLAYER_START_SPEED;
    private boolean multiShot = false;
    private int verticalSpeed;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        setImage(loadScaledImage(IMG_PLAYER, SCALE_FACTOR));

        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {
        if (speed < 1) {
            speed = 1;
        }
        if (speed > PLAYER_MAX_SPEED) {
            speed = PLAYER_MAX_SPEED;
        }
        this.currentSpeed = speed;
        return currentSpeed;
    }

    public void enableMultiShot() {
        multiShot = true;
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

    @Override
    public void act() {
        x += dx;
        y += verticalSpeed;

        int playerWidth = getImage() == null ? PLAYER_WIDTH : getImage().getWidth(null);

        if (x <= 2) {
            x = 2;
        }

        if (x >= BOARD_WIDTH - playerWidth - 2) {
            x = BOARD_WIDTH - playerWidth - 2;
        }

        if (y < 90) {
            y = 90;
        }
        if (y > BOARD_HEIGHT - 90) {
            y = BOARD_HEIGHT - 90;
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
