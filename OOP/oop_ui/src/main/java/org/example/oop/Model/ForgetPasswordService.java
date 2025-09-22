package org.example.oop.Model;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.File;
import java.security.SecureRandom;
import java.util.Scanner;


public class ForgetPasswordService
{
    private MailService mail = new MailService();
    @FXML
    private TextField emailField;
    @FXML

    public boolean CheckEmailExisted(String email){
        try{
            Scanner sc = new Scanner(new File("DataBase.txt"));
            while(sc.hasNextLine()){
                String nextln = sc.nextLine();
                if(nextln.equals(email)){
                    return true;
                }
            }
            return false;
        }
        catch(Exception e){
            return false;
        }


    }
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
        if(CheckEmailExisted(email)){
            mail.SendText(email, CreateNewPassword());
        }
        else{
            emailField.setText("NOT FOUND");
        }
    }
}
