package org.example.oop.Model.PaymentModel;

import java.time.Instant;
import java.util.Objects;

/**
 * Phản ánh cấu trúc của bảng Payment_Status_Log trong cơ sở dữ liệu.
 */
public class PaymentStatusLog {
    private Integer id;
    private int paymentId;
    private Instant changedAt; // Sửa đổi: Đổi tên từ createdAt
    private PaymentStatus status;

    // Sửa đổi: Bỏ 'reason'
    public PaymentStatusLog(Integer id, int paymentId, Instant changedAt, PaymentStatus status) {
        this.id = id;
        this.paymentId = paymentId;
        this.changedAt = changedAt;
        this.status = status;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public int getPaymentId() {
        return paymentId;
    }

    // Sửa đổi: Cập nhật để khớp với các thuộc tính mới
    public String toDataString() {
        return String.join("|",
                id == null ? "" : String.valueOf(id),
                String.valueOf(paymentId),
                changedAt == null ? "" : changedAt.toString(),
                status == null ? "" : status.code()
        );
    }

    // Sửa đổi: Cập nhật để khớp với các thuộc tính mới
    public static PaymentStatusLog fromDataString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Dữ liệu chuỗi không hợp lệ cho PaymentStatusLog");
        }
        return new PaymentStatusLog(
                parts[0].isEmpty() ? null : Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                parts[2].isEmpty() ? null : Instant.parse(parts[2]),
                parts[3].isEmpty() ? null : PaymentStatus.fromCode(parts[3])
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentStatusLog that = (PaymentStatusLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}