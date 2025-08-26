package org.example.oop.Control;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.security.SecureRandom;


public class ForgetPassword
{
    private MailService mail = new MailService();
    @FXML
    private TextField emailField;


    private String CreateNewPassword(){
        SecureRandom random = new SecureRandom();
        //Random mat khau co 8 chu so
        int randomNumb = random.nextInt(99999999) + + 10000000;
        StringBuilder newPassword = new StringBuilder();
        newPassword.append(randomNumb);
        return newPassword.toString();


    }
    @FXML
    public void SendPassword(){
        String email = emailField.getText();
        System.out.println(email);
        mail.SendText(email, CreateNewPassword());
    }
}
