package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.auth.JwtService;
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
        // Try both Content-Type and content-type (case-insensitive)
        String contentTypes = headers.get("Content-Type");
        if (contentTypes == null) {
            contentTypes = headers.get("content-type");
        }

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
     * Validate JWT token
     * Extracts Bearer token from Authorization header and verifies with JwtService
     */
    public static HttpResponse validateJWT(HttpRequest req) {
        Map<String, String> headers = req.headers;
        String authHeader = headers.get("Authorization");
        if (authHeader == null) {
            authHeader = headers.get("authorization");
        }

        // Check if Authorization header exists
        if (authHeader == null || authHeader.isEmpty()) {
            return error(401, "UNAUTHORIZED",
                    "Authorization header is required");
        }

        // Check if it's a Bearer token
        if (!authHeader.startsWith("Bearer ")) {
            return error(401, "UNAUTHORIZED",
                    "Authorization header must use Bearer scheme");
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        
        // Validate token with JwtService
        String userId = JwtService.validateTokenAndGetUserId(token);
        if (userId == null) {
            return error(401, "UNAUTHORIZED",
                    "Invalid or expired JWT token");
        }

        // Token is valid, store userId in request context for later use
        req.setAttribute("userId", userId);
        
        return null; // Valid
    }

    /**
     * Validate role/permission
     * For now, checks basic role requirements
     * Can be extended to check specific permissions from database
     */
    public static HttpResponse validateRole(HttpRequest req, String requiredRole) {
        // Get userId from request context (set by validateJWT)
        String userId = (String) req.getAttribute("userId");
        
        if (userId == null) {
            return error(403, "FORBIDDEN",
                    "User authentication required for this operation");
        }

        // TODO: Implement role checking based on your User/Role system
        // For now, we check if requiredRole is specified
        // Example: Check if user has "ADMIN" or "MANAGER" role in database
        
        // Placeholder: Allow all authenticated users
        // In production, query database: SELECT role FROM users WHERE id = userId
        // Then compare with requiredRole
        
        return null; // Valid for now (all authenticated users allowed)
    }

    /**
     * Validate standard request with Content-Type, JWT, and Role
     * Combines common validation chain
     */
    public static HttpResponse validateStandardRequest(HttpRequest req, String contentType, String requiredRole) {
        // Step 1: Content-Type
        HttpResponse contentTypeError = validateContentType(req, contentType);
        if (contentTypeError != null) return contentTypeError;

        // Step 2: JWT
        HttpResponse authError = validateJWT(req);
        if (authError != null) return authError;

        // Step 3: Role
        HttpResponse roleError = validateRole(req, requiredRole);
        if (roleError != null) return roleError;

        return null; // All validations passed
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
     * @param keyword Search keyword from user
     * @return HttpResponse with error if invalid, null if valid
     */
    public static HttpResponse validateSearchKeyword(String keyword) {
        // Check null or empty
        if (keyword == null || keyword.trim().isEmpty()) {
            return error(400, "BAD_REQUEST",
                    "Search keyword is required");
        }

        // Check minimum length
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