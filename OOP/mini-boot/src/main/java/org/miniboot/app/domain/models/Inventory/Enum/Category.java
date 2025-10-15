package org.miniboot.app.domain.models.Inventory.Enum;

/**
 * Enum định nghĩa các danh mục sản phẩm
 */
public enum Category {
    MEDICATION("Medication", "Thuốc"),
    EQUIPMENT("Equipment", "Thiết bị"),
    SUPPLIES("Supplies", "Vật tư"),
    CONSUMABLES("Consumables", "Hàng tiêu hao");

    private final String code;
    private final String displayName;

    Category(String code, String displayName) {
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
    public static Category fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return MEDICATION;
        }
        for (Category category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        return MEDICATION; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}