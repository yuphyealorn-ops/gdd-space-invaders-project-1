package gdd.sprite;

public class Alien1 extends Enemy {

    public Alien1(int x, int y) {
        super(x, y);
    }

    public Alien1(int x, int y, double speed, double drift, int hitPoints, int points) {
        super(x, y);
        configure(speed, drift, hitPoints, points);
    }

    @Override
    public void act(int direction) {
        super.act(direction);
    }
}
