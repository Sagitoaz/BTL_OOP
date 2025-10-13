package org.example.oop.Data.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp Product - đại diện cho một sản phẩm trong kho của phòng khám.
 * Theo database mới: thêm qty_on_hand, batch_no, expiry_date, serial_no
 */
public class Product {
    private int id;
    private String sku;
    private String name;
    private ProductCategory category;
    private String unit;
    private int priceCost; // giá nhập
    private int priceRetail; // giá bán lẻ mặc định
    private boolean isActive;
    private int qtyOnHand; // số lượng tồn kho
    private String batchNo; // NULL nếu không quản theo lô
    private LocalDate expiryDate;
    private String serialNo;
    private String note;
    private LocalDateTime createdAt;

    /**
     * Constructor đầy đủ
     */
    public Product(int id, String sku, String name, ProductCategory category, String unit,
                   int priceCost, int priceRetail, boolean isActive, int qtyOnHand,
                   String batchNo, LocalDate expiryDate, String serialNo, String note,
                   LocalDateTime createdAt) {
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

    /**
     * Constructor đơn giản cho sản phẩm mới
     */
    public Product(int id, String sku, String name, ProductCategory category, String unit,
                   int priceCost, int priceRetail) {
        this(id, sku, name, category, unit, priceCost, priceRetail, true, 0,
             null, null, null, null, LocalDateTime.now());
    }

    // Getters and Setters
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPriceCost() {
        return priceCost;
    }

    public void setPriceCost(int priceCost) {
        this.priceCost = priceCost;
    }

    public int getPriceRetail() {
        return priceRetail;
    }

    public void setPriceRetail(int priceRetail) {
        this.priceRetail = priceRetail;
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

    /**
     * Chuyển đổi Product thành chuỗi để lưu vào file
     * Format: id|sku|name|category|unit|price_cost|price_retail|is_active|qty_on_hand|batch_no|expiry_date|serial_no|note|created_at
     */
    public String toFileFormat() {
        return String.join("|",
                String.valueOf(id),
                sku,
                name,
                category.getValue(),
                unit != null ? unit : "",
                String.valueOf(priceCost),
                String.valueOf(priceRetail),
                String.valueOf(isActive),
                String.valueOf(qtyOnHand),
                batchNo != null ? batchNo : "",
                expiryDate != null ? expiryDate.toString() : "",
                serialNo != null ? serialNo : "",
                note != null ? note : "",
                createdAt.toString()
        );
    }

    /**
     * Tạo Product từ chuỗi trong file
     * Format: id|sku|name|category|unit|price_cost|price_retail|is_active|qty_on_hand|batch_no|expiry_date|serial_no|note|created_at
     */
    public static Product fromFileFormat(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 14) {
            throw new IllegalArgumentException("Invalid product format: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String sku = parts[1];
        String name = parts[2];
        ProductCategory category = ProductCategory.fromValue(parts[3]);
        String unit = parts[4].isEmpty() ? null : parts[4];
        int priceCost = Integer.parseInt(parts[5]);
        int priceRetail = Integer.parseInt(parts[6]);
        boolean isActive = Boolean.parseBoolean(parts[7]);
        int qtyOnHand = Integer.parseInt(parts[8]);
        String batchNo = parts[9].isEmpty() ? null : parts[9];
        LocalDate expiryDate = parts[10].isEmpty() ? null : LocalDate.parse(parts[10]);
        String serialNo = parts[11].isEmpty() ? null : parts[11];
        String note = parts[12].isEmpty() ? null : parts[12];
        LocalDateTime createdAt = LocalDateTime.parse(parts[13]);

        return new Product(id, sku, name, category, unit, priceCost, priceRetail,
                isActive, qtyOnHand, batchNo, expiryDate, serialNo, note, createdAt);
    }
}
