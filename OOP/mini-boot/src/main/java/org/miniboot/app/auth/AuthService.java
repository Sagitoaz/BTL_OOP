package org.miniboot.app.auth;

/**
 * AuthService: Xử lý logic xác thực người dùng
 * - Kiểm tra thông tin đăng nhập
 * - Tạo JWT token cho người dùng hợp lệ
 */
public class AuthService {
    
    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Xác thực người dùng và tạo token
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return JWT token nếu thành công
     * @throws Exception nếu thông tin không hợp lệ
     */
    public String authenticate(String username, String password) throws Exception {
        // Demo: Kiểm tra user/password cứng (trong thực tế sẽ query database)
        if ("admin".equals(username) && "123456".equals(password)) {
            // Tạo token với userId = username
            return jwtService.generateToken(username);
        }
        
        // Thêm các user demo khác
        if ("doctor1".equals(username) && "pass123".equals(password)) {
            return jwtService.generateToken(username);
        }
        
        throw new Exception("Thong tin dang nhap khong hop le");
    }

    /**
     * Xác thực token từ header Authorization
     * @param authHeader Header "Authorization: Bearer <token>"
     * @return userId nếu token hợp lệ
     * @throws Exception nếu token không hợp lệ
     */
    public String validateToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Thieu token xac thuc");
        }

        String token = authHeader.substring(7);
        String userId = jwtService.validateTokenAndGetUserId(token);

        if (userId == null) {
            throw new Exception("Token khong hop le hoac da het han");
        }

        return userId;
    }
}

