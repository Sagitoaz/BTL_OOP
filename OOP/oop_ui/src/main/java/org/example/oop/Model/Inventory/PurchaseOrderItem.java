package org.example.oop.Model.Inventory;

/**
 * Model đại diện cho từng item trong Purchase Order
 */
public class PurchaseOrderItem {
    private int id;
    private int poId;
    private int productId;
    private String productName;
    private int quantityOrdered;
    private int quantityReceived;
    private double unitPrice;
    private double totalPrice;

    // Constructors
    public PurchaseOrderItem() {
    }

    public PurchaseOrderItem(int id, int poId, int productId, String productName,
            int quantityOrdered, int quantityReceived,
            double unitPrice, double totalPrice) {
        this.id = id;
        this.poId = poId;
        this.productId = productId;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPoId() {
        return poId;
    }

    public void setPoId(int poId) {
        this.poId = poId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
        calculateTotal();
    }

    public int getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(int quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Utility method
    public void calculateTotal() {
        this.totalPrice = this.quantityOrdered * this.unitPrice;
    }

    @Override
    public String toString() {
        return "PurchaseOrderItem{" +
                "productName='" + productName + '\'' +
                ", quantityOrdered=" + quantityOrdered +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
