package org.example.oop.Utils;

/**
 * 🔧 API CONFIGURATION - NGÀY 8 FRONTEND INTEGRATION
 * Centralized configuration for API endpoints and settings
 */
public class ApiConfig {

     // Environment-specific URLs
     private static final String DEV_BASE_URL = "https://btl-oop-i9pi.onrender.com/";
     private static final String PROD_BASE_URL = "http://production-server:8080";

     // Current environment (change as needed)
     private static final Environment CURRENT_ENV = Environment.DEVELOPMENT;

     // Timeout settings (in seconds)
     public static final int CONNECTION_TIMEOUT = 10;
     public static final int REQUEST_TIMEOUT = 30;
     public static final int RETRY_ATTEMPTS = 3;

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
                    return "http://localhost:8081"; // Test server
               case DEVELOPMENT:
               default:
                    return DEV_BASE_URL;
          }
     }

     /**
      * Get full API URL with version
      */
     public static String getApiUrl() {
          return getBaseUrl();
     }

     // ================================
     // ENDPOINT BUILDERS
     // ================================

     /**
      * Build inventory endpoint
      */
     public static String inventoryEndpoint() {
          return "/api/inventory";
     }

     /**
      * Build inventory endpoint with ID
      */
     public static String inventoryEndpoint(long id) {
          return "/api/inventory/" + id;
     }

     /**
      * Build stock movements endpoint
      */
     public static String stockMovementsEndpoint() {
          return "/api/stock-movements";
     }

     /**
      * Build stock movements endpoint with ID
      */
     public static String stockMovementsEndpoint(long id) {
          return "/api/stock-movements/" + id;
     }

     /**
      * Build alerts endpoint
      */
     public static String alertsEndpoint() {
          return "/api/alerts";
     }

     /**
      * Build alerts endpoint with ID
      */
     public static String alertsEndpoint(long id) {
          return "/api/alerts/" + id;
     }

     /**
      * Build health check endpoint
      */
     public static String healthEndpoint() {
          return "/health";
     }

     // ================================
     // QUERY PARAMETER BUILDERS
     // ================================

     /**
      * Build pagination query string
      */
     public static String paginationParams(int page, int size) {
          return "?page=" + page + "&size=" + size;
     }

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

     // ================================
     // ENVIRONMENT CONTROL
     // ================================

     /**
      * Check if running in development mode
      */
     public static boolean isDevelopment() {
          return CURRENT_ENV == Environment.DEVELOPMENT;
     }

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
}