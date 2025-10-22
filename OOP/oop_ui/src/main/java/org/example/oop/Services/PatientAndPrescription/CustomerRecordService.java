package org.example.oop.Services.PatientAndPrescription;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.util.CustomerConfig;

/**
 * 🌐 CUSTOMER RECORD SERVICE - NGÀY 8 CUSTOMER API INTEGRATION
 *
 * Service layer làm cầu nối giữa Frontend và Backend API cho Customer operations
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


    // ================================
    // SYNCHRONOUS METHODS (ĐỒNG BỘ)
    // ================================

    /**
     * GET /customers - Lấy tất cả customers (Sync)
     */
    public ApiResponse<List<Customer>> getAllCustomers() {
        ApiResponse<String> response = apiClient.get(CustomerConfig.GET_CUSTOMER_ENDPOINT);

        if (response.isSuccess()) {
            try {
                List<Customer
                        > customers = gson.fromJson(response.getData(),
                    new TypeToken<List<Customer>>(){}.getType());
                return ApiResponse.success(customers, response.getStatusCode());
            } catch (Exception e) {
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * GET /customers?searchKey=... - Tìm kiếm customers (Sync)
     */
    public ApiResponse<List<Customer>> searchCustomers(String searchKey, Customer.Gender
                                                               gender,
                                                     LocalDate dateFrom, LocalDate dateTo) {
        String endpoint = buildSearchEndpoint(searchKey, gender, dateFrom, dateTo);
        ApiResponse<String> response = apiClient.get(endpoint);

        if (response.isSuccess()) {
            try {
                List<Customer> customers = gson.fromJson(response.getData(),
                    new TypeToken<List<Customer>>(){}.getType());
                return ApiResponse.success(customers, response.getStatusCode());
            } catch (Exception e) {
                return ApiResponse.error("JSON parse error: " + e.getMessage());
            }
        } else {
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    /**
     * POST /customers - Tạo customer mới (Sync)
     */
    public ApiResponse<Customer> createCustomer(Customer customer) {
        try {
            String jsonBody = gson.toJson(customer);
            ApiResponse<String> response = apiClient.post(CustomerConfig.POST_CUSTOMER_ENDPOINT, jsonBody);

            if (response.isSuccess()) {
                Customer createdCustomer = gson.fromJson(response.getData(), Customer.class);
                return ApiResponse.success(createdCustomer, response.getStatusCode());
            } else {
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    // update theo id
    public ApiResponse<Customer> updateCustomer(Customer customer) {
        if (customer.getId() <= 0) {
            return ApiResponse.error("Customer ID is required for update");
        }

        try {
            String jsonBody = gson.toJson(customer);
            String endpoint = CustomerConfig.PUT_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customer.getId();
            ApiResponse<String> response = apiClient.put(endpoint, jsonBody);

            if (response.isSuccess()) {
                Customer updatedCustomer = gson.fromJson(response.getData(), Customer.class);
                return ApiResponse.success(updatedCustomer, response.getStatusCode());
            } else {
                return ApiResponse.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("JSON serialization error: " + e.getMessage());
        }
    }

    /**
     * DELETE /customers/{id} - Xóa customer (Sync)
     */
    public ApiResponse<Boolean> deleteCustomer(int customerId) {
        String endpoint = CustomerConfig.DELETE_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customerId;
        ApiResponse<String> response = apiClient.delete(endpoint);

        if (response.isSuccess()) {
            return ApiResponse.success(true, response.getStatusCode());
        } else {
            return ApiResponse.error(response.getErrorMessage());
        }
    }

    // ================================
    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)
    // ================================

    /**
     * ASYNC - GET /customers - Lấy tất cả customers (Async)
     */
    public void getAllCustomersAsync(Consumer<List<Customer>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(CustomerConfig.GET_CUSTOMER_ENDPOINT,
            response -> {
                if (response.isSuccess()) {
                    try {
                        String responseData = response.getData();
                        List<Customer> customers;

                        // Kiểm tra response data trước khi parse
                        if (responseData == null || responseData.trim().isEmpty() || "null".equals(responseData.trim())) {
                            // Nếu response là null hoặc empty, trả về empty list
                            customers = new ArrayList<>();
                        } else {
                            customers = gson.fromJson(responseData, new TypeToken<List<Customer>>(){}.getType());
                            // Double check nếu gson trả về null
                            if (customers == null) {
                                customers = new ArrayList<>();
                            }
                        }

                        System.out.println("📋 Loaded " + customers.size() + " customers");
                        onSuccess.accept(customers);
                    } catch (Exception e) {
                        System.err.println("❌ JSON parse error in getAllCustomers: " + e.getMessage());
                        onError.accept("JSON parse error: " + e.getMessage());
                    }
                } else {
                    onError.accept(response.getErrorMessage());
                }
            },
            onError
        );
    }

    /**
     * ASYNC - Tìm kiếm customers theo criteria (Async)
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

                        // Kiểm tra response data trước khi parse
                        if (responseData == null || responseData.trim().isEmpty() || "null".equals(responseData.trim())) {
                            // Nếu response là null hoặc empty, trả về empty list
                            customers = new ArrayList<>();
                        } else {
                            customers = gson.fromJson(responseData, new TypeToken<List<Customer>>(){}.getType());
                            // Double check nếu gson trả về null
                            if (customers == null) {
                                customers = new ArrayList<>();
                            }
                        }

                        System.out.println("🔍 Search found " + customers.size() + " customers");
                        onSuccess.accept(customers);
                    } catch (Exception e) {
                        System.err.println("❌ JSON parse error in search: " + e.getMessage());
                        onError.accept("JSON parse error: " + e.getMessage());
                    }
                } else {
                    onError.accept(response.getErrorMessage());
                }
            },
            onError
        );
    }

    /**
     * ASYNC - Tìm customer theo ID (Async)
     */
    public void findByIdAsync(int id, Consumer<Optional<Customer>> onSuccess, Consumer<String> onError) {
        String endpoint = CustomerConfig.GET_CUSTOMER_ENDPOINT + "?id=" + id;

        apiClient.getAsync(endpoint,
            response -> {
                if (response.isSuccess()) {
                    try {
                        List<Customer> customers = gson.fromJson(response.getData(),
                            new TypeToken<List<Customer>>(){}.getType());
                        Optional<Customer> result = customers.isEmpty() ?
                            Optional.empty() : Optional.of(customers.get(0));
                        onSuccess.accept(result);
                    } catch (Exception e) {
                        onError.accept("JSON parse error: " + e.getMessage());
                    }
                } else {
                    onError.accept(response.getErrorMessage());
                }
            },
            onError
        );
    }

    /**
     * ASYNC - Tạo customer mới (Async)
     */
    public void createCustomerAsync(Customer customer, Consumer<Customer> onSuccess, Consumer<String> onError) {
        try {
            String jsonBody = gson.toJson(customer);

            apiClient.postAsync(CustomerConfig.POST_CUSTOMER_ENDPOINT, jsonBody,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            Customer createdCustomer = gson.fromJson(response.getData(), Customer.class);
                            onSuccess.accept(createdCustomer);
                        } catch (Exception e) {
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        onError.accept(response.getErrorMessage());
                    }
                },
                onError
            );
        } catch (Exception e) {
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    /**
     * ASYNC - Cập nhật customer (Async)
     */
    public void updateCustomerAsync(Customer customer, Consumer<Customer> onSuccess, Consumer<String> onError) {
        if (customer.getId() <= 0) {
            Platform.runLater(() -> onError.accept("Customer ID is required for update"));
            return;
        }

        try {
            String jsonBody = gson.toJson(customer);
            String endpoint = CustomerConfig.PUT_CUSTOMER_BY_ID_ENDPOINT
                    + "?id=" + customer.getId();

            apiClient.putAsync(endpoint, jsonBody,
                response -> {
                    if (response.isSuccess()) {
                        try {
                            Customer updatedCustomer = gson.fromJson(response.getData(), Customer.class);
                            onSuccess.accept(updatedCustomer);
                        } catch (Exception e) {
                            onError.accept("JSON parse error: " + e.getMessage());
                        }
                    } else {
                        onError.accept(response.getErrorMessage());
                    }
                },
                onError
            );
        } catch (Exception e) {
            Platform.runLater(() -> onError.accept("JSON serialization error: " + e.getMessage()));
        }
    }

    /**
     * ASYNC - Xóa customer (Async)
     */
    public void deleteCustomerAsync(int customerId, Consumer<Boolean> onSuccess, Consumer<String> onError) {
        String endpoint = CustomerConfig.DELETE_CUSTOMER_BY_ID_ENDPOINT + "?id=" + customerId;

        apiClient.deleteAsync(endpoint,
            response -> {
                if (response.isSuccess()) {
                    onSuccess.accept(true);
                } else {
                    onError.accept(response.getErrorMessage());
                }
            },
            onError
        );
    }
    // ================================
    // UTILITY METHODS (PHƯƠNG THỨC HỖ TRỢ)
    // ================================

    /**
     * Kiểm tra kết nối server (Async)
     */
    public void checkServerConnection(Consumer<Boolean> onResult) {
        apiClient.getAsync("/health", // hoặc endpoint khác để test
            response -> onResult.accept(response.isSuccess()),
            error -> onResult.accept(false)
        );
    }
    /**
     * Xây dựng endpoint tìm kiếm với query parameters
     */
    private String buildSearchEndpoint(String searchKey, Customer.Gender gender,
                                     LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder endpoint = new StringBuilder(CustomerConfig.GET_CUSTOMER_ENDPOINT);
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
