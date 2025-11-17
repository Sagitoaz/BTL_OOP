package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpResponse;

public class ChangePasswordValidator {
    final static int MIN_LENGTH = 8;
    final static int MAX_LENGTH = 128;

    public static HttpResponse validateNewPasswordFormat(String newPassword) {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu mới không được để trống.");
        }
        if (newPassword.length() < MIN_LENGTH) {
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu mới phải có ít nhất " + MIN_LENGTH + " ký tự.");
        }
        if (newPassword.length() > MAX_LENGTH) {
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu mới không được vượt quá " + MAX_LENGTH + " ký tự.");
        }
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")) {
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu mới phải bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }
        return null; // Valid
    }
    public static HttpResponse validateConfirmPasswordMatch(String newPassword, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD REQUEST", "Xác nhận mật khẩu không được để trống.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }
        return null; // Match
    }
    public static HttpResponse incorrectOldPassword() {
        return ValidationUtils.error(401, "UNAUTHORIZED", "Mật khẩu cũ không đúng.");
    }
    public static HttpResponse updateFailed() {
        return ValidationUtils.error(409, "CONFLICT", "Cập nhật mật khẩu thất bại. Vui lòng thử lại.");
    }
    public static  HttpResponse unexpectedError() {
        return ValidationUtils.error(500, "INTERNAL SERVER ERROR", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
    }
    public static HttpResponse invalidToken() {
        return ValidationUtils.error(401, "UNAUTHORIZED", "Mã xác nhận không hợp lệ hoặc đã hết hạn.");
    }



}
