package org.miniboot.app.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miniboot.app.Service.Inventory.StockMovementService;

public class StockMovementController {

     private final StockMovementService stockMovementService;

     public StockMovementController(StockMovementService stockMovementService) {
          this.stockMovementService = stockMovementService;
     }

     // GET /api/stock-movements - Lịch sử giao dịch với pagination
     public Map<String, Object> list(Integer productId, String moveType, Integer page, Integer size) {
          int p = page == null ? 0 : Math.max(0, page);
          int s = size == null ? 20 : Math.max(1, Math.min(200, size));

          // Simplified implementation - in real app would use proper service methods
          List<Map<String, Object>> movements = new ArrayList<>();

          // Sample data for now
          movements.add(Map.of(
                    "id", 1,
                    "productId", productId != null ? productId : 1,
                    "moveType", "PURCHASE",
                    "qty", 100,
                    "batchNo", "BATCH001",
                    "movedAt", LocalDateTime.now().toString(),
                    "movedBy", 1,
                    "note", "Initial stock purchase"));

          return Map.of(
                    "content", movements,
                    "page", p,
                    "size", s,
                    "totalElements", movements.size(),
                    "totalPages", 1);
     }

     // GET /api/stock-movements/{id} - Chi tiết giao dịch
     public Map<String, Object> getById(int id) {
          return Map.of(
                    "id", id,
                    "productId", 1,
                    "moveType", "PURCHASE",
                    "qty", 100,
                    "batchNo", "BATCH001",
                    "movedAt", LocalDateTime.now().toString(),
                    "movedBy", 1,
                    "note", "Stock movement details");
     }

     // POST /api/stock-movements - Tạo giao dịch mới
     public Map<String, Object> create(Map<String, Object> movementData) {
          validateMovement(movementData);

          // In real implementation, would call stockMovementService.recordPurchase/Sale
          int productId = (Integer) movementData.get("productId");
          int qty = (Integer) movementData.get("qty");
          String moveType = (String) movementData.get("moveType");
          String note = (String) movementData.getOrDefault("note", "");

          // Create movement record
          Map<String, Object> createdMovement = Map.of(
                    "id", System.currentTimeMillis() % 1000, // Simple ID generation
                    "productId", productId,
                    "moveType", moveType,
                    "qty", qty,
                    "movedAt", LocalDateTime.now().toString(),
                    "movedBy", movementData.getOrDefault("movedBy", 1),
                    "note", note,
                    "status", "SUCCESS");

          return createdMovement;
     }

     // GET /api/stock-movements/product/{productId} - Lịch sử theo sản phẩm
     public Map<String, Object> getByProductId(int productId, Integer page, Integer size) {
          int p = page == null ? 0 : Math.max(0, page);
          int s = size == null ? 20 : Math.max(1, Math.min(200, size));

          List<Map<String, Object>> movements = new ArrayList<>();

          // Sample movements for the product
          movements.add(Map.of(
                    "id", 1,
                    "productId", productId,
                    "moveType", "OPENING",
                    "qty", 100,
                    "movedAt", LocalDateTime.now().minusDays(5).toString(),
                    "note", "Initial stock"));

          movements.add(Map.of(
                    "id", 2,
                    "productId", productId,
                    "moveType", "SALE",
                    "qty", -20,
                    "movedAt", LocalDateTime.now().minusDays(2).toString(),
                    "note", "Sold to customer"));

          return Map.of(
                    "content", movements,
                    "page", p,
                    "size", s,
                    "totalElements", movements.size(),
                    "totalPages", 1,
                    "productId", productId);
     }

