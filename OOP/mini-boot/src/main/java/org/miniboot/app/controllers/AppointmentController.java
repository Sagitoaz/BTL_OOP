package org.miniboot.app.controllers;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

public class AppointmentController {
    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public static void mount(Router router, AppointmentController ac) {
        router.get("/appointments", ac.getAppointments());
        router.post("/appointments", ac.createAppointment());
        router.put("/appointments", ac.updateAppointment());
        router.delete("/appointments", ac.deleteAppointment());
    }

    /**
     * GET /appointments
     * - Không có query  -> trả mọi appointment
     * - ?id=123         -> trả 1 appointment theo id (hoặc 404)
     * - ?doctorId=&date=YYYY-MM-DD -> lọc theo bác sĩ + ngày
     */
    public Function<HttpRequest, HttpResponse> createAppointment() {
        return (HttpRequest req) ->
        {
            try {
                System.out.println("📥 Received body: " + new String(req.body, StandardCharsets.UTF_8));
                Appointment appointment = Json.fromBytes(req.body, Appointment.class);
                System.out.println("✅ Parsed appointment: " + appointment);
                appointmentRepository.save(appointment);
                return Json.created(appointment);
            } catch (Exception e) {
                System.err.println("❌ Error creating appointment: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(400,
                        "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

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

    public Function<HttpRequest, HttpResponse> updateAppointment() {
        return (HttpRequest req) -> {
            try {
                Appointment appointment = Json.fromBytes(req.body, Appointment.class);

                if (appointment.getId() == 0) {
                    return HttpResponse.of(400, "text/plain; charset=utf-8",
                            "Missing appointment ID".getBytes(StandardCharsets.UTF_8));
                }

                Optional<Appointment> existing = appointmentRepository.findById(appointment.getId());
                if (existing.isEmpty()) {
                    return HttpResponse.of(404, "text/plain; charset=utf-8",
                            "Appointment not found".getBytes(StandardCharsets.UTF_8));
                }

                // TODO: Validate slot nếu thay đổi thời gian
                // - Nếu startTime/endTime thay đổi, check slot mới có trống không

                Appointment updated = appointmentRepository.save(appointment);
                return Json.ok(updated);

            } catch (IOException e) {
                System.err.println("Error updating appointment: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    public Function<HttpRequest, HttpResponse> deleteAppointment() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;
            Optional<Integer> idOpt = extractInt(q, "id");

            if (idOpt.isEmpty()) {
                return HttpResponse.of(400, "text/plain; charset=utf-8",
                        "Missing id parameter".getBytes(StandardCharsets.UTF_8));
            }

            int id = idOpt.get();
            Optional<Appointment> existing = appointmentRepository.findById(id);

            if (existing.isEmpty()) {
                return HttpResponse.of(404, "text/plain; charset=utf-8",
                        "Appointment not found".getBytes(StandardCharsets.UTF_8));
            }

            // Đổi status thành CANCELLED thay vì xóa hẳn
            Appointment appointment = existing.get();
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);

            return HttpResponse.of(200, "text/plain; charset=utf-8",
                    "Appointment cancelled".getBytes(StandardCharsets.UTF_8));
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
