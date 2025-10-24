package org.miniboot.app.domain.models.Inventory.Enum;

/**
 * Enum định nghĩa các danh mục sản phẩm - khớp với DB schema
 * Database ENUM: 'frame','lens','contact_lens','machine','consumable','service'
 */
public enum Category {
    FRAME("frame", "Gọng kính"),
    LENS("lens", "Tròng kính"),
    CONTACT_LENS("contact_lens", "Kính áp tròng"),
    MACHINE("machine", "Máy móc"),
    CONSUMABLE("consumable", "Vật tư tiêu hao"),
    SERVICE("service", "Dịch vụ");

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
     * Lấy enum từ code string (khớp với database value)
     */
    public static Category fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return FRAME; // Default
        }
        for (Category category : values()) {
            if (category.code.equalsIgnoreCase(code.trim())) {
                return category;
            }
        }
        return FRAME; // Default fallback
    }

    @Override
    public String toString() {
        return displayName;
    }
}