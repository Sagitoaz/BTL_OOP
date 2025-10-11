package org.example.oop.Model.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model đại diện cho một sản phẩm trong kho.
 * Được dùng cho cả UI (TableView) và business logic.
 */
public class Inventory {
    // === BASIC INFO ===
    private int id;
    private String sku; // Mã SKU (bắt buộc)
    private String name; // Tên sản phẩm
    private String type; // Loại: Medication/Equipment/Supplies
    private String category; // Danh mục con: Painkiller/Diagnostic...

    // === STOCK INFO ===
    private int quantity; // Số lượng tồn kho
    private String unit; // Đơn vị: tablet/box/unit/pcs
    private Integer unitPrice; // Giá bán (VND)
    private Integer priceCost; // Giá vốn (VND)

    // === METADATA ===
    private LocalDate lastUpdated; // Ngày cập nhật cuối (dùng LocalDate để khớp file)
    private LocalDateTime createdAt; // Thời điểm tạo
    private String updatedBy; // User cập nhật

    // === STOCK MANAGEMENT ===
    private Integer reorderLevel; // Ngưỡng cảnh báo (LOW_STOCK)
    private Integer reorderQuantity; // Số lượng đặt lại mặc định
    private String location; // Vị trí trong kho

    // === STATUS ===
    private boolean active = true; // Còn kinh doanh hay không
    private String status;
    private String note; // Ghi chú

    // === CONSTRUCTORS ===
    public Inventory() {
        this.active = true;
    }

    public Inventory(int id, String name, String type, String category,
            int quantity, String unit, Integer unitPrice, Integer priceCost, LocalDate lastUpdated) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.priceCost = priceCost;
        this.lastUpdated = lastUpdated;
        this.active = true;
    }

    // === GETTERS & SETTERS ===

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(Integer priceCost) {
        this.priceCost = priceCost;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // === BUSINESS LOGIC ===

    /**
     * Tính trạng thái tồn kho.
     * 
     * @return "OUT_OF_STOCK", "LOW_STOCK", hoặc "IN_STOCK"
     */
    public String getStockStatus() {
        return this.status;
    }

    public void setStockStatus(String a) {
        this.status = a;
    }

    public void setStockStatus() {
        if (quantity <= 0) {
            this.status = "OUT_OF_STOCK";
        }
        if (reorderLevel != null && quantity < reorderLevel) {
            this.status = "LOW_STOCK";
        }
        this.status = "IN_STOCK";
    }

    /**
     * Kiểm tra có phải low stock không.
     */
    public boolean isLowStock() {
        return "LOW_STOCK".equals(getStockStatus());
    }

    /**
     * Kiểm tra có phải out of stock không.
     */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    /**
     * Validate dữ liệu cơ bản.
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty())
            return false;
        if (sku == null || sku.trim().isEmpty())
            return false;
        if (quantity < 0)
            return false;
        if (unitPrice < 0)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("Inventory[id=%d, sku=%s, name=%s, qty=%d, status=%s]",
                id, sku, name, quantity, getStockStatus());
    }
}