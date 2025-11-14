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
import org.miniboot.app.util.errorvalidation.ProductValidator;
import org.miniboot.app.util.errorvalidation.RateLimiter;
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

                    // Validate keyword (422)
                   HttpResponse keywordError = ValidationUtils.validateSearchKeyword(sku);
                   if (keywordError != null) return keywordError;

                    // Handle database errors properly
                   Optional<Product> productOpt;
                   try {
                       productOpt = productRepo.findBySku(sku);
                   } catch (Exception e) {
                       return DatabaseErrorHandler.handleDatabaseException(e);
                   }

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
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;

            try {
                // Step 4: Parse JSON
                Product product;
                try {
                    product = Json.fromBytes(req.body, Product.class);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Invalid JSON format: " + e.getMessage());
                }

                // Step 5: Validate required fields
                if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "SKU is required");
                }
                if (product.getName() == null || product.getName().trim().isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "Name is required");
                }

                // Step 6-7: Full product validation (business rules + SKU duplicate)
                HttpResponse productError = ProductValidator.validateForCreate(product, productRepo);
                if (productError != null) return productError;

                // Step 8: Save to database
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

                // Step 9: Return success response
                return Json.created(saved);

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in createProduct: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    public Function<HttpRequest, HttpResponse> updateProduct() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;

            try {
                // Step 4: Parse JSON
                Product product = Json.fromBytes(req.body, Product.class);

                // Step 5: Validate ID
                if (product.getId() <= 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Product ID is required for update");
                }

                // Step 6-8: Full product validation (exists + business rules + SKU duplicate)
                HttpResponse productError = ProductValidator.validateForUpdate(product, productRepo);
                if (productError != null) return productError;

                // Step 9: Update product
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
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-2: Validate JWT and Role
            HttpResponse authError = ValidationUtils.validateJWT(req);
            if (authError != null) return authError;

            HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
            if (roleError != null) return roleError;

            // Step 3: Extract ID from query
            Map<String, List<String>> q = req.query;
            Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

            if (idOpt.isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", "Missing id parameter");
            }

            int productId = idOpt.get();

            try {
                // Step 4: Check product exists
                HttpResponse existsError = ProductValidator.checkExists(productRepo, productId);
                if (existsError != null) return existsError;

                // Step 5: Delete product
                boolean deleted;
                try {
                    deleted = productRepo.deleteById(productId);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (deleted) {
                    return ValidationUtils.error(200, "OK", "Product deleted successfully");
                } else {
                    return ValidationUtils.error(500, "DB_ERROR", "Failed to delete product");
                }

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in deleteProduct: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }
}
