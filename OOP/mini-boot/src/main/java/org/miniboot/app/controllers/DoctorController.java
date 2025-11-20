package org.miniboot.app.controllers;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.config.ErrorMessages;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.DoctorSchedule;
import org.miniboot.app.domain.models.TimeSlot;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.DoctorScheduleRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DoctorController {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public DoctorController(DoctorRepository doctorRepository, 
                           AppointmentRepository appointmentRepository,
                           DoctorScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public static void mount(Router router, DoctorController dc) {
        router.get("/doctors", dc.getDoctors());
        router.post("/doctors", dc.createDoctor());
        router.get("/doctors/available-slots", dc.getAvailableSlots());
    }

    //POST /doctors
    public Function<HttpRequest, HttpResponse> createDoctor() {
        return (HttpRequest req) -> {
            try {
                System.out.println(new String(req.body, StandardCharsets.UTF_8));
                Doctor doctor = Json.fromBytes(req.body, Doctor.class);
                doctorRepository.saveDoctor(doctor);
                return Json.created(doctor);
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST,
                        HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                        ErrorMessages.ERROR_INVALID_REQUEST.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // GET /doctors
    // -- không có ?id= trả về danh sách bác sĩ
    // -- có ?id= thì trả về 1 bác sĩ có id

    public Function<HttpRequest, HttpResponse> getDoctors() {
        return (HttpRequest req) -> {
            Optional<Integer> idOpt = ExtractHelper.extractId(req.query);
            return idOpt.map(id -> doctorRepository.findById(id)
                            .map(Json::ok)
                            .orElse(HttpResponse.of(
                                    HttpConstants.STATUS_NOT_FOUND,
                                    HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                                    ErrorMessages.ERROR_DOCTOR_NOT_FOUND.getBytes(StandardCharsets.UTF_8)
                            )))
                    .orElseGet(() -> Json.ok(doctorRepository.findAll()));
        };
    }

    public Function<HttpRequest, HttpResponse> getAvailableSlots() {
        return (HttpRequest req) -> {
            Map<String, List<String>> q = req.query;
            Optional<Integer> doctorIdOpt = ExtractHelper.extractInt(q, "doctorId");
            Optional<String> dateOpt = ExtractHelper.extractFirst(q, "date");

            if (doctorIdOpt.isEmpty() || dateOpt.isEmpty()) {
                return HttpResponse.of(HttpConstants.STATUS_BAD_REQUEST,
                        HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8,
                        "Missing doctorId or date parameter".getBytes(StandardCharsets.UTF_8));
            }

            int doctorId = doctorIdOpt.get();
            String date = dateOpt.get();

            // Tính toán slot trống
            List<TimeSlot> availableSlots = null;
            try {
                availableSlots = calculateAvailableSlots(doctorId, date);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return Json.ok(availableSlots);
        };
    }

    /**
     * Tính toán slot trống cho bác sĩ trong ngày
     * KHÔNG lưu vào DB, chỉ tính toán runtime
     * ✅ Sử dụng lịch làm việc thực tế từ doctor_schedules
     */
    private List<TimeSlot> calculateAvailableSlots(int doctorId, String date) throws SQLException {
        List<TimeSlot> slots = new ArrayList<>();

        // 1. Parse ngày
        LocalDate localDate = LocalDate.parse(date);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        
        System.out.println("🔍 Calculating available slots:");
        System.out.println("   - Doctor ID: " + doctorId);
        System.out.println("   - Date: " + date + " (" + dayOfWeek + ")");

        // 2. ✅ Lấy lịch làm việc của bác sĩ trong ngày này
        List<DoctorSchedule> workingSchedules = scheduleRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);
        
        if (workingSchedules.isEmpty()) {
            System.out.println("⚠️ Doctor has NO working schedule on " + dayOfWeek);
            return slots; // Không làm việc ngày này → trả về list rỗng
        }
        
        System.out.println("✅ Found " + workingSchedules.size() + " working schedules for this day:");
        for (DoctorSchedule ws : workingSchedules) {
            System.out.println("   - " + ws.getStartTime() + " to " + ws.getEndTime() + " (active: " + ws.isActive() + ")");
        }

        // 3. Lấy danh sách appointment đã book
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        System.out.println("📅 Found " + bookedAppointments.size() + " booked appointments");

        // 4. ✅ Sinh slots cho MỖI khung giờ làm việc
        int slotDuration = 30; // 30 phút mỗi slot
        
        for (DoctorSchedule schedule : workingSchedules) {
            if (!schedule.isActive()) {
                System.out.println("⏭️ Skipping inactive schedule: " + schedule.getStartTime() + "-" + schedule.getEndTime());
                continue;
            }
            
            LocalTime workStart = schedule.getStartTime();
            LocalTime workEnd = schedule.getEndTime();
            
            System.out.println("🕒 Generating slots from " + workStart + " to " + workEnd);
            
            LocalTime currentTime = workStart;
            while (currentTime.isBefore(workEnd)) {
                LocalTime slotEnd = currentTime.plusMinutes(slotDuration);
                
                // Đảm bảo slot không vượt quá giờ làm việc
                if (slotEnd.isAfter(workEnd)) {
                    break;
                }

                boolean isAvailable = isSlotFree(currentTime, slotEnd, bookedAppointments);

                TimeSlot slot = new TimeSlot(currentTime, slotEnd, slotDuration, isAvailable);
                slots.add(slot);

                currentTime = slotEnd;
            }
        }
        
        System.out.println("✅ Total " + slots.size() + " slots generated");
        long availableCount = slots.stream().filter(TimeSlot::isAvailable).count();
        System.out.println("   - Available: " + availableCount);
        System.out.println("   - Booked: " + (slots.size() - availableCount));

        return slots;
    }

    /**
     * Kiểm tra slot có trống không (không trùng với appointment nào)
     */
    private boolean isSlotFree(LocalTime slotStart, LocalTime slotEnd, List<Appointment> appointments) {
        for (Appointment appt : appointments) {
            LocalTime apptStart = appt.getStartTime().toLocalTime();
            LocalTime apptEnd = appt.getEndTime().toLocalTime();

            // Check overlap
            if (slotStart.isBefore(apptEnd) && apptStart.isBefore(slotEnd)) {
                return false;
            }
        }
        return true;
    }
}
