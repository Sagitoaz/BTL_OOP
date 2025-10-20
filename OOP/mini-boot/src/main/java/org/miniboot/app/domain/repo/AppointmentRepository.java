package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Appointment> findAll();

    Optional<Appointment> findById(Integer id);

    Appointment save(Appointment appointment);

    List<Appointment> findByDoctorIdAndDate(Integer doctorId, String date);
    // (không bắt buộc) boolean existsSameSlot(int doctorId, String date, String startTime);
    List<Appointment> findWithFilters(
            Integer doctorId,      // null = không lọc
            Integer customerId,    // null = không lọc
            String status,         // null = không lọc
            String fromDate,       // null = không lọc (format: YYYY-MM-DD)
            String toDate,         // null = không lọc (format: YYYY-MM-DD)
            String searchKeyword   // null = không lọc (tìm trong notes)
    );
}

