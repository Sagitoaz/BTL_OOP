package org.example.oop.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;

/**
 * 🌐 CUSTOMER RECORD SERVICE - NGÀY 8 CUSTOMER API INTEGRATION
 *
 * Service layer làm cầu nối giữa Frontend và Backend API cho Customer
 * operations
 * Theo pattern của ApiClient với:
 * - Singleton pattern
 * - ApiResponse wrapper cho type safety
 * - Sync và Async methods
 * - JavaFX Platform threading
 * - Error handling chuẩn
 * - JSON serialization/deserialization
 */
public class CustomerRecordService {

    private final ApiClient apiClient;
    private final Gson gson;

    // Singleton instance
    private static CustomerRecordService instance;

    private CustomerRecordService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = GsonProvider.createGson();
    }

    public static synchronized CustomerRecordService getInstance() {
        if (instance == null) {
            instance = new CustomerRecordService();
        }
        return instance;
    }

    // SYNCHRONOUS METHODS (ĐỒNG BỘ)

    /**
     * GET /customers - Lấy tất cả customers (Sync)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public ApiResponse<List<Customer>> getAllCustomers() {
        ApiResponse<String> response = apiClient.get(CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải danh sách khách hàng")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<Customer> customers = gson.fromJson(response.getData(),
                        new TypeToken<List<Customer>>() {
                        }.getType());
                return ApiResponse.success(customers, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse customers list");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách khách hàng");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /customers?searchKey=... - Tìm kiếm customers (Sync)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public ApiResponse<List<Customer>> searchCustomers(String searchKey, Customer.Gender gender,
            LocalDate dateFrom, LocalDate dateTo) {
        String endpoint = buildSearchEndpoint(searchKey, gender, dateFrom, dateTo);
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tìm kiếm khách hàng")) {
                return ApiResponse.error("Empty or invalid response");
            }

            try {
                List<Customer> customers = gson.fromJson(response.getData(),
                        new TypeToken<List<Customer>>() {
                        }.getType());
                return ApiResponse.success(customers, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse search customers");
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tìm kiếm khách hàng");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * POST /customers - Tạo customer mới (Sync)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public ApiResponse<Customer> createCustomer(Customer customer) {
        try {
            String jsonBody = gson.toJson(customer);
            ApiResponse<String> response = apiClient.post(CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT,
                    jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Tạo khách hàng mới")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    Customer createdCustomer = gson.fromJson(response.getData(), Customer.class);
                    return ApiResponse.success(createdCustomer, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created customer");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tạo khách hàng mới");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize customer");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * PUT /customers - Cập nhật customer theo id (Sync)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public ApiResponse<Customer> updateCustomer(Customer customer) {
        if (customer.getId() <= 0) {
            return ApiResponse.error("Customer ID is required for update");
        }

        try {
            String jsonBody = gson.toJson(customer);
            String endpoint = CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customer.getId();
            ApiResponse<String> response = apiClient.put(endpoint, jsonBody);

            if (response.isSuccess()) {
                if (!ErrorHandler.validateResponse(response.getData(), "Cập nhật khách hàng")) {
                    return ApiResponse.error("Empty or invalid response");
                }

                try {
                    Customer updatedCustomer = gson.fromJson(response.getData(), Customer.class);
                    return ApiResponse.success(updatedCustomer, response.getStatusCode());
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated customer");
                    return ApiResponse.error("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể cập nhật khách hàng");
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize customer");
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * DELETE /customers/{id} - Xóa customer (Sync)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public ApiResponse<Boolean> deleteCustomer(int customerId) {
        String endpoint = CustomerAndPrescriptionConfig.DELETE_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customerId;
        ApiResponse<String> response = apiClient.delete(endpoint);

        if (response.isSuccess()) {
            return ApiResponse.success(true, response.getStatusCode());
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể xóa khách hàng");
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)

    /**
     * ASYNC - GET /customers - Lấy tất cả customers (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void getAllCustomersAsync(Consumer<List<Customer>> onSuccess, Consumer<String> onError) {
        System.out.println("API GETTING " + CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT);
        apiClient.getAsync(CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<Customer> customers;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                customers = new ArrayList<>();
                            } else {
                                customers = gson.fromJson(responseData, new TypeToken<List<Customer>>() {
                                }.getType());
                                if (customers == null) {
                                    customers = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(customers);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse customers list (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                "Không thể tải danh sách khách hàng");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tải danh sách khách hàng (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - Tìm kiếm customers theo criteria (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void searchCustomersAsync(String searchKey, Customer.Gender gender,
            LocalDate dateFrom, LocalDate dateTo,
            Consumer<List<Customer>> onSuccess, Consumer<String> onError) {
        String endpoint = buildSearchEndpoint(searchKey, gender, dateFrom, dateTo);

        apiClient.getAsync(endpoint,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            String responseData = response.getData();
                            List<Customer> customers;

                            if (responseData == null || responseData.trim().isEmpty()
                                    || "null".equals(responseData.trim())) {
                                customers = new ArrayList<>();
                            } else {
                                customers = gson.fromJson(responseData, new TypeToken<List<Customer>>() {
                                }.getType());
                                if (customers == null) {
                                    customers = new ArrayList<>();
                                }
                            }

                            onSuccess.accept(customers);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse search customers (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tìm kiếm khách hàng");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tìm kiếm khách hàng (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - Tìm customer theo ID (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void findByIdAsync(int id, Consumer<Optional<Customer>> onSuccess, Consumer<String> onError) {
        String endpoint = CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT + "?id=" + id;

        apiClient.getAsync(endpoint,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            List<Customer> customers = gson.fromJson(response.getData(),
                                    new TypeToken<List<Customer>>() {
                                    }.getType());
                            Optional<Customer> result = customers.isEmpty() ? Optional.empty()
                                    : Optional.of(customers.get(0));
                            onSuccess.accept(result);
                        } catch (Exception e) {
                            ErrorHandler.handleJsonParseError(e, "Parse customer by ID (async)");
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        if (response.getStatusCode() != 404) {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể tải thông tin khách hàng");
                        }
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Tải khách hàng theo ID (async)");
                    onError.accept(error);
                });
    }

    /**
     * ASYNC - Tạo customer mới (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void createCustomerAsync(Customer customer, Consumer<Customer> onSuccess, Consumer<String> onError) {
        try {
            String jsonBody = gson.toJson(customer);

            apiClient.postAsync(CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                Customer createdCustomer = gson.fromJson(response.getData(), Customer.class);
                                onSuccess.accept(createdCustomer);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse created customer (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể tạo khách hàng mới");
                            onError.accept(response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error), "Tạo khách hàng (async)");
                        onError.accept(error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize customer (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    /**
     * ASYNC - Cập nhật customer (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void updateCustomerAsync(Customer customer, Consumer<Customer> onSuccess, Consumer<String> onError) {
        if (customer.getId() <= 0) {
            Platform.runLater(() -> onError.accept("Customer ID is required for update"));
            return;
        }

        try {
            String jsonBody = gson.toJson(customer);
            String endpoint = CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT
                    + "?id=" + customer.getId();

            apiClient.putAsync(endpoint, jsonBody,
                    response -> {
                        if (response.isSuccess()) {
                            try {
                                Customer updatedCustomer = gson.fromJson(response.getData(), Customer.class);
                                onSuccess.accept(updatedCustomer);
                            } catch (Exception e) {
                                ErrorHandler.handleJsonParseError(e, "Parse updated customer (async)");
                                onError.accept("JSON parse error: " + e.getMessage());
                            }
                        } else {
                            ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                                    "Không thể cập nhật khách hàng");
                            onError.accept(response.getErrorMessage());
                        }
                    },
                    error -> {
                        ErrorHandler.handleConnectionError(new Exception(error), "Cập nhật khách hàng (async)");
                        onError.accept(error);
                    });
        } catch (Exception e) {
            ErrorHandler.handleJsonParseError(e, "Serialize customer (async)");
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    /**
     * ASYNC - Xóa customer (Async)
     * ✅ Updated với ErrorHandler framework (Ngày 4)
     */
    public void deleteCustomerAsync(int customerId, Consumer<Boolean> onSuccess, Consumer<String> onError) {
        String endpoint = CustomerAndPrescriptionConfig.DELETE_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customerId;

        apiClient.deleteAsync(endpoint,
                response -> {
                    if (response.isSuccess()) {
                        onSuccess.accept(true);
                    } else {
                        ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể xóa khách hàng");
                        onError.accept(response.getErrorMessage());
                    }
                },
                error -> {
                    ErrorHandler.handleConnectionError(new Exception(error), "Xóa khách hàng (async)");
                    onError.accept(error);
                });
    }
    // UTILITY METHODS (PHƯƠNG THỨC HỖ TRỢ)

    /**
     * Kiểm tra kết nối server (Async)
     */
    public void checkServerConnection(Consumer<Boolean> onResult) {
        apiClient.getAsync("/health", // hoặc endpoint khác để test
                response -> onResult.accept(response.isSuccess()),
                error -> onResult.accept(false));
    }

    /**
     * Xây dựng endpoint tìm kiếm với query parameters
     */
    private String buildSearchEndpoint(String searchKey, Customer.Gender gender,
            LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder endpoint = new StringBuilder(CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT);
        boolean hasParams = false;

        if (searchKey != null && !searchKey.trim().isEmpty()) {
            endpoint.append(hasParams ? "&" : "?").append("searchKey=").append(searchKey.trim());
            hasParams = true;
        }

        if (gender != null) {
            endpoint.append(hasParams ? "&" : "?").append("gender=").append(gender.name());
            hasParams = true;
        }

        if (dateFrom != null) {
            endpoint.append(hasParams ? "&" : "?").append("dateFrom=").append(dateFrom.toString());
            hasParams = true;
        }

        if (dateTo != null) {
            endpoint.append(hasParams ? "&" : "?").append("dateTo=").append(dateTo.toString());
            hasParams = true;
        }

        return endpoint.toString();
    }
}
