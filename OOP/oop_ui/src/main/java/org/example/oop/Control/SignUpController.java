package org.example.oop.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(SignUpController.class.getName());

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private TextField visibleConfirmPasswordTextField;

    @FXML
    private Button toggleConfirmPasswordButton;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField addressTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField dobTextField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private Button signUpButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label errorMessage;

    // Trạng thái hiển thị mật khẩu
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.getItems().addAll("Nam", "Nữ", "Khác");

        // Đồng bộ nội dung giữa PasswordField và TextField cho Password
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isPasswordVisible) {
                visiblePasswordTextField.setText(newValue);
            }
        });

        visiblePasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isPasswordVisible) {
                passwordTextField.setText(newValue);
            }
        });

        // Đồng bộ nội dung giữa PasswordField và TextField cho Confirm Password
        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isConfirmPasswordVisible) {
                visibleConfirmPasswordTextField.setText(newValue);
            }
        });

        visibleConfirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isConfirmPasswordVisible) {
                confirmPasswordTextField.setText(newValue);
            }
        });
    }

    /**
     * Toggle hiển thị/ẩn mật khẩu chính
     */
    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Hiển thị mật khẩu
            visiblePasswordTextField.setText(passwordTextField.getText());
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("🙈"); // Icon mắt đóng
        } else {
            // Ẩn mật khẩu
            passwordTextField.setText(visiblePasswordTextField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(false);
            togglePasswordButton.setText("👁"); // Icon mắt mở
        }
    }

    /**
     * Toggle hiển thị/ẩn xác nhận mật khẩu
     */
    @FXML
    void toggleConfirmPasswordVisibility(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            // Hiển thị mật khẩu
            visibleConfirmPasswordTextField.setText(confirmPasswordTextField.getText());
            visibleConfirmPasswordTextField.setVisible(true);
            visibleConfirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("🙈"); // Icon mắt đóng
        } else {
            // Ẩn mật khẩu
            confirmPasswordTextField.setText(visibleConfirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            visibleConfirmPasswordTextField.setVisible(false);
            visibleConfirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("👁"); // Icon mắt mở
        }
    }

    private void setErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setTextFill(Color.RED);
    }

    private void setSuccessMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setTextFill(Color.GREEN);
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
     * Chuyển đổi giới tính từ tiếng Việt sang tiếng Anh IN HOA (enum database)
     */
    private String convertGenderToEnglish(String genderVi) {
        if (genderVi == null || genderVi.isEmpty()) {
            return null;
        }

        switch (genderVi.trim()) {
            case "Nam":
                return "MALE";
            case "Nữ":
                return "FEMALE";
            case "Khác":
                return "OTHER";
            default:
                return null;
        }
    }

    /**
     * Kiểm tra định dạng ngày sinh (dd/MM/yyyy)
     */
    private boolean isValidDate(String dob) {
        if (dob == null || dob.isEmpty()) return false;
        String dateRegex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
        return Pattern.matches(dateRegex, dob);
    }

    // Kiểm tra hợp lệ - TẤT CẢ CÁC TRƯỜNG ĐỀU BẮT BUỘC
    private String validateSignUpInput(String username, String password, String confirmPassword,
                                       String fullName, String address, String email,
                                       String phone, String dob, String gender) {
        // Kiểm tra tất cả các trường bắt buộc
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || address.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || dob.isEmpty() || gender == null || gender.isEmpty()) {
            return "Vui lòng điền đầy đủ TẤT CẢ các trường thông tin";
        }

        if (username.length() < 3) {
            return "Tên đăng nhập phải có ít nhất 3 ký tự";
        }

        // Kiểm tra họ tên phải có ít nhất 2 từ (Họ + Tên)
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length < 2) {
            return "Họ và tên phải có ít nhất 2 từ (Ví dụ: Nguyễn Anh, Trần Văn A)";
        }

        if (!AuthServiceWrapper.isPasswordStrong(password)) {
            return "Mật khẩu phải có ít nhất 8 ký tự bao gồm: chữ hoa, chữ thường, số và ký tự đặc biệt";
        }

        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp";
        }

        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }

        if (!isValidPhoneNumber(phone)) {
            return "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0";
        }

        if (!isValidDate(dob)) {
            return "Ngày sinh không hợp lệ (định dạng: dd/MM/yyyy, ví dụ: 15/05/2000)";
        }

        return null;
    }

    // Chuẩn format của email và số điện thoại Việt Nam
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.length() == 10 && phone.startsWith("0") && phone.matches("\\d{10}");
    }

    @FXML
    void signUpButtonOnClick(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        // Lấy password từ field đang hiển thị
        String password = isPasswordVisible ?
                         visiblePasswordTextField.getText().trim() :
                         passwordTextField.getText().trim();
        String confirmPassword = isConfirmPasswordVisible ?
                                visibleConfirmPasswordTextField.getText().trim() :
                                confirmPasswordTextField.getText().trim();
        String fullName = fullNameTextField.getText().trim();
        String address = addressTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String phone = phoneTextField.getText().trim();
        String dob = dobTextField.getText().trim();
        String genderVi = genderComboBox.getValue(); // Giới tính tiếng Việt từ ComboBox

        // Validate input - TẤT CẢ CÁC TRƯỜNG ĐỀU BẮT BUỘC
        String validationError = validateSignUpInput(username, password, confirmPassword,
                                                     fullName, address, email, phone, dob, genderVi);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        // Chuyển đổi giới tính từ tiếng Việt sang tiếng Anh IN HOA (MALE, FEMALE, OTHER)
        String genderEn = convertGenderToEnglish(genderVi);

        if (genderEn == null) {
            setErrorMessage("Giới tính không hợp lệ");
            return;
        }

        // Gọi AuthServiceWrapper.register - truyền giới tính tiếng Anh IN HOA
        boolean success = AuthServiceWrapper.register(username, email, password, fullName,
                                                      phone, address, dob, genderEn);

        if (success) {
            setSuccessMessage("✓ Đăng ký thành công! Đang chuyển đến trang đăng nhập...");

            // Hiển thị thông báo thành công bằng Alert dialog
            showSuccessAlert(
                "Đăng ký thành công",
                "Chúc mừng! Tài khoản của bạn đã được tạo thành công.\n\n" +
                "Tên đăng nhập: " + username + "\n" +
                "Email: " + email + "\n" +
                "Họ tên: " + fullName + "\n" +
                "Số điện thoại: " + phone + "\n" +
                "Ngày sinh: " + dob + "\n" +
                "Giới tính: " + genderVi + "\n\n" +
                "Bạn có thể đăng nhập ngay bây giờ."
            );

            // Chuyển về Login
            backToLogin(event);
        } else {
            setErrorMessage("Tên đăng nhập hoặc email đã tồn tại. Vui lòng thử lại.");
        }
    }

    @FXML
    void backToLoginButtonOnClick(ActionEvent event) {
        backToLogin(event);
    }

    private void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login screen", e);
            setErrorMessage("Lỗi khi quay về trang đăng nhập");
        }
    }
}
