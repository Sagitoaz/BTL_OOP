package org.miniboot.app.test;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.PostgreSQLDoctorRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * DatabaseConnectionTest: Class để test kết nối database
 * 
 * Chạy class này để kiểm tra xem kết nối đến Supabase có thành công không
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🔌 TESTING DATABASE CONNECTION TO SUPABASE");
        System.out.println("=".repeat(60));
        
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        
        // 1. In thông tin cấu hình
        System.out.println("\n📋 Step 1: Checking Configuration");
        dbConfig.printConfig();
        
        // 2. Test kết nối cơ bản
        System.out.println("\n🔗 Step 2: Testing Basic Connection");
        if (!dbConfig.testConnection()) {
            System.err.println("❌ Connection test failed!");
            System.err.println("🔧 Please check:");
            System.err.println("   - DB_PASSWORD environment variable is set correctly");
            System.err.println("   - Network connection to Supabase");
            System.err.println("   - Database credentials are correct");
            return;
        }
        System.out.println("✅ Connection test passed!");
        
        // 3. Test query database
        System.out.println("\n📊 Step 3: Testing Database Query");
        testDatabaseQuery(dbConfig);
        
        // 4. Test Doctor Repository
        System.out.println("\n👨‍⚕️ Step 4: Testing Doctor Repository");
        testDoctorRepository();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✨ ALL TESTS COMPLETED!");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Test query trực tiếp database
     */
    private static void testDatabaseQuery(DatabaseConfig dbConfig) {
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Test query đơn giản
            String sql = "SELECT COUNT(*) as count FROM Employees WHERE role = 'doctor'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Found " + count + " doctors in database");
            }
            
            // Test query chi tiết
            sql = "SELECT id, firstname, lastname FROM Employees WHERE role = 'doctor' LIMIT 5";
            rs = stmt.executeQuery(sql);
            
            System.out.println("\n📋 Sample doctors from database:");
            int i = 1;
            while (rs.next()) {
                System.out.printf("   %d. Dr. %s %s (ID: %d)%n", 
                    i++,
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getInt("id"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database query failed: " + e.getMessage());
        }
    }
    
    /**
     * Test Doctor Repository
     */
    private static void testDoctorRepository() {
        try {
            DoctorRepository doctorRepo = new PostgreSQLDoctorRepository();
            
            // Test findAll
            System.out.println("\n🔍 Testing findAll()...");
            List<Doctor> doctors = doctorRepo.findAll();
            System.out.println("✅ Retrieved " + doctors.size() + " doctors");
            
            if (!doctors.isEmpty()) {
                // Test findById
                Doctor firstDoctor = doctors.get(0);
                System.out.println("\n🔍 Testing findById(" + firstDoctor.getId() + ")...");
                doctorRepo.findById(firstDoctor.getId()).ifPresent(doctor -> {
                    System.out.printf("✅ Found: Dr. %s %s (License: %s)%n",
                        doctor.getFirstName(),
                        doctor.getLastName(),
                        doctor.getLicenseNo());
                });
            }
            
        } catch (Exception e) {
            System.err.println("❌ Repository test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
