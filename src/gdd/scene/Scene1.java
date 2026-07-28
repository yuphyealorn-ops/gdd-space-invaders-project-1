package gdd.scene;

import gdd.AudioPlayer;
import gdd.Fonts;
import gdd.Game;
import gdd.GameMode;
import gdd.MapLoader;
import gdd.SoundFx;
import gdd.SpawnDetails;
import static gdd.Global.*;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyBullet;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Scene1 extends JPanel {
    private static final int SHOT_LIMIT = 30;
    private static final int RUSH_DURATION = 90 * 60;
    private static final int CAMPAIGN_WAVES = 3;
    private static final int POWERUP_DURATION = 900; // ~15 seconds
    private static final int COUNTDOWN_FRAMES = 236; // 3, 2, 1, START!

    private final Game game;
    private final GameMode mode;
    private final Random random = new Random();
    private final Map<Integer, List<SpawnDetails>> spawnMap = new HashMap<>();
    private final List<PowerUp> powerups = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    private Player player;
    private Timer timer;
    private AudioPlayer music;
    private int[][] starMap = new int[0][0];
    private int frame;
    private int waveFrame;
    private int level = 1;
    private int score;
    private int highScore;
    private int kills;
    private int waveSpawned;
    private int waveTarget;
    private int lives = PLAYER_LIVES;
    private int hp = PLAYER_MAX_HP;
    private int combo = 1;
    private int comboTimer;
    private int fireCooldown;
    private int invulnerableFrames;
    private int screenShake;
    private int bannerTimer = 150;
    private int countdown = COUNTDOWN_FRAMES;
    private int speedTimer;
    private int multiTimer;
    private boolean firing;
    private boolean paused;
    private boolean ended;
    private boolean victory;
    private boolean muted;
    private boolean gameOverPlayed;
    private String endMessage = "GAME OVER";

    public Scene1(Game game) {
        this(game, GameMode.CAMPAIGN);
    }

    public Scene1(Game game, GameMode mode) {
        this.game = game;
        this.mode = mode;
        loadCampaignSpawns();
    }

    public void start() {
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new GameKeys());
        starMap = MapLoader.load(MAP_STARS);
        player = new Player();
        prepareWave();
        initMusic();
        SoundFx.play(SFX_START);
        timer = new Timer(DELAY, new GameCycle());
        timer.start();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        stopMusic();
    }

    private void initMusic() {
        if (muted) {
            return;
        }
        try {
            music = new AudioPlayer(MUSIC_STAGE);
            music.play();
        } catch (Exception e) {
            System.err.println("Game music unavailable: " + e.getMessage());
        }
    }

    private void stopMusic() {
        try {
            if (music != null) {
                music.stop();
                music = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void loadCampaignSpawns() {
        spawnMap.clear();
        try {
            for (String rawLine : Files.readAllLines(Paths.get(SPAWN_CSV))) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue;
                }
                int at = Integer.parseInt(parts[0].trim());
                SpawnDetails details = new SpawnDetails(parts[1].trim(),
                        Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim()));
                spawnMap.computeIfAbsent(at, key -> new ArrayList<>()).add(details);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Spawn map unavailable; campaign will use procedural waves.");
        }
    }

    private void prepareWave() {
        waveFrame = 0;
        waveSpawned = 0;
        bannerTimer = 150;
        enemies.clear();
        enemyBullets.clear();
        shots.clear();
        powerups.clear();
        waveTarget = 10 + level * 5;
    }

    private void update() {
        if (paused || ended) {
            return;
        }
        if (countdown > 0) {
            countdown--;
            return;
        }
        frame++;
        waveFrame++;
        if (bannerTimer > 0) bannerTimer--;
        if (fireCooldown > 0) fireCooldown--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (screenShake > 0) screenShake--;
        if (speedTimer > 0) speedTimer--;
        if (multiTimer > 0) multiTimer--;
        if (comboTimer > 0) {
            comboTimer--;
        } else {
            combo = 1;
        }
        player.setSpeed(speedTimer > 0 ? PLAYER_START_SPEED + 4 : PLAYER_START_SPEED);

        spawnForMode();
        if (firing && fireCooldown == 0) {
            fireShots();
        }
        player.act();
        updatePowerUps();
        updateEnemies();
        updateShots();
        updateEnemyBullets();
        updateParticles();
        updateExplosions();
        checkProgress();
    }

    private void spawnForMode() {
        int difficulty = mode == GameMode.CAMPAIGN ? level : 1 + frame / 1200;
        if (mode != GameMode.CAMPAIGN) {
            level = difficulty;
        }
        int interval = mode == GameMode.RUSH ? Math.max(22, 48 - difficulty * 2)
                : Math.max(30, 74 - difficulty * 8);
        boolean canSpawn = mode != GameMode.CAMPAIGN || waveSpawned < waveTarget;
        if (canSpawn && frame % interval == 0) {
            createEnemy(randomY(), difficulty);
        }
        if (frame > 0 && frame % 720 == 0) {
            powerups.add(random.nextBoolean() ? new SpeedUp(BOARD_WIDTH + 40, randomY())
                    : new MultiShot(BOARD_WIDTH + 40, randomY()));
        }
    }

    private int randomY() {
        return 50 + random.nextInt(Math.max(1, BOARD_HEIGHT - 170));
    }

    private void createEnemy(int y, int difficulty) {
        double modeBoost = mode == GameMode.RUSH ? .5 : 0;
        double speed = 1.1 + difficulty * .22 + modeBoost + random.nextDouble() * .5;
        double drift = (random.nextDouble() - .5) * 1.2;
        int hp = difficulty >= 2 && random.nextInt(100) < Math.min(45, difficulty * 10) ? 2 : 1;
        int points = hp == 2 ? 220 : 100;
        enemies.add(new Alien1(BOARD_WIDTH + 40, y, speed, drift, hp, points));
        waveSpawned++;
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = powerups.iterator();
        while (iterator.hasNext()) {
            PowerUp powerup = iterator.next();
            powerup.act();
            if (powerup.collideWithOther(player)) {
                powerup.upgrade(player);
                if (powerup instanceof SpeedUp) {
                    speedTimer = POWERUP_DURATION;
                } else if (powerup instanceof MultiShot) {
                    multiTimer = POWERUP_DURATION;
                }
                score += 250;
                SoundFx.play(SFX_POWERUP);
                burst(powerup.getX() + 20, powerup.getY() + 20, new Color(65, 240, 255), 20);
                iterator.remove();
            } else if (!powerup.isVisible()) {
                iterator.remove();
            }
        }
    }

    private void updateEnemies() {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.act(0);
            if (enemy.getX() < -10) {
                iterator.remove();
                finish(false, "GAME OVER");
                return;
            }
            if (invulnerableFrames == 0 && enemy.collideWithOther(player)) {
                explosions.add(new Explosion(enemy.getX() + 25, enemy.getY() + 25));
                iterator.remove();
                damagePlayer(DMG_CONTACT);
                continue;
            }
            int fireOdds = mode == GameMode.RUSH ? 150 : 210;
            if (enemyBullets.size() < 40 && enemy.getX() < BOARD_WIDTH - 20 && random.nextInt(fireOdds) == 0) {
                boolean plasma = random.nextInt(6) == 0; // plasma is rarer than bullets
                double vy = Math.max(-1.4, Math.min(1.4, (player.getY() - enemy.getY()) / 260.0));
                double vx = plasma ? -2.2 : -4.4; // plasma travels slowly so it can be dodged
                enemyBullets.add(new EnemyBullet(enemy.getX(), enemy.getY() + 20, vx, vy, plasma));
            }
        }
    }

    private void updateShots() {
        Iterator<Shot> shotIterator = shots.iterator();
        while (shotIterator.hasNext()) {
            Shot shot = shotIterator.next();
            shot.act();
            boolean consumed = false;
            for (Enemy enemy : enemies) {
                if (enemy.isVisible() && shot.collideWithOther(enemy)) {
                    consumed = true;
                    if (enemy.hit()) {
                        enemy.die();
                        kills++;
                        combo = comboTimer > 0 ? Math.min(8, combo + 1) : 1;
                        comboTimer = 150;
                        score += enemy.getScoreValue() * combo;
                        highScore = Math.max(highScore, score);
                        explosions.add(new Explosion(enemy.getX() + 25, enemy.getY() + 25));
                        SoundFx.play(SFX_EXPLOSION);
                        burst(enemy.getX() + 25, enemy.getY() + 25,
                                combo >= 4 ? new Color(255, 90, 220) : new Color(80, 225, 255), 16);
                        screenShake = Math.min(9, 3 + combo / 2);
                    } else {
                        burst(shot.getX(), shot.getY(), new Color(255, 220, 80), 5);
                    }
                    break;
                }
            }
            if (consumed || shot.getX() > BOARD_WIDTH + 30 || shot.getY() < -30 || shot.getY() > BOARD_HEIGHT + 30) {
                shotIterator.remove();
            }
        }
        enemies.removeIf(enemy -> !enemy.isVisible());
    }

    private void updateEnemyBullets() {
        Iterator<EnemyBullet> iterator = enemyBullets.iterator();
        while (iterator.hasNext()) {
            EnemyBullet bullet = iterator.next();
            bullet.act();
            if (invulnerableFrames == 0 && bullet.collideWithOther(player)) {
                iterator.remove();
                damagePlayer(bullet.getDamage());
            } else if (!bullet.isVisible() || bullet.getX() < -40 || bullet.getY() < -40
                    || bullet.getY() > BOARD_HEIGHT + 40) {
                iterator.remove();
            }
        }
    }

    private void damagePlayer(int amount) {
        if (invulnerableFrames > 0 || ended) {
            return;
        }
        hp -= amount;
        combo = 1;
        comboTimer = 0;
        screenShake = 12;
        SoundFx.play(SFX_PLAYER_HIT);
        burst(player.getX() + 22, player.getY() + 14, new Color(255, 70, 130), 22);
        if (hp <= 0) {
            lives--;
            hp = PLAYER_MAX_HP;
            invulnerableFrames = 130;
            enemyBullets.clear();
            player.resetPosition();
            if (lives <= 0) {
                finish(false, "GAME OVER");
            }
        } else {
            invulnerableFrames = 45;
        }
    }

    private void updateParticles() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update();
            if (particle.life <= 0) iterator.remove();
        }
    }

    private void updateExplosions() {
        for (Explosion explosion : explosions) {
            explosion.visibleCountDown();
        }
        explosions.removeIf(explosion -> !explosion.isVisible());
    }

    private void checkProgress() {
        if (mode == GameMode.RUSH && frame >= RUSH_DURATION) {
            finish(true, "TIME UP");
            return;
        }
        if (mode == GameMode.CAMPAIGN && waveSpawned >= waveTarget && enemies.isEmpty()) {
            if (level >= CAMPAIGN_WAVES) {
                score += lives * 1000;
                finish(true, "YOU WIN");
            } else {
                level++;
                score += 750 * level;
                prepareWave();
            }
        }
    }

    private void finish(boolean won, String message) {
        if (ended) {
            return;
        }
        ended = true;
        victory = won;
        endMessage = message;
        firing = false;
        highScore = Math.max(highScore, score);
        stopMusic();
        if (!gameOverPlayed) {
            gameOverPlayed = true;
            SoundFx.play(SFX_GAME_OVER); // plays fully once when the game ends
        }
    }

    private void fireShots() {
        if (paused || ended || countdown > 0 || shots.size() >= SHOT_LIMIT) {
            return;
        }
        int fx = player.getX() + player.getWidth();
        int fy = player.getY() + player.getHeight() / 2 - 3;
        if (multiTimer > 0) {
            shots.add(new Shot(fx, fy - 10, -3));
            shots.add(new Shot(fx, fy, 0));
            shots.add(new Shot(fx, fy + 10, 3));
            fireCooldown = 12;
        } else {
            shots.add(new Shot(fx, fy));
            fireCooldown = 9;
        }
        SoundFx.play(SFX_SHOOT);
        for (int i = 0; i < 3; i++) {
            particles.add(new Particle(fx, fy + 3, 1 + random.nextDouble() * 2,
                    (random.nextDouble() - .5) * 1.5, 10, new Color(70, 230, 255)));
        }
    }

    private void burst(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = .8 + random.nextDouble() * 4.2;
            particles.add(new Particle(x, y, Math.cos(angle) * speed,
                    Math.sin(angle) * speed, 20 + random.nextInt(28), color));
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D screen = (Graphics2D) graphics.create();
        screen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int shakeX = screenShake > 0 ? random.nextInt(screenShake + 1) - screenShake / 2 : 0;
        int shakeY = screenShake > 0 ? random.nextInt(screenShake + 1) - screenShake / 2 : 0;
        screen.translate(shakeX, shakeY);
        drawBackground(screen);
        drawWorld(screen);
        screen.translate(-shakeX, -shakeY);
        drawHud(screen);
        drawPowerBar(screen);
        if (countdown > 0) drawCountdown(screen);
        if (bannerTimer > 0 && !ended && countdown == 0) drawWaveBanner(screen);
        if (paused) drawOverlay(screen, "PAUSED", "P TO RESUME   Q FOR MENU");
        if (ended) drawEndScreen(screen);
        screen.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics2D g) {
        Color top;
        Color bottom;
        switch (Math.min(3, level)) {
            case 2:
                top = new Color(28, 4, 35);
                bottom = new Color(45, 9, 25);
                break;
            case 3:
                top = new Color(35, 8, 4);
                bottom = new Color(65, 15, 8);
                break;
            default:
                top = new Color(3, 12, 34);
                bottom = new Color(8, 35, 65);
        }
        for (int y = 0; y < BOARD_HEIGHT; y += 4) {
            float t = y / (float) BOARD_HEIGHT;
            g.setColor(mix(top, bottom, t));
            g.fillRect(0, y, BOARD_WIDTH, 4);
        }
        // nebula blobs
        for (int i = 0; i < 5; i++) {
            int nx = Math.floorMod(i * 220 - frame / 3, BOARD_WIDTH + 300) - 150;
            int ny = 90 + (i * 130) % (BOARD_HEIGHT - 180);
            g.setColor(new Color(90 + i * 20, 60, 150, 22));
            g.fillOval(nx, ny, 260, 170);
        }
        drawStars(g, 1, 0.4, 90, 1);
        drawStars(g, 2, 0.9, 140, 1);
        drawStars(g, 3, 1.8, 60, 2);
        drawStarClusters(g);
    }

    private void drawStars(Graphics2D g, int layer, double speed, int count, int size) {
        for (int i = 0; i < count; i++) {
            int baseX = (i * 97 + layer * 31) % BOARD_WIDTH;
            int x = Math.floorMod(baseX - (int) (frame * speed), BOARD_WIDTH);
            int y = (i * 53 + layer * 17) % BOARD_HEIGHT;
            g.setColor(new Color(180, 205 + (i % 40), 255, 70 + layer * 45));
            g.fillOval(x, y, size, size);
        }
    }

    private void drawStarClusters(Graphics2D g) {
        if (starMap.length == 0) {
            return;
        }
        int cellW = 90;
        int cellH = 58;
        int worldW = starMap[0].length * cellW;
        int scroll = (int) (frame * 0.5) % worldW;
        for (int r = 0; r < starMap.length; r++) {
            for (int c = 0; c < starMap[r].length; c++) {
                if (starMap[r][c] != 1) {
                    continue;
                }
                int x = Math.floorMod(c * cellW - scroll, worldW) - 40;
                int y = 30 + r * cellH % (BOARD_HEIGHT - 60);
                g.setColor(new Color(200, 220, 255, 120));
                g.fillOval(x, y, 2, 2);
                g.fillOval(x + 6, y + 4, 1, 1);
                g.fillOval(x - 4, y + 6, 1, 1);
                g.fillOval(x + 3, y - 5, 1, 1);
            }
        }
    }

    private void drawWorld(Graphics2D g) {
        for (Particle particle : particles) particle.draw(g);
        for (Explosion explosion : explosions) {
            g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
        }
        for (PowerUp powerup : powerups) {
            int pulse = 4 + (int) (Math.sin(frame * .12) * 3);
            g.setColor(new Color(65, 230, 255, 40));
            g.fillOval(powerup.getX() - pulse, powerup.getY() - pulse, 48 + pulse * 2, 48 + pulse * 2);
            g.drawImage(powerup.getImage(), powerup.getX(), powerup.getY(), this);
        }
        for (Enemy enemy : enemies) {
            g.setColor(new Color(255, 55, 180, 35));
            g.fillOval(enemy.getX() - 6, enemy.getY() - 6, 66, 66);
            g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
        }
        for (Shot shot : shots) {
            g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
        }
        for (EnemyBullet bullet : enemyBullets) {
            if (bullet.isPlasma()) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bullet.getAlpha()));
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
                g.setComposite(AlphaComposite.SrcOver);
            } else {
                g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
            }
        }
        if (invulnerableFrames == 0 || frame % 10 < 5) {
            g.setColor(new Color(40, 220, 255, 30));
            g.fillOval(player.getX() - 8, player.getY() - 6, player.getWidth() + 16, player.getHeight() + 12);
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(2, 8, 28, 205));
        g.fillRoundRect(12, 12, BOARD_WIDTH - 24, 60, 15, 15);
        g.setColor(new Color(70, 210, 245, 100));
        g.drawRoundRect(12, 12, BOARD_WIDTH - 24, 60, 15, 15);

        // health bar (green) top-left
        g.setColor(new Color(20, 20, 24));
        g.fillRoundRect(28, 22, 170, 14, 7, 7);
        Color hpColor = hp > 50 ? new Color(70, 230, 120) : hp > 25 ? new Color(240, 210, 70) : new Color(240, 80, 90);
        g.setColor(hpColor);
        g.fillRoundRect(28, 22, Math.max(0, 170 * hp / PLAYER_MAX_HP), 14, 7, 7);
        g.setColor(new Color(120, 230, 160));
        g.drawRoundRect(28, 22, 170, 14, 7, 7);
        g.setFont(Fonts.get(16f));
        g.setColor(new Color(150, 240, 180));
        g.drawString("HP " + Math.max(0, hp) + "%", 30, 58);

        g.setFont(Fonts.get(20f));
        g.setColor(Color.WHITE);
        g.drawString("SCORE " + String.format("%07d", score), 235, 40);
        g.setFont(Fonts.get(15f));
        g.setColor(new Color(175, 190, 220));
        g.drawString("SPEED " + player.getSpeed() + "   SHOTS x" + player.getShotLevel(), 235, 60);

        g.setFont(Fonts.get(16f));
        g.setColor(new Color(255, 105, 165));
        g.drawString("LIVES " + lives, BOARD_WIDTH - 150, 40);
        String stage = mode == GameMode.CAMPAIGN ? "WAVE " + level + "/" + CAMPAIGN_WAVES : mode.getLabel();
        g.setColor(new Color(140, 220, 255));
        g.drawString(stage, BOARD_WIDTH - 150, 60);
    }

    private void drawPowerBar(Graphics2D g) {
        int y = BOARD_HEIGHT - 34;
        drawPowerSlot(g, 20, y, "SPD", speedTimer, new Color(70, 240, 140));
        drawPowerSlot(g, 132, y, "MULTI", multiTimer, new Color(255, 90, 200));
    }

    private void drawPowerSlot(Graphics2D g, int x, int y, String label, int timer, Color lit) {
        boolean active = timer > 0;
        g.setColor(active ? lit : new Color(80, 85, 95));
        g.setFont(Fonts.get(15f));
        g.drawString(label, x, y + 16);
        g.setColor(new Color(30, 32, 38));
        g.fillRoundRect(x + 52, y + 4, 44, 14, 6, 6);
        if (active) {
            g.setColor(lit);
            g.fillRoundRect(x + 52, y + 4, 44 * timer / POWERUP_DURATION, 14, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf((timer + 59) / 60), x + 100, y + 16);
        }
    }

    private void drawCountdown(Graphics2D g) {
        g.setColor(new Color(0, 2, 15, 120));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        String text;
        if (countdown > 177) text = "3";
        else if (countdown > 118) text = "2";
        else if (countdown > 59) text = "1";
        else text = "START!";
        g.setFont(Fonts.get(text.equals("START!") ? 64f : 96f));
        g.setColor(new Color(90, 235, 255));
        drawCentered(g, text, BOARD_HEIGHT / 2);
    }

    private void drawWaveBanner(Graphics2D g) {
        float alpha = Math.min(1f, bannerTimer / 35f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setFont(Fonts.get(34f));
        g.setColor(new Color(90, 230, 255));
        String title = mode == GameMode.CAMPAIGN ? "WAVE " + level : mode.getLabel();
        drawCentered(g, title, BOARD_HEIGHT / 2 - 10);
        g.setFont(Fonts.get(16f));
        g.setColor(Color.WHITE);
        drawCentered(g, mode.getDescription(), BOARD_HEIGHT / 2 + 24);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 2, 15, 205));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setFont(Fonts.get(48f));
        g.setColor(new Color(80, 225, 255));
        drawCentered(g, title, BOARD_HEIGHT / 2 - 15);
        g.setFont(Fonts.get(16f));
        g.setColor(new Color(190, 205, 230));
        drawCentered(g, subtitle, BOARD_HEIGHT / 2 + 28);
    }

    private void drawEndScreen(Graphics2D g) {
        g.setColor(new Color(0, 2, 15, 225));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(victory ? new Color(80, 240, 220) : new Color(255, 80, 150));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(105, 175, BOARD_WIDTH - 210, 330, 28, 28);
        g.setFont(Fonts.get(46f));
        drawCentered(g, endMessage, 250);
        g.setFont(Fonts.get(28f));
        g.setColor(Color.WHITE);
        drawCentered(g, String.format("%07d", score), 305);
        g.setFont(Fonts.get(16f));
        g.setColor(new Color(210, 220, 240));
        drawCentered(g, "ENEMIES  " + kills, 355);
        drawCentered(g, "R  RETRY", 420);
        drawCentered(g, "Q  BACK TO MENU", 455);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        g.drawString(text, (BOARD_WIDTH - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private static Color mix(Color a, Color b, float t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();
        }
    }

    private class GameKeys extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_Q && ended) {
                game.loadTitle();
                return;
            }
            if (key == KeyEvent.VK_ESCAPE) {
                game.loadTitle();
                return;
            }
            if (key == KeyEvent.VK_R && ended) {
                game.loadGame(mode);
                return;
            }
            if (key == KeyEvent.VK_P && !ended) {
                paused = !paused;
                firing = false;
                repaint();
                return;
            }
            if (key == KeyEvent.VK_M) {
                muted = !muted;
                SoundFx.setMuted(muted);
                if (muted) stopMusic(); else initMusic();
                return;
            }
            if (!paused && !ended && countdown == 0) {
                player.keyPressed(e);
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
                    firing = true;
                    if (fireCooldown == 0) fireShots();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (player != null) {
                player.keyReleased(e);
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                firing = false;
            }
        }
    }

    private static class Particle {
        private double x;
        private double y;
        private final double vx;
        private final double vy;
        private int life;
        private final int maxLife;
        private final Color color;

        Particle(double x, double y, double vx, double vy, int life, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
        }

        void draw(Graphics2D g) {
            int alpha = Math.max(0, Math.min(255, life * 255 / maxLife));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int size = Math.max(1, 1 + life / 12);
            g.fillOval((int) x, (int) y, size, size);
        }
    }
}
