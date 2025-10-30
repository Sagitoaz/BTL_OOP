package org.example.oop.Control.Employee;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.miniboot.app.domain.models.Employee;

import java.io.InputStream;

public class EmployeeFormController extends BaseController {

    private final HttpEmployeeService employeeService = new HttpEmployeeService();

    // Các trường nhập liệu cho thông tin nhân viên
    @FXML
    private ImageView leftImageView; // image on the left side (loaded at runtime)

    @FXML
    private TextField usernameField; // Tên đăng nhập
    @FXML
    private PasswordField passwordField; // Mật khẩu
    @FXML
    private TextField firstnameField; // Họ và tên đệm
    @FXML
    private TextField lastnameField; // Tên
    @FXML
    private TextField emailField; // Email
    @FXML
    private TextField phoneField; // Số điện thoại
    @FXML
    private ComboBox<String> roleComboBox; // Vai trò
    @FXML
    private TextField licenseNoField; // Số giấy phép (nếu là bác sĩ)
    @FXML
    private TextField avatarField; // URL ảnh đại diện
    @FXML
    private CheckBox activeCheckBox; // Trạng thái hoạt động
    @FXML
    private Button saveButton; // Nút Save
    @FXML
    private Button cancelButton; // Nút Cancel

    // Edit mode
    private Employee editingEmployee = null;

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        roleComboBox.getItems().setAll("doctor", "nurse");
        roleComboBox.setOnAction(e -> handleRoleChange());
        addClearStyleOnEdit(usernameField);
        addClearStyleOnEdit(emailField);
        addClearStyleOnEdit(phoneField);
        addClearStyleOnEdit(licenseNoField);

        // Try to load left image from resources. If missing, hide the ImageView.
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
                if (leftImageView != null) leftImageView.setVisible(false);
            }
        } catch (Exception ex) {
            System.err.println("Error loading left image: " + ex.getMessage());
            if (leftImageView != null) leftImageView.setVisible(false);
        }
    }

    /**
     * Called by detail view when opening the form in edit mode.
     */
    public void setEmployeeForEdit(Employee employee) {
        if (employee == null) return;
        this.editingEmployee = employee;

        usernameField.setText(employee.getUsername());
        usernameField.setDisable(true); // don't allow changing username in edit mode

        passwordField.clear();
        passwordField.setPromptText("Bỏ trống để giữ mật khẩu hiện tại");

        firstnameField.setText(employee.getFirstname());
        lastnameField.setText(employee.getLastname());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        roleComboBox.setValue(employee.getRole());
        licenseNoField.setText(employee.getLicenseNo() == null ? "" : employee.getLicenseNo());
        avatarField.setText(employee.getAvatar());
        activeCheckBox.setSelected(employee.isActive());

        saveButton.setText("Cập nhật");
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
        // Lấy thông tin từ các trường nhập liệu
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

        // Validation common: firstname/lastname/role
        if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank() || role == null) {
            showError("Firstname, lastname và role là bắt buộc.");
            return;
        }

        // If editing -> update path (username/password not required)
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
                    throw new RuntimeException(e);
                }
            }, this::handleUpdateResponse, throwable -> {
                // throwable có thể là RuntimeException wrapping Exception từ service
                Throwable real = throwable.getCause() != null ? throwable.getCause() : throwable;
                String msg = real.getMessage();
                System.err.println("Update employee failed: " + msg);
                String display = msg == null || msg.isBlank() ? "Không thể cập nhật nhân viên." : msg;
                handleApiError(display);
            });

            return;
        }

        // Create path validations (username/password/email/phone)
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
        newEmployee.setRole(role); // Vai trò (doctor/nurse)
        newEmployee.setLicenseNo("doctor".equals(role) ? licenseNo : null); // Chỉ cần số giấy phép nếu là bác sĩ
        newEmployee.setAvatar(avatar);
        newEmployee.setActive(isActive);

        // Tương tự ở create: wrap exception để Task thất bại và dùng onError để hiển thị message cụ thể
        executeAsync(
                () -> {
                    try {
                        return employeeService.createEmployee(newEmployee); // Gọi phương thức tạo nhân viên
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo nhân viên: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                },
                this::handleSaveResponse,
                throwable -> {
                    Throwable real = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String msg = real.getMessage();
                    System.err.println("Create employee failed: " + msg);
                    // Nếu server trả message cụ thể (ví dụ: "Username hoặc email đã tồn tại") -> hiển thị trực tiếp
                    String display = msg == null || msg.isBlank() ? "Không thể thêm nhân viên." : msg;
                    handleApiError(display);
                });
    }

    private void handleSaveResponse(Employee savedEmployee) {
        System.out.println("handleSaveResponse invoked. editingEmployee=" + (editingEmployee == null ? "null" : editingEmployee.getId()));
        if (savedEmployee != null) {
            System.out.println("Nhân viên đã được thêm/cập nhật thành công. savedEmployee.id=" + savedEmployee.getId());
            showSuccess("Nhân viên đã được thêm thành công.");
            // Nếu đang ở chế độ edit (editingEmployee != null) -> đóng cửa sổ
            if (editingEmployee != null) {
                System.out.println("Detected edit mode -> closing window");
                closeWindow();
            } else {
                System.out.println("Detected create mode -> resetting form and keeping window open");
                // Ở chế độ create: reset form để có thể tiếp tục thêm, không đóng cửa sổ
                resetForm();
                // focus vào username để tiện tạo tiếp
                if (usernameField != null) usernameField.requestFocus();
            }
        } else {
            System.err.println("Lỗi khi lưu nhân viên.");
            showError("Không thể thêm nhân viên.");
        }
    }

    /**
     * Reset form fields to default state for creating a new employee.
     */
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
            System.out.println("Nhân viên đã được cập nhật.");
            showSuccess("Nhân viên đã được cập nhật.");
            closeWindow();
        } else {
            System.err.println("Lỗi khi cập nhật nhân viên.");
            showError("Không thể cập nhật nhân viên.");
        }
    }

    @FXML
    private void handleCancel() {
        // Cancel should always close the form regardless of create/edit mode
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        // Only close the window when we're in edit mode. In create mode we intentionally keep the form open
        // so user can continue adding multiple employees. If an external caller really needs to force-close
        // the window, call cancelButton.getScene().getWindow().hide() directly.
        System.out.println("closeWindow() called. editingEmployee=" + (editingEmployee == null ? "null" : editingEmployee.getId()));
        if (editingEmployee != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        } else {
            System.out.println("closeWindow() ignored because form is in create mode (editingEmployee == null)");
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

    /**
     * Show error and highlight the corresponding input field when possible.
     */
    private void handleApiError(String msg) {
        // Always show the error alert
        showError(msg);

        String lower = msg == null ? "" : msg.toLowerCase();
        // Heuristics to detect which field violated unique constraint
        if (lower.contains("email") || lower.contains("e-mail") || lower.contains("đã tồn tại") && lower.contains("email")) {
            if (emailField != null) {
                emailField.setStyle("-fx-border-color: #ff4d4f; -fx-border-width: 2px;");
                emailField.requestFocus();
            }
        } else if (lower.contains("username") || lower.contains("user") && lower.contains("tồn tại")) {
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

    private void addClearStyleOnEdit(TextInputControl field) {
        if (field == null) return;
        field.textProperty().addListener((obs, oldV, newV) -> field.setStyle(""));
        field.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) field.setStyle("");
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.length() == 10 && phone.matches("\\d+");
    }
}
