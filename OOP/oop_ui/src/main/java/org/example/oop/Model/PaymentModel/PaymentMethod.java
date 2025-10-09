package org.example.oop.Model.PaymentModel;

public enum PaymentMethod {
    CASH("CASH", "Tiền mặt"),
    CARD("CARD", "Thẻ"),
    TRANSFER("TRASFER", "Chuyển khoản");
    private final String name;
    private final String display;

    PaymentMethod(final String name, final String display) {
        this.name = name;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public static PaymentMethod getPaymentMethod(final String name) {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            if (paymentMethod.getName().equals(name)) {
                return paymentMethod;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + name);
    }
}
