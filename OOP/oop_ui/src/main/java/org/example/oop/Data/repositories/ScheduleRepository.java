package org.example.oop.Data.repositories;

import org.example.oop.Model.Schedule.Appointment;
import org.example.oop.Model.Schedule.Schedule;
import org.example.oop.Model.Schedule.TimeSlot;
import org.example.oop.Model.Schedule.WorkingHours;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScheduleRepository {
    private final WorkingHoursRepository workingHoursRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleRepository(WorkingHoursRepository workingHoursRepository, AppointmentRepository appointmentRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Optional<Schedule> getSchedule(int doctorId, LocalDate date) {
        Optional<WorkingHours> workingHours = workingHoursRepository.findByDoctorIdAndDay(doctorId, date.getDayOfWeek());

        if (workingHours.isEmpty() || !workingHours.get().isActive()) {
            return Optional.empty();
        }

        Schedule schedule = Schedule.generateFrom(workingHours.get(), date, 30);
        List<Appointment> appointments = appointmentRepository.findByDoctorAndDate(doctorId, date);
        schedule.applyAppointments(new ArrayList<>(appointments));

        return Optional.of(schedule);
    }

    public List<TimeSlot> getAvailableSlots(int doctorId, LocalDate date) {
        Optional<Schedule> schedule = getSchedule(doctorId, date);
        return schedule.map(Schedule::getAvailableSlots).orElse(new ArrayList<>());
    }

    public boolean isSlotAvailable(int doctorId, LocalDate date, TimeSlot slot) {
        List<TimeSlot> available = getAvailableSlots(doctorId, date);
        return available.stream().anyMatch(s ->
                s.getStartTime().equals(slot.getStartTime())
                        && s.getEndTime().equals(slot.getEndTime())
        );
    }
}
