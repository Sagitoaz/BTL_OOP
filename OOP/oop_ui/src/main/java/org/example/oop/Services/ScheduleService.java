package org.example.oop.Services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.oop.Data.repositories.AppointmentRepository;
import org.example.oop.Data.repositories.WorkingHoursRepository;
import org.example.oop.Model.Schedule.Appointment;
import org.example.oop.Model.Schedule.AppointmentStatus;
import org.example.oop.Model.Schedule.AppointmentType;
import org.example.oop.Model.Schedule.Schedule;
import org.example.oop.Model.Schedule.TimeSlot;
import org.example.oop.Model.Schedule.WorkingHours;

/**
 * ScheduleService - Quản lý lịch làm việc và time slots của doctors
 * Tích hợp với WorkingHours, TimeSlots và Appointments
 * CustomerId = -1 tương đương với việc slot đó bị block
 */
public class ScheduleService {
    private final WorkingHoursRepository workingHoursRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleService() {
        this.workingHoursRepository = new WorkingHoursRepository();
        this.appointmentRepository = new AppointmentRepository();
    }

    public ScheduleService(WorkingHoursRepository workingHoursRepository, 
                          AppointmentRepository appointmentRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Tạo Schedule cho doctor trong ngày cụ thể
     * Kết hợp working hours với appointments đã book
     */
    public Optional<Schedule> generateScheduleForDoctor(int doctorId, String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);

            Optional<WorkingHours> workingHours = workingHoursRepository.findByDoctorIdAndDay(doctorId, localDate.getDayOfWeek());

            if (workingHours.isEmpty() || !workingHours.get().isActive()) {
                return Optional.empty();
            }

            Schedule schedule = Schedule.generateFrom(workingHours.get(), localDate, 30); // 30 phút/slot

            List<Appointment> appointments = appointmentRepository.findByDoctorAndDate(doctorId, localDate);

            schedule.applyAppointments((ArrayList<Appointment>) appointments);

            return Optional.of(schedule);

        } catch (Exception e) {
            System.err.println("Error generating schedule: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy danh sách time slots available để book
     */
    public List<TimeSlot> getAvailableSlots(int doctorId, String date) {
        Optional<Schedule> scheduleOpt = generateScheduleForDoctor(doctorId, date);
        
        if (scheduleOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Schedule schedule = scheduleOpt.get();
        return schedule.getAvailableSlots();
    }

    /**
     * Lấy tất cả slots (available + unavailable) để hiển thị full schedule
     */
    public List<TimeSlot> getAllSlots(int doctorId, String date) {
        Optional<Schedule> scheduleOpt = generateScheduleForDoctor(doctorId, date);
        
        if (scheduleOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Schedule schedule = scheduleOpt.get();
        return schedule.getTimeSlots();
    }

    /**
     * Update working hours của doctor
     */
    public boolean updateWorkingHours(int doctorId, DayOfWeek dayOfWeek, 
                                    LocalTime startTime, LocalTime endTime, boolean active) {
        try {
            Optional<WorkingHours> existingWH = workingHoursRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);

            WorkingHours workingHours;
            if (existingWH.isPresent()) {
                workingHours = existingWH.get();
                workingHours.setStartTime(startTime);
                workingHours.setEndTime(endTime);
                workingHours.setActive(active);
                workingHoursRepository.save(workingHours);
            } else {
                workingHours = new WorkingHours(doctorId, dayOfWeek, startTime, endTime, active);
                workingHoursRepository.save(workingHours);
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error updating working hours: " + e.getMessage());
            return false;
        }
    }

    /**
     * Block time slot cho doctor (nghỉ, meeting, etc.)
     * Tạo một "blocking appointment" để mark slot unavailable
     */
    public boolean blockTimeSlot(int doctorId, LocalDateTime start, LocalDateTime end, String reason) {
        try {
            Appointment blockingAppointment = new Appointment(0, -1, doctorId, AppointmentType.BLOCKED, start,end);
            
            blockingAppointment.setStatus(AppointmentStatus.CONFIRMED);
            blockingAppointment.setNotes("BLOCKED: " + reason);
            blockingAppointment.setCreatedAt(LocalDateTime.now());
            blockingAppointment.setUpdatedAt(LocalDateTime.now());

            appointmentRepository.save(blockingAppointment);
            return true;

        } catch (Exception e) {
            System.err.println("Error blocking time slot: " + e.getMessage());
            return false;
        }
    }

    /**
     * Unblock time slot (xóa blocking appointment)
     */
    public boolean unblockTimeSlot(int doctorId, LocalDateTime start, LocalDateTime end) {
        try {
            List<Appointment> appointments = appointmentRepository.findByDoctorAndDate(doctorId, start.toLocalDate());
            
            Optional<Appointment> blockingAppointment = appointments.stream()
                    .filter(apt -> apt.getCustomerId() == -1)
                    .filter(apt -> apt.getStartTime().equals(start) && apt.getEndTime().equals(end))
                    .findFirst();

            if (blockingAppointment.isPresent()) {
                appointmentRepository.delete(blockingAppointment.get().getId());
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error unblocking time slot: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get schedule cho nhiều ngày (week/month view)
     */
    public List<Schedule> getSchedulesForDateRange(int doctorId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> schedules = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Optional<Schedule> schedule = generateScheduleForDoctor(doctorId, currentDate.toString());
            schedule.ifPresent(schedules::add);
            currentDate = currentDate.plusDays(1);
        }

        return schedules;
    }

    /**
     * Check if doctor is available trong khoảng thời gian
     */
    public boolean isDoctorAvailable(int doctorId, LocalDateTime start, LocalDateTime end) {
        try {
            DayOfWeek dayOfWeek = start.getDayOfWeek();
            Optional<WorkingHours> workingHours = workingHoursRepository.findByDoctorIdAndDay(doctorId, dayOfWeek);

            if (workingHours.isEmpty() || !workingHours.get().isActive()) {
                return false;
            }

            WorkingHours wh = workingHours.get();
            LocalTime startTime = start.toLocalTime();
            LocalTime endTime = end.toLocalTime();

            if (startTime.isBefore(wh.getStartTime()) || endTime.isAfter(wh.getEndTime())) return false;

            List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndDate(doctorId, start.toLocalDate());

            for (Appointment appointment : existingAppointments) {
                if (appointment.getStatus() != AppointmentStatus.CANCELLED &&
                    appointment.getStatus() != AppointmentStatus.NO_SHOW) {

                    if (isTimeOverlap(start, end, appointment.getStartTime(), appointment.getEndTime())) {
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error checking doctor availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get doctor's working hours cho tất cả các ngày trong tuần
     */
    public List<WorkingHours> getDoctorWorkingHours(int doctorId) {
        return workingHoursRepository.findByDoctorId(doctorId);
    }

    /**
     * Suggest alternative time slots khi có conflict
     */
    public List<String> suggestAlternativeSlots(int doctorId, String date, int durationMinutes) {
        List<TimeSlot> availableSlots = getAvailableSlots(doctorId, date);
        
        return availableSlots.stream()
                .map(slot -> slot.getStartTime() + "-" + slot.getEndTime())
                .collect(Collectors.toList());
    }

    /**
     * Check time overlap between two time ranges
     */
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Create default working hours for a doctor (8:00-17:00, Monday-Friday)
     */
    public void createDefaultWorkingHours(int doctorId) {
        DayOfWeek[] weekdays = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        };

        for (DayOfWeek day : weekdays) {
            updateWorkingHours(doctorId, day, 
                LocalTime.of(8, 0), LocalTime.of(17, 0), true);
        }

        updateWorkingHours(doctorId, DayOfWeek.SATURDAY, 
            LocalTime.of(8, 0), LocalTime.of(17, 0), false);
        updateWorkingHours(doctorId, DayOfWeek.SUNDAY, 
            LocalTime.of(8, 0), LocalTime.of(17, 0), false);
    }

    /**
     * Get statistics cho doctor schedule
     */
    public ScheduleStatistics getScheduleStatistics(int doctorId, String date) {
        Optional<Schedule> scheduleOpt = generateScheduleForDoctor(doctorId, date);
        
        if (scheduleOpt.isEmpty()) {
            return new ScheduleStatistics(0, 0, 0);
        }

        Schedule schedule = scheduleOpt.get();
        List<TimeSlot> allSlots = schedule.getTimeSlots();
        List<TimeSlot> availableSlots = schedule.getAvailableSlots();
        
        int totalSlots = allSlots.size();
        int availableCount = availableSlots.size();
        int bookedCount = totalSlots - availableCount;

        return new ScheduleStatistics(totalSlots, availableCount, bookedCount);
    }

    // Inner class for statistics
    public static class ScheduleStatistics {
        private final int totalSlots;
        private final int availableSlots;
        private final int bookedSlots;

        public ScheduleStatistics(int totalSlots, int availableSlots, int bookedSlots) {
            this.totalSlots = totalSlots;
            this.availableSlots = availableSlots;
            this.bookedSlots = bookedSlots;
        }

        public int getTotalSlots() { return totalSlots; }
        public int getAvailableSlots() { return availableSlots; }
        public int getBookedSlots() { return bookedSlots; }
        public double getUtilizationRate() { 
            return totalSlots > 0 ? (double) bookedSlots / totalSlots * 100 : 0; 
        }

        @Override
        public String toString() {
            return String.format("Total: %d, Available: %d, Booked: %d, Utilization: %.1f%%",
                    totalSlots, availableSlots, bookedSlots, getUtilizationRate());
        }
    }
}