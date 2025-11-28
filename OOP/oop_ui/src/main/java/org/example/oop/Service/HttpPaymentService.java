package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;
import org.example.oop.Utils.PaymentConfig; // Temporary workaround

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 🌐 PAYMENT SERVICE - Payment API Integration
 *
 * Service layer làm cầu nối giữa Frontend và Backend API cho Payment operations
 * Theo pattern của CustomerRecordService với:
 * - Singleton pattern
 * - ApiResponse wrapper cho type safety
 * - Sync và Async methods
 * - JavaFX Platform threading
 * - Error handling chuẩn
 * - JSON serialization/deserialization
 * - Automatic JWT authentication via ApiClient
 */
public class HttpPaymentService {

    private final ApiClient apiClient;
    private final Gson gson;

    // Singleton instance
    private static HttpPaymentService instance;

    private HttpPaymentService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = GsonProvider.createGson();
    }

    public static synchronized HttpPaymentService getInstance() {
        if (instance == null) {
            instance = new HttpPaymentService();
        }
        return instance;
    }

    // SYNCHRONOUS METHODS (ĐỒNG BỘ)

    /**
     * GET /payments - Lấy tất cả payments (Sync)
     */
    public ApiResponse<List<Payment>> getAllPayments() {
        ApiResponse<String> response = apiClient.get(PaymentConfig.GET_PAYMENT_ENDPOINT);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải danh sách thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<Payment> payments = gson.fromJson(response.getData(),
                        new TypeToken<List<Payment>>() {
                        }.getType());
                return ApiResponse.success(payments, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payments list");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /payments?id={id} - Lấy payment theo ID (Sync)
     */
    public ApiResponse<Payment> getPaymentById(int paymentId) {
        String endpoint = PaymentConfig.GET_PAYMENT_ENDPOINT + "?id=" + paymentId;
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải thông tin thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                Payment payment = gson.fromJson(response.getData(), Payment.class);
                return ApiResponse.success(payment, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment by ID");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải thông tin thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * POST /payments - Tạo payment mới (Sync)
     * OLD API compatibility: method name 'create'
     */
    public ApiResponse<Payment> create(Payment payment) {
        return createPayment(payment);
    }

    /**
     * POST /payments - Tạo payment mới (Sync)
     */
    public ApiResponse<Payment> createPayment(Payment payment) {
        try {
            String jsonBody = gson.toJson(payment);
            ApiResponse<String> response = apiClient.post(PaymentConfig.POST_PAYMENT_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Tạo thanh toán mới")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    Payment createdPayment = gson.fromJson(response.getData(), Payment.class);
                    return ApiResponse.success(createdPayment, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created payment");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tạo thanh toán mới");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * PUT /payments - Cập nhật payment (Sync)
     */
    public ApiResponse<Payment> updatePayment(Payment payment) {
        if (payment.getId() <= 0) {
            return ApiResponse.error("Payment ID is required for update");
        }

        try {
            String jsonBody = gson.toJson(payment);
            String endpoint = PaymentConfig.PUT_PAYMENT_ENDPOINT;
            ApiResponse<String> response = apiClient.put(endpoint, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Cập nhật thanh toán")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    Payment updatedPayment = gson.fromJson(response.getData(), Payment.class);
                    return ApiResponse.success(updatedPayment, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated payment");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể cập nhật thanh toán");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * GET /payments/with-status - Lấy tất cả payments với trạng thái (Sync)
     */
    public ApiResponse<List<PaymentWithStatus>> getPaymentsWithStatus() {
        ApiResponse<String> response = apiClient.get(PaymentConfig.GET_PAYMENT_WITH_STATUS_ENDPOINT);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải danh sách thanh toán với trạng thái")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<PaymentWithStatus> payments = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentWithStatus>>() {
                        }.getType());
                return ApiResponse.success(payments, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payments with status");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /payments/with-status?id={id} - Lấy 1 payment với trạng thái theo ID
     * (Sync)
     * TỐI ƯU: Gộp 2 requests (payment + status) thành 1 request duy nhất
     */
    public ApiResponse<PaymentWithStatus> getPaymentWithStatusById(int paymentId) {
        String endpoint = PaymentConfig.GET_PAYMENT_WITH_STATUS_ENDPOINT + "?id=" + paymentId;
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải thông tin thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                // Backend trả về array với 1 phần tử
                List<PaymentWithStatus> payments = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentWithStatus>>() {
                        }.getType());

                if (payments == null || payments.isEmpty()) {
                    return ApiResponse.error("Payment not found with ID: " + paymentId);
                }

                return ApiResponse.success(payments.get(0), response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment with status by ID");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải thông tin thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)

    /**
     * ASYNC - GET /payments - Lấy tất cả payments (Async)
     */
    public void getAllPaymentsAsync(Consumer<List<Payment>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(PaymentConfig.GET_PAYMENT_ENDPOINT,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<Payment> payments;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                payments = new ArrayList<>();
                            } else {
                                payments = gson.fromJson(responseData, new TypeToken<List<Payment>>() {
                                }.getType());
                                if (payments == null) {
                                    payments = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(payments);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payments list (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải danh sách thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tải danh sách thanh toán (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - GET /payments/with-status - Lấy payments với trạng thái (Async)
     */
    public void getPaymentsWithStatusAsync(Consumer<List<PaymentWithStatus>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(PaymentConfig.GET_PAYMENT_WITH_STATUS_ENDPOINT,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<PaymentWithStatus> payments;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                payments = new ArrayList<>();
                            } else {
                                payments = gson.fromJson(responseData,
                                        new TypeToken<List<PaymentWithStatus>>() {
                                        }.getType());
                                if (payments == null) {
                                    payments = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(payments);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payments with status (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải danh sách thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error),
                            "Tải danh sách thanh toán với trạng thái (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - POST /payments - Tạo payment mới (Async)
     */
    public void createPaymentAsync(Payment payment, Consumer<Payment> onSuccess, Consumer<String> onError) {
        try {
            String jsonBody = gson.toJson(payment);

            apiClient.postAsync(PaymentConfig.POST_PAYMENT_ENDPOINT, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                Payment createdPayment = gson.fromJson(response.getData(), Payment.class);
                                onSuccess.accept(createdPayment);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse created payment (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể tạo thanh toán mới");
                            onError.accept(response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error), "Tạo thanh toán (async)");
                        onError.accept(error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    /**
     * ASYNC - PUT /payments - Cập nhật payment (Async)
     */
    public void updatePaymentAsync(Payment payment, Consumer<Payment> onSuccess, Consumer<String> onError) {
        if (payment.getId() <= 0) {
            Platform.runLater(() -> onError.accept("Payment ID is required for update"));
            return;
        }

        try {
            String jsonBody = gson.toJson(payment);
            String endpoint = PaymentConfig.PUT_PAYMENT_ENDPOINT;

            apiClient.putAsync(endpoint, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                Payment updatedPayment = gson.fromJson(response.getData(), Payment.class);
                                onSuccess.accept(updatedPayment);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse updated payment (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể cập nhật thanh toán");
                            onError.accept(response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error), "Cập nhật thanh toán (async)");
                        onError.accept(error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    // UTILITY METHODS (PHƯƠNG THỨC HỖ TRỢ)

    /**
     * Kiểm tra kết nối server (Async)
     */
    public void checkServerConnection(Consumer<Boolean> onResult) {
        apiClient.getAsync(PaymentConfig.GET_PAYMENT_ENDPOINT,
                response -> onResult.accept(response.isSuccess()),
                error -> onResult.accept(false));
    }
}
