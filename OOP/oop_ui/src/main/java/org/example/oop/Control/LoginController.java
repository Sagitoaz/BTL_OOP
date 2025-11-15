package org.example.oop.Control;

import java.util.Optional;
import java.util.logging.Logger;

import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Service.HttpEmployeeService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.UserRole;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    // Trạng thái hiển thị mật khẩu
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
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
    }

    /**
     * Toggle hiển thị/ẩn mật khẩu
     */
    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Hiển thị mật khẩu
            visiblePasswordTextField.setText(enterPasswordTextField.getText());
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(true);
            enterPasswordTextField.setVisible(false);
            enterPasswordTextField.setManaged(false);
            togglePasswordButton.setText("🙈"); // Icon mắt đóng
        } else {
            // Ẩn mật khẩu
            enterPasswordTextField.setText(visiblePasswordTextField.getText());
            enterPasswordTextField.setVisible(true);
            enterPasswordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(false);
            togglePasswordButton.setText("👁"); // Icon mắt mở
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
        if (user.isEmpty() && pass.isEmpty())
            return "Enter username and password";
        if (user.isEmpty())
            return "Enter username";
        if (pass.isEmpty())
            return "Enter password";
        return null;
    }

    @FXML
    void LoginButtonOnClick(ActionEvent event) throws Exception {
        String username = usernameTextField.getText().trim();
        // Lấy password từ field đang hiển thị
        String password = isPasswordVisible ? visiblePasswordTextField.getText().trim()
                : enterPasswordTextField.getText().trim();

        // validate input
        String msg = validateInput(username, password);
        if (msg != null) {
            invalidLoginMessage.setText(msg);
            return;
        }

        // Call mini-boot AuthService through wrapper to avoid module issues
        Optional<String> sessionOpt = AuthServiceWrapper.login(username, password);

        if (sessionOpt.isPresent()) {
            String sessionId = sessionOpt.get();
            // Save sessionId to session storage for later use
            SessionStorage.setCurrentSessionId(sessionId);

            // Save auth token to SceneManager for SessionValidator
            SceneManager.setSceneData("authToken", sessionId);

            System.out.println("Login successful" + SessionStorage.getCurrentUsername() + " "
                    + SessionStorage.getCurrentUserRole());
            // Clear error message
            invalidLoginMessage.setText("");
            // Redirect to dashboard
            String userRole = SessionStorage.getCurrentUserRole();
            System.out.println("🔍 Redirecting user with role: " + userRole);

            if ("admin".equalsIgnoreCase(userRole)) {
                // Admin không phải là Employee, tạo object giả với username từ auth
                Employee adminEmployee = new Employee();
                adminEmployee.setId(SessionStorage.getCurrentUserId());
                adminEmployee.setUsername(SessionStorage.getCurrentUsername());
                adminEmployee.setRole("admin");
                adminEmployee.setFirstname("Admin");
                adminEmployee.setLastname(""); // Admin không có họ tên thật
                adminEmployee.setActive(true);

                System.out.println(
                        "✅ Login as ADMIN: " + adminEmployee.getUsername());
                System.out.println("   Admin user - not from employees table");

                String[] key = { "role", "accountData", "authToken" };
                Object[] data = { UserRole.ADMIN, adminEmployee, sessionId };
                SceneManager.switchSceneWithData(SceneConfig.ADMIN_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD, key,
                        data);

            } else if ("customer".equalsIgnoreCase(userRole)) {
                try {
                    Customer customer = CustomerRecordService.getInstance().searchCustomers(
                            String.valueOf(SessionStorage.getCurrentUserId()),
                            null,
                            null,
                            null).getData().get(0);

                    System.out.println("✅ Login as CUSTOMER: " + customer.getUsername());

                    String[] key = { "role", "accountData", "authToken" };
                    Object[] data = { UserRole.CUSTOMER, customer, sessionId };
                    SceneManager.switchSceneWithData(SceneConfig.CUSTOMER_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD,
                            key, data);
                } catch (Exception e) {
                    invalidLoginMessage.setVisible(true);
                    invalidLoginMessage.setText("Data loading error. Please try again later.");
                }
            } else if ("doctor".equalsIgnoreCase(userRole) || "nurse".equalsIgnoreCase(userRole)) {
                // Employee: Doctor or Nurse
                HttpEmployeeService employeeService = new HttpEmployeeService();
                Employee employee = employeeService.getEmployeeById(
                        SessionStorage.getCurrentUserId());

                // ⚠️ OVERRIDE role from database with role from auth service
                employee.setRole(userRole);

                System.out.println(
                        "✅ Login as " + userRole + ": " + employee.getFirstname() + " " + employee.getLastname());
                System.out.println("   Employee role OVERRIDDEN to: " + employee.getRole());

                String[] key = { "role", "accountData", "authToken" };
                Object[] data = { UserRole.EMPLOYEE, employee, sessionId };

                if (employee.getRole().equalsIgnoreCase("doctor")) {
                    SceneManager.switchSceneWithData(SceneConfig.DOCTOR_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD,
                            key, data);
                } else if (employee.getRole().equalsIgnoreCase("nurse")) {
                    SceneManager.switchSceneWithData(SceneConfig.NURSE_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD,
                            key, data);
                } else {
                    // Fallback nếu role không match
                    System.err.println("❌ Unknown employee role: " + employee.getRole());
                    invalidLoginMessage.setVisible(true);
                    invalidLoginMessage.setText("Invalid user role configuration. Please contact administrator.");
                }
            } else {
                // Unknown role
                System.err.println("❌ Unknown user role: " + userRole);
                invalidLoginMessage.setVisible(true);
                invalidLoginMessage.setText("Invalid user role. Please contact administrator.");
            }

        } else {
            invalidLoginMessage.setVisible(true);
            invalidLoginMessage.setText("Invalid username or password");
        }
    }
}
