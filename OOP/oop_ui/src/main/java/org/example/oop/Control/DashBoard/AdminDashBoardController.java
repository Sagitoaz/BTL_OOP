package org.example.oop.Control.DashBoard;

import org.example.oop.Control.BaseController;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.SafeNavigator;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.example.oop.Utils.SessionValidator;
import org.miniboot.app.domain.models.Admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * AdminDashBoardController - Controller cho trang quản trị viên
 * 
 * Chức năng:
 * - Quản lý toàn bộ hệ thống phòng khám
 * - Điều hướng đến các module: nhân viên, bệnh nhân, lịch hẹn, kho, thanh toán
 * - Xử lý lỗi session, navigation và permission
 * 
 * @author Copilot
 * @version 1.0
 * @since 2025-11-15
 */
public class AdminDashBoardController extends BaseController {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label userNameLabel;

    private Admin currentAdmin;

    @FXML
    public void initialize() {
        System.out.println("🔵 AdminDashboard: Initializing...");

        // BƯỚC 1: Validate session (đồng bộ)
        if (!SessionValidator.validateAdminSession()) {
            System.err.println("❌ AdminDashboard: Session validation failed - redirecting to login");
            Platform.runLater(() -> {
                ErrorHandler.showCustomError(401,
                        "Phiên đăng nhập đã hết hạn.\n\n" +
                                "Vui lòng đăng nhập lại để tiếp tục.");
                redirectToLogin("Session validation failed");
            });
            return;
        }

        try {
            loadEmployeeData();
            System.out.println("✅ AdminDashboard: Employee data loaded");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard: Failed to load employee data");
            handleInitializationError(e);
            return;
        }

        if (!validateAdminRole()) {
            System.err.println("❌ AdminDashboard: Role validation failed");
            return;
        }

        try {
            setupUI();
            System.out.println("✅ AdminDashboard: UI setup complete");
        } catch (Exception e) {
            System.err.println("❌ AdminDashboard: Failed to setup UI");
            e.printStackTrace();
        }

        loadDashboardStatistics();

        System.out.println("✅ AdminDashboard: Initialization complete");
    }

    private void loadEmployeeData() throws Exception {
        currentAdmin = SceneManager.getSceneData("accountData");
        if (currentAdmin == null) {
            throw new Exception("Admin data is null in session");
        }
        System.out.println("📊 Loaded admin: " + currentAdmin.getUsername());
    }

    private boolean validateAdminRole() {
        if (currentAdmin == null) {
            redirectToLogin("Không tìm thấy thông tin người dùng");
            return false;
        }

        // Admin không có role property, luôn là admin
        return true;
    }

    private void setupUI() {
        if (currentAdmin == null)
            return;
        String displayName = currentAdmin.getUsername();

        if (welcomeLabel != null) {
            welcomeLabel.setText("Chào mừng trở lại, " + displayName + "! 👋");
        }
        if (roleLabel != null) {
            roleLabel.setText("QUẢN TRỊ VIÊN");
        }
        if (userNameLabel != null) {
            String realUsername = SessionStorage.getCurrentUsername();
            if (realUsername != null && !realUsername.isEmpty()) {
                userNameLabel.setText("@" + realUsername);
            } else {
                userNameLabel.setText("@" + currentAdmin.getUsername());
            }
        }
    }

    private void loadDashboardStatistics() {
        executeAsync(
                () -> {
                    try {
                        Thread.sleep(300);
                        return null;
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to load statistics: " + e.getMessage());
                        return null;
                    }
                },
                stats -> {
                    System.out.println("✅ Statistics loaded");
                },
                error -> {
                    System.err.println("⚠️ Statistics loading failed: " + error.getMessage());
                });
    }

    private void handleInitializationError(Exception e) {
        System.err.println("❌ Initialization error: " + e.getMessage());
        e.printStackTrace();

        Platform.runLater(() -> {
            ErrorHandler.showCustomError(500,
                    "Không thể khởi tạo trang quản trị.\n\n" +
                            "Chi tiết: " + e.getMessage() + "\n\n" +
                            "Vui lòng đăng nhập lại.");

            redirectToLogin("Initialization error");
        });
    }

