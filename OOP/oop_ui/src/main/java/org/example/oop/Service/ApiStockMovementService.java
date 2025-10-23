package org.example.oop.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Inventory.StockMovement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApiStockMovementService {
     private static final String BASE_URL = "http://localhost:8080";
     private static final Gson gson = GsonProvider.getGson();

     // tăng time out tránh mạng yếu
     private static final int CONNECT_TIMEOUT = 30000;
     private static final int READ_TIMEOUT = 60000; // 60 seconds
     private static final int MAX_RETRIES = 3; // Retry 3 lần nếu timeout

     // ✅ FIX: Đổi tên method và URL
     public List<StockMovement> getAllStockMovements() throws Exception {
          String url = BASE_URL + "/stock_movements";
          System.out.println("🔄 Fetching all stock movements from API...");
          System.out.println("🌐 URL: " + url);

          // ✅ Retry mechanism cho mạng yếu
          Exception lastException = null;
          for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
               try {
                    System.out.println("📡 Attempt " + attempt + "/" + MAX_RETRIES + "...");

                    // ✅ FIX URL: /stock_movements (đúng chính tả)
                    HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL()
                              .openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(CONNECT_TIMEOUT);
                    conn.setReadTimeout(READ_TIMEOUT);

                    int responseCode = conn.getResponseCode();
                    String responseBody = readResponse(conn);

                    if (responseCode >= 200 && responseCode < 300) {
                         // ✅ DEBUG: In ra JSON response
                         System.out.println("📦 Response Code: " + responseCode);
                         System.out.println("📦 Response Length: " + responseBody.length() + " bytes");
                         System.out.println("📦 JSON Response (first 500 chars): " +
                                   (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..."
                                             : responseBody));

                         // ✅ Kiểm tra xem có phải products không
                         if (responseBody.contains("\"sku\":")) {
                              System.err.println(
                                        "⚠️ WARNING: Response contains 'sku' field - this looks like PRODUCTS, not STOCK_MOVEMENTS!");
                              System.err.println("⚠️ URL was: " + url);
                         }

                         Type listType = new TypeToken<List<StockMovement>>() {
                         }.getType();
                         List<StockMovement> movements = gson.fromJson(responseBody, listType);

                         System.out.println("✅ Loaded " + movements.size() + " stock movements");

                         // ✅ DEBUG: In ra movement đầu tiên
                         if (!movements.isEmpty()) {
                              StockMovement first = movements.get(0);
                              System.out.println("📦 First movement: ID=" + first.getId() +
                                        ", ProductID=" + first.getProductId() +
                                        ", Qty=" + first.getQty() +
                                        ", Type=" + first.getMoveType());
                         }

                         return movements;
                    } else {
                         throw new Exception("Server error: " + responseCode + " - " + responseBody);
                    }
               } catch (java.net.SocketTimeoutException e) {
                    lastException = e;
                    System.err.println("⏱️ Timeout on attempt " + attempt + ": " + e.getMessage());
                    if (attempt < MAX_RETRIES) {
                         System.out.println("🔄 Retrying in 2 seconds...");
                         try {
                              Thread.sleep(2000); // Wait 2s trước khi retry
                         } catch (InterruptedException ie) {
                              Thread.currentThread().interrupt(); // Restore interrupted status
                              System.err.println("Thread was interrupted: " + ie.getMessage());
                         }
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

     // ✅ FIX URL
     public StockMovement getStockMovementById(int id) throws Exception {
          System.out.println("🔄 Fetching stock movement ID: " + id);

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/stock_movements?id=" + id).toURL()
                    .openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode == 200) {
               StockMovement stockMovement = gson.fromJson(responseBody, StockMovement.class);
               System.out.println("✅ Found stock movement: " + stockMovement.getId());
               return stockMovement;
          } else if (responseCode == 404) {
               throw new Exception("Stock movement not found");
          } else {
               throw new Exception("Server error: " + responseCode);
          }
     }

     // ✅ FIX: Đổi tên method và URL
     public StockMovement createStockMovement(StockMovement stockMovement) throws Exception {
          System.out.println("🔄 Creating stock movement for product ID: " + stockMovement.getProductId());

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/stock_movements").toURL()
                    .openConnection();
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type", "application/json");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);
          conn.setDoOutput(true);

          String jsonBody = gson.toJson(stockMovement);
          System.out.println("📤 Sending JSON: " + jsonBody.substring(0, Math.min(200, jsonBody.length())) + "...");

          try (OutputStream os = conn.getOutputStream()) {
               byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
               os.write(input, 0, input.length);
          }

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               StockMovement created = gson.fromJson(responseBody, StockMovement.class);
               System.out.println("✅ Stock movement created with ID: " + created.getId());
               return created;
          } else {
               throw new Exception("Failed to create stock movement: " + responseBody);
          }
     }

     public StockMovement updateStockMovement(StockMovement stockMovement) throws Exception {
          System.out.println("🔄 Updating stock movement ID: " + stockMovement.getId());
          if (stockMovement.getId() <= 0) {
               throw new Exception("Stock movement ID is missing or invalid: " + stockMovement.getId());
          }
          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/stock_movements").toURL()
                    .openConnection();
          conn.setRequestMethod("PUT");
          conn.setRequestProperty("Content-Type", "application/json");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);
          conn.setDoOutput(true);
          String jsonBody = gson.toJson(stockMovement);
          System.out.println("📤 Sending JSON: " + jsonBody.substring(0, Math.min(200, jsonBody.length())) + "...");

          try (OutputStream os = conn.getOutputStream()) {
               byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
               os.write(input, 0, input.length);
          }

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               StockMovement updated = gson.fromJson(responseBody, StockMovement.class);
               System.out.println("✅ Stock movement updated: " + updated.getId());
               return updated;
          } else {
               throw new Exception("Failed to update stock movement: " + responseBody);
          }
     }

     // ✅ FIX: Đổi tên method và URL
     public boolean deleteStockMovement(int id) throws Exception {
          System.out.println("🔄 Deleting stock movement ID: " + id);

          // ✅ FIX URL: /stock_movements (có 's')
          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/stock_movements?id=" + id).toURL()
                    .openConnection();
          conn.setRequestMethod("DELETE");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               System.out.println("✅ Stock movement deleted: " + responseBody);
               return true;
          } else if (responseCode == 404) {
               throw new Exception("Stock movement not found");
          } else {
               throw new Exception("Failed to delete stock movement: " + responseBody);
          }
     }

     public List<StockMovement> filterStockMovements(
               Integer productId, String moveType, String fromDate, String toDate) throws Exception {
          System.out.println("🔄 Filtering stock movements...");

          StringBuilder url = new StringBuilder(BASE_URL + "/stock_movements/filter?");

          if (productId != null)
               url.append("product_id=").append(productId).append("&");
          if (moveType != null && !moveType.isEmpty())
               url.append("move_type=").append(moveType).append("&");
          if (fromDate != null && !fromDate.isEmpty())
               url.append("from=").append(fromDate).append("&");
          if (toDate != null && !toDate.isEmpty())
               url.append("to=").append(toDate);

          // Remove trailing & if exists
          String finalUrl = url.toString().replaceAll("&$", "");
          System.out.println("📡 Filter URL: " + finalUrl);

          HttpURLConnection conn = (HttpURLConnection) URI.create(finalUrl).toURL().openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               Type listType = new TypeToken<List<StockMovement>>() {
               }.getType();
               List<StockMovement> movements = gson.fromJson(responseBody, listType);
               System.out.println("✅ Filtered " + movements.size() + " movements");
               return movements;
          } else {
               throw new Exception("Filter failed: " + responseBody);
          }
     }

     // ➕ THÊM: Lấy thống kê movements
     public StockMovementStats getStats() throws Exception {
          System.out.println("🔄 Getting stock movement statistics...");

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/stock_movements/stats").toURL()
                    .openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(CONNECT_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               Type mapType = new TypeToken<Map<String, Integer>>() {
               }.getType();
               Map<String, Integer> statsMap = gson.fromJson(responseBody, mapType);

               int total = statsMap.getOrDefault("total", 0);
               int in = statsMap.getOrDefault("in", 0);
               int out = statsMap.getOrDefault("out", 0);

               StockMovementStats stats = new StockMovementStats(total, in, out);
               System.out.println("✅ Stats: " + stats);
               return stats;
          } else {
               throw new Exception("Stats failed: " + responseBody);
          }
     }

     // ➕ THÊM: Lấy movements theo product ID
     public List<StockMovement> getMovementsByProductId(int productId) throws Exception {
          System.out.println("🔄 Getting movements for product ID: " + productId);
          return filterStockMovements(productId, null, null, null);
     }

     // ➕ THÊM: Lấy movements theo move type
     public List<StockMovement> getMovementsByType(String moveType) throws Exception {
          System.out.println("🔄 Getting movements by type: " + moveType);
          return filterStockMovements(null, moveType, null, null);
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
}
