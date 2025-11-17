package org.miniboot.app.controllers.payment;

import com.google.gson.Gson;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.repo.Payment.PaymentStatusLogRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.GsonProvider;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PaymentStatusLogController {
    private final PaymentStatusLogRepository statusRepo;

    public PaymentStatusLogController(PaymentStatusLogRepository statusRepo) {
        this.statusRepo = statusRepo;
    }

    public static void mount(Router router, PaymentStatusLogController c) {
        router.get("/payment-status", c.getCurrentStatus());
        router.post("/payment-status", c.setStatus());
    }

    // GET /payment-status?paymentId=...
    public Function<HttpRequest, HttpResponse> getCurrentStatus() {
        return (HttpRequest req) -> {
            Optional<Integer> idOpt = ExtractHelper.extractInt(req.query, "paymentId");
            if (idOpt.isEmpty()) {
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        "Missing paymentId".getBytes(StandardCharsets.UTF_8));
            }
            var status = statusRepo.getCurrentPaymentStatus(idOpt.get());
            if (status == null) {
                return HttpResponse.of(404, "text/plain; charset=utf-8",
                        "No status found".getBytes(StandardCharsets.UTF_8));
            }
            return Json.ok(Map.of("paymentId", idOpt.get(), "status", status.name()));
        };
    }

    // POST /payment-status
    // Body (JSON): { "paymentId": 123, "status": "PENDING" }
    public Function<HttpRequest, HttpResponse> setStatus() {
        record SetStatusBody(Integer paymentId, String status) {
        }

        return (HttpRequest req) -> {
            try {
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);

                SetStatusBody body = gson.fromJson(jsonBody, SetStatusBody.class);
                if (body == null || body.paymentId() == null || body.status() == null) {
                    return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                            HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
                }
                PaymentStatus target;
                try {
                    target = PaymentStatus.valueOf(body.status());
                } catch (IllegalArgumentException ex) {
                    return HttpResponse.of(400, "text/plain; charset=utf-8",
                            "Invalid status".getBytes(StandardCharsets.UTF_8));
                }

                var result = statusRepo.setCurrentPaymentStatus(body.paymentId(), target);
                if (result == null) {
                    return HttpResponse.of(409, "text/plain; charset=utf-8",
                            "Cannot change status (maybe terminal)".getBytes(StandardCharsets.UTF_8));
                }
                return Json.ok(Map.of("paymentId", body.paymentId(), "status", result.name()));

            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                        HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
