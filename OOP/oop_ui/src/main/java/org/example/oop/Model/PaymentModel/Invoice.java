package org.example.oop.Model.PaymentModel;

import java.util.List;

/**
 * Lớp Model chứa dữ liệu cho một hóa đơn (Invoice).
 * Dữ liệu này sẽ được Controller lấy ra để hiển thị lên FXML View.
 */
public class Invoice {
    private final String invoiceNumber; // Có thể thêm số hóa đơn riêng nếu cần
    private final Payment payment;
    private final List<PaymentItem> items;

    public Invoice(String invoiceNumber, Payment payment, List<PaymentItem> items) {
        this.invoiceNumber = invoiceNumber;
        this.payment = payment;
        this.items = items;
    }

    // Getters để Controller có thể truy cập dữ liệu
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Payment getPayment() {
        return payment;
    }

    public List<PaymentItem> getItems() {
        return items;
    }
}