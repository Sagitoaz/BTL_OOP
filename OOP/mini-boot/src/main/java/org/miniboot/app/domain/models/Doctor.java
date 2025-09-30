package org.miniboot.app.domain.models;

public class Doctor {
    private int id;
    private String firstName, lastName, lisenceNo;

    public Doctor(int id, String firstName, String lastName, String lisenceNo) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.lisenceNo = lisenceNo;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLisenceNo() {
        return lisenceNo;
    }

    @Override
    public String toString() {
        return "Doctor{id=" + this.id + ", name=" + this.lastName + " " + this.firstName + "}";
    }
}
