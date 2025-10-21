package org.miniboot.app.Service.mappers.CustomerAndPrescription;

import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;

public class PrescriptionMapper {
    public static Prescription mapResultSetToPrescription(java.sql.ResultSet rs) throws java.sql.SQLException {
        Prescription prescription = new Prescription();
        prescription.setId(rs.getInt("id"));
        prescription.setCustomerId(rs.getInt("customer_id"));
        prescription.setAppointmentId(rs.getInt("appointment_id"));
        prescription.setCreated_at(rs.getDate("created_at").toLocalDate());
        prescription.setUpdated_at(rs.getDate("updated_at").toLocalDate());
        prescription.setChiefComplaint(rs.getString("chief_complaint"));
        prescription.setRefractionNotes(rs.getString("refraction_notes"));
        prescription.setSph_od(rs.getDouble("sph_od"));
        prescription.setCyl_od(rs.getDouble("cyl_od"));
        prescription.setAxis_od(rs.getInt("axis_od"));
        prescription.setVa_od(rs.getString("va_od"));
        prescription.setPrism_od(rs.getDouble("prism_od"));
        prescription.setBase_od(Prescription.Base.valueOf(rs.getString("base_od")));
        prescription.setAdd_od(rs.getDouble("add_od"));
        prescription.setSph_os(rs.getDouble("sph_os"));
        prescription.setCyl_os(rs.getDouble("cyl_os"));
        prescription.setAxis_os(rs.getInt("axis_os"));
        prescription.setVa_os(rs.getString("va_os"));
        prescription.setPrism_os(rs.getDouble("prism_os"));
        prescription.setBase_os(Prescription.Base.valueOf(rs.getString("base_os")));
        prescription.setAdd_os(rs.getDouble("add_os"));
        prescription.setPd(rs.getDouble("pd"));
        prescription.setMaterial(Prescription.Material.valueOf(rs.getString("material")));
        prescription.setNotes(rs.getString("notes"));
        prescription.setHasAntiReflectiveCoating(rs.getBoolean("has_anti_reflective_coating"));
        prescription.setHasBlueLightFilter(rs.getBoolean("has_blue_light_filter"));
        prescription.setHasUvProtection(rs.getBoolean("has_uv_protection"));
        prescription.setPhotochromic(rs.getBoolean("is_photochromic"));
        prescription.setDiagnosis(rs.getString("diagnosis"));
        prescription.setPlan(rs.getString("plan"));
        prescription.setSignedAt(rs.getDate("signed_at").toLocalDate());
        prescription.setSignedBy(rs.getInt("signed_by"));
        prescription.setLens_type(Prescription.Lens_Type.valueOf(rs.getString("lens_type")));
        return prescription;
    }
}
