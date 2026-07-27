package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.GameMode;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TitleScene extends JPanel {
    private final Game game;
    private final GameMode[] modes = GameMode.values();
    private int selectedMode = 0;
    private Image background;
    private AudioPlayer audioPlayer;

    // selector arrow: tip sits at ARROW_TIP_X and points right at the selected button
    private static final int ARROW_TIP_X = 222;
    private static final int[] BUTTON_Y = { 405, 479, 553 };
    private static final Color SELECTOR_COLOR = new Color(0, 229, 255);

    public TitleScene(Game game) {
        this.game = game;
    }

    public void start() {
        setFocusable(true);
        addKeyListener(new MenuKeys());
        background = new ImageIcon(IMG_TITLE).getImage();
        playMusic();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void playMusic() {
        try {
            audioPlayer = new AudioPlayer("src/audio/title.wav");
            audioPlayer.play();
        } catch (Exception e) {
            System.out.println("Could not play the title music.");
        }
    }

    public void stop() {
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.out.println("Could not stop the title music.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // the custom title screen (logo, mode buttons and team names are baked into the image)
        g.drawImage(background, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // right-pointing triangle selector, tip at ARROW_TIP_X, aligned to the selected button
        if (selectedMode >= 0 && selectedMode < BUTTON_Y.length) {
            int y = BUTTON_Y[selectedMode];
            int len = 20;
            int half = 13;
            int[] xs = { ARROW_TIP_X - len, ARROW_TIP_X - len, ARROW_TIP_X };
            int[] ys = { y - half, y + half, y };
            g.setColor(SELECTOR_COLOR);
            g.fillPolygon(xs, ys, 3);
        }
    }

    private class MenuKeys extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                selectedMode--;
                if (selectedMode < 0) {
                    selectedMode = modes.length - 1;
                }
            } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                selectedMode++;
                if (selectedMode >= modes.length) {
                    selectedMode = 0;
                }
            } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                game.loadGame(modes[selectedMode]);
            }

            repaint();
        }
    }
}
