package org.example.oop.Model.PaymentModel;

public enum PaymentStatus {
    PENDING("PENDING", "Chờ xử lý"),
    PAID("PAID", "Hoàn tất"),
    FAILED("FAILED", "Thất bại");

    private final String code;
    private final String display;

    PaymentStatus(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String code() {
        return code;
    }

    public String display() {
        return display;
    }

    public static PaymentStatus fromCode(String c) {
        for (PaymentStatus s : values()) if (s.code.equals(c)) return s;
        throw new IllegalArgumentException("Unknown PaymentStatus: " + c);
    }
}
