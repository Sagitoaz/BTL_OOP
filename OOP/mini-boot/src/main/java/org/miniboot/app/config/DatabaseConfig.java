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
        // Sử dụng constants từ DatabaseConstants
        DB_URL = getConfig(DatabaseConstants.CONFIG_DB_URL, DatabaseConstants.DEFAULT_DB_URL);
        DB_USER = getConfig(DatabaseConstants.CONFIG_DB_USER, DatabaseConstants.DEFAULT_DB_USER);
        DB_PASSWORD = getConfig(DatabaseConstants.CONFIG_DB_PASSWORD, DatabaseConstants.DEFAULT_DB_PASSWORD);

        // Kiểm tra nếu password chưa được set
        if (DB_PASSWORD.isEmpty()) {
            System.err.println("⚠️  WARNING: Database password is not set!");
            System.err.println("Please set " + DatabaseConstants.CONFIG_DB_PASSWORD + " environment variable or system property");
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
        if (value != null)
            return value;

        value = System.getenv(key);
        if (value != null)
            return value;

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
     * Tạo connection mới mỗi lần thay vì reuse (để tránh connection block)
     * Connection sẽ được đóng bởi caller trong try-with-resources
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL driver
            Class.forName(DatabaseConstants.DEFAULT_DB_DRIVER);

            // Set connection timeout
            DriverManager.setLoginTimeout(DatabaseConstants.DEFAULT_DB_TIMEOUT);

            // Tạo connection MỚI mỗi lần (không cache)
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            System.out.println(SuccessMessages.SUCCESS_DB_CONNECTION);
            System.out.println("Connected to: " + DB_URL);

            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException(ErrorMessages.ERROR_DB_DRIVER, e);
        } catch (SQLException e) {
            System.err.println("❌ " + ErrorMessages.ERROR_DB_CONNECTION + ":");
            System.err.println("   URL: " + DB_URL);
            System.err.println("   User: " + DB_USER);
            System.err.println("   Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Đóng connection (nếu cần)
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully!");
            } catch (SQLException e) {
                System.err.println("❌ Failed to close database connection: " + e.getMessage());
            }
        }
    }

    /**
     * In thông tin cấu hình database
     */
    public void printConfig() {
        System.out.println("Database Configuration:");
        System.out.println("  URL: " + DB_URL);
        System.out.println("  User: " + DB_USER);
        System.out.println("  Password: " + (DB_PASSWORD.isEmpty() ? "NOT SET" : "***"));
    }

    /**
     * Test kết nối database
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
