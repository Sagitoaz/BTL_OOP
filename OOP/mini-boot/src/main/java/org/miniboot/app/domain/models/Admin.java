package org.miniboot.app.domain.models;

/**
 * Admin model - quản trị viên hệ thống
 * Theo database mới: bảng Admins
 * Đã cập nhật để tương thích với OOP_UI: sử dụng int cho ID
 */
public class Admin implements User {
    private int id;
    private String username;
    private String password; // hash
    private String email;
    private boolean active;

    public Admin() {}

    public Admin(int id, String username, String password, String email, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active;
    }

    @Override
    public UserRole getUserRole() {
        return UserRole.ADMIN;
    }

    // Getters and Setters
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
