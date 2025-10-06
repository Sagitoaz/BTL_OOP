package org.example.oop.Model.Inventory;

/**
 * Enum định nghĩa trạng thái của Purchase Order
 */
public enum PurchaseOrderStatus {
    DRAFT("DRAFT", "Bản nháp"),
    PENDING("PENDING", "Chờ duyệt"),
    APPROVED("APPROVED", "Đã duyệt"),
    RECEIVED("RECEIVED", "Đã nhận hàng"),
    CANCELLED("CANCELLED", "Đã hủy");

    private final String code;
    private final String displayName;

    PurchaseOrderStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PurchaseOrderStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return DRAFT;
        }
        for (PurchaseOrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return DRAFT; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}
