package org.example.oop.Control;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;
import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Service.HttpEmployeeService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.UserRole;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField enterPasswordTextField;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Hyperlink forgotPassword;

    @FXML
    private Label invalidLoginMessage;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpbutton;

    private boolean isPasswordVisible = false;
    private PauseTransition errorMessageTimer;

    @FXML
    public void initialize() {
        // Khởi tạo timer cho thông báo lỗi (5 giây)
        errorMessageTimer = new PauseTransition(Duration.seconds(5));
        errorMessageTimer.setOnFinished(event -> hideErrorMessage());

        // Đồng bộ nội dung giữa PasswordField và TextField
        enterPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isPasswordVisible) {
                visiblePasswordTextField.setText(newValue);
            }
        });

        visiblePasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isPasswordVisible) {
                enterPasswordTextField.setText(newValue);
            }
        });

        // Thiết lập listeners để ẩn thông báo lỗi khi người dùng tương tác
        setupFieldListeners();
    }

    /**
     * Thiết lập listeners để ẩn thông báo lỗi khi người dùng click hoặc gõ
     */
    private void setupFieldListeners() {
        usernameTextField.setOnMouseClicked(event -> hideErrorMessage());
        enterPasswordTextField.setOnMouseClicked(event -> hideErrorMessage());
        visiblePasswordTextField.setOnMouseClicked(event -> hideErrorMessage());

        usernameTextField.setOnKeyPressed(event -> hideErrorMessage());
        enterPasswordTextField.setOnKeyPressed(event -> hideErrorMessage());
        visiblePasswordTextField.setOnKeyPressed(event -> hideErrorMessage());
    }

    /**
     * Ẩn thông báo lỗi và dừng timer
     */
    private void hideErrorMessage() {
        invalidLoginMessage.setVisible(false);
        if (errorMessageTimer != null) {
            errorMessageTimer.stop();
        }
    }

    /**
     * Hiển thị thông báo lỗi với timer tự động ẩn sau 5 giây
     */
    private void showErrorMessage(String message) {
        invalidLoginMessage.setText(message);
        invalidLoginMessage.setVisible(true);

        if (errorMessageTimer != null) {
            errorMessageTimer.stop();
            errorMessageTimer.playFromStart();
        }
    }

    /**
     * Toggle hiển thị/ẩn mật khẩu
     */
    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            visiblePasswordTextField.setText(enterPasswordTextField.getText());
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(true);
            enterPasswordTextField.setVisible(false);
            enterPasswordTextField.setManaged(false);
            togglePasswordButton.setText("🙈");
        } else {
            enterPasswordTextField.setText(visiblePasswordTextField.getText());
            enterPasswordTextField.setVisible(true);
            enterPasswordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
    }

    @FXML
    void ForgotPasswordHyperLinkOnClick(ActionEvent event) {
        SceneManager.switchScene(SceneConfig.RESET_PASSWORD_FXML, SceneConfig.RESET_PASSWORD_FXML);
    }

    @FXML
    void GoToSignUpButtonOnClick(ActionEvent event) {
        SceneManager.switchScene(SceneConfig.SIGNUP_FXML, SceneConfig.SIGNUP_FXML);
    }

    private String validateInput(String user, String pass) {
        if (user.isEmpty() && pass.isEmpty()) return "Vui lòng nhập tên đăng nhập và mật khẩu";
        if (user.isEmpty()) return "Vui lòng nhập tên đăng nhập";
        if (pass.isEmpty()) return "Vui lòng nhập mật khẩu";
        return null;
    }

    @FXML
    void LoginButtonOnClick(ActionEvent event) throws Exception {
        hideErrorMessage();

        String username = usernameTextField.getText().trim();
        String password = isPasswordVisible ?
                         visiblePasswordTextField.getText().trim() :
                         enterPasswordTextField.getText().trim();

        // Validate input
        String msg = validateInput(username, password);
        if (msg != null) {
            showErrorMessage(msg);
            return;
        }

        try {
            // Sử dụng AuthServiceWrapper để login qua backend
            Optional<String> sessionOpt = AuthServiceWrapper.login(username, password);

            if (sessionOpt.isPresent()) {
                String sessionId = sessionOpt.get();
                SessionStorage.setCurrentSessionId(sessionId);
                LOGGER.info("Login successful: " + SessionStorage.getCurrentUsername() +
                           " [" + SessionStorage.getCurrentUserRole() + "]");

                hideErrorMessage();

                // Redirect to dashboard based on role
                String role = SessionStorage.getCurrentUserRole();
                int userId = SessionStorage.getCurrentUserId();

                if (role.equalsIgnoreCase("ADMIN")) {
                    String[] key = {"role", "accountData"};
                    Object[] data = {UserRole.ADMIN, null};
                    SceneManager.switchSceneWithData(SceneConfig.ADMIN_DASHBOARD_FXML,
                                                    SceneConfig.Titles.DASHBOARD, key, data);

                } else if (role.equalsIgnoreCase("CUSTOMER")) {
                    try {
                        // Lấy thông tin customer từ API endpoint
                        Customer customer = CustomerRecordService.getInstance().searchCustomers(
                                String.valueOf(userId),
                                null,
                                null,
                                null
                        ).getData().get(0);

                        String[] key = {"role", "accountData"};
                        Object[] data = {UserRole.CUSTOMER, customer};
                        SceneManager.switchSceneWithData(SceneConfig.CUSTOMER_DASHBOARD_FXML,
                                                        SceneConfig.Titles.DASHBOARD, key, data);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error loading customer data", e);
                        showErrorMessage("Lỗi tải dữ liệu khách hàng. Vui lòng thử lại.");
                    }

                } else if (role.equalsIgnoreCase("EMPLOYEE")) {
                    try {
                        HttpEmployeeService employeeService = new HttpEmployeeService();
                        Employee employee = employeeService.getEmployeeById(userId);

                        String[] key = {"role", "accountData"};
                        Object[] data = {UserRole.EMPLOYEE, employee};

                        if (employee.getRole().equalsIgnoreCase("doctor")) {
                            SceneManager.switchSceneWithData(SceneConfig.DOCTOR_DASHBOARD_FXML,
                                                            SceneConfig.Titles.DASHBOARD, key, data);
                        } else {
                            SceneManager.switchSceneWithData(SceneConfig.NURSE_DASHBOARD_FXML,
                                                            SceneConfig.Titles.DASHBOARD, key, data);
                        }

                        LOGGER.info("Login as employee: " + employee.getFirstname() +
                                   " " + employee.getLastname());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error loading employee data", e);
                        showErrorMessage("Lỗi tải dữ liệu nhân viên. Vui lòng thử lại.");
                    }
                } else {
                    showErrorMessage("Vai trò không hợp lệ");
                }

            } else {
                showErrorMessage("Tên đăng nhập hoặc mật khẩu không đúng");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            showErrorMessage("Lỗi đăng nhập. Vui lòng thử lại.");
        }
    }
}
