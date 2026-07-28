package gdd.scene;

import gdd.GameMode;

public final class SceneTimingValidation {

    private SceneTimingValidation() {
    }

    public static void main(String[] args) {
        require(Scene1.CAMPAIGN_STAGE_DURATION_FRAMES == 18_000,
                "Campaign stage is not configured for five minutes at 60 updates per second");
        require(!Scene1.isCampaignStageTimeComplete(17_999),
                "Campaign stage can complete before five minutes");
        require(Scene1.isCampaignStageTimeComplete(18_000),
                "Campaign stage does not complete at the five-minute boundary");
        require(Scene1.isCampaignStageTimeComplete(18_001),
                "Campaign stage completion is not stable after five minutes");

        require("5:00".equals(Scene1.formatStageTime(0)),
                "Stage clock does not start at 5:00");
        require("4:59".equals(Scene1.formatStageTime(60)),
                "Stage clock does not decrement after one second");
        require("0:01".equals(Scene1.formatStageTime(17_940)),
                "Stage clock does not show its final second");
        require("0:00".equals(Scene1.formatStageTime(18_000)),
                "Stage clock does not end at 0:00");

        require(Scene1.enemyKillTarget(GameMode.CAMPAIGN, 1, 1) == 12,
                "Campaign stage 1 enemy target is not 12");
        require(Scene1.enemyKillTarget(GameMode.CAMPAIGN, 2, 1) == 16,
                "Campaign stage 2 does not increase its enemy target to 16");
        require(Scene1.enemyKillTarget(GameMode.ENDLESS, 1, 2) == 14,
                "Endless cycle 2 did not slightly increase its enemy target");
        require(Scene1.enemyKillTarget(GameMode.RUSH, 1, 1) == 0,
                "Rush incorrectly received a multi-stage enemy target");

        require(!Scene1.shouldAdvanceEnemyStage(
                GameMode.CAMPAIGN, 1, 17_999, 11, 12),
                "Campaign stage advanced before either bypass condition");
        require(Scene1.shouldAdvanceEnemyStage(
                GameMode.CAMPAIGN, 1, 0, 12, 12),
                "Campaign stage did not bypass after enough enemies were defeated");
        require(Scene1.shouldAdvanceEnemyStage(
                GameMode.ENDLESS, 2, 18_000, 0, 16),
                "Endless stage did not advance at the five-minute limit");
        require(!Scene1.shouldAdvanceEnemyStage(
                GameMode.RUSH, 1, 18_000, 99, 0),
                "Rush incorrectly advanced to another stage");

        require(Scene1.RUSH_DURATION_FRAMES == 3_600,
                "Rush is not configured for one minute");
        require(!Scene1.isRushTimeComplete(3_599),
                "Rush ended before one minute of active play");
        require(Scene1.isRushTimeComplete(3_600),
                "Rush did not end at one minute");
        require("1:00".equals(Scene1.formatRemainingTime(3_600, 0)),
                "Rush clock does not start at 1:00");
        require("0:01".equals(Scene1.formatRemainingTime(3_600, 3_540)),
                "Rush clock does not show its final second");
        require("0:00".equals(Scene1.formatRemainingTime(3_600, 3_600)),
                "Rush clock does not finish at 0:00");

        System.out.println(
                "Mode timing: Campaign/Endless use kill-or-five-minute stages; Rush is one 60-second stage.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
