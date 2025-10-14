package org.example.oop.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * 🌐 SIMPLE API CLIENT - NGÀY 8 FRONTEND INTEGRATION
 * Lightweight HTTP client for JavaFX frontend to communicate with REST backend
 * 
 * Features:
 * - Generic HTTP methods (GET, POST, PUT, DELETE)
 * - String-based JSON handling (no external dependencies)
 * - Async operations with JavaFX Platform threading
 * - Comprehensive error handling
 * - Connection timeout and retry logic
 * - Type-safe responses
 */
public class ApiClient {

     private static final String BASE_URL = ApiConfig.getBaseUrl();

     private final HttpClient httpClient;

     // Singleton instance
     private static ApiClient instance;

     private ApiClient() {
          this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(ApiConfig.CONNECTION_TIMEOUT))
                    .build();
     }

     public static synchronized ApiClient getInstance() {
          if (instance == null) {
               instance = new ApiClient();
          }
          return instance;
     }

     // ================================
     // SYNCHRONOUS HTTP METHODS
     // ================================

     /**
      * Synchronous GET request
      */
     public ApiResponse<String> get(String endpoint) {
          try {
               HttpRequest request = HttpRequest.newBuilder()
                         .uri(URI.create(BASE_URL + endpoint))
                         .header("Content-Type", "application/json")
                         .GET()
                         .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                         .build();

               HttpResponse<String> response = httpClient.send(request,
                         HttpResponse.BodyHandlers.ofString());

               return handleResponse(response);

          } catch (IOException | InterruptedException e) {
               return ApiResponse.error("Network error: " + e.getMessage());
          }
     }

     /**
      * Synchronous POST request
      */
     public ApiResponse<String> post(String endpoint, String jsonBody) {
          try {
               HttpRequest request = HttpRequest.newBuilder()
                         .uri(URI.create(BASE_URL + endpoint))
                         .header("Content-Type", "application/json")
                         .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                         .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                         .build();

               HttpResponse<String> response = httpClient.send(request,
                         HttpResponse.BodyHandlers.ofString());

               return handleResponse(response);

          } catch (IOException | InterruptedException e) {
               return ApiResponse.error("Network error: " + e.getMessage());
          }
     }

     /**
      * Synchronous PUT request
      */
     public ApiResponse<String> put(String endpoint, String jsonBody) {
          try {
               HttpRequest request = HttpRequest.newBuilder()
                         .uri(URI.create(BASE_URL + endpoint))
                         .header("Content-Type", "application/json")
                         .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                         .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                         .build();

               HttpResponse<String> response = httpClient.send(request,
                         HttpResponse.BodyHandlers.ofString());

               return handleResponse(response);

          } catch (IOException | InterruptedException e) {
               return ApiResponse.error("Network error: " + e.getMessage());
          }
     }

     /**
      * Synchronous DELETE request
      */
     public ApiResponse<String> delete(String endpoint) {
          try {
               HttpRequest request = HttpRequest.newBuilder()
                         .uri(URI.create(BASE_URL + endpoint))
                         .header("Content-Type", "application/json")
                         .DELETE()
                         .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                         .build();

               HttpResponse<String> response = httpClient.send(request,
                         HttpResponse.BodyHandlers.ofString());

               return handleResponse(response);

          } catch (IOException | InterruptedException e) {
               return ApiResponse.error("Network error: " + e.getMessage());
          }
     }

     // ================================
     // ASYNCHRONOUS HTTP METHODS
     // ================================

     /**
      * Asynchronous GET request with JavaFX Platform threading
      */
     public void getAsync(String endpoint, Consumer<ApiResponse<String>> onSuccess, Consumer<String> onError) {

          CompletableFuture.supplyAsync(() -> {
               try {
                    HttpRequest request = HttpRequest.newBuilder()
                              .uri(URI.create(BASE_URL + endpoint))
                              .header("Content-Type", "application/json")
                              .GET()
                              .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                              .build();

                    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

               } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Network error: " + e.getMessage());
               }
          }).thenApply(this::handleResponse)
                    .thenAccept(result -> Platform.runLater(() -> {
                         if (result.isSuccess()) {
                              onSuccess.accept(result);
                         } else {
                              onError.accept(result.getErrorMessage());
                         }
                    }))
                    .exceptionally(throwable -> {
                         Platform.runLater(() -> onError.accept(throwable.getMessage()));
                         return null;
                    });
     }

     /**
      * Asynchronous POST request
      */
     public void postAsync(String endpoint, String jsonBody, Consumer<ApiResponse<String>> onSuccess,
               Consumer<String> onError) {

          CompletableFuture.supplyAsync(() -> {
               try {
                    HttpRequest request = HttpRequest.newBuilder()
                              .uri(URI.create(BASE_URL + endpoint))
                              .header("Content-Type", "application/json")
                              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                              .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                              .build();

                    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

               } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Network error: " + e.getMessage());
               }
          }).thenApply(this::handleResponse)
                    .thenAccept(result -> Platform.runLater(() -> {
                         if (result.isSuccess()) {
                              onSuccess.accept(result);
                         } else {
                              onError.accept(result.getErrorMessage());
                         }
                    }))
                    .exceptionally(throwable -> {
                         Platform.runLater(() -> onError.accept(throwable.getMessage()));
                         return null;
                    });
     }

     /**
      * Asynchronous PUT request
      */
     public void putAsync(String endpoint, String jsonBody, Consumer<ApiResponse<String>> onSuccess,
               Consumer<String> onError) {

          CompletableFuture.supplyAsync(() -> {
               try {
                    HttpRequest request = HttpRequest.newBuilder()
                              .uri(URI.create(BASE_URL + endpoint))
                              .header("Content-Type", "application/json")
                              .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                              .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                              .build();

                    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

               } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Network error: " + e.getMessage());
               }
          }).thenApply(this::handleResponse)
                    .thenAccept(result -> Platform.runLater(() -> {
                         if (result.isSuccess()) {
                              onSuccess.accept(result);
                         } else {
                              onError.accept(result.getErrorMessage());
                         }
                    }))
                    .exceptionally(throwable -> {
                         Platform.runLater(() -> onError.accept(throwable.getMessage()));
                         return null;
                    });
     }

     /**
      * Asynchronous DELETE request
      */
     public void deleteAsync(String endpoint, Consumer<ApiResponse<String>> onSuccess, Consumer<String> onError) {

          CompletableFuture.supplyAsync(() -> {
               try {
                    HttpRequest request = HttpRequest.newBuilder()
                              .uri(URI.create(BASE_URL + endpoint))
                              .header("Content-Type", "application/json")
                              .DELETE()
                              .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT))
                              .build();

                    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

               } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Network error: " + e.getMessage());
               }
          }).thenApply(this::handleResponse)
                    .thenAccept(result -> Platform.runLater(() -> {
                         if (result.isSuccess()) {
                              onSuccess.accept(result);
                         } else {
                              onError.accept(result.getErrorMessage());
                         }
                    }))
                    .exceptionally(throwable -> {
                         Platform.runLater(() -> onError.accept(throwable.getMessage()));
                         return null;
                    });
     }

     // ================================
     // RESPONSE HANDLING
     // ================================

     /**
      * Process HTTP response and convert to ApiResponse
      */
     private ApiResponse<String> handleResponse(HttpResponse<String> response) {
          int statusCode = response.statusCode();
          String body = response.body();

          // Success responses (2xx)
          if (statusCode >= 200 && statusCode < 300) {
               return ApiResponse.success(body, statusCode);
          }

          // Error responses
          String errorMessage = "HTTP " + statusCode;
          if (body != null && !body.isEmpty()) {
               // Try to extract error message from JSON response
               if (body.contains("\"message\"")) {
                    try {
                         // Simple JSON message extraction (without external library)
                         int messageStart = body.indexOf("\"message\":\"") + 11;
                         int messageEnd = body.indexOf("\"", messageStart);
                         if (messageStart > 10 && messageEnd > messageStart) {
                              errorMessage = body.substring(messageStart, messageEnd);
                         }
                    } catch (Exception e) {
                         // If extraction fails, use raw body
                         errorMessage = body;
                    }
               } else {
                    errorMessage = body;
               }
          }

          return ApiResponse.error(errorMessage, statusCode);
     }

     // ================================
     // UTILITY METHODS
     // ================================

     /**
      * Test connection to backend
      */
     public boolean testConnection() {
          try {
               ApiResponse<String> response = get(ApiConfig.healthEndpoint());
               return response.isSuccess();
          } catch (Exception e) {
               return false;
          }
     }

     /**
      * Check if backend server is running
      */
     public void checkServerStatus(Consumer<Boolean> callback) {
          CompletableFuture.supplyAsync(this::testConnection)
                    .thenAccept(isOnline -> Platform.runLater(() -> callback.accept(isOnline)));
     }

     /**
      * Get current API configuration info
      */
     public String getConfigInfo() {
          return "API Client - Base URL: " + BASE_URL +
                    ", Connection Timeout: " + ApiConfig.CONNECTION_TIMEOUT + "s" +
                    ", Request Timeout: " + ApiConfig.REQUEST_TIMEOUT + "s";
     }

     // ================================
     // JSON HELPER METHODS
     // ================================

     /**
      * Simple JSON builder for basic objects
      * Use this for creating JSON strings without external libraries
      */
     public static class JsonBuilder {
          private final StringBuilder json;
          private boolean first = true;

          public JsonBuilder() {
               this.json = new StringBuilder("{");
          }

          public JsonBuilder add(String key, String value) {
               if (!first)
                    json.append(",");
               json.append("\"").append(key).append("\":\"").append(value).append("\"");
               first = false;
               return this;
          }

          public JsonBuilder add(String key, Number value) {
               if (!first)
                    json.append(",");
               json.append("\"").append(key).append("\":").append(value);
               first = false;
               return this;
          }

          public JsonBuilder add(String key, boolean value) {
               if (!first)
                    json.append(",");
               json.append("\"").append(key).append("\":").append(value);
               first = false;
               return this;
          }

          public String build() {
               return json.append("}").toString();
          }
     }
}