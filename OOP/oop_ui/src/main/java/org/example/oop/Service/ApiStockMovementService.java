package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.HttpException;
import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.miniboot.app.util.GsonProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ApiStockMovementService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private static final Gson gson = GsonProvider.getGson();

    // tăng time out tránh mạng yếu
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000; // 60 seconds
    private static final int MAX_RETRIES = 3; // Retry 3 lần nếu timeout

    /**
     * GET /stock_movements - Lấy tất cả stock movements với retry mechanism
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<StockMovement> getAllStockMovements() throws Exception {
        String url = BASE_URL + ApiConfig.stockMovementsEndpoint();
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL()
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                int responseCode = conn.getResponseCode();
                String responseBody = readResponse(conn);

                if (responseCode >= 200 && responseCode < 300) {
                    if (!ErrorHandler.validateResponse(responseBody, "Tải danh sách stock movements")) {
                        return List.of();
                    }

                    try {
                        Type listType = new TypeToken<List<StockMovement>>() {
                        }.getType();
                        List<StockMovement> movements = gson.fromJson(responseBody, listType);
                        return movements;
                    } catch (Exception e) {
                        ErrorHandler.handleJsonParseError(e, "Parse stock movements list");
                        return List.of();
                    }
                } else {
                    ErrorHandler.showUserFriendlyError(responseCode, "Không thể tải danh sách stock movements");
                    throw new Exception("Server error: " + responseCode + " - " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        ErrorHandler.handleConnectionError(lastException,
                "Tải danh sách stock movements sau " + MAX_RETRIES + " lần thử");
        throw new Exception("Failed after " + MAX_RETRIES + " attempts. Last error: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    /**
     * GET /stock_movements?id={id} - Lấy stock movement theo ID
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public StockMovement getStockMovementById(int id) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.stockMovementsEndpoint() + "?id=" + id).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode == 200) {
            if (!ErrorHandler.validateResponse(responseBody, "Tải stock movement")) {
                return null;
            }

            try {
                StockMovement stockMovement = gson.fromJson(responseBody, StockMovement.class);
                return stockMovement;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse stock movement by ID");
                return null;
            }
        } else if (responseCode == 404) {
            return null;
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể tải stock movement");
            throw new Exception("Server error: " + responseCode);
        }
    }

    /**
     * POST /stock_movements - Tạo stock movement mới
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public StockMovement createStockMovement(StockMovement stockMovement) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.stockMovementsEndpoint()).toURL()
                .openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        String jsonBody = gson.toJson(stockMovement);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Tạo stock movement")) {
                return null;
            }

            try {
                StockMovement created = gson.fromJson(responseBody, StockMovement.class);
                return created;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse created stock movement");
                return null;
            }
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể tạo stock movement");
            throw new Exception("Failed to create stock movement: " + responseBody);
        }
    }

    /**
     * PUT /stock_movements - Cập nhật stock movement
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public StockMovement updateStockMovement(StockMovement stockMovement) throws Exception {
        if (stockMovement.getId() <= 0) {
            throw new Exception("Stock movement ID is missing or invalid: " + stockMovement.getId());
        }

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.stockMovementsEndpoint()).toURL()
                .openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        String jsonBody = gson.toJson(stockMovement);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Cập nhật stock movement")) {
                return null;
            }

            try {
                StockMovement updated = gson.fromJson(responseBody, StockMovement.class);
                return updated;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse updated stock movement");
                return null;
            }
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể cập nhật stock movement");
            throw new Exception("Failed to update stock movement: " + responseBody);
        }
    }

    /**
     * DELETE /stock_movements?id={id} - Xóa stock movement
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public boolean deleteStockMovement(int id) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.stockMovementsEndpoint() + "?id=" + id).toURL()
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
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể xóa stock movement");
            throw new Exception("Failed to delete stock movement: " + responseBody);
        }
    }

    /**
     * GET /stock_movements/filter - Lọc stock movements theo các tiêu chí
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<StockMovement> filterStockMovements(
            Integer productId, String moveType, String fromDate, String toDate) throws Exception {
        StringBuilder url = new StringBuilder(BASE_URL + ApiConfig.stockMovementsFilterEndpoint() + "?");

        if (productId != null)
            url.append("product_id=").append(productId).append("&");
        if (moveType != null && !moveType.isEmpty())
            url.append("move_type=").append(moveType).append("&");
        if (fromDate != null && !fromDate.isEmpty())
            url.append("from=").append(fromDate).append("&");
        if (toDate != null && !toDate.isEmpty())
            url.append("to=").append(toDate);

        String finalUrl = url.toString().replaceAll("&$", "");

        HttpURLConnection conn = (HttpURLConnection) URI.create(finalUrl).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Lọc stock movements")) {
                return List.of();
            }

            try {
                Type listType = new TypeToken<List<StockMovement>>() {
                }.getType();
                List<StockMovement> movements = gson.fromJson(responseBody, listType);
                return movements;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse filtered stock movements");
                return List.of();
            }
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể lọc stock movements");
            throw new Exception("Filter failed: " + responseBody);
        }
    }

    /**
     * GET /stock_movements/stats - Lấy thống kê movements
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public StockMovementStats getStats() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + ApiConfig.stockMovementsStatsEndpoint()).toURL()
                .openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode >= 200 && responseCode < 300) {
            if (!ErrorHandler.validateResponse(responseBody, "Tải thống kê movements")) {
                return new StockMovementStats(0, 0, 0);
            }

            try {
                Type mapType = new TypeToken<Map<String, Integer>>() {
                }.getType();
                Map<String, Integer> statsMap = gson.fromJson(responseBody, mapType);

                int total = statsMap.getOrDefault("total", 0);
                int in = statsMap.getOrDefault("in", 0);
                int out = statsMap.getOrDefault("out", 0);

                StockMovementStats stats = new StockMovementStats(total, in, out);
                return stats;
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse stock movement stats");
                return new StockMovementStats(0, 0, 0);
            }
        } else {
            ErrorHandler.showUserFriendlyError(responseCode, "Không thể tải thống kê movements");
            throw new Exception("Stats failed: " + responseBody);
        }
    }

    /**
     * Lấy movements theo product ID (helper method)
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<StockMovement> getMovementsByProductId(int productId) throws Exception {
        return filterStockMovements(productId, null, null, null);
    }

    /**
     * Lấy movements theo move type (helper method)
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public List<StockMovement> getMovementsByType(String moveType) throws Exception {
        return filterStockMovements(null, moveType, null, null);
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader br;
        if (conn.getResponseCode() < 400) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
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

    // ➕ THÊM: Inner class cho statistics
    public static class StockMovementStats {
        private final int total;
        private final int totalIn;
        private final int totalOut;

        public StockMovementStats(int total, int totalIn, int totalOut) {
            this.total = total;
            this.totalIn = totalIn;
            this.totalOut = totalOut;
        }

        public int getTotal() {
            return total;
        }

        public int getTotalIn() {
            return totalIn;
        }

        public int getTotalOut() {
            return totalOut;
        }

        public int getNetChange() {
            return totalIn - totalOut;
        }

        @Override
        public String toString() {
            return String.format(
                    "StockMovementStats{total=%d, in=%d, out=%d, net=%+d}",
                    total, totalIn, totalOut, getNetChange());
        }
    }
}
