package org.miniboot.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

// Lớp JwtService này giúp xử lý việc tạo và xác thực JWT (JSON Web Token).
public class JwtService {

    // Khóa bí mật (secret key) dùng để ký JWT. Đây là thông tin quan trọng, phải được giữ bí mật.
    // Chú ý: Khóa này phải dài ít nhất 256 bit cho thuật toán HMAC256.
    private static final String SECRET_KEY = "miniboot-secret-key-must-be-at-least-256-bits-long-for-security";

    // Thời gian hết hạn của token, ở đây là 24 giờ (86400000 milliseconds).
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    // Thuật toán sử dụng để ký token, ở đây là HMAC256 sử dụng SECRET_KEY.
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    /**
     * Phương thức này tạo ra một JWT từ userId.
     * @param userId: ID của người dùng, đây là thông tin chính được lưu trong token.
     * @return: Trả về một JWT đã được ký.
     */
    public String generateToken(String userId) {
        return JWT.create()
                // Chỉ định userId là subject của token (chứa thông tin chính về người dùng)
                .withSubject(userId)

                // Thời gian tạo token (issued at)
                .withIssuedAt(new Date())

                // Thời gian hết hạn của token, tính từ thời điểm hiện tại
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))

                // Ký token với thuật toán HMAC256 và SECRET_KEY
                .sign(algorithm);
    }

    /**
     * Phương thức này xác thực token và lấy ra userId (subject) từ token.
     * @param token: JWT cần xác thực.
     * @return: Nếu token hợp lệ, trả về userId, nếu không trả về null.
     */
    public String validateTokenAndGetUserId(String token) {
        try {
            // Kiểm tra tính hợp lệ của token và giải mã thông tin từ token
            DecodedJWT jwt = JWT.require(algorithm)
                    .build()
                    .verify(token);  // Nếu token hợp lệ, trả về đối tượng DecodedJWT

            // Trả về userId (subject) từ token
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            // Nếu có lỗi khi xác thực (ví dụ: token không hợp lệ, hết hạn), trả về null
            return null;
        }
    }

    /**
     * Phương thức này kiểm tra xem token có hết hạn hay không.
     * @param token: JWT cần kiểm tra.
     * @return: true nếu token hết hạn, false nếu token vẫn còn hiệu lực.
     */
    public boolean isTokenExpired(String token) {
        try {
            // Kiểm tra tính hợp lệ của token và giải mã thông tin từ token
            DecodedJWT jwt = JWT.require(algorithm)
                    .build()
                    .verify(token);  // Nếu token hợp lệ, trả về đối tượng DecodedJWT

            // Kiểm tra xem thời gian hết hạn của token có trước thời gian hiện tại không
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            // Nếu có lỗi khi xác thực (ví dụ: token không hợp lệ), coi như token đã hết hạn
            return true;
        }
    }
}
