package gdd.powerup;

import gdd.sprite.Player;

public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        setImage(iconFrom(213, 242, 130, 80));
    }

    @Override
    public void upgrade(Player player) {
        player.setSpeed(player.getSpeed() + 2);
        this.die();
    }
}
