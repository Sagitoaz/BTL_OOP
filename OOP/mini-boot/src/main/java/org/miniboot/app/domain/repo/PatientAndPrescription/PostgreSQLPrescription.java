package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.Service.mappers.CustomerAndPrescription.PrescriptionMapper;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLPrescription implements PrescriptionRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLPrescription() {
        dbConfig = DatabaseConfig.getInstance();
    }


    public List<Prescription> findAll(){
        List<Prescription> prescriptions = new ArrayList<>();
        String sqlQuery = "SELECT * FROM prescriptions";
        try(Connection conn = dbConfig.getConnection()){


            PreparedStatement psmt = conn.prepareStatement(sqlQuery);
            ResultSet rs = psmt.executeQuery();

            while(rs.next()){
                prescriptions.add(PrescriptionMapper.mapResultSetToPrescription(rs));
            }



        } catch (SQLException e) {
            System.err.println("❌ Error find all Prescriptions: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Throw exception để controller biết có lỗi
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }

        return prescriptions;
    }
    public List<Prescription> findByCustomerId(int customer_id){
        List<Prescription> prescriptions = new ArrayList<>();
        String sqlQuery = "SELECT * FROM prescriptions WHERE customer_id = ?";
        try(Connection conn = dbConfig.getConnection()){
            PreparedStatement psmt = conn.prepareStatement(sqlQuery);
            psmt.setInt(1, customer_id);
            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                prescriptions.add(PrescriptionMapper.mapResultSetToPrescription(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error find Prescriptions By customer Id: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Throw exception để controller biết có lỗi
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return prescriptions;
    }
    public List<Prescription> findByAppointmentId(int appointment_id){
        List<Prescription> prescriptions = new ArrayList<>();
        String sqlQuery = "SELECT * FROM prescriptions WHERE appointment_id = ?";
        try(Connection conn = dbConfig.getConnection()){
            PreparedStatement psmt = conn.prepareStatement(sqlQuery);
            psmt.setInt(1, appointment_id);
            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                prescriptions.add(PrescriptionMapper.mapResultSetToPrescription(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error find Prescriptions appointmentId: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Throw exception để controller biết có lỗi
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return prescriptions;
    }
    public Prescription save(Prescription prescription){
        if(prescription.getId() <= 0){
            // Ensure all required date fields are not null before insert
            if(prescription.getCreated_at() == null) {
                prescription.setCreated_at(LocalDate.now());
            }
            if(prescription.getUpdated_at() == null) {
                prescription.setUpdated_at(LocalDate.now());
            }
            if(prescription.getSignedAt() == null) {
                prescription.setSignedAt(LocalDate.now());
            }
            return insert(prescription);
        }
        else {
            prescription.setUpdated_at(LocalDate.now());
            return update(prescription);
        }

    }

    private Prescription insert(Prescription prescription){
        String sqlQuery = "INSERT INTO prescriptions (" +
                "appointment_id, customer_id, created_at, updated_at, " +
                "chief_complaint, refraction_notes, " +
                "sph_od, cyl_od, axis_od, va_od, prism_od, base_od, add_od, " +
                "sph_os, cyl_os, axis_os, va_os, prism_os, base_os, add_os, " +
                "pd, material, notes, " +
                "has_anti_reflective_coating, has_blue_light_filter, has_uv_protection, is_photochromic, " +
                "diagnosis, plan, signed_at, signed_by, lens_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = dbConfig.getConnection()){
            PreparedStatement psmt = conn.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            // Dates should already be non-null from save() method, but add safety check
            LocalDate createdAt = prescription.getCreated_at() != null ? prescription.getCreated_at() : LocalDate.now();
            LocalDate updatedAt = prescription.getUpdated_at() != null ? prescription.getUpdated_at() : LocalDate.now();
            LocalDate signedAt = prescription.getSignedAt() != null ? prescription.getSignedAt() : LocalDate.now();

            System.out.println("🔍 INSERT DEBUG - createdAt: " + createdAt + ", updatedAt: " + updatedAt + ", signedAt: " + signedAt);
            System.out.println("🔍 INSERT DEBUG - Material: " + prescription.getMaterial() + ", Lens_type: " + prescription.getLens_type());
            System.out.println("🔍 INSERT DEBUG - Base_od: " + prescription.getBase_od() + ", Base_os: " + prescription.getBase_os());

            psmt.setInt(1, prescription.getAppointmentId());
            psmt.setInt(2, prescription.getCustomerId());

            // Wrap Date.valueOf in try-catch to catch exact line
            try {
                psmt.setDate(3, Date.valueOf(createdAt));

            } catch (Exception e) {
                System.err.println("❌ Error setting created_at: createdAt=" + createdAt);
                throw new RuntimeException("Failed to set created_at: " + e.getMessage(), e);
            }

            try {
                psmt.setDate(4, Date.valueOf(updatedAt));
            } catch (Exception e) {
                System.err.println("❌ Error setting updated_at: updatedAt=" + updatedAt);
                throw new RuntimeException("Failed to set updated_at: " + e.getMessage(), e);
            }

            psmt.setString(5, prescription.getChiefComplaint());
            psmt.setString(6, prescription.getRefractionNotes());
            psmt.setBigDecimal(7, BigDecimal.valueOf(prescription.getSph_od()));
            psmt.setBigDecimal(8, BigDecimal.valueOf(prescription.getCyl_od()));
            psmt.setInt(9, prescription.getAxis_od());
            psmt.setString(10, prescription.getVa_od());
            psmt.setBigDecimal(11, BigDecimal.valueOf(prescription.getPrism_od()));
            psmt.setString(12, prescription.getBase_od() != null ? prescription.getBase_od().name() : Prescription.Base.NONE.name());
            psmt.setBigDecimal(13, BigDecimal.valueOf(prescription.getAdd_od()));
            psmt.setBigDecimal(14, BigDecimal.valueOf(prescription.getSph_os()));
            psmt.setBigDecimal(15, BigDecimal.valueOf(prescription.getCyl_os()));
            psmt.setInt(16, prescription.getAxis_os());
            psmt.setString(17, prescription.getVa_os());
            psmt.setBigDecimal(18, BigDecimal.valueOf(prescription.getPrism_os()));
            psmt.setString(19, prescription.getBase_os() != null ? prescription.getBase_os().name() : Prescription.Base.NONE.name());
            psmt.setBigDecimal(20, BigDecimal.valueOf(prescription.getAdd_os()));
            psmt.setBigDecimal(21, BigDecimal.valueOf(prescription.getPd()));
            psmt.setString(22, prescription.getMaterial() != null ? prescription.getMaterial().name() : "");
            psmt.setString(23, prescription.getNotes());
            psmt.setBoolean(24, prescription.hasAntiReflectiveCoating());
            psmt.setBoolean(25, prescription.hasBlueLightFilter());
            psmt.setBoolean(26, prescription.hasUvProtection());
            psmt.setBoolean(27, prescription.isPhotochromic());
            psmt.setString(28, prescription.getDiagnosis());
            psmt.setString(29, prescription.getPlan());

            try {
                psmt.setDate(30, Date.valueOf(signedAt));
            } catch (Exception e) {
                System.err.println("❌ Error setting signed_at: signedAt=" + signedAt);
                throw new RuntimeException("Failed to set signed_at: " + e.getMessage(), e);
            }

            psmt.setInt(31, prescription.getSignedBy());
            psmt.setString(32, prescription.getLens_type() != null ? prescription.getLens_type().name() : "");

            int affectedRows = psmt.executeUpdate();
            if(affectedRows == 0){
                throw new SQLException("Inserting prescription failed, no rows affected.");
            }
            try(ResultSet generatedKeys = psmt.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    prescription.setId(generatedKeys.getInt(1));
                }
            }
        }
        catch (SQLException e){
            System.err.println("❌ Error insert Prescriptions: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Throw exception để controller biết có lỗi
            throw new RuntimeException("DatabaseF insert failed: " + e.getMessage(), e);
        }
        return prescription;
    }
    private Prescription update(Prescription prescription){
        String sqlQuery = "UPDATE prescriptions SET " +
                "appointment_id = ?, customer_id = ?, updated_at = ?, " +
                "chief_complaint = ?, refraction_notes = ?, " +
                "sph_od = ?, cyl_od = ?, axis_od = ?, va_od = ?, prism_od = ?, base_od = ?, add_od = ?, " +
                "sph_os = ?, cyl_os = ?, axis_os = ?, va_os = ?, prism_os = ?, base_os = ?, add_os = ?, " +
                "pd = ?, material = ?, notes = ?, " +
                "has_anti_reflective_coating = ?, has_blue_light_filter = ?, has_uv_protection = ?, is_photochromic = ?, " +
                "diagnosis = ?, plan = ?, signed_at = ?, signed_by = ?, lens_type = ? " +
                "WHERE id = ?";

        try(Connection conn = dbConfig.getConnection()){
            PreparedStatement psmt = conn.prepareStatement(sqlQuery);

            // Ensure dates are not null
            LocalDate updatedAt = prescription.getUpdated_at() != null ? prescription.getUpdated_at() : LocalDate.now();
            LocalDate signedAt = prescription.getSignedAt() != null ? prescription.getSignedAt() : LocalDate.now();

            psmt.setInt(1, prescription.getAppointmentId());
            psmt.setInt(2, prescription.getCustomerId());
            psmt.setDate(3, Date.valueOf(updatedAt));
            psmt.setString(4, prescription.getChiefComplaint());
            psmt.setString(5, prescription.getRefractionNotes());

            // OD (Right Eye)
            psmt.setBigDecimal(6, BigDecimal.valueOf(prescription.getSph_od()));
            psmt.setBigDecimal(7, BigDecimal.valueOf(prescription.getCyl_od()));
            psmt.setInt(8, prescription.getAxis_od());
            psmt.setString(9, prescription.getVa_od());
            psmt.setBigDecimal(10, BigDecimal.valueOf(prescription.getPrism_od()));
            psmt.setString(11, prescription.getBase_od() != null ? prescription.getBase_od().name() : Prescription.Base.NONE.name());
            psmt.setBigDecimal(12, BigDecimal.valueOf(prescription.getAdd_od()));

            // OS (Left Eye)
            psmt.setBigDecimal(13, BigDecimal.valueOf(prescription.getSph_os()));
            psmt.setBigDecimal(14, BigDecimal.valueOf(prescription.getCyl_os()));
            psmt.setInt(15, prescription.getAxis_os());
            psmt.setString(16, prescription.getVa_os());
            psmt.setBigDecimal(17, BigDecimal.valueOf(prescription.getPrism_os()));
            psmt.setString(18, prescription.getBase_os() != null ? prescription.getBase_os().name() : Prescription.Base.NONE.name());
            psmt.setBigDecimal(19, BigDecimal.valueOf(prescription.getAdd_os()));

            // General Details
            psmt.setBigDecimal(20, BigDecimal.valueOf(prescription.getPd()));
            psmt.setString(21, prescription.getMaterial() != null ? prescription.getMaterial().name() : "");
            psmt.setString(22, prescription.getNotes());

            // Lens Features
            psmt.setBoolean(23, prescription.hasAntiReflectiveCoating());
            psmt.setBoolean(24, prescription.hasBlueLightFilter());
            psmt.setBoolean(25, prescription.hasUvProtection());
            psmt.setBoolean(26, prescription.isPhotochromic());

            // Diagnosis & Plan
            psmt.setString(27, prescription.getDiagnosis());
            psmt.setString(28, prescription.getPlan());
            psmt.setDate(29, Date.valueOf(signedAt));
            psmt.setInt(30, prescription.getSignedBy());
            psmt.setString(31, prescription.getLens_type() != null ? prescription.getLens_type().name() : "");

            // WHERE condition
            psmt.setInt(32, prescription.getId());

            int affectedRows = psmt.executeUpdate();
            if(affectedRows == 0){
                throw new SQLException("Updating prescription failed, no rows affected.");
            }

        } catch (SQLException e){
            System.err.println("❌ Error update Prescription: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database update failed: " + e.getMessage(), e);
        }

        return prescription;
    }
    

}
