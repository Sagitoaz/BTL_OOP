package org.miniboot.app.domain.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PatientRecord {
    public enum Gender {
        NAM, NỮ, KHÁC;
    }

    private int id;
    private String firstNamePatient;
    private String lastNamePatient;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String phoneNumber;
    private String email;
    private String notes;


    // Constructor mặc định cho JSON serialization
    public PatientRecord() {
        // this.prescriptionList = new ArrayList<>();
        // this.medicalHistoryList = new ArrayList<>();
    }

    public PatientRecord(int id, String firstNamePatient, String lastNamePatient, LocalDate dob, Gender gender,
                        String address, String phoneNumber, String email, String notes) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be > 0");
        }
        this.id = id;
        this.firstNamePatient = firstNamePatient;
        this.lastNamePatient = lastNamePatient;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.notes = notes;
        // this.prescriptionList = new ArrayList<>();
        // this.medicalHistoryList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstNamePatient() {
        return firstNamePatient;
    }

    public void setFirstNamePatient(String firstNamePatient) {
        this.firstNamePatient = firstNamePatient;
    }

    public String getLastNamePatient() {
        return lastNamePatient;
    }

    public void setLastNamePatient(String lastNamePatient) {
        this.lastNamePatient = lastNamePatient;
    }

    // Thêm method để lấy tên đầy đủ (backward compatibility)
    public String getNamePatient() {
        if (lastNamePatient == null && firstNamePatient == null) {
            return null;
        }
        if (lastNamePatient == null) {
            return firstNamePatient;
        }
        if (firstNamePatient == null) {
            return lastNamePatient;
        }
        return lastNamePatient + " " + firstNamePatient;
    }

    // Method để set tên đầy đủ (backward compatibility)
    public void setNamePatient(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            this.firstNamePatient = null;
            this.lastNamePatient = null;
            return;
        }

        String[] nameParts = fullName.trim().split("\\s+", 2);
        if (nameParts.length == 1) {
            this.firstNamePatient = nameParts[0];
            this.lastNamePatient = null;
        } else {
            this.lastNamePatient = nameParts[0];  // Họ
            this.firstNamePatient = nameParts[1]; // Tên
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

    // Data id|firstName|lastName|dob|gender|address|phone|email|note
    public String toDataString() {
        String dobString = (this.dob == null) ? "" : this.dob.toString();
        return String.join("|",
            String.valueOf(this.id),
            toSafeString(this.firstNamePatient),
            toSafeString(this.lastNamePatient),
            dobString,
            gender.name(),
            toSafeString(this.address),
            toSafeString(this.phoneNumber),
            toSafeString(this.email),
            toSafeString(this.notes)
        );
    }

    // Doc Data tu file
    // Data id|firstName|lastName|dob|gender|address|phone|email|note
    public static PatientRecord fromDataString(String line) {
        String[] fields = line.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        String firstNamePatient = (fields[1].equalsIgnoreCase("null") || fields[1].isBlank()) ? null : fields[1];
        String lastNamePatient = (fields[2].equalsIgnoreCase("null") || fields[2].isBlank()) ? null : fields[2];

        LocalDate dob = null;
        try {
            dob = (fields[3].equalsIgnoreCase("null") || fields[3].isBlank()) ? null : LocalDate.parse(fields[3]);
        } catch (Exception e) {
            // Ignore parsing error
        }

        Gender gender;
        try {
            gender = (fields[4].equalsIgnoreCase("null") || fields[4].isBlank()) ? Gender.KHÁC : Gender.valueOf(fields[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            gender = Gender.KHÁC;
        }

        String address = (fields[5].equalsIgnoreCase("null") || fields[5].isBlank()) ? null : fields[5];
        String phoneNumber = (fields[6].equalsIgnoreCase("null") || fields[6].isBlank()) ? null : fields[6];
        String email = (fields[7].equalsIgnoreCase("null") || fields[7].isBlank()) ? null : fields[7];
        String note = (fields.length > 8 && !fields[8].equalsIgnoreCase("null") && !fields[8].isBlank()) ? fields[8] : null;

        return new PatientRecord(id, firstNamePatient, lastNamePatient, dob, gender, address, phoneNumber, email, note);
    }

    // Ham so sanh
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.id == ((PatientRecord) o).id;
    }

    // Ham In ra de debug
    @Override
    public String toString() {
        return id + "." + getNamePatient();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
