package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import java.util.List;

public interface CustomerRecordRepository {

    Customer save(Customer customer);

    void saveAll(List<Customer> customers);

    List<Customer> findAll();

    List<Customer> findByFilterAll(CustomerSearchCriteria criteria);

    boolean deleteById(int id);


    boolean existsById(int id);


    long count();



}
