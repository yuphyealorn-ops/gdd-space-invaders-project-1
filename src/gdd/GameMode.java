package gdd;

public enum GameMode {
    CAMPAIGN("CAMPAIGN", "Clear two waves, then defeat the boss"),
    ENDLESS("ENDLESS", "Loop all three stages until you lose"),
    RUSH("RUSH", "Score as much as possible in one minute");

    private final String label;
    private final String description;

    GameMode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
