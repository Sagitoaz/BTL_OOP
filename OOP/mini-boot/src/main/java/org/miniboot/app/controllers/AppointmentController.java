package org.miniboot.app.controllers;


import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AppointmentController {
    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * GET /appointments
     * - Không có query  -> trả mọi appointment
     * - ?id=123         -> trả 1 appointment theo id (hoặc 404)
     * - ?doctorId=&date=YYYY-MM-DD -> lọc theo bác sĩ + ngày
     */
    public Function<HttpRequest, HttpResponse> getAppointments() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;
            // lọc theo DoctorId +Date
            Optional<Integer> doctorId = extractInt(q, "doctorId");
            Optional<String> date = extractFirst(q, "date");
            if (doctorId.isPresent() && date.isPresent()) {
                return Json.ok(appointmentRepository.findByDoctorIdAndDate(doctorId.get(), date.get()));
            }

            // nếu có ID trả về 1 bản ghi
            Optional<Integer> idOpt = extractInt(q, "id");
            return idOpt.map(integer -> appointmentRepository.findById(integer)
                    .map(Json::ok)
                    .orElse(HttpResponse.of(
                            404,
                            "text/plain; charset=utf-8",
                            AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)
                    ))).orElseGet(() -> Json.ok(appointmentRepository.findAll()));
            // trả về hết
        };
    }

    // helper
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
