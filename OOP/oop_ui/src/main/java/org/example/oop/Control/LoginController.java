package org.example.oop.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.UserRole;

import java.io.IOException;
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
        if (user.isEmpty() && pass.isEmpty()) return "Enter username and password";
        if (user.isEmpty()) return "Enter username";
        if (pass.isEmpty()) return "Enter password";
        return null;
    }

    @FXML
    void LoginButtonOnClick(ActionEvent event) {
        String username = usernameTextField.getText().trim();
        // Lấy password từ field đang hiển thị
        String password = isPasswordVisible ?
                         visiblePasswordTextField.getText().trim() :
                         enterPasswordTextField.getText().trim();

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
            System.out.println("Login successful" + SessionStorage.getCurrentUsername() + " " + SessionStorage.getCurrentUserRole());
            // Clear error message
            invalidLoginMessage.setText("");
            // Redirect to dashboard
            if (SessionStorage.getCurrentUserRole().equalsIgnoreCase("ADMIN"))
                SceneManager.switchScene(SceneConfig.ADMIN_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD);
            else if (SessionStorage.getCurrentUserRole().equalsIgnoreCase("CUSTOMER"))
            {
                try{
                    Customer customer = CustomerRecordService.getInstance().searchCustomers(
                            String.valueOf(SessionStorage.getCurrentUserId()),
                            null,
                            null,
                            null
                    ).getData().get(0);
                    String[] key = {"role", "accountData"};
                    Object[] data = {UserRole.CUSTOMER, customer};
                    SceneManager.switchSceneWithData(SceneConfig.CUSTOMER_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD, key, data);
                } catch (Exception e) {
                    invalidLoginMessage.setVisible(true);
                    invalidLoginMessage.setText("Data loading error. Please try again later.");
                }
            }
            else{
                SceneManager.switchScene(SceneConfig.DOCTOR_DASHBOARD_FXML, SceneConfig.Titles.DASHBOARD);
            }

        } else {
            invalidLoginMessage.setVisible(true);
            invalidLoginMessage.setText("Invalid username or password");
        }
    }
}
