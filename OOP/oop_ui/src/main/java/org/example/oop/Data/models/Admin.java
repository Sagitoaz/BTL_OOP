package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Admin - đại diện cho người quản trị trong hệ thống.
 * Theo database mới, Admin là bảng riêng biệt không kế thừa.
 */
public class Admin implements User {
    private int id;
    private String username;
    private String password; // hash
    private String email;
    private boolean isActive;

    /**
     * Constructor đầy đủ
     */
    public Admin(int id, String username, String password, String email, boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = isActive;
    }

    /**
     * Constructor cho admin mới (mặc định isActive = true)
     */
    public Admin(int id, String username, String password, String email) {
        this(id, username, password, email, true);
    }

    // Implement User interface
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
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
    }

    // Business methods
    public void manageUsers() {
        System.out.println("Admin " + username + " is managing users.");
    }

    public void generateReports() {
        System.out.println("Admin " + username + " is generating reports.");
    }

    public void configureSystem() {
        System.out.println("Admin " + username + " is configuring the system.");
    }
}