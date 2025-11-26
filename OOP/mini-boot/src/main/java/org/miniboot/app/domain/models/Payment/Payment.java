package org.miniboot.app.domain.models.Payment;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

// Giả sử bạn có một enum PaymentMethod như sau:
// enum PaymentMethod {
// CASH, CREDIT_CARD, BANK_TRANSFER
// }

public class Payment {
    private Integer id;
    private String code;
    private Integer customerId;
    private int cashierId;
    private LocalDateTime issuedAt;
    private int subtotal, discount, taxTotal, rounding, grandTotal;
    private PaymentMethod paymentMethod; // null khi chưa chốt
    private Integer amountPaid; // null khi chưa chốt
    private String note;
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Integer id, String code, Integer customerId, int cashierId, LocalDateTime issuedAt,
                   int subtotal, int discount, int taxTotal, int rounding, int grandTotal,
                   PaymentMethod paymentMethod, Integer amountPaid, String note, LocalDateTime createdAt) {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getCashierId() {
        return cashierId;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(int taxTotal) {
        this.taxTotal = taxTotal;
    }

    public int getRounding() {
        return rounding;
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public int getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(int grandTotal) {
        this.grandTotal = grandTotal;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Integer amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt != null) {
            this.createdAt = createdAt;
        }
        this.createdAt = Timestamp.from(Instant.now()).toLocalDateTime();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", customerId=" + customerId +
                ", cashierId=" + cashierId +
                ", issuedAt=" + issuedAt +
                ", subtotal=" + subtotal +
                ", discount=" + discount +
                ", taxTotal=" + taxTotal +
                ", rounding=" + rounding +
                ", grandTotal=" + grandTotal +
                ", paymentMethod=" + paymentMethod +
                ", amountPaid=" + amountPaid +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}