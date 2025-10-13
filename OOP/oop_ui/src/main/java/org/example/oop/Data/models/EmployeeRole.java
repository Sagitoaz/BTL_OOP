package org.example.oop.Data.models;

/**
 * EmployeeRole - vai trò của nhân viên trong hệ thống.
 * Theo database: enum('doctor','nurse')
 */
public enum EmployeeRole {
    DOCTOR("doctor"),
    NURSE("nurse"); // nurse = y tá/thu ngân/quản lý

    private final String value;

    EmployeeRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EmployeeRole fromString(String text) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.value.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No EmployeeRole with value " + text + " found");
    }

    // Thêm method fromValue() để tương thích với code khác
    public static EmployeeRole fromValue(String value) {
        return fromString(value);
    }
}
