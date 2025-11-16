package org.miniboot.app.controllers.payment;

import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;
import org.miniboot.app.domain.repo.Payment.PaymentRepository;
import org.miniboot.app.domain.repo.Payment.PaymentStatusLogRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;
import org.miniboot.app.util.errorvalidation.ValidationUtils;
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.RateLimiter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusLogRepository statusLogRepository;

    // Idempotency cache: key -> cached result (expires after 24 hours)
    private static final ConcurrentHashMap<String, CachedResult> idempotencyCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours

    public PaymentController(PaymentRepository paymentRepository,
            PaymentStatusLogRepository statusLogRepository) {
        this.paymentRepository = paymentRepository;
        this.statusLogRepository = statusLogRepository;
    }

    // Inner class for caching idempotent results
    private static class CachedResult {
        final HttpResponse response;
        final long timestamp;
        final String requestHash;

        CachedResult(HttpResponse response, String requestHash) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
            this.requestHash = requestHash;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }

    public static void mount(Router router, PaymentController pc) {
        router.get("/payments", pc.getPayments());
        router.post("/payments", pc.createPayment());
        router.put("/payments", pc.updatePayment());
        // có thể thêm route status vào controller riêng (ở dưới mình làm controller
        // riêng)
        router.get("/payments/with-status", pc.getPaymentsWithStatus());
    }

    /**
     * GET /payments
     * - Không query -> trả tất cả
     * - ?id= -> trả theo id (404 nếu không có)
     * - ?code= -> trả theo mã code (404 nếu không có)
     * - Requires JWT authentication
     */
    public Function<HttpRequest, HttpResponse> getPayments() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null)
                return rateLimitError;

            // Step 1: Validate JWT (no Content-Type check for GET)
            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null)
                return jwtError;

            try {
                Map<String, List<String>> q = req.query;

                // Ưu tiên id
                Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");
                if (idOpt.isPresent()) {
                    int requestedId = idOpt.get();
                    Optional<Payment> payment;
                    try {
                        payment = paymentRepository.getPaymentById(requestedId);
                    } catch (Exception e) {
                        return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    if (payment.isEmpty()) {
                        return ValidationUtils.error(404, "PAYMENT_NOT_FOUND",
                                "Payment with ID " + requestedId + " not found");
                    }
                    return Json.ok(payment.get());
                }

                // Tìm theo code
                Optional<String> codeOpt = ExtractHelper.extractFirst(q, "code");
                if (codeOpt.isPresent()) {
                    String requestedCode = codeOpt.get();
                    Optional<Payment> payment = Optional.empty();

                    try {
                        var method = paymentRepository.getClass().getMethod("getPaymentByCode", String.class);
                        @SuppressWarnings("unchecked")
                        Optional<Payment> found = (Optional<Payment>) method.invoke(paymentRepository, requestedCode);
                        payment = found;
                    } catch (NoSuchMethodException ignore) {
                        try {
                            payment = paymentRepository.getPayments().stream()
                                    .filter(p -> requestedCode.equals(p.getCode()))
                                    .findFirst();
                        } catch (Exception e) {
                            return DatabaseErrorHandler.handleDatabaseException(e);
                        }
                    } catch (Exception e) {
                        return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    if (payment.isEmpty()) {
                        return ValidationUtils.error(404, "PAYMENT_NOT_FOUND",
                                "Payment with code '" + requestedCode + "' not found");
                    }
                    return Json.ok(payment.get());
                }

                // Trả tất cả payments
                List<Payment> payments;
                try {
                    payments = paymentRepository.getPayments();
                    System.out.println("📋 Fetching all payments: " + payments.size() + " records");
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                return Json.ok(payments);

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in getPayments: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    /**
     * POST /payments
     * Body: Payment (JSON)
     * - Tạo payment mới với đầy đủ validation
     * - Ghi status log ban đầu = UNPAID
     * - Hỗ trợ Idempotency Key để tránh duplicate payments
     */
    public Function<HttpRequest, HttpResponse> createPayment() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null)
                return rateLimitError;

            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "STAFF");
            if (validationError != null)
                return validationError;

            try {
                // Step 4: Check Idempotency Key
                Map<String, String> headers = req.headers;
                String idempotencyKey = headers.get("Idempotency-Key");
                if (idempotencyKey == null) {
                    idempotencyKey = headers.get("idempotency-key");
                }

                String requestHash = req.body != null ? String.valueOf(new String(req.body).hashCode()) : "";

                if (idempotencyKey != null) {
                    CachedResult cached = idempotencyCache.get(idempotencyKey);
                    if (cached != null) {
                        if (cached.isExpired()) {
                            idempotencyCache.remove(idempotencyKey);
                        } else {
                            // Check if request content matches
                            if (cached.requestHash.equals(requestHash)) {
                                System.out.println("♻️ Returning cached result for idempotency key: " + idempotencyKey);
                                return cached.response;
                            } else {
                                return ValidationUtils.error(409, "IDEMPOTENCY_KEY_CONFLICT",
                                        "Idempotency Key reuse conflict: different request content");
                            }
                        }
                    }
                }

                // Step 5: Parse JSON
                Payment payment;
                try {
                    payment = Json.fromBytes(req.body, Payment.class);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Invalid JSON format: " + e.getMessage());
                }

                // Step 6: Validate required fields
                if (payment.getCode() == null || payment.getCode().trim().isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Payment code is required");
                }
                if (payment.getGrandTotal() <= 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Grand total must be greater than 0");
                }
                if (payment.getPaymentMethod() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Payment method is required");
                }
                if (payment.getAmountPaid() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Amount paid is required");
                }

                // Step 7: Business rules validation
                if (payment.getAmountPaid() < payment.getGrandTotal()) {
                    return ValidationUtils.error(422, "VALIDATION_FAILED",
                            "Amount paid (" + payment.getAmountPaid() + ") must be >= grand total ("
                                    + payment.getGrandTotal() + ")");
                }

                // Maximum payment limit: 1 billion VND
                if (payment.getAmountPaid() > 1_000_000_000) {
                    return ValidationUtils.error(422, "VALIDATION_FAILED",
                            "Amount paid exceeds maximum limit of 1,000,000,000 VND");
                }

                // Step 8: Check for duplicate payment (same code + PAID status)
                try {
                    List<Payment> existingPayments = paymentRepository.getPayments();
                    for (Payment p : existingPayments) {
                        if (payment.getCode().equals(p.getCode())) {
                            // Check if this payment is already PAID
                            try {
                                PaymentStatus status = statusLogRepository.getCurrentPaymentStatus(p.getId());
                                if (status == PaymentStatus.PAID) {
                                    return ValidationUtils.error(409, "CONFLICT",
                                            "Payment with code '" + payment.getCode() + "' is already PAID");
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ Warning: Could not check payment status: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 9: Insert payment
                Payment saved;
                try {
                    saved = paymentRepository.savePayment(payment);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (saved == null || saved.getId() == null || saved.getId() == 0) {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Cannot create payment");
                }

                // Step 10: Create initial status log
                try {
                    statusLogRepository.setCurrentPaymentStatus(saved.getId(), PaymentStatus.UNPAID);
                } catch (Exception e) {
                    System.err.println("⚠️ Warning: Payment created but status log failed: " + e.getMessage());
                }

                System.out.println("✅ Payment created successfully: code=" + saved.getCode() + ", id=" + saved.getId());

                // Step 11: Cache result for idempotency
                HttpResponse response = Json.created(saved);
                if (idempotencyKey != null) {
                    idempotencyCache.put(idempotencyKey, new CachedResult(response, requestHash));
                }

                return response;

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in createPayment: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    /**
     * PUT /payments
     * Body: Payment (JSON) — phải có id
     * - Cập nhật payment với validation đầy đủ
     * - Không cho phép chuyển từ PAID về UNPAID
     */
    public Function<HttpRequest, HttpResponse> updatePayment() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null)
                return rateLimitError;

            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "STAFF");
            if (validationError != null)
                return validationError;

            try {
                // Step 4: Parse JSON
                Payment payment;
                try {
                    payment = Json.fromBytes(req.body, Payment.class);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Invalid JSON format: " + e.getMessage());
                }

                // Step 5: Validate payment ID
                if (payment.getId() == null || payment.getId() == 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Payment ID is required for update");
                }

                // Step 6: Check if payment exists
                Optional<Payment> existing;
                try {
                    existing = paymentRepository.getPaymentById(payment.getId());
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (existing.isEmpty()) {
                    return ValidationUtils.error(404, "PAYMENT_NOT_FOUND",
                            "Payment with ID " + payment.getId() + " not found");
                }

                // Step 7: Validate status transition (không cho chuyển PAID → UNPAID)
                try {
                    PaymentStatus currentStatus = statusLogRepository.getCurrentPaymentStatus(payment.getId());

                    if (currentStatus == PaymentStatus.PAID) {
                        Payment existingPayment = existing.get();
                        if (payment.getAmountPaid() != null &&
                                payment.getAmountPaid() < existingPayment.getGrandTotal()) {
                            return ValidationUtils.error(422, "INVALID_STATUS_TRANSITION",
                                    "Cannot reduce amount paid below grand total for a PAID payment");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Warning: Could not check payment status: " + e.getMessage());
                }

                // Step 8: Validate business rules
                if (payment.getAmountPaid() != null && payment.getAmountPaid() < 0) {
                    return ValidationUtils.error(422, "VALIDATION_FAILED",
                            "Amount paid cannot be negative");
                }

                if (payment.getGrandTotal() < 0) {
                    return ValidationUtils.error(422, "VALIDATION_FAILED",
                            "Grand total cannot be negative");
                }

                // Step 9: Update payment
                Payment updated;
                try {
                    updated = paymentRepository.savePayment(payment);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (updated == null) {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to update payment");
                }

                System.out.println("✅ Payment updated successfully: ID=" + updated.getId());
                return Json.ok(updated);

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in updatePayment: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    /**
     * GET /payments/with-status
     * Lấy tất cả các payment, kèm theo trạng thái hiện tại của chúng.
     * Sử dụng DTO 'PaymentWithStatus'.
     * Requires JWT authentication.
     */
    public Function<HttpRequest, HttpResponse> getPaymentsWithStatus() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null)
                return rateLimitError;

            // Step 1: Validate JWT (no Content-Type check for GET)
            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null)
                return jwtError;

            try {
                // Step 2: Get all payments with status
                List<PaymentWithStatus> result;
                try {
                    result = paymentRepository.getAllPaymentsWithStatus();
                    System.out.println("📊 Fetching all payments with status: " + result.size() + " records");
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 3: Return result
                return Json.ok(result);

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in getPaymentsWithStatus: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }
}
