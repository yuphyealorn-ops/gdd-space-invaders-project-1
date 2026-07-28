package gdd.scene;

import gdd.GameMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ModeProgressionValidation {

    private ModeProgressionValidation() {
    }

    public static void main(String[] args) throws Exception {
        validateCampaignBypass();
        validateEndlessLoop();
        validateGameOverBlocksStageAdvance();
        System.out.println(
                "Mode progression: kill bypass reaches the boss; Endless loops; Game Over blocks advancement.");
    }

    private static void validateCampaignBypass() throws Exception {
        Scene1 campaign = new Scene1(null, GameMode.CAMPAIGN);
        set(campaign, "muted", true);
        invoke(campaign, "prepareWave");

        set(campaign, "stageKills",
                Scene1.enemyKillTarget(GameMode.CAMPAIGN, 1, 1));
        invoke(campaign, "checkProgress");
        require(value(campaign, "level") == 2,
                "Campaign stage 1 kill target did not bypass its timer");
        require(value(campaign, "waveFrame") == 0 && value(campaign, "stageKills") == 0,
                "Campaign stage 2 did not receive fresh stage progress");
        require(value(campaign, "stageKillTarget")
                        == Scene1.enemyKillTarget(GameMode.CAMPAIGN, 2, 1),
                "Campaign stage 2 did not receive its larger enemy target");

        set(campaign, "stageKills",
                Scene1.enemyKillTarget(GameMode.CAMPAIGN, 2, 1));
        invoke(campaign, "checkProgress");
        require(value(campaign, "level") == 3 && booleanValue(campaign, "bossStarted"),
                "Campaign stage 2 kill target did not start the boss stage");
        require(objectValue(campaign, "boss") != null,
                "Campaign boss was not created after the stage 2 bypass");
    }

    private static void validateEndlessLoop() throws Exception {
        Scene1 endless = new Scene1(null, GameMode.ENDLESS);
        set(endless, "level", 3);
        set(endless, "endlessCycle", 2);
        set(endless, "score", 4_200);
        set(endless, "lives", 2);
        set(endless, "waveFrame", 900);
        set(endless, "stageKills", 7);
        set(endless, "bossStarted", true);
        set(endless, "pendingVictory", true);
        set(endless, "muted", true);

        invoke(endless, "beginNextEndlessCycle");

        require(value(endless, "level") == 1,
                "Endless boss completion did not loop to stage 1");
        require(value(endless, "endlessCycle") == 3,
                "Endless cycle counter did not advance");
        require(value(endless, "score") == 4_200,
                "Endless loop discarded the player's score");
        require(value(endless, "lives") == 2,
                "Endless loop discarded the player's lives");
        require(value(endless, "waveFrame") == 0 && value(endless, "stageKills") == 0,
                "Endless loop did not reset stage progress");
        require(value(endless, "stageKillTarget")
                        == Scene1.enemyKillTarget(GameMode.ENDLESS, 1, 3),
                "Endless loop did not prepare the next cycle's larger target");
        require(!booleanValue(endless, "bossStarted")
                        && !booleanValue(endless, "pendingVictory"),
                "Endless loop did not reset boss resolution state");

    }

    private static void validateGameOverBlocksStageAdvance() throws Exception {
        Scene1 campaign = new Scene1(null, GameMode.CAMPAIGN);
        set(campaign, "level", 1);
        set(campaign, "stageKillTarget",
                Scene1.enemyKillTarget(GameMode.CAMPAIGN, 1, 1));
        set(campaign, "stageKills",
                Scene1.enemyKillTarget(GameMode.CAMPAIGN, 1, 1));
        set(campaign, "ended", true);
        set(campaign, "muted", true);

        invoke(campaign, "checkProgress");

        require(value(campaign, "level") == 1,
                "Game Over allowed a same-tick stage advance");
    }

    private static void invoke(Scene1 scene, String name) throws Exception {
        Method method = Scene1.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(scene);
    }

    private static void set(Scene1 scene, String name, Object value) throws Exception {
        Field field = Scene1.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(scene, value);
    }

    private static int value(Scene1 scene, String name) throws Exception {
        Field field = Scene1.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(scene);
    }

    private static boolean booleanValue(Scene1 scene, String name) throws Exception {
        Field field = Scene1.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getBoolean(scene);
    }

    private static Object objectValue(Scene1 scene, String name) throws Exception {
        Field field = Scene1.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(scene);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
