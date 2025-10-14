package org.miniboot.app.domain.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class CustomerRecord {
    public enum Gender {
        NAM, NỮ, KHÁC;
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

    // Data id|firstName|lastName|dob|gender|address|phone|email|note
    public String toDataString() {
        String dobString = (this.dob == null) ? "" : this.dob.toString();
        return String.join("|",
            String.valueOf(this.id),
            toSafeString(this.firstNameCustomer),
            toSafeString(this.lastNameCustomer),
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
    public static CustomerRecord fromDataString(String line) {
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

        return new CustomerRecord(id, firstNamePatient, lastNamePatient, dob, gender, address, phoneNumber, email, note);
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
