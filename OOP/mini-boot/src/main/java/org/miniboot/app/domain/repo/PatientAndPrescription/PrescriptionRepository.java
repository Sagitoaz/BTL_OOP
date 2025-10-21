package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;

import java.util.List;

public interface PrescriptionRepository {

    Prescription save(Prescription prescription);

    List<Prescription> findAll();

    List<Prescription> findByCustomerId(int customerId);

    boolean deleteById(int id);


}
