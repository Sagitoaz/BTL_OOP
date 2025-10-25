package org.miniboot.app.domain.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.AppointmentType;

/**
 * PostgreSQLAppointmentRepository: Implementation của AppointmentRepository sử dụng PostgreSQL
 *
 * Repository này kết nối đến database Supabase và thực hiện các thao tác CRUD
 * với bảng Appointments
 */
public class PostgreSQLAppointmentRepository implements AppointmentRepository {

    private final DatabaseConfig dbConfig;

    public PostgreSQLAppointmentRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Lấy tất cả appointments từ database
     */
    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT id, customer_id, doctor_id, appointment_type, notes, " +
                     "start_time, end_time, status, created_at, updated_at " +
                     "FROM Appointments ORDER BY start_time DESC";

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment appointment = mapResultSetToAppointment(rs);
                appointments.add(appointment);
            }

            System.out.println("✅ Found " + appointments.size() + " appointments in database");

        } catch (SQLException e) {
            System.err.println("❌ Error fetching appointments: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Tìm appointment theo ID
     */
    @Override
    public Optional<Appointment> findById(Integer id) {
        String sql = "SELECT id, customer_id, doctor_id, appointment_type, notes, " +
                     "start_time, end_time, status, created_at, updated_at " +
                     "FROM Appointments WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    return Optional.of(appointment);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error finding appointment by id: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Lưu appointment (INSERT nếu id = 0, UPDATE nếu id > 0)
     */
    @Override
    public Appointment save(Appointment appointment) {
        // Auto-fill timestamps nếu null
        LocalDateTime now = LocalDateTime.now();
        if (appointment.getCreatedAt() == null) {
            appointment.setCreatedAt(now);
        }
        if (appointment.getUpdatedAt() == null) {
            appointment.setUpdatedAt(now);
        }

        if (appointment.getId() == 0) {
            return insert(appointment);
        } else {
            return update(appointment);
        }
    }

    /**
     * INSERT appointment mới
     */
    private Appointment insert(Appointment appointment) {
        // Cast sang ENUM type cho PostgreSQL
        String sql = "INSERT INTO Appointments (customer_id, doctor_id, appointment_type, " +
                     "notes, start_time, end_time, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?::appointment_type, ?, ?, ?, ?::appointment_status, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            pstmt.setInt(1, appointment.getCustomerId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setString(3, appointment.getAppointmentType() != null ? appointment.getAppointmentType().getValue() : "visit");
            pstmt.setString(4, appointment.getNotes());
            pstmt.setTimestamp(5, Timestamp.valueOf(appointment.getStartTime()));
            pstmt.setTimestamp(6, Timestamp.valueOf(appointment.getEndTime()));
            pstmt.setString(7, appointment.getStatus() != null ? appointment.getStatus().getValue() : "scheduled");
            pstmt.setTimestamp(8, Timestamp.valueOf(now));
            pstmt.setTimestamp(9, Timestamp.valueOf(now));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating appointment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                    appointment.setCreatedAt(now);
                    appointment.setUpdatedAt(now);
                    System.out.println("✅ Appointment created with ID: " + appointment.getId());
                } else {
                    throw new SQLException("Creating appointment failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return appointment;
    }

    /**
     * UPDATE appointment hiện có
     */
    private Appointment update(Appointment appointment) {
        // Cast sang ENUM type cho PostgreSQL
        String sql = "UPDATE Appointments SET customer_id = ?, doctor_id = ?, " +
                     "appointment_type = ?::appointment_type, notes = ?, start_time = ?, end_time = ?, " +
                     "status = ?::appointment_status, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            pstmt.setInt(1, appointment.getCustomerId());
            pstmt.setInt(2, appointment.getDoctorId());
            // Convert ENUM → String for DB
            pstmt.setString(3, appointment.getAppointmentType().getValue());
            pstmt.setString(4, appointment.getNotes());
            pstmt.setTimestamp(5, Timestamp.valueOf(appointment.getStartTime()));
            pstmt.setTimestamp(6, Timestamp.valueOf(appointment.getEndTime()));
            // Convert ENUM → String for DB
            pstmt.setString(7, appointment.getStatus().getValue());
            pstmt.setTimestamp(8, Timestamp.valueOf(now));
            pstmt.setInt(9, appointment.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("❌ Appointment update failed, no rows affected.");
                return null;
            }

            appointment.setUpdatedAt(now);
            System.out.println("✅ Appointment updated: ID = " + appointment.getId());

        } catch (SQLException e) {
            System.err.println("❌ Error updating appointment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return appointment;
    }

    /**
     * Tìm appointments theo doctor_id và date
     * date format: "2024-10-14" (YYYY-MM-DD)
     */
    @Override
    public List<Appointment> findByDoctorIdAndDate(Integer doctorId, String date) {
        List<Appointment> appointments = new ArrayList<>();

        // Query lấy tất cả appointments trong ngày
        // start_time >= '2024-10-14 00:00:00' AND start_time < '2024-10-15 00:00:00'
        String sql = "SELECT id, customer_id, doctor_id, appointment_type, notes, " +
                     "start_time, end_time, status, created_at, updated_at " +
                     "FROM Appointments " +
                     "WHERE doctor_id = ? " +
                     "AND start_time >= ?::date " +
                     "AND start_time < (?::date + interval '1 day') " +
                     "ORDER BY start_time";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);
            pstmt.setString(3, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    appointments.add(appointment);
                }
            }

            System.out.println("✅ Found " + appointments.size() +
                             " appointments for doctor " + doctorId + " on " + date);

        } catch (SQLException e) {
            System.err.println("❌ Error finding appointments by doctor and date: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Lọc appointments với nhiều điều kiện (tất cả optional)
     * Chỉ những param != null mới được thêm vào WHERE clause
     */
    @Override
    public List<Appointment> findWithFilters(
            Integer doctorId,
            Integer customerId,
            String status,
            String fromDate,
            String toDate,
            String searchKeyword) {

        List<Appointment> appointments = new ArrayList<>();

        // 1. Build SQL động với WHERE 1=1 (luôn đúng, để append AND dễ dàng)
        StringBuilder sql = new StringBuilder(
                "SELECT id, customer_id, doctor_id, appointment_type, notes, " +
                        "start_time, end_time, status, created_at, updated_at " +
                        "FROM Appointments WHERE 1=1"
        );

        // 2. List để lưu params (theo thứ tự)
        List<Object> params = new ArrayList<>();

        // 3. Append WHERE conditions nếu param != null
        if (doctorId != null) {
            sql.append(" AND doctor_id = ?");
            params.add(doctorId);
        }

        if (customerId != null) {
            sql.append(" AND customer_id = ?");
            params.add(customerId);
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (fromDate != null && !fromDate.isEmpty()) {
            sql.append(" AND DATE(start_time) >= CAST(? AS DATE)");
            params.add(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            sql.append(" AND DATE(start_time) <= CAST(? AS DATE)");
            params.add(toDate);
        }

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            sql.append(" AND notes ILIKE ?");
            params.add("%" + searchKeyword + "%"); // ILIKE = case-insensitive LIKE
        }

        // 4. Sort theo start_time giảm dần (mới nhất lên đầu)
        sql.append(" ORDER BY start_time DESC");

        // 5. Execute query
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set parameters theo thứ tự
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                }
            }

            System.out.println("🔍 Executing SQL: " + sql);
            System.out.println("📌 Parameters: " + params);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    appointments.add(appointment);
                }
            }

            System.out.println("✅ Found " + appointments.size() + " appointments with filters");

        } catch (SQLException e) {
            System.err.println("❌ Error in findWithFilters: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Helper method: Map ResultSet to Appointment object
     */
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setCustomerId(rs.getInt("customer_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));

        // Convert String from DB → AppointmentType ENUM
        String typeStr = rs.getString("appointment_type");
        if (typeStr != null) {
            appointment.setAppointmentType(AppointmentType.fromValue(typeStr));
        }

        appointment.setNotes(rs.getString("notes"));

        // Convert SQL Timestamp to LocalDateTime
        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            appointment.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) {
            appointment.setEndTime(endTs.toLocalDateTime());
        }

        // Convert String from DB → AppointmentStatus ENUM
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            appointment.setStatus(AppointmentStatus.fromValue(statusStr));
        }

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            appointment.setCreatedAt(createdTs.toLocalDateTime());
        }

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            appointment.setUpdatedAt(updatedTs.toLocalDateTime());
        }

        return appointment;
    }
}
