package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.GameMode;
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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Scene1 extends JPanel {
    private static final int SHOT_LIMIT = 30;
    private static final int RUSH_DURATION = 90 * 60;
    private static final int CAMPAIGN_WAVES = 3;

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
    private Image background;
    private Timer timer;
    private AudioPlayer audioPlayer;
    private int frame;
    private int waveFrame;
    private int level = 1;
    private int score;
    private int highScore;
    private int kills;
    private int waveKills;
    private int waveSpawned;
    private int waveTarget;
    private int campaignLastSpawn;
    private int lives = 3;
    private int combo = 1;
    private int comboTimer;
    private int fireCooldown;
    private int invulnerableFrames;
    private int screenShake;
    private int bannerTimer = 150;
    private boolean firing;
    private boolean paused;
    private boolean ended;
    private boolean victory;
    private boolean muted;
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
        background = new ImageIcon(IMG_BACKGROUND).getImage();
        player = new Player();
        prepareWave();
        initAudio();
        timer = new Timer(DELAY, new GameCycle());
        timer.start();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        stopAudio();
    }

    private void initAudio() {
        if (muted) {
            return;
        }
        try {
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Game music unavailable: " + e.getMessage());
        }
    }

    private void stopAudio() {
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void loadCampaignSpawns() {
        spawnMap.clear();
        campaignLastSpawn = 0;
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
                if (SpawnDetails.ALIEN1.equals(details.type)) {
                    campaignLastSpawn = Math.max(campaignLastSpawn, at);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Spawn map unavailable; campaign will use procedural waves.");
        }
    }

    private void prepareWave() {
        waveFrame = 0;
        waveKills = 0;
        waveSpawned = 0;
        bannerTimer = 150;
        enemies.clear();
        enemyBullets.clear();
        shots.clear();
        powerups.clear();
        if (mode == GameMode.CAMPAIGN && level == 1 && !spawnMap.isEmpty()) {
            waveTarget = countCampaignEnemies();
        } else {
            waveTarget = 8 + level * 4;
        }
    }

    private int countCampaignEnemies() {
        int count = 0;
        for (List<SpawnDetails> entries : spawnMap.values()) {
            for (SpawnDetails entry : entries) {
                if (SpawnDetails.ALIEN1.equals(entry.type)) {
                    count++;
                }
            }
        }
        return Math.max(1, count);
    }

    private void update() {
        if (paused || ended) {
            return;
        }
        frame++;
        waveFrame++;
        if (bannerTimer > 0) bannerTimer--;
        if (fireCooldown > 0) fireCooldown--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (screenShake > 0) screenShake--;
        if (comboTimer > 0) {
            comboTimer--;
        } else {
            combo = 1;
        }

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
        if (mode == GameMode.CAMPAIGN) {
            if (level == 1 && !spawnMap.isEmpty()) {
                List<SpawnDetails> entries = spawnMap.get(waveFrame);
                if (entries != null) {
                    for (SpawnDetails entry : entries) {
                        spawnMappedObject(entry);
                    }
                }
            } else {
                int interval = Math.max(34, 76 - level * 11);
                if (waveSpawned < waveTarget && waveFrame % interval == 0) {
                    spawnEnemy(level);
                }
                if (waveFrame == 270 || waveFrame == 690) {
                    powerups.add(random.nextBoolean() ? new SpeedUp(randomX(), -40)
                            : new MultiShot(randomX(), -40));
                }
            }
        } else {
            int difficulty = 1 + frame / 1200;
            level = difficulty;
            int interval = mode == GameMode.RUSH ? Math.max(22, 46 - difficulty * 2)
                    : Math.max(28, 82 - difficulty * 5);
            if (frame % interval == 0) {
                spawnEnemy(difficulty);
            }
            if (frame % 780 == 0) {
                powerups.add(random.nextBoolean() ? new SpeedUp(randomX(), -40)
                        : new MultiShot(randomX(), -40));
            }
        }
    }

    private void spawnMappedObject(SpawnDetails entry) {
        if (SpawnDetails.ALIEN1.equals(entry.type)) {
            createEnemy(entry.x, level);
        } else if (SpawnDetails.SPEED_UP.equals(entry.type) || "PowerUp-SpeedUp".equals(entry.type)) {
            powerups.add(new SpeedUp(entry.x, entry.y));
        } else if (SpawnDetails.MULTI_SHOT.equals(entry.type) || "PowerUp-MultiShot".equals(entry.type)) {
            powerups.add(new MultiShot(entry.x, entry.y));
        }
    }

    private void spawnEnemy(int difficulty) {
        createEnemy(randomX(), difficulty);
    }

    private int randomX() {
        return 25 + random.nextInt(BOARD_WIDTH - 85);
    }

    private void createEnemy(int x, int difficulty) {
        double modeBoost = mode == GameMode.RUSH ? .45 : 0;
        double speed = 0.8 + difficulty * .18 + modeBoost + random.nextDouble() * .45;
        double drift = (random.nextDouble() - .5) * (0.6 + difficulty * .08);
        int hp = difficulty >= 3 && random.nextInt(100) < Math.min(45, difficulty * 8) ? 2 : 1;
        int points = hp == 2 ? 220 : 100;
        enemies.add(new Alien1(Math.max(5, Math.min(BOARD_WIDTH - 50, x)), -45,
                speed, drift, hp, points));
        waveSpawned++;
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = powerups.iterator();
        while (iterator.hasNext()) {
            PowerUp powerup = iterator.next();
            powerup.act();
            if (powerup.collideWithOther(player)) {
                powerup.upgrade(player);
                score += 250;
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
            if (enemy.getY() > BOARD_HEIGHT - 70) {
                iterator.remove();
                damagePlayer();
                continue;
            }
            if (invulnerableFrames == 0 && enemy.collideWithOther(player)) {
                burst(enemy.getX() + 18, enemy.getY() + 18, new Color(255, 65, 160), 18);
                iterator.remove();
                damagePlayer();
                continue;
            }
            int fireOdds = mode == GameMode.RUSH ? 520 : 720;
            if (enemyBullets.size() < 32 && enemy.getY() > 25 && random.nextInt(fireOdds) == 0) {
                double aim = (player.getX() - enemy.getX()) / 220.0;
                enemyBullets.add(new EnemyBullet(enemy.getX() + 15, enemy.getY() + 28,
                        Math.max(-1.6, Math.min(1.6, aim)), 3.0 + level * .12));
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
                    shot.die();
                    consumed = true;
                    if (enemy.hit()) {
                        enemy.die();
                        kills++;
                        waveKills++;
                        combo = comboTimer > 0 ? Math.min(8, combo + 1) : 1;
                        comboTimer = 150;
                        int gained = enemy.getScoreValue() * combo;
                        score += gained;
                        highScore = Math.max(highScore, score);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        burst(enemy.getX() + 18, enemy.getY() + 18,
                                combo >= 4 ? new Color(255, 90, 220) : new Color(80, 225, 255), 16);
                        screenShake = Math.min(10, 3 + combo / 2);
                    } else {
                        burst(shot.getX(), shot.getY(), new Color(255, 220, 80), 5);
                    }
                    break;
                }
            }
            if (consumed || shot.getY() < -30 || shot.getX() < -30 || shot.getX() > BOARD_WIDTH + 30) {
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
                damagePlayer();
            } else if (bullet.getY() > BOARD_HEIGHT + 20 || bullet.getX() < -20 || bullet.getX() > BOARD_WIDTH + 20) {
                iterator.remove();
            }
        }
    }

    private void damagePlayer() {
        if (invulnerableFrames > 0 || ended) return;
        lives--;
        combo = 1;
        comboTimer = 0;
        invulnerableFrames = 130;
        screenShake = 14;
        burst(player.getX() + 22, player.getY() + 16, new Color(255, 70, 130), 30);
        enemyBullets.clear();
        player.resetPosition();
        if (lives <= 0) {
            finish(false, "GAME OVER");
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
        ended = true;
        victory = won;
        endMessage = message;
        firing = false;
        highScore = Math.max(highScore, score);
    }

    private void fireShots() {
        if (paused || ended || shots.size() >= SHOT_LIMIT) return;
        int x = player.getX();
        int y = player.getY();
        if (player.hasMultiShot()) {
            shots.add(new Shot(x, y, -12, -2));
            shots.add(new Shot(x, y, 0, 0));
            shots.add(new Shot(x, y, 12, 2));
        } else {
            shots.add(new Shot(x, y));
        }
        fireCooldown = player.hasMultiShot() ? 12 : 9;
        for (int i = 0; i < 3; i++) {
            particles.add(new Particle(x + 22, y, (random.nextDouble() - .5) * 1.5,
                    1 + random.nextDouble() * 2, 10, new Color(70, 230, 255)));
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
        if (bannerTimer > 0 && !ended) drawWaveBanner(screen);
        if (paused) drawOverlay(screen, "PAUSED", "P TO RESUME  •  ESC FOR MENU");
        if (ended) drawEndScreen(screen);
        screen.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics2D g) {
        g.drawImage(background, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        g.setColor(new Color(0, 3, 17, 70));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        for (int i = 0; i < 100; i++) {
            int x = Math.floorMod(i * 83 + 19, BOARD_WIDTH);
            int layer = 1 + i % 3;
            int y = Math.floorMod(i * 47 + frame * layer, BOARD_HEIGHT);
            int size = layer == 3 && i % 4 == 0 ? 3 : 1;
            g.setColor(new Color(120, 190 + i % 60, 255, 70 + layer * 45));
            g.fillOval(x, y, size, size + layer - 1);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(20, 50, 110, 45), 0, 130,
                new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, BOARD_WIDTH, 140);
    }

    private void drawWorld(Graphics2D g) {
        for (Particle particle : particles) particle.draw(g);
        for (Explosion explosion : explosions) {
            g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
        }
        for (PowerUp powerup : powerups) {
            int pulse = 4 + (int) (Math.sin(frame * .12) * 3);
            g.setColor(new Color(65, 230, 255, 40));
            g.fillOval(powerup.getX() - pulse, powerup.getY() - pulse, 40 + pulse * 2, 40 + pulse * 2);
            g.drawImage(powerup.getImage(), powerup.getX(), powerup.getY(), this);
        }
        for (Enemy enemy : enemies) {
            g.setColor(new Color(255, 55, 180, 35));
            g.fillOval(enemy.getX() - 7, enemy.getY() - 7, 50, 50);
            g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
        }
        for (Shot shot : shots) {
            g.setStroke(new BasicStroke(4f));
            g.setColor(new Color(30, 220, 255, 70));
            g.drawLine(shot.getX() + 1, shot.getY() + 18, shot.getX() + 1, shot.getY() - 4);
            g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
        }
        for (EnemyBullet bullet : enemyBullets) {
            g.drawImage(bullet.getImage(), bullet.getX(), bullet.getY(), this);
        }
        if (invulnerableFrames == 0 || frame % 10 < 5) {
            g.setColor(new Color(40, 220, 255, 30));
            g.fillOval(player.getX() - 10, player.getY() - 8, 66, 52);
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(2, 8, 28, 205));
        g.fillRoundRect(12, 12, BOARD_WIDTH - 24, 66, 15, 15);
        g.setColor(new Color(70, 210, 245, 100));
        g.drawRoundRect(12, 12, BOARD_WIDTH - 24, 66, 15, 15);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(100, 225, 255));
        g.drawString(mode.getLabel(), 30, 36);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.WHITE);
        g.drawString(String.format("%07d", score), 30, 62);

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(175, 190, 220));
        String stage = mode == GameMode.CAMPAIGN ? "WAVE " + level + "/" + CAMPAIGN_WAVES
                : "LEVEL " + level;
        drawCentered(g, stage, 36);
        if (mode == GameMode.RUSH) {
            int remaining = Math.max(0, (RUSH_DURATION - frame + 59) / 60);
            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.setColor(remaining < 15 ? new Color(255, 85, 145) : Color.WHITE);
            drawCentered(g, String.format("%02d:%02d", remaining / 60, remaining % 60), 63);
        } else {
            g.setColor(new Color(130, 155, 185));
            drawCentered(g, "KILLS " + kills, 61);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(255, 105, 165));
        g.drawString("LIVES " + heartString(), BOARD_WIDTH - 151, 37);
        g.setColor(combo > 1 ? new Color(255, 220, 75) : new Color(130, 150, 180));
        g.drawString("COMBO  x" + combo, BOARD_WIDTH - 151, 61);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(125, 145, 175));
        g.drawString("P PAUSE   M AUDIO   ESC MENU", 18, BOARD_HEIGHT - 16);
    }

    private String heartString() {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < lives; i++) hearts.append("◆ ");
        return hearts.toString();
    }

    private void drawWaveBanner(Graphics2D g) {
        float alpha = Math.min(1f, bannerTimer / 35f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.setColor(new Color(90, 230, 255));
        String title = mode == GameMode.CAMPAIGN ? "WAVE " + level : mode.getLabel();
        drawCentered(g, title, BOARD_HEIGHT / 2 - 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        drawCentered(g, mode.getDescription(), BOARD_HEIGHT / 2 + 20);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 2, 15, 205));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 45));
        g.setColor(new Color(80, 225, 255));
        drawCentered(g, title, BOARD_HEIGHT / 2 - 15);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(190, 205, 230));
        drawCentered(g, subtitle, BOARD_HEIGHT / 2 + 28);
    }

    private void drawEndScreen(Graphics2D g) {
        g.setColor(new Color(0, 2, 15, 222));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setPaint(new GradientPaint(110, 180, new Color(15, 80, 120, 210),
                BOARD_WIDTH - 110, 500, new Color(70, 15, 100, 210)));
        g.fillRoundRect(105, 165, BOARD_WIDTH - 210, 350, 28, 28);
        g.setColor(victory ? new Color(80, 240, 220) : new Color(255, 80, 150));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(105, 165, BOARD_WIDTH - 210, 350, 28, 28);
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        drawCentered(g, endMessage, 235);
        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        drawCentered(g, String.format("%07d", score), 300);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(155, 190, 220));
        drawCentered(g, "SCORE", 326);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(210, 220, 240));
        drawCentered(g, "ENEMIES  " + kills, 370);
        drawCentered(g, "R  RETRY", 435);
        drawCentered(g, "ESC  BACK TO MENU", 467);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        g.drawString(text, (BOARD_WIDTH - g.getFontMetrics().stringWidth(text)) / 2, y);
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
                if (muted) stopAudio(); else initAudio();
                return;
            }
            if (!paused && !ended) {
                player.keyPressed(e);
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
                    firing = true;
                    if (fireCooldown == 0) fireShots();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
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
