package org.miniboot.auth;

// Lớp này kiểm tra và xác thực token JWT trong header của yêu cầu (request) HTTP.
public class AuthMiddleware {

    // Đối tượng `JwtService` dùng để xác thực token và lấy thông tin người dùng từ token
    private final JwtService jwtService;

    // Constructor của AuthMiddleware nhận đối tượng JwtService để thực hiện các thao tác với JWT
    public AuthMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Phương thức này kiểm tra xem yêu cầu HTTP có chứa token hợp lệ không
    public String validateRequest(String authHeader) throws Exception {

        // Kiểm tra xem header có chứa token hay không và có đúng dạng "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Thieu token xac thuc");  // Nếu không có token, ném lỗi
        }

        // Lấy token ra bằng cách cắt bỏ phần "Bearer " ở đầu header
        String token = authHeader.substring(7); // Từ vị trí index 7 trở đi là token

        // Kiểm tra tính hợp lệ của token và lấy thông tin userId từ token đó
        String userId = jwtService.validateTokenAndGetUserId(token);

        // Nếu không có userId, có thể là token đã hết hạn hoặc không hợp lệ
        if (userId == null) {
            throw new Exception("Token khong hop le hoac da het han");  // Ném lỗi nếu token không hợp lệ
        }

        // Nếu token hợp lệ, trả về userId để biết ai đang đăng nhập
        return userId; // Trả về userId từ token
    }

    // Lớp ngoại lệ tùy chỉnh (custom exception) để báo lỗi khi người dùng không được phép truy cập
    public static class UnauthorizedException extends Exception {
        // Constructor nhận thông điệp lỗi
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
