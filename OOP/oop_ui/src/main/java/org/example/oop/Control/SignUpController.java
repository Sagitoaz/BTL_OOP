package org.example.oop.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {

    private static final Logger LOGGER = Logger.getLogger(SignUpController.class.getName());

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField dobTextField;

    @FXML
    private ComboBox<Customer.Gender> genderComboBox;

    @FXML
    private TextField addressTextField;

    @FXML
    private Label errorMessage;

    @FXML
    private Button signUpButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Button toggleConfirmPasswordButton;

    @FXML
    private TextField visibleConfirmPasswordTextField;

    // Trạng thái hiển thị mật khẩu
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @FXML
    public void initialize() {
        // Đồng bộ nội dung giữa PasswordField và TextField cho password
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isPasswordVisible) {
                visiblePasswordTextField.setText(newValue);
            }
        });

        visiblePasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isPasswordVisible) {
                passwordTextField.setText(newValue);
            }
        });

        // Đồng bộ cho confirm password
        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isConfirmPasswordVisible) {
                visibleConfirmPasswordTextField.setText(newValue);
            }
        });

        visibleConfirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isConfirmPasswordVisible) {
                confirmPasswordTextField.setText(newValue);
            }
        });

        // Initialize gender combo box
        genderComboBox.getItems().addAll(Customer.Gender.values());
    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            visiblePasswordTextField.setText(passwordTextField.getText());
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("🙈");
        } else {
            passwordTextField.setText(visiblePasswordTextField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
    }

    @FXML
    void toggleConfirmPasswordVisibility(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            visibleConfirmPasswordTextField.setText(confirmPasswordTextField.getText());
            visibleConfirmPasswordTextField.setVisible(true);
            visibleConfirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("🙈");
        } else {
            confirmPasswordTextField.setText(visibleConfirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            visibleConfirmPasswordTextField.setVisible(false);
            visibleConfirmPasswordTextField.setManaged(false);
            toggleConfirmPasswordButton.setText("👁");
        }
    }

    @FXML
    void signUpButtonOnClick(ActionEvent event) {
        // Implement signup logic here

        // For now, just show a message
        errorMessage.setText("Signup functionality not implemented yet.");
        errorMessage.setVisible(true);
    }

    @FXML
    void backToLoginButtonOnClick(ActionEvent event) {
        SceneManager.switchScene(SceneConfig.LOGIN_FXML, SceneConfig.LOGIN_FXML);
    }
}
