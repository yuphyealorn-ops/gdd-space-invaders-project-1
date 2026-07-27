package gdd;

public enum GameMode {
    CAMPAIGN("CAMPAIGN", "Complete all three waves"),
    ENDLESS("ENDLESS", "Keep playing until you lose"),
    RUSH("RUSH", "Get points before the time runs out");

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
