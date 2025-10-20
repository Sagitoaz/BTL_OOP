package org.miniboot.app.controllers.Inventory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.domain.repo.Inventory.ProductRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

public class InventoryController {
     private final ProductRepository productRepo;

     public InventoryController(ProductRepository productRepo) {
          this.productRepo = productRepo;
     }

     public static void mount(Router router, InventoryController ic) {
          router.get("/products", ic.getProducts());
          router.post("/products", ic.createProduct());
          router.put("/products", ic.updateProduct());
          router.delete("/products", ic.deleteProduct());
     }

     public Function<HttpRequest, HttpResponse> getProduct() {
          return (HttpRequest req) -> {
               Map<String, List<String>> q = req.query;
               Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

               if (idOpt.isPresent()) {
                    return productRepo.findById(idOpt.get())
                              .map(Json::ok)
                              .orElse(HttpResponse.of(404, "text/plain",
                                        "Product not found".getBytes(StandardCharsets.UTF_8)));
               }

               return Json.ok(productRepo.findAll());
          };
     }

     public Function<HttpRequest, HttpResponse> getProducts() {
          return (HttpRequest req) -> {
               try {
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    if (idOpt.isPresent()) {
                         return productRepo.findById(idOpt.get())
                                   .map(Json::ok)
                                   .orElse(HttpResponse.of(404, "text/plain",
                                             "Product not found".getBytes(StandardCharsets.UTF_8)));
                    }

                    System.out.println("🔄 Fetching all products...");
                    List<Product> products = productRepo.findAll();
                    System.out.println("📦 Got " + products.size() + " products from repo");

                    System.out.println("🔄 Converting to JSON...");
                    HttpResponse response = Json.ok(products);
                    System.out.println("✅ JSON conversion successful");

                    return response;
               } catch (Exception e) {
                    System.err.println("❌ ERROR in getProducts():");
                    System.err.println("   Type: " + e.getClass().getName());
                    System.err.println("   Message: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
               }
          };
     }

     // POST /products
     public Function<HttpRequest, HttpResponse> createProduct() {
          return (HttpRequest req) -> {
               try {
                    Product product = Json.fromBytes(req.body, Product.class);
                    Product saved = productRepo.save(product);

                    // ✅ CHECK NULL: If save failed, return 500 error
                    if (saved == null) {
                         System.err.println("❌ ERROR: productRepo.save() returned null");
                         return HttpResponse.of(500, "text/plain",
                                   "Failed to save product to database".getBytes(StandardCharsets.UTF_8));
                    }

                    return Json.created(saved);
               } catch (Exception e) {
                    System.err.println("❌ ERROR in createProduct(): " + e.getMessage());
                    e.printStackTrace();
                    return HttpResponse.of(400, "text/plain",
                              ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
               }
          };
     }

     // PUT /products
     public Function<HttpRequest, HttpResponse> updateProduct() {
          return (HttpRequest req) -> {
               try {
                    System.out.println("📥 PUT /products - Parsing request body...");
                    System.out.println("   Body size: " + req.body.length + " bytes");

                    Product product = Json.fromBytes(req.body, Product.class);

                    System.out.println("✅ Product parsed successfully:");
                    System.out.println("   ID: " + product.getId());
                    System.out.println("   SKU: " + product.getSku());
                    System.out.println("   Name: " + product.getName());
                    System.out.println("   Category: " + product.getCategory());

                    if (product.getId() <= 0) {
                         System.err.println("❌ ERROR: Missing or invalid product ID: " + product.getId());
                         return HttpResponse.of(400, "text/plain",
                                   "Missing product ID".getBytes(StandardCharsets.UTF_8));
                    }

                    System.out.println("🔄 Saving product to database...");
                    Product updated = productRepo.save(product);
                    System.out.println("✅ Product updated successfully: ID=" + updated.getId());

                    return Json.ok(updated);
               } catch (Exception e) {
                    System.err.println("❌ ERROR in updateProduct():");
                    System.err.println("   Type: " + e.getClass().getName());
                    System.err.println("   Message: " + e.getMessage());
                    e.printStackTrace();

                    return HttpResponse.of(400, "text/plain",
                              ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
               }
          };
     }

     // DELETE /products?id=123
     public Function<HttpRequest, HttpResponse> deleteProduct() {
          return (HttpRequest req) -> {
               Map<String, List<String>> q = req.query;
               Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

               if (idOpt.isEmpty()) {
                    return HttpResponse.of(400, "text/plain",
                              "Missing id parameter".getBytes(StandardCharsets.UTF_8));
               }

               boolean deleted = productRepo.deleteById(idOpt.get());

               if (deleted) {
                    return HttpResponse.of(200, "text/plain",
                              "Product deleted".getBytes(StandardCharsets.UTF_8));
               } else {
                    return HttpResponse.of(404, "text/plain",
                              "Product not found".getBytes(StandardCharsets.UTF_8));
               }
          };
     }
}
