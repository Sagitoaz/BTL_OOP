package org.miniboot.app.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * JwtService: Dịch vụ xử lý JWT (JSON Web Token)
 * - Tạo token từ userId
 * - Xác thực token và lấy userId
 * - Kiểm tra token hết hạn
 */
public class JwtService {

    // Khóa bí mật để ký JWT (phải >= 256 bits cho HMAC256)
    private static final String SECRET_KEY = "miniboot-secret-key-must-be-at-least-256-bits-long-for-security";

    // Thời gian hết hạn: 24 giờ
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    // Thuật toán HMAC256
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    /**
     * Tạo JWT từ userId
     * Bao gồm cả claim "username" để test có thể verify
     */
    public String generateToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("username", userId)  // Thêm claim username
                .withClaim("userId", userId)    // Thêm claim userId (explicit)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    /**
     * Xác thực token và lấy userId
     * @return userId nếu hợp lệ, null nếu không hợp lệ
     */
    public String validateTokenAndGetUserId(String token) {
        try {
            DecodedJWT jwt = JWT.require(algorithm)
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.require(algorithm)
                    .build()
                    .verify(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }
}
