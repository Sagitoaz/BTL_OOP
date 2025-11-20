package org.miniboot.app.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.miniboot.app.config.AuthConstants;

import java.util.Date;

/**
 * JwtService: Dịch vụ xử lý JWT (JSON Web Token)
 * - Tạo token từ userId
 * - Xác thực token và lấy userId
 * - Kiểm tra token hết hạn
 */
public class JwtService {

    // Sử dụng constants từ AuthConstants
    private static final Algorithm algorithm = Algorithm.HMAC256(AuthConstants.JWT_SECRET_KEY);

    /**
     * Tạo JWT từ userId
     * Bao gồm cả claim "username" để test có thể verify
     */
    public static String generateToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withIssuer(AuthConstants.JWT_ISSUER)
                .withAudience(AuthConstants.JWT_AUDIENCE)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + AuthConstants.JWT_EXPIRATION_TIME))
                .sign(algorithm);
    }

    /**
     * Xác thực token và lấy userId
     * 
     * @return userId nếu hợp lệ, null nếu không hợp lệ
     */
    public static String validateTokenAndGetUserId(String token) {
        try {
            System.out.println("🔍 [JwtService] Validating token...");
            System.out.println("🔍 [JwtService] Expected issuer: " + AuthConstants.JWT_ISSUER);
            System.out.println("🔍 [JwtService] Expected audience: " + AuthConstants.JWT_AUDIENCE);
            
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer(AuthConstants.JWT_ISSUER)
                    .withAudience(AuthConstants.JWT_AUDIENCE)
                    .build()
                    .verify(token);
            
            String userId = jwt.getSubject();
            System.out.println("✅ [JwtService] Token valid! UserId: " + userId);
            return userId;
        } catch (JWTVerificationException e) {
            System.out.println("❌ [JwtService] Validation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     */
    public static boolean isTokenExpired(String token) {
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
