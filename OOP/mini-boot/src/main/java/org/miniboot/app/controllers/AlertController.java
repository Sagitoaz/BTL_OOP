package org.miniboot.app.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miniboot.app.Service.Inventory.InventoryService;

public class AlertController {

     private final InventoryService inventoryService;

     public AlertController(InventoryService inventoryService) {
          this.inventoryService = inventoryService;
     }

     // GET /api/alerts - Lấy active alerts
     public Map<String, Object> getActiveAlerts() {
          List<Map<String, Object>> alerts = new ArrayList<>();

          // Sample alerts - in real implementation would check inventory levels
          alerts.add(Map.of(
                    "id", 1,
                    "productId", 1,
                    "productName", "Paracetamol 500mg",
                    "alertType", "LOW_STOCK",
                    "priority", "HIGH",
                    "currentStock", 5,
                    "reorderLevel", 20,
                    "message", "Stock level is critically low (5 units remaining, reorder level: 20)",
                    "createdAt", LocalDateTime.now().minusHours(2).toString(),
                    "active", true));

          alerts.add(Map.of(
                    "id", 2,
                    "productId", 3,
                    "productName", "Aspirin 100mg",
                    "alertType", "LOW_STOCK",
                    "priority", "MEDIUM",
                    "currentStock", 15,
                    "reorderLevel", 30,
                    "message", "Stock level is below reorder point (15 units remaining, reorder level: 30)",
                    "createdAt", LocalDateTime.now().minusHours(1).toString(),
                    "active", true));

          return Map.of(
                    "alerts", alerts,
                    "totalAlerts", alerts.size(),
                    "highPriority", alerts.stream().mapToLong(a -> "HIGH".equals(a.get("priority")) ? 1 : 0).sum(),
                    "mediumPriority", alerts.stream().mapToLong(a -> "MEDIUM".equals(a.get("priority")) ? 1 : 0).sum(),
                    "lowPriority", alerts.stream().mapToLong(a -> "LOW".equals(a.get("priority")) ? 1 : 0).sum());
     }

     // POST /api/alerts/check - Manual trigger check alerts
     public Map<String, Object> checkAlerts() {
          // In real implementation, would scan all inventory items and check levels
          // For now, simulate the check process

          int checkedItems = 100; // Simulated
          int newAlerts = 2; // Simulated

          return Map.of(
                    "success", true,
                    "message", "Alert check completed",
                    "checkedItems", checkedItems,
                    "newAlertsGenerated", newAlerts,
                    "checkTime", LocalDateTime.now().toString());
     }

     // PUT /api/alerts/{id}/resolve - Mark alert as resolved
     public Map<String, Object> resolveAlert(int alertId) {
          // In real implementation, would update alert status in database

          return Map.of(
                    "success", true,
                    "alertId", alertId,
                    "message", "Alert marked as resolved",
                    "resolvedAt", LocalDateTime.now().toString());
     }

     // GET /api/alerts/stats - Alert statistics
     public Map<String, Object> getAlertStats() {
          return Map.of(
                    "totalActiveAlerts", 5,
                    "criticalAlerts", 2,
                    "warningAlerts", 3,
                    "infoAlerts", 0,
                    "resolvedToday", 3,
                    "averageResolutionTime", "2.5 hours",
                    "topAffectedCategories", List.of("Medications", "Medical Supplies"),
                    "alertTrends", Map.of(
                              "today", 5,
                              "yesterday", 7,
                              "thisWeek", 23,
                              "lastWeek", 18));
     }

     // GET /api/alerts/priority/{priority} - Filter by priority
     public Map<String, Object> getAlertsByPriority(String priority) {
          List<Map<String, Object>> alerts = new ArrayList<>();

          if ("HIGH".equalsIgnoreCase(priority)) {
               alerts.add(Map.of(
                         "id", 1,
                         "productId", 1,
                         "productName", "Critical Medicine",
                         "alertType", "LOW_STOCK",
                         "priority", "HIGH",
                         "message", "Critically low stock - immediate attention required",
                         "createdAt", LocalDateTime.now().minusHours(1).toString()));
          } else if ("MEDIUM".equalsIgnoreCase(priority)) {
               alerts.add(Map.of(
                         "id", 2,
                         "productId", 2,
                         "productName", "Standard Medicine",
                         "alertType", "LOW_STOCK",
                         "priority", "MEDIUM",
                         "message", "Stock below reorder level",
                         "createdAt", LocalDateTime.now().minusHours(3).toString()));
          }

          return Map.of(
                    "alerts", alerts,
                    "priority", priority.toUpperCase(),
                    "count", alerts.size());
     }

     private static RuntimeException badRequest(String message) {
          return new RuntimeException("400: " + message);
     }

     private static RuntimeException notFound(String message) {
          return new RuntimeException("404: " + message);
     }

     // Mount alert routes to router
     public static void mount(org.miniboot.app.router.Router router) {
          AlertController controller = new AlertController(new InventoryService());

          // GET /api/alerts - Get active alerts
          router.get("/api/alerts", req -> {
               try {
                    Map<String, Object> result = controller.getActiveAlerts();
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // POST /api/alerts/check - Manual trigger alert check
          router.post("/api/alerts/check", req -> {
               try {
                    Map<String, Object> result = controller.checkAlerts();
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // PUT /api/alerts/{id}/resolve - Resolve alert
          router.put("/api/alerts/{id}/resolve", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/alerts/{id}/resolve");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    int alertId = Integer.parseInt(pathParams.get("id"));

                    Map<String, Object> result = controller.resolveAlert(alertId);
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

          // GET /api/alerts/stats - Alert statistics
          router.get("/api/alerts/stats", req -> {
               try {
                    Map<String, Object> result = controller.getAlertStats();
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // GET /api/alerts/priority/{priority} - Filter by priority
          router.get("/api/alerts/priority/{priority}", req -> {
               try {
                    org.miniboot.app.router.PathPattern pattern = new org.miniboot.app.router.PathPattern(
                              "/api/alerts/priority/{priority}");
                    Map<String, String> pathParams = pattern.extract(req.path);
                    String priority = pathParams.get("priority");

                    Map<String, Object> result = controller.getAlertsByPriority(priority);
                    return org.miniboot.app.util.Json.ok(result);
               } catch (Exception e) {
                    return org.miniboot.app.util.Json.error(500, "Error: " + e.getMessage());
               }
          });

          // GET /api/alerts/docs - API Documentation for alerts
          router.get("/api/alerts/docs", req -> {
               return org.miniboot.app.util.Json.ok(Map.of(
                         "title", "Alert Management API",
                         "version", "1.0.0",
                         "description", "Alert system for inventory low stock notifications",
                         "endpoints", Map.of(
                                   "GET /api/alerts", "Get all active alerts",
                                   "POST /api/alerts/check", "Manually trigger alert check",
                                   "PUT /api/alerts/{id}/resolve", "Mark alert as resolved",
                                   "GET /api/alerts/stats", "Get alert statistics",
                                   "GET /api/alerts/priority/{priority}",
                                   "Filter alerts by priority (HIGH, MEDIUM, LOW)"),
                         "alertTypes", List.of("LOW_STOCK", "OUT_OF_STOCK", "EXPIRED"),
                         "priorities", List.of("HIGH", "MEDIUM", "LOW")));
          });
     }
}