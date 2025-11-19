package org.miniboot.app.domain.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import com.google.gson.annotations.SerializedName;

/**
 * DoctorSchedule - Lịch làm việc của bác sĩ
 * 
 * Đại diện cho ca làm việc định kỳ của bác sĩ theo ngày trong tuần
 * Ví dụ: Bác sĩ A làm việc T2-T6, mỗi ngày từ 8:00-12:00 và 13:00-17:00
 */
public class DoctorSchedule {
    private int id;
    private int doctorId;              // Foreign key to Employees.id (role='doctor')
    private DayOfWeek dayOfWeek;       // MONDAY, TUESDAY, etc.
    private LocalTime startTime;       // Giờ bắt đầu ca, ví dụ: 08:00
    private LocalTime endTime;         // Giờ kết thúc ca, ví dụ: 17:00
    
    @SerializedName(value = "active", alternate = {"isActive"})
    private boolean isActive;          // Có đang hoạt động không (để tạm ngưng)
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public DoctorSchedule() {
        this.isActive = true;
    }
    
    public DoctorSchedule(int id, int doctorId, DayOfWeek dayOfWeek, 
                         LocalTime startTime, LocalTime endTime, boolean isActive,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
    
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    // Alias for JSON deserialization compatibility
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Kiểm tra xem một thời điểm có nằm trong ca làm việc này không
     */
    public boolean containsTime(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    /**
     * Kiểm tra xem khoảng thời gian có overlap với ca làm việc không
     */
    public boolean overlaps(LocalTime start, LocalTime end) {
        return start.isBefore(endTime) && end.isAfter(startTime);
    }
    
    @Override
    public String toString() {
        return "DoctorSchedule{" +
                "id=" + id +
                ", doctorId=" + doctorId +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isActive=" + isActive +
                '}';
    }
}
