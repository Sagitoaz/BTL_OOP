package org.miniboot.app.controllers;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.domain.models.DoctorSchedule;
import org.miniboot.app.domain.repo.DoctorScheduleRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;
import org.miniboot.app.util.errorvalidation.ValidationUtils;
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.RateLimiter;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Controller để quản lý lịch làm việc của bác sĩ
 * 
 * Endpoints:
 * - GET /doctor-schedules?doctorId=X - Lấy tất cả lịch làm việc của bác sĩ
 * - GET /doctor-schedules?doctorId=X&day=MONDAY - Lấy lịch làm việc theo ngày
 * - POST /doctor-schedules - Tạo lịch làm việc mới
 * - PUT /doctor-schedules?id=X - Cập nhật lịch làm việc
 * - DELETE /doctor-schedules?id=X - Xóa lịch làm việc
 */
public class DoctorScheduleController {
    
    private final DoctorScheduleRepository repository;
    
    public DoctorScheduleController(DoctorScheduleRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Mount routes
     */
    public static void mount(Router router, DoctorScheduleRepository repository) {
        DoctorScheduleController controller = new DoctorScheduleController(repository);
        
        router.get("/doctor-schedules", controller.getSchedules());
        router.post("/doctor-schedules", controller.createSchedule());
        router.put("/doctor-schedules", controller.updateSchedule());
        router.delete("/doctor-schedules", controller.deleteSchedule());
        
        System.out.println("✅ Mounted DoctorScheduleController with 4 endpoints");
    }
    
    /**
     * GET /doctor-schedules?doctorId=X&day=MONDAY
     * Lấy lịch làm việc của bác sĩ
     */
    private Function<HttpRequest, HttpResponse> getSchedules() {
        return (HttpRequest req) -> {
            // Rate limiting
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // JWT validation - ai cũng có thể xem lịch làm việc bác sĩ
            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null) return jwtError;
            
            try {
                Map<String, List<String>> query = req.query;
                Optional<Integer> doctorIdOpt = ExtractHelper.extractInt(query, "doctorId");
                
                if (doctorIdOpt.isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST",
                        "doctorId parameter is required");
                }
                
                int doctorId = doctorIdOpt.get();
                
                // Check if day parameter is provided
                Optional<String> dayOpt = ExtractHelper.extractFirst(query, "day");
                
                List<DoctorSchedule> schedules;
                
                if (dayOpt.isPresent()) {
                    // Lấy lịch theo ngày cụ thể
                    try {
                        DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayOpt.get().toUpperCase());
                        schedules = repository.findByDoctorIdAndDay(doctorId, dayOfWeek);
                    } catch (IllegalArgumentException e) {
                        return ValidationUtils.error(400, "INVALID_DAY",
                            "Invalid day of week. Must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
                    }
                } else {
                    // Lấy tất cả lịch làm việc
                    schedules = repository.findByDoctorId(doctorId);
                }
                
                return Json.ok(schedules);
                
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
        };
    }
    
