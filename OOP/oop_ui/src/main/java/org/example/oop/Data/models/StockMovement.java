package org.example.oop.Data.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StockMovement - ghi nhận các lần xuất nhập kho.
 * Theo database: Stock_Movements
 */
public class StockMovement {
    private int id;
    private int productId;
    private int qty; // >0 nhập, <0 xuất
    private MoveType moveType;
    private String refTable; // ví dụ 'Payments','PurchaseOrders','InventoryTransfers'
    private Integer refId; // id chứng từ nguồn
    private String batchNo;
    private LocalDate expiryDate;
    private String serialNo;
    private LocalDateTime movedAt;
    private int movedBy; // id người move

    /**
     * Constructor đầy đủ
     */
    public StockMovement(int id, int productId, int qty, MoveType moveType,
                        String refTable, Integer refId, String batchNo, LocalDate expiryDate,
                        String serialNo, LocalDateTime movedAt, int movedBy) {
        this.id = id;
        this.productId = productId;
        this.qty = qty;
        this.moveType = moveType;
        this.refTable = refTable;
        this.refId = refId;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.serialNo = serialNo;
        this.movedAt = movedAt;
        this.movedBy = movedBy;
    }

    /**
     * Constructor đơn giản
     */
    public StockMovement(int id, int productId, int qty, MoveType moveType, int movedBy) {
        this(id, productId, qty, moveType, null, null, null, null, null,
             LocalDateTime.now(), movedBy);
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }

    public Integer getRefId() {
        return refId;
    }

    public void setRefId(Integer refId) {
        this.refId = refId;
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

    public LocalDateTime getMovedAt() {
        return movedAt;
    }

    public void setMovedAt(LocalDateTime movedAt) {
        this.movedAt = movedAt;
    }

    public int getMovedBy() {
        return movedBy;
    }

    public void setMovedBy(int movedBy) {
        this.movedBy = movedBy;
    }
}
