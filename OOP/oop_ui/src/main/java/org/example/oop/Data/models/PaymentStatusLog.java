package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * PaymentStatusLog - lịch sử thay đổi trạng thái thanh toán.
 * Theo database: Payment_Status_Log
 */
public class PaymentStatusLog {
    private int id;
    private int paymentId;
    private LocalDateTime changedAt;
    private PaymentStatus status;

    public PaymentStatusLog(int id, int paymentId, LocalDateTime changedAt, PaymentStatus status) {
        this.id = id;
        this.paymentId = paymentId;
        this.changedAt = changedAt;
        this.status = status;
    }

    public PaymentStatusLog(int id, int paymentId, PaymentStatus status) {
        this(id, paymentId, LocalDateTime.now(), status);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}

