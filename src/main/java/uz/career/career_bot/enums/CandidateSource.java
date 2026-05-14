package uz.career.career_bot.enums;

public enum CandidateSource {
    MATCHED("matched"),
    APPLIED("applied");

    private final String value;

    CandidateSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CandidateSource fromValue(String value) {
        if (value == null) return MATCHED;
        for (CandidateSource source : values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        return MATCHED; // default
    }
}