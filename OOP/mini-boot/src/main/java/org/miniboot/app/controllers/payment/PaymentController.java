package org.miniboot.app.controllers.payment;

import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.repo.Payment.PaymentRepository;
import org.miniboot.app.domain.repo.Payment.PaymentStatusLogRepository;
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

public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusLogRepository statusLogRepository;

    public PaymentController(PaymentRepository paymentRepository,
                             PaymentStatusLogRepository statusLogRepository) {
        this.paymentRepository = paymentRepository;
        this.statusLogRepository = statusLogRepository;
    }

    public static void mount(Router router, PaymentController pc) {
        router.get("/payments", pc.getPayments());
        router.post("/payments", pc.createPayment());
        router.put("/payments", pc.updatePayment());
        // có thể thêm route status vào controller riêng (ở dưới mình làm controller riêng)
    }

    /**
     * GET /payments
     * - Không query      -> trả tất cả
     * - ?id=             -> trả theo id (404 nếu không có)
     * - ?code=           -> trả theo mã code (404 nếu không có)
     */
    public Function<HttpRequest, HttpResponse> getPayments() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;

            // Ưu tiên id
            Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");
            if (idOpt.isPresent()) {
                return paymentRepository.getPaymentById(idOpt.get())
                        .map(Json::ok)
                        .orElse(HttpResponse.of(404, "text/plain; charset=utf-8",
                                AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)));
            }

            // Tìm theo code
            Optional<String> codeOpt = ExtractHelper.extractFirst(q, "code");
            if (codeOpt.isPresent()) {
                // Nếu repo có hàm getPaymentByCode, dùng nó. Nếu chưa, fallback lọc tạm.
                try {
                    var method = paymentRepository.getClass().getMethod("getPaymentByCode", String.class);
                    @SuppressWarnings("unchecked")
                    Optional<Payment> found = (Optional<Payment>) method.invoke(paymentRepository, codeOpt.get());
                    return found.map(Json::ok).orElse(HttpResponse.of(404, "text/plain; charset=utf-8",
                            AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)));
                } catch (NoSuchMethodException ignore) {
                    // fallback: lọc từ all (đủ dùng tạm thời, FE vẫn chủ động lọc)
                    return paymentRepository.getPayments().stream()
                            .filter(p -> codeOpt.get().equals(p.getCode()))
                            .findFirst()
                            .map(Json::ok)
                            .orElse(HttpResponse.of(404, "text/plain; charset=utf-8",
                                    AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    e.printStackTrace();
                    return HttpResponse.of(500, "text/plain; charset=utf-8",
                            "Server error".getBytes(StandardCharsets.UTF_8));
                }
            }

            // Trả tất cả
            return Json.ok(paymentRepository.getPayments());
        };
    }

    /**
     * POST /payments
     * Body: Payment (JSON)
     * - Tạo payment mới
     * - Ghi status log ban đầu = UNPAID
     */
    public Function<HttpRequest, HttpResponse> createPayment() {
        return (HttpRequest req) -> {
            try {
                Payment payment = Json.fromBytes(req.body, Payment.class);

                // Insert payment
                Payment saved = paymentRepository.savePayment(payment);
                if (saved == null || saved.getId() == null || saved.getId() == 0) {
                    return HttpResponse.of(500, "text/plain; charset=utf-8",
                            "Cannot create payment".getBytes(StandardCharsets.UTF_8));
                }

                // Ghi status ban đầu = UNPAID
                statusLogRepository.setCurrentPaymentStatus(saved.getId(), PaymentStatus.UNPAID);

                return Json.created(saved);
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    /**
     * PUT /payments
     * Body: Payment (JSON) — phải có id
     */
    public Function<HttpRequest, HttpResponse> updatePayment() {
        return (HttpRequest req) -> {
            try {
                Payment payment = Json.fromBytes(req.body, Payment.class);

                if (payment.getId() == null || payment.getId() == 0) {
                    return HttpResponse.of(400, "text/plain; charset=utf-8",
                            "Missing payment ID".getBytes(StandardCharsets.UTF_8));
                }

                Optional<Payment> existing = paymentRepository.getPaymentById(payment.getId());
                if (existing.isEmpty()) {
                    return HttpResponse.of(404, "text/plain; charset=utf-8",
                            "Payment not found".getBytes(StandardCharsets.UTF_8));
                }

                Payment updated = paymentRepository.savePayment(payment);
                if (updated == null) {
                    return HttpResponse.of(500, "text/plain; charset=utf-8",
                            "Update failed".getBytes(StandardCharsets.UTF_8));
                }
                return Json.ok(updated);

            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
