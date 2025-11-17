package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;

public class LoginValidator {
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 128;

    public static HttpResponse validateUsername(String username) {
        if(username == null || username.trim().isEmpty()){
            return ValidationUtils.error(400, "BAD REQUEST", "Tên đăng nhập không được để trống.");
        }
        if(username.length() > MAX_USERNAME_LENGTH){
            return ValidationUtils.error(400, "BAD REQUEST", "Tên đăng nhập không được vượt quá " + MAX_USERNAME_LENGTH + " ký tự.");
        }
        if(!username.matches("^[a-zA-Z0-9_@.]+$")){
            return ValidationUtils.error(400, "BAD REQUEST", "Tên đăng nhập chỉ được chứa chữ cái, số và ký tự _, @, .");
        }
        return null; // Valid
    }
    public static  HttpResponse validatePassword(String password) {
        if(password == null || password.trim().isEmpty()){
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu không được để trống.");
        }
        if(password.length() > MAX_PASSWORD_LENGTH){
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu không hợp lệ.");
        }
        if(!password.matches("^[\\S]+$")){
            return ValidationUtils.error(400, "BAD REQUEST", "Mật khẩu không được chứa khoảng trắng.");
        }
        return null; // Valid
    }
    public static HttpResponse validateLoginCredentials(String username, String password) {
        HttpResponse usernameError = validateUsername(username);
        if (usernameError != null) return usernameError;

        HttpResponse passwordError = validatePassword(password);
        if (passwordError != null) return passwordError;

        return null; // All validations passed
    }


    public static HttpResponse userNotFound() {
        return ValidationUtils.error(401, "UNAUTHORIZED", "Tên đăng nhập hoặc mật khẩu không đúng. Vui lòng thử lại.");
    }
    public static HttpResponse incorectPassword() {
        return ValidationUtils.error(401, "UNAUTHORIZED", "Tên đăng nhập hoặc mật khẩu không đúng. Vui lòng thử lại.");
    }
    public static HttpResponse accountInactive(){
        return ValidationUtils.error(401, "UNAUTHORIZED", "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
    }


}
