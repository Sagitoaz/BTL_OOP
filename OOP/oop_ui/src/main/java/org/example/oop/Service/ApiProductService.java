package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.HttpException;
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

    /**
     * GET /products - Lấy tất cả products với retry mechanism
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<Product> getAllProducts() throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsEndpoint()).toURL()
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                int responseCode = conn.getResponseCode();
                String responseBody = readResponse(conn);

                if (responseCode >= 200 && responseCode < 300) {
                    if (!ErrorHandler.validateResponse(responseBody, "Tải danh sách sản phẩm")) {
                        return List.of();
                    }

                    try {
                        Type listType = new TypeToken<List<Product>>() {
                        }.getType();
                        List<Product> products = gson.fromJson(responseBody, listType);
                        return products;
                    } catch (Exception e) {
                        ErrorHandler.handleJsonParseError(e, "Parse products list");
                        return List.of();
                    }
                } else {
                    ErrorHandler.showUserFriendlyError(responseCode, "Không thể tải danh sách sản phẩm");
                    throw new Exception("Server error: " + responseCode + " - " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                throw e;
            }
        }

        ErrorHandler.handleConnectionError(lastException, "Tải danh sách sản phẩm sau " + MAX_RETRIES + " lần thử");
        throw new Exception("Failed after " + MAX_RETRIES + " attempts. Last error: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    /**
     * GET /products?id={id} - Lấy product theo ID
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public Product getProductById(int id) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsEndpoint() + "?id=" + id).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        switch (responseCode) {
            case 200: {
                if (!ErrorHandler.validateResponse(responseBody, "Tải thông tin sản phẩm")) {
                    return null;
                }

                try {
                    Product product = gson.fromJson(responseBody, Product.class);
                    return product;
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse product by ID");
                    return null;
                }
            }
            case 404:
                return null;
            default:
                ErrorHandler.showUserFriendlyError(responseCode, "Không thể tải thông tin sản phẩm");
                throw new Exception("Server error: " + responseCode);
        }
    }

    /**
     * GET /products/search?sku={sku} - Tìm product theo SKU
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public Product getProductBySku(String sku) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsSearchEndpoint() + "?sku=" + sku).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode == 200) {
            if (!ErrorHandler.validateResponse(responseBody, "Tìm sản phẩm theo SKU")) {
                return null;
            }

            try {
                Product product = gson.fromJson(responseBody, Product.class);
                return product;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse product by SKU");
                return null;
            }
        } else if (responseCode == 404) {
            return null;
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể tìm sản phẩm theo SKU");
            throw new Exception("Server error: " + responseCode);
        }
    }

    /**
     * POST /products - Tạo product mới
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public Product createProduct(Product product) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsEndpoint()).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        String jsonBody = gson.toJson(product);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Tạo sản phẩm mới")) {
                return null;
            }

            try {
                Product created = gson.fromJson(responseBody, Product.class);
                return created;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse created product");
                return null;
            }
        } else if (responseCode >= 500) {
            ErrorHandler.showUserFriendlyError(responseCode, "Lỗi server khi tạo sản phẩm");
            throw new Exception("Lỗi server (" + responseCode + "): " + responseBody);
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể tạo sản phẩm");
            throw new Exception("Lỗi tạo sản phẩm (" + responseCode + "): " + responseBody);
        }
    }

    /**
     * PUT /products - Cập nhật product
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public Product updateProduct(Product product) throws Exception {
        if (product.getId() <= 0) {
            throw new Exception("Product ID is missing or invalid: " + product.getId());
        }

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsEndpoint()).toURL().openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        String jsonBody = gson.toJson(product);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Cập nhật sản phẩm")) {
                return null;
            }

            try {
                Product updated = gson.fromJson(responseBody, Product.class);
                return updated;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse updated product");
                return null;
            }
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể cập nhật sản phẩm");
            throw new Exception("Failed to update product: " + responseBody);
        }
    }

    /**
     * DELETE /products?id={id} - Xóa product
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public boolean deleteProduct(int id) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.productsEndpoint() + "?id=" + id).toURL()
                .openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            return true;
        } else if (responseCode == 404) {
            return false;
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể xóa sản phẩm");
            throw new Exception("Failed to delete product: " + responseBody);
        }
    }

    /**
     * Tìm kiếm products theo keyword (local filter)
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<Product> searchProducts(String keyword) throws Exception {
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
