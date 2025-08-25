package org.example.oop.Control;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ForgetPassword
{
    @FXML
    private TextField emailField;
    @FXML
    public void SendPassword(){
        String email = emailField.getText();
        System.out.println(email);
    }
}
