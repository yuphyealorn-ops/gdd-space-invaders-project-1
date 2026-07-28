package gdd.sprite;

import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.DMG_BULLET;
import static gdd.Global.IMG_ENEMY_EFFECT;
import java.awt.Image;
import java.awt.image.BufferedImage;

public final class EnemyBulletClipLifecycleValidation {

    private static final int[][] RECTS = {
        {311, 508, 48, 83},
        {580, 536, 129, 27},
        {820, 492, 120, 115},
        {1084, 513, 63, 73},
        {1324, 520, 56, 59}
    };
    private static final int[][] RUNTIME_SIZES = {
        {9, 15},
        {24, 5},
        {22, 21},
        {12, 14},
        {10, 11}
    };

    private EnemyBulletClipLifecycleValidation() {
    }

    public static void main(String[] args) {
        validateStartAndTravelFrames();
        validateFacingDirection();
        validateFacingDirectionBounds();
        validateImpactLifecycle();
        validateBombPathIsUnchanged();
        System.out.println("Enemy bullet clip lifecycle validation passed.");
    }

    private static void validateStartAndTravelFrames() {
        BufferedImage[] expected = expectedFrames();
        EnemyBullet bullet = new EnemyBullet(300, 200, -6.6, 0);
        double startCenterX = bullet.getBulletCenterX();
        double startCenterY = bullet.getBulletCenterY();

        require(bullet.getBulletFrameIndex() == 0,
                "Regular bullet did not begin with the documented start clip");
        requireSamePixels(expected[0], bullet.getImage(), "bullet start");
        for (int tick = 0; tick < 3; tick++) {
            bullet.act();
            require(bullet.getBulletFrameIndex() == 0,
                    "Bullet start clip was not held briefly");
            require(bullet.getX() == 300 && bullet.getY() == 200,
                    "Bullet moved while its muzzle/start clip was playing");
        }

        bullet.act();
        require(bullet.getBulletFrameIndex() == 1,
                "Regular bullet did not transition to its travel clip");
        requireSamePixels(expected[1], bullet.getImage(), "bullet travel");
        requirePositionClose(bullet.getBulletCenterX(), startCenterX - 60,
                "Travel clip did not appear 60 runtime pixels forward");
        requirePositionClose(bullet.getBulletCenterY(), startCenterY,
                "Frame-size change shifted the bullet off its centreline");

        double travelCenterX = bullet.getBulletCenterX();
        double travelCenterY = bullet.getBulletCenterY();
        bullet.act();
        requirePositionClose(bullet.getBulletCenterX(), travelCenterX - 6.6,
                "Travel clip did not continue along its firing direction");
        requirePositionClose(bullet.getBulletCenterY(), travelCenterY,
                "Straight bullet moved vertically during travel");
        requireSamePixels(expected[1], bullet.getImage(), "sustained bullet travel");
    }

    private static void validateFacingDirection() {
        ControlledEnemy enemy = new ControlledEnemy();
        enemy.face(270);
        requireClose(enemy.getFacingDirectionX(), -1.0,
                "Left-facing enemy returned the wrong horizontal shot direction");
        requireClose(enemy.getFacingDirectionY(), 0.0,
                "Left-facing enemy returned the wrong vertical shot direction");

        enemy.face(180);
        requireClose(enemy.getFacingDirectionX(), 0.0,
                "Down-facing enemy returned the wrong horizontal shot direction");
        requireClose(enemy.getFacingDirectionY(), 1.0,
                "Down-facing enemy returned the wrong vertical shot direction");

        EnemyBullet bullet = new EnemyBullet(300, 100,
                enemy.getFacingDirectionX() * 6.6,
                enemy.getFacingDirectionY() * 6.6);
        double startCenterX = bullet.getBulletCenterX();
        double startCenterY = bullet.getBulletCenterY();
        for (int tick = 0; tick < 4; tick++) {
            bullet.act();
        }
        requirePositionClose(bullet.getBulletCenterX(), startCenterX,
                "Vertical bullet shifted horizontally during its frame transition");
        requirePositionClose(bullet.getBulletCenterY(), startCenterY + 60,
                "Bullet start offset did not follow the enemy's facing direction");
        double travelCenterY = bullet.getBulletCenterY();
        bullet.act();
        requirePositionClose(bullet.getBulletCenterY(), travelCenterY + 6.6,
                "Bullet travel did not continue in the enemy's facing direction");
    }

