package org.miniboot.app.domain.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * AppointmentType - loại hình lịch hẹn trong hệ thống.
 * Theo database: enum('visit','test','surgery')
 */
public enum AppointmentType {
    VISIT("visit"),
    Test("test"),
    SURGERY("surgery"),
    BLOCKED("blocked"); // Thêm cho blocking appointments


    private final String value;

    AppointmentType(String value) {
        this.value = value;
    }

    @JsonValue // Jackson serialize bằng method này
    public String getValue() {
        return value;
    }

    @JsonCreator // Jackson deserialize bằng method này
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
