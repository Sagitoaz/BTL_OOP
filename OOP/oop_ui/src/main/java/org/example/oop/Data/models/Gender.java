package org.example.oop.Data.models;

/**
 * Gender - giới tính
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse gender from string value (case-insensitive)
     */
    public static Gender fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(text) ||
                gender.displayName.equalsIgnoreCase(text)) {
                return gender;
            }
        }

        // Try common variations
        String normalized = text.trim().toLowerCase();
        if (normalized.equals("m") || normalized.equals("male") || normalized.equals("nam")) {
            return MALE;
        } else if (normalized.equals("f") || normalized.equals("female") || normalized.equals("nữ") || normalized.equals("nu")) {
            return FEMALE;
        }

        return OTHER;
    }
}
