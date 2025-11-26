package org.miniboot.app.domain.models.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.miniboot.app.domain.models.Inventory.Enum.Category;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Product model - đại diện cho sản phẩm trong hệ thống
 * Khớp với database schema: Products table
 */
public class Product {
    private int id;
    private String sku;
    private String name;
    private String category; // frame, lens, contact_lens, machine, consumable, service (stored as String for
                             // DB)
    private String unit; // chiếc, hộp, dịch vụ...

    @JsonProperty("price_cost") //  Map JSON: priceCost → price_cost
    private Integer priceCost; // Giá nhập (INT)

    @JsonProperty("price_retail") //  Map JSON: priceRetail → price_retail
    private Integer priceRetail; // Giá bán lẻ (INT)

    @JsonProperty("is_active") //  Map JSON: isActive → is_active (chú ý: getter là isActive())
    private boolean isActive;

    @JsonProperty("qty_on_hand") //  Map JSON: qtyOnHand → qty_on_hand
    private int qtyOnHand; // Số lượng tồn kho

    @JsonProperty("batch_no") //  Map JSON: batchNo → batch_no
    private String batchNo; // Số lô (NULL nếu không quản theo lô)

    @JsonProperty("expiry_date") //  Map JSON: expiryDate → expiry_date
    private LocalDate expiryDate; // Hạn sử dụng

    @JsonProperty("serial_no") //  Map JSON: serialNo → serial_no
    private String serialNo; // Số serial

    private String note; // Ghi chú

    @JsonProperty("created_at") //  Map JSON: createdAt → created_at
    private LocalDateTime createdAt;

    // Constructors
    public Product() {
    }

    public Product(int id, String sku, String name, String category, String unit,
            Integer priceCost, Integer priceRetail, boolean isActive, int qtyOnHand,
            String batchNo, LocalDate expiryDate, String serialNo,
            String note, LocalDateTime createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
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

    // Getters & Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Alias for backward compatibility
    public String getCategoryCode() {
        return category;
    }

    public void setCategoryCode(String category) {
        this.category = category;
    }

    // Category Enum support
    public Category getCategoryEnum() {
        return Category.fromCode(category);
    }

    public void setCategoryEnum(Category category) {
        this.category = (category != null) ? category.getCode() : null;
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

    // Backward compatibility - alias cho price_retail
    public Integer getPrice() {
        return priceRetail;
    }

    public void setPrice(Integer price) {
        this.priceRetail = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", priceCost=" + priceCost +
                ", priceRetail=" + priceRetail +
                ", qtyOnHand=" + qtyOnHand +
                ", unit='" + unit + '\'' +
                ", isActive=" + isActive +
                ", batchNo='" + batchNo + '\'' +
                ", expiryDate=" + expiryDate +
                ", serialNo='" + serialNo + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}