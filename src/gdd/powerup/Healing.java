package gdd.powerup;

import gdd.sprite.Player;

// Only spawned when the player is below max HP (Scene1 decides). The actual
// heal is applied by Scene1 (which owns the HP value).
public class Healing extends PowerUp {

    public Healing(int x, int y) {
        super(x, y);
        setImage(iconFrom(213, 149, 130, 80));
    }

    @Override
    public void upgrade(Player player) {
        this.die();
    }
}
