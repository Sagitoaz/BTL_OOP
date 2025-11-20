package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ApiConfig;
import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.util.GsonProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiProductService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private static final Gson gson = GsonProvider.getGson();
    private static final int CONNECT_TIMEOUT = 30000; // 30 seconds
    private static final int READ_TIMEOUT = 60000; // 60 seconds
    private static final int MAX_RETRIES = 3; // Retry 3 lần nếu timeout

    public List<Product> getAllProducts() throws Exception {
        System.out.println("🔄 Fetching all products from API...");

        // ✅ Retry mechanism cho mạng yếu
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("📡 Attempt " + attempt + "/" + MAX_RETRIES + "...");

                HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL()
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                
                // Add JWT token for authentication
                String token = SessionStorage.getJwtToken();
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                
                conn.setConnectTimeout(CONNECT_TIMEOUT); // 30 seconds
                conn.setReadTimeout(READ_TIMEOUT); // 60 seconds

                int responseCode = conn.getResponseCode();
                String responseBody = readResponse(conn);

                if (responseCode >= 200 && responseCode < 300) {
                    System.out.println("📦 JSON Response (first 500 chars): " +
                            (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..."
                                    : responseBody));

                    Type listType = new TypeToken<List<Product>>() {
                    }.getType();
                    List<Product> products = gson.fromJson(responseBody, listType);

                    // ✅ DEBUG: In ra sample product
                    if (!products.isEmpty()) {
                        Product sample = products.get(0);
                        System.out.println("📦 Sample Product:");
                        System.out.println("   - ID: " + sample.getId());
                        System.out.println("   - Name: " + sample.getName());
                        System.out.println("   - QtyOnHand: " + sample.getQtyOnHand());
                        System.out.println("   - PriceRetail: " + sample.getPriceRetail());
                        System.out.println("   - PriceCost: " + sample.getPriceCost());
                        System.out.println("   - Category: " + sample.getCategory());
                        System.out.println("   - IsActive: " + sample.isActive());
                    }

                    System.out.println("✅ Loaded " + products.size() + " products");
                    return products;
                } else {
                    throw new Exception("Server error: " + responseCode + " - " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                lastException = e;
                System.err.println("⏱️ Timeout on attempt " + attempt + ": " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    System.out.println("🔄 Retrying in 2 seconds...");
                    Thread.sleep(2000); // Wait 2s trước khi retry
                }
            } catch (Exception e) {
                // Lỗi khác không retry
                throw e;
            }
        }

        // Nếu retry hết vẫn fail
        throw new Exception("Failed after " + MAX_RETRIES + " attempts. Last error: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    public Product getProductById(int id) throws Exception {
        System.out.println("🔄 Fetching product ID: " + id);

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products?id=" + id).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // Add JWT token
        String token = SessionStorage.getJwtToken();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        switch (responseCode) {
            case 200: {
                Product product = gson.fromJson(responseBody, Product.class);
                System.out.println("✅ Found product: " + product.getName());
                return product;
            }
            case 404:
                throw new Exception("Product not found");
            default:
                throw new Exception("Server error: " + responseCode);
        }
    }

    public Product getProductBySku(String sku) throws Exception {
        System.out.println("🔄 Fetching product SKU: " + sku);

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products/search?sku=" + sku).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // Add JWT token
        String token = SessionStorage.getJwtToken();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode == 200) {
            Product product = gson.fromJson(responseBody, Product.class);
            System.out.println("✅ Found product: " + product.getName());
            return product;
        } else if (responseCode == 404) {
            throw new Exception("Product not found");
        } else {
            throw new Exception("Server error: " + responseCode);
        }
    }

    public Product createProduct(Product product) throws Exception {
        System.out.println("🔄 Creating product: " + product.getName());
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        
        // Add JWT token
        String token = SessionStorage.getJwtToken();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        // Serialize product to JSON
        String jsonBody = gson.toJson(product);
        System.out.println("📤 Sending JSON: " + jsonBody);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        System.out.println("📥 Response Code: " + responseCode);
        System.out.println("📥 Response Body: " + responseBody);

        if (responseCode >= 200 && responseCode < 300) {
            // Check if response body is empty
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.out.println("⚠️ Warning: Server returned empty response body");
                return null;
            }

            Product created = gson.fromJson(responseBody, Product.class);

            if (created == null) {
                System.out.println("⚠️ Warning: Failed to parse JSON response");
                return null;
            }

            System.out.println("✅ Product created with ID: " + created.getId());
            return created;
        } else {
        // Build detailed error message
        String errorMessage = "HTTP " + responseCode + ": ";

        // Try to parse JSON error response
        if (responseBody != null && responseBody.contains("{") && responseBody.contains("message")) {
            // Response có JSON format
            errorMessage += responseBody; // Keep full JSON for parsing in Controller
        } else {
            // Plain text response
            errorMessage += (responseBody != null ? responseBody : "Unknown error");
        }

        throw new Exception(errorMessage);
    }
    }

    public Product updateProduct(Product product) throws Exception {
        System.out.println("🔄 Updating product ID: " + product.getId());

        // 🔍 DEBUG: Check product data before sending
        if (product.getId() <= 0) {
            throw new Exception("Product ID is missing or invalid: " + product.getId());
        }

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        
        // Add JWT token
        String token = SessionStorage.getJwtToken();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        // Write request body
        String jsonBody = gson.toJson(product);
        System.out.println("📤 Sending JSON: " + jsonBody.substring(0, Math.min(200, jsonBody.length())) + "...");

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            Product updated = gson.fromJson(responseBody, Product.class);
            System.out.println("✅ Product updated: " + updated.getName());
            return updated;
        } else {
            throw new Exception("Failed to update product: " + responseBody);
        }
    }

    public boolean deleteProduct(int id) throws Exception {
        System.out.println("🔄 Deleting product ID: " + id);

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products?id=" + id).toURL()
                .openConnection();
        conn.setRequestMethod("DELETE");

        // Add JWT token
        String token = SessionStorage.getJwtToken();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("✅ Product deleted: " + responseBody);
            return true;
        } else if (responseCode == 404) {
            throw new Exception("Product not found");
        } else {
            throw new Exception("Failed to delete product: " + responseBody);
        }
    }

    public List<Product> searchProducts(String keyword) throws Exception {
        System.out.println("🔄 Searching products with keyword: " + keyword);

        List<Product> allProducts = getAllProducts();

        String kw = keyword == null ? "" : keyword.toLowerCase();

        return allProducts.stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(kw)) ||
                        (p.getCategory() != null && String.valueOf(p.getCategory()).toLowerCase().contains(kw)) ||
                        (p.getSku() != null && p.getSku().toLowerCase().contains(kw)))
                .toList();
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader br;
        if (conn.getResponseCode() < 400) {
            br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            br = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        return response.toString();
    }
}
