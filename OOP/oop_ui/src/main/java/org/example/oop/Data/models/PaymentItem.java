package org.example.oop.Data.models;

/**
 * PaymentItem - chi tiết hàng trong hóa đơn.
 * Theo database: Payment_Items
 */
public class PaymentItem {
    private int id;
    private int productId;
    private int paymentId;
    private String description;
    private int qty;
    private int unitPrice;
    private int totalLine; // qty * unitPrice

    public PaymentItem(int id, int productId, int paymentId, String description,
                       int qty, int unitPrice, int totalLine) {
        this.id = id;
        this.productId = productId;
        this.paymentId = paymentId;
        this.description = description;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.totalLine = totalLine;
    }

    public PaymentItem(int id, int productId, int paymentId, String description,
                       int qty, int unitPrice) {
        this(id, productId, paymentId, description, qty, unitPrice, qty * unitPrice);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
        this.totalLine = qty * this.unitPrice; // auto update
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
        this.totalLine = this.qty * unitPrice; // auto update
    }

    public int getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(int totalLine) {
        this.totalLine = totalLine;
    }
}

