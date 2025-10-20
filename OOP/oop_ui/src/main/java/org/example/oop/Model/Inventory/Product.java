// filepath: oop_ui/src/main/java/org/example/oop/Model/Inventory/Product.java
package org.example.oop.Model.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.oop.Model.Inventory.Enum.Category;

import com.google.gson.annotations.SerializedName;

/**
 * Product Model - Khớp 100% với Database Schema
 * 
 * Database: Products table
 * - category:
 * ENUM('frame','lens','contact_lens','machine','consumable','service')
 * - is_active: BOOLEAN (default: true)
 * - Không có InventoryStatus enum
 * 
 * ⚠️ Backend JSON dùng snake_case, Java dùng camelCase
 */
public class Product {

    // ====================
    // FIELDS - Khớp 100% với DB (với @SerializedName mapping)
    // ====================

    private int id; // PK

    private String sku; // VARCHAR(40) UNIQUE NOT NULL

    private String name; // NVARCHAR(200) NOT NULL

    // ⚠️ Backend JSON trả "category":"service" (lowercase String)
    // Gson không thể tự động convert String → Enum
    // Solution: Dùng String, getter/setter handle conversion
    private String category; // Backend JSON: "category": "service"

    private String unit; // NVARCHAR(20)

    @SerializedName("price_cost") // ✅ Map JSON snake_case → Java camelCase
    private Integer priceCost; // INT (nullable)

    @SerializedName("price_retail") // ✅ Map JSON snake_case → Java camelCase
    private Integer priceRetail; // INT (nullable)

    @SerializedName("is_active") // ✅ Map JSON snake_case → Java camelCase
    private boolean isActive; // ✅ BOOLEAN (default: true)

    @SerializedName("qty_on_hand") // ✅ Map JSON snake_case → Java camelCase
    private int qtyOnHand; // INT NOT NULL DEFAULT 0

    @SerializedName("batch_no") // ✅ Map JSON snake_case → Java camelCase
    private String batchNo; // VARCHAR(40) (nullable)

    @SerializedName("expiry_date") // ✅ Map JSON snake_case → Java camelCase
    private LocalDate expiryDate; // DATE (nullable)

    @SerializedName("serial_no") // ✅ Map JSON snake_case → Java camelCase
    private String serialNo; // VARCHAR(60) (nullable)

    private String note; // NVARCHAR(255) (nullable)

    @SerializedName("created_at") // ✅ Map JSON snake_case → Java camelCase
    private LocalDateTime createdAt; // DATETIME

    // ====================
    // CONSTRUCTORS
    // ====================

    /**
     * Constructor đầy đủ - Dùng khi tạo mới Product với ENUM
     */
    public Product(int id, String sku, String name, Category category, String unit,
            Integer priceCost, Integer priceRetail, boolean isActive, int qtyOnHand,
            String batchNo, LocalDate expiryDate, String serialNo, String note,
            LocalDateTime createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = (category != null) ? category.getCode() : null; // Enum → String
        this.unit = unit;
        this.priceCost = priceCost;
        this.priceRetail = priceRetail;
        this.isActive = isActive;
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
        this.isActive = true; // Default: Hoạt động
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

    /**
     * Get category as Enum (for business logic)
     */
    public Category getCategory() {
        return Category.fromCode(category); // String → Enum
    }

    /**
     * Get category code as String (for JSON/DB)
     */
    public String getCategoryCode() {
        return category; // Return raw String
    }

    /**
     * Set category from Enum
     */
    public void setCategory(Category category) {
        this.category = (category != null) ? category.getCode() : null; // Enum → String
    }

    /**
     * Set category from String code (for JSON deserialization)
     */
    public void setCategoryCode(String categoryCode) {
        this.category = categoryCode; // Save raw String
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

    /**
     * Getter/Setter cho isActive (boolean)
     * - true: Hoạt động
     * - false: Ngừng hoạt động
     */
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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