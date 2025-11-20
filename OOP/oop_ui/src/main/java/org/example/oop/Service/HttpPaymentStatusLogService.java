package org.example.oop.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.GsonProvider;
import org.example.oop.Utils.PaymentConfig;
import org.miniboot.app.domain.models.Payment.PaymentStatusLog; // Temporary workaround

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;

/**
 * 🌐 PAYMENT STATUS LOG SERVICE - PaymentStatusLog API Integration
 * <p>
 * Service layer làm cầu nối giữa Frontend và Backend API cho PaymentStatusLog
 * operations
 * Theo pattern của CustomerRecordService với:
 * - Singleton pattern
 * - ApiResponse wrapper cho type safety
 * - Sync và Async methods
 * - JavaFX Platform threading
 * - Error handling chuẩn
 * - JSON serialization/deserialization
 * - Automatic JWT authentication via ApiClient
 */
public class HttpPaymentStatusLogService {

    // Singleton instance
    private static HttpPaymentStatusLogService instance;
    private final ApiClient apiClient;
    private final Gson gson;

    private HttpPaymentStatusLogService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = GsonProvider.createGson();
    }

    public static synchronized HttpPaymentStatusLogService getInstance() {
        if (instance == null) {
            instance = new HttpPaymentStatusLogService();
        }
        return instance;
    }

    // ================================
    // SYNCHRONOUS METHODS (ĐỒNG BỘ)
    // ================================

    /**
     * GET /paymentStatusLogs - Lấy tất cả payment status logs (Sync)
     */
    public ApiResponse<List<PaymentStatusLog>> getAllPaymentStatusLogs() {
        ApiResponse<String> response = apiClient.get(PaymentConfig.GET_PAYMENT_STATUS_LOG_ENDPOINT);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải lịch sử trạng thái thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<PaymentStatusLog> logs = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentStatusLog>>() {
                        }.getType());
                return ApiResponse.success(logs, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment status logs list");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải lịch sử trạng thái thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /paymentStatusLogs?paymentId={id} - Lấy status logs theo payment ID
     * (Sync)
     */
    public ApiResponse<List<PaymentStatusLog>> getPaymentStatusLogsByPaymentId(int paymentId) {
        String endpoint = PaymentConfig.GET_PAYMENT_STATUS_LOG_ENDPOINT + "?paymentId=" + paymentId;
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải lịch sử trạng thái thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<PaymentStatusLog> logs = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentStatusLog>>() {
                        }.getType());
                return ApiResponse.success(logs, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment status logs by payment ID");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải lịch sử trạng thái thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /payment-status?paymentId={id} - Lấy trạng thái hiện tại (Sync)
     * Backend returns: { "paymentId": 123, "status": "PENDING" }
     */
    public ApiResponse<PaymentStatusLog> getCurrentStatusById(int paymentId) {
        String endpoint = PaymentConfig.GET_PAYMENT_STATUS_LOG_ENDPOINT + "?paymentId=" + paymentId;
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải trạng thái hiện tại")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                // Backend returns: { "paymentId": 123, "status": "PENDING" }
                // We need to parse and create PaymentStatusLog
                com.google.gson.JsonObject jsonObj = gson.fromJson(response.getData(),
                        com.google.gson.JsonObject.class);

                PaymentStatusLog log = new PaymentStatusLog();
                log.setPaymentId(jsonObj.get("paymentId").getAsInt());
                log.setStatus(org.miniboot.app.domain.models.Payment.PaymentStatus
                        .valueOf(jsonObj.get("status").getAsString()));

                return ApiResponse.success(log, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse current payment status");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải trạng thái hiện tại");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * POST /payment-status - Tạo/cập nhật payment status (Sync)
     * Backend expects: { "paymentId": 123, "status": "PENDING" }
     * Backend returns: { "paymentId": 123, "status": "PENDING" }
     */
    public ApiResponse<PaymentStatusLog> createPaymentStatusLog(PaymentStatusLog log) {
        try {
            // ✅ Create request body matching backend format
            String jsonBody = String.format("{\"paymentId\":%d,\"status\":\"%s\"}",
                    log.getPaymentId(),
                    log.getStatus().name());

            ApiResponse<String> response = apiClient.post(PaymentConfig.POST_PAYMENT_STATUS_LOG_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Tạo log trạng thái mới")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    // Backend returns: { "paymentId": 123, "status": "PENDING" }
                    com.google.gson.JsonObject jsonObj = gson.fromJson(response.getData(),
                            com.google.gson.JsonObject.class);

                    PaymentStatusLog createdLog = new PaymentStatusLog();
                    createdLog.setPaymentId(jsonObj.get("paymentId").getAsInt());
                    createdLog.setStatus(org.miniboot.app.domain.models.Payment.PaymentStatus
                            .valueOf(jsonObj.get("status").getAsString()));

                    return ApiResponse.success(createdLog, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created payment status log");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tạo log trạng thái mới");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment status log");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * PUT /paymentStatusLogs - Cập nhật payment status log (Sync)
     */
    public ApiResponse<PaymentStatusLog> updatePaymentStatusLog(PaymentStatusLog log) {
        if (log.getId() <= 0) {
            return ApiResponse.error("PaymentStatusLog ID is required for update");
        }

        try {
            String jsonBody = gson.toJson(log);
            ApiResponse<String> response = apiClient.put(PaymentConfig.PUT_PAYMENT_STATUS_LOG_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Cập nhật log trạng thái")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    PaymentStatusLog updatedLog = gson.fromJson(response.getData(), PaymentStatusLog.class);
                    return ApiResponse.success(updatedLog, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated payment status log");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể cập nhật log trạng thái");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment status log");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái thanh toán - Helper method (Sync)
     * OLD API compatibility: accepts PaymentStatusLog object
     */
    public ApiResponse<PaymentStatusLog> updatePaymentStatus(PaymentStatusLog log) {
        return createPaymentStatusLog(log);
    }

    /**
     * Cập nhật trạng thái thanh toán - Helper method with parameters (Sync)
     * Note: Backend PaymentStatusLog chỉ có: id, paymentId, changedAt, status
     * KHÔNG có changedBy hay notes
     */
    public ApiResponse<PaymentStatusLog> updatePaymentStatus(int paymentId,
            org.miniboot.app.domain.models.Payment.PaymentStatus newStatus) {
        PaymentStatusLog log = new PaymentStatusLog();
        log.setPaymentId(paymentId);
        log.setStatus(newStatus);
        return createPaymentStatusLog(log);
    }

    // ================================
    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)
    // ================================

    /**
     * ASYNC - GET /paymentStatusLogs - Lấy tất cả logs (Async)
     */
    public void getAllPaymentStatusLogsAsync(Consumer<List<PaymentStatusLog>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(PaymentConfig.GET_PAYMENT_STATUS_LOG_ENDPOINT,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<PaymentStatusLog> logs;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                logs = new ArrayList<>();
                            } else {
                                logs = gson.fromJson(responseData,
                                        new TypeToken<List<PaymentStatusLog>>() {
                                        }.getType());
                                if (logs == null) {
                                    logs = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(logs);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payment status logs list (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải lịch sử trạng thái thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error),
                            "Tải lịch sử trạng thái thanh toán (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - GET /paymentStatusLogs?paymentId={id} - Lấy logs theo payment ID
     * (Async)
     */
    public void getPaymentStatusLogsByPaymentIdAsync(int paymentId, Consumer<List<PaymentStatusLog>> onSuccess,
            Consumer<String> onError) {
        String endpoint = PaymentConfig.GET_PAYMENT_STATUS_LOG_ENDPOINT + "?paymentId=" + paymentId;
        apiClient.getAsync(endpoint,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<PaymentStatusLog> logs;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                logs = new ArrayList<>();
                            } else {
                                logs = gson.fromJson(responseData,
                                        new TypeToken<List<PaymentStatusLog>>() {
                                        }.getType());
                                if (logs == null) {
                                    logs = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(logs);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payment status logs by payment ID (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải lịch sử trạng thái thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error),
                            "Tải lịch sử trạng thái thanh toán (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - Cập nhật trạng thái thanh toán (Async)
     * Note: Backend PaymentStatusLog chỉ có: id, paymentId, changedAt, status
     * KHÔNG có changedBy hay notes
     */
    public void updatePaymentStatusAsync(int paymentId, org.miniboot.app.domain.models.Payment.PaymentStatus newStatus,
            Consumer<PaymentStatusLog> onSuccess, Consumer<String> onError) {
        PaymentStatusLog log = new PaymentStatusLog();
        log.setPaymentId(paymentId);
        log.setStatus(newStatus);
        // changedAt sẽ được set tự động ở backend

        try {
            String jsonBody = gson.toJson(log);
            apiClient.postAsync(PaymentConfig.POST_PAYMENT_STATUS_LOG_ENDPOINT, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                PaymentStatusLog createdLog = gson.fromJson(response.getData(),
                                        PaymentStatusLog.class);
                                onSuccess.accept(createdLog);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse created payment status log (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể cập nhật trạng thái thanh toán");
                            onError.accept(response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error),
                                "Cập nhật trạng thái thanh toán (async)");
                        onError.accept(error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment status log (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }
}
