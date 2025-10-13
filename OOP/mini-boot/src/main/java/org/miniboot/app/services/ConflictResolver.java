package org.miniboot.app.services;

import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.repo.AppointmentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConflictResolver {
    private final AppointmentRepository appointmentRepository;

    public ConflictResolver(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Kiểm tra conflict cho appointment mới
     */
    public List<String> detectTimeConflict(Appointment newAppt) {
        List<String> conflicts = new ArrayList<>();
        List<Appointment> existedAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getDoctorId() == newAppt.getDoctorId())
                .filter(apt -> apt.getStartTime().toLocalDate().equals(newAppt.getStartTime().toLocalDate()))
                .filter(apt -> !apt.getStatus().equals("cancelled"))
                .toList();

        for (Appointment existed : existedAppointments) {
            if (isTimeOverlap(newAppt.getStartTime(), newAppt.getEndTime(),
                    existed.getStartTime(), existed.getEndTime())) {
                conflicts.add("Conflict with existing appointment: " +
                        existed.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "-" + existed.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
        return conflicts;
    }

    /**
     * Kiểm tra conflict khi reschedule
     */
    public List<String> detectTimeConflictForReschedule(Appointment appointment) {
        List<String> conflicts = new ArrayList<>();

        List<Appointment> existedAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getDoctorId() == appointment.getDoctorId())
                .filter(apt -> apt.getId() != appointment.getId())
                .filter(apt -> apt.getStartTime().toLocalDate().equals(appointment.getStartTime().toLocalDate()))
                .filter(apt -> !apt.getStatus().equals("cancelled"))
                .toList();

        for (Appointment existed : existedAppointments) {
            if (isTimeOverlap(appointment.getStartTime(), appointment.getEndTime(),
                    existed.getStartTime(), existed.getEndTime())) {
                conflicts.add("Reschedule conflict with: " +
                        existed.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "-" + existed.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
        return conflicts;
    }

    /**
     * Gợi ý các slot thời gian trống
     */
    public List<String> suggestAlternativeSlots(int doctorId, String date) {
        List<String> availableSlots = new ArrayList<>();

        // Working hours: 8:00 - 17:00
        LocalDateTime workStart = LocalDateTime.parse(date + "T08:00:00");
        LocalDateTime workEnd = LocalDateTime.parse(date + "T17:00:00");

        // Lấy appointments đã book trong ngày
        List<Appointment> bookedAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getDoctorId() == doctorId)
                .filter(apt -> apt.getStartTime().toLocalDate().toString().equals(date))
                .filter(apt -> !apt.getStatus().equals("cancelled"))
                .toList();

        // Tạo slots mỗi 30 phút 1 slot và check available
        LocalDateTime currentSlot = workStart;
        while (currentSlot.plusMinutes(30).isBefore(workEnd) || currentSlot.plusMinutes(30).isEqual(workEnd)) {
            LocalDateTime slotEnd = currentSlot.plusMinutes(30);

            boolean isAvailable = true;
            for (Appointment booked : bookedAppointments) {
                if (isTimeOverlap(currentSlot, slotEnd, booked.getStartTime(), booked.getEndTime())) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                availableSlots.add(currentSlot.format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "-" + slotEnd.format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            currentSlot = currentSlot.plusMinutes(30);
        }

        return availableSlots;
    }

    /**
     * Validate đơn giản có conflict không dựa trên time overlap
     */
    public boolean validateScheduleConflict(int doctorId, LocalDateTime start, LocalDateTime end) {
        List<Appointment> conflicts = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getDoctorId() == doctorId)
                .filter(apt -> apt.getStartTime().toLocalDate().equals(start.toLocalDate()))
                .filter(apt -> !apt.getStatus().equals("cancelled"))
                .filter(apt -> isTimeOverlap(start, end, apt.getStartTime(), apt.getEndTime()))
                .toList();

        return conflicts.isEmpty();
    }

    /**
     * Kiểm tra 2 khoảng thời gian có overlap không
     */
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

}
