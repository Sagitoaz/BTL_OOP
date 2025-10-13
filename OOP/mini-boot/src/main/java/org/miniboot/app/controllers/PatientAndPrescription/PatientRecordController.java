package org.miniboot.app.controllers.PatientAndPrescription;
import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.PatientRecord;
import org.miniboot.app.domain.repo.PatientAndPrescription.PatientRecordRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PatientRecordController {
    private final PatientRecordRepository patientRecordRepository;

    public PatientRecordController(PatientRecordRepository patientRecordRepository) {
        this.patientRecordRepository = patientRecordRepository;
    }

    public static void mount(org.miniboot.app.router.Router router, PatientRecordController prc) {
        router.post("/patients", prc.createPatient());
        router.get("/patients", prc.getPatientById());


    }

    public Function<HttpRequest, HttpResponse> createPatient() {
        return (HttpRequest req) -> {
            PatientRecord createdPatient = null;
            try {
                createdPatient = Json.fromBytes(req.body, PatientRecord.class);
                patientRecordRepository.save(createdPatient);
                return Json.created(createdPatient);
            } catch (IOException e) {
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
    public Function<HttpRequest, HttpResponse> getPatientById() {
        return (HttpRequest req) -> {
            Optional<Integer> id = extractInt(req.query, "id");

            if (id.isEmpty()) {
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }

            Optional<PatientRecord> patient = patientRecordRepository.findById(id.get());

            if (patient.isEmpty()) {
                return HttpResponse.of(404,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8));
            }

            // Sửa lỗi: Dùng Json.ok() thay vì Json.created()
            return Json.ok(patient.get());
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
