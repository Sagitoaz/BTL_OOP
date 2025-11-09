package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ValidationUtils {

    /**
     * Validate Content-Type header
     * @return HttpResponse with 415 if invalid, null if valid
     */
    public static HttpResponse validateContentType(HttpRequest req, String expectedType) {
        Map<String, String> headers = req.headers;
        String contentTypes = headers.get("Content-Type");

        if (contentTypes == null || contentTypes.isEmpty()) {
            return error(415, "UNSUPPORTED_MEDIA_TYPE",
                    "Content-Type header is required");
        }

        String contentType = contentTypes.toLowerCase();
        if (!contentType.contains(expectedType.toLowerCase())) {
            return error(415, "UNSUPPORTED_MEDIA_TYPE",
                    "Expected Content-Type: " + expectedType + ", got: " + contentType);
        }

        return null; // Valid
    }

    /**
     * Validate JWT token (placeholder - implement với JWT library)
     * @return HttpResponse with 401 if invalid, null if valid
     */
    public static HttpResponse validateJWT(HttpRequest req) {
        // TODO: Implement JWT validation
        // For now, check Authorization header exists
        Map<String, String> headers = req.headers;
        String authHeader = headers.get("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return error(401, "UNAUTHORIZED",
                    "Authorization header is required");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return error(401, "UNAUTHORIZED",
                    "Invalid Authorization format. Expected: Bearer <token>");
        }

        // TODO: Validate token signature, expiration
        return null; // Valid for now
    }

    /**
     * Validate user role (placeholder)
     * @return HttpResponse with 403 if forbidden, null if allowed
     */
    public static HttpResponse validateRole(HttpRequest req, String requiredRole) {
        // TODO: Extract role from JWT token
        // For now, always allow (implement sau khi có JWT)
        return null;
    }

    /**
     * Validate required fields
     * @return HttpResponse with 400 if invalid, null if valid
     */
    public static HttpResponse validateRequiredFields(Map<String, Object> data, String... requiredFields) {
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null ||
                    data.get(field).toString().trim().isEmpty()) {
                return error(400, "BAD_REQUEST",
                        "Required field '" + field + "' is missing or empty");
            }
        }
        return null;
    }

    /**
     * Validate business rules for Product
     */
    public static HttpResponse validateProductBusinessRules(
            int qtyOnHand, Integer priceCost, Integer priceRetail) {

        if (qtyOnHand < 0) {
            return error(422, "VALIDATION_FAILED",
                    "Quantity on hand cannot be negative");
        }

        if (priceCost != null && priceCost < 0) {
            return error(422, "VALIDATION_FAILED",
                    "Price cost cannot be negative");
        }

        if (priceRetail != null && priceRetail < 0) {
            return error(422, "VALIDATION_FAILED",
                    "Price retail cannot be negative");
        }

        if (priceCost != null && priceRetail != null && priceRetail < priceCost) {
            return error(422, "VALIDATION_FAILED",
                    "Price retail should be greater than or equal to price cost");
        }

        return null; // Valid
    }

    /**
     * Validate search keyword
     */
    public static HttpResponse validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return error(400, "BAD_REQUEST",
                    "Search keyword is required");
        }

        if (keyword.trim().length() < 2) {
            return error(422, "VALIDATION_ERROR",
                    "Search keyword must be at least 2 characters");
        }

        // Check forbidden characters (SQL injection prevention)
        if (keyword.matches(".*[';\"\\\\].*")) {
            return error(422, "VALIDATION_ERROR",
                    "Search keyword contains forbidden characters");
        }

        return null; // Valid
    }

    /**
     * Helper: Create error response
     */
    public static HttpResponse error(int status, String errorCode, String message) {
        String json = String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
                errorCode, message);
        return HttpResponse.of(status, "application/json",
                json.getBytes(StandardCharsets.UTF_8));
    }
}