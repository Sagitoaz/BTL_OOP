package org.miniboot.app.domain.models.CustomerAndPrescription;

import java.time.LocalDate;
import java.util.Objects;

public class Prescription {
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

    public enum Base {
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

    //  ID Fields 
    private int id;
    private int appointmentId;
    private int customerId;

    //  Examination Info 
    private LocalDate created_at;
    private LocalDate updated_at;
    private String chiefComplaint;
    private String refractionNotes;

    //  OD (Right Eye) Prescription 
    private double sph_od;
    private double cyl_od;
    private int axis_od;
    private String va_od;
    private double prism_od;
    private Base base_od;
    private double add_od;

    //  OS (Left Eye) Prescription 
    private double sph_os;
    private double cyl_os;
    private int axis_os;
    private String va_os;
    private double prism_os;
    private Base base_os;
    private double add_os;

    //  General Prescription Details 
    private double pd;
    private Material material;
    private String notes;

    //  Lens Features 
    private boolean hasAntiReflectiveCoating;
    private boolean hasBlueLightFilter;
    private boolean hasUvProtection;
    private boolean isPhotochromic;

    //  Diagnosis & Plan 
    private String diagnosis;
    private String plan;

    //  Signature 
    private LocalDate signedAt;
    private int signedBy;
    private Lens_Type lens_type;
    public Prescription() {

    }

    // Constructor đầy đủ
    public Prescription(int id, int appointmentId, int customerId, LocalDate created_at, LocalDate updated_at,
                        String chiefComplaint, String refractionNotes,
                        double sph_od, double cyl_od, int axis_od, String va_od, double prism_od, Base base_od, double add_od,
                        double sph_os, double cyl_os, int axis_os, String va_os, double prism_os, Base base_os, double add_os,
                        double pd, Material material, String notes,
                        boolean hasAntiReflectiveCoating, boolean hasBlueLightFilter, boolean hasUvProtection, boolean isPhotochromic,
                        String diagnosis, String plan,
                        LocalDate signedAt, int signedBy, Lens_Type lens_type) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.created_at = created_at;
        this.updated_at = updated_at;
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

    //  Getters 
    public int getId() { return id; }
    public int getAppointmentId() { return appointmentId; }
    public int getCustomerId() { return customerId; }
    public LocalDate getCreated_at() { return created_at; }
    public LocalDate getUpdated_at() { return updated_at; }
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

    //  Setters 
    public void setId(int id) { this.id = id; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setCreated_at(LocalDate created_at) { this.created_at = created_at; }
    public void setUpdated_at(LocalDate updated_at) { this.updated_at = updated_at; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public void setRefractionNotes(String refractionNotes) { this.refractionNotes = refractionNotes; }
    public void setSph_od(double sph_od) { this.sph_od = sph_od; }
    public void setCyl_od(double cyl_od) { this.cyl_od = cyl_od; }
    public void setAxis_od(int axis_od) { this.axis_od = axis_od; }
    public void setVa_od(String va_od) { this.va_od = va_od; }
    public void setPrism_od(double prism_od) { this.prism_od = prism_od; }
    public void setBase_od(Base base_od) { this.base_od = base_od; }
    public void setAdd_od(double add_od) { this.add_od = add_od; }
    public void setSph_os(double sph_os) { this.sph_os = sph_os; }
    public void setCyl_os(double cyl_os) { this.cyl_os = cyl_os; }
    public void setAxis_os(int axis_os) { this.axis_os = axis_os; }
    public void setVa_os(String va_os) { this.va_os = va_os; }
    public void setPrism_os(double prism_os) { this.prism_os = prism_os; }
    public void setBase_os(Base base_os) { this.base_os = base_os; }
    public void setAdd_os(double add_os) { this.add_os = add_os; }
    public void setPd(double pd) { this.pd = pd; }
    public void setMaterial(Material material) { this.material = material; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setHasAntiReflectiveCoating(boolean hasAntiReflectiveCoating) { this.hasAntiReflectiveCoating = hasAntiReflectiveCoating; }
    public void setHasBlueLightFilter(boolean hasBlueLightFilter) { this.hasBlueLightFilter = hasBlueLightFilter; }
    public void setHasUvProtection(boolean hasUvProtection) { this.hasUvProtection = hasUvProtection; }
    public void setPhotochromic(boolean photochromic) { isPhotochromic = photochromic; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setPlan(String plan) { this.plan = plan; }
    public void setSignedAt(LocalDate signedAt) { this.signedAt = signedAt; }
    public void setSignedBy(int signedBy) { this.signedBy = signedBy; }
    public void setLens_type(Lens_Type lens_type) { this.lens_type = lens_type; }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
