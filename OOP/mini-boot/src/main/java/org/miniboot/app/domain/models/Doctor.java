package org.miniboot.app.domain.models;

public class Doctor {
    private int id;
    private String firstName, lastName,lisenceNO;
    public Doctor(int id, String firstName, String lastName, String lisenceNO) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.lisenceNO = lisenceNO;
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

    public String getLisenceNO() {
        return lisenceNO;
    }

    @Override
    public String toString() {
        return "Doctor{id="+this.id+", name="+this.lastName+" "+this.firstName+"}";
    }
}
