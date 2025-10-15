package org.example.oop.Model.Inventory.Enum;

/**
 * Enum định nghĩa các trạng thái của sản phẩm trong kho
 */
public enum InventoryStatus {
    ACTIVE("Active", "Đang hoạt động"),
    DISCONTINUED("Discontinued", "Ngừng kinh doanh"),
    OUT_OF_STOCK("Out of Stock", "Hết hàng"),
    LOW_STOCK("Low Stock", "Sắp hết hàng");

    private final String code;
    private final String displayName;

    InventoryStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lấy enum từ code string
     */
    public static InventoryStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ACTIVE;
        }
        for (InventoryStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return ACTIVE; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}