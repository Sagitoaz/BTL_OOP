// filepath: oop_ui/src/main/java/org/example/oop/Model/Inventory/Product.java
package org.example.oop.Model.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.oop.Model.Inventory.Enum.Category;
import org.example.oop.Model.Inventory.Enum.InventoryStatus;

/**
 * Product Model cho UI với ENUM support
 * 
 * ⚠️ Model này KHÁC với backend Product.java trong mini-boot
 * ✅ Sử dụng ENUM cho category và status
 * ✅ Deserialize từ JSON response của API (GsonProvider tự động convert)
 */
public class Product {

    // ====================
    // FIELDS - Khớp với backend JSON response
    // ====================

    private int id; // Primary key
    private String sku; // Mã hàng (unique)
    private String name; // Tên sản phẩm
    private Category category; // ✅ ENUM: Medication, Equipment, Supplies, Consumables
    private String unit; // Đơn vị: Chiếc, Hộp, Dịch vụ...
    private Integer priceCost; // Giá nhập (có thể null)
    private Integer priceRetail; // Giá bán lẻ (có thể null)
    private InventoryStatus status; // ✅ ENUM: Active, Discontinued, Out of Stock, Low Stock
    private int qtyOnHand; // Số lượng tồn kho
    private String batchNo; // Số lô (nullable)
    private LocalDate expiryDate; // Hạn sử dụng (nullable)
    private String serialNo; // Serial number (nullable)
    private String note; // Ghi chú (nullable)
    private LocalDateTime createdAt; // Thời gian tạo

    // ====================
    // CONSTRUCTORS
    // ====================

    /**
     * Constructor đầy đủ - Dùng khi tạo mới Product với ENUM
     */
    public Product(int id, String sku, String name, Category category, String unit,
            Integer priceCost, Integer priceRetail, InventoryStatus status, int qtyOnHand,
            String batchNo, LocalDate expiryDate, String serialNo, String note,
            LocalDateTime createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.priceCost = priceCost;
        this.priceRetail = priceRetail;
        this.status = status;
        this.qtyOnHand = qtyOnHand;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.serialNo = serialNo;
        this.note = note;
        this.createdAt = createdAt;
    }

    /**
     * Constructor rỗng - Gson cần để deserialize JSON
     */
    public Product() {
        this.status = InventoryStatus.ACTIVE; // Default
    }

    // ====================
    // GETTERS & SETTERS
    // ====================

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(Integer priceCost) {
        this.priceCost = priceCost;
    }

    public Integer getPriceRetail() {
        return priceRetail;
    }

    public void setPriceRetail(Integer priceRetail) {
        this.priceRetail = priceRetail;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    /**
     * Helper method để check active status
     */
    public boolean isActive() {
        return status == InventoryStatus.ACTIVE;
    }

    public int getQtyOnHand() {
        return qtyOnHand;
    }

    public void setQtyOnHand(int qtyOnHand) {
        this.qtyOnHand = qtyOnHand;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
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

    // ====================
    // HELPER METHODS
    // ====================

    /**
     * Format giá tiền Việt Nam
     */
    public String getFormattedPriceCost() {
        if (priceCost == null)
            return "N/A";
        return String.format("%,d đ", priceCost);
    }

    public String getFormattedPriceRetail() {
        if (priceRetail == null)
            return "N/A";
        return String.format("%,d đ", priceRetail);
    }

    /**
     * Hiển thị expiry date dễ đọc
     */
    public String getFormattedExpiryDate() {
        if (expiryDate == null)
            return "N/A";
        return expiryDate.toString();
    }

    /**
     * Trạng thái tồn kho
     */
    public String getStockStatus() {
        if (qtyOnHand == 0)
            return "Hết hàng";
        if (qtyOnHand < 10)
            return "Sắp hết";
        return "Còn hàng";
    }

    /**
     * Màu sắc cho stock status (dùng trong TableView)
     */
    public String getStockStatusColor() {
        if (qtyOnHand == 0)
            return "red";
        if (qtyOnHand < 10)
            return "orange";
        return "green";
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", qtyOnHand=" + qtyOnHand +
                ", priceRetail=" + priceRetail +
                '}';
    }
}