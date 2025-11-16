package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpResponse;

public class ChangePasswordValidator {
    final static int MIN_LENGTH = 8;
    final static int MAX_LENGTH = 128;

    public static HttpResponse validateNewPasswordFormat(String newPassword) {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ValidationUtils.error(400, "Bad Request", "Mật khẩu mới không được để trống.");
        }
        if (newPassword.length() < MIN_LENGTH) {
            return ValidationUtils.error(400, "Bad Request", "Mật khẩu mới phải có ít nhất " + MIN_LENGTH + " ký tự.");
        }
        if (newPassword.length() > MAX_LENGTH) {
            return ValidationUtils.error(400, "Bad Request", "Mật khẩu mới không được vượt quá " + MAX_LENGTH + " ký tự.");
        }
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")) {
            return ValidationUtils.error(400, "Bad Request", "Mật khẩu mới phải bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }
        return null; // Valid
    }
    public static HttpResponse validateConfirmPasswordMatch(String newPassword, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return ValidationUtils.error(400, "Bad Request", "Xác nhận mật khẩu không được để trống.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ValidationUtils.error(400, "Bad Request", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }
        return null; // Match
    }
    public static HttpResponse incorrectOldPassword(String oldPassword) {
        return ValidationUtils.error(400, "Bad Request", "Mật khẩu cũ không đúng.");
    }
    public static  HttpResponse unexpectedError() {
        return ValidationUtils.error(500, "Internal Server Error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
    }



}
