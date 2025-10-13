package org.example.oop.Data.models;

/**
 * LensType - loại tròng kính.
 * Theo database: enum('single_vision','bifocal','progressive','contact','other')
 */
public enum LensType {
    SINGLE_VISION("single_vision"),
    BIFOCAL("bifocal"),
    PROGRESSIVE("progressive"),
    CONTACT("contact"),
    OTHER("other");

    private final String value;

    LensType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LensType fromString(String text) {
        for (LensType type : LensType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No LensType with value " + text + " found");
    }
}

