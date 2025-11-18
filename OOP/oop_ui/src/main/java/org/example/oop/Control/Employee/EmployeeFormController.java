package org.example.oop.Control.Employee;

import java.io.InputStream;

import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.miniboot.app.domain.models.Employee;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class EmployeeFormController extends BaseController {

    private HttpEmployeeService employeeService;

    @FXML
    private ImageView leftImageView;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private TextField licenseNoField;
    @FXML
    private TextField avatarField;
    @FXML
    private CheckBox activeCheckBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Employee editingEmployee = null;

    @FXML
    public void initialize() {
        // Initialize service with token
        String token = org.example.oop.Utils.SceneManager.getSceneData("authToken");
        employeeService = new HttpEmployeeService(org.example.oop.Utils.ApiConfig.getBaseUrl(), token);

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        roleComboBox.getItems().setAll("doctor", "nurse");
        roleComboBox.setOnAction(e -> handleRoleChange());
        loadLeftImage();
    }

    private void loadLeftImage() {
        try (InputStream is = getClass().getResourceAsStream("/Image/bac_si_toan_beo.jpg")) {
            if (is != null) {
                Image img = new Image(is);
                leftImageView.setImage(img);
                leftImageView.setVisible(true);
            } else {
                System.err.println("Resource image not found: /Image/bac_si_toan_beo.jpg");
                if (leftImageView != null)
                    leftImageView.setVisible(false);
            }
        } catch (Exception ex) {
            System.err.println("Error loading left image: " + ex.getMessage());
            if (leftImageView != null)
                leftImageView.setVisible(false);
        }
    }

    @FXML
    private void handleRoleChange() {
        if ("doctor".equals(roleComboBox.getValue())) {
            licenseNoField.setDisable(false);
        } else {
            licenseNoField.setDisable(true);
            licenseNoField.clear();
        }
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String role = roleComboBox.getValue();
        String licenseNo = licenseNoField.getText();
        String avatar = avatarField.getText();
        boolean isActive = activeCheckBox.isSelected();

        if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank() || role == null) {
            showError("Firstname, lastname và role là bắt buộc.");
            return;
        }

        if (editingEmployee != null) {
            if ("doctor".equals(role) && (licenseNo == null || licenseNo.isBlank())) {
                showError("Vui lòng nhập số giấy phép hành nghề cho bác sĩ.");
                return;
            }

            Employee updated = new Employee();
            updated.setId(editingEmployee.getId());
            updated.setFirstname(firstname);
            updated.setLastname(lastname);
            updated.setEmail(email);
            updated.setPhone(phone);
            updated.setRole(role);
            updated.setLicenseNo("doctor".equals(role) ? licenseNo : null);
            updated.setAvatar(avatar);
            updated.setActive(isActive);

            executeAsync(() -> {
                try {
                    return employeeService.updateEmployee(updated);
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật nhân viên: " + e.getMessage());
                    return null; // ✅ Return null instead of throwing RuntimeException
                }
            }, this::handleUpdateResponse, throwable -> {
                Throwable real = throwable.getCause() != null ? throwable.getCause() : throwable;
                String msg = real.getMessage();
                System.err.println("Update employee failed: " + msg);
                String display = msg == null || msg.isBlank() ? "Không thể cập nhật nhân viên." : msg;
                handleApiError(display);
            });

            return;
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Username và password là bắt buộc khi tạo mới.");
            return;
        }
        if (email == null || email.isBlank() || !isValidEmail(email)) {
            showError("Email không hợp lệ.");
            return;
        }
        if (phone == null || phone.isBlank() || !isValidPhone(phone)) {
            showError("Số điện thoại không hợp lệ (10 chữ số). ");
            return;
        }

        if ("doctor".equals(role) && (licenseNo == null || licenseNo.isEmpty())) {
            showError("Vui lòng nhập số giấy phép hành nghề cho bác sĩ.");
            return;
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        Employee newEmployee = new Employee();
        newEmployee.setUsername(username);
        newEmployee.setPassword(hashedPassword);
        newEmployee.setFirstname(firstname);
        newEmployee.setLastname(lastname);
        newEmployee.setEmail(email);
        newEmployee.setPhone(phone);
        newEmployee.setRole(role);
        newEmployee.setLicenseNo("doctor".equals(role) ? licenseNo : null);
        newEmployee.setAvatar(avatar);
        newEmployee.setActive(isActive);

        executeAsync(
                () -> {
                    try {
                        return employeeService.createEmployee(newEmployee);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo nhân viên: " + e.getMessage());
                        return null;
                    }
                },
                this::handleSaveResponse,
                throwable -> {
                    Throwable real = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String msg = real.getMessage();
                    System.err.println("Create employee failed: " + msg);
                    String display = msg == null || msg.isBlank() ? "Không thể thêm nhân viên." : msg;
                    handleApiError(display);
                });
    }

    private void handleSaveResponse(Employee savedEmployee) {
        if (savedEmployee != null) {
            showSuccess("Nhân viên đã được thêm thành công.");
            if (editingEmployee != null) {
                closeWindow();
            } else {
                resetForm();
                if (usernameField != null)
                    usernameField.requestFocus();
            }
        } else {
            showError("Không thể thêm nhân viên.");
        }
    }

    private void resetForm() {
        editingEmployee = null;
        usernameField.setDisable(false);
        usernameField.clear();
        passwordField.clear();
        passwordField.setPromptText("");
        firstnameField.clear();
        lastnameField.clear();
        emailField.clear();
        phoneField.clear();
        roleComboBox.setValue(null);
        licenseNoField.clear();
        licenseNoField.setDisable(true);
        avatarField.clear();
        activeCheckBox.setSelected(true);
        saveButton.setText("Thêm");
    }

    private void handleUpdateResponse(Employee updatedEmployee) {
        if (updatedEmployee != null) {
            showSuccess("Nhân viên đã được cập nhật.");
            closeWindow();
        } else {
            showError("Không thể cập nhật nhân viên.");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        if (editingEmployee != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void onButtonHover() {
        cancelButton.setStyle("-fx-background-color: #FFDD57;");
    }

    @FXML
    private void onButtonExit() {
        cancelButton.setStyle("-fx-background-color: #FFFFFF;");
    }

    private void handleApiError(String msg) {
        showError(msg);
        String lower = msg == null ? "" : msg.toLowerCase();
        if (lower.contains("email") || lower.contains("e-mail")
                || (lower.contains("đã tồn tại") && lower.contains("email"))) {
            if (emailField != null) {
                emailField.setStyle("-fx-border-color: #ff4d4f; -fx-border-width: 2px;");
                emailField.requestFocus();
            }
        } else if (lower.contains("username") || (lower.contains("user") && lower.contains("tồn tại"))) {
            if (usernameField != null) {
                usernameField.setStyle("-fx-border-color: #ff4d4f; -fx-border-width: 2px;");
                usernameField.requestFocus();
            }
        } else if (lower.contains("license") || lower.contains("giấy phép")) {
            if (licenseNoField != null) {
                licenseNoField.setStyle("-fx-border-color: #ff4d4f; -fx-border-width: 2px;");
                licenseNoField.requestFocus();
            }
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.length() == 10 && phone.matches("\\d+");
    }
}
