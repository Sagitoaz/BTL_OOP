package org.miniboot.app.domain.models.CustomerAndPrescription;

import org.miniboot.app.domain.models.User;
import org.miniboot.app.domain.models.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Customer model - khách hàng/bệnh nhân
 * Theo database mới: bảng Customers thay thế cho Patient
 * Đã cập nhật để tương thích với OOP_UI: sử dụng int cho ID
 */
public class Customer implements User {
    public enum Gender{
        MALE,
        FEMALE,
        OTHER;
    }
    private int id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String note;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(int id, String username, String password, String firstname, String lastname,
                    String phone, String email, LocalDate dob, Gender gender, String address,
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
    public UserRole getUserRole() {
        return UserRole.CUSTOMER;
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
        return true; // Customer mặc định luôn active
    }

    @Override
    public void setActive(boolean active) {
        // Customer không có trường active, nên không làm gì
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

    // Customer-specific getters and setters
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

    public LocalDate getDob() {
        return dob;
    }

    public int getAge() {
        if (dob == null) return 0;
        return LocalDate.now().getYear() - dob.getYear();
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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
    public String toString() {
        return getId()+"."+getFullName();
    }
}
