package org.example.oop.Model.PatientAndPrescription;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class CustomerRecord {
    public enum Gender{
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
    private List<SpectaclePrescription> prescriptionList;
    private List<MedicalHistory> medicalHistoryList;

    public CustomerRecord(int id, String firstNamePatient, String lastNamePatient, LocalDate dob, Gender gender, String address, String phoneNumber, String email, String allergies) {
        if(id <= 0){
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
        this.prescriptionList = new ArrayList<>();
        this.medicalHistoryList = new ArrayList<>();
        this.notes = allergies;
    }

    // Constructor với namePatient để backward compatibility
    public CustomerRecord(int id, String namePatient, LocalDate dob, Gender gender, String address, String phoneNumber, String email, String allergies) {
        if(id <= 0){
            throw new IllegalArgumentException("id must be > 0");
        }
        this.id = id;
        setNamePatient(namePatient); // Sử dụng setter để parse tên
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.prescriptionList = new ArrayList<>();
        this.medicalHistoryList = new ArrayList<>();
        this.notes = allergies;
    }

    public int getId(){
        return id;
    }

    public String getFirstNamePatient(){
        return firstNamePatient;
    }

    public void setFirstNamePatient(String firstNamePatient){
        this.firstNamePatient = firstNamePatient;
    }

    public String getLastNamePatient(){
        return lastNamePatient;
    }

    public void setLastNamePatient(String lastNamePatient){
        this.lastNamePatient = lastNamePatient;
    }

    // Method để lấy tên đầy đủ (backward compatibility)
    public String getNamePatient(){
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

    public LocalDate getDob(){
        return dob;
    }
    public int getAge(){
        if(dob == null){
            return 0;
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }
    public Gender getGender(){
        return gender;
    }
    public String getAddress(){
        return address;
    }
    public String getPhoneNumber(){

        return phoneNumber;
    }
    public String getEmail(){
        return email;
    }
    public String getNotes(){
        return notes;
    }
    public void addPrescription(SpectaclePrescription prescription){
        if(prescription == null){
            return;
        }
        prescriptionList.add(prescription);
    }
    public List<SpectaclePrescription> getPrescriptionList(){

        return Collections.unmodifiableList(prescriptionList);
    }
    public void addMedicalHistory(MedicalHistory medicalHistory){
        if(medicalHistory == null){
            return;
        }
        medicalHistoryList.add(medicalHistory);
    }
    public List<MedicalHistory> getMedicalHistoryList(){
        return Collections.unmodifiableList(medicalHistoryList);
    }
    // Dung de xu li khi in ra an toan
    public String toSafeString(String s){
        if(s == null){
            return "";
        }
        return s;
    }


    // Ham so sanh
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return this.id == ((CustomerRecord)o).id;
    }


    // Ham In ra de debug
    @Override
    public String toString() {
        return id + "." + getNamePatient();
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }




}
