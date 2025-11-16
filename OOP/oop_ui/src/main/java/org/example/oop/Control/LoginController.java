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

        // 🔐 Reset form state AFTER all FXML components are injected
        // Use Platform.runLater to ensure this runs after scene is fully loaded
        javafx.application.Platform.runLater(() -> resetLoginForm());
    }

    /**
     * Reset login form to initial state
     * Called when returning to login page after logout
     */
    private void resetLoginForm() {
        try {
            // Clear input fields (null-safe)
            if (usernameTextField != null) {
                usernameTextField.clear();
            }
            if (enterPasswordTextField != null) {
                enterPasswordTextField.clear();
            }
            if (visiblePasswordTextField != null) {
                visiblePasswordTextField.clear();
            }

            // Enable login button
            if (loginButton != null) {
                loginButton.setDisable(false);
            }

            // Clear error message
            if (invalidLoginMessage != null) {
                invalidLoginMessage.setText("");
                invalidLoginMessage.setVisible(false);
            }

            // Reset password visibility
            isPasswordVisible = false;
            if (enterPasswordTextField != null) {
                enterPasswordTextField.setVisible(true);
                enterPasswordTextField.setManaged(true);
            }
            if (visiblePasswordTextField != null) {
                visiblePasswordTextField.setVisible(false);
                visiblePasswordTextField.setManaged(false);
            }
            if (togglePasswordButton != null) {
                togglePasswordButton.setText("👁"); // Icon mắt mở
            }

            System.out.println("✅ Login form reset complete");
        } catch (Exception e) {
            System.err.println("⚠️ Error resetting login form: " + e.getMessage());
            e.printStackTrace();
        }
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

        // 🔐 Clear any old session data before login
        SessionStorage.clear();
        SceneManager.removeSceneData("authToken");
        SceneManager.removeSceneData("accountData");
        SceneManager.removeSceneData("role");

        // validate input
        String msg = validateInput(username, password);
        if (msg != null) {
            invalidLoginMessage.setText(msg);
            return;
        }

        // Disable login button và hiển thị "Đang đăng nhập..."
        loginButton.setDisable(true);
        invalidLoginMessage.setText("⏳ Đang đăng nhập...");

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
                    loginButton.setDisable(false);
                }
            } else if ("EMPLOYEE".equalsIgnoreCase(userRole)) {
                // ✅ EMPLOYEE role từ AuthService - cần query employees table để lấy role cụ thể
                // Load async để không block UI
                Thread loadEmployeeThread = new Thread(() -> {
                    try {
                        System.out.println("🔍 EMPLOYEE role detected, querying employee table for specific role...");

                        // ⚡ Tăng timeout lên 30 giây cho API call này
                        HttpEmployeeService employeeService = new HttpEmployeeService();
                        Employee employee = employeeService.getEmployeeById(
                                SessionStorage.getCurrentUserId());

                        if (employee == null) {
                            System.err.println("❌ Employee not found in database");
                            javafx.application.Platform.runLater(() -> {
                                invalidLoginMessage.setVisible(true);
                                invalidLoginMessage
                                        .setText("Employee information not found. Please contact administrator.");
                                loginButton.setDisable(false);
                            });
                            return;
                        }

                        String actualRole = employee.getRole(); // Get role từ database
                        System.out.println("✅ Found employee with actual role: " + actualRole);
                        System.out.println("   Employee: " + employee.getFirstname() + " " + employee.getLastname());

                        String[] key = { "role", "accountData", "authToken" };
                        Object[] data = { UserRole.EMPLOYEE, employee, sessionId };

                        // Update UI trên main thread
                        javafx.application.Platform.runLater(() -> {
                            // Navigate theo role cụ thể
                            if ("doctor".equalsIgnoreCase(actualRole)) {
                                System.out.println("🔄 Redirecting to Doctor Dashboard");
                                SceneManager.switchSceneWithData(SceneConfig.DOCTOR_DASHBOARD_FXML,
                                        SceneConfig.Titles.DASHBOARD, key, data);
                            } else if ("nurse".equalsIgnoreCase(actualRole)) {
                                System.out.println("🔄 Redirecting to Nurse Dashboard");
                                SceneManager.switchSceneWithData(SceneConfig.NURSE_DASHBOARD_FXML,
                                        SceneConfig.Titles.DASHBOARD, key, data);
                            } else {
                                System.err.println("❌ Unknown employee role: " + actualRole);
                                invalidLoginMessage.setVisible(true);
                                invalidLoginMessage.setText("Unknown employee role: " + actualRole);
                                loginButton.setDisable(false);
                            }
                        });

                    } catch (java.net.http.HttpTimeoutException timeoutEx) {
                        System.err.println("❌ Network timeout - Backend server is slow");
                        timeoutEx.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            invalidLoginMessage.setVisible(true);
                            invalidLoginMessage.setText(
                                    "⚠️ Server is slow. Login successful but dashboard loading failed. Please try again.");
                            loginButton.setDisable(false);
                        });
                    } catch (Exception e) {
                        System.err.println("❌ Error querying employee data: " + e.getMessage());
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            invalidLoginMessage.setVisible(true);
                            invalidLoginMessage.setText("Error loading employee information. Please try again later.");
                            loginButton.setDisable(false);
                        });
                    }
                });

                loadEmployeeThread.setName("EmployeeDataLoader");
                loadEmployeeThread.setDaemon(true);
                loadEmployeeThread.start();

            } else if ("doctor".equalsIgnoreCase(userRole) || "nurse".equalsIgnoreCase(userRole)) {
                // Fallback: nếu AuthService trả về role cụ thể (doctor/nurse)
                Thread loadEmployeeThread = new Thread(() -> {
                    try {
                        HttpEmployeeService employeeService = new HttpEmployeeService();
                        Employee employee = employeeService.getEmployeeById(
                                SessionStorage.getCurrentUserId());

                        if (employee == null) {
                            System.err.println("❌ Employee not found");
                            javafx.application.Platform.runLater(() -> {
                                invalidLoginMessage.setVisible(true);
                                invalidLoginMessage.setText("Employee information not found.");
                                loginButton.setDisable(false);
                            });
                            return;
                        }

                        System.out.println(
                                "✅ Login as " + userRole + ": " + employee.getFirstname() + " "
                                        + employee.getLastname());

                        String[] key = { "role", "accountData", "authToken" };
                        Object[] data = { UserRole.EMPLOYEE, employee, sessionId };

                        javafx.application.Platform.runLater(() -> {
                            if ("doctor".equalsIgnoreCase(userRole)) {
                                SceneManager.switchSceneWithData(SceneConfig.DOCTOR_DASHBOARD_FXML,
                                        SceneConfig.Titles.DASHBOARD, key, data);
                            } else if ("nurse".equalsIgnoreCase(userRole)) {
                                SceneManager.switchSceneWithData(SceneConfig.NURSE_DASHBOARD_FXML,
                                        SceneConfig.Titles.DASHBOARD, key, data);
                            }
                        });

                    } catch (java.net.http.HttpTimeoutException timeoutEx) {
                        System.err.println("❌ Network timeout - Backend server is slow");
                        javafx.application.Platform.runLater(() -> {
                            invalidLoginMessage.setVisible(true);
                            invalidLoginMessage.setText("⚠️ Server is slow. Please try again.");
                            loginButton.setDisable(false);
                        });
                    } catch (Exception e) {
                        System.err.println("❌ Error during employee login: " + e.getMessage());
                        javafx.application.Platform.runLater(() -> {
                            invalidLoginMessage.setVisible(true);
                            invalidLoginMessage.setText("Error loading employee data. Please try again later.");
                            loginButton.setDisable(false);
                        });
                    }
                });

                loadEmployeeThread.setName("EmployeeDataLoader");
                loadEmployeeThread.setDaemon(true);
                loadEmployeeThread.start();

            } else {
                // Unknown role
                System.err.println("❌ Unknown user role: " + userRole);
                invalidLoginMessage.setVisible(true);
                invalidLoginMessage.setText("Invalid user role: " + userRole + ". Please contact administrator.");
                loginButton.setDisable(false);
            }

        } else {
            invalidLoginMessage.setVisible(true);
            invalidLoginMessage.setText("Invalid username or password");
            loginButton.setDisable(false);
        }
    }
}
