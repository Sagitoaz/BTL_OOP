package org.miniboot.controller;

import org.miniboot.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

// Lớp này xử lý các yêu cầu liên quan đến đăng nhập, xác thực và tạo token.
public class AuthController {

    // Đối tượng JwtService dùng để tạo và xác thực token JWT.
    private final JwtService jwtService;

    // ObjectMapper từ thư viện Jackson để chuyển đổi giữa đối tượng Java và JSON.
    private final ObjectMapper objectMapper;

    // Constructor của AuthController, khởi tạo JwtService và ObjectMapper
    public AuthController() {
        this.jwtService = new JwtService(); // Khởi tạo JwtService để làm việc với JWT
        this.objectMapper = new ObjectMapper(); // Khởi tạo ObjectMapper để làm việc với JSON
    }

    /**
     * Phương thức xử lý yêu cầu đăng nhập.
     * @param requestBody: JSON body từ client chứa thông tin đăng nhập (username, password)
     * @return: JSON response với token nếu đăng nhập thành công hoặc lỗi nếu thất bại.
     */
    public String login(String requestBody) {
        try {
            // Parse JSON body từ client thành Map để lấy thông tin đăng nhập
            @SuppressWarnings("unchecked")
            Map<String, String> loginData = objectMapper.readValue(requestBody, Map.class);

            // Lấy thông tin username và password từ Map
            String username = loginData.get("username");
            String password = loginData.get("password");

            // Kiểm tra thông tin đăng nhập (user giả, sau này sẽ thay bằng database)
            if (isValidUser(username, password)) {
                // Nếu thông tin đăng nhập hợp lệ, tạo JWT token
                String token = jwtService.generateToken(username);

                // Tạo một Map để lưu trữ response, chứa token, userId và thông báo thành công
                Map<String, Object> response = new HashMap<>();
                response.put("token", token); // Thêm token vào response
                response.put("userId", username); // Thêm userId vào response
                response.put("message", "Đăng nhập thành công"); // Thêm thông báo thành công

                // Trả về response dưới dạng JSON
                return objectMapper.writeValueAsString(response);
            } else {
                // Nếu thông tin đăng nhập không hợp lệ, trả về lỗi
                Map<String, String> error = new HashMap<>();
                error.put("error", "Sai tên đăng nhập hoặc mật khẩu");
                return objectMapper.writeValueAsString(error); // Trả về JSON lỗi
            }
        } catch (Exception e) {
            // Nếu có lỗi trong quá trình xử lý (parse JSON, kiểm tra đăng nhập), trả về lỗi chung
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad request: " + e.getMessage());
            try {
                return objectMapper.writeValueAsString(error); // Trả về lỗi dưới dạng JSON
            } catch (Exception ex) {
                // Nếu có lỗi trong khi xử lý lỗi, trả về thông báo lỗi server
                return "{\"error\":\"Internal server error\"}";
            }
        }
    }

    /**
     * Kiểm tra xem tên đăng nhập và mật khẩu có hợp lệ không.
     * Đây là một hàm giả lập, sau này sẽ thay thế bằng việc kiểm tra với cơ sở dữ liệu.
     * @param username: Tên người dùng.
     * @param password: Mật khẩu của người dùng.
     * @return: true nếu tên đăng nhập và mật khẩu hợp lệ, false nếu không hợp lệ.
     */
    private boolean isValidUser(String username, String password) {
        // Kiểm tra các giá trị đăng nhập giả (sau này thay bằng database)
        return ("admin".equals(username) && "admin123".equals(password)) ||
                ("doctor".equals(username) && "doctor123".equals(password)) ||
                ("nurse".equals(username) && "nurse123".equals(password));
    }
}
