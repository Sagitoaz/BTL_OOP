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
                    return Json.created(saved);
               } catch (Exception e) {
                    return HttpResponse.of(400, "text/plain",
                              ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
               }
          };
     }

     // PUT /products
     public Function<HttpRequest, HttpResponse> updateProduct() {
          return (HttpRequest req) -> {
               try {
                    Product product = Json.fromBytes(req.body, Product.class);

                    if (product.getId() <= 0) {
                         return HttpResponse.of(400, "text/plain",
                                   "Missing product ID".getBytes(StandardCharsets.UTF_8));
                    }

                    Product updated = productRepo.save(product);
                    return Json.ok(updated);
               } catch (Exception e) {
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