    @FXML
    private void handleBackButton() {
        try {
            if (isGoingBackToLogin()) {
                boolean confirmed = showConfirmation(
                        "Đăng xuất",
                        "Bạn có chắc chắn muốn đăng xuất?");

                if (confirmed) {
                    logout();
                } else {
                    System.out.println("⚠️ User cancelled logout");
                }
            } else {
                // Navigate back bình thường
                SceneManager.goBack();
                System.out.println("✅ Navigated back successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Cannot go back: " + e.getMessage());
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
    private void handleOpenCustomerHub() {
        System.out.println("🔄 Admin: Opening Customer Hub...");
        SafeNavigator.navigate(
                SceneConfig.CUSTOMER_HUB_FXML,
                SceneConfig.Titles.CUSTOMER_HUB);
    }

    @FXML
    private void handleOpenAppointmentManagement() {
        System.out.println("🔄 Admin: Opening Appointment Management...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_MANAGEMENT_FXML,
                SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }

    @FXML
    private void handleOpenInventory() {
        System.out.println("🔄 Admin: Opening Inventory...");
        SafeNavigator.navigate(
                SceneConfig.PRODUCT_CRUD_VIEW_FXML,
                SceneConfig.Titles.PRODUCT_CRUD);
    }

    @FXML
    private void handleOpenPayment() {
        System.out.println("🔄 Admin: Opening Payment...");
        SafeNavigator.navigate(
                SceneConfig.INVOICE_FXML,
                SceneConfig.Titles.INVOICE);
    }

    @FXML
    private void handleOpenEmployeeManagement() {
        System.out.println("🔄 Admin: Opening Employee Management...");
        SafeNavigator.navigateWithPermissionCheck(
                "admin",
                "EMPLOYEE_MANAGEMENT",
                SceneConfig.EMPLOYEE_MANAGEMENT_FXML,
                SceneConfig.Titles.EMPLOYEE_MANAGEMENT);
    }

    @FXML
    private void handleOpenDoctorSchedule() {
        System.out.println("🔄 Admin: Opening Doctor Schedule...");
        SafeNavigator.navigate(
                SceneConfig.DOCTOR_SCHEDULE_FXML,
                SceneConfig.Titles.DOCTOR_SCHEDULE);
    }

    @FXML
    private void handleOpenWarehouse() {
        System.out.println("🔄 Admin: Opening Warehouse...");
        SafeNavigator.navigate(
                SceneConfig.SEARCH_INVENTORY_VIEW_FXML,
                SceneConfig.Titles.SEARCH_INVENTORY);
    }

    @FXML
    private void handleOpenStockMovement() {
        System.out.println("🔄 Admin: Opening Stock Movement...");
        SafeNavigator.navigate(
                SceneConfig.STOCK_MOVEMENT_VIEW_FXML,
                SceneConfig.Titles.STOCK_MOVEMENT);
    }

    @FXML
    private void handleOpenPrescriptions() {
        System.out.println("🔄 Admin: Opening Prescriptions (via Customer Hub)...");
        // Navigate to CustomerHub where Admin can view prescription list
        // and double-click on any prescription to edit it
        SafeNavigator.navigate(
                SceneConfig.CUSTOMER_HUB_FXML,
                SceneConfig.Titles.CUSTOMER_HUB);
    }

    @FXML
    private void handleOpenProfile() {
        System.out.println("🔄 Admin: Opening Profile...");

        // Set admin data cho profile view
        SceneManager.setSceneData("adminDetailData", currentAdmin);
        SceneManager.setSceneData("isModal", true);

        SafeNavigator.openModal(
                SceneConfig.EMPLOYEE_DETAIL_FXML,
                SceneConfig.Titles.EMPLOYEE_DETAIL,
                () -> {
                    SceneManager.removeSceneData("adminDetailData");
                    SceneManager.removeSceneData("isModal");
                });
    }

    @FXML
    private void handleLogout() {
        System.out.println("🔄 Admin: Logging out...");

        // Confirmation dialog
        boolean confirmed = showConfirmation(
                "Đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất?");

        if (confirmed) {
            logout();
        }
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
            case "doctor":
                fxml = SceneConfig.DOCTOR_DASHBOARD_FXML;
                title = "Doctor Dashboard";
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
}
