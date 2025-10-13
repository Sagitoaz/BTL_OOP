package org.example.oop.Data.models;

/**
 * PaymentMethod - phương thức thanh toán.
 * Theo database: enum('cash','bank')
 */
public enum PaymentMethod {
    CASH("cash"),
    BANK("bank");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentMethod fromString(String text) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equalsIgnoreCase(text)) {
                return method;
            }
        }
        throw new IllegalArgumentException("No PaymentMethod with value " + text + " found");
    }

    public static PaymentMethod fromValue(String value) {
        return fromString(value);
    }
}
