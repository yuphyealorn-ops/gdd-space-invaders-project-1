package gdd.sprite;

public class Alien1 extends Enemy {

    public Alien1(int x, int y) {
        super(x, y, 1);
    }

    public Alien1(int x, int y, double speed, double drift, int hitPoints, int points, int kind) {
        super(x, y, kind);
        configure(speed, drift, hitPoints, points);
    }

    @Override
    public void act(int px, int py) {
        super.act(px, py);
    }
}
