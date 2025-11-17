package org.miniboot.app.domain.models.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PaymentWithStatus {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("customerId")
    private Integer customerId;
    @JsonProperty("cashierId")
    private Integer cashierId;
    @JsonProperty("issuedAt")
    private LocalDateTime issuedAt;
    @JsonProperty("subtotal")
    private Integer subtotal;
    @JsonProperty("discount")
    private Integer discount;
    @JsonProperty("taxTotal")
    private Integer taxTotal;
    @JsonProperty("rounding")
    private Integer rounding;
    @JsonProperty("grandTotal")
    private Integer grandTotal;
    @JsonProperty("paymentMethod")
    private PaymentMethod paymentMethod;
    @JsonProperty("amountPaid")
    private Integer amountPaid;
    @JsonProperty("note")
    private String note;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonProperty("status")
    private PaymentStatus status;
    @JsonProperty("statusUpdatedAt")
    private LocalDateTime statusUpdatedAt;

    public PaymentWithStatus() {}

    public PaymentWithStatus(Payment payment, PaymentStatus status, LocalDateTime statusUpdatedAt) {
        this.id = payment.getId();
        this.code = payment.getCode();
        this.customerId = payment.getCustomerId();
        this.cashierId = payment.getCashierId();
        this.issuedAt = payment.getIssuedAt();
        this.subtotal = payment.getSubtotal();
        this.discount = payment.getDiscount();
        this.taxTotal = payment.getTaxTotal();
        this.rounding = payment.getRounding();
        this.grandTotal = payment.getGrandTotal();
        this.paymentMethod = payment.getPaymentMethod();
        this.amountPaid = payment.getAmountPaid();
        this.note = payment.getNote();
        this.createdAt = payment.getCreatedAt();
        this.status = status;
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public Payment getPayment() {
        Payment payment = new Payment();
        payment.setId(this.id);
        payment.setCode(this.code);
        payment.setCustomerId(this.customerId);
        payment.setCashierId(this.cashierId);
        payment.setIssuedAt(this.issuedAt);
        payment.setSubtotal(this.subtotal);
        payment.setDiscount(this.discount);
        payment.setTaxTotal(this.taxTotal);
        payment.setRounding(this.rounding);
        payment.setGrandTotal(this.grandTotal);
        payment.setPaymentMethod(this.paymentMethod);
        payment.setAmountPaid(this.amountPaid);
        payment.setNote(this.note);
        payment.setCreatedAt(this.createdAt);
        return payment;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getCashierId() { return cashierId; }
    public void setCashierId(Integer cashierId) { this.cashierId = cashierId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public Integer getSubtotal() { return subtotal; }
    public void setSubtotal(Integer subtotal) { this.subtotal = subtotal; }
    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }
    public Integer getTaxTotal() { return taxTotal; }
    public void setTaxTotal(Integer taxTotal) { this.taxTotal = taxTotal; }
    public Integer getRounding() { return rounding; }
    public void setRounding(Integer rounding) { this.rounding = rounding; }
    public Integer getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Integer grandTotal) { this.grandTotal = grandTotal; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public Integer getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Integer amountPaid) { this.amountPaid = amountPaid; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }
}

