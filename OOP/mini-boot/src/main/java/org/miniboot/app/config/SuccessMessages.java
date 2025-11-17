package org.miniboot.app.config;
/**
 * SuccessMessages: Contains all success messages for the system
 */
public final class SuccessMessages {
    private SuccessMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    // ========== GENERAL SUCCESS ==========
    public static final String SUCCESS_GENERAL = "Operation successful";
    public static final String SUCCESS_CREATED = "Resource created successfully";
    public static final String SUCCESS_UPDATED = "Resource updated successfully";
    public static final String SUCCESS_DELETED = "Resource deleted successfully";
    public static final String SUCCESS_RETRIEVED = "Resource retrieved successfully";
    // ========== AUTHENTICATION SUCCESS ==========
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_LOGOUT = "Logout successful";
    public static final String SUCCESS_REGISTER = "Registration successful";
    public static final String SUCCESS_PASSWORD_CHANGED = "Password changed successfully";
    public static final String SUCCESS_PASSWORD_RESET = "Password reset successful";
    public static final String SUCCESS_TOKEN_REFRESHED = "Token refreshed successfully";
    // ========== DATABASE SUCCESS ==========
    public static final String SUCCESS_DB_CONNECTION = "Database connection established successfully";
    public static final String SUCCESS_DB_QUERY = "Query executed successfully";
    public static final String SUCCESS_DB_INSERT = "Record inserted successfully";
    public static final String SUCCESS_DB_UPDATE = "Record updated successfully";
    public static final String SUCCESS_DB_DELETE = "Record deleted successfully";
    // ========== CUSTOMER SUCCESS ==========
    public static final String SUCCESS_CUSTOMER_CREATED = "Customer created successfully";
    public static final String SUCCESS_CUSTOMER_UPDATED = "Customer updated successfully";
    public static final String SUCCESS_CUSTOMER_DELETED = "Customer deleted successfully";
    public static final String SUCCESS_CUSTOMER_RETRIEVED = "Customer information retrieved successfully";
    // ========== EMPLOYEE SUCCESS ==========
    public static final String SUCCESS_EMPLOYEE_CREATED = "Employee created successfully";
    public static final String SUCCESS_EMPLOYEE_UPDATED = "Employee updated successfully";
    public static final String SUCCESS_EMPLOYEE_DELETED = "Employee deleted successfully";
    // ========== DOCTOR SUCCESS ==========
    public static final String SUCCESS_DOCTOR_CREATED = "Doctor created successfully";
    public static final String SUCCESS_DOCTOR_UPDATED = "Doctor updated successfully";
    public static final String SUCCESS_DOCTOR_DELETED = "Doctor deleted successfully";
    // ========== APPOINTMENT SUCCESS ==========
    public static final String SUCCESS_APPOINTMENT_CREATED = "Appointment created successfully";
    public static final String SUCCESS_APPOINTMENT_UPDATED = "Appointment updated successfully";
    public static final String SUCCESS_APPOINTMENT_CANCELLED = "Appointment cancelled successfully";
    public static final String SUCCESS_APPOINTMENT_CONFIRMED = "Appointment confirmed successfully";
    public static final String SUCCESS_APPOINTMENT_COMPLETED = "Appointment completed successfully";
    // ========== PRESCRIPTION SUCCESS ==========
    public static final String SUCCESS_PRESCRIPTION_CREATED = "Prescription created successfully";
    public static final String SUCCESS_PRESCRIPTION_UPDATED = "Prescription updated successfully";
    public static final String SUCCESS_PRESCRIPTION_DELETED = "Prescription deleted successfully";
    // ========== PRODUCT SUCCESS ==========
    public static final String SUCCESS_PRODUCT_CREATED = "Product created successfully";
    public static final String SUCCESS_PRODUCT_UPDATED = "Product updated successfully";
    public static final String SUCCESS_PRODUCT_DELETED = "Product deleted successfully";
    // ========== INVENTORY SUCCESS ==========
    public static final String SUCCESS_INVENTORY_UPDATED = "Inventory updated successfully";
    public static final String SUCCESS_STOCK_ADDED = "Stock added successfully";
    public static final String SUCCESS_STOCK_REMOVED = "Stock removed successfully";
    // ========== PAYMENT SUCCESS ==========
    public static final String SUCCESS_PAYMENT_CREATED = "Payment created successfully";
    public static final String SUCCESS_PAYMENT_COMPLETED = "Payment completed successfully";
    public static final String SUCCESS_PAYMENT_CANCELLED = "Payment cancelled successfully";
    public static final String SUCCESS_PAYMENT_REFUNDED = "Payment refunded successfully";
    // ========== FILE SUCCESS ==========
    public static final String SUCCESS_FILE_UPLOADED = "File uploaded successfully";
    public static final String SUCCESS_FILE_DOWNLOADED = "File downloaded successfully";
    public static final String SUCCESS_FILE_DELETED = "File deleted successfully";
    // ========== EMAIL SUCCESS ==========
    public static final String SUCCESS_EMAIL_SENT = "Email sent successfully";
    public static final String SUCCESS_VERIFICATION_EMAIL_SENT = "Verification email sent successfully";
    public static final String SUCCESS_RESET_EMAIL_SENT = "Password reset email sent successfully";
}