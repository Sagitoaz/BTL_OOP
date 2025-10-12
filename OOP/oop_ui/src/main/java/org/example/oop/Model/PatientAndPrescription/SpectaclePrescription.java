package org.example.oop.Model.PatientAndPrescription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class SpectaclePrescription {
    public enum Status {
        CÒN_HẠN, HẾT_HẠN, KHÁC
    }

    public enum Lens_Type {
        SINGLE_VISION("Đơn tròng"),
        BIFOCAL("Hai tròng"),
        PROGRESSIVE("Đa tròng"),
        CONTACT("Kính áp tròng"),
        OTHER("Khác");
        private final String display_Name;
        Lens_Type(String lens_type) {
            this.display_Name = lens_type;
        }
        @Override
        public String toString() {
            return display_Name;
        }
    }
    public enum Material {
        CR_39("1.50 Standard CR-39"),
        TRIVEX("1.53 Trivex - Siêu nhẹ & Bền"),
        POLYCARBONATE("1.59 Polycarbonate - Chống va đập"),
        HIGH_INDEX_1_60("1.60 High-Index - Mỏng"),
        HIGH_INDEX_1_67("1.67 High-Index - Siêu mỏng"),
        HIGH_INDEX_1_74("1.74 High-Index - Mỏng nhất"),
        OTHER("Khác");

        private final String display_Name;

        Material(String material) {
            this.display_Name = material;
        }
        @Override
        public String toString() {
            return display_Name;
        }
    }
    public enum Base{
        IN("In - Hướng vào"),
        OUT("Out - Hướng ra"),
        UP("Up - Hướng lên"),
        DOWN("Down - Hướng xuống"),
        NONE("None - Không có");
        private final String display_Name;
        Base(String base) {
            this.display_Name = base;
        }
        @Override
        public String toString() {
            return display_Name;
        }
    }


    // --- ID Fields ---
    private int id;
    private int doctorId;
    private int patientId;
    private int appointmentId;

    // --- Examination Info ---
    private LocalDate dateIssued;
    private LocalDate expiryDate;
    private String chiefComplaint;
    private String refractionNotes;

    // --- OD (Right Eye) Prescription ---
    private double sph_od;
    private double cyl_od;
    private int axis_od;
    private String va_od;
    private double prism_od;
    private Base base_od;
    private double add_od;

    // --- OS (Left Eye) Prescription ---
    private double sph_os;
    private double cyl_os;
    private int axis_os;
    private String va_os;
    private double prism_os;
    private Base base_os;
    private double add_os;

    // --- General Prescription Details ---
    private double pd;
    private Material material;
    private String notes;

    // --- Lens Features ---
    private boolean hasAntiReflectiveCoating;
    private boolean hasBlueLightFilter;
    private boolean hasUvProtection;
    private boolean isPhotochromic;

    // --- Diagnosis & Plan ---
    private String diagnosis;
    private String plan;

    // --- Signature & Status ---
    private LocalDate signedAt;
    private int signedBy;
    private Status status;
    private Lens_Type lens_type;

    // Constructor đã được cập nhật
    public SpectaclePrescription(int id, int doctorId, int patientId, int appointmentId, LocalDate dateIssued, LocalDate expiryDate,
                                 String chiefComplaint, String refractionNotes,
                                 double sph_od, double cyl_od, int axis_od, String va_od, double prism_od, Base base_od, double add_od,
                                 double sph_os, double cyl_os, int axis_os, String va_os, double prism_os, Base base_os, double add_os,
                                 double pd, Material material, String notes,
                                 boolean hasAntiReflectiveCoating, boolean hasBlueLightFilter, boolean hasUvProtection, boolean isPhotochromic,
                                 String diagnosis, String plan,
                                 LocalDate signedAt, int signedBy, Status status, Lens_Type lens_type) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.dateIssued = dateIssued;
        this.expiryDate = expiryDate;
        this.chiefComplaint = chiefComplaint;
        this.refractionNotes = refractionNotes;
        this.sph_od = sph_od;
        this.cyl_od = cyl_od;
        this.axis_od = axis_od;
        this.va_od = va_od;
        this.prism_od = prism_od;
        this.base_od = base_od;
        this.add_od = add_od;
        this.sph_os = sph_os;
        this.cyl_os = cyl_os;
        this.axis_os = axis_os;
        this.va_os = va_os;
        this.prism_os = prism_os;
        this.base_os = base_os;
        this.add_os = add_os;
        this.pd = pd;
        this.material = material;
        this.notes = notes;
        this.hasAntiReflectiveCoating = hasAntiReflectiveCoating;
        this.hasBlueLightFilter = hasBlueLightFilter;
        this.hasUvProtection = hasUvProtection;
        this.isPhotochromic = isPhotochromic;
        this.diagnosis = diagnosis;
        this.plan = plan;
        this.signedAt = signedAt;
        this.signedBy = signedBy;
        this.status = status;
        this.lens_type = lens_type;
    }

    // --- Getters cho tất cả các trường ---

    public int getId() { return id; }
    public int getDoctorId() { return doctorId; }
    public int getPatientId() { return patientId; }
    public int getAppointmentId() { return appointmentId; }
    public LocalDate getDateIssued() { return dateIssued; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getRefractionNotes() { return refractionNotes; }
    public double getSph_od() { return sph_od; }
    public double getCyl_od() { return cyl_od; }
    public int getAxis_od() { return axis_od; }
    public String getVa_od() { return va_od; }
    public double getPrism_od() { return prism_od; }
    public Base getBase_od() { return base_od; }
    public double getAdd_od() { return add_od; }
    public double getSph_os() { return sph_os; }
    public double getCyl_os() { return cyl_os; }
    public int getAxis_os() { return axis_os; }
    public String getVa_os() { return va_os; }
    public double getPrism_os() { return prism_os; }
    public Base getBase_os() { return base_os; }
    public double getAdd_os() { return add_os; }
    public double getPd() { return pd; }
    public Material getMaterial() { return material; }
    public String getNotes() { return notes; }
    public boolean hasAntiReflectiveCoating() { return hasAntiReflectiveCoating; }
    public boolean hasBlueLightFilter() { return hasBlueLightFilter; }
    public boolean hasUvProtection() { return hasUvProtection; }
    public boolean isPhotochromic() { return isPhotochromic; }
    public String getDiagnosis() { return diagnosis; }
    public String getPlan() { return plan; }
    public LocalDate getSignedAt() { return signedAt; }
    public int getSignedBy() { return signedBy; }

    public Lens_Type getLens_type() { return lens_type; }
    public Status getStatus() {
        LocalDate now = LocalDate.now();
        if(now.isAfter(expiryDate)){
            status = Status.HẾT_HẠN;
        }
        else{
            if(expiryDate == null){
                status = Status.KHÁC;
            }
            else{
                status = Status.CÒN_HẠN;
            }
        }
        return status;
    }



    public String toDataString() {
        return id + "|" +
                doctorId + "|" +
                patientId + "|" +
                appointmentId + "|" +
                (dateIssued != null ? dateIssued : "") + "|" +
                (expiryDate != null ? expiryDate : "") + "|" +
                (chiefComplaint != null ? chiefComplaint : "") + "|" +
                (refractionNotes != null ? refractionNotes : "") + "|" +
                sph_od + "|" +
                cyl_od + "|" +
                axis_od + "|" +
                (va_od != null ? va_od : "") + "|" +
                prism_od + "|" +
                (base_od != null ? base_od : "") + "|" +
                add_od + "|" +
                sph_os + "|" +
                cyl_os + "|" +
                axis_os + "|" +
                (va_os != null ? va_os : "") + "|" +
                prism_os + "|" +
                (base_os != null ? base_os : "") + "|" +
                add_os + "|" +
                pd + "|" +
                (material != null ? material : "") + "|" +
                (notes != null ? notes : "") + "|" +
                hasAntiReflectiveCoating + "|" +
                hasBlueLightFilter + "|" +
                hasUvProtection + "|" +
                isPhotochromic + "|" +
                (diagnosis != null ? diagnosis : "") + "|" +
                (plan != null ? plan : "") + "|" +
                (signedAt != null ? signedAt : "") + "|" +
                signedBy + "|" +
                (status != null ? status.name() : "") + "|" +
                (lens_type != null ? lens_type.name() : "");
    }

    // Đã cập nhật fromDataString
    public static SpectaclePrescription fromDataString(String line) {
        String[] fields = line.split("\\|", -1);

        int id = Integer.parseInt(fields[0]);
        int doctorId = Integer.parseInt(fields[1]);
        int patientId = Integer.parseInt(fields[2]);
        int appointmentId = Integer.parseInt(fields[3]);
        LocalDate dateIssued = (fields[4].isEmpty() || fields[4].equals("null")) ? null : LocalDate.parse(fields[4]);
        LocalDate expiryDate = (fields[5].isEmpty() || fields[5].equals("null")) ? null : LocalDate.parse(fields[5]);
        String chiefComplaint = (fields[6].isEmpty() || fields[6].equalsIgnoreCase("null")) ? null : fields[6];
        String refractionNotes = (fields[7].isEmpty() || fields[7].equalsIgnoreCase("null")) ? null : fields[7];
        double sph_od = (fields[8].isEmpty() || fields[8].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[8]);
        double cyl_od = (fields[9].isEmpty() || fields[9].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[9]);
        int axis_od = (fields[10].isEmpty() || fields[10].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[10]);
        String va_od = (fields[11].isEmpty() || fields[11].equalsIgnoreCase("null")) ? null : fields[11];
        double prism_od = (fields[12].isEmpty() || fields[12].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[12]);
        Base base_od = (fields[13].isEmpty() || fields[13].equalsIgnoreCase("null")) ? null : Base.valueOf(fields[13]);
        double add_od = (fields[14].isEmpty() || fields[14].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[14]);
        double sph_os = (fields[15].isEmpty() || fields[15].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[15]);
        double cyl_os = (fields[16].isEmpty() || fields[16].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[16]);
        int axis_os = (fields[17].isEmpty() || fields[17].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[17]);
        String va_os = (fields[18].isEmpty() || fields[18].equalsIgnoreCase("null")) ? null : fields[18];
        double prism_os = (fields[19].isEmpty() || fields[19].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[19]);
        Base base_os = (fields[20].isEmpty() || fields[20].equalsIgnoreCase("null")) ? null : Base.valueOf(fields[20]);
        double add_os = (fields[21].isEmpty() || fields[21].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[21]);
        double pd = (fields[22].isEmpty() || fields[22].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[22]);
        Material material = (fields[23].isEmpty() || fields[23].equalsIgnoreCase("null")) ? null : Material.valueOf(fields[23]);
        String notes = (fields[24].isEmpty() || fields[24].equalsIgnoreCase("null")) ? null : fields[24];
        boolean hasAntiReflectiveCoating = Boolean.parseBoolean(fields[25]);
        boolean hasBlueLightFilter = Boolean.parseBoolean(fields[26]);
        boolean hasUvProtection = Boolean.parseBoolean(fields[27]);
        boolean isPhotochromic = Boolean.parseBoolean(fields[28]);
        String diagnosis = (fields[29].isEmpty() || fields[29].equalsIgnoreCase("null")) ? null : fields[29];
        String plan = (fields[30].isEmpty() || fields[30].equalsIgnoreCase("null")) ? null : fields[30];
        LocalDate signedAt = (fields[31].isEmpty() || fields[31].equalsIgnoreCase("null")) ? null : LocalDate.parse(fields[31]);
        int signedBy = (fields[32].isEmpty() || fields[32].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[32]);
        Status status = (fields[33].isEmpty() || fields[33].equalsIgnoreCase("null")) ? null : Status.valueOf(fields[33].toUpperCase());
        Lens_Type lens_type = (fields[34].isEmpty() || fields[34].equalsIgnoreCase("null")) ? Lens_Type.OTHER : Lens_Type.valueOf(fields[34].toUpperCase());

        return new SpectaclePrescription(
                id, doctorId, patientId, appointmentId, dateIssued, expiryDate, chiefComplaint, refractionNotes,
                sph_od, cyl_od, axis_od, va_od, prism_od, base_od, add_od,
                sph_os, cyl_os, axis_os, va_os, prism_os, base_os, add_os,
                pd, material, notes,
                hasAntiReflectiveCoating, hasBlueLightFilter, hasUvProtection, isPhotochromic,
                diagnosis, plan,
                signedAt, signedBy, status, lens_type
        );
    }

    @Override
    public String toString() {
        return "Prescription{id=" + id + ", patientId=" + patientId + ", appointmentId=" + appointmentId + ", doctorId=" + doctorId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.id == ((SpectaclePrescription) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}