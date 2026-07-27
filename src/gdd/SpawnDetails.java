package gdd;

public class SpawnDetails {
    public static final String ALIEN1 = "Alien1";
    public static final String SPEED_UP = "SpeedUp";
    public static final String MULTI_SHOT = "MultiShot";

    public String type;
    public int x;
    public int y;

    public SpawnDetails(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }
}
