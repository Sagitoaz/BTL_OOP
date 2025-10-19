package org.example.oop.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.example.oop.Model.Inventory.Product;
import org.example.oop.Utils.GsonProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApiProductService {
     private static final String BASE_URL = "http://localhost:8080";
     private static final Gson gson = GsonProvider.getGson();

     public List<Product> getAllProducts() throws Exception {
          System.out.println("🔄 Fetching all products from API...");

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");
          conn.setConnectTimeout(5000); // 5 seconds connect timeout
          conn.setReadTimeout(10000); // 10 seconds read timeout

          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);

          if (responseCode >= 200 && responseCode < 300) {
               Type listType = new TypeToken<List<Product>>() {
               }.getType();
               List<Product> products = gson.fromJson(responseBody, listType);
               System.out.println("✅ Loaded " + products.size() + " products");
               return products;
          } else {
               throw new Exception("Server error: " + responseCode + " - " + responseBody);
          }
     }

     public Product getProductById(int id) throws Exception {
          System.out.println("🔄 Fetching product ID: " + id);

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products?id=" + id).toURL()
                    .openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");

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
          System.out.print("🔄 Creating product:" + product.getName());
          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type", "application/json");
          conn.setRequestProperty("Accept", "application/json");
          conn.setDoOutput(true);
          String jsonBody = gson.toJson(product);
          try (OutputStream os = conn.getOutputStream()) {
               byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
               os.write(input, 0, input.length);
          }
          int responseCode = conn.getResponseCode();
          String responseBody = readResponse(conn);
          if (responseCode >= 200 && responseCode < 300) {
               Product created = gson.fromJson(responseBody, Product.class);
               System.out.println("✅ Product created with ID: " + created.getId());
               return created;
          } else {
               throw new Exception("Failed to create product: " + responseBody);
          }
     }

     public Product updateProduct(Product product) throws Exception {
          System.out.println("🔄 Updating product ID: " + product.getId());

          HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
          conn.setRequestMethod("PUT");
          conn.setRequestProperty("Content-Type", "application/json");
          conn.setRequestProperty("Accept", "application/json");
          conn.setDoOutput(true);

          // Write request body
          String jsonBody = gson.toJson(product);
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
