package org.example.oop.Model.PaymentModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

// Giả sử bạn có một enum PaymentMethod như sau:
// enum PaymentMethod {
//     CASH, CREDIT_CARD, BANK_TRANSFER
// }

public class Payment {
    private Integer id;
    private String code;
    private Integer customerId;
    private int cashierId;
    private Instant issuedAt;
    private int subtotal, discount, taxTotal, rounding, grandTotal;
    private PaymentMethod paymentMethod;   // null khi chưa chốt
    private BigDecimal amountPaid;         // null khi chưa chốt
    private String note;
    private Instant createdAt;

    public Payment() {
    }

    public Payment(Integer id, String code, Integer customerId, int cashierId, Instant issuedAt,
                   int subtotal, int discount, int taxTotal, int rounding, int grandTotal,
                   PaymentMethod paymentMethod, BigDecimal amountPaid, String note, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.issuedAt = issuedAt;
        this.subtotal = subtotal;
        this.discount = discount;
        this.taxTotal = taxTotal;
        this.rounding = rounding;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.note = note;
        this.createdAt = createdAt;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    /**
     * Chuyển đổi đối tượng thành chuỗi dữ liệu, phân tách bằng dấu '|'.
     * Đã sửa lại để bao gồm cả thuộc tính 'code' và xử lý null nhất quán.
     */
    public String toDataString() {
        return String.join("|",
                id == null ? "" : String.valueOf(id),
                code == null ? "" : code,
                customerId == null ? "" : String.valueOf(customerId),
                String.valueOf(cashierId),
                issuedAt == null ? "" : issuedAt.toString(),
                String.valueOf(subtotal),
                String.valueOf(discount),
                String.valueOf(taxTotal),
                String.valueOf(rounding),
                String.valueOf(grandTotal),
                paymentMethod == null ? "" : paymentMethod.name(),
                amountPaid == null ? "" : amountPaid.toPlainString(),
                note == null ? "" : note,
                createdAt == null ? "" : createdAt.toString()
        );
    }

    /**
     * Tạo đối tượng Payment từ một chuỗi dữ liệu.
     */
    public static Payment fromDataString(String line) {
        // Sử dụng split(regex, -1) để giữ lại các chuỗi rỗng ở cuối nếu có
        String[] parts = line.split("\\|", -1);

        // Kiểm tra để đảm bảo chuỗi đầu vào có đủ phần tử
        if (parts.length < 14) {
            throw new IllegalArgumentException("Dữ liệu chuỗi không hợp lệ để tạo đối tượng Payment.");
        }

        Integer id = parts[0].isEmpty() ? null : Integer.parseInt(parts[0]);
        String code = parts[1].isEmpty() ? null : parts[1];
        Integer customerId = parts[2].isEmpty() ? null : Integer.parseInt(parts[2]);
        int cashierId = Integer.parseInt(parts[3]);
        Instant issuedAt = parts[4].isEmpty() ? null : Instant.parse(parts[4]);
        int subtotal = Integer.parseInt(parts[5]);
        int discount = Integer.parseInt(parts[6]);
        int taxTotal = Integer.parseInt(parts[7]);
        int rounding = Integer.parseInt(parts[8]);
        int grandTotal = Integer.parseInt(parts[9]);
        PaymentMethod paymentMethod = parts[10].isEmpty() ? null : PaymentMethod.valueOf(parts[10]);
        BigDecimal amountPaid = parts[11].isEmpty() ? null : new BigDecimal(parts[11]);
        String note = parts[12].isEmpty() ? null : parts[12];
        Instant createdAt = parts[13].isEmpty() ? null : Instant.parse(parts[13]);

        return new Payment(
                id, code, customerId, cashierId, issuedAt,
                subtotal, discount, taxTotal, rounding, grandTotal,
                paymentMethod, amountPaid, note, createdAt
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(code, payment.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}