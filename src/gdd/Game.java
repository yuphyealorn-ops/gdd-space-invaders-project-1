package gdd;

import gdd.scene.Scene1;
import gdd.scene.TitleScene;
import java.awt.Dimension;
import javax.swing.JFrame;

public class Game extends JFrame {
    private TitleScene titleScene;
    private Scene1 scene1;

    public Game() {
        setupWindow();
        loadTitle();
    }

    private void setupWindow() {
        setTitle("Nebula Strike");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setPreferredSize(
                new Dimension(Global.BOARD_WIDTH, Global.BOARD_HEIGHT));
        pack();
        setLocationRelativeTo(null);
    }

    public void loadTitle() {
        if (scene1 != null) {
            scene1.stop();
        }

        getContentPane().removeAll();
        titleScene = new TitleScene(this);
        add(titleScene);
        titleScene.start();
        refreshWindow();
    }

    public void loadScene1() {
        loadGame(GameMode.CAMPAIGN);
    }

    public void loadScene2() {
        loadGame(GameMode.CAMPAIGN);
    }

    public void loadGame(GameMode mode) {
        getContentPane().removeAll();
        scene1 = new Scene1(this, mode);
        add(scene1);

        if (titleScene != null) {
            titleScene.stop();
        }

        scene1.start();
        refreshWindow();
    }

    private void refreshWindow() {
        revalidate();
        repaint();
    }
}
