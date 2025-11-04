package org.example.oop.Utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ErrorHandler - Xử lý lỗi HTTP một cách tập trung và user-friendly
 * 
 * Chức năng:
 * - Mapping HTTP status codes sang thông báo tiếng Việt
 * - Hiển thị alert cho user
 * - Quyết định có nên retry request hay không
 * - Log errors cho debugging
 * 
 * Usage:
 * 
 * <pre>
 * if (response.statusCode() != 200) {
 *     String errorMsg = ErrorHandler.getErrorMessage(response.statusCode());
 *     ErrorHandler.showUserFriendlyError(response.statusCode(), "Không thể tải dữ liệu");
 * 
 *     if (ErrorHandler.shouldRetry(response.statusCode())) {
 *         // Retry logic here
 *     }
 *     throw new HttpException(response.statusCode(), errorMsg);
 * }
 * </pre>
 * 
 * @author Person 4 - Error Handling & Service Layer Developer
 * @since 2025-11-02
 */
public class ErrorHandler {

    // Error messages mapping (HTTP Status Code -> Vietnamese Message)
    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<>();

    // Error titles mapping (HTTP Status Code -> Alert Title)
    private static final Map<Integer, String> ERROR_TITLES = new HashMap<>();

    static {
        // 4xx Client Errors
        ERROR_MESSAGES.put(400, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin nhập vào.");
        ERROR_TITLES.put(400, "Dữ liệu không hợp lệ");

        ERROR_MESSAGES.put(401, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        ERROR_TITLES.put(401, "Phiên đăng nhập hết hạn");

        ERROR_MESSAGES.put(403, "Bạn không có quyền thực hiện thao tác này.");
        ERROR_TITLES.put(403, "Không có quyền truy cập");

        ERROR_MESSAGES.put(404, "Không tìm thấy dữ liệu yêu cầu.");
        ERROR_TITLES.put(404, "Không tìm thấy");

        ERROR_MESSAGES.put(409, "Dữ liệu đã tồn tại hoặc có xung đột. Vui lòng kiểm tra lại.");
        ERROR_TITLES.put(409, "Dữ liệu bị xung đột");

        ERROR_MESSAGES.put(422, "Dữ liệu không đáp ứng quy tắc nghiệp vụ. Vui lòng kiểm tra lại.");
        ERROR_TITLES.put(422, "Không đáp ứng quy tắc");

        ERROR_MESSAGES.put(429, "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút.");
        ERROR_TITLES.put(429, "Quá nhiều yêu cầu");

        // 5xx Server Errors
        ERROR_MESSAGES.put(500, "Lỗi máy chủ nội bộ. Vui lòng liên hệ quản trị viên hoặc thử lại sau.");
        ERROR_TITLES.put(500, "Lỗi máy chủ");

        ERROR_MESSAGES.put(503, "Máy chủ đang bảo trì hoặc quá tải. Vui lòng thử lại sau.");
        ERROR_TITLES.put(503, "Máy chủ đang bảo trì");

        ERROR_MESSAGES.put(504, "Kết nối hết thời gian chờ. Vui lòng kiểm tra kết nối mạng và thử lại.");
        ERROR_TITLES.put(504, "Hết thời gian chờ");

        // Generic/Unknown Error
        ERROR_MESSAGES.put(0, "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.");
        ERROR_TITLES.put(0, "Lỗi kết nối");
    }

    /**
     * Lấy thông báo lỗi tiếng Việt từ HTTP status code
     * 
     * @param statusCode HTTP status code (400, 401, 403, 404, 409, 422, 429, 500,
     *                   503, 504)
     * @return Thông báo lỗi tiếng Việt, hoặc thông báo mặc định nếu không tìm thấy
     */
    public static String getErrorMessage(int statusCode) {
        return ERROR_MESSAGES.getOrDefault(statusCode,
                "Đã xảy ra lỗi không xác định (Mã lỗi: " + statusCode + "). Vui lòng thử lại sau.");
    }

    /**
     * Lấy tiêu đề alert từ HTTP status code
     * 
     * @param statusCode HTTP status code
     * @return Tiêu đề alert tiếng Việt
     */
    public static String getErrorTitle(int statusCode) {
        return ERROR_TITLES.getOrDefault(statusCode, "Lỗi");
    }

    /**
     * Xử lý lỗi HTTP và log ra console cho debugging
     * 
     * @param statusCode HTTP status code
     * @param context    Ngữ cảnh của lỗi (VD: "Khi tải danh sách lịch hẹn")
     */
    public static void handleHttpError(int statusCode, String context) {
        String errorMsg = getErrorMessage(statusCode);

        // Log lỗi ra console cho debugging
        System.err.println("═══════════════════════════════════════════");
        System.err.println("❌ HTTP ERROR [" + statusCode + "]");
        System.err.println("📍 Context: " + context);
        System.err.println("💬 Message: " + errorMsg);
        System.err.println("⏰ Time: " + java.time.LocalDateTime.now());
        System.err.println("═══════════════════════════════════════════");
    }

    /**
     * Hiển thị thông báo lỗi user-friendly dưới dạng Alert
     * (An toàn cho JavaFX threading)
     * 
     * @param statusCode        HTTP status code
     * @param additionalContext Thông tin bổ sung (VD: "Không thể tải danh sách bệnh
     *                          nhân")
     */
    public static void showUserFriendlyError(int statusCode, String additionalContext) {
        String title = getErrorTitle(statusCode);
        String message = getErrorMessage(statusCode);

        // Thêm context nếu có
        if (additionalContext != null && !additionalContext.trim().isEmpty()) {
            message = additionalContext + "\n\n" + message;
        }

        // Hiển thị alert (an toàn cho UI thread)
        showErrorAlert(title, message);

        // Log vào console
        handleHttpError(statusCode, additionalContext != null ? additionalContext : "Unknown context");
    }

    /**
     * Hiển thị thông báo lỗi với message tùy chỉnh
     * 
     * @param statusCode    HTTP status code
     * @param customMessage Thông báo tùy chỉnh hoàn toàn
     */
    public static void showCustomError(int statusCode, String customMessage) {
        String title = getErrorTitle(statusCode);
        showErrorAlert(title, customMessage);

        // Log
        System.err.println("❌ Custom Error [" + statusCode + "]: " + customMessage);
    }

    /**
     * Kiểm tra xem có nên retry request hay không
     * Chỉ retry cho lỗi tạm thời (5xx errors)
     * 
     * @param statusCode HTTP status code
     * @return true nếu nên retry (503, 504), false nếu không nên
     */
    public static boolean shouldRetry(int statusCode) {
        // Retry cho server errors tạm thời
        return statusCode == 503 || statusCode == 504 || statusCode == 500;
    }

    /**
     * Hiển thị Alert với xử lý threading an toàn
     * 
     * @param title   Tiêu đề alert
     * @param message Nội dung thông báo
     */
    private static void showErrorAlert(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            // Đã ở UI thread -> hiển thị trực tiếp
            displayAlert(title, message);
        } else {
            // Đang ở background thread -> chuyển sang UI thread
            Platform.runLater(() -> displayAlert(title, message));
        }
    }

    /**
     * Hiển thị Alert thực sự (phải gọi trên UI thread)
     * 
     * @param title   Tiêu đề alert
     * @param message Nội dung thông báo
     */
    private static void displayAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling
        alert.getDialogPane().setMinWidth(400);

        alert.showAndWait();
    }

