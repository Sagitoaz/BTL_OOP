package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.Payment.PaymentStatusLog;

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
        this("http://localhost:8080");
    }

    public HttpPaymentStatusLogService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /payment-status?id=
     * tìm trạng thái gần nhất của payment có id
     */
    public PaymentStatusLog getCurrentStatusById(int id) {
        try {
            String url = String.format("%s/payment-status?paymentId=%d", baseUrl, id);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<PaymentStatusLog>() {
                        }.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public PaymentStatusLog updatePaymentStatus(PaymentStatusLog paymentStatusLog) {
        try {
            String jsonBody = gson.toJson(paymentStatusLog);
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payment-status"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), PaymentStatusLog.class);
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
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
                    .uri(URI.create(baseUrl + "/payment-status"))
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
