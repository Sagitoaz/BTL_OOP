package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.Doctor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDoctorRepository implements DoctorRepository {
    private final Map<Integer, Doctor> data = new ConcurrentHashMap<>();

    @Override
    public List<Doctor> findAll() {
        return new ArrayList<>(data.values());// trả về bản sao mới
    }
    @Override
    public Optional<Doctor> findById(int id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void saveAll(Collection<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            data.put(doctor.getId(),doctor);
        }
    }
    void saveDoctor(Doctor doctor) {
        data.put(doctor.getId(),doctor);
    }
}
