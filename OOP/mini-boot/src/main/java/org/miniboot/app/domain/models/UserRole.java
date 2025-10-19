package org.miniboot.app.domain.models;

/**
 * Enum UserRole - tương thích với OOP UI
 */
public enum UserRole {
    ADMIN("Administrator"),
    EMPLOYEE("Employee"),
    CUSTOMER("Customer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
