package org.example.oop.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.HttpException;
import com.google.gson.Gson;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;

/**
 * Prescription Service - ✅ Updated với ErrorHandler framework (Ngày 4)
 */
public class PrescriptionService {
    private final ApiClient apiClient;
    private final Gson gson;

    public PrescriptionService() {
        this.apiClient = ApiClient.getInstance();
        this.gson = GsonProvider.getGson();
    }

    /**
     * GET /prescriptions - Lấy tất cả prescriptions (Sync)
     */
    public ApiResponse<List<Prescription>> getAllPrescriptions() {
        ApiResponse<String> response = apiClient.get(CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT);
        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải danh sách đơn thuốc")) {
                return ApiResponse.error("Empty or invalid response", response.getStatusCode());
            }

            try {
                String jsonBody = response.getData();
                Prescription[] prescriptionsArray = gson.fromJson(jsonBody, Prescription[].class);
                List<Prescription> prescriptions = Arrays.asList(prescriptionsArray);
                return ApiResponse.success(prescriptions, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse prescriptions list");
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(),
                        response.getStatusCode());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách đơn thuốc");
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(),
                    response.getStatusCode());
        }
    }

    /**
     * GET /prescriptions?customer_id= - Lấy prescriptions theo customer ID
     */
    public ApiResponse<List<Prescription>> getPrescriptionByCustomer_id(int customer_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT + "?customer_id=" + customer_id;
        ApiResponse<String> response = apiClient.get(endpoint);
        if (response.isSuccess()) {
            if (!ErrorHandler.validateResponse(response.getData(), "Tải đơn thuốc của khách hàng")) {
                return ApiResponse.error("Empty or invalid response", response.getStatusCode());
            }

            try {
                String jsonBody = response.getData();
                Prescription[] prescriptionsArray = gson.fromJson(jsonBody, Prescription[].class);
                List<Prescription> prescriptions = Arrays.asList(prescriptionsArray);
                return ApiResponse.success(prescriptions, response.getStatusCode());
            } catch (Exception e) {
                ErrorHandler.handleJsonParseError(e, "Parse prescriptions by customer");
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(),
                        response.getStatusCode());
            }
        } else {
            ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải đơn thuốc của khách hàng");
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(),
                    response.getStatusCode());
        }
    }

    public ApiResponse<Prescription> getPrescriptionByAppointment_id(int appointment_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT + "?appointment_id=" + appointment_id;
        ApiResponse<String> response = apiClient.get(endpoint);
        if (response.isSuccess()) {
            try {
                String jsonBody = response.getData();
                Prescription prescription = gson.fromJson(jsonBody, Prescription.class);

                return ApiResponse.success(prescription, response.getStatusCode());
            } catch (Exception e) {
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(),
                        response.getStatusCode());
            }

        } else {
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(),
                    response.getStatusCode());
        }
    }

    public ApiResponse<Prescription> createPrescription(Prescription prescription) {
        String jsonBody = gson.toJson(prescription);
        ApiResponse<String> response = apiClient.post(CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT, jsonBody);
        if (response.isSuccess()) {
            try {
                Prescription createdPrescription = gson.fromJson(response.getData(), Prescription.class);
                return ApiResponse.success(createdPrescription, response.getStatusCode());
            } catch (Exception e) {
                return ApiResponse.error("Error parsing created prescription data: " + e.getMessage(),
                        response.getStatusCode());
            }
        } else {
            return ApiResponse.error("Failed to create prescription: " + response.getErrorMessage(),
                    response.getStatusCode());
        }
    }

    public ApiResponse<Prescription> updatePrescription(Prescription prescription) {
        String jsonBody = gson.toJson(prescription);
        ApiResponse<String> response = apiClient.put(CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT,
                jsonBody);
        if (response.isSuccess()) {
            try {
                Prescription updatedPrescription = gson.fromJson(response.getData(), Prescription.class);
                return ApiResponse.success(updatedPrescription, response.getStatusCode());
            } catch (Exception e) {
                return ApiResponse.error("Error parsing updated prescription data: " + e.getMessage(),
                        response.getStatusCode());
            }
        } else {
            return ApiResponse.error("Failed to update prescription: " + response.getErrorMessage(),
                    response.getStatusCode());
        }
    }

    // ================================
    // ASYNCHRONOUS METHODS (BẤT ĐỒNG BỘ)
    // ✅ Updated với ErrorHandler framework (Ngày 4)
    // ================================

    /**
     * ASYNC - GET /prescriptions - Lấy tất cả prescriptions (Async)
     */
    public void getAllPrescriptionAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError) {
        apiClient.getAsync(CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT, response -> {
            if (response.isSuccess()) {
                try {
                    String responseData = response.getData();
                    List<Prescription> prescriptions;
                    if (responseData == null || responseData.trim().isEmpty() || responseData.equals("null")) {
                        prescriptions = new ArrayList<>();
                    } else {
                        Prescription[] prescriptionsArray = gson.fromJson(responseData, Prescription[].class);
                        if (prescriptionsArray == null)
                            prescriptions = new ArrayList<>();
                        else {
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                    }
                    onSuccess.accept(prescriptions);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse prescriptions list (async)");
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tải danh sách đơn thuốc");
                onError.accept(response.getErrorMessage());
            }
        }, error -> {
            ErrorHandler.handleConnectionError(new Exception(error), "Tải danh sách đơn thuốc (async)");
            onError.accept(error);
        });
    }

    /**
     * ASYNC - GET /prescriptions?customer_id={id} - Lấy prescriptions theo
     * customer_id (Async)
     */
    public void getPrescriptionByCustomer_idAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError,
            int customer_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT + "?customer_id=" + customer_id;
        apiClient.getAsync(endpoint, response -> {
            if (response.isSuccess()) {
                try {
                    String responseData = response.getData();
                    List<Prescription> prescriptions;
                    if (responseData == null || responseData.trim().isEmpty() || responseData.equals("null")) {
                        prescriptions = new ArrayList<>();
                    } else {
                        Prescription[] prescriptionsArray = gson.fromJson(responseData, Prescription[].class);
                        if (prescriptionsArray == null)
                            prescriptions = new ArrayList<>();
                        else {
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                    }
                    onSuccess.accept(prescriptions);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse prescriptions by customer_id (async)");
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                        "Không thể tải đơn thuốc của khách hàng ID: " + customer_id);
                onError.accept(response.getErrorMessage());
            }
        }, error -> {
            ErrorHandler.handleConnectionError(new Exception(error), "Tải đơn thuốc theo customer_id (async)");
            onError.accept(error);
        });
    }

    /**
     * ASYNC - GET /prescriptions?appointment_id={id} - Lấy prescriptions theo
     * appointment_id (Async)
     */
    public void getPrescriptionByAppointment_idAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError,
            int appointment_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT + "?appointment_id=" + appointment_id;
        apiClient.getAsync(endpoint, response -> {
            if (response.isSuccess()) {
                try {
                    String responseData = response.getData();
                    List<Prescription> prescriptions;
                    if (responseData == null || responseData.trim().isEmpty() || responseData.equals("null")) {
                        prescriptions = new ArrayList<>();
                    } else {
                        Prescription[] prescriptionsArray = gson.fromJson(responseData, Prescription[].class);
                        if (prescriptionsArray == null)
                            prescriptions = new ArrayList<>();
                        else {
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                    }
                    onSuccess.accept(prescriptions);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse prescriptions by appointment_id (async)");
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(),
                        "Không thể tải đơn thuốc của lịch hẹn ID: " + appointment_id);
                onError.accept(response.getErrorMessage());
            }
        }, error -> {
            ErrorHandler.handleConnectionError(new Exception(error), "Tải đơn thuốc theo appointment_id (async)");
            onError.accept(error);
        });
    }

    /**
     * ASYNC - POST /prescriptions - Tạo prescription mới (Async)
     */
    public void createPrescriptionAsync(Consumer<Prescription> onSuccess, Consumer<String> onError,
            Prescription prescription) {
        String endpoint = CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT;
        String jsonBody = gson.toJson(prescription);
        apiClient.postAsync(endpoint, jsonBody, response -> {
            if (response.isSuccess()) {
                try {
                    String responseData = response.getData();
                    Prescription createdPrescription = gson.fromJson(responseData, Prescription.class);
                    onSuccess.accept(createdPrescription);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse created prescription (async)");
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể tạo đơn thuốc mới");
                onError.accept(response.getErrorMessage());
            }
        }, error -> {
            ErrorHandler.handleConnectionError(new Exception(error), "Tạo đơn thuốc mới (async)");
            onError.accept(error);
        });
    }

    /**
     * ASYNC - PUT /prescriptions/{id} - Cập nhật prescription (Async)
     */
    public void updatePrescriptionAsync(Consumer<Prescription> onSuccess, Consumer<String> onError,
            Prescription prescription, int id) {
        String endpoint = CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT + "?id=" + id;
        String jsonBody = gson.toJson(prescription);
        apiClient.putAsync(endpoint, jsonBody, response -> {
            if (response.isSuccess()) {
                try {
                    String responseData = response.getData();
                    Prescription updatedPrescription = gson.fromJson(responseData, Prescription.class);
                    onSuccess.accept(updatedPrescription);
                } catch (Exception e) {
                    ErrorHandler.handleJsonParseError(e, "Parse updated prescription (async)");
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                ErrorHandler.showUserFriendlyError(response.getStatusCode(), "Không thể cập nhật đơn thuốc ID: " + id);
                onError.accept(response.getErrorMessage());
            }
        }, error -> {
            ErrorHandler.handleConnectionError(new Exception(error), "Cập nhật đơn thuốc (async)");
            onError.accept(error);
        });
    }
}
