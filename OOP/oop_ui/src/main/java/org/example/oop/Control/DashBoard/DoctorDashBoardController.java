package org.example.oop.Control.DashBoard;

import org.example.oop.Control.BaseController;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.SafeNavigator;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.example.oop.Utils.SessionValidator;
import org.miniboot.app.domain.models.Employee;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class DoctorDashBoardController extends BaseController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label userNameLabel;

    private Employee currEmployee;

    @FXML
    public void initialize() {
        System.out.println("DoctorDashBoard : Initialinzing ...");
        if (!SessionValidator.validateEmployeeSession()) {
            System.err.println("DoctorDahboard : Session validation failed");
            Platform.runLater(() -> {
                ErrorHandler.showCustomError(401, "Phiên đăng nhập đã hết hạn.\n\nVui lòng đăng nhập lại.");
                redirectToLogin("Session validation fail");
            });
            return;
        }
        try {
            loadEmployeeData();
        } catch (Exception e) {
            System.err.println("DoctordashBoard : fail to load employee data");
            handleInitializationError(e);
            return;
        }
        if (!validateDoctorRole()) {
            System.err.println("DoctordashBoard: Role validation failed");
            return;
        }
        setupUI();
        System.out.println("DoctordashBoard : Initialization complete");
    }

    private void setupUI() {
        if (currEmployee == null) {
            return;
        }
        String fullName = currEmployee.getFirstname() + currEmployee.getLastname();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Chào mừng trở lại, " + fullName + "!");
        }
        if (roleLabel != null) {
            roleLabel.setText("BÁC SĨ");
        }
        if (userNameLabel != null) {
            String username = SessionStorage.getCurrentUsername();
            if (username != null && !username.isEmpty()) {
                userNameLabel.setText("@" + username);
            } else {
                userNameLabel.setText("@" + currEmployee.getUsername());
            }
        }
    }

    private boolean validateDoctorRole() {
        if (currEmployee == null) {
            redirectToLogin("Không tìm thấy thông tin người dùng");
            return false;
        }
        String role = currEmployee.getRole();
        if (!"doctor".equalsIgnoreCase(role)) {
            Platform.runLater(() -> {
                ErrorHandler.showUserFriendlyError(403, "Bạn không có quyền truy cập trang bác sĩ.\n\n" +
                        "Vai trò của bạn: " + role);
            });
            redirectToDashboardByRole(role);
            return false;
        }
        return true;
    }

    private void redirectToDashboardByRole(String role) {
        if (role == null) {
            redirectToLogin("Null Role");
            return;
        }
        String fxml;
        String title;
        switch (role.toLowerCase()) {
            case "admin":
                fxml = SceneConfig.ADMIN_DASHBOARD_FXML;
                title = "Admin Dashboard";
                break;
            case "nurse":
                fxml = SceneConfig.NURSE_DASHBOARD_FXML;
                title = "Nurse Dashboard";
                break;
            case "customer":
                fxml = SceneConfig.CUSTOMER_DASHBOARD_FXML;
                title = "Customer Dashboard";
                break;
            default:
                redirectToLogin("Unknown role: " + role);
                return;
        }
        SafeNavigator.navigate(fxml, title);
    }

    private void loadEmployeeData() throws Exception {
        currEmployee = SceneManager.getSceneData("accountData");
        if (currEmployee == null) {
            throw new Exception("Employee data is null is session");
        }
        System.out.println("Loaded employee: " + currEmployee.getUsername()
                + "(role " + currEmployee.getRole() + ")");
    }

    private void redirectToLogin(String reason) {
        System.out.println("redirecting to login . Reason :" + reason);
        SceneManager.removeSceneData("accountData");
        SceneManager.removeSceneData("authToken");
        SceneManager.removeSceneData("role");
        SafeNavigator.navigate(SceneConfig.LOGIN_FXML, SceneConfig.Titles.LOGIN);
    }

    private void handleInitializationError(Exception e) {
        System.err.println("Initialization error : " + e.getMessage());
        Platform.runLater(() -> {
            ErrorHandler.showCustomError(500,
                    "Không thể khởi tạo trang bác sĩ.\n\n" +
                            "Chi tiết: " + e.getMessage() + "\n\n" +
                            "Vui lòng đăng nhập lại.");
            redirectToLogin("Initialization error");
        });
    }

    @FXML
    private void handleBackButton() {
        try {
            if (isGoingBackToLogin()) {
                boolean comfirm = showConfirmation("Đăng Xuất", "Bạn có chắc chắn muốn đăng xuất hay không ?");
                if (comfirm) {
                    logout();
                } else {
                    System.out.println("User cancelled logout");
                }
            } else {
                SceneManager.goBack();
                System.out.println("navigated back successfully");
            }
        } catch (Exception e) {
            System.err.println("Cannot go back" + e.getMessage());
            showWarning("Không thể quay lại trang trước");
        }
    }

    @FXML
    private void handleForwardButton() {
        try {
            SceneManager.goForward();
            System.out.println("✅ Navigated forward successfully");
        } catch (Exception e) {
            System.err.println("❌ Cannot go forward: " + e.getMessage());
            showWarning("Không thể tiến tới trang tiếp theo");
        }
    }

    @FXML
    private void handleReloadButton() {
        try {
            SceneManager.reloadCurrentScene();
            System.out.println("✅ Page reloaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Cannot reload: " + e.getMessage());
            showError("Không thể tải lại trang.\n\nChi tiết: " + e.getMessage());
        }
    }

    @FXML
    private void openSchedule() {
        System.out.println("🔄 Doctor: Opening Schedule...");
        SafeNavigator.navigate(
                SceneConfig.CALENDAR_FXML,
                SceneConfig.Titles.CALENDAR);
    }

    @FXML
    private void openProfile() {
        System.out.println("🔄 Doctor: Opening Profile...");
        SceneManager.setSceneData("employeeDetailData", currEmployee);
        SceneManager.setSceneData("isModal", true);
        SafeNavigator.openModal(SceneConfig.EMPLOYEE_DETAIL_FXML, SceneConfig.Titles.EMPLOYEE_DETAIL, () -> {
            SceneManager.removeSceneData("employeeDetailData");
            SceneManager.removeSceneData("isModal");
        });

    }

    @FXML
    private void onOpenCustomerHub() {
        System.out.println("🔄 Doctor: Opening Customer Hub...");
        SafeNavigator.navigate(
                SceneConfig.CUSTOMER_HUB_FXML,
                SceneConfig.Titles.CUSTOMER_HUB);
    }

    @FXML
    private void onOpenAppointmentManagement() {
        System.out.println("🔄 Doctor: Opening Appointment Management...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_MANAGEMENT_FXML,
                SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }

    private boolean isGoingBackToLogin() {
        return true;
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType yesButtonType = new ButtonType("Có");
        ButtonType noButtonType = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButtonType, noButtonType);
        return alert.showAndWait().map(response -> response == yesButtonType).orElse(false);
    }

    private void logout() {
        try {
            SceneManager.removeSceneData("accountData");
            SceneManager.removeSceneData("authToken");
            SceneManager.removeSceneData("role");
            SessionStorage.clear();
            SafeNavigator.navigate(SceneConfig.LOGIN_FXML, SceneConfig.Titles.LOGIN);
            System.out.println("Logout successful");
        } catch (Exception e) {
            System.err.println("logout error " + e.getMessage());
            showError("Lỗi khi đăng xuất" + e.getMessage());
        }
    }
}
