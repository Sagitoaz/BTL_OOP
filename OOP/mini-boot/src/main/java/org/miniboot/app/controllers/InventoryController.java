package org.miniboot.app.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miniboot.app.Service.Inventory.InventoryService;
import org.miniboot.app.Service.Inventory.StockMovementService;
import org.miniboot.app.Service.mappers.InventoryMapper;
import org.miniboot.app.controllers.dto.inventory.InitialStockLineDTO;
import org.miniboot.app.controllers.dto.inventory.InventoryCreateDTO;
import org.miniboot.app.controllers.dto.inventory.InventoryDTO;
import org.miniboot.app.controllers.dto.inventory.InventoryUpdateDTO;
import org.miniboot.app.domain.models.Inventory.Inventory;

public class InventoryController {

     private final InventoryService inventoryService;
     private final StockMovementService movementService;

     public InventoryController(InventoryService inventoryService, StockMovementService movementService) {
          this.inventoryService = inventoryService;
          this.movementService = movementService;
     }

     // GET /api/inventory
     public Map<String, Object> list(String q, String category, Boolean lowStock, Integer page, Integer size,
               String sort) {
          int p = page == null ? 0 : Math.max(0, page);
          int s = size == null ? 20 : Math.max(1, Math.min(200, size));

          var result = inventoryService.search(q, category, lowStock != null && lowStock, p, s, sort);
          List<InventoryDTO> dtos = new ArrayList<>();
          for (Inventory inv : result.items) {
               dtos.add(InventoryMapper.toDTO(inv));
          }

          Map<String, Object> rsp = new HashMap<>();
          rsp.put("content", dtos);
          rsp.put("page", result.page);
          rsp.put("size", result.size);
          rsp.put("totalElements", result.totalElements);
          rsp.put("totalPages", result.totalPages);
          return rsp;
     }

     // GET /api/inventory/{id}
     public InventoryDTO getById(int id) {
          Inventory i = inventoryService.findById(id)
                    .orElseThrow(() -> notFound("Inventory not found: " + id));
          return InventoryMapper.toDTO(i);
     }

     // POST /api/inventory
     public InventoryDTO create(InventoryCreateDTO req) {
          validateCreate(req);
          Inventory i = new Inventory();
          InventoryMapper.applyCreate(i, req);
          i.setCreatedAt(LocalDateTime.now());
          i.setCreatedAt(i.getCreatedAt());
          Inventory saved = inventoryService.create(i);
          return InventoryMapper.toDTO(saved);
     }

     // PUT /api/inventory/{id}
     public InventoryDTO update(int id, InventoryUpdateDTO req) {
          Inventory i = inventoryService.findById(id)
                    .orElseThrow(() -> notFound("Inventory not found: " + id));
          InventoryMapper.applyUpdate(i, req);
          i.setLastUpdated(LocalDate.now());
          Inventory saved = inventoryService.update(i);
          return InventoryMapper.toDTO(saved);
     }

     // DELETE /api/inventory/{id}
     public Map<String, Object> delete(int id) {
          boolean ok = inventoryService.deleteById(id);
          if (!ok)
               throw notFound("Inventory not found: " + id);
          return Map.of("deleted", true, "id", id);
     }

     // GET /api/inventory/{id}/movements
     public List<Map<String, Object>> listMovements(int id) {
          ensureExists(id);
          // Note: StockMovementService API is different, return simplified result for now
          List<Map<String, Object>> out = new ArrayList<>();
          out.add(Map.of(
                    "id", 1,
                    "type", "OPENING",
                    "qty", 0,
                    "note", "No movements yet"));
          return out;
     }

     // POST /api/inventory/{id}/initial-stock
     public Map<String, Object> createInitialStock(int id, InitialStockLineDTO req, int userId) {
          Inventory i = inventoryService.findById(id)
                    .orElseThrow(() -> notFound("Inventory not found: " + id));
          if (req == null || req.qty <= 0)
               throw badRequest("qty must be > 0");
          // Update inventory quantity directly for now
          // In a real implementation, this would create a stock movement record
          i.setQuantity(req.qty);
          i.setLastUpdated(LocalDate.now());

          try {
               inventoryService.update(i);
          } catch (Exception e) {
               throw new RuntimeException("Failed to update inventory: " + e.getMessage());
          }

          return Map.of("created", true, "productId", i.getId());
     }

     private void ensureExists(int id) {
          if (inventoryService.findById(id).isEmpty())
               throw notFound("Inventory not found: " + id);
     }

     private static RuntimeException notFound(String m) {
          return new RuntimeException("404: " + m);
     }

     private static RuntimeException badRequest(String m) {
          return new RuntimeException("400: " + m);
     }

     private static void validateCreate(InventoryCreateDTO req) {
          if (req == null)
               throw badRequest("Body is required");
          if (req.sku == null || req.sku.isBlank())
               throw badRequest("sku is required");
          if (req.name == null || req.name.isBlank())
               throw badRequest("name is required");
     }

