package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.PatientRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryPatientRecordRepository implements PatientRecordRepository {
    private final Map<Integer, PatientRecord> patientsData = new ConcurrentHashMap<>();
    private AtomicInteger nextId = new AtomicInteger(0);

    @Override
    public PatientRecord save(PatientRecord patient) {
        if(patient.getId() <=0 ){
            int id = nextId.incrementAndGet();
            patient.setId(id);
        }
        patientsData.put(patient.getId(), patient);
        return patient;
    }
    @Override
    public void saveAll(List<PatientRecord> patients) {
        for (PatientRecord patient : patients) {
            save(patient);
        }
    }
    @Override
    public Optional<PatientRecord> findById(int id) {
        return Optional.ofNullable(patientsData.get(id));
    }
    @Override
    public List<PatientRecord> findAll() {
        return new ArrayList<>(patientsData.values());
    }
    @Override
    public List<PatientRecord> findByName(String name) {
        String lowerName = name.toLowerCase();
        List<PatientRecord> res = new ArrayList<PatientRecord>();
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getNamePatient().toLowerCase().contains(lowerName)){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public Optional<PatientRecord> findByPhoneNumber(String phoneNumber) {
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getPhoneNumber().equals(phoneNumber)){
                return Optional.of(patientRecord);
            }
        }
        return Optional.empty();
    }
    @Override
    public Optional<PatientRecord> findByEmail(String email) {
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getEmail().equals(email)){
                return Optional.of(patientRecord);
            }
        }
        return Optional.empty();
    }
    @Override
    public List<PatientRecord> findByGender(PatientRecord.Gender gender) {
        List<PatientRecord> res = new ArrayList<PatientRecord>();
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getGender().equals(gender)){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public List<PatientRecord> findByDateFrom(LocalDate from){
        List<PatientRecord> res = new ArrayList<PatientRecord>();
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getDob() != null && (patientRecord.getDob().isEqual(from) || patientRecord.getDob().isAfter(from))){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public List<PatientRecord> findByDateTo(LocalDate to){
        List<PatientRecord> res = new ArrayList<PatientRecord>();
        for (PatientRecord patientRecord : patientsData.values()) {
            if(patientRecord.getDob() != null && (patientRecord.getDob().isEqual(to) || patientRecord.getDob().isBefore(to))){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public boolean deleteById(int id) {
        return patientsData.remove(id) != null;
    }
    @Override
    public boolean existsById(int id) {
        return patientsData.containsKey(id);
    }
    @Override
    public long count() {
        return patientsData.size();
    }
}
