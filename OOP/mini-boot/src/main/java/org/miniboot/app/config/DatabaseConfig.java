package org.miniboot.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.miniboot.app.AppConfig;

/**
 * DatabaseConfig: Quản lý kết nối đến PostgreSQL Database (Supabase)
 * 
 * ⚠️ QUAN TRỌNG: 
 * - Mỗi lần gọi getConnection() sẽ tạo connection MỚI
 * - PHẢI dùng try-with-resources để tự động đóng connection
 * - Nếu không đóng connection → "Max client connections reached"
 */
public class DatabaseConfig {
    
    // Singleton instance
    private static DatabaseConfig instance;
    
    // Database credentials
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;
    
    /**
     * Constructor private để implement Singleton pattern
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
     * 
     * ⚠️ QUAN TRỌNG: Tạo connection MỚI mỗi lần gọi
     * PHẢI dùng trong try-with-resources để tự động đóng:
     * 
     * try (Connection conn = dbConfig.getConnection()) {
     *     // Use connection
     * } // Connection tự động đóng ở đây
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Thêm connection properties để tối ưu
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("connectTimeout", "10"); // 10 giây timeout
            props.setProperty("socketTimeout", "30");   // 30 giây socket timeout
            props.setProperty("tcpKeepAlive", "true");  // Keep alive
            
            // Tạo connection mới
            Connection conn = DriverManager.getConnection(DB_URL, props);
            
            // Set auto-commit = true để không giữ transaction lâu
            conn.setAutoCommit(true);
            
            System.out.println("✅ Database connection established");
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found!", e);
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            throw e;
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
