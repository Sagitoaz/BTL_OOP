package org.example.oop.Model.PaymentModel;

public enum PaymentMethod {
    CASH("CASH", "Tiền mặt"),
    CARD("CARD", "Thẻ"),
    TRANSFER("TRANSFER", "Chuyển khoản");

    private final String code;
    private final String display;

    PaymentMethod(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equalsIgnoreCase(code)) return method;
        }
        throw new IllegalArgumentException("Unknown PaymentMethod: " + code);
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return display; // hiển thị đẹp trong ComboBox
    }
}
