package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private Bomb bomb;

    public Alien1(int x, int y) {
        super(x, y);
    }

    public Alien1(int x, int y, double speed, double drift, int hitPoints, int points) {
        super(x, y);
        configure(speed, drift, hitPoints, points);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        setImage(loadScaledImage(IMG_ENEMY, SCALE_FACTOR));
    }

    public void act(int direction) {
        super.act(direction);
        if (x < 5 || x > BOARD_WIDTH - 45) {
            driftSpeed = -driftSpeed;
            preciseX = Math.max(5, Math.min(BOARD_WIDTH - 45, preciseX));
        }
    }

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
}
