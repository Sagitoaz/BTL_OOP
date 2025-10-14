package org.miniboot.app.Service;

import org.miniboot.app.domain.models.CustomerRecord;
import org.miniboot.app.domain.repo.PatientAndPrescription.CustomerRecordRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerRecordService {
    private final CustomerRecordRepository customerRecordRepository;
    public CustomerRecordService(CustomerRecordRepository customerRecordRepository) {
        this.customerRecordRepository = customerRecordRepository;
    }
    public List<CustomerRecord> searchPatientRecords(CustomerSearchCriteria criteria) {
        List<CustomerRecord> res = customerRecordRepository.findAll();
        return res.stream().filter(customerRecord -> matchCriteria(customerRecord, criteria)).collect(Collectors.toList());
    }

    private boolean matchCriteria(CustomerRecord customerRecord, CustomerSearchCriteria criteria) {
        if(criteria.getSearchKey() != null ){

            String lowerKey = criteria.getSearchKey().trim().toLowerCase();
            if(!(customerRecord.getNameCustomer().toLowerCase().contains(lowerKey) ||
                 customerRecord.getPhoneNumber().toLowerCase().equals(lowerKey))){
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
            if(!customerRecord.getGender().equals(criteria.getGender())){
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
