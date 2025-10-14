package org.miniboot.app.domain.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class CustomerRecord {
    public enum Gender {
        MALE, FEMALE, OTHER;
    }

    private int id;
    private String firstNameCustomer;
    private String lastNameCustomer;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String phoneNumber;
    private String email;
    private String notes;


    // Constructor mặc định cho JSON serialization
    public CustomerRecord() {
    }
    public CustomerRecord(int id, String firstNameCustomer, String lastNameCustomer, LocalDate dob, Gender gender,
                          String address, String phoneNumber, String email, String notes) {
        this.id = id;
        this.firstNameCustomer = firstNameCustomer;
        this.lastNameCustomer = lastNameCustomer;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstNameCustomer() {
        return firstNameCustomer;
    }

    public void setFirstNameCustomer(String firstNameCustomer) {
        this.firstNameCustomer = firstNameCustomer;
    }

    public String getLastNameCustomer() {
        return lastNameCustomer;
    }

    public void setLastNameCustomer(String lastNameCustomer) {
        this.lastNameCustomer = lastNameCustomer;
    }

    // Thêm method để lấy tên đầy đủ (backward compatibility)
    public String getNameCustomer() {
        if (lastNameCustomer == null && firstNameCustomer == null) {
            return null;
        }
        if (lastNameCustomer == null) {
            return firstNameCustomer;
        }
        if (firstNameCustomer == null) {
            return lastNameCustomer;
        }
        return lastNameCustomer + " " + firstNameCustomer;
    }

    // Method để set tên đầy đủ (backward compatibility)
    public void setNameCustomer(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            this.firstNameCustomer = null;
            this.lastNameCustomer = null;
            return;
        }

        String[] nameParts = fullName.trim().split("\\s+", 2);
        if (nameParts.length == 1) {
            this.firstNameCustomer = nameParts[0];
            this.lastNameCustomer = null;
        } else {
            this.lastNameCustomer = nameParts[0];  // Họ
            this.firstNameCustomer = nameParts[1]; // Tên
        }
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public int getAge() {
        if (dob == null) {
            return 0;
        }
        return Period.between(dob, LocalDate.now()).getYears();
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    // Dung de xu li khi in ra an toan
    public String toSafeString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    // Ham so sanh
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.id == ((CustomerRecord) o).id;
    }

    // Ham In ra de debug
    @Override
    public String toString() {
        return id + "." + getNameCustomer();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
