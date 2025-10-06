package org.example.oop.Model.Inventory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model quản lý đơn đặt hàng (Purchase Order)
 */
public class PurchaseOrder {
    private int id;
    private String poNumber; // PO-2025-001
    private int supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDate receivedDate;
    private String status; // DRAFT, PENDING, APPROVED, RECEIVED, CANCELLED
    private double totalAmount;
    private String currency;
    private String notes;
    private List<PurchaseOrderItem> items;

    // Constructors
    public PurchaseOrder() {
        this.items = new ArrayList<>();
        this.currency = "VND";
    }

    public PurchaseOrder(int id, String poNumber, int supplierId, String supplierName,
            LocalDate orderDate, LocalDate expectedDate, LocalDate receivedDate,
            String status, double totalAmount, String currency, String notes) {
        this.id = id;
        this.poNumber = poNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.receivedDate = receivedDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.notes = notes;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items;
    }

    // Utility methods
    public void addItem(PurchaseOrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(PurchaseOrderItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(PurchaseOrderItem::getTotalPrice)
                .sum();
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id=" + id +
                ", poNumber='" + poNumber + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
