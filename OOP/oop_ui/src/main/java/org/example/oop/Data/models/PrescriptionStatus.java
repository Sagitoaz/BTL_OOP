package org.example.oop.Data.models;

/**
 * PrescriptionStatus - trạng thái đơn kính.
 * Theo database: enum('active','expired','void')
 */
public enum PrescriptionStatus {
    ACTIVE("active"),
    EXPIRED("expired"),
    VOID("void");

    private final String value;

    PrescriptionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PrescriptionStatus fromString(String text) {
        for (PrescriptionStatus status : PrescriptionStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No PrescriptionStatus with value " + text + " found");
    }
}

