package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ApiConfig;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;
import org.miniboot.app.util.GsonProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpPaymentService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private String jwtToken = null; // JWT token for authentication

    public HttpPaymentService() {
        this(ApiConfig.getBaseUrl());
        // Auto-load JWT token from SessionStorage
        this.jwtToken = SessionStorage.getJwtToken();
    }

    public HttpPaymentService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
        // Auto-load JWT token from SessionStorage
        this.jwtToken = SessionStorage.getJwtToken();
    }

    /**
     * Set JWT token for authenticated requests
     */
    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    /**
     * Build request with JWT authentication header
     */
    private HttpRequest.Builder buildAuthenticatedRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json");

        if (jwtToken != null && !jwtToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return builder;
    }

    /**
     * GET /payments - Lấy tất cả các payment
     * Throws exception with detailed error message for UI handling
     */
    public List<Payment> getAllPayments() throws Exception {
        HttpRequest request = buildAuthenticatedRequest(baseUrl + "/payments")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(),
                    new TypeToken<List<Payment>>() {
                    }.getType());
        } else if (response.statusCode() == 401) {
            throw new Exception("401: Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } else if (response.statusCode() == 429) {
            throw new Exception("429: Quá nhiều yêu cầu. Vui lòng đợi 1 phút và thử lại.");
        } else if (response.statusCode() == 503 || response.statusCode() == 504) {
            throw new Exception(response.statusCode() + ": Hệ thống đang bận. Vui lòng thử lại sau.");
        } else {
            throw new Exception(response.statusCode() + ": Lỗi không xác định - " + response.body());
        }
    }

    /**
     * GET /payments?id={} - Lấy payment theo id
     * Throws exception with detailed error message for UI handling
     */
    public Payment getPaymentById(int paymentId) throws Exception {
        String url = String.format("%s/payments?id=%d", baseUrl, paymentId);

        HttpRequest request = buildAuthenticatedRequest(url)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(),
                    new TypeToken<Payment>() {
                    }.getType());
        } else if (response.statusCode() == 404) {
            throw new Exception("404: Không tìm thấy hóa đơn với ID " + paymentId);
        } else if (response.statusCode() == 401) {
            throw new Exception("401: Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } else if (response.statusCode() == 403) {
            throw new Exception("403: Bạn không có quyền xem hóa đơn này.");
        } else if (response.statusCode() == 429) {
            throw new Exception("429: Quá nhiều yêu cầu. Vui lòng đợi 1 phút.");
        } else if (response.statusCode() == 503 || response.statusCode() == 504) {
            throw new Exception(response.statusCode() + ": Hệ thống đang bận. Vui lòng thử lại sau.");
        } else {
            throw new Exception(response.statusCode() + ": Lỗi không xác định - " + response.body());
        }
    }

    /**
     * POST /payments - Tạo payment mới
     * Supports Idempotency-Key header to prevent duplicate payments
     * Throws exception with detailed error message for UI handling
     */
    public Payment create(Payment payment) throws Exception {
        return create(payment, java.util.UUID.randomUUID().toString());
    }

    /**
     * POST /payments với Idempotency Key
     */
    public Payment create(Payment payment, String idempotencyKey) throws Exception {
        String jsonBody = gson.toJson(payment);
        System.out.println("📤 Sending JSON: " + jsonBody);

        HttpRequest.Builder builder = buildAuthenticatedRequest(baseUrl + "/payments")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            builder.header("Idempotency-Key", idempotencyKey);
        }

        HttpRequest request = builder.build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), Payment.class);
        } else if (response.statusCode() == 400) {
            throw new Exception("400: Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin đã nhập.");
        } else if (response.statusCode() == 401) {
            throw new Exception("401: Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } else if (response.statusCode() == 404) {
            throw new Exception("404: Không tìm thấy hóa đơn này trong hệ thống.");
        } else if (response.statusCode() == 409) {
            String body = response.body();
            if (body.contains("PAID")) {
                throw new Exception("409: Hóa đơn này đã được thanh toán trước đó.");
            } else if (body.contains("Idempotency")) {
                throw new Exception("409: Yêu cầu thanh toán đang được xử lý. Vui lòng đợi.");
            } else {
                throw new Exception("409: Xung đột dữ liệu - " + body);
            }
        } else if (response.statusCode() == 422) {
            String body = response.body();
            if (body.contains("amount") && body.contains("grand total")) {
                throw new Exception("422: Số tiền thanh toán phải lớn hơn hoặc bằng tổng tiền hóa đơn.");
            } else if (body.contains("maximum")) {
                throw new Exception("422: Số tiền thanh toán không được vượt quá 1 tỷ VNĐ.");
            } else {
                throw new Exception("422: Dữ liệu không hợp lệ - " + body);
            }
        } else if (response.statusCode() == 429) {
            throw new Exception("429: Quá nhiều yêu cầu. Vui lòng đợi 1 phút và thử lại.");
        } else if (response.statusCode() == 502 || response.statusCode() == 503 || response.statusCode() == 504) {
            throw new Exception(response.statusCode()
                    + ": Lỗi kết nối với cổng thanh toán hoặc hệ thống quá tải. Vui lòng thử lại sau.");
        } else {
            throw new Exception(response.statusCode() + ": Lỗi không xác định - " + response.body());
        }
    }

    /**
     * PUT /payments - Cập nhật payment
     * Throws exception with detailed error message for UI handling
     */
    public Payment updatePayment(Payment payment) throws Exception {
        String jsonBody = gson.toJson(payment);
        System.out.println("🔄 Updating JSON: " + jsonBody);

        HttpRequest request = buildAuthenticatedRequest(baseUrl + "/payments")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Payment.class);
        } else if (response.statusCode() == 400) {
            throw new Exception("400: Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.");
        } else if (response.statusCode() == 401) {
            throw new Exception("401: Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } else if (response.statusCode() == 404) {
            throw new Exception("404: Không tìm thấy payment với ID này.");
        } else if (response.statusCode() == 412) {
            throw new Exception("412: Xung đột phiên bản. Dữ liệu đã được sửa bởi người khác, vui lòng tải lại.");
        } else if (response.statusCode() == 422) {
            String body = response.body();
            if (body.contains("PAID")) {
                throw new Exception("422: Không thể giảm số tiền của hóa đơn đã thanh toán.");
            } else {
                throw new Exception("422: Dữ liệu không hợp lệ - " + body);
            }
        } else if (response.statusCode() == 429) {
            throw new Exception("429: Quá nhiều yêu cầu. Vui lòng đợi 1 phút.");
        } else if (response.statusCode() == 503 || response.statusCode() == 504) {
            throw new Exception(response.statusCode() + ": Hệ thống đang bận. Vui lòng thử lại sau.");
        } else {
            throw new Exception(response.statusCode() + ": Lỗi không xác định - " + response.body());
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

    /**
     * GET /payments/with-status - Lấy danh sách tất cả payments với trạng thái của
     * chúng
     * Throws exception with detailed error message for UI handling
     */
    public List<PaymentWithStatus> getPaymentsWithStatus() throws Exception {
        HttpRequest request = buildAuthenticatedRequest(baseUrl + "/payments/with-status")
                .GET()
                .build();

        System.out.println("⏳ Sending request to: " + baseUrl + "/payments/with-status");

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println("📥 Response code: " + response.statusCode());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            System.out.println("📦 Response body length: " + responseBody.length() + " chars");
            System.out.println("📦 Response preview: "
                    + (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

            List<PaymentWithStatus> result = gson.fromJson(responseBody,
                    new TypeToken<List<PaymentWithStatus>>() {
                    }.getType());

            System.out.println("✅ Parsed " + (result != null ? result.size() : 0) + " payments");
            return result;
        } else if (response.statusCode() == 401) {
            throw new Exception("401: Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } else if (response.statusCode() == 429) {
            throw new Exception("429: Quá nhiều yêu cầu. Vui lòng đợi 1 phút.");
        } else if (response.statusCode() == 503 || response.statusCode() == 504) {
            throw new Exception(response.statusCode() + ": Hệ thống đang bận. Vui lòng thử lại sau.");
        } else {
            throw new Exception(response.statusCode() + ": Lỗi không xác định - " + response.body());
        }
    }
}
