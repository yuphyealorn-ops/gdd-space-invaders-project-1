package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;

public final class FinalAnimationValidation {

    private FinalAnimationValidation() {
    }

    public static void main(String[] args) {
        validateEnemyBomb();
        validateBossPlasma();
        validateBossFlame();
        validateBossDamageCharge();
        System.out.println("Final animation validation passed.");
    }

    private static void validateEnemyBomb() {
        int[][] dimensions = {
            {67, 64}, {65, 62}, {61, 62}, {59, 55}, {54, 54},
            {47, 49}, {41, 44}, {38, 38}, {35, 34}
        };
        EnemyBullet bomb = new EnemyBullet(600, BOARD_HEIGHT / 2, -2.2, 0, true);
        boolean[] seen = new boolean[dimensions.length];
        boolean reachedPlayerArea = false;
        boolean passedPlayer = false;
        int previousFrame = 0;
        int lastFrameStartX = Integer.MAX_VALUE;
        int ticks = 0;

        while (bomb.isVisible() && ticks < 1000) {
            int frame = bomb.getFrameIndex();
            require(frame >= previousFrame, "Enemy bomb frames moved backwards");
            require(frame <= previousFrame + 1, "Enemy bomb skipped a documented frame");
            requireDimensions(bomb, dimensions[frame], "Enemy bomb frame " + frame);
            seen[frame] = true;
            previousFrame = frame;
            if (frame == dimensions.length - 1 && lastFrameStartX == Integer.MAX_VALUE) {
                lastFrameStartX = bomb.getX();
            }
            if (bomb.getX() <= BOARD_WIDTH / 4) {
                reachedPlayerArea = true;
            }
            if (bomb.getX() < 0) {
                passedPlayer = true;
            }
            require(bomb.getAlpha() == 1f,
                    "Enemy bomb added a code fade on top of the documented fade frames");
            bomb.act();
            ticks++;
        }

        requireAllFrames(seen, "Enemy bomb");
        require(reachedPlayerArea, "Enemy bomb died before reaching the player's area");
        require(passedPlayer, "Enemy bomb did not fly past the player toward the left edge");
        require(lastFrameStartX <= 40,
                "Enemy bomb fade sequence completed too far from the left edge: x=" + lastFrameStartX);
        require(!bomb.isVisible(), "Enemy bomb did not leave the board");
        System.out.println("Enemy bomb: " + dimensions.length + " frames, " + ticks
                + " travel ticks, final fade near x=" + lastFrameStartX);
    }

    private static void validateBossPlasma() {
        int[][] dimensions = {
            {10, 10},
            {26, 24}, {36, 28}, {34, 22}, {26, 14},
            {14, 14}, {22, 20}, {32, 34}, {52, 52},
            {62, 60}, {74, 76}, {84, 82}, {92, 94},
            {100, 98}, {106, 108}
        };
        BossBullet plasma = new BossBullet(613, BOARD_HEIGHT / 2, 84, BOARD_HEIGHT / 2);
        boolean[] seen = new boolean[dimensions.length];
        boolean reachedPlayer = false;
        boolean passedPlayer = false;
        int peakFrameX = Integer.MAX_VALUE;
        int previousFrame = 0;
        int ticks = 0;

        while (plasma.isVisible() && ticks < 1000) {
            int frame = plasma.getFrameIndex();
            require(frame >= previousFrame, "Boss plasma frames moved backwards");
            require(frame <= previousFrame + 1, "Boss plasma skipped a documented frame");
            requireDimensions(plasma, dimensions[frame], "Boss plasma frame " + frame);
            seen[frame] = true;
            previousFrame = frame;
            if (frame == 12) {
                peakFrameX = Math.min(peakFrameX, (int) Math.round(plasma.getCenterX()));
            }
            if (plasma.getCenterX() <= 84) {
                reachedPlayer = true;
            }
            if (plasma.getCenterX() < 0) {
                passedPlayer = true;
            }
            plasma.act();
            ticks++;
        }

        requireAllFrames(seen, "Boss plasma");
        require(reachedPlayer, "Boss plasma died before reaching its player target");
        require(passedPlayer, "Boss plasma did not remain alive past the player");
        require(peakFrameX <= 130,
                "Boss plasma peak frame appeared too early: centre x=" + peakFrameX);
        require(!plasma.isVisible(), "Boss plasma did not leave the board");
        System.out.println("Boss plasma: " + dimensions.length + " frames, " + ticks
                + " travel ticks, peak near x=" + peakFrameX);
    }

    private static void validateBossFlame() {
        int[][] dimensions = {
            {14, 16},
            {36, 24}, {58, 28}, {88, 24}, {68, 18}, {40, 18},
            {46, 54}, {50, 52}, {70, 82},
            {80, 96}, {82, 102}, {84, 104}, {78, 96}
        };
        int targetX = BOARD_WIDTH / 4;
        int targetY = BOARD_HEIGHT / 2;
        BossFlame flame = new BossFlame(613, 120, targetX, targetY);
        boolean[] seen = new boolean[dimensions.length];
        boolean reachedTarget = false;
        boolean passedTarget = false;
        int previousFrame = 0;
        int ticks = 0;

        while (flame.isVisible() && ticks < 1000) {
            int frame = flame.getFrameIndex();
            require(frame >= previousFrame, "Boss flame frames moved backwards");
            require(frame <= previousFrame + 1, "Boss flame skipped a documented frame");
            requireDimensions(flame, dimensions[frame], "Boss flame frame " + frame);
            seen[frame] = true;
            previousFrame = frame;
            if (Math.hypot(flame.getCenterX() - targetX,
                    flame.getCenterY() - targetY) <= 5.0) {
                reachedTarget = true;
            }
            if (flame.getCenterX() < targetX - 40) {
                passedTarget = true;
            }
            flame.act();
            ticks++;
        }

        requireAllFrames(seen, "Boss flame");
        require(reachedTarget,
                "Boss flame animation did not remain alive through the player's central target area");
        require(passedTarget, "Boss flame did not continue past its target area");
        require(flame.getDamage() == 2, "Boss flame damage tick is not 2% health");
        require(!flame.isVisible(), "Boss flame did not leave the board");
        System.out.println("Boss flame: " + dimensions.length + " frames, " + ticks
                + " travel ticks, reached target (" + targetX + "," + targetY + ")");
    }

    private static void validateBossDamageCharge() {
        Player player = new Player();
        require(player.getUltCharge() == 0, "Ultimate charge did not start at zero");
        player.gainBossDamageCharge(1);
        require(player.getUltCharge() == 5,
                "Normal boss damage did not increase ultimate charge");
        player.gainBossDamageCharge(5);
        require(player.getUltCharge() == 30,
                "Ultimate boss damage did not increase charge by damage dealt");
        player.gainBossDamageCharge(100);
        require(player.getUltCharge() == 100, "Ultimate charge did not cap at 100");
        player.resetUlt();
        player.gainBossDamageCharge(0);
        require(player.getUltCharge() == 0, "Non-damage changed ultimate charge");
        System.out.println("Boss damage charge: normal, ultimate, cap, and zero-damage cases passed");
    }

    private static void requireDimensions(Sprite sprite, int[] expected, String label) {
        int width = sprite.getImage().getWidth(null);
        int height = sprite.getImage().getHeight(null);
        require(width == expected[0] && height == expected[1],
                label + " expected " + expected[0] + "x" + expected[1]
                        + " but was " + width + "x" + height);
    }

    private static void requireAllFrames(boolean[] seen, String label) {
        for (int i = 0; i < seen.length; i++) {
            require(seen[i], label + " did not render documented frame " + i);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
