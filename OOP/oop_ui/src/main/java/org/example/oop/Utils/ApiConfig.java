package org.example.oop.Utils;

import org.example.oop.config.ApiConstants;

/**
 * 🔧 API CONFIGURATION - NGÀY 8 FRONTEND INTEGRATION
 * Centralized configuration for API endpoints and settings
 */
public class ApiConfig {

     // Environment-specific URLs
     private static final String DEV_BASE_URL = "https://btl-oop-i9pi.onrender.com";
     private static final String PROD_BASE_URL = "http://production-server:8080";

     // Current environment (change as needed)
     private static final Environment CURRENT_ENV = Environment.DEVELOPMENT;

     // Timeout settings - sử dụng từ ApiConstants
     public static final int CONNECTION_TIMEOUT = ApiConstants.CONNECTION_TIMEOUT_SECONDS;
     public static final int REQUEST_TIMEOUT = ApiConstants.REQUEST_TIMEOUT_SECONDS;
     public static final int RETRY_ATTEMPTS = ApiConstants.RETRY_ATTEMPTS;

     // Pagination defaults
     public static final int DEFAULT_PAGE_SIZE = 10;
     public static final int MAX_PAGE_SIZE = 100;

     // API version
     public static final String API_VERSION = "v1";

     public enum Environment {
          DEVELOPMENT, PRODUCTION, TESTING
     }

     // ================================
     // URL METHODS
     // ================================

     /**
      * Get base URL based on current environment
      */
     public static String getBaseUrl() {
          switch (CURRENT_ENV) {
               case PRODUCTION:
                    return PROD_BASE_URL;
               case TESTING:
                    return "http://localhost:8080"; // Test server
               case DEVELOPMENT:
               default:
                    return DEV_BASE_URL;
          }
     }

     // ================================
     // API ENDPOINTS
     // ================================

     // Authentication
     public static final String AUTH_LOGIN = "/auth/login";
     public static final String AUTH_PROFILE = "/auth/profile";
     public static final String AUTH_LOGOUT = "/auth/logout";

     // Customers
     public static final String CUSTOMERS = "/customers";
     public static final String CUSTOMER_RECORDS = "/customer-records";
     public static final String CUSTOMER_SEARCH = "/customers/search";

     // Employees
     public static final String EMPLOYEES = "/employees";

     // Doctors
     public static final String DOCTORS = "/doctors";

     // Appointments
     public static final String APPOINTMENTS = "/appointments";

     // Prescriptions
     public static final String PRESCRIPTIONS = "/prescriptions";

     // Products & Inventory
     public static final String PRODUCTS = "/products";
     public static final String INVENTORY = "/inventory";
     public static final String STOCK_MOVEMENTS = "/stock-movements";

     // Payments
     public static final String PAYMENTS = "/payments";
     public static final String PAYMENT_ITEMS = "/payment-items";
     public static final String PAYMENT_STATUS_LOG = "/payment-status-log";

     // Health check
     public static final String HEALTH = "/health";

     /**
      * Build full URL for an endpoint
      */
     public static String buildUrl(String endpoint) {
          return getBaseUrl() + endpoint;
     }

     /**
      * Build URL with query parameters
      */
     public static String buildUrl(String endpoint, java.util.Map<String, String> params) {
          StringBuilder url = new StringBuilder(buildUrl(endpoint));
          if (params != null && !params.isEmpty()) {
               url.append("?");
               params.forEach((key, value) -> url.append(key).append("=").append(value).append("&"));
               url.setLength(url.length() - 1); // Remove last &
          }
          return url.toString();
     }

     /**
      * Get health check endpoint
      */
     public static String healthEndpoint() {
          return buildUrl(HEALTH);
     }
}