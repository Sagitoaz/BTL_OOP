package org.example.oop.Data.models;

/**
 * Enum UserRole - xác định các vai trò người dùng trong hệ thống.
 * Theo database mới: có 3 bảng riêng biệt (Admins, Employees, Customers)
 * Role này dùng để phân quyền và quản lý session
 */
public enum UserRole {
    ADMIN("Administrator"),
    EMPLOYEE("Employee"), // bao gồm doctor và nurse
    CUSTOMER("Customer"); // thay thế cho PATIENT

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy tên hiển thị thân thiện của vai trò (dùng cho UI).
     */
    public String getDisplayName() {
        return displayName;
    }
}