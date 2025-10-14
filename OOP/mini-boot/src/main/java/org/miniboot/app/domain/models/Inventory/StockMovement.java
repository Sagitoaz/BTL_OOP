package org.miniboot.app.domain.models.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.miniboot.app.domain.models.Inventory.Enum.MovementType;

/**
 * Model quản lý các giao dịch xuất nhập kho
 * Khớp với database schema: Stock_Movements table
 */
public class StockMovement {
    private int id;
    private int productId;
    private int qty; // >0 nhập, <0 xuất
    private MovementType moveType;
    private String refTable; // Bảng tham chiếu: Payments, PurchaseOrders, InventoryTransfers...
    private Integer refId; // ID của chứng từ nguồn
    private String batchNo; // Số lô
    private LocalDate expiryDate; // Hạn sử dụng
    private String serialNo; // Số serial (cho thiết bị y tế)
    private LocalDateTime movedAt;
    private int movedBy; // ID người thực hiện (int, not String)
    private String note = null;

    // Constructors
    public StockMovement() {
    }

    public StockMovement(int id, int productId, int qty, String moveType,
            String refTable, Integer refId, String batchNo, LocalDate expiryDate,
            String serialNo, LocalDateTime movedAt, int movedBy) {
        this.id = id;
        this.productId = productId;
        this.qty = qty;
        this.moveType = MovementType.valueOf(moveType);
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
        return moveType != null ? moveType.name() : null;
    }

    public void setMoveType(String moveType) {
        this.moveType = MovementType.valueOf(moveType);
    }

    public void setMoveType(MovementType moveType) {
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
        if (moveType == null) {
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

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public String toDataString() {
        // rỗng -> ""
        String refIdStr = (refId == null) ? "" : String.valueOf(refId);
        String expiryStr = (expiryDate == null) ? "" : expiryDate.toString();

        // thay '|' trong note bằng ' ' để tránh vỡ cột
        String safeNote = note == null ? "" : note.replace("|", " ");

        return String.join("|",
                String.valueOf(id),
                String.valueOf(productId),
                String.valueOf(qty),
                moveType.name(),
                nullToEmpty(refTable),
                refIdStr,
                nullToEmpty(batchNo),
                expiryStr,
                nullToEmpty(serialNo),
                movedAt.toString(), // ISO-8601
                String.valueOf(movedBy),
                safeNote);
    }

    public static StockMovement fromDataString(String line) {
        String[] p = line.split("\\|", -1);
        // đảm bảo đủ 12 cột
        if (p.length < 12)
            throw new IllegalArgumentException("Invalid line: " + line);

        StockMovement m = new StockMovement();
        int i = 0;
        m.id = Integer.parseInt(p[i++]);
        m.productId = Integer.parseInt(p[i++]);
        m.qty = Integer.parseInt(p[i++]);
        m.moveType = MovementType.valueOf(p[i++]);

        m.refTable = emptyToNull(p[i++]);
        String refIdStr = p[i++];
        m.refId = refIdStr.isBlank() ? null : Integer.valueOf(refIdStr);

        m.batchNo = emptyToNull(p[i++]);

        String expiryStr = p[i++];
        m.expiryDate = expiryStr.isBlank() ? null : LocalDate.parse(expiryStr);

        m.serialNo = emptyToNull(p[i++]);

        m.movedAt = LocalDateTime.parse(p[i++]);
        m.movedBy = Integer.parseInt(p[i++]);

        m.note = emptyToNull(p[i++]);
        return m;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
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

    public Object getCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }
}
