package org.example.oop.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.miniboot.app.domain.models.Payment.PaymentItem;
import org.miniboot.app.util.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class HttpPaymentItemService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpPaymentItemService() {
        this("https://btl-oop-i9pi.onrender.com");
    }

    public HttpPaymentItemService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /payment-items
     * Lấy tất cả PaymentItem hoặc theo paymentId hoặc id
     */
    public List<PaymentItem> getAllPaymentItems(Optional<Integer> paymentId, Optional<Integer> id) {
        try {
            String url = baseUrl + "/payment-items";
            if (paymentId.isPresent()) {
                url += "?paymentId=" + paymentId.get();
            } else if (id.isPresent()) {
                url += "?id=" + id.get();
            } else {
                throw new IllegalArgumentException("At least one query parameter is required: paymentId or id");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<PaymentItem>>() {
                }.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * POST /payment-items
     * Tạo mới PaymentItem
     */
    public PaymentItem createPaymentItem(PaymentItem paymentItem) {
        try {
            String jsonBody = gson.toJson(paymentItem);
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payment-items"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), PaymentItem.class);
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * PUT /payment-items
     * Cập nhật PaymentItem theo ID
     */
    public PaymentItem updatePaymentItem(PaymentItem paymentItem) {
        try {
            String jsonBody = gson.toJson(paymentItem);
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payment-items"))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), PaymentItem.class);
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * PUT /payment-items/replace
     * Thay thế toàn bộ PaymentItem của một Payment
     */
    public List<PaymentItem> replaceAllPaymentItems(int paymentId, List<PaymentItem> items) {
        try {
            String jsonBody = gson.toJson(new ReplaceBody(paymentId, items));
            System.out.println("📤 Sending JSON: " + jsonBody); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payment-items/replace"))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<PaymentItem>>() {
                }.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * DELETE /payment-items
     * Xóa một hoặc nhiều PaymentItem theo ID hoặc paymentId
     */
    public boolean deletePaymentItems(Optional<Integer> id, Optional<Integer> paymentId) {
        try {
            String url = baseUrl + "/payment-items";
            if (id.isPresent()) {
                url += "?id=" + id.get();
            } else if (paymentId.isPresent()) {
                url += "?paymentId=" + paymentId.get();
            } else {
                throw new IllegalArgumentException("At least one query parameter is required: id or paymentId");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * POST /payment-items
     * Gửi theo định dạng batch (wrapper object) mà server mong đợi,
     * TÁI SỬ DỤNG class 'ReplaceBody' đã có.
     */
    public List<PaymentItem> saveAllPaymentItems(List<PaymentItem> paymentItems) {
        if (paymentItems == null || paymentItems.isEmpty()) {
            return List.of();
        }

        try {
            // 1. LẤY paymentId (bạn đã set trong InvoiceController)
            // (Bạn cần thêm getPaymentId() vào model PaymentItem)
            int paymentId = paymentItems.get(0).getPaymentId();
            if (paymentId <= 0) {
                throw new IllegalArgumentException("Payment ID chưa được gán cho các items.");
            }

            // 2. TẠO WRAPPER OBJECT BẰNG 'ReplaceBody'
            // Server mong đợi: record SaveAllBody(Integer paymentId, List<PaymentItem> items)
            // Cấu trúc này khớp 100% với 'ReplaceBody'
            var saveBody = new ReplaceBody(paymentId, paymentItems);

            // 3. Chuyển DTO (wrapper) thành JSON
            // Kết quả sẽ là: {"paymentId": 123, "items": [...]}
            String jsonBody = gson.toJson(saveBody);
            System.out.println("📤 Sending JSON (Batch Wrapper): " + jsonBody);

            // 4. Gửi yêu cầu POST đến đúng URL
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payment-items")) //
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. Server trả về 201 (Created)
            if (response.statusCode() == 201) {
                return gson.fromJson(response.body(), new TypeToken<List<PaymentItem>>() {
                }.getType());
            } else {
                System.err.println("❌ HTTP Error: " + response.statusCode());
                System.err.println("Response Body: " + response.body());
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return List.of();
        }
    }

    // DTO cho replace
    public static class ReplaceBody {
        public int paymentId;
        public List<PaymentItem> items;

        public ReplaceBody(int paymentId, List<PaymentItem> items) {
            this.paymentId = paymentId;
            this.items = items;
        }

        public int getPaymentId() {
            return paymentId;
        }

        public List<PaymentItem> getItems() {
            return items;
        }
    }

}
