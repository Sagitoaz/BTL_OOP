package org.example.oop.Control.Employee;

import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmployeeEditFormController extends BaseController {

    private HttpEmployeeService service;
    private Employee employeeToEdit;

    @FXML
    private ImageView leftImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
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
    private CheckBox activeCheckBox;
    @FXML
    private VBox licenseBox;
    @FXML
    private TextField licenseNoField;
    @FXML
    private TextField avatarField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        // Initialize service with token
        String token = org.example.oop.Utils.SceneManager.getSceneData("authToken");
        service = new HttpEmployeeService(org.example.oop.Utils.ApiConfig.getBaseUrl(), token);

        if (SceneManager.getSceneData("employeeEdit") == null) {
            System.err.println("Không tìm thấy dữ liệu nhân viên để chỉnh sửa");
            return;
        } else {
            Employee emp = (Employee) SceneManager.getSceneData("employeeEdit");
            setEmployeeForEdit(emp);
            SceneManager.removeSceneData("employeeEdit");
        }
        var imageUrl = getClass().getResource("/Image/bac_si_toan_beo.jpg");
        if (imageUrl != null) {
            leftImageView.setImage(new Image(imageUrl.toExternalForm()));
        }
        if (roleComboBox != null) {
            roleComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> updateLicenseBoxVisibility(newVal));
        }
        if (licenseBox != null) {
            licenseBox.setVisible(false);
            licenseBox.setManaged(false);
        }
    }

    public void setEmployeeForEdit(Employee employee) {
        this.employeeToEdit = employee;
        populateFields();
    }

    private void populateFields() {
        if (employeeToEdit == null)
            return;

        firstnameField.setText(safe(employeeToEdit.getFirstname()));
        lastnameField.setText(safe(employeeToEdit.getLastname()));
        emailField.setText(safe(employeeToEdit.getEmail()));
        phoneField.setText(safe(employeeToEdit.getPhone()));
        avatarField.setText(safe(employeeToEdit.getAvatar()));
        String role = employeeToEdit.getRole();
        if (role != null) {
            roleComboBox.setValue(role.toLowerCase());
            updateLicenseBoxVisibility(role);
        }
        if ("doctor".equalsIgnoreCase(role)) {
            licenseNoField.setText(safe(employeeToEdit.getLicenseNo()));
        }
        activeCheckBox.setSelected(employeeToEdit.isActive());
        if (subtitleLabel != null) {
            subtitleLabel.setText("Chỉnh sửa thông tin cho: " +
                    (employeeToEdit.getUsername() != null ? "@" + employeeToEdit.getUsername()
                            : "#" + employeeToEdit.getId()));
        }
    }

    private void updateLicenseBoxVisibility(String role) {
        if (licenseBox == null)
            return;
        boolean isDoctor = "doctor".equalsIgnoreCase(role);
        licenseBox.setVisible(isDoctor);
        licenseBox.setManaged(isDoctor);
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (employeeToEdit == null)
            return;

        // Validate
        String firstname = firstnameField.getText();
        String lastname = lastnameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String role = roleComboBox.getValue();
        String licenseNo = licenseNoField.getText();
        String avatar = avatarField.getText();
        boolean active = activeCheckBox.isSelected();
        if (firstname == null || firstname.trim().isEmpty()) {
            showError("Vui lòng nhập họ và tên đệm");
            return;
        }
        if (lastname == null || lastname.trim().isEmpty()) {
            showError("Vui lòng nhập tên");
            return;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            showError("Vui lòng nhập email hợp lệ");
            return;
        }
        if (phone == null || phone.trim().isEmpty() || phone.trim().length() < 10) {
            showError("Vui lòng nhập số điện thoại hợp lệ (10 số)");
            return;
        }
        if (role == null || role.trim().isEmpty()) {
            showError("Vui lòng chọn vai trò");
            return;
        }
        if ("doctor".equalsIgnoreCase(role) && (licenseNo == null || licenseNo.trim().isEmpty())) {
            showError("Bác sĩ phải có số giấy phép hành nghề");
            return;
        }

        // Update employee object
        employeeToEdit.setFirstname(firstname.trim());
        employeeToEdit.setLastname(lastname.trim());
        employeeToEdit.setEmail(email.trim());
        employeeToEdit.setPhone(phone.trim());
        employeeToEdit.setRole(role.toLowerCase());
        employeeToEdit.setActive(active);

        if ("doctor".equalsIgnoreCase(role)) {
            employeeToEdit.setLicenseNo(licenseNo.trim());
        } else {
            employeeToEdit.setLicenseNo(null);
        }

        if (avatar != null && !avatar.trim().isEmpty()) {
            employeeToEdit.setAvatar(avatar.trim());
        } else {
            employeeToEdit.setAvatar(null);
        }
        executeAsync(() -> {
            try {
                return service.updateEmployee(employeeToEdit);
            } catch (Exception ex) {
                System.err.println("Cập nhật lỗi: " + ex.getMessage());
                return null;
            }
        }, updatedEmployee -> {
            if (updatedEmployee != null) {
                showSuccess("Cập nhật nhân viên thành công!");
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
                SceneManager.removeSceneData("employeeDetailData");
                SceneManager.setSceneData("employeeDetailData", employeeToEdit);
            } else {
                showError("Cập nhật nhân viên thất bại");
            }
        });
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onButtonHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button button) {
            button.setStyle(button.getStyle() + "; -fx-opacity: 0.9; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        }
    }

    @FXML
    public void onButtonExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button button) {
            button.setStyle(button.getStyle().replace("; -fx-opacity: 0.9; -fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
