package org.example.oop.Model.Schedule;

/**
 * AppointmentStatus - trạng thái của một lịch hẹn trong hệ thống.
 * Theo database: enum('scheduled','confirmed','checked_in','in_progress','completed','cancelled','no_show')
 */
public enum AppointmentStatus {
    SCHEDULED("scheduled"),
    CONFIRMED("confirmed"),
    CHECKED_IN("checked_in"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    NO_SHOW("no_show");

    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AppointmentStatus fromString(String text) {
        for (AppointmentStatus status : AppointmentStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No AppointmentStatus with value " + text + " found");
    }

    public static AppointmentStatus fromValue(String value) {
        return fromString(value);
    }
}
