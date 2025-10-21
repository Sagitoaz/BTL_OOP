package org.example.oop.Model.Inventory.Enum;

/**
 * Enum định nghĩa các danh mục sản phẩm (khớp với cột "category" trong bảng
 * products)
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
     * Lấy enum tương ứng từ mã code (dùng cho DB mapping)
     */
    public static Category fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return FRAME; // mặc định an toàn
        }
        for (Category c : values()) {
            if (c.code.equalsIgnoreCase(code)) {
                return c;
            }
        }
        return FRAME; // fallback nếu không khớp
    }

    @Override
    public String toString() {
        return displayName;
    }
}
