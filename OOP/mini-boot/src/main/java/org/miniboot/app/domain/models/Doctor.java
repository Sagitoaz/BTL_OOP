package org.miniboot.app.domain.models;

public class Doctor {
    private int id;
    private String firstName, lastName, licenseNo;

    //constructor mặc định
    Doctor doctor;

    public Doctor() {
    }

    //constructor tiện lợi
    public Doctor(int id, String firstName, String lastName, String licenseNo) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNo = licenseNo;
    }

    // getter & setter
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

    @Override
    public String toString() {
        return "Doctor{id=" + this.id + ", name=" + this.lastName + " " + this.firstName + "}";
    }
}
