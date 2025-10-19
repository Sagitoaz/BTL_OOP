package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.domain.models.Payment.PaymentStatus;

public interface PaymentStatusLogRepository {
    PaymentStatus getCurrentPaymentStatus(int paymentId);

    PaymentStatus setCurrentPaymentStatus(int paymentId, PaymentStatus paymentStatus);
}
