package org.example.oop.Model;

import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentItem;

import java.util.List;


/**
 * Lớp Model chứa dữ liệu cho một biên lai (Receipt).
 * Xác nhận một giao dịch đã được thanh toán thành công.
 */
public class Receipt {
    private final String receiptNumber; // Số biên lai riêng
    private final Payment payment;
    private final List<PaymentItem> items;

    public Receipt(String receiptNumber, Payment payment, List<PaymentItem> items) {
        // Quy tắc nghiệp vụ: Chỉ tạo biên lai cho giao dịch đã thanh toán
        if (payment.getPaymentMethod() == null || payment.getAmountPaid() == null) {
            throw new IllegalStateException("Cannot create a receipt for an unpaid payment.");
        }
        this.receiptNumber = receiptNumber;
        this.payment = payment;
        this.items = items;
    }

    // Getters để Controller truy cập
    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Payment getPayment() {
        return payment;
    }

    public List<PaymentItem> getItems() {
        return items;
    }

}