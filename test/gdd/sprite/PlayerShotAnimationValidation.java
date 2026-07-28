package gdd.sprite;

import static gdd.Global.IMG_PLAYER_SHEET;
import static gdd.Global.PLAYER_SCALE;
import java.awt.Image;
import java.awt.image.BufferedImage;

public final class PlayerShotAnimationValidation {

    private static final int[][] RECTS = {
        {290, 38, 4, 4},
        {280, 37, 8, 6},
        {266, 37, 12, 6}
    };

    private PlayerShotAnimationValidation() {
    }

    public static void main(String[] args) {
        validateExactFramesAndOrder();
        validateSustainedFinalTravel();
        validateDiagonalRotationAndVelocity();
        System.out.println("Player shot animation validation passed.");
    }

    private static void validateExactFramesAndOrder() {
        BufferedImage sheet = Sprite.loadSheet(IMG_PLAYER_SHEET);
        BufferedImage[] expected = expectedFrames(sheet);
        Shot shot = new Shot(100, 200);

        require(shot.getNormalFrameIndex() == 0, "Normal shot did not start on frame 0");
        requireSamePixels(expected[0], shot.getImage(), "start frame");

        actAndRequireCenterVelocity(shot, 12, 0);
        actAndRequireCenterVelocity(shot, 12, 0);
        require(shot.getNormalFrameIndex() == 0, "Start frame was not held briefly");
        actAndRequireCenterVelocity(shot, 12, 0);
        require(shot.getNormalFrameIndex() == 1, "Normal shot did not advance to travel frame");
        requireSamePixels(expected[1], shot.getImage(), "travel frame");

        actAndRequireCenterVelocity(shot, 12, 0);
        actAndRequireCenterVelocity(shot, 12, 0);
        require(shot.getNormalFrameIndex() == 1, "Travel frame was not held briefly");
        actAndRequireCenterVelocity(shot, 12, 0);
        require(shot.getNormalFrameIndex() == 2, "Normal shot did not advance to final frame");
        requireSamePixels(expected[2], shot.getImage(), "final travel frame");
    }

    private static void validateSustainedFinalTravel() {
        BufferedImage finalFrame = expectedFrames(Sprite.loadSheet(IMG_PLAYER_SHEET))[2];
        Shot shot = new Shot(30, 140);
        for (int i = 0; i < 6; i++) {
            actAndRequireCenterVelocity(shot, 12, 0);
        }

        int finalStartCenterX = centerX(shot);
        int finalStartCenterY = centerY(shot);
        for (int i = 0; i < 50; i++) {
            actAndRequireCenterVelocity(shot, 12, 0);
            require(shot.getNormalFrameIndex() == 2,
                    "Final normal-shot frame did not persist during travel");
            requireSamePixels(finalFrame, shot.getImage(), "sustained final travel frame");
        }

        require(centerX(shot) == finalStartCenterX + 50 * 12,
                "Normal shot centre horizontal travel speed changed");
        require(centerY(shot) == finalStartCenterY,
                "Straight normal shot centre moved vertically");
    }

    private static void validateDiagonalRotationAndVelocity() {
        BufferedImage[] frames = expectedFrames(Sprite.loadSheet(IMG_PLAYER_SHEET));
        Shot shot = new Shot(50, 220, 3);
        int startCenterX = centerX(shot);
        int startCenterY = centerY(shot);
        int rotation = (int) Math.round(Math.toDegrees(Math.atan2(3, 12)));

        requireSamePixels(Sprite.rotate(frames[0], rotation), shot.getImage(),
                "rotated start frame");
        for (int i = 0; i < 6; i++) {
            actAndRequireCenterVelocity(shot, 12, 3);
        }
        requireSamePixels(Sprite.rotate(frames[2], rotation), shot.getImage(),
                "rotated final frame");
        require(centerX(shot) == startCenterX + 6 * 12,
                "Diagonal normal shot centre horizontal velocity changed");
        require(centerY(shot) == startCenterY + 6 * 3,
                "Diagonal normal shot centre vertical velocity changed");
    }

    private static BufferedImage[] expectedFrames(BufferedImage sheet) {
        BufferedImage[] frames = new BufferedImage[RECTS.length];
        for (int i = 0; i < RECTS.length; i++) {
            int[] r = RECTS[i];
            frames[i] = Sprite.scaleImage(sheet.getSubimage(r[0], r[1], r[2], r[3]),
                    r[2] * PLAYER_SCALE, r[3] * PLAYER_SCALE);
        }
        return frames;
    }

    private static void actAndRequireCenterVelocity(Shot shot, int velocityX, int velocityY) {
        int previousCenterX = centerX(shot);
        int previousCenterY = centerY(shot);
        shot.act();
        require(centerX(shot) == previousCenterX + velocityX,
                "Shot frame swap shifted the horizontal collision centre");
        require(centerY(shot) == previousCenterY + velocityY,
                "Shot frame swap shifted the vertical collision centre");
    }

    private static int centerX(Shot shot) {
        return shot.getX() + shot.getImage().getWidth(null) / 2;
    }

    private static int centerY(Shot shot) {
        return shot.getY() + shot.getImage().getHeight(null) / 2;
    }

    private static void requireSamePixels(BufferedImage expected, Image actual, String label) {
        require(actual instanceof BufferedImage, label + " is not a buffered sprite frame");
        BufferedImage actualImage = (BufferedImage) actual;
        require(actualImage.getWidth() == expected.getWidth()
                        && actualImage.getHeight() == expected.getHeight(),
                label + " has incorrect dimensions");
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                require(actualImage.getRGB(x, y) == expected.getRGB(x, y),
                        label + " does not match the documented source rectangle at "
                                + x + "," + y);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
