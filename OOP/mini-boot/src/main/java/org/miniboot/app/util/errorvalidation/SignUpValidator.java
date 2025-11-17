package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.config.ErrorMessages;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.util.Json;

public class SignUpValidator {

    public static HttpResponse validateRequiredFields(String username, String firstname, String lastname, String password, String email, String phone) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", ErrorMessages.ERROR_MISSING_USERNAME);
        }
        if (!username.matches("^[a-zA-Z0-9_]{3,50}$")) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Username phải có 3-50 ký tự và chỉ chứa chữ, số, hoặc dấu gạch dưới.");
        }
        if (password == null || password.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", ErrorMessages.ERROR_MISSING_PASSWORD);
        }
        if (email == null || email.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "Email không được để trống.");
        }
        if (firstname == null || firstname.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "Họ không được để trống.");
        }
        if (lastname == null || lastname.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST", "Tên không được để trống.");
        }
        // 7. Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ValidationUtils.error(400, "BAD_REQUEST", ErrorMessages.ERROR_INVALID_EMAIL);
        }

        // 8. Validate phone (optional but if provided, must be valid)
        if (phone != null && !phone.isEmpty() && !phone.matches("^[0-9]{10,11}$")) {
            return ValidationUtils.error(400, "BAD_REQUEST", ErrorMessages.ERROR_INVALID_PHONE);
        }
        return null; // Valid
    }
    public static HttpResponse existingUsername() {
        return ValidationUtils.error(HttpConstants.STATUS_CONFLICT, "CONFLICT", ErrorMessages.ERROR_USER_ALREADY_EXISTS);
    }
    public static HttpResponse existingEmail() {
        return ValidationUtils.error(HttpConstants.STATUS_CONFLICT, "CONFLICT", "Email đã được sử dụng bởi tài khoản khác.");
    }
    public static HttpResponse errorCreatingUser() {
        return ValidationUtils.error(HttpConstants.STATUS_INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Đã xảy ra lỗi khi tạo tài khoản. Vui lòng thử lại sau.");
    }


}
