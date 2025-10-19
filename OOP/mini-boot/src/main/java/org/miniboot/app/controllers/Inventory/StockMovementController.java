package org.miniboot.app.controllers.Inventory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.miniboot.app.domain.repo.Inventory.StockMovementRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

public class StockMovementController {
     private StockMovementRepository stockMoveRepo;

     public StockMovementController(StockMovementRepository sm) {
          this.stockMoveRepo = sm;
     }

     public static void mount(Router router, StockMovementController sc) {
          router.get("/stock_movements", sc.getProducts());
          router.post("/stock_movements", sc.createProduct());
          router.put("/stock_movements", sc.updateProduct());
          router.delete("/stock_movements", sc.deleteProduct());
     }

     public Function<HttpRequest, HttpResponse> getProduct() {
          return (HttpRequest req) -> {
               Map<String, List<String>> q = req.query;
               Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

               if (idOpt.isPresent()) {
                    return stockMoveRepo.findById(idOpt.get())
                              .map(Json::ok)
                              .orElse(HttpResponse.of(404, "text/plain",
                                        "Product not found".getBytes(StandardCharsets.UTF_8)));
               }

               return Json.ok(stockMoveRepo.findAll());
          };
     }

     public Function<HttpRequest, HttpResponse> getProducts() {
          return (HttpRequest req) -> {
               try {
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    if (idOpt.isPresent()) {
                         return stockMoveRepo.findById(idOpt.get())
                                   .map(Json::ok)
                                   .orElse(HttpResponse.of(404, "text/plain",
                                             "Product not found".getBytes(StandardCharsets.UTF_8)));
                    }

                    System.out.println("🔄 Fetching all products...");
                    List<StockMovement> st = stockMoveRepo.findAll();
                    System.out.println("📦 Got " + st.size() + " products from repo");

                    System.out.println("🔄 Converting to JSON...");
                    HttpResponse response = Json.ok(st);
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
                    StockMovement product = Json.fromBytes(req.body, StockMovement.class);
                    StockMovement saved = stockMoveRepo.save(product);
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
                    StockMovement product = Json.fromBytes(req.body, StockMovement.class);

                    if (product.getId() <= 0) {
                         return HttpResponse.of(400, "text/plain",
                                   "Missing product ID".getBytes(StandardCharsets.UTF_8));
                    }

                    StockMovement updated = stockMoveRepo.save(product);
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

               boolean deleted = stockMoveRepo.deleteById(idOpt.get());

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
