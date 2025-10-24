package org.example.oop.Services.PatientAndPrescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.example.oop.Utils.ApiClient;
import org.example.oop.Utils.ApiResponse;
import com.google.gson.Gson;
import org.example.oop.Utils.GsonProvider;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;

public class PrescriptionService {
    private final ApiClient apiClient;
    private final Gson gson;
    public PrescriptionService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = GsonProvider.getGson();
    }

    public ApiResponse<List<Prescription>> getAllPrescriptions() {
        ApiResponse<String> response = apiClient.get(CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT);
        if(response.isSuccess()) {
            try{
                String jsonBody = response.getData();
                Prescription[] prescriptionsArray = gson.fromJson(jsonBody, Prescription[].class);
                List<Prescription> prescriptions = Arrays.asList(prescriptionsArray);
                return ApiResponse.success(prescriptions, response.getStatusCode());
            }
            catch (Exception e){
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(), response.getStatusCode());
            }

        }
        else{
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(), response.getStatusCode());
        }

    }
    public ApiResponse<List<Prescription>> getPrescriptionByCustomer_id(int customer_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT+"?customer_id=" + customer_id;
        ApiResponse<String> response = apiClient.get(endpoint);
        if(response.isSuccess()) {
            try{
                String jsonBody = response.getData();
                Prescription[] prescriptionsArray = gson.fromJson(jsonBody, Prescription[].class);
                List<Prescription> prescriptions = Arrays.asList(prescriptionsArray);
                return ApiResponse.success(prescriptions, response.getStatusCode());
            }
            catch (Exception e){
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(), response.getStatusCode());
            }

        }
        else{
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(), response.getStatusCode());
        }
    }
    public ApiResponse<Prescription> getPrescriptionByAppointment_id(int appointment_id) {
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT+"?appointment_id=" + appointment_id;
        ApiResponse<String> response = apiClient.get(endpoint);
        if(response.isSuccess()) {
            try{
                String jsonBody = response.getData();
                Prescription prescription = gson.fromJson(jsonBody, Prescription.class);

                return ApiResponse.success(prescription, response.getStatusCode());
            }
            catch (Exception e){
                return ApiResponse.error("Error parsing prescriptions data: " + e.getMessage(), response.getStatusCode());
            }

        }
        else{
            return ApiResponse.error("Failed to fetch prescriptions: " + response.getErrorMessage(), response.getStatusCode());
        }
    }
    public ApiResponse<Prescription> createPrescription(Prescription prescription) {
        String jsonBody = gson.toJson(prescription);
        ApiResponse<String> response = apiClient.post(CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT, jsonBody);
        if(response.isSuccess()){
            try{
                Prescription createdPrescription = gson.fromJson(response.getData(), Prescription.class);
                return ApiResponse.success(createdPrescription, response.getStatusCode());
            }
            catch (Exception e){
                return ApiResponse.error("Error parsing created prescription data: " + e.getMessage(), response.getStatusCode());
            }
        }
        else{
            return ApiResponse.error("Failed to create prescription: " + response.getErrorMessage(), response.getStatusCode());
        }
    }
    public ApiResponse<Prescription> updatePrescription(Prescription prescription) {
        String jsonBody = gson.toJson(prescription);
        ApiResponse<String> response = apiClient.put(CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT, jsonBody);
        if(response.isSuccess()){
            try{
                Prescription updatedPrescription = gson.fromJson(response.getData(), Prescription.class);
                return ApiResponse.success(updatedPrescription, response.getStatusCode());
            }
            catch (Exception e){
                return ApiResponse.error("Error parsing updated prescription data: " + e.getMessage(), response.getStatusCode());
            }
        }
        else{
            return ApiResponse.error("Failed to update prescription: " + response.getErrorMessage(), response.getStatusCode());
        }
    }

    // Async methods
    public void getAllPrescriptionAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError){
        apiClient.getAsync(CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT, response ->{
            if(response.isSuccess()){
                try{
                    String responseData = response.getData();
                    List<Prescription> prescriptions;
                    if(responseData == null || responseData.trim().isEmpty() || responseData.equals("null")){
                        prescriptions = new ArrayList<>();
                    }
                    else{
                        Prescription[] prescriptionsArray = gson.fromJson(responseData, Prescription[].class);
                        if(prescriptionsArray == null) prescriptions = new ArrayList<>();
                        else{
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                        System.out.println("📋 Loaded " + prescriptions.size() + " prescriptions");
                        onSuccess.accept(prescriptions);

                    }
                }
                catch (Exception e){
                    System.err.println("❌ JSON parse error in getAllPrescription: " + e.getMessage());
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            }
            else{
                onError.accept(response.getErrorMessage());
            }
        }, onError);
    }
    public void getPrescriptionByCustomer_idAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError, int customer_id){
        String endpoint = CustomerAndPrescriptionConfig.GET_PRESCRIPTION_ENDPOINT+"?customer_id=" + customer_id;
        apiClient.getAsync(endpoint, response ->{
            if(response.isSuccess()){
                try{
                    String responseData = response.getData();
                    List<Prescription> prescriptions;
                    if(responseData == null || responseData.trim().isEmpty() || responseData.equals("null")){
                        prescriptions = new ArrayList<>();
                    }
                    else{
                        Prescription[] prescriptionsArray = gson.fromJson(responseData, Prescription[].class);
                        if(prescriptionsArray == null) prescriptions = new ArrayList<>();
                        else{
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                        System.out.println("📋 Loaded " + prescriptions.size() + " prescriptions for customer_id " + customer_id);
                        onSuccess.accept(prescriptions);
                    }

                }
                catch (Exception e){
                    System.err.println("❌ JSON parse error in getPrescriptionByCustomer_id: " + e.getMessage());
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            }
            else{
                onError.accept(response.getErrorMessage());
            }
        }, onError);
    }
    public void getPrescriptionByAppointment_idAsync(Consumer<List<Prescription>> onSuccess, Consumer<String> onError, int appointment_id) {
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
                        if (prescriptionsArray == null) prescriptions = new ArrayList<>();
                        else {
                            prescriptions = Arrays.asList(prescriptionsArray);
                        }
                        System.out.println("📋 Loaded " + prescriptions.size() + " prescriptions for appointment_id " + appointment_id);
                        onSuccess.accept(prescriptions);
                    }

                } catch (Exception e) {
                    System.err.println("❌ JSON parse error in getPrescriptionByAppointment_id: " + e.getMessage());
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            } else {
                onError.accept(response.getErrorMessage());
            }
        }, onError);
    }
    public void createPrescriptionAsync(Consumer<Prescription> onSuccess, Consumer<String> onError, Prescription prescription) {
        String endpoint = CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT;
        String jsonBody = gson.toJson(prescription);
        apiClient.postAsync(endpoint, jsonBody, response ->{
            if(response.isSuccess()){
                try{
                    String responseData = response.getData();
                    Prescription createdPrescription = gson.fromJson(responseData, Prescription.class);
                    System.out.println("✅ Created prescription with ID " + createdPrescription.getId());
                    onSuccess.accept(createdPrescription);
                }
                catch (Exception e){
                    System.err.println("❌ JSON parse error in createPrescription: " + e.getMessage());
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            }
            else{
                onError.accept(response.getErrorMessage());
            }
        }, onError);

    }
    public void updatePrescriptionAsync(Consumer<Prescription> onSuccess, Consumer<String> onError, Prescription prescription, int id) {
        String endpoint = CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT + "?id=" +id;
        String jsonBody = gson.toJson(prescription);
        apiClient.putAsync(endpoint, jsonBody, response->{
            if(response.isSuccess()){
                try{
                    String responseData = response.getData();
                    Prescription updatedPrescription = gson.fromJson(responseData, Prescription.class);
                    System.out.println("✅ Updated prescription with ID " + updatedPrescription.getId());
                    onSuccess.accept(updatedPrescription);
                }
                catch (Exception e){
                    System.err.println("❌ JSON parse error in updatedPrescription: " + e.getMessage());
                    onError.accept("JSON parse error: " + e.getMessage());
                }
            }
            else{
                onError.accept(response.getErrorMessage());
            }
        }, onError);

    }
}
