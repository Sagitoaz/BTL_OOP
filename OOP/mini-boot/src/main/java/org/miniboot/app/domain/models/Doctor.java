package org.miniboot.app.domain.models;

/**
 * Doctor model - alias cho Employee với role="doctor"
 * Theo database mới: Doctor là Employee với role='doctor'
 * Giữ lại class này để tương thích với code cũ
 */
public class Doctor {
    private int id;
    private String firstName, lastName, licenseNo;

    public Doctor() {
    }

    public Doctor(int id, String firstName, String lastName, String licenseNo) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNo = licenseNo;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "Doctor{id=" + this.id + ", name=" + this.lastName + " " + this.firstName + "}";
    }
}
