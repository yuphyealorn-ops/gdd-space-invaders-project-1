package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;

public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y);

        setImage(loadScaledImage(IMG_POWERUP_MULTISHOT, 1));
    }

    @Override
    public void act() {
        this.y += 2;
        if (this.y > BOARD_HEIGHT) {
            die();
        }
    }

    @Override
    public void upgrade(Player player) {
        player.enableMultiShot();
        this.die();
    }
}
