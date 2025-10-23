package org.miniboot.app.test;

import java.util.List;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.domain.repo.Inventory.PostgreSQLProductRepository;

public class TestProductRepo {
     public static void main(String[] args) {
          System.out.println("🧪 Testing Product Repository...\n");

          try {
               // Test database connection
               System.out.println("1️⃣ Testing database connection...");
               var conn = DatabaseConfig.getInstance().getConnection();
               System.out.println("✅ Database connected: " + conn.getMetaData().getURL());
               conn.close();

               // Test repository
               System.out.println("\n2️⃣ Testing repository findAll()...");
               var repo = new PostgreSQLProductRepository();
               List<Product> products = repo.findAll();

               System.out.println("✅ Query successful!");
               System.out.println("📦 Found " + products.size() + " products\n");

               // Print first 3 products
               int count = Math.min(3, products.size());
               for (int i = 0; i < count; i++) {
                    Product p = products.get(i);
                    System.out.println("Product #" + (i + 1) + ":");
                    System.out.println("  ID: " + p.getId());
                    System.out.println("  SKU: " + p.getSku());
                    System.out.println("  Name: " + p.getName());
                    System.out.println("  Price Cost: " + p.getPriceCost());
                    System.out.println("  Price Retail: " + p.getPriceRetail());
                    System.out.println("  Qty: " + p.getQtyOnHand());
                    System.out.println("  Expiry: " + p.getExpiryDate());
                    System.out.println("  Created: " + p.getCreatedAt());
                    System.out.println();
               }

               System.out.println("🎉 All tests passed!");

          } catch (Exception e) {
               System.err.println("❌ Test failed!");
               e.printStackTrace();
          }
     }
}
