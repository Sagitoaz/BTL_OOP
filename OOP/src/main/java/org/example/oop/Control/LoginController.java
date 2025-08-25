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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/hello-view.fxml"));
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

    private String checkLogin(String username, String password) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/User/user.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] path = line.split("\\|");
                int id = Integer.parseInt(path[0]);
                String fileUsername = path[1];
                String filePassword = path[2];
                String fileRole = path[3];
                if (username.equals(fileUsername) && password.equals(filePassword)) {
                    return String.format("%d %s", id, fileRole);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // check login
        String idAndRole = checkLogin(username, password);
        if (idAndRole == null) {
            invalidLoginMessage.setText("Wrong username or password");
            return;
        }

        // tách ID và Role
        String[] parts = idAndRole.split(" ");
        int id = Integer.parseInt(parts[0]);
        String role = parts[1];

        // ánh xạ role -> FXML file
        Map<String, String> roleToFXML = Map.of(
                "admin", "/FXML/hello-view.fxml",
                "patient", "/FXML/hello-view.fxml",
                "staff", "/FXML/hello-view.fxml",
                "doctor", "/FXML/hello-view.fxml"
        );

        String fxmlPath = roleToFXML.get(role);
        if (fxmlPath == null) {
            invalidLoginMessage.setText("Unknown role: " + role);
            return;
        }

        // load scene mới
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // nếu cần, có thể truyền dữ liệu user cho controller mới
            // ExampleController controller = loader.getController();
            // controller.setUserId(id);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            invalidLoginMessage.setText("⚠️ Error loading UI for role: " + role);
        }
    }
}





