package org.miniboot.app.config;

/**
 * ErrorMessages: Chứa tất cả các thông báo lỗi của hệ thống
 */
public final class ErrorMessages {

    // Private constructor để ngăn khởi tạo
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // GENERAL ERRORS 
    public static final String ERROR_GENERAL = "An error occurred";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error";
    public static final String ERROR_SERVICE_UNAVAILABLE = "Service temporarily unavailable";
    public static final String ERROR_INVALID_REQUEST = "Invalid request";
    public static final String ERROR_MISSING_PARAMETER = "Missing required parameter: %s";
    public static final String ERROR_INVALID_PARAMETER = "Invalid parameter: %s";
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_ALREADY_EXISTS = "Resource already exists";
    public static final String ERROR_OPERATION_FAILED = "Operation failed";

    // AUTHENTICATION ERRORS 
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
    public static final String ERROR_FORBIDDEN = "Access forbidden";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_TOKEN_EXPIRED = "Token has expired";
    public static final String ERROR_TOKEN_INVALID = "Invalid token";
    public static final String ERROR_TOKEN_MISSING = "Authorization token is missing";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_USER_ALREADY_EXISTS = "User already exists";
    public static final String ERROR_WEAK_PASSWORD = "Password is too weak";
    public static final String ERROR_MISSING_USERNAME = "Username is required";
    public static final String ERROR_MISSING_PASSWORD = "Password is required";
    public static final String ERROR_PASSWORD_MISMATCH = "Passwords do not match";
    public static final String ERROR_OLD_PASSWORD_INCORRECT = "Old password is incorrect";

    // DATABASE ERRORS 
    public static final String ERROR_DB_CONNECTION = "Failed to connect to database";
    public static final String ERROR_DB_QUERY = "Failed to execute query";
    public static final String ERROR_DB_DRIVER = "Database driver not found";
    public static final String ERROR_DB_DUPLICATE_KEY = "Duplicate key violation";
    public static final String ERROR_DB_FOREIGN_KEY = "Foreign key constraint violation";
    public static final String ERROR_DB_TIMEOUT = "Database operation timeout";
    public static final String ERROR_DB_TRANSACTION = "Transaction failed";

    // VALIDATION ERRORS 
    public static final String ERROR_VALIDATION = "Validation error";
    public static final String ERROR_INVALID_EMAIL = "Invalid email address";
    public static final String ERROR_INVALID_PHONE = "Invalid phone number";
    public static final String ERROR_INVALID_DATE = "Invalid date format";
    public static final String ERROR_INVALID_TIME = "Invalid time format";
    public static final String ERROR_INVALID_NUMBER = "Invalid number format";
    public static final String ERROR_VALUE_TOO_LONG = "Value exceeds maximum length: %s";
    public static final String ERROR_VALUE_TOO_SHORT = "Value is below minimum length: %s";
    public static final String ERROR_VALUE_OUT_OF_RANGE = "Value is out of range: %s";
    public static final String ERROR_REQUIRED_FIELD = "Field is required: %s";

    // CUSTOMER ERRORS 
    public static final String ERROR_CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String ERROR_CUSTOMER_ALREADY_EXISTS = "Customer already exists";
    public static final String ERROR_INVALID_CUSTOMER_ID = "Invalid customer ID";

    // EMPLOYEE ERRORS 
    public static final String ERROR_EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String ERROR_EMPLOYEE_ALREADY_EXISTS = "Employee already exists";
    public static final String ERROR_INVALID_EMPLOYEE_ID = "Invalid employee ID";

    // DOCTOR ERRORS 
    public static final String ERROR_DOCTOR_NOT_FOUND = "Doctor not found";
    public static final String ERROR_DOCTOR_NOT_AVAILABLE = "Doctor is not available";
    public static final String ERROR_INVALID_DOCTOR_ID = "Invalid doctor ID";

    // APPOINTMENT ERRORS 
    public static final String ERROR_APPOINTMENT_NOT_FOUND = "Appointment not found";
    public static final String ERROR_APPOINTMENT_CONFLICT = "Appointment time conflict";
    public static final String ERROR_INVALID_APPOINTMENT_TIME = "Invalid appointment time";
    public static final String ERROR_APPOINTMENT_PAST_TIME = "Cannot create appointment in the past";
    public static final String ERROR_APPOINTMENT_ALREADY_CANCELLED = "Appointment is already cancelled";
    public static final String ERROR_APPOINTMENT_CANNOT_CANCEL = "Cannot cancel appointment";

    // PRESCRIPTION ERRORS 
    public static final String ERROR_PRESCRIPTION_NOT_FOUND = "Prescription not found";
    public static final String ERROR_INVALID_PRESCRIPTION_DATA = "Invalid prescription data";
    public static final String ERROR_PRESCRIPTION_EXPIRED = "Prescription has expired";

    // PRODUCT ERRORS 
    public static final String ERROR_PRODUCT_NOT_FOUND = "Product not found";
    public static final String ERROR_PRODUCT_ALREADY_EXISTS = "Product already exists";
    public static final String ERROR_INVALID_PRODUCT_ID = "Invalid product ID";
    public static final String ERROR_INVALID_SKU = "Invalid SKU";
    public static final String ERROR_PRODUCT_OUT_OF_STOCK = "Product is out of stock";
    public static final String ERROR_INSUFFICIENT_STOCK = "Insufficient stock";

    // INVENTORY ERRORS 
    public static final String ERROR_INVENTORY_NOT_FOUND = "Inventory record not found";
    public static final String ERROR_INVALID_QUANTITY = "Invalid quantity";
    public static final String ERROR_NEGATIVE_QUANTITY = "Quantity cannot be negative";

    // PAYMENT ERRORS 
    public static final String ERROR_PAYMENT_NOT_FOUND = "Payment not found";
    public static final String ERROR_PAYMENT_ALREADY_COMPLETED = "Payment is already completed";
    public static final String ERROR_PAYMENT_FAILED = "Payment processing failed";
    public static final String ERROR_INVALID_PAYMENT_AMOUNT = "Invalid payment amount";
    public static final String ERROR_INVALID_PAYMENT_METHOD = "Invalid payment method";

    // FILE ERRORS 
    public static final String ERROR_FILE_NOT_FOUND = "File not found";
    public static final String ERROR_FILE_READ = "Failed to read file";
    public static final String ERROR_FILE_WRITE = "Failed to write file";
    public static final String ERROR_FILE_DELETE = "Failed to delete file";
    public static final String ERROR_INVALID_FILE_FORMAT = "Invalid file format";

    // JSON ERRORS 
    public static final String ERROR_JSON_PARSE = "Failed to parse JSON";
    public static final String ERROR_JSON_INVALID = "Invalid JSON format";
    public static final String ERROR_JSON_MISSING_FIELD = "Missing required JSON field: %s";

    // HTTP ERRORS 
    public static final String ERROR_METHOD_NOT_ALLOWED = "HTTP method not allowed";
    public static final String ERROR_PAYLOAD_TOO_LARGE = "Request payload too large";
    public static final String ERROR_UNSUPPORTED_MEDIA_TYPE = "Unsupported media type";
    public static final String ERROR_TOO_MANY_REQUESTS = "Too many requests";
}

