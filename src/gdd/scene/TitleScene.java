package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.GameMode;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Font;
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

    public TitleScene(Game game) {
        this.game = game;
    }

    public void start() {
        setFocusable(true);
        addKeyListener(new MenuKeys());
        background = new ImageIcon(IMG_BACKGROUND).getImage();
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

        g.drawImage(background, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.setColor(Color.WHITE);
        drawCentered(g, "SPACE INVADERS", 130);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.LIGHT_GRAY);
        drawCentered(g, "Choose a game mode", 180);

        for (int i = 0; i < modes.length; i++) {
            int y = 270 + i * 90;

            if (i == selectedMode) {
                g.setColor(new Color(40, 100, 160));
                g.fillRect(150, y - 35, BOARD_WIDTH - 300, 65);
            }

            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.setColor(Color.WHITE);
            String arrow = i == selectedMode ? "> " : "  ";
            g.drawString(arrow + modes[i].getLabel(), 185, y - 5);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(modes[i].getDescription(), 215, y + 18);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.setColor(Color.WHITE);
        drawCentered(g, "Use UP and DOWN to choose", 590);
        drawCentered(g, "Press ENTER or SPACE to start", 620);
    }

    private void drawCentered(Graphics g, String text, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int x = (BOARD_WIDTH - textWidth) / 2;
        g.drawString(text, x, y);
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
