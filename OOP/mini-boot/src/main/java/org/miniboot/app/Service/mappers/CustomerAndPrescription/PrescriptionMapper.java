package org.miniboot.app.Service.mappers.CustomerAndPrescription;

import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import java.math.BigDecimal;
import java.sql.Date;

public class PrescriptionMapper {
    public static Prescription mapResultSetToPrescription(java.sql.ResultSet rs) throws java.sql.SQLException {
        Prescription prescription = new Prescription();
        prescription.setId(rs.getInt("id"));
        prescription.setCustomerId(rs.getInt("customer_id"));
        prescription.setAppointmentId(rs.getInt("appointment_id"));

        // Handle dates
        Date createdAt = rs.getDate("created_at");
        if (createdAt != null) {
            prescription.setCreated_at(createdAt.toLocalDate());
        }

        Date updatedAt = rs.getDate("updated_at");
        if (updatedAt != null) {
            prescription.setUpdated_at(updatedAt.toLocalDate());
        }

        prescription.setChiefComplaint(rs.getString("chief_complaint"));
        prescription.setRefractionNotes(rs.getString("refraction_notes"));

        // Right Eye (OD) - Handle null BigDecimal values
        BigDecimal sphOd = rs.getBigDecimal("sph_od");
        prescription.setSph_od(sphOd != null ? sphOd.doubleValue() : 0.0);

        BigDecimal cylOd = rs.getBigDecimal("cyl_od");
        prescription.setCyl_od(cylOd != null ? cylOd.doubleValue() : 0.0);

        prescription.setAxis_od(rs.getInt("axis_od"));
        prescription.setVa_od(rs.getString("va_od"));

        BigDecimal prismOd = rs.getBigDecimal("prism_od");
        prescription.setPrism_od(prismOd != null ? prismOd.doubleValue() : 0.0);

        // Handle empty/null base_od - từ CSV thấy có trường hợp empty
        String baseOd = rs.getString("base_od");
        if (baseOd != null && !baseOd.trim().isEmpty()) {
            prescription.setBase_od(Prescription.Base.valueOf(baseOd));
        } else {
            prescription.setBase_od(Prescription.Base.NONE); // hoặc null tùy thiết kế
        }

        BigDecimal addOd = rs.getBigDecimal("add_od");
        prescription.setAdd_od(addOd != null ? addOd.doubleValue() : 0.0);

        // Left Eye (OS) - Handle null BigDecimal values
        BigDecimal sphOs = rs.getBigDecimal("sph_os");
        prescription.setSph_os(sphOs != null ? sphOs.doubleValue() : 0.0);

        BigDecimal cylOs = rs.getBigDecimal("cyl_os");
        prescription.setCyl_os(cylOs != null ? cylOs.doubleValue() : 0.0);

        prescription.setAxis_os(rs.getInt("axis_os"));
        prescription.setVa_os(rs.getString("va_os"));

        BigDecimal prismOs = rs.getBigDecimal("prism_os");
        prescription.setPrism_os(prismOs != null ? prismOs.doubleValue() : 0.0);

        // Handle empty/null base_os
        String baseOs = rs.getString("base_os");
        if (baseOs != null && !baseOs.trim().isEmpty()) {
            prescription.setBase_os(Prescription.Base.valueOf(baseOs));
        } else {
            prescription.setBase_os(Prescription.Base.NONE);
        }

        BigDecimal addOs = rs.getBigDecimal("add_os");
        prescription.setAdd_os(addOs != null ? addOs.doubleValue() : 0.0);

        // General information
        BigDecimal pd = rs.getBigDecimal("pd");
        prescription.setPd(pd != null ? pd.doubleValue() : 0.0);

        String material = rs.getString("material");
        if (material != null && !material.trim().isEmpty()) {
            prescription.setMaterial(Prescription.Material.valueOf(material));
        }

        prescription.setNotes(rs.getString("notes"));

        // Boolean fields
        prescription.setHasAntiReflectiveCoating(rs.getBoolean("has_anti_reflective_coating"));
        prescription.setHasBlueLightFilter(rs.getBoolean("has_blue_light_filter"));
        prescription.setHasUvProtection(rs.getBoolean("has_uv_protection"));
        prescription.setPhotochromic(rs.getBoolean("is_photochromic"));

        prescription.setDiagnosis(rs.getString("diagnosis"));
        prescription.setPlan(rs.getString("plan"));

        // Handle null signed_at - từ CSV thấy có trường hợp empty
        Date signedAt = rs.getDate("signed_at");
        if (signedAt != null) {
            prescription.setSignedAt(signedAt.toLocalDate());
        }

        // Handle signed_by - có thể là 0 hoặc null
        int signedBy = rs.getInt("signed_by");
        if (!rs.wasNull()) {
            prescription.setSignedBy(signedBy);
        }

        // Handle lens_type
        String lensType = rs.getString("lens_type");
        if (lensType != null && !lensType.trim().isEmpty()) {
            prescription.setLens_type(Prescription.Lens_Type.valueOf(lensType));
        }

        return prescription;
    }
}
