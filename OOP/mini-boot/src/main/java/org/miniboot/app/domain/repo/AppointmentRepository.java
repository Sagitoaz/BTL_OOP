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
}

