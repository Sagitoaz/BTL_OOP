package org.example.oop.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.GsonProvider;
import org.example.oop.Utils.PaymentConfig;
import org.miniboot.app.domain.models.Payment.PaymentItem; // Temporary workaround

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;

/**
 * 🌐 PAYMENT ITEM SERVICE - PaymentItem API Integration
 *
 * Service layer làm cầu nối giữa Frontend và Backend API cho PaymentItem
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
public class HttpPaymentItemService {

    private final ApiClient apiClient;
    private final Gson gson;

    // Singleton instance
    private static HttpPaymentItemService instance;

    private HttpPaymentItemService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = GsonProvider.createGson();
    }

    public static synchronized HttpPaymentItemService getInstance() {
        if (instance == null) {
            instance = new HttpPaymentItemService();
        }
        return instance;
    }

    // ================================
    // SYNCHRONOUS METHODS (ĐỒNG BỘ)
    // ================================

    /**
     * GET /paymentItems - Lấy tất cả payment items (Sync)
     */
    public ApiResponse<List<PaymentItem>> getAllPaymentItems() {
        ApiResponse<String> response = apiClient.get(PaymentConfig.GET_PAYMENT_ITEM_ENDPOINT);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải danh sách mục thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<PaymentItem> items = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentItem>>() {
                        }.getType());
                return ApiResponse.success(items, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment items list");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách mục thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /paymentItems?paymentId={id} - Lấy payment items theo payment ID (Sync)
     */
    public ApiResponse<List<PaymentItem>> getPaymentItemsByPaymentId(int paymentId) {
        String endpoint = PaymentConfig.GET_PAYMENT_ITEM_ENDPOINT + "?paymentId=" + paymentId;
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải mục thanh toán")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<PaymentItem> items = gson.fromJson(response.getData(),
                        new TypeToken<List<PaymentItem>>() {
                        }.getType());
                return ApiResponse.success(items, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse payment items by payment ID");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải mục thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * POST /paymentItems - Tạo payment item mới (Sync)
     */
    public ApiResponse<PaymentItem> createPaymentItem(PaymentItem item) {
        try {
            String jsonBody = gson.toJson(item);
            ApiResponse<String> response = apiClient.post(PaymentConfig.POST_PAYMENT_ITEM_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Tạo mục thanh toán mới")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    PaymentItem createdItem = gson.fromJson(response.getData(), PaymentItem.class);
                    return ApiResponse.success(createdItem, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created payment item");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tạo mục thanh toán mới");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment item");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * PUT /paymentItems - Cập nhật payment item (Sync)
     */
    public ApiResponse<PaymentItem> updatePaymentItem(PaymentItem item) {
        if (item.getId() <= 0) {
            return ApiResponse.error("PaymentItem ID is required for update");
        }

        try {
            String jsonBody = gson.toJson(item);
            ApiResponse<String> response = apiClient.put(PaymentConfig.PUT_PAYMENT_ITEM_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Cập nhật mục thanh toán")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    PaymentItem updatedItem = gson.fromJson(response.getData(), PaymentItem.class);
                    return ApiResponse.success(updatedItem, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated payment item");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể cập nhật mục thanh toán");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment item");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * DELETE /paymentItems?id={id} - Xóa payment item (Sync)
     */
    public ApiResponse<Boolean> deletePaymentItem(int itemId) {
        String endpoint = PaymentConfig.DELETE_PAYMENT_ITEM_ENDPOINT + "?id=" + itemId;
        ApiResponse<String> response = apiClient.delete(endpoint);

        if (response.isSuccess()) {
            return ApiResponse.success(true, response.getStatusCode());
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể xóa mục thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * DELETE /paymentItems?paymentId={id} - Xóa tất cả payment items của một
     * payment (Sync)
     */
    public ApiResponse<Boolean> deletePaymentItemsByPaymentId(int paymentId) {
        String endpoint = PaymentConfig.DELETE_PAYMENT_ITEM_ENDPOINT + "?paymentId=" + paymentId;
        ApiResponse<String> response = apiClient.delete(endpoint);

        if (response.isSuccess()) {
            return ApiResponse.success(true, response.getStatusCode());
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể xóa các mục thanh toán");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * Batch save - Xóa hết và tạo mới (Sync)
     * Dùng cho trường hợp cập nhật toàn bộ danh sách items
     * OLD API compatibility: returns List<PaymentItem> directly (wrapped in
     * ApiResponse)
     */
    public ApiResponse<List<PaymentItem>> saveAllPaymentItems(List<PaymentItem> items) {
        if (items == null || items.isEmpty()) {
            return ApiResponse.error("Items list is empty");
        }

        // Get paymentId from first item
        int paymentId = items.get(0).getPaymentId();
        if (paymentId <= 0) {
            return ApiResponse.error("PaymentId is required for all items");
        }

        // ✅ NEW LOGIC: Check if items already exist before deleting
        // If payment is new (no existing items), skip delete step
        ApiResponse<List<PaymentItem>> existingItemsResponse = getPaymentItemsByPaymentId(paymentId);
        boolean hasExistingItems = existingItemsResponse.isSuccess() &&
                existingItemsResponse.getData() != null &&
                !existingItemsResponse.getData().isEmpty();

        // Step 1: Delete existing items (only if they exist)
        if (hasExistingItems) {
            ApiResponse<Boolean> deleteResponse = deletePaymentItemsByPaymentId(paymentId);
            if (!deleteResponse.isSuccess()) {
                return ApiResponse.error("Failed to delete existing items: " + deleteResponse.getErrorMessage());
            }
        }

        // Step 2: Create new items
        List<PaymentItem> createdItems = new ArrayList<>();
        for (PaymentItem item : items) {
            item.setPaymentId(paymentId); // Ensure paymentId is set
            ApiResponse<PaymentItem> createResponse = createPaymentItem(item);
            if (createResponse.isSuccess()) {
                createdItems.add(createResponse.getData());
            } else {
                return ApiResponse.error("Failed to create item: " + createResponse.getErrorMessage());
            }
        }

        return ApiResponse.success(createdItems, 200);
    }

    /**
     * Batch save with explicit paymentId - Xóa hết và tạo mới (Sync)
     */
    public ApiResponse<List<PaymentItem>> saveAllPaymentItems(int paymentId, List<PaymentItem> items) {
        // ✅ NEW LOGIC: Check if items already exist before deleting
        ApiResponse<List<PaymentItem>> existingItemsResponse = getPaymentItemsByPaymentId(paymentId);
        boolean hasExistingItems = existingItemsResponse.isSuccess() &&
                existingItemsResponse.getData() != null &&
                !existingItemsResponse.getData().isEmpty();

        // Step 1: Delete existing items (only if they exist)
        if (hasExistingItems) {
            ApiResponse<Boolean> deleteResponse = deletePaymentItemsByPaymentId(paymentId);
            if (!deleteResponse.isSuccess()) {
                return ApiResponse.error("Failed to delete existing items: " + deleteResponse.getErrorMessage());
            }
        }

        // Step 2: Create new items
        List<PaymentItem> createdItems = new ArrayList<>();
        for (PaymentItem item : items) {
            item.setPaymentId(paymentId); // Ensure paymentId is set
            ApiResponse<PaymentItem> createResponse = createPaymentItem(item);
            if (createResponse.isSuccess()) {
                createdItems.add(createResponse.getData());
            } else {
                return ApiResponse.error("Failed to create item: " + createResponse.getErrorMessage());
            }
        }

        return ApiResponse.success(createdItems, 200);
    }

    // ================================
    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)
    // ================================

    /**
     * ASYNC - GET /paymentItems - Lấy tất cả payment items (Async)
     */
    public void getAllPaymentItemsAsync(Consumer<List<PaymentItem>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(PaymentConfig.GET_PAYMENT_ITEM_ENDPOINT,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<PaymentItem> items;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                items = new ArrayList<>();
                            } else {
                                items = gson.fromJson(responseData, new TypeToken<List<PaymentItem>>() {
                                }.getType());
                                if (items == null) {
                                    items = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(items);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payment items list (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải danh sách mục thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tải danh sách mục thanh toán (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - GET /paymentItems?paymentId={id} - Lấy payment items theo payment ID
     * (Async)
     */
    public void getPaymentItemsByPaymentIdAsync(int paymentId, Consumer<List<PaymentItem>> onSuccess,
            Consumer<String> onError) {
        String endpoint = PaymentConfig.GET_PAYMENT_ITEM_ENDPOINT + "?paymentId=" + paymentId;
        apiClient.getAsync(endpoint,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<PaymentItem> items;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                items = new ArrayList<>();
                            } else {
                                items = gson.fromJson(responseData, new TypeToken<List<PaymentItem>>() {
                                }.getType());
                                if (items == null) {
                                    items = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(items);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse payment items by payment ID (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải mục thanh toán");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tải mục thanh toán (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - Batch save payment items (Async)
     */
    public void saveAllPaymentItemsAsync(int paymentId, List<PaymentItem> items,
            Consumer<List<PaymentItem>> onSuccess, Consumer<String> onError) {
        // Delete existing items first
        String deleteEndpoint = PaymentConfig.DELETE_PAYMENT_ITEM_ENDPOINT + "?paymentId=" + paymentId;
        apiClient.deleteAsync(deleteEndpoint,
                deleteResponse -> {
                    if (deleteResponse.isSuccess()) {
                        // Then create new items
                        List<PaymentItem> createdItems = new ArrayList<>();
                        createItemsRecursively(items, 0, paymentId, createdItems, onSuccess, onError);
                    } else {
                        ErrorHandler.showUserFriendlyError(deleteResponse.getStatusCode(),
                                "Không thể xóa các mục thanh toán cũ");
                        onError.accept("Failed to delete existing items: " + deleteResponse.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Xóa các mục thanh toán cũ (async)");
                    onError.accept("Failed to delete existing items: " + error);
                });
    }

    /**
     * Helper method to create items recursively (for async batch creation)
     */
    private void createItemsRecursively(List<PaymentItem> items, int index, int paymentId,
            List<PaymentItem> createdItems, Consumer<List<PaymentItem>> onSuccess, Consumer<String> onError) {
        if (index >= items.size()) {
            // All items created successfully
            onSuccess.accept(createdItems);
            return;
        }

        PaymentItem item = items.get(index);
        item.setPaymentId(paymentId);

        try {
            String jsonBody = gson.toJson(item);
            apiClient.postAsync(PaymentConfig.POST_PAYMENT_ITEM_ENDPOINT, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                PaymentItem createdItem = gson.fromJson(response.getData(), PaymentItem.class);
                                createdItems.add(createdItem);
                                // Create next item
                                createItemsRecursively(items, index + 1, paymentId, createdItems, onSuccess, onError);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse created payment item (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể tạo mục thanh toán");
                            onError.accept("Failed to create item: " + response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error), "Tạo mục thanh toán (async)");
                        onError.accept("Failed to create item: " + error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize payment item (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }
}
