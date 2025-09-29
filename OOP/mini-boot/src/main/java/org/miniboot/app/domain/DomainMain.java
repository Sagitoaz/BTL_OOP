package org.miniboot.app.domain;

import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.repo.DoctorRepository;
import org.miniboot.app.domain.repo.InMemoryDoctorRepository;

import java.util.*;

public class DomainMain {
    public static void main(String[] args) {
        DoctorRepository doctorRepository = new InMemoryDoctorRepository();

        doctorRepository.saveAll(List.of(
                new Doctor(1,"Hậu","Trần Văn","123456"),
                new Doctor(2,"Dúng","Dương Chí","1236780"),
                new Doctor(3,"Toàn","Phạm Minh","71439"),
                new Doctor(4,"Hếu","Phan Minh","1637428"),
                new Doctor(5,"Trung","Nuyễn Thành","23647")
        ));
        System.out.println(doctorRepository.findAll());
        System.out.println(doctorRepository.findById(1).orElse(null));
        System.out.println(doctorRepository.findById(6).orElse(null));
    }
}
