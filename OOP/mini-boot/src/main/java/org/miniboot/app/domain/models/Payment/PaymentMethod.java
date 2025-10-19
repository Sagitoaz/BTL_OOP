package org.miniboot.app.domain.models.Payment;

public enum PaymentMethod {
    CASH("CASH", "Tiền mặt"),
    CARD("CARD", "Thẻ"),
    TRANSFER("BANK", "Chuyển khoản");

    private final String code;
    private final String display;

    PaymentMethod(String code, String display) {
        this.code = code;
        this.display = display;
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
