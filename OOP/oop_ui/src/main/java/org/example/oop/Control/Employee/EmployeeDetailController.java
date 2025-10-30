package org.example.oop.Control.Employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.miniboot.app.domain.models.Employee;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EmployeeDetailController extends BaseController {

    private static final DateTimeFormatter CREATED_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private final HttpEmployeeService service = new HttpEmployeeService();
    private Employee employee;

    // Header / top
    @FXML
    private Label avatarLabel;
    @FXML
    private Label statusBadge;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label employeeIdLabel;
    @FXML
    private Label usernameLabel;

    // Contact
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;

    // Job
    @FXML
    private Label roleDetailLabel;
    @FXML
    private HBox licenseBox; // container for license UI
    @FXML
    private Label licenseNoLabel;

    // System
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label activeStatusLabel;

    // Stats (optional)
    @FXML
    private Label totalAppointmentsLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label workingDaysLabel;

    // Buttons
    @FXML
    private Button editButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button deleteButton;

    @FXML
    public void initialize() {
        // Wire actions
        if (closeButton != null) closeButton.setOnAction(e -> handleClose(e));
        if (editButton != null) editButton.setOnAction(e -> handleEdit(e));
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete(e));

        // Default hide license box until data loaded
        if (licenseBox != null) {
            licenseBox.setVisible(false);
            licenseBox.setManaged(false);
        }
    }

    public void setEmployeeDetails(Employee employee) {
        this.employee = employee;
        updateUI();
    }

    private void updateUI() {
        if (employee == null) return;

        // Header
        fullNameLabel.setText(safe(employee.getFirstname()) + " " + safe(employee.getLastname()));
        roleLabel.setText(safe(employee.getRole()));
        usernameLabel.setText(employee.getUsername() == null ? "" : "@" + employee.getUsername());
        employeeIdLabel.setText("ID: " + employee.getId());

        // Avatar (if avatar URL present we could load image later)
        avatarLabel.setText(employee.getAvatar() != null && !employee.getAvatar().isBlank() ? "🖼️" : "👤");

        // Contact
        emailLabel.setText(safe(employee.getEmail()));
        phoneLabel.setText(safe(employee.getPhone()));

        // Job
        roleDetailLabel.setText(employee.getRole() != null ? employee.getRole().toUpperCase() : "");
        boolean isDoctor = "doctor".equalsIgnoreCase(employee.getRole());
        if (isDoctor && employee.getLicenseNo() != null && !employee.getLicenseNo().isBlank()) {
            licenseNoLabel.setText(employee.getLicenseNo());
            licenseBox.setVisible(true);
            licenseBox.setManaged(true);
        } else {
            licenseBox.setVisible(false);
            licenseBox.setManaged(false);
            licenseNoLabel.setText("");
        }

        // System
        LocalDateTime created = employee.getCreatedAt();
        createdAtLabel.setText(created == null ? "—" : CREATED_FMT.format(created));

        boolean active = employee.isActive();
        activeStatusLabel.setText(active ? "Đang hoạt động" : "Không hoạt động");
        statusBadge.setText(active ? "HOẠT ĐỘNG" : "KHÓA");
        if (active) {
            statusBadge.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            activeStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            statusBadge.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            activeStatusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        }

        // Stats: currently no backend endpoint for stats
        totalAppointmentsLabel.setText("0");
        totalPatientsLabel.setText("0");
        workingDaysLabel.setText("0");
    }

    @FXML
    public void handleClose(ActionEvent event) {
        if (closeButton == null) return;
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        if (employee == null) return;
        try {
            var res = getClass().getResource("/FXML/Employee/EmployeeForm.fxml");
            if (res == null) {
                showError("Không tìm thấy form chỉnh sửa: /FXML/Employee/EmployeeForm.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof EmployeeFormController) {
                ((EmployeeFormController) controller).setEmployeeForEdit(employee);
            }

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa nhân viên — " + (employee.getUsername() == null ? "#" + employee.getId() : employee.getUsername()));
            stage.setScene(new Scene(root));
            if (closeButton != null && closeButton.getScene() != null && closeButton.getScene().getWindow() != null) {
                stage.initOwner(closeButton.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.showAndWait();

            // After editing, try to refresh UI from possibly updated employee (re-fetch by id)
            try {
                Employee refreshed = service.getEmployeeById(employee.getId());
                if (refreshed != null) setEmployeeDetails(refreshed);
            } catch (Exception ex) {
                // ignore refresh errors but log
                System.err.println("Không thể làm mới thông tin sau khi edit: " + ex.getMessage());
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi mở form chỉnh sửa: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        if (employee == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa nhân viên");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa nhân viên #" + employee.getId() + " (" + employee.getUsername() + ")?");
        Optional<ButtonType> pressed = confirm.showAndWait();
        if (pressed.isPresent() && pressed.get() == ButtonType.OK) {
            executeAsync(() -> {
                try {
                    return service.deleteEmployee(employee.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }, ok -> {
                if (Boolean.TRUE.equals(ok)) {
                    showSuccess("Đã xóa nhân viên #" + employee.getId());
                    // close detail window
                    handleClose(null);
                } else {
                    showError("Xóa thất bại");
                }
            });
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
