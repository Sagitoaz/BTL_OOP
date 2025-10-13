package org.example.oop.Data.models;

/**
 * PaymentStatus - trạng thái thanh toán.
 * Theo database: enum('pending','paid','failed')
 */
public enum PaymentStatus {
    PENDING("pending"),
    PAID("paid"),
    FAILED("failed");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromString(String text) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No PaymentStatus with value " + text + " found");
    }
}

