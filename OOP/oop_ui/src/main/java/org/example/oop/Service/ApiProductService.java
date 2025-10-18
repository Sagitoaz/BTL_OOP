package org.example.oop.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
