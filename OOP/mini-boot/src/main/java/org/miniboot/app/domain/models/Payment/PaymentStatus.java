package org.miniboot.app.domain.models.Payment;

public enum PaymentStatus {
    UNPAID("UNPAID", "Chưa thanh toán"),
    PENDING("PENDING", "Chờ xử lý"),
    PAID("PAID", "Hoàn tất"),
    CANCELLED("CANCELLED", "Đã hủy");

    private final String code;
    private final String display;

    PaymentStatus(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public static PaymentStatus fromCode(String c) {
        for (PaymentStatus s : values())
            if (s.code.equals(c))
                return s;
        throw new IllegalArgumentException("Unknown PaymentStatus: " + c);
    }

    public String code() {
        return code;
    }

    public String display() {
        return display;
    }
}
