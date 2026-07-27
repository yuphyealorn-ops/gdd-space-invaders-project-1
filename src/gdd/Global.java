package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3;

    public static final int BOARD_WIDTH = 716;
    public static final int BOARD_HEIGHT = 700;
    public static final int BORDER_RIGHT = 60;
    public static final int BORDER_LEFT = 10;

    public static final int GROUND = 580;
    public static final int BOMB_HEIGHT = 10;

    public static final int ALIEN_HEIGHT = 24;
    public static final int ALIEN_WIDTH = 24;
    public static final int ALIEN_INIT_X = 300;
    public static final int ALIEN_INIT_Y = 10;
    public static final int ALIEN_GAP = 30;

    public static final int GO_DOWN = 30;
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 12;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 30;
    public static final int PLAYER_HEIGHT = 20;

    public static final int PLAYER_START_SPEED = 4;
    public static final int PLAYER_MAX_SPEED = 12;

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_PLAYER = "src/images/player.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_POWERUP_MULTISHOT = "src/images/powerup-m.png";
    public static final String IMG_BACKGROUND = "src/images/space-background-v2.png";

    // External data files
    public static final String MAP_BACKGROUND_CSV = "src/maps/background.csv";
    public static final String SPAWN_CSV = "src/maps/spawns.csv";
}
