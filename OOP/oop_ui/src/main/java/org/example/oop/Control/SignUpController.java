package org.example.oop.Control;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import java.util.regex.Pattern;

public class SignUpController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField dobTextField;

    @FXML
    private ComboBox<Customer.Gender> genderComboBox;

    @FXML
    private TextField addressTextField;

    @FXML
    private Label errorMessage;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Button toggleConfirmPasswordButton;

    @FXML
    private TextField visibleConfirmPasswordTextField;

    // Trạng thái hiển thị mật khẩu
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Timer để tự động ẩn thông báo lỗi sau 5 giây
    private PauseTransition errorMessageTimer;

    @FXML
    public void initialize() {
        // Khởi tạo timer cho thông báo lỗi (5 giây)
        errorMessageTimer = new PauseTransition(Duration.seconds(5));
        errorMessageTimer.setOnFinished(event -> hideErrorMessage());

        // Đồng bộ nội dung giữa PasswordField và TextField cho password
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

        // Đồng bộ cho confirm password
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

        // Initialize gender combo box
        genderComboBox.getItems().addAll(Customer.Gender.values());

        // Thêm listener để ẩn thông báo lỗi khi người dùng click vào bất kỳ trường nào
        setupFieldListeners();
    }

    /**
     * Thiết lập listeners cho tất cả các trường để ẩn thông báo lỗi khi người dùng click vào
     */
    private void setupFieldListeners() {
        // Ẩn thông báo lỗi khi click vào bất kỳ trường nhập liệu nào
        usernameTextField.setOnMouseClicked(event -> hideErrorMessage());
        passwordTextField.setOnMouseClicked(event -> hideErrorMessage());
        visiblePasswordTextField.setOnMouseClicked(event -> hideErrorMessage());
        confirmPasswordTextField.setOnMouseClicked(event -> hideErrorMessage());
        visibleConfirmPasswordTextField.setOnMouseClicked(event -> hideErrorMessage());
        fullNameTextField.setOnMouseClicked(event -> hideErrorMessage());
        emailTextField.setOnMouseClicked(event -> hideErrorMessage());
        phoneTextField.setOnMouseClicked(event -> hideErrorMessage());
        dobTextField.setOnMouseClicked(event -> hideErrorMessage());
        addressTextField.setOnMouseClicked(event -> hideErrorMessage());
        genderComboBox.setOnMouseClicked(event -> hideErrorMessage());

        // Cũng ẩn khi người dùng bắt đầu gõ
        usernameTextField.setOnKeyPressed(event -> hideErrorMessage());
        passwordTextField.setOnKeyPressed(event -> hideErrorMessage());
        visiblePasswordTextField.setOnKeyPressed(event -> hideErrorMessage());
        confirmPasswordTextField.setOnKeyPressed(event -> hideErrorMessage());
        visibleConfirmPasswordTextField.setOnKeyPressed(event -> hideErrorMessage());
        fullNameTextField.setOnKeyPressed(event -> hideErrorMessage());
        emailTextField.setOnKeyPressed(event -> hideErrorMessage());
        phoneTextField.setOnKeyPressed(event -> hideErrorMessage());
        dobTextField.setOnKeyPressed(event -> hideErrorMessage());
        addressTextField.setOnKeyPressed(event -> hideErrorMessage());
    }

    /**
     * Ẩn thông báo lỗi và dừng timer
     */
    private void hideErrorMessage() {
        errorMessage.setVisible(false);
        if (errorMessageTimer != null) {
            errorMessageTimer.stop();
        }
    }

    @FXML
    void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            visiblePasswordTextField.setText(passwordTextField.getText());
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("🙈");
        } else {
            passwordTextField.setText(visiblePasswordTextField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
    }

    @FXML
    void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            visibleConfirmPasswordTextField.setText(confirmPasswordTextField.getText());
            visibleConfirmPasswordTextField.setVisible(true);
            visibleConfirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("🙈");
        } else {
            confirmPasswordTextField.setText(visibleConfirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            visibleConfirmPasswordTextField.setVisible(false);
            visibleConfirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("👁");
        }
    }

    private void setErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setTextFill(Color.RED);
        errorMessage.setVisible(true);

        // Khởi động lại timer 5 giây mỗi khi có thông báo lỗi mới
        if (errorMessageTimer != null) {
            errorMessageTimer.stop();
            errorMessageTimer.playFromStart();
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
     * Chuyển đổi giới tính từ enum sang tiếng Anh IN HOA (enum database)
     */
    private String convertGenderToEnglish(Customer.Gender gender) {
        if (gender == null) {
            return null;
        }

        return switch (gender) {
            case MALE -> "MALE";
            case FEMALE -> "FEMALE";
            case OTHER -> "OTHER";
        };
    }

    /**
     * Kiểm tra định dạng ngày sinh (dd/MM/yyyy)
     */
    private boolean isValidDate(String dob) {
        if (dob == null || dob.isEmpty()) return false;
        String dateRegex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
        return Pattern.matches(dateRegex, dob);
    }

    /**
     * Kiểm tra hợp lệ - TẤT CẢ CÁC TRƯỜNG ĐỀU BẮT BUỘC
     */
    private String validateSignUpInput(String username, String password, String confirmPassword,
                                       String fullName, String address, String email,
                                       String phone, String dob, Customer.Gender gender) {
        // Kiểm tra tất cả các trường bắt buộc
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || address.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || dob.isEmpty() || gender == null) {
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
            return "Ngày sinh không hợp lệ (định dạng: dd/MM/yyyy)";
        }

        return null;
    }

    /**
     * Chuẩn format của email và số điện thoại Việt Nam
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.length() == 10 && phone.startsWith("0") && phone.matches("\\d{10}");
    }

    @FXML
    void signUpButtonOnClick(ActionEvent event) {
        // Reset error message trước
        hideErrorMessage();

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
        Customer.Gender gender = genderComboBox.getValue(); // Giới tính từ ComboBox

        // Validate input - TẤT CẢ CÁC TRƯỜNG ĐỀU BẮT BUỘC
        String validationError = validateSignUpInput(username, password, confirmPassword,
                                                     fullName, address, email, phone, dob, gender);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        // Chuyển đổi giới tính từ enum sang tiếng Anh IN HOA (MALE, FEMALE, OTHER)
        String genderEn = convertGenderToEnglish(gender);

        if (genderEn == null) {
            setErrorMessage("Giới tính không hợp lệ");
            return;
        }

        // Gọi AuthServiceWrapper.register - truyền giới tính tiếng Anh IN HOA
        boolean success = AuthServiceWrapper.register(username, email, password, fullName,
                                                      phone, address, dob, genderEn);

        if (success) {
            // Hiển thị thông báo thành công bằng Alert dialog
            showSuccessAlert(
                "Đăng ký thành công",
                "Chúc mừng! Tài khoản của bạn đã được tạo thành công.\n\n" +
                "Tên đăng nhập: " + username + "\n" +
                "Email: " + email + "\n" +
                "Họ tên: " + fullName + "\n" +
                "Số điện thoại: " + phone + "\n" +
                "Ngày sinh: " + dob + "\n" +
                "Giới tính: " + gender + "\n\n" +
                "Bạn có thể đăng nhập ngay bây giờ."
            );

            // Clear tất cả các field sau khi đăng ký thành công
            clearAllFields();

            // Chuyển về Login
            backToLoginButtonOnClick(event);
        } else {
            // Hiển thị lỗi trên giao diện thay vì chỉ log
            setErrorMessage("Tên đăng nhập hoặc email đã tồn tại. Vui lòng thử tên khác!");
        }
    }

    /**
     * Clear tất cả các field trong form đăng ký
     */
    private void clearAllFields() {
        usernameTextField.clear();
        passwordTextField.clear();
        visiblePasswordTextField.clear();
        confirmPasswordTextField.clear();
        visibleConfirmPasswordTextField.clear();
        fullNameTextField.clear();
        emailTextField.clear();
        phoneTextField.clear();
        dobTextField.clear();
        addressTextField.clear();
        genderComboBox.setValue(null);
        errorMessage.setVisible(false);

        // Reset về trạng thái ẩn password
        if (isPasswordVisible) {
            togglePasswordVisibility();
        }
        if (isConfirmPasswordVisible) {
            toggleConfirmPasswordVisibility();
        }
    }

    @FXML
    void backToLoginButtonOnClick(ActionEvent event) {
        SceneManager.switchScene(SceneConfig.LOGIN_FXML, SceneConfig.LOGIN_FXML);
    }
}
