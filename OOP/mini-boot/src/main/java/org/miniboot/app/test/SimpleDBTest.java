package org.miniboot.app.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.miniboot.app.config.DatabaseConfig;

public class SimpleDBTest {
     public static void main(String[] args) {
          System.out.println("=== SIMPLE DATABASE TEST ===\n");

          try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
               System.out.println("✓ Connected to: " + conn.getMetaData().getURL());

               // Test 1: Count products
               System.out.println("\n1. Counting products...");
               String countSql = "SELECT COUNT(*) as total FROM Products WHERE is_active = TRUE";
               try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(countSql)) {
                    if (rs.next()) {
                         System.out.println("   Total active products: " + rs.getInt("total"));
                    }
               }

               // Test 2: List column names
               System.out.println("\n2. Checking table structure...");
               String selectSql = "SELECT * FROM Products WHERE is_active = TRUE LIMIT 1";
               try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(selectSql)) {

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    System.out.println("   Columns found: " + columnCount);

                    for (int i = 1; i <= columnCount; i++) {
                         String columnName = metaData.getColumnName(i);
                         String columnType = metaData.getColumnTypeName(i);
                         System.out.println("   - " + columnName + " (" + columnType + ")");
                    }

                    // Test 3: Try to fetch one product
                    if (rs.next()) {
                         System.out.println("\n3. Sample product:");
                         System.out.println("   ID: " + rs.getInt("id"));
                         System.out.println("   SKU: " + rs.getString("sku"));
                         System.out.println("   Name: " + rs.getString("name"));
                         System.out.println("   Price Cost: " + rs.getObject("price_cost"));
                         System.out.println("   Price Retail: " + rs.getObject("price_retail"));
                         System.out.println("   Qty: " + rs.getInt("qty_on_hand"));
                    } else {
                         System.out.println("\n3. No products found!");
                    }
               }

               System.out.println("\n=== TEST COMPLETED ===");

          } catch (Exception e) {
               System.err.println("ERROR: " + e.getMessage());
               e.printStackTrace();
          }
     }
}
