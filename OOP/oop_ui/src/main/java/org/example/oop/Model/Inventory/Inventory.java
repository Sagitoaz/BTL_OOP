package org.example.oop.Model.Inventory;

import java.time.LocalDateTime;

/**
 * Đại diện cho một mặt hàng trong kho (bản đơn giản để dùng với UI hiện tại).
 */
public class Inventory {
    // --- Trường dữ liệu cũ (đang được UI/loader dùng)
    private int id;
    private String name;
    private String type; // ví dụ: Medication / Supplies / E quipment (nếu bạn đang dùng)
    private String sku;
    private String category;
    private int quantity; // tồn hiện tại (đang hiển thị trên bảng)
    private String unit;
    private int unitPrice; // giá bán lẻ mặc định (đang hiển thị trên bảng)
    private LocalDateTime lastUpdated;
    // --- Trường bổ sung (theo schema/nhu cầu nghiệp vụ)
    private String supplier;
    private String description;
    private Integer price_cost; // giá nhập (Products.price_cost)
    private boolean active = true; // trạng thái hoạt động (Products.is_active)
    private String note; // ghi chú (Products.note)
    private LocalDateTime createdAt; // thời điểm tạo (Products.created_at)
    private String updatedBy; // user cập nhật lần cuối
    private Integer reorderLevel; // ngưỡng cảnh báo hết hàng
    private String location; // vị trí lưu trữ trong kho
    private Integer reorderQuantity; // số lượng đặt lại mặc định
    // --- Constructors ---

    public Inventory() {
    }

    public Inventory(int id,
            String name,
            String type,
            String category,
            int quantity,
            String unit,
            int unitPrice,
            LocalDateTime lastUpdated,
            String updateBy,
            Integer reorderLevel,
            String location,
            Integer reorderQuantity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updateBy;
        this.reorderLevel = reorderLevel;
        this.location = location;
        this.reorderQuantity = reorderQuantity;
    }

    // --- Getters (cũ – giữ nguyên tên để không gãy UI/loader) ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    // --- Getters/Setters đầy đủ (bổ sung) ---
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getDescription() {
        return description;
    }

    // Sửa lỗi: tham số trước đây tên "desciption" và gán nhầm biến
    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice_cost() {
        return price_cost;
    }

    public void setPrice_cost(Integer price_cost) {
        this.price_cost = price_cost;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Setters cho các trường cũ (nếu bạn cần cập nhật từ code khác) ---
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public boolean isLowStock() {
        if (reorderLevel == null || reorderLevel == 0) {
            return false;
        }
        return this.quantity < this.reorderLevel;
    }

    public boolean isOutOfStock() {
        return quantity == 0;
    }

    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (quantity < 0) {
            return false;
        }
        if (unitPrice < 0) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("Inventory[id=%d, sku=%s, name=%s, qty=%d, status=%s]",
                id, sku, name, quantity, active ? "ACTIVE" : "INACTIVE");
    }
}
