package org.miniboot.app.domain.validate;

import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.repo.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleValidator {
    private final AppointmentRepository appointmentRepository;

    public ScheduleValidator(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Kiểm tra slot có trống không
     * @return true nếu slot trống, false nếu bị trùng
     */
    public boolean isSlotAvailable(int doctorId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeAppointmentId) {
        // Lấy tất cả appointments của bác sĩ trong ngày
        String date = startTime.toLocalDate().toString();
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);

        // Check overlap
        for (Appointment appt : appointments) {
            // Bỏ qua appointment đang update
            if (excludeAppointmentId != null && appt.getId() == excludeAppointmentId) {
                continue;
            }

            // Check overlap
            if (startTime.isBefore(appt.getEndTime()) && appt.getStartTime().isBefore(endTime)) {
                System.out.println("Slot conflict with appointment #" + appt.getId());
                return false;
            }
        }

        return true;
    }

    /**
     * Validate appointment trước khi lưu
     */
    public ValidationResult validate(Appointment appointment) {
        // Check thời gian hợp lệ
        if (appointment.getStartTime().isAfter(appointment.getEndTime())) {
            return ValidationResult.error("Start time must be before end time");
        }

        // Check thời gian trong quá khứ
        if (appointment.getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            return ValidationResult.error("Cannot create appointment in the past");
        }

        // Check slot trống
        boolean available = isSlotAvailable(
                appointment.getDoctorId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getId() > 0 ? appointment.getId() : null
        );

        if (!available) {
            return ValidationResult.error("Time slot is not available");
        }

        return ValidationResult.success();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, "Valid");
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
