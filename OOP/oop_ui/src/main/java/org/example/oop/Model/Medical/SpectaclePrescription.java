package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class SpectaclePrescription {
    public enum Status{

        ACTIVE, EXPIRED, VOID

    }
    public enum Lens_Type{
        SINGLE_VISION, BIFOCAL, PROGRESSIVE, CONTACT, OTHER
    }
    private int id;
    private int doctorId;
    private int patientRecordId;
    private int appointmentId;
    private LocalDate dateIssued;
    private double sph_od;
    private double cyl_od;
    private int axis_od;
    private String va_od;
    private double sph_os;
    private double cyl_os;
    private int axis_os;
    private String va_os;
    private double add_power;
    private double pd ;
    private String material;
    private String features;
    private int recheck_after_months;
    private String notes;
    private LocalDate signedAt;
    private int signedBy;
    private Status status;
    private Lens_Type lens_type;

    public SpectaclePrescription(int id, int doctorId, int patientRecordId, int appointmentId, LocalDate dateIssued, double sph_od
    , double cyl_od, int axis_od, String va_od, double sph_os, double cyl_os, int axis_os, String va_os, double add_power, double pd,
                                 String material, String features, int recheck_after_months, String notes, LocalDate signedAt, int signedBy,
                                 Status status, Lens_Type lens_type ) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientRecordId = patientRecordId;
        this.appointmentId = appointmentId;
        this.dateIssued = dateIssued;
        this.sph_od = sph_od;
        this.cyl_od = cyl_od;
        this.axis_od = axis_od;
        this.va_od = va_od;
        this.sph_os = sph_os;
        this.cyl_os = cyl_os;
        this.axis_os = axis_os;
        this.va_os = va_os;
        this.add_power = add_power;
        this.pd = pd;
        this.material = material;
        this.features = features;
        this.recheck_after_months = recheck_after_months;
        this.notes = notes;
        this.signedAt = signedAt;
        this.signedBy = signedBy;
        this.status = status;
        this.lens_type = lens_type;
    }
    public int getId() {
        return id;
    }
    public int getDoctorId() {
        return doctorId;
    }
    public int getPatientRecordId() {
        return patientRecordId;
    }
    public int getAppointmentId() {
        return appointmentId;
    }
    public LocalDate getDateIssued() {

        return dateIssued;
    }
    public double getSph_od() {
        return sph_od;
    }
    public double getCyl_od() {
        return cyl_od;
    }
    public int getAxis_od() {
        return axis_od;
    }
    public String getVa_od() {
        return va_od;
    }
    public double getSph_os() {
        return sph_os;
    }
    public double getCyl_os() {
        return cyl_os;
    }
    public int getAxis_os() {
        return axis_os;
    }
    public String getVa_os() {
        return va_os;
    }
    public double getAdd_power() {
        return add_power;
    }
    public double getPd() {
        return pd;
    }
    public String getMaterial() {
        return material;
    }
    public String getFeatures() {
        return features;
    }
    public int getRecheck_after_months() {
        return recheck_after_months;
    }
    public String getNotes() {
        return notes;
    }
    public LocalDate getSignedAt() {
        return signedAt;
    }
    public int getSignedBy() {
        return signedBy;
    }
    public Status getStatus() {
        return status;
    }
    public Lens_Type getLens_type() {
        return lens_type;
    }

    public String toDataString() {

        return id + "|" +
                doctorId + "|" +
                patientRecordId + "|" +
                appointmentId + "|" +
                (dateIssued != null ? dateIssued : "") + "|" +
                sph_od + "|" +
                cyl_od + "|" +
                axis_od + "|" +
                (va_od != null ? va_od : "") + "|" +
                sph_os + "|" +
                cyl_os + "|" +
                axis_os + "|" +
                (va_os != null ? va_os : "") + "|" +
                add_power + "|" +
                pd + "|" +
                (material != null ? material : "") + "|" +
                (features != null ? features : "") + "|" +
                recheck_after_months + "|" +
                (notes != null ? notes : "") + "|" +
                (signedAt != null ? signedAt : "") + "|" +
                signedBy + "|" +
                (status != null ? status.name() : "") + "|" +
                (lens_type != null ? lens_type.name() : "");
    }

    public static SpectaclePrescription fromDataString(String line) {
        String[] fields = line.split("\\|", -1);

        int id = Integer.parseInt(fields[0]);
        int doctorId = Integer.parseInt(fields[1]);
        int patientRecordId = Integer.parseInt(fields[2]);
        int appointmentId = Integer.parseInt(fields[3]);

        LocalDate dateIssued = (fields[4].isEmpty() || fields[4].equals("null")) ? null : LocalDate.parse(fields[4]);
        double sph_od = (fields[5].isEmpty() || fields[5].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[5]);
        double cyl_od = (fields[6].isEmpty() || fields[6].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[6]);
        int axis_od = (fields[7].isEmpty() || fields[7].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[7]);
        String va_od = (fields[8].isEmpty() || fields[8].equalsIgnoreCase("null")) ? null : fields[8];
        double sph_os = (fields[9].isEmpty() || fields[9].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[9]);
        double cyl_os = (fields[10].isEmpty() || fields[10].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[10]);
        int axis_os = (fields[11].isEmpty() || fields[11].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[11]);
        String va_os = (fields[12].isEmpty() || fields[12].equalsIgnoreCase("null")) ? null : fields[12];
        double add_power = (fields[13].isEmpty() || fields[13].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[13]);
        double pd = (fields[14].isEmpty() || fields[14].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[14]);
        String material = (fields[15].isEmpty() || fields[15].equalsIgnoreCase("null")) ? null : fields[15];
        String features = (fields[16].isEmpty() || fields[16].equalsIgnoreCase("null")) ? null : fields[16];
        int recheck_after_months = (fields[17].isEmpty() || fields[17].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[17]);
        String notes = (fields[18].isEmpty() || fields[18].equalsIgnoreCase("null")) ? null : fields[18];
        LocalDate signedAt = (fields[19].isEmpty() || fields[19].equalsIgnoreCase("null")) ? null : LocalDate.parse(fields[19]);
        int signedBy = (fields[20].isEmpty() || fields[20].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[20]);
        Status status = (fields[21].isEmpty() || fields[21].equalsIgnoreCase("null")) ? null : Status.valueOf(fields[21].toUpperCase());
        Lens_Type lens_type = (fields[22].isEmpty() || fields[22].equalsIgnoreCase("null")) ? null : Lens_Type.valueOf(fields[22].toUpperCase());

        return new SpectaclePrescription(
                id, doctorId, patientRecordId, appointmentId, dateIssued,
                sph_od, cyl_od, axis_od, va_od, sph_os, cyl_os, axis_os, va_os,
                add_power, pd, material, features, recheck_after_months,
                notes, signedAt, signedBy, status, lens_type
        );
    }

    @Override
    public String toString(){
        return "Prescription{id=" + id + ", patientRecordId=" + patientRecordId + ", appointmentId=" + appointmentId + ", doctorId=" + doctorId +"}";
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.id == ((SpectaclePrescription) o).id;
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
