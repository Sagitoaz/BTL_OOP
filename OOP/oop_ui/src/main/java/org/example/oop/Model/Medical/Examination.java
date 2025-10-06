package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.util.Objects;

public class Examination {

    private int id;
    private int patientRecordId;
    private double visionLeft;
    private double visionRight;
    private boolean colorBlindTest;
    private double eyePressure;
    private String diagnosis;
    private String recommendation;
    private  String note;
    private int  doctorId;
    private LocalDate examinationDate;
    //Data file id|recordId|visionLeft|visionRight|colorBlindTest|eyePressure|diagnosis|recommendation|note|doctorId|date
    public Examination(int id, int patientRecordId, double visionLeft, double visionRight, boolean colorBlindTest, double eyePressure, String diagnosis
                       , String recommendation, String note, int doctorId, LocalDate examinationDate) {
        if(id <= 0 || patientRecordId <= 0 || doctorId <= 0){
            throw new IllegalArgumentException("id must > 0");
        }
        this.id = id;
        this.patientRecordId = patientRecordId;
        this.visionLeft = visionLeft;
        this.visionRight = visionRight;
        this.colorBlindTest = colorBlindTest;
        this.eyePressure = eyePressure;
        this.diagnosis = diagnosis;
        this.recommendation = recommendation;
        this.note = note;
        this.doctorId = doctorId;
        this.examinationDate = examinationDate;
    }
    public  int getId() {
        return id;
    }
    public int getPatientRecordId() {
        return patientRecordId;
    }
    public double getVisionLeft() {
        return visionLeft;
    }

    public double getVisionRight() {
        return visionRight;
    }
    public boolean getColorBindTest() {
        return colorBlindTest;
    }
    public double getEyePressure() {
        return eyePressure;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public String getRecommendation() {
        return recommendation;
    }
    public String getNote() {
        return note;
    }
    public int getDoctorId() {
        return doctorId;
    }
    public LocalDate getExaminationDate() {
        return examinationDate;
    }
    public String toSafeString(String s){
        if(s == null){
            return "";
        }
        return s;
    }
    //Data file id|recordId|visionLeft|visionRight|colorBlindTest|eyePressure|diagnosis|recommendation|note|doctorId|date
    public String ToDataString(){
        String examinationString = "";
        if(examinationDate != null){
            examinationString = examinationDate.toString();
        }
        return String.join("|", String.valueOf(id), String.valueOf(patientRecordId), String.valueOf(visionLeft), String.valueOf(visionRight)
                ,String.valueOf(colorBlindTest), String.valueOf(eyePressure), toSafeString(diagnosis), toSafeString(recommendation),
                toSafeString(note), String.valueOf(doctorId), examinationString
        );
    }

    public static Examination fromDataString(String line){
        String[] fields  = line.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        int patientRecordId = Integer.parseInt(fields[1]);
        double visionLeft = Double.parseDouble(fields[2]);
        double visionRight = Double.parseDouble(fields[3]);
        boolean colorBindTest = (fields[4].equals("null") || fields[4].isBlank()) ? false: Boolean.parseBoolean(fields[4]);
        double eyePressure =(fields[5].equals("null") || fields[5].isBlank()) ? 0.0:  Double.parseDouble(fields[5]);
        String diagnosis = (fields[6].equals("null") || fields[6].isBlank()) ? null: fields[6];
        String recommendation = (fields[7].equals("null") || fields[7].isBlank()) ? null: fields[7];
        String note = (fields[8].equals("null") || fields[8].isBlank()) ? null: fields[8];
        int doctorId = Integer.parseInt(fields[9]);
        LocalDate examinationDate = LocalDate.parse(fields[10]);
        return new Examination(id, patientRecordId, visionLeft, visionRight, colorBindTest, eyePressure, diagnosis, recommendation, note, doctorId, examinationDate);
    }

    @Override
    public String toString(){
        return "Examination{" + "id=" + id + ", patientRecordId=" + patientRecordId+", visionLeft="+visionLeft+", visionRight="+visionRight+"}";
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return id == ((Examination)o).id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
