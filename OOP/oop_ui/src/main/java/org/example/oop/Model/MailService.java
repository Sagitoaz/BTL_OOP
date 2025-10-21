package org.example.oop.Model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

public class MailService {
    // Thông tin email gửi
    private String username = "letribach1938@gmail.com";
    private String password = "ithqurzlbxeyuaik";
    Session session;

    public MailService(){
        Properties props = new Properties();
        // Cài đặt các thông số config
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo session với authentication
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Gửi mã xác nhận reset password qua email
     */
    public void SendText(String to, String token) {
        MimeMessage message = new MimeMessage(session);

        try {
            // Nội dung email cho reset password
            String emailContent = "Xin chào,\n\n" +
                                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại Hệ thống Phòng khám Mắt.\n\n" +
                                "MÃ XÁC NHẬN CỦA BẠN LÀ: " + token + "\n\n" +
                                "Vui lòng nhập mã này vào form đặt lại mật khẩu.\n" +
                                "Mã này có hiệu lực trong 15 phút.\n\n" +
                                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                                "Trân trọng,\n" +
                                "Hệ thống Phòng khám Mắt";

            // Thiết lập nội dung
            message.setText(emailContent, "UTF-8");

            // Tiêu đề
            message.setSubject("Mã xác nhận đặt lại mật khẩu", "UTF-8");

            // Gmail gửi
            message.setFrom(new InternetAddress(username));

            // Gmail nhận
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Gửi email
            Transport.send(message);

            System.out.println("✓ Email đã được gửi thành công đến: " + to);
            System.out.println("✓ Mã xác nhận: " + token);

        } catch (MessagingException e) {
            System.err.println("✗ Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email", e);
        }
    }
}
