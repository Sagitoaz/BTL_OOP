package org.miniboot.app.domain.models.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Product model - đại diện cho sản phẩm trong hệ thống
 * Khớp với database schema: Products table
 */
public class Product {
    private int id;
    private String sku;
    private String name;
    private String category; // frame, lens, contact_lens, machine, consumable, service
    private String unit; // chiếc, hộp, dịch vụ...

    @JsonProperty("price_cost") // ✅ Map JSON: priceCost → price_cost
    private Integer price_cost; // Giá nhập (INT)

    @JsonProperty("price_retail") // ✅ Map JSON: priceRetail → price_retail
    private Integer price_retail; // Giá bán lẻ (INT)

    @JsonProperty("is_active") // ✅ Map JSON: isActive → is_active (chú ý: getter là isActive())
    private boolean isActive;

    @JsonProperty("qty_on_hand") // ✅ Map JSON: qtyOnHand → qty_on_hand
    private int qty_on_hand; // Số lượng tồn kho

    @JsonProperty("batch_no") // ✅ Map JSON: batchNo → batch_no
    private String batch_no; // Số lô (NULL nếu không quản theo lô)

    @JsonProperty("expiry_date") // ✅ Map JSON: expiryDate → expiry_date
    private LocalDate expiry_date; // Hạn sử dụng

    @JsonProperty("serial_no") // ✅ Map JSON: serialNo → serial_no
    private String serial_no; // Số serial

    private String note; // Ghi chú

    @JsonProperty("created_at") // ✅ Map JSON: createdAt → created_at
    private LocalDateTime created_at;

    // Constructors
    public Product() {
    }

    public Product(int id, String sku, String name, String category, String unit,
            Integer price_cost, Integer price_retail, boolean isActive, int qty_on_hand,
            String batch_no, LocalDate expiry_date, String serial_no,
            String note, LocalDateTime created_at) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.price_cost = price_cost;
        this.price_retail = price_retail;
        this.isActive = isActive;
        this.qty_on_hand = qty_on_hand;
        this.batch_no = batch_no;
        this.expiry_date = expiry_date;
        this.serial_no = serial_no;
        this.note = note;
        this.created_at = created_at;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getPrice_cost() {
        return price_cost;
    }

    public void setPrice_cost(Integer price_cost) {
        this.price_cost = price_cost;
    }

    public Integer getPrice_retail() {
        return price_retail;
    }

    public void setPrice_retail(Integer price_retail) {
        this.price_retail = price_retail;
    }

    // Backward compatibility - alias cho price_retail
    public Integer getPrice() {
        return price_retail;
    }

    public void setPrice(Integer price) {
        this.price_retail = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getQty_on_hand() {
        return qty_on_hand;
    }

    public void setQty_on_hand(int qty_on_hand) {
        this.qty_on_hand = qty_on_hand;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public LocalDate getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(LocalDate expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price_cost=" + price_cost +
                ", price_retail=" + price_retail +
                ", qty_on_hand=" + qty_on_hand +
                ", unit='" + unit + '\'' +
                ", isActive=" + isActive +
                ", batch_no='" + batch_no + '\'' +
                ", expiry_date=" + expiry_date +
                ", serial_no='" + serial_no + '\'' +
                ", note='" + note + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}