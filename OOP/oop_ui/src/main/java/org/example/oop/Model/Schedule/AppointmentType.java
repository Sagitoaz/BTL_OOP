package org.example.oop.Model.Schedule;

/**
 * AppointmentType - loại hình lịch hẹn trong hệ thống.
 * Theo database: enum('visit','test','surgery')
 */
public enum AppointmentType {
    VISIT("visit"),
    TEST("test"),
    SURGERY("surgery"),
    BLOCKED("blocked"); // Thêm cho blocking appointments

    private final String value;

    AppointmentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AppointmentType fromString(String text) {
        for (AppointmentType type : AppointmentType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No AppointmentType with value " + text + " found");
    }

    public static AppointmentType fromValue(String value) {
        return fromString(value);
    }
}
