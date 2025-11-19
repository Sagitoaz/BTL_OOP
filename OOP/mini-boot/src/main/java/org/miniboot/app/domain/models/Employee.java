package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * Employee model - nhân viên (doctor và nurse)
 * Theo database mới: bảng Employees với role enum('doctor','nurse')
 * Đã cập nhật để tương thích với OOP_UI: sử dụng int cho ID
 */
public class Employee implements User {
    private int id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String avatar;
    private String role; // "doctor" hoặc "nurse"
    private String licenseNo;
    private String email;
    private String phone;
    private String gender; // "male" hoặc "female"
    private boolean active;
    private LocalDateTime createdAt;

    public Employee() {
    }

    public Employee(int id, String username, String password, String firstname, String lastname,
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

    @Override
    public UserRole getUserRole() {
        return UserRole.EMPLOYEE;
    }

    // Implement User interface methods
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
    public String getFullName() {
        return firstname + " " + lastname;
    }

    @Override
    public void setFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        this.firstname = parts[0];
        this.lastname = parts.length > 1 ? parts[1] : "";
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Employee-specific getters and setters
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                '}';
    }
}
