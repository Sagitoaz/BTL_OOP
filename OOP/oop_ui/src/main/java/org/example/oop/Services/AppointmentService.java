package org.example.oop.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.oop.Data.repositories.AppointmentRepository;
import org.example.oop.Model.Schedule.Appointment;

public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ConflictResolver conflictResolver;

    public AppointmentService() {
        this.appointmentRepository = new AppointmentRepository();
        this.conflictResolver = new ConflictResolver(appointmentRepository);
    }

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.conflictResolver = new ConflictResolver(appointmentRepository);
    }

    /**
     * Validate appointment data trước khi book
     */
    public boolean validateAppointment(Appointment appointment) {
        if (appointment == null) return false;

        if (appointment.getCustomerId() <= 0 || appointment.getDoctorId() <= 0) return false;

        if (appointment.getStartTime() == null || appointment.getEndTime() == null) return false;

        if (!appointment.getStartTime().isBefore(appointment.getEndTime())) return false;

        if (appointment.getStartTime().isBefore(LocalDateTime.now().minusHours(1))) return false;

        if (appointment.getAppointmentType() == null) return false;

        return true;
    }

    /**
     * Book appointment mới
     */
    public Appointment bookAppointment(Appointment appointment) {
        if (!validateAppointment(appointment)) {
            throw new RuntimeException("Invalid appointment data. Please check all required fields.");
        }

        List<String> conflicts = conflictResolver.detectTimeConflict(appointment);
        if (!conflicts.isEmpty()) {
            String conflictMessage = "Appointment conflict detected:\n" + String.join("\n", conflicts);
            throw new RuntimeException(conflictMessage);
        }

        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setStatus(org.example.oop.Model.Schedule.AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    /**
     * Cancel appointment với reason
     */
    public boolean cancelAppointment(int appointmentId, String reason) {
        try {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
            if (optionalAppointment.isEmpty()) return false;

            Appointment appointment = optionalAppointment.get();
            if (appointment.getStatus() == org.example.oop.Model.Schedule.AppointmentStatus.COMPLETED || 
                appointment.getStatus() == org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED) {
                return false;
            }

            appointment.setStatus(org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED);
            appointment.setUpdatedAt(LocalDateTime.now());

            String currentNote = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(currentNote + "\nCancelled: " + reason + " (at " + LocalDateTime.now() + ")");

            appointmentRepository.save(appointment);
            return true;
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reschedule appointment
     */
    public Appointment rescheduleAppointment(int appointmentId, LocalDateTime newStart, LocalDateTime newEnd) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Appointment not found with ID: " + appointmentId);
        }

        Appointment appointment = optionalAppointment.get();
        if (appointment.getStatus() == org.example.oop.Model.Schedule.AppointmentStatus.COMPLETED || 
            appointment.getStatus() == org.example.oop.Model.Schedule.AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot reschedule " + appointment.getStatus() + " appointment");
        }

        if (newStart == null || newEnd == null || !newStart.isBefore(newEnd)) {
            throw new RuntimeException("Invalid new time range");
        }

        if (newStart.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new RuntimeException("Cannot reschedule to past time");
        }

        // Tạo temp appointment với constructor có đầy đủ tham số
        Appointment tmpAppointment = new Appointment(
            appointment.getId(), 
            appointment.getCustomerId(), 
            appointment.getDoctorId(),
            appointment.getAppointmentType(),
            newStart,
            newEnd
        );

        List<String> conflicts = conflictResolver.detectTimeConflictForReschedule(tmpAppointment);
        if (!conflicts.isEmpty()) {
            String conflictMessage = "Reschedule conflict detected:\n" + String.join("\n", conflicts);
            throw new RuntimeException(conflictMessage);
        }

        LocalDateTime oldStart = appointment.getStartTime();
        LocalDateTime oldEnd = appointment.getEndTime();

        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);
        appointment.setUpdatedAt(LocalDateTime.now());

        String rescheduleNote = String.format("\nRescheduled from %s-%s to %s-%s (at %s)",
                oldStart, oldEnd, newStart, newEnd, LocalDateTime.now());
        String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
        appointment.setNotes(currentNotes + rescheduleNote);

        return appointmentRepository.save(appointment);
    }

    /**
     * Confirm appointment (từ scheduled → confirmed)
     */
    public boolean confirmAppointment(int appointmentId) {
        try {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);

            if (optionalAppointment.isEmpty()) {
                return false;
            }

            Appointment appointment = optionalAppointment.get();

            // Chỉ confirm được appointment đang scheduled
            if (appointment.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.SCHEDULED) {
                return false;
            }

            appointment.setStatus(org.example.oop.Model.Schedule.AppointmentStatus.CONFIRMED);
            appointment.setUpdatedAt(LocalDateTime.now());

            appointmentRepository.save(appointment);
            return true;
        } catch (Exception e) {
            System.err.println("Error confirming appointment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Complete appointment với notes
     */
    public boolean completeAppointment(int appointmentId, String completionNotes) {
        try {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);

            if (optionalAppointment.isEmpty()) return false;

            Appointment appointment = optionalAppointment.get();

            if (appointment.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CONFIRMED) return false;

            appointment.setStatus(org.example.oop.Model.Schedule.AppointmentStatus.COMPLETED);
            appointment.setUpdatedAt(LocalDateTime.now());

            String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(currentNotes + "\nCompleted: " + completionNotes + " (at " + LocalDateTime.now() + ")");

            appointmentRepository.save(appointment);
            return true;

        } catch (Exception e) {
            System.err.println("Error completing appointment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start appointment (khi patient đến)
     */
    public boolean startAppointment(int appointmentId) {
        try {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);

            if (optionalAppointment.isEmpty()) return false;

            Appointment appointment = optionalAppointment.get();

            if (appointment.getStatus() != org.example.oop.Model.Schedule.AppointmentStatus.CONFIRMED) return false;

            appointment.setStatus(org.example.oop.Model.Schedule.AppointmentStatus.IN_PROGRESS);
            appointment.setUpdatedAt(LocalDateTime.now());

            appointmentRepository.save(appointment);
            return true;

        } catch (Exception e) {
            System.err.println("Error starting appointment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get appointments by customer
     */
    public List<Appointment> getAppointmentsByCustomer(int customerId) {
        return appointmentRepository.findAll()
                .stream()
                .filter(appointment -> appointment.getCustomerId() == customerId)
                .collect(java.util.stream.Collectors.toList());
    }
}