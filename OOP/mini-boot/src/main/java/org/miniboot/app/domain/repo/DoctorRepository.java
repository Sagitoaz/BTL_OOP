package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.Doctor;

import java.util.*;

public interface DoctorRepository {
    List<Doctor> findAll();//tất cả các bác sĩ

    Optional<Doctor> findById(int id);// tìm kiếm bằng id

    void saveAll(Collection<Doctor> doctors);//lưu seed nhiều bác sĩ

    void saveDoctor(Doctor doctor);
}
