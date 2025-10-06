package org.example.oop.Model.Inventory;

import java.time.LocalDate;

/**
 * Model để hiển thị dữ liệu inventory trong TableView
 */
public class InventoryRow {
    private Integer id;
    private String sku;
    private String name;
    private String type;
    private String category;
    private Integer quantity;
    private String unit;
    private Integer unitPrice;
    private LocalDate lastUpdated;
    private String supplier;
    private String status;
    private Integer reorderLevel;
    private String stockStatus; // LOW_STOCK, IN_STOCK, OUT_OF_STOCK

    // Constructor cũ (giữ nguyên để tương thích)
    public InventoryRow(int id, String name, String type, String category,
            int quantity, String unit, int unitPrice, LocalDate lastUpdated) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.lastUpdated = lastUpdated;
    }

    // Constructor đầy đủ
    public InventoryRow(Integer id, String sku, String name, String type, String category,
            Integer quantity, String unit, Integer unitPrice, LocalDate lastUpdated,
            String supplier, String status, Integer reorderLevel, String stockStatus) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.lastUpdated = lastUpdated;
        this.supplier = supplier;
        this.status = status;
        this.reorderLevel = reorderLevel;
        this.stockStatus = stockStatus;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        updateStockStatus();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
        updateStockStatus();
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    // Utility method để tự động cập nhật stock status
    private void updateStockStatus() {
        if (quantity == null) {
            stockStatus = "UNKNOWN";
        } else if (quantity == 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (reorderLevel != null && quantity <= reorderLevel) {
            stockStatus = "LOW_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }
    }

    @Override
    public String toString() {
        return "InventoryRow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", stockStatus='" + stockStatus + '\'' +
                '}';
    }
}
