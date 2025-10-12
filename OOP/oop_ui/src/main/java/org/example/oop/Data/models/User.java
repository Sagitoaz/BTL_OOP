package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Abstract base class representing a user in the system.
 * This class encapsulates common user attributes and provides
 * a foundation for different user types (Admin, Doctor, Staff, Patient).
 * All user subclasses inherit from this class and set their specific role.
 */

/*
  Ghi chú chi tiết dành cho người duy trì sau này:
  - Lớp User chứa các thuộc tính chung (id, username, password, role, contact info).
  - Các lớp con (Admin, Doctor, Staff, Patient) kế thừa và có thể mở rộng thêm thuộc tính
    hoặc hành vi (ví dụ: Doctor có chuyên khoa, Patient có ngày sinh).
  - Tránh lưu mật khẩu ở dạng plain text khi triển khai thật; nên lưu hash và salt phù hợp.
  - createdAt được khởi tạo tự động khi tạo đối tượng, dùng để audit/kiểm tra.
*/
public abstract class User {
    protected int id;
    protected String username;
    protected String password; // Will be hashed for security
    protected UserRole role;
    protected String email;
    protected String fullName;
    protected String phone;
    protected LocalDateTime createdAt;
    protected boolean active;

    /**
     * Constructor for creating a new User instance.
     * Initializes all user attributes and sets default values for createdAt and active.
     */
    // Thêm comment chi tiết bằng tiếng Việt cho constructor:
    // - Dùng khi khởi tạo user mới trong bộ nhớ (chưa chắc đã lưu vào file/DB).
    // - createdAt được gán LocalDateTime.now() ở đây, nên khi parse từ file, createdAt cần được
    //   overwrite nếu muốn giữ timestamp gốc (hiện fromFileFormat không làm điều đó).
    public User(int id, String username, String password, UserRole role,
                String email, String fullName, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    // Getters and Setters with JavaDoc

    /**
     * Gets the unique identifier for this user.
     * @return The user's ID as a String.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this user.
     * @param id The new ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username for this user.
     * @return The username as a String.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for this user.
     * @param username The new username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password for this user.
     * Note: This should be hashed for security.
     * @return The password as a String.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this user.
     * @param password The new password to set (should be hashed).
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the role of this user.
     * @return The UserRole enum value.
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the role for this user.
     * @param role The new UserRole to set.
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Gets the email address of this user.
     * @return The email as a String.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for this user.
     * @param email The new email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the full name of this user.
     * @return The full name as a String.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name for this user.
     * @param fullName The new full name to set.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the phone number of this user.
     * @return The phone number as a String.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number for this user.
     * @param phone The new phone number to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the creation timestamp of this user account.
     * @return The LocalDateTime when the account was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp for this user account.
     * @param createdAt The new LocalDateTime to set.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Checks if this user account is active.
     * @return true if the account is active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status for this user account.
     * @param active The new active status to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Convert to file format: id|username|password|role|email|fullname|phone|created|active
     */
    // Chú ý khi thay đổi định dạng file:
    // - Nếu thêm/bỏ trường, cần đồng bộ cả toFileFormat() và fromFileFormat()
    // - Trường createdAt chuyển thành String bằng toString(); khi parse lại cần dùng LocalDateTime.parse
    public String toFileFormat() {
        return String.join("|",
                String.valueOf(id), username, password, role.name(), email, fullName, phone,
                createdAt.toString(), String.valueOf(active)
        );
    }

    // Parse from file format
    // Ghi chú quan trọng:
    // - fromFileFormat giả sử file có đầy đủ các phần và thứ tự chính xác.
    // - Nếu dữ liệu bị thiếu hoặc format không đúng, có thể ném ArrayIndexOutOfBoundsException hoặc
    //   IllegalArgumentException khi value không thuộc UserRole; nên cân nhắc validate chuỗi trước khi parse.
    // - Hàm hiện tại khởi tạo đối tượng con (Admin/Doctor/Staff/Patient) nhưng không gán createdAt và active
    //   từ file; nếu muốn giữ timestamp và trạng thái, cần chỉnh lại constructor hoặc gọi setter sau khi tạo.
    public static User fromFileFormat(String line) {
        // Remove BOM if present and trim whitespace
        line = line.replace("\uFEFF", "").trim();
        if (line.isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\|");
        if (parts.length < 7) {
            return null; // Skip invalid lines
        }

        // Trim each part to remove any whitespace
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        int id = Integer.parseInt(parts[0]);
        UserRole role = UserRole.valueOf(parts[3]);

        switch (role) {
            case ADMIN: return new Admin(id, parts[1], parts[2], parts[4], parts[5], parts[6]);
            case DOCTOR: return new Doctor(id, parts[1], parts[2], parts[4], parts[5], parts[6]);
            case STAFF: return new Staff(id, parts[1], parts[2], parts[4], parts[5], parts[6]);
            case PATIENT: return new Patient(id, parts[1], parts[2], parts[4], parts[5], parts[6]);
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    // toString() method for debugging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", createdAt=" + createdAt +
                ", active=" + active +
                '}';
    }
}