package org.miniboot.app.controllers;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

import org.miniboot.app.AppConfig;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.service.ScheduleService;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;
import org.miniboot.app.util.errorvalidation.ValidationUtils;
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.RateLimiter;

public class AppointmentController {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleService scheduleService;

    // Idempotency cache for appointment booking
    private static final ConcurrentHashMap<String, CachedAppointment> idempotencyCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours

    public AppointmentController(AppointmentRepository appointmentRepository, ScheduleService scheduleService) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleService = scheduleService;
    }

    // Inner class for caching idempotent appointment results
    private static class CachedAppointment {
        final HttpResponse response;
        final long timestamp;
        final String requestHash;

        CachedAppointment(HttpResponse response, String requestHash) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
            this.requestHash = requestHash;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }

    public static void mount(Router router, AppointmentController ac) {
        router.get("/appointments", ac.getAppointments());
        router.get("/appointments/available-slots", ac.getAvailableSlots());
        router.post("/appointments", ac.createAppointment());
        router.put("/appointments", ac.updateAppointment());
        router.delete("/appointments", ac.deleteAppointment());
    }

    /**
     * POST /appointments
     * - Tạo appointment mới với đầy đủ validation
     * - Hỗ trợ Idempotency Key để tránh double booking
     * - Check slot availability, working hours, conflicts
     */
    public Function<HttpRequest, HttpResponse> createAppointment() {
        return (HttpRequest req) -> {
            // Step 0: Rate limiting check
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null)
                return rateLimitError;

            // Step 1-3: Standard validations (Content-Type, JWT, optional Role check)
            HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
            if (contentTypeError != null)
                return contentTypeError;

            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null)
                return jwtError;

            try {
                // Step 4: Check Idempotency Key
                Map<String, String> headers = req.headers;
                String idempotencyKey = headers.get("Idempotency-Key");
                if (idempotencyKey == null) {
                    idempotencyKey = headers.get("idempotency-key");
                }

                String requestHash = req.body != null ? String.valueOf(new String(req.body).hashCode()) : "";

                if (idempotencyKey != null) {
                    CachedAppointment cached = idempotencyCache.get(idempotencyKey);
                    if (cached != null) {
                        if (cached.isExpired()) {
                            idempotencyCache.remove(idempotencyKey);
                        } else {
                            if (cached.requestHash.equals(requestHash)) {
                                System.out.println(
                                        "♻️ Returning cached appointment for idempotency key: " + idempotencyKey);
                                return cached.response;
                            } else {
                                return ValidationUtils.error(409, "IDEMPOTENCY_KEY_CONFLICT",
                                        "Idempotency Key reuse conflict: different request content");
                            }
                        }
                    }
                }

                // Step 5: Parse JSON
                System.out.println("📥 Received body: " + new String(req.body, StandardCharsets.UTF_8));
                Appointment appointment;
                try {
                    appointment = Json.fromBytes(req.body, Appointment.class);
                    System.out.println("✅ Parsed appointment: " + appointment);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Invalid JSON format: " + e.getMessage());
                }

                // Step 6: Validate required fields
                if (appointment.getDoctorId() == 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Doctor ID is required");
                }
                if (appointment.getCustomerId() == 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Customer ID (Patient) is required");
                }
                if (appointment.getStartTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "Start time is required");
                }
                if (appointment.getEndTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                            "End time is required");
                }

                // Step 7: Business rules validation
                // Check slot không bị trùng (409 Conflict)
                try {
                    String date = appointment.getStartTime().toLocalDate().toString();
                    List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndDate(
                            appointment.getDoctorId(), date);

                    for (Appointment existing : existingAppointments) {
                        // Skip cancelled appointments
                        if (existing.getStatus() == AppointmentStatus.CANCELLED) {
                            continue;
                        }

                        // Check time overlap
                        if (appointment.getStartTime().isBefore(existing.getEndTime()) &&
                                existing.getStartTime().isBefore(appointment.getEndTime())) {
                            return ValidationUtils.error(409, "SLOT_CONFLICT",
                                    "This time slot is already booked by another appointment");
                        }
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Check thời gian hợp lệ
                if (!appointment.getStartTime().isBefore(appointment.getEndTime())) {
                    return ValidationUtils.error(422, "INVALID_TIME_RANGE",
                            "Start time must be before end time");
                }

                // Check working hours - Bác sĩ có làm việc trong khung giờ này không?
                try {
                    boolean isWithinWorkingHours = scheduleService.isWithinWorkingHours(
                        appointment.getDoctorId(), 
                        appointment.getStartTime(), 
                        appointment.getEndTime()
                    );
                    
                    if (!isWithinWorkingHours) {
                        java.time.DayOfWeek dayOfWeek = appointment.getStartTime().getDayOfWeek();
                        return ValidationUtils.error(422, "OUTSIDE_WORKING_HOURS",
                            "Doctor is not working at this time on " + dayOfWeek + 
                            ". Please check doctor's schedule and choose an available time slot.");
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Step 8: Save appointment
                Appointment saved;
                try {
                    saved = appointmentRepository.save(appointment);
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                if (saved == null || saved.getId() == 0) {
                    return ValidationUtils.error(500, "DB_ERROR",
                            "Cannot create appointment");
                }

                System.out.println("✅ Appointment created successfully: ID=" + saved.getId());

                // Step 9: Cache result for idempotency
                HttpResponse response = Json.created(saved);
                if (idempotencyKey != null) {
                    idempotencyCache.put(idempotencyKey, new CachedAppointment(response, requestHash));
                }

                return response;

            } catch (Exception e) {
                System.err.println("❌ Unexpected error in createAppointment: " + e.getMessage());
                e.printStackTrace();
                return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred");
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
                                AppConfig.RESPONSE_404.getBytes(StandardCharsets.UTF_8)));
            }

            // 2. Nếu có ?doctorId=X&date=YYYY-MM-DD -> dùng findByDoctorIdAndDate()
            Optional<Integer> doctorIdOpt = ExtractHelper.extractInt(q, "doctorId");
            Optional<String> dateOpt = ExtractHelper.extractFirst(q, "date");

            if (doctorIdOpt.isPresent() && dateOpt.isPresent()) {
                System.out.println(
                        "🔍 DEBUG: GET /appointments?doctorId=" + doctorIdOpt.get() + "&date=" + dateOpt.get());
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
                        doctorId, customerId, status, fromDate, toDate, search);

                return Json.ok(filtered);
            }

            // 4. Không có gì -> trả về tất cả (backward compatible)
            return Json.ok(appointmentRepository.findAll());
        };
    }

    /**
     * GET /appointments/available-slots?doctorId=X&date=YYYY-MM-DD
     * Lấy danh sách time slots available cho việc đặt lịch
     */
    public Function<HttpRequest, HttpResponse> getAvailableSlots() {
        return (HttpRequest req) -> {
            // Rate limiting
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // JWT validation
            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null) return jwtError;
            
            try {
                Map<String, List<String>> query = req.query;
                
                // Validate required parameters
                Optional<Integer> doctorIdOpt = ExtractHelper.extractInt(query, "doctorId");
                Optional<String> dateStrOpt = ExtractHelper.extractFirst(query, "date");
                
                if (doctorIdOpt.isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                        "doctorId parameter is required");
                }
                if (dateStrOpt.isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                        "date parameter is required (format: YYYY-MM-DD)");
                }
                
                int doctorId = doctorIdOpt.get();
                String dateStr = dateStrOpt.get();
                
                // Parse date
                java.time.LocalDate date;
                try {
                    date = java.time.LocalDate.parse(dateStr);
                } catch (Exception e) {
                    return ValidationUtils.error(400, "INVALID_DATE_FORMAT",
                        "Invalid date format. Expected: YYYY-MM-DD");
                }
                
                // Check if date is in the past
                if (date.isBefore(java.time.LocalDate.now())) {
                    return ValidationUtils.error(422, "PAST_DATE",
                        "Cannot book appointments for past dates");
                }
                
                // Get available slots from ScheduleService
                List<org.miniboot.app.domain.models.TimeSlot> availableSlots = 
                    scheduleService.getAvailableSlots(doctorId, date);
                
                if (availableSlots.isEmpty()) {
                    // Check if doctor is not working on this day
                    if (!scheduleService.isDoctorWorking(doctorId, date)) {
                        return ValidationUtils.error(422, "DOCTOR_NOT_WORKING",
                            "Doctor is not working on " + date + " (" + date.getDayOfWeek() + ")");
                    }
                }
                
                return Json.ok(availableSlots);
                
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
        };
    }

    public Function<HttpRequest, HttpResponse> updateAppointment() {
        return (HttpRequest req) -> {
            try {
                Appointment appointment = Json.fromBytes(req.body, Appointment.class);

                if (appointment.getId() == 0) {
                    return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST,
                            HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                            "Missing appointment ID".getBytes(StandardCharsets.UTF_8));
                }

                Optional<Appointment> existing = appointmentRepository.findById(appointment.getId());
                if (existing.isEmpty()) {
                    return HttpResponse.of(HttpConstants.STATUS_NOT_FOUND,
                            HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                            "Appointment not found".getBytes(StandardCharsets.UTF_8));
                }

                // - Nếu startTime/endTime thay đổi, check slot mới có trống không
                Appointment updated = appointmentRepository.save(appointment);
                return Json.ok(updated);

            } catch (IOException e) {
                System.err.println("Error updating appointment: " + e.getMessage());
                e.printStackTrace();
                return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST,
                        HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                        HttpConstants.REASON_BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
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
