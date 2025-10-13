package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * Employee model - nhân viên (doctor và nurse)
 * Theo database mới: bảng Employees với role enum('doctor','nurse')
 */
public class Employee implements User {
    private String id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String avatar;
    private String role; // "doctor" hoặc "nurse"
    private String licenseNo;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;

    public Employee() {}

    public Employee(String id, String username, String password, String firstname, String lastname,
                    String avatar, String role, String licenseNo, String email, String phone,
                    boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.avatar = avatar;
        this.role = role;
        this.licenseNo = licenseNo;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Implement User interface
    @Override
    public String getRole() {
        return "EMPLOYEE"; // hoặc có thể return role field nếu cần phân biệt doctor/nurse
    }

    public String getEmployeeRole() {
        return role; // "doctor" hoặc "nurse"
    }

    public void setEmployeeRole(String role) {
        this.role = role;
    }

    public boolean isDoctor() {
        return "doctor".equalsIgnoreCase(role);
    }

    public boolean isNurse() {
        return "nurse".equalsIgnoreCase(role);
    }

    // Getters and Setters
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

