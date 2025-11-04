package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.ApiConfig;
import org.example.oop.Utils.HttpException;
import org.miniboot.app.domain.models.Payment.PaymentStatusLog;
import org.miniboot.app.util.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpPaymentStatusLogService {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpPaymentStatusLogService() {
        this(ApiConfig.getBaseUrl());
    }

    public HttpPaymentStatusLogService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /payment-status?id=
     * tìm trạng thái gần nhất của payment có id
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public PaymentStatusLog getCurrentStatusById(int id) {
        try {
            String url = String.format("%s%s?paymentId=%d", baseUrl, ApiConfig.paymentStatusEndpoint(), id);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Tải trạng thái thanh toán")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), new TypeToken<PaymentStatusLog>() {
                    }.getType());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse payment status");
                    return null;
                }
            } else if (response.statusCode() == 404) {
                return null;
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tải trạng thái thanh toán");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Tải trạng thái thanh toán");
            return null;
        }
    }

    /**
     * POST /payment-status
     * Cập nhật trạng thái thanh toán
     * ✅ Updated với ErrorHandler framework (Ngày 3)
     */
    public PaymentStatusLog updatePaymentStatus(PaymentStatusLog paymentStatusLog) {
        try {
            String jsonBody = gson.toJson(paymentStatusLog);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + ApiConfig.paymentStatusEndpoint()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                if (!ErrorHandler.validateResponse(response.body(), "Cập nhật trạng thái thanh toán")) {
                    return null;
                }

                try {
                    return gson.fromJson(response.body(), PaymentStatusLog.class);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated payment status");
                    return null;
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể cập nhật trạng thái thanh toán");
                return null;
            }

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleConnectionError(e, "Cập nhật trạng thái thanh toán");
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
                    .uri(URI.create(baseUrl + ApiConfig.paymentStatusEndpoint()))
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
}
