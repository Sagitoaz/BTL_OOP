package org.example.oop.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.oop.Model.MailService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho chức năng quên mật khẩu
 * Bước 1: Nhập email → Gửi mã token qua email
 * Bước 2: Nhập token + mật khẩu mới → Reset mật khẩu
 */
public class ForgotPasswordController {

    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordController.class.getName());

    @FXML
    private TextField emailField;

    @FXML
    private TextField tokenField;

    @FXML
    private TextField newPasswordField;

    @FXML
    private TextField confirmPasswordField;

    @FXML
    private Button sendTokenButton;

    @FXML
    private Button resetPasswordButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label messageLabel;

    private String currentToken;

    // Khởi tạo MailService để gửi email
    private final MailService mailService = new MailService();

    @FXML
    public void initialize() {
        // Bước đầu chỉ cho phép gửi token
        if (tokenField != null) tokenField.setDisable(true);
        if (newPasswordField != null) newPasswordField.setDisable(true);
        if (confirmPasswordField != null) confirmPasswordField.setDisable(true);
        if (resetPasswordButton != null) resetPasswordButton.setDisable(true);
    }

    private void setErrorMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.RED);
        }
    }

    private void setSuccessMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.GREEN);
        }
    }

    private void setInfoMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.BLUE);
        }
    }

    /**
     * Hiển thị Alert dialog thông báo thành công
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Bước 1: Gửi yêu cầu reset mật khẩu
     * GỬI MÃ TOKEN QUA EMAIL THAY VÌ HIỂN THỊ TRÊN MÀN HÌNH
     */
    @FXML
    void SendPassword(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            setErrorMessage("Vui lòng nhập địa chỉ email");
            return;
        }

        if (!isValidEmail(email)) {
            setErrorMessage("Email không hợp lệ");
            return;
        }

        // Hiển thị loading message
        setInfoMessage("Đang gửi mã xác nhận đến email của bạn...");
        sendTokenButton.setDisable(true);

        // Chạy trong thread riêng để không block UI
        new Thread(() -> {
            try {
                // Gọi AuthServiceWrapper để tạo token
                String token = AuthServiceWrapper.requestPasswordReset(email);

                javafx.application.Platform.runLater(() -> {
                    if (token != null) {
                        currentToken = token;

                        try {
                            // GỬI EMAIL QUA MAILSERVICE
                            mailService.SendText(email, token);

                            setSuccessMessage("✓ Mã xác nhận đã được gửi đến email: " + email +
                                            "\nVui lòng kiểm tra hộp thư (bao gồm cả spam/junk)");

                            // Mở khóa các trường để nhập mật khẩu mới
                            if (tokenField != null) tokenField.setDisable(false);
                            if (newPasswordField != null) newPasswordField.setDisable(false);
                            if (confirmPasswordField != null) confirmPasswordField.setDisable(false);
                            if (resetPasswordButton != null) resetPasswordButton.setDisable(false);

                            LOGGER.info("✓ Reset token sent to email: " + email);

                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to send email", e);
                            setErrorMessage("Lỗi khi gửi email. Vui lòng thử lại sau.\n" +
                                          "Mã của bạn là: " + token + " (dùng tạm nếu không nhận được email)");
                        }

                    } else {
                        setErrorMessage("Email không tồn tại trong hệ thống");
                    }

                    sendTokenButton.setDisable(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error in password reset", e);
                    setErrorMessage("Có lỗi xảy ra. Vui lòng thử lại.");
                    sendTokenButton.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Bước 2: Reset mật khẩu với token
     */
    @FXML
    void ResetPassword(ActionEvent event) {
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate
        if (token.isEmpty()) {
            setErrorMessage("Vui lòng nhập mã xác nhận");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            setErrorMessage("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (!AuthServiceWrapper.isPasswordStrong(newPassword)) {
            setErrorMessage("Mật khẩu phải có ít nhất 8 ký tự bao gồm: chữ hoa, chữ thường, số và ký tự đặc biệt");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            setErrorMessage("Mật khẩu xác nhận không khớp");
            return;
        }

        // Gọi AuthServiceWrapper để reset
        boolean success = AuthServiceWrapper.resetPassword(token, newPassword);

        if (success) {
            setSuccessMessage("✓ Đặt lại mật khẩu thành công!");

            // Hiển thị thông báo thành công bằng Alert dialog
            showSuccessAlert(
                "Đổi mật khẩu thành công",
                "Mật khẩu của bạn đã được thay đổi thành công!\n\n" +
                "Bạn có thể đăng nhập bằng mật khẩu mới ngay bây giờ."
            );

            // Chuyển về Login
            backToLogin(event);
        } else {
            setErrorMessage("Mã xác nhận không hợp lệ hoặc đã hết hạn");
        }
    }

    @FXML
    void BackToLogin(ActionEvent event) {
        backToLogin(event);
    }

    private void backToLogin(ActionEvent event) {
        SceneManager.switchScene(SceneConfig.LOGIN_FXML, SceneConfig.LOGIN_FXML);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

