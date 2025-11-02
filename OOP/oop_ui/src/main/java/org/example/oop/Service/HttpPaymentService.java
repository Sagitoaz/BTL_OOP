package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.ErrorHandler;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;
import org.miniboot.app.util.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * HttpPaymentService - Payment API Service
 * ✅ Updated với ErrorHandler framework (Ngày 2 - Person 4)
 */
public class HttpPaymentService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpPaymentService() {
        this(ApiConfig.getBaseUrl());
    }

    public HttpPaymentService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /payment - Lấy tất cả các payment
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public List<Payment> getAllPayments() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + ApiConfig.paymentsEndpoint()))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải danh sách thanh toán")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<Payment>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse payments list");
                    return List.of();
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tải danh sách thanh toán");
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải danh sách thanh toán");
            return List.of();
        }
    }

    /**
     * GET /payment?id={}
     * Lấy payment theo id
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Payment getPaymentById(int paymentId) {
        try {
            String url = String.format("%s%s?id=%d", baseUrl, ApiConfig.paymentsEndpoint(), paymentId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải thông tin thanh toán")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), new TypeToken<Payment>() {
                    }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse payment by ID");
                    return null;
                }
            } else if (response.statusCode() == 404) {
                return null;
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tải thông tin thanh toán");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải thông tin thanh toán");
            return null;
        }
    }

    /**
     * POST /payment - Tạo payment mới
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Payment create(Payment payment) {
        try {
            String jsonBody = gson.toJson(payment);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.paymentsEndpoint()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tạo thanh toán mới")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), Payment.class);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created payment");
                    return null;
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tạo thanh toán mới");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tạo thanh toán mới");
            return null;
        }
    }

    /**
     * PUT /payment - Cập nhật payment
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public Payment updatePayment(Payment payment) {
        try {
            String jsonBody = gson.toJson(payment);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.paymentsEndpoint()))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Cập nhật thanh toán")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), Payment.class);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated payment");
                    return null;
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể cập nhật thanh toán");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Cập nhật thanh toán");
            return null;
        }
    }

    /**
     * Kiểm tra kết nối server
     */
    public boolean isServerAvailable() {
        try {
            // Thay đổi từ /echo sang /appointments vì đã xóa EchoController
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.paymentsEndpoint()))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GET /payments/with-status - Lấy danh sách tất cả payments với trạng thái của
     * chúng
     * ✅ Updated với ErrorHandler framework (Ngày 2)
     */
    public List<PaymentWithStatus> getPaymentsWithStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.paymentsWithStatusEndpoint()))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải danh sách thanh toán với trạng thái")) {
                    return List.of();
                }

                try {
                    return gson.fromJson(response.body(),
                            new TypeToken<List<PaymentWithStatus>>() {
                            }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse payments with status");
                    return List.of();
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(),
                        "Không thể tải danh sách thanh toán với trạng thái");
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải danh sách thanh toán với trạng thái");
            return List.of();
        }
    }
}
