package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

// Side-scroll enemy: enters from the right, flies left toward the player,
// weaving vertically. Uses Enemy1.png rotated to face left.
public class Enemy extends Sprite {

    private static Image enemyImage;

    protected double preciseX;
    protected double preciseY;
    protected double fallSpeed = 1.0;   // leftward speed in side-scroll
    protected double driftSpeed;        // vertical drift
    protected double weavePhase;
    protected int health = 1;
    protected int scoreValue = 100;

    public Enemy(int x, int y) {
        initEnemy(x, y);
    }

    private static Image enemyImage() {
        if (enemyImage == null) {
            BufferedImage src = loadSheet(IMG_ENEMY1);
            BufferedImage rotated = rotate(src, 90); // Enemy1 faces down -> face left
            int targetH = 56;
            int targetW = Math.max(1, rotated.getWidth() * targetH / rotated.getHeight());
            enemyImage = scaleImage(rotated, targetW, targetH);
        }
        return enemyImage;
    }

    private void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.preciseX = x;
        this.preciseY = y;
        this.weavePhase = Math.random() * Math.PI * 2;
        setImage(enemyImage());
    }

    public void act(int direction) {
        weavePhase += 0.05;
        preciseX -= fallSpeed;
        preciseY += driftSpeed + Math.sin(weavePhase) * 0.7;
        if (preciseY < 30) {
            preciseY = 30;
        }
        if (preciseY > BOARD_HEIGHT - 70) {
            preciseY = BOARD_HEIGHT - 70;
        }
        this.x = (int) preciseX;
        this.y = (int) preciseY;
    }

    public void configure(double speed, double drift, int hitPoints, int points) {
        fallSpeed = speed;
        driftSpeed = drift;
        health = Math.max(1, hitPoints);
        scoreValue = points;
    }

    public boolean hit() {
        health--;
        return health <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
