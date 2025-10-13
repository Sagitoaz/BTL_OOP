package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Employee - đại diện cho nhân viên (bác sĩ và y tá) trong hệ thống.
 * Theo database mới: bảng Employees với role enum('doctor','nurse')
 */
public class Employee implements User {
    private int id;
    private String username;
    private String password; // hash
    private String firstname;
    private String lastname;
    private String avatar;
    private EmployeeRole role;
    private String licenseNo; // điền khi role=doctor
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;

    /**
     * Constructor đầy đủ
     */
    public Employee(int id, String username, String password, String firstname, String lastname,
                    String avatar, EmployeeRole role, String licenseNo, String email, String phone,
                    boolean isActive, LocalDateTime createdAt) {
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
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    /**
     * Constructor cho nhân viên mới (mặc định isActive = true, createdAt = now)
     */
    public Employee(int id, String username, String password, String firstname, String lastname,
                    EmployeeRole role, String email, String phone) {
        this(id, username, password, firstname, lastname, null, role, null, email, phone, 
             true, LocalDateTime.now());
    }

    /**
     * Constructor rỗng (default constructor)
     */
    public Employee() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Implement User interface
    @Override
    public UserRole getUserRole() {
        return UserRole.EMPLOYEE;
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

    @Override
    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            this.firstname = parts[0];
            this.lastname = parts.length > 1 ? parts[1] : "";
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
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
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    public boolean isDoctor() {
        return role == EmployeeRole.DOCTOR;
    }

    public boolean isNurse() {
        return role == EmployeeRole.NURSE;
    }
}
