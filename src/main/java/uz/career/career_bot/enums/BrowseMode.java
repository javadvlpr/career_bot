package uz.career.career_bot.enums;

public enum BrowseMode {
    MATCHED("matched"),
    ALL("all"),
    SAVED("saved");

    private final String value;

    BrowseMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BrowseMode fromValue(String value) {
        if (value == null) return MATCHED;
        for (BrowseMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return MATCHED;
    }
}