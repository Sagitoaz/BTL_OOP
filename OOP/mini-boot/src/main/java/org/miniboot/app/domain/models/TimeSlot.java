package org.miniboot.app.domain.models;

import java.time.LocalTime;

/**
 * TimeSlot - DTO (Data Transfer Object) đại diện cho 1 khoảng thời gian slot
 *
 * ⚠️ QUAN TRỌNG: Class này KHÔNG map với bảng trong database!
 * Chỉ dùng để:
 * - Tính toán slot trống (từ WorkingHours - Appointments)
 * - Trả về cho Frontend qua API
 * - Hiển thị danh sách slot available cho user chọn
 *
 * Logic:
 * 1. Lấy giờ làm việc bác sĩ từ WorkingHours (hoặc hardcode 8:00-17:00)
 * 2. Chia thành các slot 30 phút
 * 3. Check slot nào đã có appointment → available = false
 * 4. Trả về danh sách TimeSlot cho frontend
 */

public class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
    private int duration; // Tính bằng phút
    private boolean available;

    public TimeSlot(LocalTime startTime, LocalTime endTime, int duration, boolean available) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.available = available;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
