package org.example.oop.Model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
public class MailService {
    // Ten mail va pass App cua mail de gui
    private String username = "letribach1938@gmail.com";
    private String password = "ithqurzlbxeyuaik";
    private String subjectGmail = "Khôi phục mật khẩu";
    private String newPasswordIntro = "Mật khẩu của bạn là: ";
    Session session;


    public MailService(){
        Properties props = new Properties();
        // Cai dat cac thong so config
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        // getInstance tao ra 1 session dua theo thong tin duoc cung cap
        // props thong tin config
        // Authen chua thong tin username va password
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username,password);
            }
        });
    }
    public void SendText(String to, String new_pass) {
        MimeMessage message = new MimeMessage(session);
        System.out.println(new_pass);
        try{
            // Noi dung gui
            message.setText(newPasswordIntro + new_pass, "UTF-8");
            // Tieu de
            message.setSubject("Khôi phục mật khẩu", "UTF-8");
            // Gmail gui
            message.setFrom(new InternetAddress(username));
            // gmail nhan
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            //Gui tin
            Transport.send(message);

        }
        catch (MessagingException e){
            e.printStackTrace();

        }






    }

}
