package org.example.oop.Model.Schedule;

public enum AppointmentStatus {
    SCHEDULED("scheduled", "Đã lên lịch"),
    CONFIRMED("confirmed", "Đã xác nhận"),
    CHECKED_IN("checked_in", "Đã đến khám"),
    IN_PROGRESS("in_progress", "Đang khám"),
    COMPLETED("completed", "Hoàn thành"),
    CANCELLED("cancelled", "Đã hủy"),
    NO_SHOW("no_show", "Vắng mặt");

    private final String code;
    private final String display;

    AppointmentStatus(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static AppointmentStatus fromCode(String code) {
        for (AppointmentStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid appointment status: " + code);
    }
}
