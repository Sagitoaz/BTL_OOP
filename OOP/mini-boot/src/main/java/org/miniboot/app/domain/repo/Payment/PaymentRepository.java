package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.domain.models.Payment.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    List<Payment> getPayments();

    Optional<Payment> getPaymentById(int id);

    Payment savePayment(Payment payment);
}
