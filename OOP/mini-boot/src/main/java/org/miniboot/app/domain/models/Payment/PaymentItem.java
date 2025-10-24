package org.miniboot.app.domain.models.Payment;

public class PaymentItem {
    private Integer id;
    private Integer productId;
    private int paymentId;
    private String description;
    private int qty;
    private int unitPrice;
    private int totalLine;

    public PaymentItem() {
    }

    public PaymentItem(Integer id, Integer productId, int paymentId, String description, int qty, int unitPrice, int totalLine) {
        this.id = id;
        this.productId = productId;
        this.paymentId = paymentId;
        this.description = description;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.totalLine = totalLine;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;

    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(int totalLine) {
        this.totalLine = totalLine;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PaymentItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", paymentId=" + paymentId +
                ", description=" + description +
                ", qty=" + qty +
                ", unitPrice=" + unitPrice +
                ", totalLine=" + totalLine +
                "}";
    }
}