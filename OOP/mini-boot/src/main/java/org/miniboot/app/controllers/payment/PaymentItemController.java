package org.miniboot.app.controllers.payment;

import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.Payment.PaymentItem;
import org.miniboot.app.domain.repo.Payment.PaymentItemRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PaymentItemController {
    private final PaymentItemRepository repo;

    public PaymentItemController(PaymentItemRepository repo) {
        this.repo = repo;
    }

    public static void mount(Router router, PaymentItemController c) {
        router.get("/payment-items", c.getItems());
        router.post("/payment-items", c.createItems());      // create 1 hoặc nhiều
        router.put("/payment-items", c.updateItem());        // update 1
        router.put("/payment-items/replace", c.replaceAll()); // thay toàn bộ items cho 1 payment
        router.delete("/payment-items", c.deleteItems());    // delete by id hoặc by paymentId
    }

    /**
     * GET /payment-items
     * ?paymentId=...  -> list theo payment
     * ?id=...         -> một item theo id
     * (thiếu query)   -> 400 (cần ít nhất một filter)
     */
    public Function<HttpRequest, HttpResponse> getItems() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;

            Optional<Integer> paymentId = ExtractHelper.extractInt(q, "paymentId");
            if (paymentId.isPresent()) {
                return Json.ok(repo.findByPaymentId(paymentId.get()));
            }

            Optional<Integer> id = ExtractHelper.extractInt(q, "id");
            if (id.isPresent()) {
                return repo.findById(id.get())
                        .map(Json::ok)
                        .orElse(HttpResponse.of(404, AppConfig.JSON_UTF_8_TYPE,
                                Json.stringify(Map.of("error", "PaymentItem not found")).getBytes(StandardCharsets.UTF_8)));
            }

            return HttpResponse.of(400, AppConfig.JSON_UTF_8_TYPE,
                    Json.stringify(Map.of("error", "Missing query: paymentId or id")).getBytes(StandardCharsets.UTF_8));
        };
    }

    /**
     * POST /payment-items
     * Body 1: PaymentItem (JSON) -> tạo một dòng
     * Body 2: { "paymentId": 123, "items": [ PaymentItem, ... ] } -> tạo nhiều dòng (batch)
     */
    public Function<HttpRequest, HttpResponse> createItems() {
        // DTO cho batch
        record SaveAllBody(Integer paymentId, List<PaymentItem> items) {
        }

        return (HttpRequest req) -> {
            try {
                String body = new String(req.body, StandardCharsets.UTF_8).trim();

                if (body.startsWith("{") && body.contains("\"items\"")) {
                    // batch
                    SaveAllBody dto = Json.fromString(body, SaveAllBody.class);
                    if (dto == null || dto.paymentId() == null || dto.paymentId() == 0 || dto.items() == null) {
                        return Json.error(400, "Missing paymentId or items");
                    }
                    List<PaymentItem> saved = repo.saveAll(dto.paymentId(), dto.items());
                    return Json.created(saved);
                } else {
                    // single
                    PaymentItem item = Json.fromBytes(req.body, PaymentItem.class);
                    if (item == null) return Json.error(400, "Invalid body");
                    PaymentItem saved = repo.save(item);
                    if (saved == null) return Json.error(500, "Insert failed");
                    return Json.created(saved);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Json.error(400, AppConfig.RESPONSE_400);
            }
        };
    }

    /**
     * PUT /payment-items
     * Body: PaymentItem (JSON) -> phải có id
     */
    public Function<HttpRequest, HttpResponse> updateItem() {
        return (HttpRequest req) -> {
            try {
                PaymentItem item = Json.fromBytes(req.body, PaymentItem.class);
                if (item == null || item.getId() == null || item.getId() == 0) {
                    return Json.error(400, "Missing item id");
                }
                // tồn tại không?
                if (repo.findById(item.getId()).isEmpty()) {
                    return Json.error(404, "PaymentItem not found");
                }
                PaymentItem updated = repo.save(item);
                if (updated == null) return Json.error(500, "Update failed");
                return Json.ok(updated);
            } catch (Exception e) {
                e.printStackTrace();
                return Json.error(400, AppConfig.RESPONSE_400);
            }
        };
    }

    /**
     * PUT /payment-items/replace
     * Body: { "paymentId": 123, "items": [ PaymentItem... ] }
     * Xoá toàn bộ items cũ của payment rồi insert list mới (transaction)
     */
    public Function<HttpRequest, HttpResponse> replaceAll() {
        record ReplaceBody(Integer paymentId, List<PaymentItem> items) {
        }

        return (HttpRequest req) -> {
            try {
                ReplaceBody dto = Json.fromBytes(req.body, ReplaceBody.class);
                if (dto == null || dto.paymentId() == null || dto.paymentId() == 0) {
                    return Json.error(400, "Missing paymentId");
                }
                List<PaymentItem> result = repo.replaceAllForPayment(dto.paymentId(), dto.items() != null ? dto.items() : List.of());
                return Json.ok(result);
            } catch (Exception e) {
                e.printStackTrace();
                return Json.error(400, AppConfig.RESPONSE_400);
            }
        };
    }

    /**
     * DELETE /payment-items
     * ?id=...         -> xoá 1 item
     * ?paymentId=...  -> xoá tất cả items của payment
     */
    public Function<HttpRequest, HttpResponse> deleteItems() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;

            Optional<Integer> id = ExtractHelper.extractInt(q, "id");
            if (id.isPresent()) {
                boolean ok = repo.deleteById(id.get());
                return ok ? Json.ok(Map.of("deleted", 1)) : Json.error(404, "PaymentItem not found");
            }

            Optional<Integer> paymentId = ExtractHelper.extractInt(q, "paymentId");
            if (paymentId.isPresent()) {
                int n = repo.deleteByPaymentId(paymentId.get());
                return Json.ok(Map.of("deleted", n));
            }

            return Json.error(400, "Missing id or paymentId");
        };
    }
}
