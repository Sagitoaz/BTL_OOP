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
     * ✅ BATCH DELETE: Xóa tất cả lịch làm việc của bác sĩ (1 query duy nhất)
     * Sử dụng khi update lịch làm việc để tránh N+1 query problem
     */
    public int deleteByDoctorId(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctor_schedules WHERE doctor_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            int affected = stmt.executeUpdate();
            
            System.out.println("🗑️ Batch deleted " + affected + " schedules for doctor #" + doctorId);
            return affected;
        }
    }
    
    /**
     * ✅ BATCH INSERT: Tạo nhiều lịch làm việc cùng lúc (1 query với VALUES multiple rows)
     * Tối ưu hiệu suất khi tạo lịch làm việc mới
     */
    public List<DoctorSchedule> insertBatch(List<DoctorSchedule> schedules) throws SQLException {
        if (schedules == null || schedules.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Build SQL with multiple VALUES clauses
        StringBuilder sql = new StringBuilder(
            "INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active, created_at, updated_at) VALUES ");
        
        for (int i = 0; i < schedules.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("(?, ?::day_of_week_enum, ?, ?, ?, ?, ?)");
        }
        sql.append(" RETURNING id");
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            LocalDateTime now = LocalDateTime.now();
            int paramIndex = 1;
            
            for (DoctorSchedule schedule : schedules) {
                stmt.setInt(paramIndex++, schedule.getDoctorId());
                stmt.setString(paramIndex++, schedule.getDayOfWeek().name());
                stmt.setTime(paramIndex++, Time.valueOf(schedule.getStartTime()));
                stmt.setTime(paramIndex++, Time.valueOf(schedule.getEndTime()));
                stmt.setBoolean(paramIndex++, schedule.isActive());
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(now));
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(now));
            }
            
            ResultSet rs = stmt.executeQuery();
            int index = 0;
            while (rs.next() && index < schedules.size()) {
                schedules.get(index).setId(rs.getInt("id"));
                schedules.get(index).setCreatedAt(now);
                schedules.get(index).setUpdatedAt(now);
                index++;
            }
            
            System.out.println("✅ Batch inserted " + schedules.size() + " schedules");
            return schedules;
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
