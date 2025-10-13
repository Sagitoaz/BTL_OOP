package org.example.oop.Data.models;

/**
 * MoveType - loại giao dịch xuất nhập kho.
 * Theo database: enum('purchase','sale','return_in','return_out','adjustment','consume','transfer')
 */
public enum MoveType {
    PURCHASE("purchase"),
    SALE("sale"),
    RETURN_IN("return_in"),
    RETURN_OUT("return_out"),
    ADJUSTMENT("adjustment"),
    CONSUME("consume"),
    TRANSFER("transfer");

    private final String value;

    MoveType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MoveType fromString(String text) {
        for (MoveType type : MoveType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No MoveType with value " + text + " found");
    }
}
