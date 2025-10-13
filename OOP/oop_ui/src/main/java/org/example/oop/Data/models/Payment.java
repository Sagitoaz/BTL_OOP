package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Payment - đại diện cho một giao dịch thanh toán trong hệ thống.
 * Theo database mới: customer_id có thể NULL, cashier_id là int ref to Employees
 */
public class Payment {
    private int id;
    private String code; // Số chứng từ/in biên lai
    private Integer customerId; // null khi chưa đk tài khoản
    private int cashierId; // id của employee
    private LocalDateTime issuedAt;

    // Tổng tiền
    private int subtotal;
    private int discount;
    private int taxTotal;
    private int rounding;
    private int grandTotal;

    // Thanh toán 1 lần tại quầy
    private PaymentMethod paymentMethod;
    private double amountPaid; // = grand_total khi thanh toán xong

    private String note;
    private LocalDateTime createdAt;

    /**
     * Constructor đầy đủ
     */
    public Payment(int id, String code, Integer customerId, int cashierId, LocalDateTime issuedAt,
                   int subtotal, int discount, int taxTotal, int rounding, int grandTotal,
                   PaymentMethod paymentMethod, double amountPaid, String note, LocalDateTime createdAt) {
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

    /**
     * Constructor cho payment mới
     */
    public Payment(int id, String code, Integer customerId, int cashierId,
                   int subtotal, int discount, int taxTotal, int rounding, int grandTotal,
                   PaymentMethod paymentMethod) {
        this(id, code, customerId, cashierId, LocalDateTime.now(),
             subtotal, discount, taxTotal, rounding, grandTotal,
             paymentMethod, 0.0, null, LocalDateTime.now());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
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
        this.createdAt = createdAt;
    }

    /**
     * Chuyển đổi Payment thành chuỗi để lưu vào file
     * Format: id|code|customer_id|cashier_id|issued_at|subtotal|discount|tax_total|rounding|grand_total|payment_method|amount_paid|note|created_at
     */
    public String toFileFormat() {
        return String.join("|",
                String.valueOf(id),
                code != null ? code : "",
                customerId != null ? String.valueOf(customerId) : "",
                String.valueOf(cashierId),
                issuedAt.toString(),
                String.valueOf(subtotal),
                String.valueOf(discount),
                String.valueOf(taxTotal),
                String.valueOf(rounding),
                String.valueOf(grandTotal),
                paymentMethod != null ? paymentMethod.getValue() : "",
                String.valueOf(amountPaid),
                note != null ? note : "",
                createdAt.toString()
        );
    }

    /**
     * Tạo Payment từ chuỗi trong file
     * Format: id|code|customer_id|cashier_id|issued_at|subtotal|discount|tax_total|rounding|grand_total|payment_method|amount_paid|note|created_at
     */
    public static Payment fromFileFormat(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 14) {
            throw new IllegalArgumentException("Invalid payment format: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String code = parts[1].isEmpty() ? null : parts[1];
        Integer customerId = parts[2].isEmpty() ? null : Integer.parseInt(parts[2]);
        int cashierId = Integer.parseInt(parts[3]);
        LocalDateTime issuedAt = LocalDateTime.parse(parts[4]);
        int subtotal = Integer.parseInt(parts[5]);
        int discount = Integer.parseInt(parts[6]);
        int taxTotal = Integer.parseInt(parts[7]);
        int rounding = Integer.parseInt(parts[8]);
        int grandTotal = Integer.parseInt(parts[9]);
        PaymentMethod paymentMethod = parts[10].isEmpty() ? null : PaymentMethod.fromValue(parts[10]);
        double amountPaid = Double.parseDouble(parts[11]);
        String note = parts[12].isEmpty() ? null : parts[12];
        LocalDateTime createdAt = LocalDateTime.parse(parts[13]);

        return new Payment(id, code, customerId, cashierId, issuedAt,
                subtotal, discount, taxTotal, rounding, grandTotal,
                paymentMethod, amountPaid, note, createdAt);
    }
}
