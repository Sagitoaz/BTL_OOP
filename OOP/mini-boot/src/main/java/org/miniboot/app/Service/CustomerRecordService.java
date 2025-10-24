package org.miniboot.app.Service;

import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerRecordService {
    private final CustomerRecordRepository customerRecordRepository;
    public CustomerRecordService(CustomerRecordRepository customerRecordRepository) {
        this.customerRecordRepository = customerRecordRepository;
    }
    public List<Customer> searchPatientRecords(CustomerSearchCriteria criteria) {
        List<Customer> res = customerRecordRepository.findAll();
        return res.stream().filter(customerRecord -> matchCriteria(customerRecord, criteria)).collect(Collectors.toList());
    }

    private boolean matchCriteria(Customer customerRecord, CustomerSearchCriteria criteria) {
        if(criteria.getSearchKey() != null || criteria.isEmpty()){

            String lowerKey = criteria.getSearchKey().trim().toLowerCase();
            if(!(customerRecord.getFullName().toLowerCase().contains(lowerKey) ||
                 customerRecord.getPhone().toLowerCase().equals(lowerKey))){
                try{
                    int id = Integer.parseInt(lowerKey);
                    if(customerRecord.getId() != id){
                        return false;
                    }
                }
                catch (Exception e){
                    return false;
                }
            }

        }
        if(criteria.getGender() != null){
            if(customerRecord.getGender() != criteria.getGender()){
                return false;
            }
        }
        if(criteria.getDateFrom() != null){
            if(customerRecord.getDob().isBefore(criteria.getDateFrom())){
                return false;
            }
        }
        if(criteria.getDateTo() != null){
            if(customerRecord.getDob().isAfter(criteria.getDateTo())){
                return false;
            }
        }
        return true;
    }

}
