package org.miniboot.app.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * PasswordService
 *
 * Lớp này cung cấp các tiện ích liên quan đến mật khẩu:
 * - băm mật khẩu (hash) với salt
 * - tạo salt ngẫu nhiên
 * - kiểm tra độ mạnh mật khẩu
 * - tạo mật khẩu ngẫu nhiên
 *
 * Mục tiêu: giữ code đơn giản, dễ đọc, và an toàn ở mức hợp lý cho mục đích học/ứng dụng nhỏ.
 */
public class PasswordService {
    // Độ dài (bytes) của salt được tạo ngẫu nhiên. Salt càng dài càng an toàn.
    private static final int SALT_LENGTH = 16;

    // Biểu thức chính quy để kiểm tra mật khẩu "mạnh" tối thiểu.
    // Yêu cầu: ít nhất 8 ký tự, có chữ số, chữ thường, chữ hoa, ký tự đặc biệt và không có khoảng trắng.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    /**
     * Băm mật khẩu bằng SHA-256 cùng với salt.
     *
     * Giải thích ngắn:
     * - Salt là chuỗi ngẫu nhiên thêm vào trước khi băm để chống lại rainbow tables.
     * - Sử dụng SHA-256 (không phải thuật toán chuyên dụng như bcrypt/argon2) — đủ cho ví dụ,
     *   nhưng nếu là hệ thống thực tế nên dùng bcrypt/argon2 với work factor.
     * - Kết quả được mã hóa Base64 để dễ lưu trữ dưới dạng chuỗi.
     *
     * @param password mật khẩu thuần (plaintext)
     * @param salt salt (đã ở dạng chuỗi Base64) được sử dụng để băm
     * @return chuỗi Base64 của giá trị băm
     */
    public static String hashPassword(String password, String salt) {
        try {
            // Tạo MessageDigest cho SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Cập nhật salt (byte) vào digest trước khi băm password
            md.update(Base64.getDecoder().decode(salt));
            // Thực hiện băm password
            byte[] hashedPassword = md.digest(password.getBytes());
            // Trả về kết quả đã mã hóa Base64 (dễ lưu vào file/DB)
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            // Trường hợp không tìm thấy SHA-256 (rất hiếm), ném RuntimeException để phát hiện sớm.
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Tạo salt ngẫu nhiên (Base64 string) để sử dụng khi băm.
     *
     * Lưu ý: Salt nên được tạo bằng SecureRandom để đảm bảo tính ngẫu nhiên.
     *
     * @return chuỗi salt đã mã hóa Base64
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt); // điền mảng bằng byte ngẫu nhiên
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Băm mật khẩu và trả về kết quả kèm salt để dễ lưu trữ.
     * Định dạng trả về: salt:hashedValue (cả hai là Base64 strings)
     *
     * Ví dụ lưu vào DB: "<saltBase64>:<hashBase64>".
     * Khi kiểm tra mật khẩu, tách salt ra và băm mật khẩu nhập vào với salt đó.
     *
     * @param password mật khẩu thuần cần băm
     * @return chuỗi có dạng "salt:hash"
     */
    public static String hashPasswordWithSalt(String password) {
        String salt = generateSalt();
        String hashed = hashPassword(password, salt);
        return salt + ":" + hashed;
    }

    /**
     * Xác thực mật khẩu: so sánh mật khẩu đầu vào với chuỗi đã lưu (salt:hash).
     *
     * Quy trình:
     * 1. Tách storedHash theo dấu ':' thành salt và hash.
     * 2. Băm mật khẩu nhập vào với salt đó.
     * 3. So sánh hash thu được và hash đã lưu (so sánh chuỗi an toàn hơn sẽ tốt hơn ở hệ thống thực tế).
     *
     * @param password mật khẩu nhập vào (plaintext)
     * @param storedHash chuỗi đã lưu ở định dạng salt:hash
     * @return true nếu khớp, false nếu không khớp hoặc định dạng lưu không hợp lệ
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            String salt = parts[0];
            String hash = parts[1];
            String hashedInput = hashPassword(password, salt);
            return hash.equals(hashedInput);
        } catch (Exception e) {
            // Nếu có lỗi (ví dụ storedHash null), trả về false
            return false;
        }
    }

    /**
     * Kiểm tra mật khẩu có đạt chuẩn "mạnh" tối thiểu hay không (theo PASSWORD_PATTERN).
     *
     * @param password mật khẩu cần kiểm tra
     * @return true nếu mật khẩu mạnh theo quy tắc, false nếu không
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.isEmpty()) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Tính điểm độ mạnh mật khẩu (0-5) dựa trên các tiêu chí đơn giản:
     * - độ dài >=8, >=12
     * - có chữ thường, chữ hoa, chữ số, ký tự đặc biệt
     *
     * Mục đích: hỗ trợ UI hiển thị thanh đánh giá mật khẩu. Không thay thế việc kiểm tra bảo mật.
     *
     * @param password mật khẩu cần đánh giá
     * @return điểm từ 0 đến 5 (5 là mạnh nhất)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[@#$%^&+=!].*")) score++;

        // Giới hạn tối đa 5 điểm
        return Math.min(score, 5);
    }

    /**
     * Sinh mật khẩu ngẫu nhiên mạnh với độ dài chỉ định.
     * Logic:
     * - Đảm bảo có ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt.
     * - Điền các ký tự còn lại từ tập ký tự cho phép.
     * - Xáo trộn vị trí để tránh pattern cố định.
     *
     * Lưu ý: nếu length < 4 thì vẫn sẽ sinh ra nhưng có thể không chứa đủ các loại ký tự bắt buộc.
     *
     * @param length độ dài mật khẩu mong muốn
     * @return mật khẩu ngẫu nhiên
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&+=!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Đảm bảo mỗi loại ký tự có mặt ít nhất một lần
        password.append((char) ('A' + random.nextInt(26))); // chữ hoa
        password.append((char) ('a' + random.nextInt(26))); // chữ thường
        password.append((char) ('0' + random.nextInt(10))); // chữ số
        password.append("@#$%^&+=!".charAt(random.nextInt(9))); // ký tự đặc biệt

        // Thêm các ký tự ngẫu nhiên còn lại
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Xáo trộn để đảo vị trí các ký tự đã thêm
        char[] array = password.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        return new String(array);
    }
}