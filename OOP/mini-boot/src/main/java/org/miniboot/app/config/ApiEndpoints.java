package org.miniboot.app.config;

/**
 * ApiEndpoints: Chứa tất cả các API endpoints của hệ thống
 */
public final class ApiEndpoints {

    // Private constructor để ngăn khởi tạo
    private ApiEndpoints() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== BASE PATHS ==========
    public static final String BASE_API = "/api";
    public static final String BASE_AUTH = "/auth";
    public static final String BASE_USERS = "/users";
    public static final String BASE_CUSTOMERS = "/customers";
    public static final String BASE_EMPLOYEES = "/employees";
    public static final String BASE_DOCTORS = "/doctors";
    public static final String BASE_APPOINTMENTS = "/appointments";
    public static final String BASE_PRESCRIPTIONS = "/prescriptions";
    public static final String BASE_PRODUCTS = "/products";
    public static final String BASE_INVENTORY = "/inventory";
    public static final String BASE_STOCK_MOVEMENTS = "/stock-movements";
    public static final String BASE_PAYMENTS = "/payments";
    public static final String BASE_PAYMENT_ITEMS = "/payment-items";
    public static final String BASE_PAYMENT_STATUS_LOG = "/payment-status-log";

    // ========== AUTHENTICATION ENDPOINTS ==========
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_LOGOUT = "/auth/logout";
    public static final String AUTH_REGISTER = "/auth/register";
    public static final String AUTH_PROFILE = "/auth/profile";
    public static final String AUTH_CHANGE_PASSWORD = "/auth/change-password";
    public static final String AUTH_RESET_PASSWORD = "/auth/reset-password";
    public static final String AUTH_VERIFY = "/auth/verify";
    public static final String AUTH_REFRESH = "/auth/refresh";

    // ========== USER ENDPOINTS ==========
    public static final String USERS_LIST = "/users";
    public static final String USERS_GET = "/users/{id}";
    public static final String USERS_CREATE = "/users";
    public static final String USERS_UPDATE = "/users/{id}";
    public static final String USERS_DELETE = "/users/{id}";
    public static final String USERS_SEARCH = "/users/search";

    // ========== CUSTOMER ENDPOINTS ==========
    public static final String CUSTOMERS_LIST = "/customers";
    public static final String CUSTOMERS_GET = "/customers/{id}";
    public static final String CUSTOMERS_CREATE = "/customers";
    public static final String CUSTOMERS_UPDATE = "/customers/{id}";
    public static final String CUSTOMERS_DELETE = "/customers/{id}";
    public static final String CUSTOMERS_SEARCH = "/customers/search";
    public static final String CUSTOMERS_RECORDS = "/customer-records";

    // ========== EMPLOYEE ENDPOINTS ==========
    public static final String EMPLOYEES_LIST = "/employees";
    public static final String EMPLOYEES_GET = "/employees/{id}";
    public static final String EMPLOYEES_CREATE = "/employees";
    public static final String EMPLOYEES_UPDATE = "/employees/{id}";
    public static final String EMPLOYEES_DELETE = "/employees/{id}";

    // ========== DOCTOR ENDPOINTS ==========
    public static final String DOCTORS_LIST = "/doctors";
    public static final String DOCTORS_GET = "/doctors/{id}";
    public static final String DOCTORS_CREATE = "/doctors";
    public static final String DOCTORS_UPDATE = "/doctors/{id}";
    public static final String DOCTORS_DELETE = "/doctors/{id}";
    public static final String DOCTORS_SCHEDULE = "/doctors/{id}/schedule";

    // ========== APPOINTMENT ENDPOINTS ==========
    public static final String APPOINTMENTS_LIST = "/appointments";
    public static final String APPOINTMENTS_GET = "/appointments/{id}";
    public static final String APPOINTMENTS_CREATE = "/appointments";
    public static final String APPOINTMENTS_UPDATE = "/appointments/{id}";
    public static final String APPOINTMENTS_DELETE = "/appointments/{id}";
    public static final String APPOINTMENTS_CANCEL = "/appointments/{id}/cancel";
    public static final String APPOINTMENTS_CONFIRM = "/appointments/{id}/confirm";
    public static final String APPOINTMENTS_COMPLETE = "/appointments/{id}/complete";

