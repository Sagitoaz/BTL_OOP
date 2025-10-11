package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * User model representing a user in the system
 *
 * Mô tả:
 * - Lớp User là POJO/Model đại diện cho thực thể người dùng trong ứng dụng.
 * - Các trường cơ bản gồm id, username, password, role, email, fullName, phone, active, createdAt.
 * - Lưu ý bảo mật: trường password hiện đang lưu dưới dạng plain-text trong mô hình này.
 *   Trong môi trường sản xuất phải lưu password ở dạng đã băm (bcrypt/argon2) và KHÔNG trả
 *   password trong các API response. Khi refactor, nên tạo DTO/View object để trả về client
 *   mà không chứa password.
 */
public class User {
    // ID duy nhất của người dùng (có thể là UUID hoặc chuỗi do DB cấp)
    private String id;

    // Tên đăng nhập - dùng để xác thực; là duy nhất trong hệ thống
    private String username;

    // Mật khẩu: Lưu ý phải được băm trước khi lưu vào persistent store ở môi trường production
    private String password;

    // Vai trò của người dùng (ví dụ: ADMIN, DOCTOR, STAFF, PATIENT)
    private String role;

    // Email liên hệ
    private String email;

    // Tên đầy đủ hiển thị
    private String fullName;

    // Số điện thoại
    private String phone;

    // Trạng thái tài khoản: true = active, false = disabled/blocked
    private boolean active;

    // Thời điểm tạo tài khoản (LocalDateTime)
    private LocalDateTime createdAt;

    // Default constructor cho frameworks/serialization
    public User() {}

    /**
     * Constructor chính để khởi tạo User mới
     *
     * @param id id người dùng
     * @param username tên đăng nhập
     * @param password mật khẩu (ở đây là plain-text, cần băm trước khi lưu production)
     * @param role vai trò
     * @param email email
     * @param fullName tên đầy đủ
     * @param phone số điện thoại
     * @param active trạng thái hoạt động
     */
    public User(String id, String username, String password, String role, String email, String fullName, String phone, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    // === Getters and Setters ===

    // Lấy ID
    public String getId() { return id; }
    // Gán ID (cẩn thận khi gọi từ bên ngoài - tốt nhất do repository/DB đảm nhiệm)
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * Lấy mật khẩu (thận trọng):
     * - Trong đa số trường hợp không nên gọi getPassword() để tránh lộ mật khẩu.
     * - Nếu cần so sánh mật khẩu, hãy dùng service chuyên dụng (ví dụ PasswordService.verifyPassword)
     */
    public String getPassword() { return password; }

    /**
     * Gán mật khẩu:
     * - Trường hợp production: trước khi gọi setPassword, hãy băm mật khẩu bằng PasswordService.hashPasswordWithSalt
     */
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * toString(): Chuẩn hoá chuỗi hiển thị đối tượng nhưng KHÔNG in mật khẩu để tránh rò rỉ thông tin.
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
