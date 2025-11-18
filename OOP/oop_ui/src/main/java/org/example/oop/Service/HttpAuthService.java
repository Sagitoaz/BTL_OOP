package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.GsonProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP Authentication Service - Handles JWT-based authentication
 * Calls the /auth/login endpoint to get JWT token
 */
public class HttpAuthService {
    
    private static final Logger LOGGER = Logger.getLogger(HttpAuthService.class.getName());
    private static final Gson gson = GsonProvider.createGson();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    
    private static HttpAuthService instance;
    
    private HttpAuthService() {
        this.baseUrl = ApiConfig.getBaseUrl();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // Force HTTP/1.1
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
    }
    
    public static synchronized HttpAuthService getInstance() {
        if (instance == null) {
            instance = new HttpAuthService();
        }
        return instance;
    }
    
    /**
     * Login and get JWT token
     * Calls POST /auth/login
     * 
     * @param username Username
     * @param password Password
     * @return JWT access token if successful, null otherwise
     */
    public LoginResult login(String username, String password) {
        try {
            // Build JSON body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("username", username);
            requestBody.addProperty("password", password);
            
            String jsonBody = gson.toJson(requestBody);
            
            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .version(HttpClient.Version.HTTP_1_1) // Force HTTP/1.1
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(DEFAULT_TIMEOUT)
                    .build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse response
                JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
                
                // Backend returns "access_token" (with underscore), not "accessToken"
                String accessToken = responseJson.has("access_token") ? 
                        responseJson.get("access_token").getAsString() : null;
                String tokenType = responseJson.has("token_type") ? 
                        responseJson.get("token_type").getAsString() : "Bearer";
                
                if (accessToken != null) {
                    LOGGER.info("Login successful via HTTP, got JWT token");
                    return new LoginResult(true, accessToken, tokenType, null);
                } else {
                    LOGGER.warning("Login response missing access_token field");
                    return new LoginResult(false, null, null, "Invalid response format");
                }
            } else {
                // Parse error message
                String errorMessage = "Login failed: HTTP " + response.statusCode();
                try {
                    JsonObject errorJson = gson.fromJson(response.body(), JsonObject.class);
                    if (errorJson.has("message")) {
                        errorMessage = errorJson.get("message").getAsString();
                    } else if (errorJson.has("error")) {
                        errorMessage = errorJson.get("error").getAsString();
                    }
                } catch (Exception e) {
                    // Use default error message
                }
                
                LOGGER.warning("Login failed: " + errorMessage);
                return new LoginResult(false, null, null, errorMessage);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login request failed", e);
            return new LoginResult(false, null, null, "Network error: " + e.getMessage());
        }
    }
    
    /**
     * Result of login operation
     */
    public static class LoginResult {
        private final boolean success;
        private final String accessToken;
        private final String tokenType;
        private final String errorMessage;
        
        public LoginResult(boolean success, String accessToken, String tokenType, String errorMessage) {
            this.success = success;
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getTokenType() {
            return tokenType;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
