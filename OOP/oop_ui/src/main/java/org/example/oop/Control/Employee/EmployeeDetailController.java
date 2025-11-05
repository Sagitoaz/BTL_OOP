package org.example.oop.Control.Employee;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpAppointmentService;
import org.example.oop.Service.HttpEmployeeService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Employee;

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

public class EmployeeDetailController extends BaseController {

    private static final DateTimeFormatter CREATED_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private final HttpEmployeeService service = new HttpEmployeeService();
    private final HttpAppointmentService appointmentService = new HttpAppointmentService();
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
    @FXML
    private Label roleDetailLabel;
    @FXML
    private HBox licenseBox;
    @FXML
    private Label licenseNoLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label activeStatusLabel;
    @FXML
    private Label totalAppointmentsLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label workingDaysLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button deleteButton;
    @FXML
    private void handleBackButton(){
        SceneManager.goBack();
    }
    @FXML
    private void handleForwardButton(){
        SceneManager.goForward();
    }
    @FXML
    private void handleReloadButton(){
        SceneManager.reloadCurrentScene();
    }

    @FXML
    public void initialize() {
        if (closeButton != null)
            closeButton.setOnAction(e -> handleClose(e));
        if (editButton != null)
            editButton.setOnAction(e -> handleEdit(e));
        if (deleteButton != null)
            deleteButton.setOnAction(e -> handleDelete(e));
        if (licenseBox != null) {
            licenseBox.setVisible(false);
            licenseBox.setManaged(false);
        }
        if(SceneManager.getSceneData("employeeDetailData") != null){
            Employee emp = (Employee)SceneManager.getSceneData("employeeDetailData");
            setEmployeeDetails(emp);
        }
    }
    public void setEmployeeDetails(Employee employee) {
        this.employee = employee;
        updateUI();
    }

    private void updateUI() {
        if (employee == null)
            return;
        fullNameLabel.setText(safe(employee.getFirstname()) + " " + safe(employee.getLastname()));
        roleLabel.setText(safe(employee.getRole()));
        usernameLabel.setText(employee.getUsername() == null ? "" : "@" + employee.getUsername());
        employeeIdLabel.setText("ID: " + employee.getId());
        avatarLabel.setText(employee.getAvatar() != null && !employee.getAvatar().isBlank() ? "🖼️" : "👤");
        emailLabel.setText(safe(employee.getEmail()));
        phoneLabel.setText(safe(employee.getPhone()));
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
        LocalDateTime created = employee.getCreatedAt();
        createdAtLabel.setText(created == null ? "—" : CREATED_FMT.format(created));

        boolean active = employee.isActive();
        activeStatusLabel.setText(active ? "Đang hoạt động" : "Không hoạt động");
        statusBadge.setText(active ? "HOẠT ĐỘNG" : "KHÓA");
        if (active) {
            statusBadge.setStyle(
                    "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            activeStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            statusBadge.setStyle(
                    "-fx-background-color: #f56565; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12;");
            activeStatusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        }
        if (totalAppointmentsLabel != null)
            totalAppointmentsLabel.setText("—");
        if (totalPatientsLabel != null)
            totalPatientsLabel.setText("—");
        if (workingDaysLabel != null)
            workingDaysLabel.setText("—");
        if (appointmentService != null && employee.getId() > 0) {
            executeAsync(() -> {
                try {
                    List<Appointment> appts = appointmentService.getAppointmentsFiltered(employee.getId(), null, null,
                            null, null, null);
                    int totalAppointments = appts.size();
                    Set<Integer> distinctPatients = new HashSet<>();
                    Set<LocalDate> workingDates = new HashSet<>();
                    for (Appointment a : appts) {
                        distinctPatients.add(a.getCustomerId());
                        if (a.getStartTime() != null)
                            workingDates.add(a.getStartTime().toLocalDate());
                    }
                    int totalPatients = distinctPatients.size();
                    int workingDays = workingDates.size();

                    return new int[] { totalAppointments, totalPatients, workingDays };
                } catch (Exception ex) {
                    System.err.println("Khong the lay appointments: " + ex.getMessage());
                    return new int[] { 0, 0, 0 };
                }
            }, result -> {
                if (result != null && result.length == 3) {
                    if (totalAppointmentsLabel != null)
                        totalAppointmentsLabel.setText(String.valueOf(result[0]));
                    if (totalPatientsLabel != null)
                        totalPatientsLabel.setText(String.valueOf(result[1]));
                    if (workingDaysLabel != null)
                        workingDaysLabel.setText(String.valueOf(result[2]));
                } else {
                    if (totalAppointmentsLabel != null)
                        totalAppointmentsLabel.setText("0");
                    if (totalPatientsLabel != null)
                        totalPatientsLabel.setText("0");
                    if (workingDaysLabel != null)
                        workingDaysLabel.setText("0");
                }
            }, throwable -> {
                System.err.println("Không thể lấy thống kê appointments: " + throwable.getMessage());
                if (totalAppointmentsLabel != null)
                    totalAppointmentsLabel.setText("0");
                if (totalPatientsLabel != null)
                    totalPatientsLabel.setText("0");
                if (workingDaysLabel != null)
                    workingDaysLabel.setText("0");
            });
        } else {
            if (totalAppointmentsLabel != null)
                totalAppointmentsLabel.setText("0");
            if (totalPatientsLabel != null)
                totalPatientsLabel.setText("0");
            if (workingDaysLabel != null)
                workingDaysLabel.setText("0");
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        if (closeButton == null)
            return;
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        if (employee == null)
            return;


        SceneManager.setSceneData("employeeEdit", employee);
        SceneManager.openModalWindow(SceneConfig.EMPLOYEE_EDIT_FORM_FXML, SceneConfig.Titles.EMPLOYEE_EDIT_FORM, () -> {
                try {
                    Employee refreshed = SceneManager.getSceneData("employeeDetailData");
                    if (refreshed != null)
                        SceneManager.removeSceneData("employeeDetailData");
                        setEmployeeDetails(refreshed);
                } catch (Exception ex) {
                    System.err.println("Không thể làm mới thông tin sau khi edit: " + ex.getMessage());
                }
        });

    }

    @FXML
    public void handleDelete(ActionEvent event) {
        if (employee == null)
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa nhân viên");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Bạn có chắc muốn xóa nhân viên #" + employee.getId() + " (" + employee.getUsername() + ")?");
        Optional<ButtonType> pressed = confirm.showAndWait();
        if (pressed.isPresent() && pressed.get() == ButtonType.OK) {
            executeAsync(() -> {
                try {
                    return service.deleteEmployee(employee.getId());
                } catch (Exception ex) {
                    System.err.println("Xóa lỗi: " + ex.getMessage());
                    return false;
                }
            }, ok -> {
                if (Boolean.TRUE.equals(ok)) {
                    showSuccess("Đã xóa nhân viên #" + employee.getId());
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
