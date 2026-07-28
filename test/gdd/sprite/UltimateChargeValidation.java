package gdd.sprite;

public final class UltimateChargeValidation {

    private UltimateChargeValidation() {
    }

    public static void main(String[] args) {
        Player player = new Player();

        require(player.getUltCharge() == 0, "Ultimate charge must start at zero");
        require(!player.ultReady(), "Ultimate must not start ready");

        player.gainDamageCharge(1);
        require(player.getUltCharge() == 5,
                "Regular enemy damage must award charge");

        player.gainBossDamageCharge(2);
        require(player.getUltCharge() == 15,
                "Boss compatibility API must delegate to generic damage charge");

        player.gainDamageCharge(0);
        player.gainDamageCharge(-10);
        require(player.getUltCharge() == 15,
                "Non-positive damage must not change charge");

        player.gainDamageCharge(Integer.MAX_VALUE);
        require(player.getUltCharge() == 100,
                "Large damage must clamp charge to 100 without overflowing");
        require(player.ultReady(), "Ultimate must be ready at full charge");

        player.resetPosition();
        require(player.getUltCharge() == 100,
                "Resetting the player position must preserve charge");

        player.resetUlt();
        require(player.getUltCharge() == 0,
                "Using/resetting the ultimate must clear charge");
        require(!player.ultReady(), "Ultimate must not remain ready after reset");

        player.gainBossDamageCharge(Integer.MIN_VALUE);
        require(player.getUltCharge() == 0,
                "Negative boss damage must not restore charge");

        System.out.println("Ultimate charge validation passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
