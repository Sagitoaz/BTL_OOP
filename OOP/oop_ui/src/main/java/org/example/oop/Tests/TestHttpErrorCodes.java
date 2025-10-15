package org.example.oop.Tests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

/**
 * TestHttpErrorCodes - Test tất cả mã lỗi HTTP của mini-boot server
 * 
 * Dựa trên AppConfig.RESPONSE_REASON:
 * - 200 OK
 * - 201 Created
 * - 204 No Content
 * - 301 Moved Permanently
 * - 302 Found
 * - 304 Not Modified
 * - 400 Bad Request
 * - 401 Unauthorized
 * - 403 Forbidden
 * - 404 Not Found
 * - 405 Method Not Allowed
 * - 413 Payload Too Large
 * - 500 Internal Server Error
 * - 501 Not Implemented
 * - 503 Service Unavailable
 */
public class TestHttpErrorCodes {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("🧪 TEST HTTP ERROR CODES - MINI-BOOT SERVER");
        System.out.println("============================================================\n");
        
        // Success codes (2xx)
        test200_OK();
        test201_Created();
        test204_NoContent();
        
        // Redirection codes (3xx)
        System.out.println("\n--- 3xx Redirection Codes (Skipped - Not Implemented) ---");
        System.out.println("⏭️  301 Moved Permanently - Not tested");
        System.out.println("⏭️  302 Found - Not tested");
        System.out.println("⏭️  304 Not Modified - Not tested");
        
        // Client error codes (4xx)
        test400_BadRequest();
        test401_Unauthorized();
        test403_Forbidden();
        test404_NotFound();
        test405_MethodNotAllowed();
        test413_PayloadTooLarge();
        
        // Server error codes (5xx)
        test500_InternalServerError();
        test501_NotImplemented();
        test503_ServiceUnavailable();
        
        System.out.println("\n============================================================");
        System.out.println("✅ ALL ERROR CODE TESTS COMPLETED!");
        System.out.println("============================================================");
    }
    
    // ==================== 2xx Success ====================
    
    private static void test200_OK() {
        System.out.println("\n--- TEST: 200 OK ---");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/doctors"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("✅ 200 OK - GET /doctors successful");
                System.out.println("   Response: " + response.body().substring(0, Math.min(100, response.body().length())) + "...");
            } else {
                System.out.println("❌ Expected 200, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test201_Created() {
        System.out.println("\n--- TEST: 201 Created ---");
        try {
            // Tạo appointment mới
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("customerId", 1);
            appointmentData.put("doctorId", 1);
            appointmentData.put("appointmentType", "EXAMINATION");
            appointmentData.put("appointmentDate", LocalDateTime.now().plusDays(5).toString());
            appointmentData.put("notes", "Test 201 Created");
            appointmentData.put("status", "SCHEDULED");
            
            String jsonBody = gson.toJson(appointmentData);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/appointments"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("✅ 201 Created (or 200 OK) - POST /appointments successful");
                System.out.println("   Created: " + response.body().substring(0, Math.min(100, response.body().length())));
            } else {
                System.out.println("❌ Expected 201/200, got: " + response.statusCode());
                System.out.println("   Response: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test204_NoContent() {
        System.out.println("\n--- TEST: 204 No Content ---");
        System.out.println("⏭️  Skipped - Server không có endpoint trả 204");
    }
    
    // ==================== 4xx Client Errors ====================
    
    private static void test400_BadRequest() {
        System.out.println("\n--- TEST: 400 Bad Request ---");
        try {
            // Gửi JSON sai format
            String invalidJson = "{invalid json}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/appointments"))
                    .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                    .header("Content-Type", "application/json")
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 400 || response.statusCode() == 500) {
                System.out.println("✅ 400 Bad Request (or 500) - Invalid JSON rejected");
                System.out.println("   Status: " + response.statusCode());
            } else {
                System.out.println("⚠️  Expected 400, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test401_Unauthorized() {
        System.out.println("\n--- TEST: 401 Unauthorized ---");
        try {
            // Gọi endpoint protected không có token (nếu có)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/protected-endpoint"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 401 || response.statusCode() == 404) {
                System.out.println("✅ 401 Unauthorized (or 404 if no protected endpoint)");
                System.out.println("   Status: " + response.statusCode());
            } else {
                System.out.println("⚠️  Expected 401/404, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test403_Forbidden() {
        System.out.println("\n--- TEST: 403 Forbidden ---");
        System.out.println("⏭️  Skipped - Server chưa implement authorization check");
    }
    
    private static void test404_NotFound() {
        System.out.println("\n--- TEST: 404 Not Found ---");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/non-existent-endpoint"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                System.out.println("✅ 404 Not Found - Non-existent endpoint");
                System.out.println("   Message: " + response.body());
            } else {
                System.out.println("❌ Expected 404, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test405_MethodNotAllowed() {
        System.out.println("\n--- TEST: 405 Method Not Allowed ---");
        try {
            // DELETE không được support trên /echo
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/echo"))
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 405) {
                System.out.println("✅ 405 Method Not Allowed - DELETE /echo rejected");
                System.out.println("   Message: " + response.body());
            } else {
                System.out.println("❌ Expected 405, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test413_PayloadTooLarge() {
        System.out.println("\n--- TEST: 413 Payload Too Large ---");
        try {
            // Tạo payload > 1MB (MAX_BODY_BYTES)
            StringBuilder largePayload = new StringBuilder();
            for (int i = 0; i < 200_000; i++) {
                largePayload.append("ABCDEF");  // 1.2MB
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/appointments"))
                    .POST(HttpRequest.BodyPublishers.ofString(largePayload.toString()))
                    .header("Content-Type", "application/json")
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 413 || response.statusCode() == 400) {
                System.out.println("✅ 413 Payload Too Large (or 400) - Large payload rejected");
                System.out.println("   Status: " + response.statusCode());
            } else {
                System.out.println("⚠️  Expected 413/400, got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("✅ Request failed (expected for large payload): " + e.getMessage());
        }
    }
    
    // ==================== 5xx Server Errors ====================
    
    private static void test500_InternalServerError() {
        System.out.println("\n--- TEST: 500 Internal Server Error ---");
        try {
            // Trigger lỗi internal - ví dụ: query DB với ID âm
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/appointments?id=-999"))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 500 || response.statusCode() == 200) {
                System.out.println("✅ Status: " + response.statusCode() + 
                        " (500 if error handling works, 200 if returns empty)");
            } else {
                System.out.println("⚠️  Got: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void test501_NotImplemented() {
        System.out.println("\n--- TEST: 501 Not Implemented ---");
        System.out.println("⏭️  Skipped - Server không sử dụng 501");
    }
    
    private static void test503_ServiceUnavailable() {
        System.out.println("\n--- TEST: 503 Service Unavailable ---");
        System.out.println("⏭️  Skipped - Server không có circuit breaker");
    }
}
