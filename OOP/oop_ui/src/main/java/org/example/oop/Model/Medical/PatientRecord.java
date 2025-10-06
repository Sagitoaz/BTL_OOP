package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class PatientRecord {
    public enum Gender{
        MALE, FEMALE, OTHER;
    }
    private int id;
    private String namePatient;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String phoneNumber;
    private String email;
    private List<Prescription> prescriptionList;
    private List<Examination> examinationList;
    private List<MedicalHistory> medicalHistoryList;
    public PatientRecord(int id, String namePatient, LocalDate dob, Gender gender, String address, String phoneNumber, String email) {
        this.id = id;
        this.namePatient = namePatient;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.prescriptionList = new ArrayList<>();
        this.examinationList = new ArrayList<>();
        this.medicalHistoryList = new ArrayList<>();
    }
    public int getId(){
        return id;
    }
    public String getNamePatient(){
        return namePatient;
    }
    public LocalDate getDob(){
        return dob;
    }
    public int getAge(){
        if(dob == null){
            return 0;
        }
        return dob.getYear() - LocalDate.now().getYear();
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
    public void addPrescription(Prescription prescription){
        prescriptionList.add(prescription);
    }
    public void addExamination(Examination examination){
        examinationList.add(examination);
    }
    public void addMedicalHistory(MedicalHistory medicalHistory){
        medicalHistoryList.add(medicalHistory);
    }



}

