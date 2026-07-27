package gdd.sprite;

import static gdd.Global.*;

public class Enemy extends Sprite {

    protected double preciseX;
    protected double preciseY;
    protected double fallSpeed = 1.0;
    protected double driftSpeed;
    protected int health = 1;
    protected int scoreValue = 100;

    // private Bomb bomb;

    public Enemy(int x, int y) {

        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;
        this.preciseX = x;
        this.preciseY = y;

        // bomb = new Bomb(x, y);

        setImage(loadScaledImage(IMG_ENEMY, SCALE_FACTOR));
    }

    public void act(int direction) {
        preciseX += driftSpeed;
        preciseY += fallSpeed;
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
/* 
    public Bomb getBomb() {

        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {

            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) {

            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {

            return destroyed;
        }
    }
*/
}
