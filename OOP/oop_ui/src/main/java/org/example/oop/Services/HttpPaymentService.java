package org.example.oop.Services;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Model.PaymentModel.Payment;
import org.example.oop.Utils.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpPaymentService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpPaymentService() {
        this("http://localhost:8080");
    }

    public HttpPaymentService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /payment - Lấy tất cả các payment
     */
    public List<Payment> getAllPayments() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/payments"))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<List<Payment>>() {
                        }.getType());
            } else {
                System.err.println("HTTP ERROR CODE: " + response.statusCode());
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * GET /payment?id={}
     * Lấy payment theo id
     */
    public Payment getPaymentById(int paymentId) {
        try {
            String url = String.format("%s/payments?id=%d",
                    baseUrl, paymentId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(),
                        new TypeToken<Payment>() {
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

    /**
     * POST /payment - Tạo payment mới
     */
    public Payment create(Payment payment) {
        try {
            String jsonBody = gson.toJson(payment);
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payments"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Payment.class);
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
                    .uri(URI.create(baseUrl + "/payments"))
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
