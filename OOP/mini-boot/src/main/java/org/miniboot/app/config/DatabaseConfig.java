package org.miniboot.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.miniboot.app.AppConfig;

/**
 * DatabaseConfig: Quản lý kết nối đến PostgreSQL Database (Supabase)
 * 
 * Sử dụng Singleton pattern để đảm bảo chỉ có một connection instance
 * được sử dụng xuyên suốt ứng dụng.
 */
public class DatabaseConfig {
    
    // Singleton instance
    private static DatabaseConfig instance;
    private Connection connection;
    
    // Database credentials
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;
    
    /**
     * Constructor private để implement Singleton pattern
     * Đọc thông tin kết nối từ environment variables hoặc system properties
     */
    private DatabaseConfig() {
        // Đọc từ environment variables hoặc system properties, có giá trị mặc định
        DB_URL = getConfig(AppConfig.DB_URL_KEY,
            "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres");
        DB_USER = getConfig(AppConfig.DB_USER_KEY,
            "postgres.dwcpuomioxgqznusjewq");
        DB_PASSWORD = getConfig(AppConfig.DB_PASSWORD_KEY, "Nguhotuongd23@");
        
        // Kiểm tra nếu password chưa được set
        if (DB_PASSWORD.isEmpty()) {
            System.err.println("⚠️  WARNING: Database password is not set!");
            System.err.println("Please set DB_PASSWORD environment variable or system property");
        }
    }
    
    /**
     * Helper method để đọc config từ nhiều nguồn theo thứ tự ưu tiên:
     * 1. System property (-D parameter)
     * 2. Environment variable
     * 3. Default value
     */
    private String getConfig(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) return value;
        
        value = System.getenv(key);
        if (value != null) return value;
        
        return defaultValue;
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    /**
     * Lấy connection đến database
     * TẠO CONNECTION MỚI cho mỗi request để tránh lock và timeout
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // TẠO CONNECTION MỚI mỗi lần gọi (không dùng singleton connection)
            Connection newConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            System.out.println("Database connection established successfully!");
            System.out.println("Connected to: " + DB_URL);
            
            return newConnection;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found!", e);
        }
    }
    
    /**
     * Đóng connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * In thông tin cấu hình database (ẩn password)
     */
    public void printConfig() {
        System.out.println("Database Configuration:");
        System.out.println("  URL: " + DB_URL);
        System.out.println("  User: " + DB_USER);
        System.out.println("  Password: " + (DB_PASSWORD.isEmpty() ? "❌ NOT SET" : "✅ SET (hidden)"));
    }
}
