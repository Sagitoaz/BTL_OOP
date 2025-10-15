package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.domain.models.CustomerRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryPatientRecordRepository implements CustomerRecordRepository {
    private final Map<Integer, CustomerRecord> patientsData = new ConcurrentHashMap<>();
    private AtomicInteger nextId = new AtomicInteger(0);

    @Override
    public CustomerRecord save(CustomerRecord customer) {
        if(customer.getId() <=0 ){
            int id = nextId.incrementAndGet();
            customer.setId(id);
        }
        patientsData.put(customer.getId(), customer);
        return customer;
    }
    @Override
    public void saveAll(List<CustomerRecord> customers) {
        for (CustomerRecord patient : customers) {
            save(patient);
        }
    }
    @Override
    public Optional<CustomerRecord> findById(int id) {
        return Optional.ofNullable(patientsData.get(id));
    }
    @Override
    public List<CustomerRecord> findAll() {
        return new ArrayList<>(patientsData.values());
    }
    @Override
    public List<CustomerRecord> findByName(String name) {
        String lowerName = name.toLowerCase();
        List<CustomerRecord> res = new ArrayList<CustomerRecord>();
        for (CustomerRecord patientRecord : patientsData.values()) {
            if(patientRecord.getNameCustomer().toLowerCase().contains(lowerName)){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public Optional<CustomerRecord> findByPhoneNumber(String phoneNumber) {
        for (CustomerRecord patientRecord : patientsData.values()) {
            if(patientRecord.getPhone().equals(phoneNumber)){
                return Optional.of(patientRecord);
            }
        }
        return Optional.empty();
    }
    @Override
    public Optional<CustomerRecord> findByEmail(String email) {
        for (CustomerRecord patientRecord : patientsData.values()) {
            if(patientRecord.getEmail().equals(email)){
                return Optional.of(patientRecord);
            }
        }
        return Optional.empty();
    }
    @Override
    public List<CustomerRecord> findByGender(CustomerRecord.Gender gender) {
        List<CustomerRecord> res = new ArrayList<CustomerRecord>();
        for (CustomerRecord patientRecord : patientsData.values()) {
            if(patientRecord.getGender().equals(gender)){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public List<CustomerRecord> findByDateFrom(LocalDate from){
        List<CustomerRecord> res = new ArrayList<CustomerRecord>();
        for (CustomerRecord patientRecord : patientsData.values()) {
            if(patientRecord.getDob() != null && (patientRecord.getDob().isEqual(from) || patientRecord.getDob().isAfter(from))){
                res.add(patientRecord);
            }
        }
        return res;
    }
    @Override
    public List<CustomerRecord> findByDateTo(LocalDate to){
        List<CustomerRecord> res = new ArrayList<CustomerRecord>();
        for (CustomerRecord patientRecord : patientsData.values()) {
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