     // POST /api/stock-movements/bulk - Tạo nhiều giao dịch
     @SuppressWarnings("unchecked")
     public Map<String, Object> createBulk(List<Map<String, Object>> movements) {
          if (movements == null || movements.isEmpty()) {
               throw badRequest("Movements list cannot be empty");
          }

          List<Map<String, Object>> createdMovements = new ArrayList<>();

          for (Map<String, Object> movement : movements) {
               try {
                    Map<String, Object> created = create(movement);
                    createdMovements.add(created);
               } catch (Exception e) {
                    // In real implementation, would rollback previous movements
                    throw new RuntimeException("Failed to process movement: " + e.getMessage());
               }
          }

          return Map.of(
                    "success", true,
                    "processed", createdMovements.size(),
                    "movements", createdMovements);
     }

     // Validation helper
     private void validateMovement(Map<String, Object> data) {
          if (data == null) {
               throw badRequest("Movement data is required");
          }

          if (!data.containsKey("productId") || data.get("productId") == null) {
               throw badRequest("productId is required");
          }

          if (!data.containsKey("qty") || data.get("qty") == null) {
               throw badRequest("qty is required");
          }

          if (!data.containsKey("moveType") || data.get("moveType") == null) {
               throw badRequest("moveType is required");
          }

          String moveType = (String) data.get("moveType");
          if (!moveType.matches("PURCHASE|SALE|ADJUSTMENT|OPENING|CLOSING")) {
               throw badRequest("Invalid moveType. Must be one of: PURCHASE, SALE, ADJUSTMENT, OPENING, CLOSING");
          }
     }

     private static RuntimeException badRequest(String message) {
          return new RuntimeException("400: " + message);
     }

     private static RuntimeException notFound(String message) {
          return new RuntimeException("404: " + message);
     }

     // Mount stock movement routes to router
     public static void mount(org.miniboot.app.router.Router router) {
          StockMovementController controller = new StockMovementController(new StockMovementService());

          // GET /api/stock-movements - List movements with filtering
          router.get("/api/stock-movements", req -> {
               try {
                    String productIdStr = req.firstQueryValue("productId");
                    Integer productId = productIdStr != null ? Integer.parseInt(productIdStr) : null;
                    String moveType = req.firstQueryValue("moveType");
                    String pageStr = req.firstQueryValue("page");
                    Integer page = pageStr != null ? Integer.parseInt(pageStr) : null;
                    String sizeStr = req.firstQueryValue("size");
                    Integer size = sizeStr != null ? Integer.parseInt(sizeStr) : null;

                    Map<String, Object> result = controller.list(productId, moveType, page, size);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // GET /api/stock-movements/{id} - Get movement by ID
          router.get("/api/stock-movements/{id}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/stock-movements/{id}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    Map<String, Object> result = controller.getById(id);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // POST /api/stock-movements - Create new movement
          router.post("/api/stock-movements", req -> {
               try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> movementData = org.miniboot.app.util.Json.fromString(req.bodyText(), Map.class);
                    Map<String, Object> result = controller.create(movementData);
                    return org.miniboot.app.util.Response.created("/api/stock-movements/" + result.get("id"), result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("400:")) {
                         return org.miniboot.app.util.Json.error(400, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(400, "Invalid JSON: " + e.getMessage());
               }
          });

          // GET /api/stock-movements/product/{productId} - Get movements by product
          router.get("/api/stock-movements/product/{productId}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/stock-movements/product/{productId}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int productId = Integer.parseInt(pathParams.get("productId"));

                    String pageStr = req.firstQueryValue("page");
                    Integer page = pageStr != null ? Integer.parseInt(pageStr) : null;
                    String sizeStr = req.firstQueryValue("size");
                    Integer size = sizeStr != null ? Integer.parseInt(sizeStr) : null;

                    Map<String, Object> result = controller.getByProductId(productId, page, size);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // POST /api/stock-movements/bulk - Create multiple movements
          router.post("/api/stock-movements/bulk", req -> {
               try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> movements = org.miniboot.app.util.Json.fromString(req.bodyText(),
                              List.class);
                    Map<String, Object> result = controller.createBulk(movements);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("400:")) {
                         return org.miniboot.app.util.Json.error(400, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(400, "Invalid JSON: " + e.getMessage());
               }
          });
     }
}