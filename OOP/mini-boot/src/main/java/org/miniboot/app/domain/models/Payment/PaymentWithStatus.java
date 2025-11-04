package org.miniboot.app.domain.models.Payment;

public class PaymentWithStatus {
    private Payment payment;
    private PaymentStatus status;

    // Constructor
    public PaymentWithStatus(Payment payment, PaymentStatus status) {
        this.payment = payment;
        this.status = status;
    }

    // Getters
    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
