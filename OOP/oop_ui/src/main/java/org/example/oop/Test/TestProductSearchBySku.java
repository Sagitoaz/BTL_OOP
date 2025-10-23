package org.example.oop.Test;

import org.example.oop.Service.ApiProductService;
import org.miniboot.app.domain.models.Inventory.Product;

/**
 * Test tìm kiếm sản phẩm bằng SKU
 */
public class TestProductSearchBySku {
     public static void main(String[] args) {
          ApiProductService productService = new ApiProductService();

          // Danh sách SKU để test
          String[] testSkus = {
                    "LEN001",
                    "LEN002",
                    "FRM001",
                    "SRV101",
                    "INVALID_SKU" // Test case không tìm thấy
          };

          System.out.println("🧪 TESTING PRODUCT SEARCH BY SKU");
          System.out.println("=================================\n");

          for (String sku : testSkus) {
               testSearch(productService, sku);
               System.out.println(); // Dòng trống giữa các test
          }
     }

     private static void testSearch(ApiProductService service, String sku) {
          try {
               System.out.println("🔍 Searching for SKU: " + sku);
               System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

               Product product = service.getProductBySku(sku);

               if (product != null) {
                    System.out.println("✅ FOUND!");
                    System.out.println("   ID: " + product.getId());
                    System.out.println("   SKU: " + product.getSku());
                    System.out.println("   Name: " + product.getName());
                    System.out.println("   Category: " + product.getCategory());
                    System.out.println("   Price Retail: " + product.getPriceRetail());
                    System.out.println("   Qty On Hand: " + product.getQtyOnHand());
                    System.out.println("   Active: " + product.isActive());
               } else {
                    System.out.println("⚠️ Product returned null");
               }

          } catch (Exception e) {
               if (e.getMessage().contains("Product not found")) {
                    System.out.println("❌ NOT FOUND - Product with SKU '" + sku + "' does not exist");
               } else {
                    System.out.println("❌ ERROR: " + e.getMessage());
                    e.printStackTrace();
               }
          }
     }
}
