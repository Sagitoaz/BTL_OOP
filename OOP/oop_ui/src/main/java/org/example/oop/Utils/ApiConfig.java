package org.example.oop.Utils;

/**
 * 🔧 API CONFIGURATION - CENTRALIZED ENDPOINT MANAGEMENT
 * <p>
 * Centralized configuration for all API endpoints and settings.
 * Updated with complete endpoint list from backend ServerMain.java
 *
 * @author Person 4 - Error Handling & Service Layer Developer
 * @version 2.0
 * @since Day 7 - Complete endpoint registration
 */
public class ApiConfig {

    // Timeout settings (in seconds)
    public static final int CONNECTION_TIMEOUT = 10;
    public static final int REQUEST_TIMEOUT = 30;
    public static final int RETRY_ATTEMPTS = 3;
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    // API version
    public static final String API_VERSION = "v1";
    // Environment-specific URLs
    private static final String DEV_BASE_URL = "https://btl-oop-i9pi.onrender.com/";
    private static final String LOCAL_BASE_URL = "http://localhost:8080/";
    private static final String PROD_BASE_URL = "http://production-server:8080/";
    // Current environment (change as needed)
    private static final Environment CURRENT_ENV = Environment.LOCAL;

    /**
     * Get base URL based on current environment
     */
    public static String getBaseUrl() {
        switch (CURRENT_ENV) {
            case PRODUCTION:
                return PROD_BASE_URL;
            case TESTING:
                return "http://localhost:8081/"; // Test server
            case LOCAL:
                return LOCAL_BASE_URL; // Local development
            case DEVELOPMENT:
            default:
                return DEV_BASE_URL;
        }
    }

    // ================================
    // URL METHODS
    // ================================

    /**
     * Get full API URL with version
     */
    public static String getApiUrl() {
        return getBaseUrl();
    }

    // 🔐 AUTHENTICATION ENDPOINTS (2)
    public static String authLoginEndpoint() {
        return "/auth/login";
    }

    // ================================
    // ENDPOINT BUILDERS - COMPLETE LIST (43 endpoints)
    // ================================

    public static String authProfileEndpoint() {
        return "/auth/profile";
    }

    // 👥 USER ENDPOINTS (5)
    public static String usersEndpoint() {
        return "/users";
    }

    public static String usersEndpoint(int id) {
        return "/users/" + id;
    }

    // 📅 APPOINTMENT ENDPOINTS (4)
    public static String appointmentsEndpoint() {
        return "/appointments";
    }

    // 👨‍⚕️ DOCTOR ENDPOINTS (3)
    public static String doctorsEndpoint() {
        return "/doctors";
    }

    public static String doctorAvailableSlotsEndpoint() {
        return "/doctors/available-slots";
    }

    // 📦 PRODUCT ENDPOINTS (5)
    public static String productsEndpoint() {
        return "/products";
    }

    public static String productsSearchEndpoint() {
        return "/products/search";
    }

    public static String productsEndpoint(int id) {
        return "/products/" + id;
    }

    // 📊 STOCK MOVEMENT ENDPOINTS (6)
    public static String stockMovementsEndpoint() {
        return "/stock_movements";
    }

    public static String stockMovementsFilterEndpoint() {
        return "/stock_movements/filter";
    }

    public static String stockMovementsStatsEndpoint() {
        return "/stock_movements/stats";
    }

    public static String stockMovementsEndpoint(int id) {
        return "/stock_movements/" + id;
    }

    // 💳 PAYMENT ENDPOINTS (4)
    public static String paymentsEndpoint() {
        return "/payments";
    }

    public static String paymentsWithStatusEndpoint() {
        return "/payments/with-status";
    }

    public static String paymentsEndpoint(int id) {
        return "/payments/" + id;
    }

    // 📝 PAYMENT STATUS ENDPOINTS (2)
    public static String paymentStatusEndpoint() {
        return "/payment-status";
    }

    // 🧾 PAYMENT ITEM ENDPOINTS (5)
    public static String paymentItemsEndpoint() {
        return "/payment-items";
    }

    public static String paymentItemsReplaceEndpoint() {
        return "/payment-items/replace";
    }

    public static String paymentItemsEndpoint(int id) {
        return "/payment-items/" + id;
    }

    // 👤 CUSTOMER ENDPOINTS (4)
    public static String customersEndpoint() {
        return "/customers";
    }

    public static String customersEndpoint(int id) {
        return "/customers/" + id;
    }

    // 💊 PRESCRIPTION ENDPOINTS (3)
    public static String prescriptionsEndpoint() {
        return "/prescriptions";
    }

    public static String prescriptionsEndpoint(int id) {
        return "/prescriptions/" + id;
    }

    // 🏥 HEALTH CHECK
    public static String healthEndpoint() {
        return "/health";
    }

    /**
     * @deprecated Use productsEndpoint() instead
     */
    @Deprecated
    public static String inventoryEndpoint() {
        return "/products";
    }

