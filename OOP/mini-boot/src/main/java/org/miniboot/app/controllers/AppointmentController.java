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
import org.miniboot.app.util.ExtractHelper;
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

            // 1. Nếu có ?id=123 -> trả về 1 appointment
            Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");
            if (idOpt.isPresent()) {
                return appointmentRepository.findById(idOpt.get())
                        .map(Json::ok)
                        .orElse(HttpResponse.of(
                                404,
                                "text/plain; charset=utf-8",
                                AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)
                        ));
            }

            // 2. Nếu có ?doctorId=X&date=YYYY-MM-DD -> dùng findByDoctorIdAndDate()
            Optional<Integer> doctorIdOpt = ExtractHelper.extractInt(q, "doctorId");
            Optional<String> dateOpt = ExtractHelper.extractFirst(q, "date");
            
            if (doctorIdOpt.isPresent() && dateOpt.isPresent()) {
                System.out.println("🔍 DEBUG: GET /appointments?doctorId=" + doctorIdOpt.get() + "&date=" + dateOpt.get());
                List<Appointment> result = appointmentRepository.findByDoctorIdAndDate(
                        doctorIdOpt.get(), dateOpt.get());
                System.out.println("✅ DEBUG: Returning " + result.size() + " appointments for date " + dateOpt.get());
                return Json.ok(result);
            }

            // 3. Check xem có filter params không
            boolean hasFilters = q.containsKey("doctorId") ||
                    q.containsKey("customerId") ||
                    q.containsKey("status") ||
                    q.containsKey("fromDate") ||
                    q.containsKey("toDate") ||
                    q.containsKey("search");

            // 4. Nếu có filters -> dùng findWithFilters()
            if (hasFilters) {
                Integer doctorId = ExtractHelper.extractInt(q, "doctorId").orElse(null);
                Integer customerId = ExtractHelper.extractInt(q, "customerId").orElse(null);
                String status = ExtractHelper.extractFirst(q, "status").orElse(null);
                String fromDate = ExtractHelper.extractFirst(q, "fromDate").orElse(null);
                String toDate = ExtractHelper.extractFirst(q, "toDate").orElse(null);
                String search = ExtractHelper.extractFirst(q, "search").orElse(null);

                List<Appointment> filtered = appointmentRepository.findWithFilters(
                        doctorId, customerId, status, fromDate, toDate, search
                );

                return Json.ok(filtered);
            }

            // 4. Không có gì -> trả về tất cả (backward compatible)
            return Json.ok(appointmentRepository.findAll());
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
            Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

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
}
