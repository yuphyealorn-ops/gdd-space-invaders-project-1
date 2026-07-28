package gdd.scene;

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

        System.out.println(
                "Campaign timing: stages 1 and 2 are gated for 18,000 updates and display 5:00 through 0:00.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
