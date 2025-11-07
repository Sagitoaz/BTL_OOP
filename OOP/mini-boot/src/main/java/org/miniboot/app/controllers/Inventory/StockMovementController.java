package org.miniboot.app.controllers.Inventory;

import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.miniboot.app.domain.repo.Inventory.StockMovementRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StockMovementController {
    private StockMovementRepository stockMoveRepo;

    public StockMovementController(StockMovementRepository sm) {
        this.stockMoveRepo = sm;
    }

    public static void mount(Router router, StockMovementController sc) {
        router.get("/stock_movements", sc.getMovements());
        router.get("/stock_movements/filter", sc.filterMovements());
        router.get("/stock_movements/stats", sc.getStats());
        router.post("/stock_movements", sc.createMovement());
        router.put("/stock_movements", sc.updateMovement());
        router.delete("/stock_movements", sc.deleteMovement());
        router.post("/stock_movements/batch", sc.createMovementsBatch());
    }
    
    // POST /stock_movements/batch
    public Function<HttpRequest, HttpResponse> createMovementsBatch() {
        return (HttpRequest req) -> {
            try {
                System.out.println("📥 POST /stock_movements/batch - Parsing list of stock movements...");
                List<StockMovement> movements = Json.fromBytesToList(req.body, StockMovement.class);

                if (movements == null || movements.isEmpty()) {
                    return HttpResponse.of(400, "text/plain",
                            "Danh sách stock movement rỗng".getBytes(StandardCharsets.UTF_8));
                }

                System.out.println("📦 Received " + movements.size() + " stock movements.");
                List<StockMovement> savedList = stockMoveRepo.saveAll(movements);

                if (savedList == null || savedList.isEmpty()) {
                    return HttpResponse.of(500, "text/plain",
                            "Không thể lưu danh sách stock movement".getBytes(StandardCharsets.UTF_8));
                }

                System.out.println("✅ Saved " + savedList.size() + " stock movements successfully!");
                return Json.created(savedList);

            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain",
                        ("Lỗi xử lý danh sách stock movements: " + e.getMessage())
                                .getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    public Function<HttpRequest, HttpResponse> getMovements() {
        return (HttpRequest req) -> {
            try {
                Map<String, List<String>> q = req.query;
                Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                if (idOpt.isPresent()) {
                    return stockMoveRepo.findById(idOpt.get())
                            .map(Json::ok)
                            .orElse(HttpResponse.of(404, "text/plain",
                                    "Stock movement not found".getBytes(StandardCharsets.UTF_8)));
                }

                System.out.println("🔄 Fetching all stock movements...");
                List<StockMovement> movements = stockMoveRepo.findAll();
                System.out.println("📦 Got " + movements.size() + " stock movements from repo");

                System.out.println("🔄 Converting to JSON...");
                HttpResponse response = Json.ok(movements);
                System.out.println("✅ JSON conversion successful");

                return response;
            } catch (Exception e) {
                System.err.println("❌ ERROR in getMovements():");
                System.err.println("   Type: " + e.getClass().getName());
                System.err.println("   Message: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        };
    }

    // POST /stock_movements
    public Function<HttpRequest, HttpResponse> createMovement() {
        return (HttpRequest req) -> {
            try {
                StockMovement movement = Json.fromBytes(req.body, StockMovement.class);
                StockMovement saved = stockMoveRepo.save(movement);

                if (saved == null) {
                    return HttpResponse.of(500, "text/plain",
                            "Failed to save stock movement".getBytes(StandardCharsets.UTF_8));
                }

                return Json.created(saved);
            } catch (Exception e) {
                return HttpResponse.of(400, "text/plain",
                        ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // PUT /stock_movements
    public Function<HttpRequest, HttpResponse> updateMovement() {
        return (HttpRequest req) -> {
            try {
                System.out.println("📥 PUT /stock_movements - Parsing request body...");
                System.out.println("   Body size: " + req.body.length + " bytes");

                StockMovement movement = Json.fromBytes(req.body, StockMovement.class);

                System.out.println("✅ Movement parsed successfully:");
                System.out.println("   ID: " + movement.getId());
                System.out.println("   ProductID: " + movement.getProductId());
                System.out.println("   Qty: " + movement.getQty());
                System.out.println("   MoveType: " + movement.getMoveType());

                if (movement.getId() <= 0) {
                    System.err.println("❌ ERROR: Missing or invalid movement ID: " + movement.getId());
                    return HttpResponse.of(400, "text/plain",
                            "Missing movement ID".getBytes(StandardCharsets.UTF_8));
                }

                System.out.println("🔄 Saving stock movement to database...");
                StockMovement updated = stockMoveRepo.save(movement);

                if (updated == null) {
                    System.err.println("❌ ERROR: stockMoveRepo.save() returned null");
                    return HttpResponse.of(500, "text/plain",
                            "Failed to update stock movement".getBytes(StandardCharsets.UTF_8));
                }

                System.out.println("✅ Stock movement updated successfully: ID=" + updated.getId());
                return Json.ok(updated);
            } catch (Exception e) {
                System.err.println("❌ ERROR in updateMovement():");
                System.err.println("   Type: " + e.getClass().getName());
                System.err.println("   Message: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain",
                        ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // DELETE /stock_movements?id=123
    public Function<HttpRequest, HttpResponse> deleteMovement() {
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
                        "Stock movement deleted".getBytes(StandardCharsets.UTF_8));
            } else {
                return HttpResponse.of(404, "text/plain",
                        "Stock movement not found".getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    public Function<HttpRequest, HttpResponse> filterMovements() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;

            Optional<Integer> productId = ExtractHelper.extractInt(q, "product_id");
            Optional<String> moveType = ExtractHelper.extractString(q, "move_type");
            Optional<String> fromDate = ExtractHelper.extractString(q, "from");
            Optional<String> toDate = ExtractHelper.extractString(q, "to");

            List<StockMovement> results = stockMoveRepo.findAll();

            // Filter by product_id
            if (productId.isPresent()) {
                final int pid = productId.get();
                results = results.stream()
                        .filter(m -> m.getProductId() == pid)
                        .toList();
            }

            // Filter by move_type
            if (moveType.isPresent()) {
                final String mt = moveType.get().toUpperCase();
                results = results.stream()
                        .filter(m -> m.getMoveType() != null && m.getMoveType().equalsIgnoreCase(mt))
                        .toList();
            }

            // Filter by date range
            if (fromDate.isPresent() || toDate.isPresent()) {
                results = results.stream()
                        .filter(m -> {
                            if (m.getMovedAt() == null)
                                return false;

                            LocalDate movDate = m.getMovedAt().toLocalDate();

                            if (fromDate.isPresent()) {
                                LocalDate from = LocalDate.parse(fromDate.get());
                                if (movDate.isBefore(from))
                                    return false;
                            }

                            if (toDate.isPresent()) {
                                LocalDate to = LocalDate.parse(toDate.get());
                                if (movDate.isAfter(to))
                                    return false;
                            }

                            return true;
                        })
                        .toList();
            }

            System.out.println("✅ Filtered results: " + results.size());
            return Json.ok(results);
        };
    }

    // Thêm method stats
    public Function<HttpRequest, HttpResponse> getStats() {
        return (HttpRequest req) -> {
            List<StockMovement> all = stockMoveRepo.findAll();

            long totalIn = all.stream()
                    .filter(StockMovement::isInMovement)
                    .mapToInt(StockMovement::getQty)
                    .sum();

            long totalOut = all.stream()
                    .filter(StockMovement::isOutMovement)
                    .mapToInt(m -> Math.abs(m.getQty()))
                    .sum();

            Map<String, Object> stats = Map.of(
                    "total", all.size(),
                    "in", totalIn,
                    "out", totalOut);

            return Json.ok(stats);
        };
    }
}
