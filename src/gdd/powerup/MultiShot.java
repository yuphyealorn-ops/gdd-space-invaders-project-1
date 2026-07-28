package gdd.powerup;

import gdd.sprite.Player;

public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y);
        setImage(iconFrom(360, 148, 127, 79));
    }

    @Override
    public void upgrade(Player player) {
        player.enableMultiShot();
        this.die();
    }
}
