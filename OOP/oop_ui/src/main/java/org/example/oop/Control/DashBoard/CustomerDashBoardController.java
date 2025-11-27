package org.example.oop.Control.DashBoard;

import org.example.oop.Control.BaseController;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Utils.ErrorHandler;
import org.example.oop.Utils.SafeNavigator;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.example.oop.Utils.SessionValidator;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;

public class CustomerDashBoardController extends BaseController {

    @FXML
    private StackPane rootPane;

    @FXML
    private MenuButton nameField;

    @FXML
    private Label welcomeText;

    @FXML
    private Label dateLabel;

    @FXML
    private javafx.scene.layout.AnchorPane loadingOverlay;

    private Customer currentCustomer;

    @FXML
    public void initialize() {
        System.out.println("🔵 CustomerDashboard: Initializing...");

        if (!SessionValidator.validateCustomerSession()) {
            System.err.println("❌ CustomerDashboard: Session validation failed");
            Platform.runLater(() -> {
                ErrorHandler.showCustomError(401,
                        "Phiên đăng nhập đã hết hạn.\n\nVui lòng đăng nhập lại.");
                redirectToLogin("Session validation failed");
            });
            return;
        }

        try {
            loadCustomerData();
        } catch (Exception e) {
            System.err.println("❌ CustomerDashboard: Failed to load customer data");
            handleInitializationError(e);
            return;
        }

        if (!validateCustomerRole()) {
            System.err.println("❌ CustomerDashboard: Role validation failed");
            return;
        }

        setupUI();

        // Hide loading overlay after initialization
        hideLoadingOverlay();

        System.out.println("✅ CustomerDashboard: Initialization complete");
    }

    private void loadCustomerData() throws Exception {
        currentCustomer = SceneManager.getSceneData("accountData");
        if (currentCustomer == null) {
            throw new Exception("Customer data is null in session");
        }
        System.out.println("📊 Loaded customer: " + currentCustomer.getFullName());
    }

    private boolean validateCustomerRole() {
        if (currentCustomer == null) {
            redirectToLogin("Không tìm thấy thông tin người dùng");
            return false;
        }
        return true;
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            System.out.println("✅ Loading overlay hidden");
        }
    }

    private void setupUI() {
        if (currentCustomer == null) {
            System.err.println("⚠️ Customer is null, cannot setup UI");
            return;
        }

        String fullName = currentCustomer.getFullName();

        // Fallback if fullName is null or empty
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = currentCustomer.getUsername();
            System.out.println("⚠️ Using username as fallback: " + fullName);
        }

        System.out.println("📝 Setting up UI with name: " + fullName);

        if (nameField != null) {
            nameField.setText(fullName);
            nameField.setStyle("-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 15px;");
        }
        if (welcomeText != null) {
            welcomeText.setText("Xin chào, " + fullName + "! 👋");
            welcomeText.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        }
        if (dateLabel != null) {
            dateLabel.setText("📅 Hôm nay: " + getVietnameseDateString());
        }

        System.out.println("✅ UI setup complete");
    }
    
    private String getVietnameseDateString() {
        java.time.LocalDate today = java.time.LocalDate.now();
        String[] dayNames = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String dayOfWeek = dayNames[today.getDayOfWeek().getValue() % 7];
        return String.format("%s, %02d tháng %02d năm %d", 
            dayOfWeek, today.getDayOfMonth(), today.getMonthValue(), today.getYear());
    }

    private void handleInitializationError(Exception e) {
        System.err.println("❌ Initialization error: " + e.getMessage());
        e.printStackTrace();

        Platform.runLater(() -> {
            ErrorHandler.showCustomError(500,
                    "Không thể khởi tạo trang khách hàng.\n\n" +
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
    private void handleAppointmentBookingButton() {
        System.out.println("🔄 Customer: Opening Appointment Booking...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_BOOKING_FXML,
                SceneConfig.Titles.APPOINTMENT_BOOKING);
    }

    @FXML
    private void handlePaymentHistoryButton() {
        System.out.println("🔄 Customer: Opening Payment History...");
        SafeNavigator.navigate(
                SceneConfig.PAYMENT_HISTORY_FXML,
                SceneConfig.Titles.PAYMENT_HISTORY);
    }

    @FXML
    private void handleAppointmentManagement() {
        System.out.println("🔄 Customer: Opening Appointment Management...");
        SafeNavigator.navigate(
                SceneConfig.APPOINTMENT_MANAGEMENT_FXML,
                SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }

    @FXML
    private void handleCustomerDetailViewButton() {
        System.out.println("🔄 Customer: Opening Customer Detail...");
        SafeNavigator.navigate(
                SceneConfig.CUSTOMER_DETAIL_FXML,
                SceneConfig.Titles.CUSTOMER_DETAIL);
    }

    @FXML
    private void handleLogout() {
        System.out.println("🔄 Customer: Logout requested...");
        boolean confirmed = showConfirmation(
                "Đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất?");

        if (confirmed) {
            logout();
        } else {
            System.out.println("⚠️ User cancelled logout");
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

        javafx.scene.control.ButtonType yesButton = new javafx.scene.control.ButtonType("Yes");
        javafx.scene.control.ButtonType noButton = new javafx.scene.control.ButtonType(
                "No",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait()
                .map(response -> response == yesButton)
                .orElse(false);
    }
}
