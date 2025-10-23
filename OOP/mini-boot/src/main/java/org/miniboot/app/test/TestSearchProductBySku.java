package org.miniboot.app.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Test endpoint tìm sản phẩm bằng SKU
 * Usage: java org.miniboot.app.test.TestSearchProductBySku <SKU>
 */
public class TestSearchProductBySku {
     private static final String BASE_URL = "http://localhost:8080";

     public static void main(String[] args) {
          // Danh sách SKU để test
          String[] testSkus = {
                    "LEN001",
                    "LEN002",
                    "FRM001",
                    "SRV101",
                    "INVALID_SKU" // Test case không tìm thấy
          };

          System.out.println("🧪 Testing /products/search endpoint");
          System.out.println("=====================================\n");

          for (String sku : testSkus) {
               testSearchBySku(sku);
               System.out.println(); // Dòng trống giữa các test
          }
     }

     private static void testSearchBySku(String sku) {
          try {
               String url = BASE_URL + "/products/search?sku=" + sku;
               System.out.println("🔍 Testing SKU: " + sku);
               System.out.println("📡 URL: " + url);

               HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
               conn.setRequestMethod("GET");
               conn.setRequestProperty("Accept", "application/json");
               conn.setConnectTimeout(5000);
               conn.setReadTimeout(5000);

               int responseCode = conn.getResponseCode();
               String responseBody = readResponse(conn);

               System.out.println("📦 Response Code: " + responseCode);
               System.out.println("📦 Response Body: " + responseBody);

               if (responseCode == 200) {
                    System.out.println("✅ SUCCESS - Product found!");
               } else if (responseCode == 404) {
                    System.out.println("⚠️ NOT FOUND - Product not found");
               } else {
                    System.out.println("❌ ERROR - Unexpected response code");
               }

          } catch (Exception e) {
               System.err.println("❌ EXCEPTION: " + e.getMessage());
               e.printStackTrace();
          }
     }

     private static String readResponse(HttpURLConnection conn) throws Exception {
          BufferedReader reader;

          // Kiểm tra response code để đọc từ đúng stream
          if (conn.getResponseCode() >= 400) {
               reader = new BufferedReader(
                         new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
          } else {
               reader = new BufferedReader(
                         new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
          }

          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
               response.append(line);
          }
          reader.close();
          return response.toString();
     }
}
