package org.example.oop.Utils;

/**
 * HttpException - Custom exception cho HTTP errors
 * 
 * Exception này được throw khi gặp lỗi HTTP (status code != 2xx)
 * Chứa thông tin về status code và error message để xử lý chi tiết
 * 
 * Usage:
 * 
 * <pre>
 * if (response.statusCode() != 200) {
 *     String errorMsg = ErrorHandler.getErrorMessage(response.statusCode());
 *     throw new HttpException(response.statusCode(), errorMsg);
 * }
 * 
 * // Catch và xử lý:
 * try {
 *     // HTTP request
 * } catch (HttpException e) {
 *     if (e.getStatusCode() == 401) {
 *         // Redirect to login
 *     } else if (ErrorHandler.shouldRetry(e.getStatusCode())) {
 *         // Retry logic
 *     }
 * }
 * </pre>
 * 
 * @author Person 4 - Error Handling & Service Layer Developer
 * @since 2025-11-02
 */
public class HttpException extends Exception {

    /**
     * HTTP status code (400, 401, 403, 404, 409, 422, 429, 500, 503, 504, etc.)
     */
    private final int statusCode;

    /**
     * Error message (user-friendly hoặc từ server)
     */
    private final String errorMessage;

    /**
     * Response body từ server (optional, cho debugging)
     */
    private final String responseBody;

    /**
     * Constructor với status code và error message
     * 
     * @param statusCode   HTTP status code
     * @param errorMessage Error message
     */
    public HttpException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.responseBody = null;
    }

    /**
     * Constructor với status code, error message và response body
     * 
     * @param statusCode   HTTP status code
     * @param errorMessage Error message
     * @param responseBody Response body từ server (cho debugging)
     */
    public HttpException(int statusCode, String errorMessage, String responseBody) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.responseBody = responseBody;
    }

    /**
     * Constructor với status code, error message và cause
     * 
     * @param statusCode   HTTP status code
     * @param errorMessage Error message
     * @param cause        Exception gốc
     */
    public HttpException(int statusCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.responseBody = null;
    }

    /**
     * Lấy HTTP status code
     * 
     * @return HTTP status code (400, 401, 403, 404, 409, 422, 429, 500, 503, 504)
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Lấy error message
     * 
     * @return Error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Lấy response body từ server (nếu có)
     * 
     * @return Response body hoặc null
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Kiểm tra xem có phải client error (4xx) hay không
     * 
     * @return true nếu status code trong khoảng 400-499
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Kiểm tra xem có phải server error (5xx) hay không
     * 
     * @return true nếu status code trong khoảng 500-599
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * Kiểm tra xem có nên retry request hay không
     * Dựa trên ErrorHandler.shouldRetry()
     * 
     * @return true nếu nên retry (503, 504, 500)
     */
    public boolean shouldRetry() {
        return ErrorHandler.shouldRetry(statusCode);
    }

    /**
     * Lấy user-friendly error message từ ErrorHandler
     * 
     * @return Thông báo lỗi tiếng Việt
     */
    public String getUserFriendlyMessage() {
        return ErrorHandler.getErrorMessage(statusCode);
    }

    /**
     * Override toString() để hiển thị thông tin đầy đủ
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpException{");
        sb.append("statusCode=").append(statusCode);
        sb.append(", errorMessage='").append(errorMessage).append('\'');

        if (responseBody != null && !responseBody.isEmpty()) {
            // Truncate response body nếu quá dài
            String truncated = responseBody.length() > 100
                    ? responseBody.substring(0, 100) + "..."
                    : responseBody;
            sb.append(", responseBody='").append(truncated).append('\'');
        }

        sb.append('}');
        return sb.toString();
    }

    /**
     * Hiển thị alert cho user với thông báo lỗi này
     */
    public void showAlert() {
        ErrorHandler.showUserFriendlyError(statusCode, errorMessage);
    }

    /**
     * Hiển thị alert cho user với context bổ sung
     * 
     * @param additionalContext Context bổ sung (VD: "Khi tải danh sách bệnh nhân")
     */
    public void showAlert(String additionalContext) {
        ErrorHandler.showUserFriendlyError(statusCode, additionalContext);
    }
}
