package org.miniboot.app.domain.repo;

import org.miniboot.app.AppConfig;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.DoctorSchedule;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho DoctorSchedule (Lịch làm việc của bác sĩ)
 */
public class DoctorScheduleRepository {

    private final DatabaseConfig dbConfig;
    
    public DoctorScheduleRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Lấy tất cả lịch làm việc của một bác sĩ
     */
    public List<DoctorSchedule> findByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctor_schedules WHERE doctor_id = ? ORDER BY day_of_week, start_time";
        List<DoctorSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapRow(rs));
            }
        }
        
        return schedules;
    }
    
    /**
     * Lấy lịch làm việc của bác sĩ trong một ngày cụ thể
     */
    public List<DoctorSchedule> findByDoctorIdAndDay(int doctorId, DayOfWeek dayOfWeek) throws SQLException {
        String sql = "SELECT * FROM doctor_schedules WHERE doctor_id = ? AND day_of_week = CAST(? AS day_of_week_enum) AND is_active = true ORDER BY start_time";
        List<DoctorSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setString(2, dayOfWeek.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapRow(rs));
            }
        }
        
        return schedules;
    }
    
    /**
     * Tạo lịch làm việc mới
     */
    public DoctorSchedule save(DoctorSchedule schedule) throws SQLException {
        if (schedule.getId() == 0) {
            return insert(schedule);
        } else {
            return update(schedule);
        }
    }
    
    /**
     * Insert lịch làm việc mới
     */
    private DoctorSchedule insert(DoctorSchedule schedule) throws SQLException {
        String sql = "INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active, created_at, updated_at) " +
                    "VALUES (?, ?::day_of_week_enum, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, schedule.getDoctorId());
            stmt.setString(2, schedule.getDayOfWeek().name());
            stmt.setTime(3, Time.valueOf(schedule.getStartTime()));
            stmt.setTime(4, Time.valueOf(schedule.getEndTime()));
            stmt.setBoolean(5, schedule.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int affected = stmt.executeUpdate();
            
            if (affected == 0) {
                throw new SQLException("Creating schedule failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating schedule failed, no ID obtained.");
                }
            }
        }
        
        return schedule;
    }
    
    /**
     * Update lịch làm việc
     */
    private DoctorSchedule update(DoctorSchedule schedule) throws SQLException {
        String sql = "UPDATE doctor_schedules SET doctor_id = ?, day_of_week = ?::day_of_week_enum, " +
                    "start_time = ?, end_time = ?, is_active = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, schedule.getDoctorId());
            stmt.setString(2, schedule.getDayOfWeek().name());
            stmt.setTime(3, Time.valueOf(schedule.getStartTime()));
            stmt.setTime(4, Time.valueOf(schedule.getEndTime()));
            stmt.setBoolean(5, schedule.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(7, schedule.getId());
            
            int affected = stmt.executeUpdate();
            
            if (affected == 0) {
                throw new SQLException("Updating schedule failed, no rows affected.");
            }
        }
        
        return schedule;
    }
    
    /**
     * Xóa lịch làm việc
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM doctor_schedules WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            
            return affected > 0;
        }
    }
    
    /**
     * Lấy lịch làm việc theo ID
     */
    public Optional<DoctorSchedule> findById(int id) throws SQLException {
        String sql = "SELECT * FROM doctor_schedules WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Map ResultSet to DoctorSchedule object
     */
    private DoctorSchedule mapRow(ResultSet rs) throws SQLException {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setId(rs.getInt("id"));
        schedule.setDoctorId(rs.getInt("doctor_id"));
        schedule.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));
        schedule.setStartTime(rs.getTime("start_time").toLocalTime());
        schedule.setEndTime(rs.getTime("end_time").toLocalTime());
        schedule.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            schedule.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            schedule.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return schedule;
    }
}
