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
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.ValidationUtils;

public class InventoryController {
     private final ProductRepository productRepo;

     public InventoryController(ProductRepository productRepo) {
          this.productRepo = productRepo;
     }

     public static void mount(Router router, InventoryController ic) {
          router.get("/products", ic.getProducts());
          router.get("/products/search", ic.searchProductBySku());
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

     public Function<HttpRequest, HttpResponse> searchProductBySku() {
          return (HttpRequest req) -> {
               try {
                    Map<String, List<String>> q = req.query;
                    Optional<String> skuOpt = ExtractHelper.extractString(q, "sku");

                    if (skuOpt.isEmpty()) {
                         return HttpResponse.of(400, "text/plain",
                                   "Missing sku parameter".getBytes(StandardCharsets.UTF_8));
                    }

                    String sku = skuOpt.get();
                    System.out.println("🔍 Searching product by SKU: " + sku);

                    Optional<Product> productOpt = productRepo.findBySku(sku);

                    if (productOpt.isPresent()) {
                         Product product = productOpt.get();
                         System.out.println("✅ Found product: ID=" + product.getId() + ", Name=" + product.getName());
                         return Json.ok(product);
                    } else {
                         System.out.println("❌ Product not found with SKU: " + sku);
                         return HttpResponse.of(404, "text/plain",
                                   "Product not found".getBytes(StandardCharsets.UTF_8));
                    }
               } catch (Exception e) {
                    System.err.println("❌ ERROR in searchProductBySku():");
                    System.err.println("   Type: " + e.getClass().getName());
                    System.err.println("   Message: " + e.getMessage());
                    e.printStackTrace();
                    return HttpResponse.of(500, "text/plain",
                              ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
               }
          };
     }

    public Function<HttpRequest, HttpResponse> createProduct() {
        return (HttpRequest req) -> {
            // STEP 1: Validate Content-Type (415)
            HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
            if (contentTypeError != null) return contentTypeError;

            // STEP 2: Validate JWT (401)
            HttpResponse authError = ValidationUtils.validateJWT(req);
            if (authError != null) return authError;

            // STEP 3: Validate Role - ADMIN only (403)
            HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
            if (roleError != null) return roleError;

            try {
                // STEP 4: Parse JSON (400)
                Product product;
                try {
                    product = Json.fromBytes(req.body, Product.class);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Invalid JSON format: " + e.getMessage());
                }

                // STEP 5: Validate required fields (400)
                if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "SKU is required");
                }
                if (product.getName() == null || product.getName().trim().isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Name is required");
                }

                // STEP 6: Validate business rules (422)
                HttpResponse businessRuleError = ValidationUtils.validateProductBusinessRules(
                        product.getQtyOnHand(),
                        product.getPriceCost(),
                        product.getPriceRetail()
                );
                if (businessRuleError != null) return businessRuleError;

                // STEP 7: Check SKU conflict (409)
                try {
                    Optional<Product> existing = productRepo.findBySku(product.getSku());
                    if (existing.isPresent()) {
                        return ValidationUtils.error(409, "INVENTORY_CONFLICT",
                                "Product with SKU '" + product.getSku() + "' already exists");
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // STEP 8: Save to database (503/504/500)
                Product saved;
                try {
                    saved = productRepo.save(product);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (saved == null) {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to save product to database");
                }

                // STEP 9: Success (201)
                return Json.created(saved);

            } catch (Exception e) {
                // STEP 10: Catch-all for unexpected errors (500)
                System.err.println("❌ Unexpected error in createProduct: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    public Function<HttpRequest, HttpResponse> updateProduct() {
        return (HttpRequest req) -> {
            // Validations giống createProduct
            HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
            if (contentTypeError != null) return contentTypeError;

            HttpResponse authError = ValidationUtils.validateJWT(req);
            if (authError != null) return authError;

            HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
            if (roleError != null) return roleError;

            try {
                Product product = Json.fromBytes(req.body, Product.class);

                // Validate ID (400)
                if (product.getId() <= 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Product ID is required for update");
                }

                // Check existence (404)
                Optional<Product> existing;
                try {
                    existing = productRepo.findById(product.getId());
                    if (existing.isEmpty()) {
                        return ValidationUtils.error(404, "NOT_FOUND",
                                "Product with ID " + product.getId() + " not found");
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Validate business rules
                HttpResponse businessRuleError = ValidationUtils.validateProductBusinessRules(
                        product.getQtyOnHand(),
                        product.getPriceCost(),
                        product.getPriceRetail()
                );
                if (businessRuleError != null) return businessRuleError;

                // Check SKU conflict with OTHER products (409)
                if (product.getSku() != null &&
                        !product.getSku().equals(existing.get().getSku())) {
                    try {
                        Optional<Product> skuConflict = productRepo.findBySku(product.getSku());
                        if (skuConflict.isPresent() &&
                                skuConflict.get().getId() != product.getId()) {
                            return ValidationUtils.error(409, "SKU_CONFLICT",
                                    "SKU '" + product.getSku() + "' is already used by another product");
                        }
                    } catch (Exception e) {
                        return DatabaseErrorHandler.handleDatabaseException(e);
                    }
                }

                // TODO: Implement version/ETag check for optimistic locking (412)

                // Update
                Product updated;
                try {
                    updated = productRepo.save(product);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (updated == null) {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to update product");
                }

                return Json.ok(updated);

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in updateProduct: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

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
