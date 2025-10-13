package org.example.oop.Data.models;

/**
 * ProductCategory - danh mục sản phẩm trong hệ thống.
 * Theo database: enum('frame','lens','contact_lens','machine','consumable','service')
 */
public enum ProductCategory {
    FRAME("frame"),
    LENS("lens"),
    CONTACT_LENS("contact_lens"),
    MACHINE("machine"),
    CONSUMABLE("consumable"),
    SERVICE("service");

    private final String value;

    ProductCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductCategory fromString(String text) {
        for (ProductCategory category : ProductCategory.values()) {
            if (category.value.equalsIgnoreCase(text)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No ProductCategory with value " + text + " found");
    }

    public static ProductCategory fromValue(String value) {
        return fromString(value);
    }
}
