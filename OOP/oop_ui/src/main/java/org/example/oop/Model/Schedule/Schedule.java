package org.example.oop.Model.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Schedule {
    private int doctorId;
    private LocalDate date;
    private ArrayList<TimeSlot> timeSlots = new ArrayList<>();
    private boolean available;

    public Schedule(int doctorId, LocalDate date, ArrayList<TimeSlot> timeSlots, boolean available) {
        this.doctorId = doctorId;
        this.date = date;
        this.timeSlots = timeSlots;
        this.available = available;
    }

    public static Schedule generateFrom(WorkingHours wh, LocalDate date, int defaultDuration) {
        ArrayList<TimeSlot> slots = new ArrayList<>();
        LocalTime current = wh.getStartTime();
        int id = 1;
        while (current.plusMinutes(defaultDuration).isBefore(wh.getEndTime()) || current.plusMinutes(defaultDuration).equals(wh.getEndTime())) {
            LocalTime end = current.plusMinutes(defaultDuration);
            TimeSlot slot = new TimeSlot(id++, current, end, defaultDuration, "", true);
            slots.add(slot);
            current = end;
        }
        return new Schedule(wh.getDoctorId(), date, slots, true);
    }

    public void applyAppointments(ArrayList<Appointment> appointments) {
        for (Appointment appt : appointments) {
            if (!appt.getStartTime().toLocalDate().equals(this.date)) continue;

            LocalTime apptStart = appt.getStartTime().toLocalTime();
            LocalTime apptEnd = appt.getEndTime().toLocalTime();

            for (TimeSlot slot : timeSlots) {
                if (slot.overlaps(apptStart, apptEnd)) {
                    slot.setAvailable(false);
                }
            }
        }

        this.available = timeSlots.stream().anyMatch(TimeSlot::isAvailable);
    }

    public ArrayList<TimeSlot> getAvailableSlots() {
        return timeSlots.stream()
                .filter(TimeSlot::isAvailable)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String toString() {
        return "Schedule for doctor " + doctorId + " on " + date + ": " +
                getAvailableSlots().size() + " slots available.";
    }

    public int getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public ArrayList<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setTimeSlots(ArrayList<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }
}
