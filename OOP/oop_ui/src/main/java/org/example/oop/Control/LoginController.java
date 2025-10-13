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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField enterPasswordTextField;

    @FXML
    private Hyperlink forgotPassword;

    @FXML
    private Label invalidLoginMessage;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpbutton;

    @FXML
    private TextField usernameTextField;

    @FXML
    void ForgotPasswordHyperLinkOnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/hello-view.fxml"));
            Parent root = loader.load();
            Stage state = (Stage) ((Node) event.getSource()).getScene().getWindow();
            state.setScene(new Scene(root));
            state.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToSignUpButtonOnClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Signup.fxml"));
            Parent root = loader.load();
            Stage state = (Stage) ((Node) event.getSource()).getScene().getWindow();
            state.setScene(new Scene(root));
            state.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String password = enterPasswordTextField.getText().trim();

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

            // Clear error message
            invalidLoginMessage.setText("");

            // Redirect to dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/hello-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                invalidLoginMessage.setText("Error loading dashboard");
            }
        } else {
            invalidLoginMessage.setText("Invalid username or password");
        }
    }
}
