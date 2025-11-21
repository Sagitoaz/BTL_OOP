package org.miniboot.app.domain.service;

import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.DoctorSchedule;
import org.miniboot.app.domain.models.TimeSlot;
import org.miniboot.app.domain.repo.AppointmentRepository;
import org.miniboot.app.domain.repo.DoctorScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service để tính toán available time slots cho booking appointment
 * 
 * Logic:
 * 1. Lấy working schedule của bác sĩ cho ngày cụ thể
 * 2. Chia working hours thành các time slots (mỗi slot 30 phút)
 * 3. Check slot nào đã có appointment → mark as unavailable
 * 4. Trả về list các TimeSlot với trạng thái available/unavailable
 */
public class ScheduleService {
    
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    
    // Default slot duration: 30 minutes
    private static final int SLOT_DURATION_MINUTES = 30;
    
    public ScheduleService(DoctorScheduleRepository scheduleRepository, 
                          AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Lấy danh sách time slots available cho bác sĩ trong một ngày cụ thể
     * 
     * @param doctorId ID của bác sĩ
     * @param date Ngày cần kiểm tra
     * @return List các TimeSlot với trạng thái available
     */
    public List<TimeSlot> getAvailableSlots(int doctorId, LocalDate date) throws Exception {
        // 1. Get working schedule for this day
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> workingSchedules = scheduleRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);
        
        if (workingSchedules.isEmpty()) {
            // Bác sĩ không làm việc vào ngày này
            return new ArrayList<>();
        }
        
        // 2. Get all appointments for this doctor on this date
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(
            doctorId, date.toString());
        
        // 3. Generate time slots from working schedules
        List<TimeSlot> allSlots = new ArrayList<>();
        
        for (DoctorSchedule schedule : workingSchedules) {
            List<TimeSlot> slotsForSchedule = generateSlotsForSchedule(schedule, appointments, date);
            allSlots.addAll(slotsForSchedule);
        }
        
        return allSlots;
    }
    
    /**
     * Generate time slots cho một ca làm việc cụ thể
     */
    private List<TimeSlot> generateSlotsForSchedule(DoctorSchedule schedule, 
                                                    List<Appointment> appointments,
                                                    LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        
        LocalTime currentSlotStart = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        
        while (currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES).isBefore(endTime) ||
               currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES).equals(endTime)) {
            
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES);
            
            // Check if this slot is available
            boolean isAvailable = isSlotAvailable(currentSlotStart, currentSlotEnd, appointments, date);
            
            TimeSlot slot = new TimeSlot(
                currentSlotStart,
                currentSlotEnd,
                SLOT_DURATION_MINUTES,
                isAvailable
            );
            
            slots.add(slot);
            
            // Move to next slot
            currentSlotStart = currentSlotEnd;
        }
        
        return slots;
    }
    
    /**
     * Kiểm tra xem một time slot có available không
     * (không bị trùng với appointment nào)
     */
    private boolean isSlotAvailable(LocalTime slotStart, LocalTime slotEnd, 
                                   List<Appointment> appointments, LocalDate date) {
        LocalDateTime slotStartDateTime = LocalDateTime.of(date, slotStart);
        LocalDateTime slotEndDateTime = LocalDateTime.of(date, slotEnd);
        
        for (Appointment appointment : appointments) {
            // Skip cancelled appointments
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            
            // Check if appointment overlaps with this slot
            if (appointment.getStartTime().isBefore(slotEndDateTime) &&
                appointment.getEndTime().isAfter(slotStartDateTime)) {
                return false; // Slot is occupied
            }
        }
        
        return true; // Slot is available
    }
    
    /**
     * Kiểm tra xem bác sĩ có làm việc vào ngày cụ thể không
     */
    public boolean isDoctorWorking(int doctorId, LocalDate date) throws Exception {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);
        return !schedules.isEmpty();
    }
    
    /**
     * Kiểm tra xem một khoảng thời gian có nằm trong working hours không
     */
    public boolean isWithinWorkingHours(int doctorId, LocalDateTime startTime, LocalDateTime endTime) 
            throws Exception {
        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);
        
        if (schedules.isEmpty()) {
            return false; // Bác sĩ không làm việc vào ngày này
        }
        
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();
        
        // Check if the time range falls within any working schedule
        for (DoctorSchedule schedule : schedules) {
            if (!start.isBefore(schedule.getStartTime()) && 
                !end.isAfter(schedule.getEndTime())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Kiểm tra xem có appointment nào conflict với thời gian này không
     */
    public boolean hasConflictingAppointment(int doctorId, LocalDateTime startTime, 
                                            LocalDateTime endTime, Integer excludeAppointmentId) 
            throws Exception {
        String date = startTime.toLocalDate().toString();
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        
        for (Appointment appointment : appointments) {
            // Skip the appointment being updated
            if (excludeAppointmentId != null && appointment.getId() == excludeAppointmentId) {
                continue;
            }
            
            // Skip cancelled appointments
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            
            // Check for time overlap
            if (startTime.isBefore(appointment.getEndTime()) &&
                endTime.isAfter(appointment.getStartTime())) {
                return true; // Conflict found
            }
        }
        
        return false; // No conflict
    }
}
