package org.example.oop.Model.Inventory;

/**
 * Enum định nghĩa các loại giao dịch stock
 */
public enum MovementType {
    IN("IN", "Nhập kho"),
    OUT("OUT", "Xuất kho"),
    ADJUSTMENT("ADJUSTMENT", "Điều chỉnh"),
    RETURN("RETURN", "Trả hàng");

    private final String code;
    private final String displayName;

    MovementType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MovementType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return IN;
        }
        for (MovementType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return IN; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}