    private static void validateFacingDirectionBounds() {
        EnemyBullet rightFacing = new EnemyBullet(BOARD_WIDTH - 20, 120, 6.6, 0);
        int ticks = 0;
        while (rightFacing.isVisible() && ticks < 100) {
            rightFacing.act();
            ticks++;
        }
        require(!rightFacing.isVisible(),
                "Right-facing enemy bullet was not removed after leaving the board");
    }

    private static void validateImpactLifecycle() {
        BufferedImage[] expected = expectedFrames();
        EnemyBullet bullet = new EnemyBullet(300, 200, -6.6, 0);
        for (int tick = 0; tick < 5; tick++) {
            bullet.act();
        }

        double impactCenterX = bullet.getBulletCenterX();
        double impactCenterY = bullet.getBulletCenterY();
        require(bullet.canDamagePlayer(),
                "Travelling regular bullet could not damage the player");
        require(bullet.beginImpact(),
                "Player collision did not start the regular-bullet impact");
        require(!bullet.canDamagePlayer(),
                "Impacting bullet could damage the player more than once");
        require(!bullet.beginImpact(),
                "Impact animation could be restarted and apply damage twice");
        require(bullet.getDamage() == DMG_BULLET,
                "Regular-bullet damage changed");
        requireSamePixels(expected[2], bullet.getImage(), "bullet hit");

        boolean[] seen = new boolean[3];
        seen[0] = true;
        for (int tick = 0; tick < 9; tick++) {
            bullet.act();
            if (!bullet.isVisible()) {
                continue;
            }
            int impactFrame = bullet.getBulletFrameIndex() - 2;
            require(impactFrame >= 0 && impactFrame < seen.length,
                    "Impact used an undocumented frame");
            seen[impactFrame] = true;
            requireSamePixels(expected[bullet.getBulletFrameIndex()],
                    bullet.getImage(), "impact frame " + impactFrame);
            requirePositionClose(bullet.getBulletCenterX(), impactCenterX,
                    "Impact animation moved horizontally after collision");
            requirePositionClose(bullet.getBulletCenterY(), impactCenterY,
                    "Impact animation moved vertically after collision");
        }

        for (int i = 0; i < seen.length; i++) {
            require(seen[i], "Impact animation skipped documented frame " + i);
        }
        require(!bullet.isVisible(),
                "Regular bullet remained visible after hit and fade frames");
    }

    private static void validateBombPathIsUnchanged() {
        EnemyBullet bomb = new EnemyBullet(300, 200, -2.2, 0, true);
        require(bomb.isPlasma(), "Bomb type changed");
        require(bomb.canDamagePlayer(), "Bomb could no longer damage the player");
        require(!bomb.beginImpact(),
                "Regular-bullet impact lifecycle was incorrectly applied to bombs");
        require(bomb.getImage().getWidth(null) == 67
                        && bomb.getImage().getHeight(null) == 64,
                "Bomb's first documented frame changed");
        bomb.act();
        require(bomb.getX() == 297 && bomb.getY() == 200,
                "Bomb movement path changed");
    }

    private static BufferedImage[] expectedFrames() {
        BufferedImage sheet = Sprite.loadSheet(IMG_ENEMY_EFFECT);
        BufferedImage[] expected = new BufferedImage[RECTS.length];
        for (int i = 0; i < RECTS.length; i++) {
            int[] rect = RECTS[i];
            int[] size = RUNTIME_SIZES[i];
            expected[i] = Sprite.scaleImage(
                    sheet.getSubimage(rect[0], rect[1], rect[2], rect[3]),
                    size[0], size[1]);
        }
        return expected;
    }

    private static void requireSamePixels(BufferedImage expected, Image actual, String label) {
        require(actual instanceof BufferedImage,
                label + " is not a buffered sprite frame");
        BufferedImage actualImage = (BufferedImage) actual;
        require(actualImage.getWidth() == expected.getWidth()
                        && actualImage.getHeight() == expected.getHeight(),
                label + " has incorrect runtime dimensions");
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                require(actualImage.getRGB(x, y) == expected.getRGB(x, y),
                        label + " does not match its documented source rectangle at "
                                + x + "," + y);
            }
        }
    }

    private static void requireClose(double actual, double expected, String message) {
        require(Math.abs(actual - expected) < 0.01,
                message + ": expected " + expected + " but was " + actual);
    }

    private static void requirePositionClose(double actual, double expected, String message) {
        require(Math.abs(actual - expected) <= 0.75,
                message + ": expected " + expected + " but was " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class ControlledEnemy extends Enemy {

        ControlledEnemy() {
            super(500, 250, 1);
        }

        void face(double angle) {
            facingAngle = angle;
        }
    }
}