    /**
     * Hiển thị confirmation dialog cho retry
     * 
     * @param statusCode HTTP status code
     * @param context    Ngữ cảnh
     * @return true nếu user chọn retry, false nếu cancel
     */
    public static boolean showRetryDialog(int statusCode, String context) {
        String message = getErrorMessage(statusCode) + "\n\n" +
                "Ngữ cảnh: " + context + "\n\n" +
                "Bạn có muốn thử lại không?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lỗi kết nối");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType retryButton = new ButtonType("Thử lại");
        ButtonType cancelButton = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(retryButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == retryButton;
    }

    /**
     * Xử lý lỗi kết nối (IOException, InterruptedException)
     * 
     * @param exception Exception xảy ra
     * @param context   Ngữ cảnh
     */
    public static void handleConnectionError(Exception exception, String context) {
        String message = "Không thể kết nối đến máy chủ.\n\n" +
                "Chi tiết lỗi: " + exception.getMessage() + "\n\n" +
                "Vui lòng kiểm tra:\n" +
                "- Kết nối mạng\n" +
                "- Máy chủ đang chạy\n" +
                "- Firewall không chặn kết nối";

        showErrorAlert("Lỗi kết nối", message);

        // Log đầy đủ stack trace
        System.err.println("═══════════════════════════════════════════");
        System.err.println("❌ CONNECTION ERROR");
        System.err.println("📍 Context: " + context);
        System.err.println("💬 Exception: " + exception.getClass().getSimpleName());
        System.err.println("📄 Message: " + exception.getMessage());
        System.err.println("⏰ Time: " + java.time.LocalDateTime.now());
        System.err.println("═══════════════════════════════════════════");
        exception.printStackTrace();
    }

    /**
     * Validate response trước khi parse JSON
     * 
     * @param responseBody Response body string
     * @param context      Ngữ cảnh
     * @return true nếu valid, false nếu null/empty
     */
    public static boolean validateResponse(String responseBody, String context) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            String message = "Máy chủ trả về dữ liệu rỗng.\n\n" +
                    "Ngữ cảnh: " + context;
            showErrorAlert("Lỗi dữ liệu", message);

            System.err.println("❌ EMPTY RESPONSE: " + context);
            return false;
        }
        return true;
    }

    /**
     * Xử lý JSON parse error
     * 
     * @param exception JSON parse exception
     * @param context   Ngữ cảnh
     */
    public static void handleJsonParseError(Exception exception, String context) {
        String message = "Dữ liệu trả về từ máy chủ không đúng định dạng.\n\n" +
                "Vui lòng thử lại hoặc liên hệ quản trị viên nếu lỗi tiếp diễn.";

        showErrorAlert("Lỗi dữ liệu", message);

        System.err.println("═══════════════════════════════════════════");
        System.err.println("❌ JSON PARSE ERROR");
        System.err.println("📍 Context: " + context);
        System.err.println("💬 Exception: " + exception.getClass().getSimpleName());
        System.err.println("📄 Message: " + exception.getMessage());
        System.err.println("⏰ Time: " + java.time.LocalDateTime.now());
        System.err.println("═══════════════════════════════════════════");
        exception.printStackTrace();
    }
}