    /**
     * POST /doctor-schedules
     * Tạo lịch làm việc mới
     * Body: {"doctorId": 201, "dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "17:00", "isActive": true}
     */
    private Function<HttpRequest, HttpResponse> createSchedule() {
        return (HttpRequest req) -> {
            // Rate limiting
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Standard validations (Content-Type, JWT, Role = ADMIN)
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;
            
            try {
                // Parse JSON
                String jsonBody = new String(req.body, StandardCharsets.UTF_8);
                DoctorSchedule schedule = Json.fromBytes(req.body, DoctorSchedule.class);
                
                // Validate required fields
                if (schedule.getDoctorId() == 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "doctorId is required");
                }
                if (schedule.getDayOfWeek() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "dayOfWeek is required");
                }
                if (schedule.getStartTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "startTime is required");
                }
                if (schedule.getEndTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "endTime is required");
                }
                
                // Validate time range
                if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
                    return ValidationUtils.error(422, "INVALID_TIME_RANGE",
                        "startTime must be before endTime");
                }
                
                // TODO: Validate doctor exists and role='doctor'
                
                // Check for overlapping schedules
                List<DoctorSchedule> existingSchedules = repository.findByDoctorIdAndDay(
                    schedule.getDoctorId(), schedule.getDayOfWeek());
                
                for (DoctorSchedule existing : existingSchedules) {
                    if (existing.overlaps(schedule.getStartTime(), schedule.getEndTime())) {
                        return ValidationUtils.error(409, "SCHEDULE_CONFLICT",
                            "This schedule overlaps with an existing schedule on " + schedule.getDayOfWeek());
                    }
                }
                
                // Save schedule
                DoctorSchedule saved = repository.save(schedule);
                
                return Json.created(saved);
                
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
        };
    }
    
    /**
     * PUT /doctor-schedules?id=X
     * Cập nhật lịch làm việc
     */
    private Function<HttpRequest, HttpResponse> updateSchedule() {
        return (HttpRequest req) -> {
            // Rate limiting
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // Standard validations
            HttpResponse validationError = ValidationUtils.validateStandardRequest(req, "application/json", "ADMIN");
            if (validationError != null) return validationError;
            
            try {
                // Get schedule ID from query
                Map<String, List<String>> query = req.query;
                Optional<Integer> idOpt = ExtractHelper.extractInt(query, "id");
                
                if (idOpt.isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "id parameter is required");
                }
                
                int scheduleId = idOpt.get();
                
                // Check if schedule exists
                Optional<DoctorSchedule> existingOpt = repository.findById(scheduleId);
                if (existingOpt.isEmpty()) {
                    return ValidationUtils.error(404, "NOT_FOUND",
                        "Schedule not found with ID: " + scheduleId);
                }
                
                // Parse updated schedule
                DoctorSchedule schedule = Json.fromBytes(req.body, DoctorSchedule.class);
                schedule.setId(scheduleId);
                
                // Validate required fields
                if (schedule.getDoctorId() == 0) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "doctorId is required");
                }
                if (schedule.getDayOfWeek() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "dayOfWeek is required");
                }
                if (schedule.getStartTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "startTime is required");
                }
                if (schedule.getEndTime() == null) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "endTime is required");
                }
                
                // Validate time range
                if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
                    return ValidationUtils.error(422, "INVALID_TIME_RANGE",
                        "startTime must be before endTime");
                }
                
                // Check for overlapping schedules (excluding current schedule)
                List<DoctorSchedule> existingSchedules = repository.findByDoctorIdAndDay(
                    schedule.getDoctorId(), schedule.getDayOfWeek());
                
                for (DoctorSchedule existing : existingSchedules) {
                    if (existing.getId() != scheduleId && 
                        existing.overlaps(schedule.getStartTime(), schedule.getEndTime())) {
                        return ValidationUtils.error(409, "SCHEDULE_CONFLICT",
                            "This schedule overlaps with an existing schedule on " + schedule.getDayOfWeek());
                    }
                }
                
                // Update schedule
                DoctorSchedule updated = repository.save(schedule);
                
                return Json.ok(updated);
                
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
        };
    }
    
    /**
     * DELETE /doctor-schedules?id=X
     * Xóa lịch làm việc
     */
    private Function<HttpRequest, HttpResponse> deleteSchedule() {
        return (HttpRequest req) -> {
            // Rate limiting
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
            if (rateLimitError != null) return rateLimitError;
            
            // JWT và Role validation
            HttpResponse jwtError = ValidationUtils.validateJWT(req);
            if (jwtError != null) return jwtError;
            
            HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
            if (roleError != null) return roleError;
            
            try {
                // Get schedule ID from query
                Map<String, List<String>> query = req.query;
                Optional<Integer> idOpt = ExtractHelper.extractInt(query, "id");
                
                if (idOpt.isEmpty()) {
                    return ValidationUtils.error(400, "BAD_REQUEST", "id parameter is required");
                }
                
                int scheduleId = idOpt.get();
                
                // Check if schedule exists
                Optional<DoctorSchedule> existingOpt = repository.findById(scheduleId);
                if (existingOpt.isEmpty()) {
                    return ValidationUtils.error(404, "NOT_FOUND",
                        "Schedule not found with ID: " + scheduleId);
                }
                
                // Delete schedule
                boolean deleted = repository.delete(scheduleId);
                
                if (deleted) {
                    return HttpResponse.of(200, "text/plain", "Schedule deleted successfully".getBytes());
                } else {
                    return ValidationUtils.error(500, "DELETE_FAILED", "Failed to delete schedule");
                }
                
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
        };
    }
}
