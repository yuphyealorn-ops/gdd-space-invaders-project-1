package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public final class EnemyExhaustValidation {

    private EnemyExhaustValidation() {
    }

    public static void main(String[] args) {
        ControlledEnemy enemy = new ControlledEnemy();
        boolean[] seen = new boolean[5];

        enemy.moveForward();
        for (int i = 0; i < 27; i++) {
            enemy.act(0, enemy.playerAimY());
            require(enemy.isExhaustActive(),
                    "Exhaust stopped while the enemy was moving forward");
            seen[enemy.getExhaustFrameIndex()] = true;
            Image exhaust = enemy.getExhaustImage();
            require(exhaust != null && exhaust.getWidth(null) > 0 && exhaust.getHeight(null) > 0,
                    "Forward movement did not produce a valid exhaust frame");
            requireHasTransparency(exhaust);
            double enemyCenterX = enemy.getX() + enemy.getImage().getWidth(null) / 2.0;
            double exhaustCenterX = enemy.getExhaustX() + exhaust.getWidth(null) / 2.0;
            require(exhaustCenterX > enemyCenterX,
                    "Left-facing enemy exhaust was not positioned behind the ship");
        }
        requireAllFrames(seen);

        enemy.moveBackward();
        enemy.act(0, enemy.playerAimY());
        require(!enemy.isExhaustActive(),
                "Exhaust played while the enemy moved backward");
        require(enemy.getExhaustImage() == null,
                "Inactive exhaust still returned an image");

        enemy.strafePerpendicular();
        enemy.act(0, enemy.playerAimY());
        require(!enemy.isExhaustActive(),
                "Exhaust played during movement perpendicular to the facing direction");

        System.out.println(
                "Enemy exhaust: all 5 transparent clips animate only during forward movement and render behind the ship.");
    }

    private static void requireHasTransparency(Image image) {
        BufferedImage sample = new BufferedImage(
                image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sample.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        boolean foundTransparentPixel = false;
        boolean foundVisiblePixel = false;
        for (int y = 0; y < sample.getHeight(); y++) {
            for (int x = 0; x < sample.getWidth(); x++) {
                int alpha = sample.getRGB(x, y) >>> 24;
                foundTransparentPixel |= alpha == 0;
                foundVisiblePixel |= alpha > 0;
            }
        }
        require(foundTransparentPixel && foundVisiblePixel,
                "Exhaust frame does not preserve a transparent background");
    }

    private static void requireAllFrames(boolean[] seen) {
        for (int i = 0; i < seen.length; i++) {
            require(seen[i], "Enemy exhaust did not display documented frame " + i);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class ControlledEnemy extends Enemy {

        ControlledEnemy() {
            super(BOARD_WIDTH + 40, BOARD_HEIGHT / 2, 1);
            fallSpeed = 1.0;
        }

        void moveForward() {
            targetX = preciseX - 200;
            driftSpeed = 0;
            weavePhase = -0.04;
            facingAngle = 270;
        }

        void moveBackward() {
            targetX = preciseX + 200;
            driftSpeed = 0;
            weavePhase = -0.04;
            facingAngle = 270;
        }

        void strafePerpendicular() {
            targetX = preciseX;
            driftSpeed = 1.0;
            weavePhase = -0.04;
            facingAngle = 270;
        }

        int playerAimY() {
            return (int) preciseY + 34;
        }
    }
}
