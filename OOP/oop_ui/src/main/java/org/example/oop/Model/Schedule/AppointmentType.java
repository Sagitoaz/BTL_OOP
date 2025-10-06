package org.example.oop.Model.Schedule;

public enum AppointmentType {
    VISIT("visit", "Khám tổng quát"),
    TEST("test", "Đo, xét nghiệm mắt"),
    SURGERY("surgery", "Phẫu thuật hoặc tiểu phẫu");

    private final String code;
    private final String display;

    AppointmentType(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static AppointmentType fromCode(String code) {
        for (AppointmentType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid appointment type: " + code);
    }
}
