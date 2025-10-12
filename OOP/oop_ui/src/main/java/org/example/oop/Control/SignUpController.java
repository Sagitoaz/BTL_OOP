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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.oop.Data.models.Patient;
import org.example.oop.Data.repositories.UserRepository;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class SignUpController implements Initializable {

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField confirmPasswordTextField;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hiển thị tiếng Anh trong ComboBox
        genderComboBox.getItems().addAll("Male", "Female");
    }

    private void setErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setTextFill(Color.RED);
    }

    private void setSuccessMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setTextFill(Color.GREEN);
    }

    // Chuyển đổi từ tiếng Anh sang tiếng Việt để lưu database
    private String convertGenderToVietnamese(String englishGender) {
        if (englishGender == null) return null;
        switch (englishGender) {
            case "Male":
                return "nam";
            case "Female":
                return "nữ";
            default:
                return null;
        }
    }

    // Kiểm tra hợp lệ - chỉ yêu cầu username, password, full name, address, email
    // Không bắt buộc: phone, date of birth, gender
    private String validateSignUpInput(String username, String password, String confirmPassword,
                                       String fullName, String address, String email, String phone) {
        // Kiểm tra các trường bắt buộc
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || address.isEmpty() || email.isEmpty()) {
            return "Username, password, full name, address and email are required";
        }

        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }

        if (!AuthServiceWrapper.isPasswordStrong(password)) {
            return "Password must be at least 8 characters with uppercase, lowercase, number, and special character";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        if (username.contains("|") || password.contains("|") || fullName.contains("|") ||
                address.contains("|") || email.contains("|")) {
            return "Fields cannot contain '|' character";
        }

        if (!isValidEmail(email)) {
            return "Invalid email format";
        }

        // Chỉ kiểm tra phone nếu người dùng có nhập
        if (!phone.isEmpty() && !isValidPhoneNumber(phone)) {
            return "Phone number must be 10 digits and start with 0";
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

    // Kiểm tra username đã tồn tại hay chưa
    private boolean isUsernameExists(String username) {
        UserRepository repo = new UserRepository();
        return repo.findByUsername(username).isPresent();
    }

    // Kiểm tra email đã tồn tại hay chưa
    private boolean isEmailExists(String email) {
        UserRepository repo = new UserRepository();
        return repo.findAll().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    // Gán ID tiếp theo cho user mới
    // ID mới = ID lớn nhất hiện có + 1
    private int getNextUserId() {
        UserRepository repo = new UserRepository();
        long count = repo.count();
        return (int) (count + 1);
    }

    // Lưu thông tin user mới
    private boolean saveNewUser(String username, String password, String fullName, String email, String phone) {
        try {
            int newId = getNextUserId();
            String hashedPassword = AuthServiceWrapper.hashPasswordWithSalt(password);
            Patient patient = new Patient(newId, username, hashedPassword, email, fullName, phone);
            UserRepository repo = new UserRepository();
            repo.save(patient);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving new user: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void signUpButtonOnClick(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();
        String confirmPassword = confirmPasswordTextField.getText().trim();
        String fullName = fullNameTextField.getText().trim();
        String address = addressTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String phone = phoneTextField.getText().trim();
        String dob = dobTextField.getText().trim();
        String selectedGender = genderComboBox.getValue();

        // Nếu có lỗi sẽ hiện lỗi và dừng
        String validationError = validateSignUpInput(username, password, confirmPassword, fullName, address, email, phone);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        // Kiểm tra username có tồn tại
        if (isUsernameExists(username)) {
            setErrorMessage("Username already exists");
            return;
        }

        // Kiểm tra email có tồn tại
        if (isEmailExists(email)) {
            setErrorMessage("Email already exists");
            return;
        }

        if (saveNewUser(username, password, fullName, email, phone)) {
            setSuccessMessage("✓ Account created successfully! Redirecting to login...");

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> backToLogin(event));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrupted during login redirect: " + e.getMessage());
                }
            }).start();
        } else {
            setErrorMessage("Error creating account. Please try again.");
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
            System.err.println("Error loading login screen: " + e.getMessage());
            setErrorMessage("Error returning to login screen");
        }
    }
}
