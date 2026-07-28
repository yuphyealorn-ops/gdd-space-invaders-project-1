package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3;
    public static final int PLAYER_SCALE = 2;

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

    // Side-scroll gameplay tuning
    public static final int PLAYER_MAX_HP = 100;
    public static final int PLAYER_LIVES = 3;
    public static final int DMG_PLASMA = 30;
    public static final int DMG_BULLET = 20;
    public static final int DMG_CONTACT = 20;

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_PLAYER = "src/images/player.png";
    public static final String IMG_PLAYER_SHEET = "src/images/sprites.png";
    public static final String IMG_ENEMY1 = "src/images/Enemy1.png";
    public static final String IMG_ENEMY_SHEET = "src/images/EnemySprite.png";
    public static final String IMG_ENEMY_EFFECT = "src/images/EnemySpriteEffect.png";
    public static final String IMG_POWERUPS = "src/images/powerups.png";
    public static final String IMG_ENEMY_PROJECTILE = "src/images/enemy_projectile.png";
    public static final String IMG_ENEMY_EXPLOSION = "src/images/enemy_explosion.png";
    public static final String IMG_BOSS = "src/images/boss_sprites.png";
    public static final String IMG_BOSS_ATTACK = "src/images/boss_attack.png";
    public static final String IMG_BOSS_EXPLOSION = "src/images/boss_explosion.png";
    public static final String IMG_BOSS_EXPLOSION2 = "src/images/boss_explosion2.png";
    public static final String IMG_BOSS_EXPLOSION3 = "src/images/boss_explosion3.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/TitleScreen.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_POWERUP_MULTISHOT = "src/images/powerup-m.png";

    // External data files
    public static final String FONT_MAIN = "src/font/ThaleahFat.ttf";
    public static final String MAP_STARS = "src/maps/maps.txt";
    public static final String MAP_BACKGROUND_CSV = "src/maps/background.csv";
    public static final String SPAWN_CSV = "src/maps/spawns.csv";

    // Audio cues
    public static final String SFX_START = "src/audio/start.wav";
    public static final String SFX_SHOOT = "src/audio/shoot.wav";
    public static final String SFX_POWERUP = "src/audio/powerup.wav";
    public static final String SFX_PLAYER_HIT = "src/audio/player_hit.wav";
    public static final String SFX_EXPLOSION = "src/audio/explosion.wav";
    public static final String SFX_GAME_OVER = "src/audio/game_over.wav";
    public static final String SFX_BOSS_HIT = "src/audio/boss_hit.wav";
    public static final String SFX_BOSS_FIGHT = "src/audio/boss_fight.wav";
    public static final String SFX_BOSS_DEATH = "src/audio/boss_death.wav";
    public static final String MUSIC_STAGE = "src/audio/scene1.wav";
    public static final String MUSIC_STAGE2 = "src/audio/scene2.wav";
}
