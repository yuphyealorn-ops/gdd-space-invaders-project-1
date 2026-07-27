package gdd.powerup;

import gdd.sprite.Player;
import gdd.sprite.Sprite;

abstract public class PowerUp extends Sprite {

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    abstract public void upgrade(Player player);
}
