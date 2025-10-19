package org.miniboot.app.domain.repo.Payment;

public interface PaymentRepository {
    List<Payment> getPayments();

    Payment getPaymentById(long id);

    void addPayment(Payment payment);

    void updatePayment(Payment payment);
}
