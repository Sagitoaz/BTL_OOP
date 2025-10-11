package org.example.oop.Model.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model quản lý các giao dịch xuất nhập kho
 * Khớp với database schema: Stock_Movements table
 */
public class StockMovement {
    private int id;
    private int productId;
    private int qty; // >0 nhập, <0 xuất
    private String moveType; // purchase, sale, return_in, return_out, adjustment, consume, transfer
    private String refTable; // Bảng tham chiếu: Payments, PurchaseOrders, InventoryTransfers...
    private Integer refId; // ID của chứng từ nguồn
    private String batchNo; // Số lô
    private LocalDate expiryDate; // Hạn sử dụng
    private String serialNo; // Số serial (cho thiết bị y tế)
    private LocalDateTime movedAt;
    private int movedBy; // ID người thực hiện (int, not String)

    // Constructors
    public StockMovement() {
    }

    public StockMovement(int id, int productId, int qty, String moveType,
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

    public String getMoveType() {
        return moveType;
    }

    public void setMoveType(String moveType) {
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

    // Utility methods
    public boolean isValid() {
        if (productId <= 0) {
            return false;
        }
        if (moveType == null || moveType.trim().isEmpty()) {
            return false;
        }
        if (qty == 0) {
            return false;
        }
        if (movedBy <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra xem giao dịch có phải là nhập kho không (qty > 0)
     */
    public boolean isInMovement() {
        return qty > 0;
    }

    /**
     * Kiểm tra xem giao dịch có phải là xuất kho không (qty < 0)
     */
    public boolean isOutMovement() {
        return qty < 0;
    }

    /**
     * Lấy giá trị tuyệt đối của qty
     */
    public int getAbsoluteQty() {
        return Math.abs(qty);
    }

    // File I/O methods
    /**
     * Chuyển đổi object sang dòng text để lưu file
     * Format:
     * id|productId|qty|moveType|refTable|refId|batchNo|expiryDate|serialNo|movedAt|movedBy
     */
    public String toFileString() {
        return id + "|" +
                productId + "|" +
                qty + "|" +
                (moveType != null ? moveType : "") + "|" +
                (refTable != null ? refTable : "") + "|" +
                (refId != null ? refId : "") + "|" +
                (batchNo != null ? batchNo : "") + "|" +
                (expiryDate != null ? expiryDate.toString() : "") + "|" +
                (serialNo != null ? serialNo : "") + "|" +
                (movedAt != null ? movedAt.toString() : "") + "|" +
                movedBy;
    }

    /**
     * Tạo object từ dòng text đọc từ file
     * Format:
     * id|productId|qty|moveType|refTable|refId|batchNo|expiryDate|serialNo|movedAt|movedBy
     */
    public static StockMovement fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\|", -1); // -1 để giữ empty strings
        if (parts.length < 11) {
            return null;
        }

        try {
            StockMovement movement = new StockMovement();
            movement.setId(Integer.parseInt(parts[0]));
            movement.setProductId(Integer.parseInt(parts[1]));
            movement.setQty(Integer.parseInt(parts[2]));
            movement.setMoveType(parts[3].isEmpty() ? null : parts[3]);
            movement.setRefTable(parts[4].isEmpty() ? null : parts[4]);
            movement.setRefId(parts[5].isEmpty() ? null : Integer.parseInt(parts[5]));
            movement.setBatchNo(parts[6].isEmpty() ? null : parts[6]);
            movement.setExpiryDate(parts[7].isEmpty() ? null : LocalDate.parse(parts[7]));
            movement.setSerialNo(parts[8].isEmpty() ? null : parts[8]);
            movement.setMovedAt(parts[9].isEmpty() ? null : LocalDateTime.parse(parts[9]));
            movement.setMovedBy(Integer.parseInt(parts[10]));

            return movement;
        } catch (Exception e) {
            System.err.println("Error parsing StockMovement from line: " + line);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", productId=" + productId +
                ", qty=" + qty +
                ", moveType='" + moveType + '\'' +
                ", refTable='" + refTable + '\'' +
                ", refId=" + refId +
                ", batchNo='" + batchNo + '\'' +
                ", expiryDate=" + expiryDate +
                ", serialNo='" + serialNo + '\'' +
                ", movedAt=" + movedAt +
                ", movedBy=" + movedBy +
                '}';
    }
}