     // Mount inventory routes to router
     public static void mount(org.miniboot.app.router.Router router) {
          InventoryController controller = new InventoryController(
                    new InventoryService(),
                    new StockMovementService());

          // GET /api/inventory - List inventories with pagination and filtering
          router.get("/api/inventory", req -> {
               try {
                    String q = req.firstQueryValue("q");
                    String category = req.firstQueryValue("category");
                    String lowStockStr = req.firstQueryValue("lowStock");
                    Boolean lowStock = lowStockStr != null ? Boolean.parseBoolean(lowStockStr) : null;
                    String pageStr = req.firstQueryValue("page");
                    Integer page = pageStr != null ? Integer.parseInt(pageStr) : null;
                    String sizeStr = req.firstQueryValue("size");
                    Integer size = sizeStr != null ? Integer.parseInt(sizeStr) : null;
                    String sort = req.firstQueryValue("sort");

                    Map<String, Object> result = controller.list(q, category, lowStock, page, size, sort);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // GET /api/inventory/{id} - Get inventory by ID
          router.get("/api/inventory/{id}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/inventory/{id}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    InventoryDTO result = controller.getById(id);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("404:")) {
                         return org.miniboot.app.util.Json.error(404, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // POST /api/inventory - Create new inventory
          router.post("/api/inventory", req -> {
               try {
                    InventoryCreateDTO dto = org.miniboot.app.util.Json.fromString(req.bodyText(),
                              InventoryCreateDTO.class);
                    InventoryDTO result = controller.create(dto);
                    return org.miniboot.app.util.Response.created("/api/inventory/" + result.id, result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("400:")) {
                         return org.miniboot.app.util.Json.error(400, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(400, "Invalid JSON: " + e.getMessage());
               }
          });

          // PUT /api/inventory/{id} - Update inventory
          router.put("/api/inventory/{id}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/inventory/{id}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    InventoryUpdateDTO dto = org.miniboot.app.util.Json.fromString(req.bodyText(),
                              InventoryUpdateDTO.class);
                    InventoryDTO result = controller.update(id, dto);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("404:")) {
                         return org.miniboot.app.util.Json.error(404, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(400, "Invalid JSON: " + e.getMessage());
               }
          });

          // DELETE /api/inventory/{id} - Delete inventory
          router.delete("/api/inventory/{id}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/inventory/{id}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    Map<String, Object> result = controller.delete(id);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("404:")) {
                         return org.miniboot.app.util.Json.error(404, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // GET /api/inventory/{id}/movements - Get stock movements for inventory
          router.get("/api/inventory/{id}/movements", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/inventory/{id}/movements");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    List<Map<String, Object>> result = controller.listMovements(id);
                    return org.miniboot.app.util.Json.ok(Map.of("movements", result));
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // POST /api/inventory/{id}/initial-stock - Record initial stock
          router.post("/api/inventory/{id}/initial-stock", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/inventory/{id}/initial-stock");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int id = Integer.parseInt(pathParams.get("id"));

                    InitialStockLineDTO dto = org.miniboot.app.util.Json.fromString(req.bodyText(),
                              InitialStockLineDTO.class);
                    Map<String, Object> result = controller.createInitialStock(id, dto, 1); // userId = 1 for now
                    return org.miniboot.app.util.Json.ok(result);
               } catch (RuntimeException e) {
                    if (e.getMessage().startsWith("404:")) {
                         return org.miniboot.app.util.Json.error(404, e.getMessage().substring(5));
                    }
                    if (e.getMessage().startsWith("400:")) {
                         return org.miniboot.app.util.Json.error(400, e.getMessage().substring(5));
                    }
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(400, "Invalid JSON: " + e.getMessage());
               }
          });

          // API Documentation endpoint
          router.get("/api/inventory/docs", req -> {
               return org.miniboot.app.util.Json.ok(Map.of(
                         "title", "Inventory Management API",
                         "version", "1.0.0",
                         "description", "Complete REST API for Inventory Management System",
                         "endpoints", Map.of(
                                   "GET /api/inventory", "List inventories with pagination and filtering",
                                   "GET /api/inventory/{id}", "Get inventory by ID",
                                   "POST /api/inventory", "Create new inventory item",
                                   "PUT /api/inventory/{id}", "Update inventory item",
                                   "DELETE /api/inventory/{id}", "Delete inventory item",
                                   "GET /api/inventory/{id}/movements", "Get stock movements for inventory",
                                   "POST /api/inventory/{id}/initial-stock", "Record initial stock for inventory"),
                         "examples", Map.of(
                                   "create", Map.of(
                                             "url", "POST /api/inventory",
                                             "body",
                                             Map.of("sku", "PROD001", "name", "Product 1", "category", "electronics")),
                                   "update", Map.of(
                                             "url", "PUT /api/inventory/1",
                                             "body", Map.of("name", "Updated Product", "category", "updated")),
                                   "initialStock", Map.of(
                                             "url", "POST /api/inventory/1/initial-stock",
                                             "body", Map.of("qty", 100, "note", "Initial inventory")))));
          });
     }

}
