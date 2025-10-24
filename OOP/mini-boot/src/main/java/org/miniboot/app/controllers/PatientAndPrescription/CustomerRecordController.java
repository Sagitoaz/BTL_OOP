package org.miniboot.app.controllers.PatientAndPrescription;
import com.google.gson.Gson;

import org.miniboot.app.AppConfig;
import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;
import org.miniboot.app.util.GsonProvider;
import org.miniboot.app.util.Json;

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
            try {
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                Customer customerToCreate = gson.fromJson(jsonBody, Customer.class);

                System.out.println("🔄 Attempting to create customer: " + customerToCreate.getFirstname() + " " + customerToCreate.getLastname());

                // Gọi repository save - có thể throw RuntimeException
                Customer savedCustomer = customerRecordRepository.save(customerToCreate);

                if (savedCustomer != null && savedCustomer.getId() > 0) {
                    String jsonResponse = gson.toJson(savedCustomer);
                    System.out.println("✅ Customer created successfully with ID: " + savedCustomer.getId());
                    return HttpResponse.of(201, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
                } else {
                    System.err.println("❌ Customer creation failed - no customer returned");
                    return HttpResponse.of(500, "text/plain; charset=utf-8",
                            "Internal Server Error: Failed to create customer".getBytes(StandardCharsets.UTF_8));
                }
            } catch (RuntimeException e) {
                // Database errors từ repository
                System.err.println("❌ Database error creating customer: " + e.getMessage());
                return HttpResponse.of(500, "text/plain; charset=utf-8",
                        ("Database Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                // JSON parsing hoặc lỗi khác
                System.err.println("❌ General error creating customer: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
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
                    return HttpResponse.of(400,
                            "text/plain; charset=utf-8",
                            AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
                }
            }



        };
    }

    public Function<HttpRequest, HttpResponse> updateCustomer() {
        return (HttpRequest req) -> {
            try {
                Customer createdCustomer = null;
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);;
                createdCustomer = gson.fromJson(jsonBody, Customer.class);
                customerRecordRepository.save(createdCustomer);
                String jsonResponse = gson.toJson(createdCustomer);

                return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
            catch (Exception e) {
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
    public Function<HttpRequest, HttpResponse> deleteCustomer() {
        return (HttpRequest req)->{
            try{
                Customer deletedCustomer = null;
                Gson gson = GsonProvider.getGson();
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);;
                deletedCustomer = gson.fromJson(jsonBody, Customer.class);
                boolean deleted = customerRecordRepository.deleteById(deletedCustomer.getId());
                if(deleted){
                    String jsonResponse = gson.toJson(deletedCustomer);
                    return HttpResponse.of(200, "application/json", jsonResponse.getBytes(StandardCharsets.UTF_8));
                }
                else{
                    return HttpResponse.of(404,
                            "text/plain; charset=utf-8",
                            AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8));
                }
            }
            catch (Exception e) {
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
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