    // ========== PRESCRIPTION ENDPOINTS ==========
    public static final String PRESCRIPTIONS_LIST = "/prescriptions";
    public static final String PRESCRIPTIONS_GET = "/prescriptions/{id}";
    public static final String PRESCRIPTIONS_CREATE = "/prescriptions";
    public static final String PRESCRIPTIONS_UPDATE = "/prescriptions/{id}";
    public static final String PRESCRIPTIONS_DELETE = "/prescriptions/{id}";
    public static final String PRESCRIPTIONS_BY_CUSTOMER = "/prescriptions/customer/{customerId}";

    // ========== PRODUCT ENDPOINTS ==========
    public static final String PRODUCTS_LIST = "/products";
    public static final String PRODUCTS_GET = "/products/{id}";
    public static final String PRODUCTS_CREATE = "/products";
    public static final String PRODUCTS_UPDATE = "/products/{id}";
    public static final String PRODUCTS_DELETE = "/products/{id}";
    public static final String PRODUCTS_SEARCH = "/products/search";
    public static final String PRODUCTS_BY_SKU = "/products/sku/{sku}";

    // ========== INVENTORY ENDPOINTS ==========
    public static final String INVENTORY_LIST = "/inventory";
    public static final String INVENTORY_GET = "/inventory/{id}";
    public static final String INVENTORY_UPDATE = "/inventory/{id}";
    public static final String INVENTORY_SEARCH = "/inventory/search";
    public static final String INVENTORY_LOW_STOCK = "/inventory/low-stock";

    // ========== STOCK MOVEMENT ENDPOINTS ==========
    public static final String STOCK_MOVEMENTS_LIST = "/stock-movements";
    public static final String STOCK_MOVEMENTS_GET = "/stock-movements/{id}";
    public static final String STOCK_MOVEMENTS_CREATE = "/stock-movements";
    public static final String STOCK_MOVEMENTS_BY_PRODUCT = "/stock-movements/product/{productId}";

    // ========== PAYMENT ENDPOINTS ==========
    public static final String PAYMENTS_LIST = "/payments";
    public static final String PAYMENTS_GET = "/payments/{id}";
    public static final String PAYMENTS_CREATE = "/payments";
    public static final String PAYMENTS_UPDATE = "/payments/{id}";
    public static final String PAYMENTS_DELETE = "/payments/{id}";
    public static final String PAYMENTS_BY_CUSTOMER = "/payments/customer/{customerId}";
    public static final String PAYMENTS_HISTORY = "/payments/history";

    // ========== PAYMENT ITEM ENDPOINTS ==========
    public static final String PAYMENT_ITEMS_LIST = "/payment-items";
    public static final String PAYMENT_ITEMS_GET = "/payment-items/{id}";
    public static final String PAYMENT_ITEMS_CREATE = "/payment-items";
    public static final String PAYMENT_ITEMS_UPDATE = "/payment-items/{id}";
    public static final String PAYMENT_ITEMS_DELETE = "/payment-items/{id}";
    public static final String PAYMENT_ITEMS_BY_PAYMENT = "/payment-items/payment/{paymentId}";

    // ========== PAYMENT STATUS LOG ENDPOINTS ==========
    public static final String PAYMENT_STATUS_LOG_LIST = "/payment-status-log";
    public static final String PAYMENT_STATUS_LOG_GET = "/payment-status-log/{id}";
    public static final String PAYMENT_STATUS_LOG_CREATE = "/payment-status-log";
    public static final String PAYMENT_STATUS_LOG_BY_PAYMENT = "/payment-status-log/payment/{paymentId}";

    // ========== HEALTH CHECK ENDPOINTS ==========
    public static final String HEALTH = "/health";
    public static final String PING = "/ping";
    public static final String STATUS = "/status";

    // ========== UTILITY ENDPOINTS ==========
    public static final String ECHO = "/echo";
    public static final String VERSION = "/version";
    public static final String INFO = "/info";
}

