package org.example.oop.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.oop.Data.repositories.AppointmentRepository;
import org.example.oop.Model.Schedule.Appointment;

public class ConflictResolver {
    private final AppointmentRepository appointmentRepository;

    public ConflictResolver(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Kiểm tra conflict cho appointment mới
     */
    public List<String> detectTimeConflict(Appointment newAppointment) {
        List<String> conflicts = new ArrayList<>();

        List<Appointment> existingAppointments = appointmentRepository.findAll()
                .stream()
                .filter(apt -> apt.getDoctorId() == newAppointment.getDoctorId())
                .filter(apt -> apt.getStartTime().toLocalDate().equals(newAppointment.getStartTime().toLocalDate()))
                .filter(apt -> apt.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        for (Appointment existing : existingAppointments) {
            if (isTimeOverlap(newAppointment.getStartTime(), newAppointment.getEndTime(),
                    existing.getStartTime(), existing.getEndTime())) {
                conflicts.add("Conflict with existing appointment: " +
                        existing.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "-" + existing.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        return conflicts;
    }

    /**
     * Kiểm tra conflict khi reschedule (loại trừ chính appointment đang reschedule)
     */
    public List<String> detectTimeConflictForReschedule(Appointment appointment) {
        List<String> conflicts = new ArrayList<>();

        List<Appointment> existingAppointments = appointmentRepository.findAll()
                .stream()
                .filter(apt -> apt.getDoctorId() == appointment.getDoctorId())
                .filter(apt -> apt.getId() != appointment.getId()) // Loại trừ chính nó
                .filter(apt -> apt.getStartTime().toLocalDate().equals(appointment.getStartTime().toLocalDate()))
                .filter(apt -> apt.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        for (Appointment existing : existingAppointments) {
            if (isTimeOverlap(appointment.getStartTime(), appointment.getEndTime(),
                    existing.getStartTime(), existing.getEndTime())) {
                conflicts.add("Reschedule conflict with: " +
                        existing.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "-" + existing.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        return conflicts;
    }

    /**
     * Gợi ý các slot thời gian trống
     */
    public List<String> suggestAlternativeSlots(int doctorId, String date) {
        List<String> availableSlots = new ArrayList<>();

        // Giả định working hours: 8:00 - 17:00 (có thể đọc từ file)
        LocalDateTime workStart = LocalDateTime.parse(date + "T08:00:00");
        LocalDateTime workEnd = LocalDateTime.parse(date + "T17:00:00");

        // Lấy appointments đã book trong ngày
        List<Appointment> bookedAppointments = appointmentRepository.findAll()
                .stream()
                .filter(apt -> apt.getDoctorId() == doctorId)
                .filter(apt -> apt.getStartTime().toLocalDate().toString().equals(date))
                .filter(apt -> apt.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        // Tạo slots 30 phút và check available
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
     * Validate đơn giản có conflict không
     */
    public boolean validateScheduleConflict(int doctorId, LocalDateTime start, LocalDateTime end) {
        List<Appointment> conflicts = appointmentRepository.findAll()
                .stream()
                .filter(apt -> apt.getDoctorId() == doctorId)
                .filter(apt -> apt.getStartTime().toLocalDate().equals(start.toLocalDate()))
                .filter(apt -> apt.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED)
                .filter(apt -> isTimeOverlap(start, end, apt.getStartTime(), apt.getEndTime()))
                .collect(Collectors.toList());

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