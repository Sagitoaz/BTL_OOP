package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Product - đại diện cho một sản phẩm trong kho của phòng khám.
 *
 * Ghi chú dành cho người duy trì:
 * - Các trường chính: id, sku, name, category, unit, priceCost, priceRetail, isActive, note, createdAt.
 * - Giá trị tiền được lưu dưới dạng int (đơn vị nhỏ nhất, ví dụ đồng), cân nhắc đổi sang long nếu cần.
 * - createdAt dùng để audit; khi parse từ file cần đảm bảo định dạng ISO-8601.
 *
 * Định dạng lưu file:
 * - toFileFormat() trả về chuỗi phân cách bởi '|': id|sku|name|category|unit|priceCost|priceRetail|isActive|note|createdAt
 * - fromFileFormat() giả sử file có đầy đủ 10 phần và dùng parse/parseInt/Boolean.parseBoolean cho các trường tương ứng.
 *
 * Lưu ý khi mở rộng:
 * - Nếu thêm trường mới, cập nhật cả toFileFormat() và fromFileFormat().
 * - Xử lý ngoại lệ khi parse dữ liệu từ file (NumberFormatException, DateTimeParseException, ArrayIndexOutOfBoundsException).
 */
public class Product {
    private String id;
    private String sku;
    private String name;
    private ProductCategory category;
    private String unit;
    private int priceCost;
    private int priceRetail;
    private boolean isActive;
    private String note;
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a new Product instance.
     */
    public Product(String id, String sku, String name, ProductCategory category,
                   String unit, int priceCost, int priceRetail, boolean isActive, String note,
                   LocalDateTime createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.priceCost = priceCost;
        this.priceRetail = priceRetail;
        this.isActive = isActive;
        this.note = note;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    // Convert to file format: id|sku|name|category|unit|priceCost|priceRetail|isActive|note|createdAt
    public String toFileFormat() {
        return String.join("|",
                id, sku, name, category.name(), unit,
                String.valueOf(priceCost), String.valueOf(priceRetail), String.valueOf(isActive),
                note, createdAt.toString()
        );
    }

    // Parse from file format
    public static Product fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        return new Product(
                parts[0], parts[1], parts[2], ProductCategory.valueOf(parts[3]), parts[4],
                Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Boolean.parseBoolean(parts[7]),
                parts[8], LocalDateTime.parse(parts[9])
        );
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", unit='" + unit + '\'' +
                ", priceCost=" + priceCost +
                ", priceRetail=" + priceRetail +
                ", isActive=" + isActive +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
