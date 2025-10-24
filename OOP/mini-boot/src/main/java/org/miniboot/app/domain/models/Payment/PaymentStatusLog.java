package org.miniboot.app.domain.models.Payment;

import java.time.LocalDateTime;

/**
 * Phản ánh cấu trúc của bảng Payment_Status_Log trong cơ sở dữ liệu.
 */
public class PaymentStatusLog {
    private Integer id;
    private int paymentId;
    private LocalDateTime changedAt;
    private PaymentStatus status;

    public PaymentStatusLog() {
    }

    public PaymentStatusLog(Integer id, int paymentId, LocalDateTime changedAt, PaymentStatus status) {
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

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public String toString() {
        return "PaymentStatusLog{" +
                "id=" + id +
                ", paymentId=" + paymentId +
                ", changedAt=" + changedAt +
                ", status=" + status +
                '}';
    }
}