    // ⚠️ DEPRECATED - For backward compatibility

    /**
     * @deprecated Use productsEndpoint(id) instead
     */
    @Deprecated
    public static String inventoryEndpoint(long id) {
        return "/products/" + id;
    }

    /**
     * @deprecated Not implemented in backend
     */
    @Deprecated
    public static String alertsEndpoint() {
        return "/api/alerts";
    }

    /**
     * @deprecated Not implemented in backend
     */
    @Deprecated
    public static String alertsEndpoint(long id) {
        return "/api/alerts/" + id;
    }

    /**
     * Build pagination query string
     */
    public static String paginationParams(int page, int size) {
        return "?page=" + page + "&size=" + size;
    }

    // ================================
    // QUERY PARAMETER BUILDERS
    // ================================

    /**
     * Build search query string
     */
    public static String searchParams(String query) {
        return "?search=" + query;
    }

    /**
     * Build filter query string
     */
    public static String filterParams(String category, Integer minStock) {
        StringBuilder params = new StringBuilder("?");
        boolean hasParam = false;

        if (category != null && !category.isEmpty()) {
            params.append("category=").append(category);
            hasParam = true;
        }

        if (minStock != null) {
            if (hasParam)
                params.append("&");
            params.append("minStock=").append(minStock);
            hasParam = true;
        }

        return hasParam ? params.toString() : "";
    }

    /**
     * Check if running in development mode
     */
    public static boolean isDevelopment() {
        return CURRENT_ENV == Environment.DEVELOPMENT;
    }

    // ================================
    // ENVIRONMENT CONTROL
    // ================================

    /**
     * Check if running in production mode
     */
    public static boolean isProduction() {
        return CURRENT_ENV == Environment.PRODUCTION;
    }

    /**
     * Get current environment
     */
    public static Environment getCurrentEnvironment() {
        return CURRENT_ENV;
    }

    /**
     * Get environment info for debugging
     */
    public static String getEnvironmentInfo() {
        return String.format("Environment: %s, Base URL: %s, API Version: %s",
                CURRENT_ENV, getBaseUrl(), API_VERSION);
    }

    /**
     * Print all available endpoints (for debugging)
     */
    public static void printAllEndpoints() {
        System.out.println("\n📋 ════════════════════════════════════════════════════════");
        System.out.println("   FRONTEND API CONFIGURATION - ALL ENDPOINTS");
        System.out.println("   Base URL: " + getBaseUrl());
        System.out.println("   ════════════════════════════════════════════════════════");

        System.out.println("\n   🔐 AUTHENTICATION (2 endpoints)");
        System.out.println("      " + authLoginEndpoint());
        System.out.println("      " + authProfileEndpoint());

        System.out.println("\n   👥 USERS (5 endpoints)");
        System.out.println("      " + usersEndpoint());
        System.out.println("      " + usersEndpoint(1) + " (example)");

        System.out.println("\n   📅 APPOINTMENTS (4 endpoints)");
        System.out.println("      " + appointmentsEndpoint());

        System.out.println("\n   👨‍⚕️ DOCTORS (3 endpoints)");
        System.out.println("      " + doctorsEndpoint());
        System.out.println("      " + doctorAvailableSlotsEndpoint());

        System.out.println("\n   📦 PRODUCTS (5 endpoints)");
        System.out.println("      " + productsEndpoint());
        System.out.println("      " + productsSearchEndpoint());

        System.out.println("\n   📊 STOCK MOVEMENTS (6 endpoints)");
        System.out.println("      " + stockMovementsEndpoint());
        System.out.println("      " + stockMovementsFilterEndpoint());
        System.out.println("      " + stockMovementsStatsEndpoint());

        System.out.println("\n   💳 PAYMENTS (4 endpoints)");
        System.out.println("      " + paymentsEndpoint());
        System.out.println("      " + paymentsWithStatusEndpoint());

        System.out.println("\n   📝 PAYMENT STATUS (2 endpoints)");
        System.out.println("      " + paymentStatusEndpoint());

        System.out.println("\n   🧾 PAYMENT ITEMS (5 endpoints)");
        System.out.println("      " + paymentItemsEndpoint());
        System.out.println("      " + paymentItemsReplaceEndpoint());

        System.out.println("\n   👤 CUSTOMERS (4 endpoints)");
        System.out.println("      " + customersEndpoint());

        System.out.println("\n   💊 PRESCRIPTIONS (3 endpoints)");
        System.out.println("      " + prescriptionsEndpoint());

        System.out.println("\n   🏥 HEALTH CHECK");
        System.out.println("      " + healthEndpoint());

        System.out.println("\n   ════════════════════════════════════════════════════════");
        System.out.println("   Total: 43 endpoints registered");
        System.out.println("   ════════════════════════════════════════════════════════\n");
    }

    // ================================
    // ENDPOINT DOCUMENTATION
    // ================================

    public enum Environment {
        DEVELOPMENT, PRODUCTION, TESTING, LOCAL
    }
}