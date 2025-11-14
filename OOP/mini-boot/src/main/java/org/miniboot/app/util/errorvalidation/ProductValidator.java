package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.domain.repo.Inventory.ProductRepository;
import org.miniboot.app.http.HttpResponse;

import java.util.Optional;

/**
 * Validator for Product business rules and constraints
 * Centralizes product validation logic to avoid code duplication
 */
public class ProductValidator {

    /**
     * Validate product business rules (quantity, price constraints)
     */
    public static HttpResponse validateBusinessRules(Product product) {
        // Check negative quantity
        if (product.getQtyOnHand() < 0) {
            return ValidationUtils.error(422, "VALIDATION_FAILED",
                    "Quantity on hand cannot be negative");
        }

        // Check negative price cost
        if (product.getPriceCost() != null && product.getPriceCost() < 0) {
            return ValidationUtils.error(422, "VALIDATION_FAILED",
                    "Price cost cannot be negative");
        }

        // Check negative price retail
        if (product.getPriceRetail() != null && product.getPriceRetail() < 0) {
            return ValidationUtils.error(422, "VALIDATION_FAILED",
                    "Price retail cannot be negative");
        }

        // Check price retail >= price cost (if both provided)
        if (product.getPriceCost() != null && product.getPriceRetail() != null) {
            if (product.getPriceRetail() < product.getPriceCost()) {
                return ValidationUtils.error(422, "VALIDATION_FAILED",
                        "Price retail must be greater than or equal to price cost");
            }
        }

        return null; // Valid
    }

    /**
     * Check SKU duplicate
     * For updates: excludes the product's own ID
     */
    public static HttpResponse checkSkuDuplicate(
            ProductRepository repository,
            String sku,
            Integer excludeProductId) {

        try {
            Optional<Product> existing = repository.findBySku(sku);
            if (existing.isPresent()) {
                // If excludeProductId is provided, check if it's the same product
                if (excludeProductId != null && existing.get().getId() == excludeProductId) {
                    return null; // Same product, not a duplicate
                }
                return ValidationUtils.error(409, "INVENTORY_CONFLICT",
                        "Product with SKU '" + sku + "' already exists");
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
        return null; // No duplicate
    }

    /**
     * Full validation for product (create scenario)
     */
    public static HttpResponse validateForCreate(
            Product product,
            ProductRepository repository) {

        // Step 1: Business rules
        HttpResponse businessError = validateBusinessRules(product);
        if (businessError != null) return businessError;

        // Step 2: SKU duplicate (no exclusion for create)
        HttpResponse skuError = checkSkuDuplicate(repository, product.getSku(), null);
        if (skuError != null) return skuError;

        return null; // All validations passed
    }

    /**
     * Full validation for product (update scenario)
     * Excludes own ID from duplicate checks and checks existence
     */
    public static HttpResponse validateForUpdate(
            Product product,
            ProductRepository repository) {

        // Step 1: Check if product exists
        HttpResponse existsError = checkExists(repository, product.getId());
        if (existsError != null) return existsError;

        // Step 2: Business rules
        HttpResponse businessError = validateBusinessRules(product);
        if (businessError != null) return businessError;

        // Step 3: SKU duplicate check (exclude own ID if SKU changed)
        try {
            Optional<Product> existing = repository.findById(product.getId());
            if (existing.isPresent()) {
                // Only check duplicate if SKU changed
                if (!product.getSku().equals(existing.get().getSku())) {
                    HttpResponse skuError = checkSkuDuplicate(repository, product.getSku(), product.getId());
                    if (skuError != null) return skuError;
                }
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }

        return null; // All validations passed
    }

    /**
     * Check if product exists in database
     */
    public static HttpResponse checkExists(
            ProductRepository repository,
            int productId) {

        try {
            Optional<Product> existing = repository.findById(productId);
            if (existing.isEmpty()) {
                return ValidationUtils.error(404, "NOT_FOUND",
                        "Product with ID " + productId + " not found");
            }
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
        return null; // Exists
    }
}
