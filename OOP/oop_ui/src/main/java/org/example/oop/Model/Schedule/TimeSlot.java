package org.example.oop.Model.Schedule;

import java.time.LocalTime;

public class TimeSlot {
    private int id;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;
    private String type;
    private boolean available;

    public TimeSlot(int id, LocalTime startTime, LocalTime endTime, int durationMinutes, String type, boolean available) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.type = type;
        this.available = available;
    }

    public String toDataString() {
        return id + "|" + startTime + "|" + endTime + "|" + durationMinutes + "|" + type + "|" + available;
    }

    public static TimeSlot fromDataString(String line) {
        String[] p = line.split("\\|");
        int id = Integer.parseInt(p[0]);
        LocalTime start = LocalTime.parse(p[1]);
        LocalTime end = LocalTime.parse(p[2]);
        int duration = Integer.parseInt(p[3]);
        String type = p[4];
        boolean available = Boolean.parseBoolean(p[5]);
        return new TimeSlot(id, start, end, duration, type, available);
    }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    @Override
    public String toString() {
        return "[" + startTime + "–" + endTime + "] " + (available ? "✅" : "❌");
    }

    public int getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
