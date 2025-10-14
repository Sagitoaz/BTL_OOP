package org.miniboot.app.controllers.PatientAndPrescription;
import org.miniboot.app.AppConfig;
import org.miniboot.app.Service.CustomerRecordService;
import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.domain.models.CustomerRecord;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CustomerRecordController {
    private final CustomerRecordRepository customerRecordRepository;
    private final CustomerRecordService customerRecordService;

    public CustomerRecordController(CustomerRecordRepository customerRecordRepository) {
        this.customerRecordRepository = customerRecordRepository;
        this.customerRecordService = new CustomerRecordService(customerRecordRepository);
    }

    public static void mount(org.miniboot.app.router.Router router, CustomerRecordController prc) {
        router.get("/customers/getCustomers", prc.getCustomer());
        router.post("/customers/createNewCustomer", prc.createCustomer());
    }

    public Function<HttpRequest, HttpResponse> createCustomer() {
        return (HttpRequest req) -> {
            CustomerRecord createdCustomer = null;
            try {
                createdCustomer = Json.fromBytes(req.body, CustomerRecord.class);
                customerRecordRepository.save(createdCustomer);
                return Json.created(createdCustomer);
            } catch (IOException e) {
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
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
            CustomerRecord.Gender genderEnum = null;
            if (gender.isPresent()) {
                try {
                    genderEnum = CustomerRecord.Gender.valueOf(gender.get().toUpperCase());
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
                    List<CustomerRecord> results = customerRecordService.searchPatientRecords(criteria);
                    return Json.ok(results);
                } catch (Exception e) {
                    return HttpResponse.of(400,
                            "text/plain; charset=utf-8",
                            AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
                }
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
