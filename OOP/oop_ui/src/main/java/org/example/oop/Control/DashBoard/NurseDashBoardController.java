package org.example.oop.Control.DashBoard;

import org.example.oop.Control.BaseController;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.SafeNavigator;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.example.oop.Utils.SessionValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.miniboot.app.domain.models.Employee;

public class NurseDashBoardController extends BaseController {
    @FXML
    private StackPane rootPane;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label dateLabel;

    private Employee currentEmployee;

    @FXML
    public void initialize() {
        System.out.println("NurseDashBoard : Initializing ..");

        if (!SessionValidator.validateEmployeeSession()) {
            System.err.println("NurseDashBoard : session validation failed");
            Platform.runLater(() -> {
                ErrorHandler.showCustomError(401,
                        "Phiên đăng nhập đã hết hạn. \n\n Vui lòng đăng nhập lại.");
                redirectToLogin("Session  validation failed");
            });
            return;
        }

        try {
            loadEmployeeData();
        } catch (Exception e) {
            System.err.println("NurseDashBoard : failed to load employee data");
            handleInitializationError(e);
            return;
        }

        if (!validateNurseRole()) {
            System.err.println("NurseDashBoard : Role validation failed");
            return;
        }

        setupUI();

        System.out.println("NurseDashBoard : Initialization complete");
    }

    private void loadEmployeeData() throws Exception {
        currentEmployee = SceneManager.getSceneData("accountData");
        if (currentEmployee == null) {
            throw new Exception("Employee data ius null session");
        }
        System.out
                .println("Load Employee " + currentEmployee.getUsername() + "(role " + currentEmployee.getRole() + ")");
    }

    private boolean validateNurseRole() {
        if (currentEmployee == null) {
            redirectToLogin("Không tìm thấy thông tin người dùng");
            return false;
        }
        String role = currentEmployee.getRole();
        if (!"nurse".equalsIgnoreCase(role)) {
            Platform.runLater(() -> {
                ErrorHandler.showUserFriendlyError(403,
                        "Bạn không có quyền truy cập trang y tá. \n\n " + "Vai trò của bạn là " + role);
            });
            redirectToDashboardByRole(role);
            return false;
        }
        return true;
    }

