package org.example.oop.Data.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * SpectaclePrescription - đơn kính theo database mới.
 * Theo database: Spectacle_Prescriptions
 */
public class SpectaclePrescription {
    private int id;
    private int appointmentId;
    private LocalDate dateIssued;
    private PrescriptionStatus status;

    // Mắt phải (OD - Oculus Dexter)
    private BigDecimal sphOd; // Sphere
    private BigDecimal cylOd; // Cylinder
    private Integer axisOd;
    private String vaOd; // Visual Acuity

    // Mắt trái (OS - Oculus Sinister)
    private BigDecimal sphOs;
    private BigDecimal cylOs;
    private Integer axisOs;
    private String vaOs;

    // Thông số khác
    private BigDecimal addPower;
    private BigDecimal pd; // Pupillary Distance

    private LensType lensType;
    private String material;
    private String features;

    private Integer recheckAfterMonths;
    private String notes;
    private LocalDateTime signedAt;

    /**
     * Constructor đầy đủ
     */
    public SpectaclePrescription(int id, int appointmentId, LocalDate dateIssued, PrescriptionStatus status,
                                 BigDecimal sphOd, BigDecimal cylOd, Integer axisOd, String vaOd,
                                 BigDecimal sphOs, BigDecimal cylOs, Integer axisOs, String vaOs,
                                 BigDecimal addPower, BigDecimal pd, LensType lensType, String material,
                                 String features, Integer recheckAfterMonths, String notes, LocalDateTime signedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.dateIssued = dateIssued;
        this.status = status;
        this.sphOd = sphOd;
        this.cylOd = cylOd;
        this.axisOd = axisOd;
        this.vaOd = vaOd;
        this.sphOs = sphOs;
        this.cylOs = cylOs;
        this.axisOs = axisOs;
        this.vaOs = vaOs;
        this.addPower = addPower;
        this.pd = pd;
        this.lensType = lensType;
        this.material = material;
        this.features = features;
        this.recheckAfterMonths = recheckAfterMonths;
        this.notes = notes;
        this.signedAt = signedAt;
    }

    /**
     * Constructor đơn giản cho đơn kính mới
     */
    public SpectaclePrescription(int id, int appointmentId, LocalDate dateIssued) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.dateIssued = dateIssued;
        this.status = PrescriptionStatus.ACTIVE;
        this.signedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(LocalDate dateIssued) {
        this.dateIssued = dateIssued;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public BigDecimal getSphOd() {
        return sphOd;
    }

    public void setSphOd(BigDecimal sphOd) {
        this.sphOd = sphOd;
    }

    public BigDecimal getCylOd() {
        return cylOd;
    }

    public void setCylOd(BigDecimal cylOd) {
        this.cylOd = cylOd;
    }

    public Integer getAxisOd() {
        return axisOd;
    }

    public void setAxisOd(Integer axisOd) {
        this.axisOd = axisOd;
    }

    public String getVaOd() {
        return vaOd;
    }

    public void setVaOd(String vaOd) {
        this.vaOd = vaOd;
    }

    public BigDecimal getSphOs() {
        return sphOs;
    }

    public void setSphOs(BigDecimal sphOs) {
        this.sphOs = sphOs;
    }

    public BigDecimal getCylOs() {
        return cylOs;
    }

    public void setCylOs(BigDecimal cylOs) {
        this.cylOs = cylOs;
    }

    public Integer getAxisOs() {
        return axisOs;
    }

    public void setAxisOs(Integer axisOs) {
        this.axisOs = axisOs;
    }

    public String getVaOs() {
        return vaOs;
    }

    public void setVaOs(String vaOs) {
        this.vaOs = vaOs;
    }

    public BigDecimal getAddPower() {
        return addPower;
    }

    public void setAddPower(BigDecimal addPower) {
        this.addPower = addPower;
    }

    public BigDecimal getPd() {
        return pd;
    }

    public void setPd(BigDecimal pd) {
        this.pd = pd;
    }

    public LensType getLensType() {
        return lensType;
    }

    public void setLensType(LensType lensType) {
        this.lensType = lensType;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Integer getRecheckAfterMonths() {
        return recheckAfterMonths;
    }

    public void setRecheckAfterMonths(Integer recheckAfterMonths) {
        this.recheckAfterMonths = recheckAfterMonths;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }
}

