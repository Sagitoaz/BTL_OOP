package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpResponse;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.nio.charset.StandardCharsets;

public class DatabaseErrorHandler {

    /**
     * Map SQLException to appropriate HTTP response
     */
    public static HttpResponse handleDatabaseException(Exception e) {
        if (e instanceof SQLTimeoutException) {
            return error(504, "TIMEOUT",
                    "Database query timeout. Please try again.");
        }

        if (e instanceof SQLException sqlEx) {
            String sqlState = sqlEx.getSQLState();

            // PostgreSQL error codes
            // 23505 = Unique violation
            if ("23505".equals(sqlState)) {
                return error(409, "CONFLICT",
                        "Record already exists (duplicate key)");
            }

            // 23503 = Foreign key violation
            if ("23503".equals(sqlState)) {
                return error(422, "VALIDATION_FAILED",
                        "Cannot delete: record is referenced by other data");
            }

            // Connection errors
            if (sqlState != null && sqlState.startsWith("08")) {
                return error(503, "SERVICE_UNAVAILABLE",
                        "Database connection error. Please try again later.");
            }

            // Deadlock
            if ("40P01".equals(sqlState)) {
                return error(500, "DB_ERROR",
                        "Database deadlock detected. Please retry.");
            }
        }

        // Generic database error
        return error(500, "DB_ERROR",
                "Database error: " + e.getMessage());
    }

    /**
     * Check if error is retryable (for client-side retry logic)
     */
    public static boolean isRetryable(Exception e) {
        if (e instanceof SQLTimeoutException) {
            return true;
        }

        if (e instanceof SQLException) {
            String sqlState = ((SQLException) e).getSQLState();
            // Connection errors and deadlocks are retryable
            return (sqlState != null && sqlState.startsWith("08")) ||
                    "40P01".equals(sqlState);
        }

        return false;
    }

    private static HttpResponse error(int status, String errorCode, String message) {
        String json = String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
                errorCode, message);
        return HttpResponse.of(status, "application/json",
                json.getBytes(StandardCharsets.UTF_8));
    }
}