    private void setupUI() {
        if (currentEmployee == null)
            return;
        String fullName = currentEmployee.getFirstname() + " " + currentEmployee.getLastname();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Chào mừng trở lại, " + fullName + "! 👋");
        }
        if (roleLabel != null) {
            roleLabel.setText("Y TÁ");
        }
        if (userNameLabel != null) {
            String username = SessionStorage.getCurrentUsername();
            if (username != null && !username.isEmpty()) {
                userNameLabel.setText("@" + username);
            } else {
                userNameLabel.setText("@" + currentEmployee.getUsername());
            }
        }
        if (dateLabel != null) {
            dateLabel.setText("📅 Hôm nay: " + getVietnameseDateString());
        }
    }
    
    private String getVietnameseDateString() {
        java.time.LocalDate today = java.time.LocalDate.now();
        String[] dayNames = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String dayOfWeek = dayNames[today.getDayOfWeek().getValue() % 7];
        return String.format("%s, %02d tháng %02d năm %d", 
            dayOfWeek, today.getDayOfMonth(), today.getMonthValue(), today.getYear());
    }

    private void handleInitializationError(Exception e) {
        System.err.println("Initialization error " + e.getMessage());
        e.printStackTrace();
        Platform.runLater(() -> {
            ErrorHandler.showCustomError(500, "Không thể khởi tạo trang y tá.\n\n" +
                    "Chi tiết: " + e.getMessage() + "\n\n" +
                    "Vui lòng đăng nhập lại.");
            redirectToLogin("Initialization error");
        });
    }

    @FXML
    private void handleBackButton() {
        try {
            if (isGoingBackToLogin()) {
                boolean confirm = showConfirmation("Đăng xuất", " Bạn có muốn đăng xuất hay không ?");
                if (confirm) {
                    logout();
                } else {
                    System.out.println("User cancelled logout");
                }
            } else {
                SceneManager.goBack();
                System.out.println("Navigate back successfully");
            }
        } catch (Exception e) {
            System.err.println("Cannot go back() " + e.getMessage());
            showWarning("không thể uqy lại trang trước");
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
    private void openCustomerHub() {
        System.out.println("🔄 Nurse: Opening Customer Hub...");
        SafeNavigator.navigate(
                SceneConfig.CUSTOMER_HUB_FXML,
                SceneConfig.Titles.CUSTOMER_HUB);
    }

    @FXML
    private void openPayment() {
        System.out.println("🔄 Nurse: Opening Payment...");
        SafeNavigator.navigate(
                SceneConfig.INVOICE_FXML,
                SceneConfig.Titles.INVOICE);
    }

    @FXML
    private void openProfile() {
        System.out.println("🔄 Nurse: Opening Profile...");

        SceneManager.setSceneData("employeeDetailData", currentEmployee);
        SceneManager.setSceneData("isModal", true);

        SafeNavigator.openModal(
                SceneConfig.EMPLOYEE_DETAIL_FXML,
                SceneConfig.Titles.EMPLOYEE_DETAIL,
                () -> {
                    SceneManager.removeSceneData("employeeDetailData");
                    SceneManager.removeSceneData("isModal");
                });
    }

    @FXML
    private void openInventory() {
        System.out.println("🔄 Nurse: Opening Inventory...");
        SafeNavigator.navigate(
                SceneConfig.PRODUCT_CRUD_VIEW_FXML,
                SceneConfig.Titles.PRODUCT_CRUD);
    }

    @FXML
    private void handleAppointmentBookingButton() {
        System.out.println("🔄 Nurse: Opening Appointment Booking...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_BOOKING_FXML,
                SceneConfig.Titles.APPOINTMENT_BOOKING);
    }

    @FXML
    private void handleAppointmentManagement() {
        System.out.println("🔄 Nurse: Opening Appointment Management...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_MANAGEMENT_FXML,
                SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }

    private boolean isGoingBackToLogin() {
        return true;
    }

    private void redirectToLogin(String reason) {
        System.out.println("⚠️ Redirecting to login. Reason: " + reason);
        SceneManager.removeSceneData("accountData");
        SceneManager.removeSceneData("authToken");
        SceneManager.removeSceneData("role");
        SafeNavigator.navigate(
                SceneConfig.LOGIN_FXML,
                SceneConfig.Titles.LOGIN);
    }

    private void redirectToDashboardByRole(String role) {
        if (role == null) {
            redirectToLogin("Null role");
            return;
        }

        String fxml;
        String title;

        switch (role.toLowerCase()) {
            case "admin":
                fxml = SceneConfig.ADMIN_DASHBOARD_FXML;
                title = "Admin Dashboard";
                break;
            case "doctor":
                fxml = SceneConfig.DOCTOR_DASHBOARD_FXML;
                title = "Doctor Dashboard";
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

    private void logout() {
        try {
            SceneManager.clearSceneData();
            SceneManager.clearCache();
            SessionStorage.clear();
            
            // Clear Login page from cache to force re-initialization
            SceneManager.removeFromCache(SceneConfig.LOGIN_FXML);
            
            SafeNavigator.navigate(
                    SceneConfig.LOGIN_FXML,
                    SceneConfig.Titles.LOGIN);

            System.out.println("✅ Logout successful");

        } catch (Exception e) {
            System.err.println("❌ Logout error: " + e.getMessage());
            showError("Lỗi khi đăng xuất: " + e.getMessage());
        }
    }

    private boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        javafx.scene.control.ButtonType yesButton = new javafx.scene.control.ButtonType("Có");
        javafx.scene.control.ButtonType noButton = new javafx.scene.control.ButtonType(
                "Không",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait()
                .map(response -> response == yesButton)
                .orElse(false);
    }

    @FXML
    private void handleLogout() {
        System.out.println("🔄 Nurse: Logging out...");

        // Confirmation dialog
        boolean confirmed = showConfirmation("Đăng xuất", "Bạn có chắc chắn muốn đăng xuất?");

        if (confirmed) {
            logout();
        }
    }
}
