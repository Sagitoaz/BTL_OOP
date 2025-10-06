package org.example.oop.Model.Schedule;

import java.time.DayOfWeek;

import java.time.LocalTime;
import java.util.Objects;

public class WorkingHours {
    private int doctorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;

    public WorkingHours(int doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, boolean active) {
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }

    public String toDataString() {
        return doctorId + "|" + dayOfWeek + "|" + startTime + "|" + endTime + "|" + active;
    }

    public static WorkingHours fromDataString(String line) {
        String[] p = line.split("\\|");
        int doctorId = Integer.parseInt(p[0]);
        DayOfWeek day = DayOfWeek.valueOf(p[1]);
        LocalTime start = LocalTime.parse(p[2]);
        LocalTime end = LocalTime.parse(p[3]);
        boolean active = Boolean.parseBoolean(p[4]);
        return new WorkingHours(doctorId, day, start, end, active);
    }

    public void validate() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingHours that = (WorkingHours) o;
        return doctorId == that.doctorId && dayOfWeek == that.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId, dayOfWeek);
    }

    public int getDoctorId() {
        return doctorId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
