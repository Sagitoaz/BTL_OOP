package org.example.oop.Model.PatientAndPrescription;

import java.time.LocalDate;
import java.util.Objects;

public class SpectaclePrescription {

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
    private int appointmentId;

    // --- Examination Info ---
    private LocalDate created_at;
    private LocalDate updated_at;
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

    // --- Signature ---
    private LocalDate signedAt;
    private int signedBy;
    private Lens_Type lens_type;

    // Constructor đã được cập nhật - bỏ expiryDate vì không có trong toDataString()
    public SpectaclePrescription(int id, int appointmentId, LocalDate dateIssued,
                                 String chiefComplaint, String refractionNotes,
                                 double sph_od, double cyl_od, int axis_od, String va_od, double prism_od, Base base_od, double add_od,
                                 double sph_os, double cyl_os, int axis_os, String va_os, double prism_os, Base base_os, double add_os,
                                 double pd, Material material, String notes,
                                 boolean hasAntiReflectiveCoating, boolean hasBlueLightFilter, boolean hasUvProtection, boolean isPhotochromic,
                                 String diagnosis, String plan,
                                 LocalDate signedAt, int signedBy, Lens_Type lens_type) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.created_at = dateIssued;
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
        this.lens_type = lens_type;
    }


    // --- Getters cho tất cả các trường ---

    public int getId() { return id; }
    public int getAppointmentId() { return appointmentId; }
    public LocalDate getCreated_at() { return created_at; }
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



    public String toDataString() {
        return id + "|" +
                appointmentId + "|" +
                (created_at != null ? created_at : "") + "|" +
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
                (lens_type != null ? lens_type.name() : "");
    }

    // Đã cập nhật fromDataString
    public static SpectaclePrescription fromDataString(String line) {
        String[] fields = line.split("\\|", -1);

        // Đọc theo đúng thứ tự trong toDataString()
        int id = Integer.parseInt(fields[0]);
        int appointmentId = Integer.parseInt(fields[1]);
        LocalDate created_at = (fields[2].isEmpty() || fields[2].equals("null")) ? null : LocalDate.parse(fields[2]);
        String chiefComplaint = (fields[3].isEmpty() || fields[3].equalsIgnoreCase("null")) ? null : fields[3]; // Index 3
        String refractionNotes = (fields[4].isEmpty() || fields[4].equalsIgnoreCase("null")) ? null : fields[4]; // Index 4

        // OD (Right Eye) - Index 5-11
        double sph_od = (fields[5].isEmpty() || fields[5].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[5]);
        double cyl_od = (fields[6].isEmpty() || fields[6].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[6]);
        int axis_od = (fields[7].isEmpty() || fields[7].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[7]);
        String va_od = (fields[8].isEmpty() || fields[8].equalsIgnoreCase("null")) ? null : fields[8];
        double prism_od = (fields[9].isEmpty() || fields[9].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[9]);
        Base base_od = (fields[10].isEmpty() || fields[10].equalsIgnoreCase("null")) ? null : Base.valueOf(fields[10]);
        double add_od = (fields[11].isEmpty() || fields[11].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[11]);

        // OS (Left Eye) - Index 12-18
        double sph_os = (fields[12].isEmpty() || fields[12].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[12]);
        double cyl_os = (fields[13].isEmpty() || fields[13].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[13]);
        int axis_os = (fields[14].isEmpty() || fields[14].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[14]);
        String va_os = (fields[15].isEmpty() || fields[15].equalsIgnoreCase("null")) ? null : fields[15];
        double prism_os = (fields[16].isEmpty() || fields[16].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[16]);
        Base base_os = (fields[17].isEmpty() || fields[17].equalsIgnoreCase("null")) ? null : Base.valueOf(fields[17]);
        double add_os = (fields[18].isEmpty() || fields[18].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[18]);

        // General Details - Index 19-21
        double pd = (fields[19].isEmpty() || fields[19].equalsIgnoreCase("null")) ? 0 : Double.parseDouble(fields[19]);
        Material material = (fields[20].isEmpty() || fields[20].equalsIgnoreCase("null")) ? null : Material.valueOf(fields[20]);
        String notes = (fields[21].isEmpty() || fields[21].equalsIgnoreCase("null")) ? null : fields[21];

        // Lens Features - Index 22-25
        boolean hasAntiReflectiveCoating = Boolean.parseBoolean(fields[22]);
        boolean hasBlueLightFilter = Boolean.parseBoolean(fields[23]);
        boolean hasUvProtection = Boolean.parseBoolean(fields[24]);
        boolean isPhotochromic = Boolean.parseBoolean(fields[25]);

        // Diagnosis & Plan - Index 26-27
        String diagnosis = (fields[26].isEmpty() || fields[26].equalsIgnoreCase("null")) ? null : fields[26];
        String plan = (fields[27].isEmpty() || fields[27].equalsIgnoreCase("null")) ? null : fields[27];

        // Signature - Index 28-30
        LocalDate signedAt = (fields[28].isEmpty() || fields[28].equalsIgnoreCase("null")) ? null : LocalDate.parse(fields[28]);
        int signedBy = (fields[29].isEmpty() || fields[29].equalsIgnoreCase("null")) ? 0 : Integer.parseInt(fields[29]);
        Lens_Type lens_type = (fields[30].isEmpty() || fields[30].equalsIgnoreCase("null")) ? Lens_Type.OTHER : Lens_Type.valueOf(fields[30].toUpperCase());

        return new SpectaclePrescription(
                id, appointmentId, created_at, chiefComplaint, refractionNotes,
                sph_od, cyl_od, axis_od, va_od, prism_od, base_od, add_od,
                sph_os, cyl_os, axis_os, va_os, prism_os, base_os, add_os,
                pd, material, notes,
                hasAntiReflectiveCoating, hasBlueLightFilter, hasUvProtection, isPhotochromic,
                diagnosis, plan,
                signedAt, signedBy, lens_type
        );
    }



    @Override
    public String toString() {
        return "Prescription{id=" + id +  ", appointmentId=" + appointmentId + "}";
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