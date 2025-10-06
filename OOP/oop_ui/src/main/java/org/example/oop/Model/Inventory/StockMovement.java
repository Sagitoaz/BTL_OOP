package org.example.oop.Model.Inventory;

import java.time.LocalDateTime;

/**
 * Model quản lý các giao dịch xuất nhập kho
 */
public class StockMovement {
    private int id;
    private int productId;
    private String movementType; // IN, OUT, ADJUSTMENT, RETURN
    private int quantityBefore;
    private int quantityChange;
    private int quantityAfter;
    private String reason;
    private String reference; // Mã tham chiếu (PO number, invoice number)
    private LocalDateTime movedAt;
    private String movedBy; // User ID
    private String notes;

    // Constructors
    public StockMovement() {
    }

    public StockMovement(int id, int productId, String movementType,
            int quantityBefore, int quantityChange, int quantityAfter,
            String reason, String reference, LocalDateTime movedAt,
            String movedBy, String notes) {
        this.id = id;
        this.productId = productId;
        this.movementType = movementType;
        this.quantityBefore = quantityBefore;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.reference = reference;
        this.movedAt = movedAt;
        this.movedBy = movedBy;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(int quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public int getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(int quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getMovedAt() {
        return movedAt;
    }

    public void setMovedAt(LocalDateTime movedAt) {
        this.movedAt = movedAt;
    }

    public String getMovedBy() {
        return movedBy;
    }

    public void setMovedBy(String movedBy) {
        this.movedBy = movedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", productId=" + productId +
                ", movementType='" + movementType + '\'' +
                ", quantityChange=" + quantityChange +
                ", movedAt=" + movedAt +
                '}';
    }
}
