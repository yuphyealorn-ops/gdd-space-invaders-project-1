package gdd.sprite;

import static gdd.Global.*;

public class Shot extends Sprite {

    private static final int H_SPACE = 20;
    private static final int V_SPACE = 1;
    private final int velocityX;
    private final int velocityY;

    public Shot() {
        velocityX = 0;
        velocityY = -18;
    }

    public Shot(int x, int y) {
        this(x, y, 0, 0);
    }

    public Shot(int x, int y, int xOffset) {
        this(x, y, xOffset, xOffset == 0 ? 0 : xOffset / 9);
    }

    public Shot(int x, int y, int xOffset, int velocityX) {
        this.velocityX = velocityX;
        this.velocityY = -18;
        initShot(x, y, xOffset);
    }

    private void initShot(int x, int y, int xOffset) {
        setImage(loadScaledImage(IMG_SHOT, SCALE_FACTOR));

        setX(x + H_SPACE + xOffset);
        setY(y - V_SPACE);
    }

    @Override
    public void act() {
        x += velocityX;
        y += velocityY;
    }
}
