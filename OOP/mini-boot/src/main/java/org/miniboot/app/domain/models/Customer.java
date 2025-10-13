package org.miniboot.app.domain.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Customer model - khách hàng/bệnh nhân
 * Theo database mới: bảng Customers thay thế cho Patient
 */
public class Customer implements User {
    private String id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private LocalDate dob;
    private String gender;
    private String address;
    private String note;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(String id, String username, String password, String firstname, String lastname,
                    String phone, String email, LocalDate dob, String gender, String address,
                    String note, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.note = note;
        this.createdAt = createdAt;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean isActive() {
        return true; // Customer không có trường isActive
    }

    @Override
    public void setActive(boolean active) {
        // Customer không có trường isActive
    }
}

