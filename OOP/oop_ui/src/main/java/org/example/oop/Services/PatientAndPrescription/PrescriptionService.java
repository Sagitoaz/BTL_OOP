package org.example.oop.Services.PatientAndPrescription;

import java.util.Arrays;
import java.util.List;

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

}
