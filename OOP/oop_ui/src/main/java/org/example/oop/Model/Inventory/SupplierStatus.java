package org.example.oop.Model.Inventory;

/**
 * Enum định nghĩa trạng thái của Supplier
 */
public enum SupplierStatus {
    ACTIVE("ACTIVE", "Đang hoạt động"),
    INACTIVE("INACTIVE", "Không hoạt động");

    private final String code;
    private final String displayName;

    SupplierStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SupplierStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ACTIVE;
        }
        for (SupplierStatus status : values()) {
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
