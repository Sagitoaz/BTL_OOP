package org.miniboot.app.controllers.PatientAndPrescription;
import com.google.gson.Gson;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;
import org.miniboot.app.util.GsonProvider;
import org.miniboot.app.util.Json;
import org.miniboot.app.util.errorvalidation.CustomerValidator;
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.RateLimiter;
import org.miniboot.app.util.errorvalidation.ValidationUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CustomerRecordController {
    private final CustomerRecordRepository customerRecordRepository;


    public CustomerRecordController(CustomerRecordRepository customerRecordRepository) {
        this.customerRecordRepository = customerRecordRepository;

    }

    public static void mount(org.miniboot.app.router.Router router, CustomerRecordController prc) {
        router.get(CustomerAndPrescriptionConfig.GET_CUSTOMER_ENDPOINT, prc.getCustomer());
        router.post(CustomerAndPrescriptionConfig.POST_CUSTOMER_ENDPOINT, prc.createCustomer());
        router.put(CustomerAndPrescriptionConfig.PUT_CUSTOMER_BY_ID_ENDPOINT, prc.updateCustomer());
        router.delete(CustomerAndPrescriptionConfig.DELETE_CUSTOMER_BY_ID_ENDPOINT, prc.deleteCustomer());

    }

    public Function<HttpRequest, HttpResponse> createCustomer() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;

            try {
                // Step 4: Parse JSON
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                Customer customerToCreate = gson.fromJson(jsonBody, Customer.class);

                // Step 5: Full customer validation (required fields + business rules + duplicates)
                HttpResponse customerError = CustomerValidator.validateForCreate(customerToCreate, customerRecordRepository);
                if (customerError != null) return customerError;

                // Step 6: Save customer
                Customer savedCustomer;
                try {
                    savedCustomer = customerRecordRepository.save(customerToCreate);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 7: Return success response
                if (savedCustomer != null && savedCustomer.getId() > 0) {
                    String jsonResponse = gson.toJson(savedCustomer);
                    return HttpResponse.of(201, "application/json",
                            jsonResponse.getBytes(StandardCharsets.UTF_8));
                } else {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to create customer");
                }

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in createCustomer: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }
    public Function<HttpRequest, HttpResponse> getCustomer() {
        return (HttpRequest req) -> {

            Optional<String> searchKey = extractFirst(req.query, "searchKey");
            Optional<String> gender = extractFirst(req.query, "gender");
            Optional<LocalDate> dateFrom = extractFirst(req.query, "dateFrom")
                    .map(s -> {
                        try {
                            return LocalDate.parse(s);
                        } catch (Exception e) {
                            return null;
                        }});
            Optional<LocalDate> dateTo = extractFirst(req.query, "dateTo")
                    .map(s ->{
                        try{
                            return LocalDate.parse(s);
                        }
                        catch (Exception e){
                            return null;
                        }
                    });

            // Convert string gender to Gender enum trong CustomerSearchCriteria
            Customer.Gender genderEnum = null;
            System.out.println(gender);
            if (gender.isPresent()) {
                try {
                    genderEnum = Customer.Gender.valueOf(gender.get());
                } catch (IllegalArgumentException e) {
                    genderEnum = null;
                }
            }

            CustomerSearchCriteria criteria = new CustomerSearchCriteria(searchKey.orElse(null), genderEnum, dateFrom.orElse(null), dateTo.orElse(null));
            if(criteria.isEmpty()){

                return Json.ok(customerRecordRepository.findAll());
            }
            else{

                try {
                    List<Customer> results = customerRecordRepository.findByFilterAll(criteria);

                    return Json.ok(results);
                } catch (Exception e) {
                    return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST,
                            HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                            HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
                }
            }



        };
    }

    public Function<HttpRequest, HttpResponse> updateCustomer() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;

            try {
                // Step 4: Parse JSON
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                Customer customerToUpdate = gson.fromJson(jsonBody, Customer.class);

                // Step 5: Check if customer exists
                HttpResponse existsError = CustomerValidator.checkExists(customerRecordRepository, customerToUpdate.getId());
                if (existsError != null) return existsError;

                // Step 6: Full customer validation (with ID exclusion for duplicates)
                HttpResponse customerError = CustomerValidator.validateForUpdate(customerToUpdate, customerRecordRepository);
                if (customerError != null) return customerError;

                // Step 7: Save customer
                Customer savedCustomer;
                try {
                    savedCustomer = customerRecordRepository.save(customerToUpdate);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 8: Return success response
                if (savedCustomer != null && savedCustomer.getId() > 0) {
                    String jsonResponse = gson.toJson(savedCustomer);
                    return HttpResponse.of(201, "application/json",
                            jsonResponse.getBytes(StandardCharsets.UTF_8));
                } else {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to create customer");
                }

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in createCustomer: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }
    public Function<HttpRequest, HttpResponse> deleteCustomer() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Step 1-3: Standard validations (Content-Type, JWT, Role)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;

            try {
                // Step 4: Parse JSON
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                Customer deletedCustomer = gson.fromJson(jsonBody, Customer.class);

                // Step 5: Validate ID
                if (deletedCustomer == null || deletedCustomer.getId() <= 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Valid customer ID is required");
                }

                // Step 6: Check if customer exists
                HttpResponse existsError = CustomerValidator.checkExists(customerRecordRepository, deletedCustomer.getId());
                if (existsError != null) return existsError;

                // Step 7: Delete customer
                boolean deleted;
                try {
                    deleted = customerRecordRepository.deleteById(deletedCustomer.getId());
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 8: Return success response
                if (deleted) {
                    String jsonResponse = gson.toJson(deletedCustomer);
                    return HttpResponse.of(200, "application/json", 
                            jsonResponse.getBytes(StandardCharsets.UTF_8));
                } else {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Failed to delete customer");
                }

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in deleteCustomer: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
            }
        };
    }

    private Optional<Integer> extractInt(Map<String, List<String>> q, String key) {
        Optional<String> s = extractFirst(q, key);
        if (s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s.get()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    private Optional<String> extractFirst(Map<String, List<String>> q, String key) {
        if (q == null) return Optional.empty();
        List<String> vals = q.get(key);
        if (vals == null || vals.isEmpty()) return Optional.empty();
        String first = vals.get(0);
        return (first == null || first.isBlank()) ? Optional.empty() : Optional.of(first);
    }




}
