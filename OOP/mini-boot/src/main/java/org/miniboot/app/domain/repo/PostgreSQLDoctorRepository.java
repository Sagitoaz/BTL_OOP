package org.miniboot.app.domain.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Doctor;

/**
 * PostgreSQLDoctorRepository: Implementation của DoctorRepository sử dụng PostgreSQL
 * 
 * Repository này kết nối đến database Supabase và thực hiện các thao tác CRUD
 * với bảng Employees (role = 'doctor')
 */
public class PostgreSQLDoctorRepository implements DoctorRepository {
    
    private final DatabaseConfig dbConfig;
    
    public PostgreSQLDoctorRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Lấy tất cả bác sĩ từ database
     * Query từ bảng Employees với điều kiện role = 'doctor'
     */
    @Override
    public List<Doctor> findAll() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT id, firstname, lastname, license_no FROM Employees WHERE role = 'doctor' AND is_active = TRUE";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setId(rs.getInt("id"));
                doctor.setFirstName(rs.getString("firstname"));
                doctor.setLastName(rs.getString("lastname"));
                doctor.setLicenseNo(rs.getString("license_no"));
                doctors.add(doctor);
            }
            
            System.out.println("✅ Found " + doctors.size() + " doctors in database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error fetching doctors: " + e.getMessage());
            e.printStackTrace();
        }
        
        return doctors;
    }
    
    /**
     * Tìm bác sĩ theo ID
     */
    @Override
    public Optional<Doctor> findById(int id) {
        String sql = "SELECT id, firstname, lastname, license_no FROM Employees WHERE id = ? AND role = 'doctor' AND is_active = true";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = new Doctor();
                    doctor.setId(rs.getInt("id"));
                    doctor.setFirstName(rs.getString("firstname"));
                    doctor.setLastName(rs.getString("lastname"));
                    doctor.setLicenseNo(rs.getString("license_no"));
                    return Optional.of(doctor);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding doctor by id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Lưu nhiều bác sĩ cùng lúc (bulk insert)
     * Dùng cho việc seed data
     */
    @Override
    public void saveAll(Collection<Doctor> doctors) {
        String sql = "INSERT INTO employees (username, password, firstname, lastname, role, license_no, is_active) " +
                     "VALUES (?, ?, ?, ?, 'doctor', ?, true) " +
                     "ON CONFLICT (username) DO UPDATE SET " +
                     "firstname = EXCLUDED.firstname, " +
                     "lastname = EXCLUDED.lastname, " +
                     "license_no = EXCLUDED.license_no";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            for (Doctor doctor : doctors) {
                // Tạo username từ firstname + lastname
                String username = (doctor.getFirstName() + doctor.getLastName()).toLowerCase().replaceAll("\\s+", "");
                
                pstmt.setString(1, username);
                pstmt.setString(2, "default123"); // Password mặc định, nên thay đổi
                pstmt.setString(3, doctor.getFirstName());
                pstmt.setString(4, doctor.getLastName());
                pstmt.setString(5, doctor.getLicenseNo());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit(); // Commit transaction
            
            System.out.println("✅ Saved " + doctors.size() + " doctors to database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lưu một bác sĩ
     */
    @Override
    public void saveDoctor(Doctor doctor) {
        String sql = "INSERT INTO employees (username, password, firstname, lastname, role, license_no, is_active) " +
                     "VALUES (?, ?, ?, ?, 'doctor', ?, true) " +
                     "ON CONFLICT (username) DO UPDATE SET " +
                     "firstname = EXCLUDED.firstname, " +
                     "lastname = EXCLUDED.lastname, " +
                     "license_no = EXCLUDED.license_no " +
                     "RETURNING id";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String username = (doctor.getFirstName() + doctor.getLastName()).toLowerCase().replaceAll("\\s+", "");
            
            pstmt.setString(1, username);
            pstmt.setString(2, "default123"); // Password mặc định
            pstmt.setString(3, doctor.getFirstName());
            pstmt.setString(4, doctor.getLastName());
            pstmt.setString(5, doctor.getLicenseNo());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    doctor.setId(rs.getInt("id"));
                    System.out.println("✅ Saved doctor with ID: " + doctor.getId());
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving doctor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
