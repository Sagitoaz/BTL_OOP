package org.miniboot.app.Service;

import org.miniboot.app.domain.models.PatientRecord;
import org.miniboot.app.domain.repo.PatientAndPrescription.PatientRecordRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatientRecordService {
    private final PatientRecordRepository patientRecordRepository;
    public PatientRecordService(PatientRecordRepository patientRecordRepository) {
        this.patientRecordRepository = patientRecordRepository;
    }
    public List<PatientRecord> searchPatientRecords(PatientSearchCriteria criteria) {
        List<PatientRecord> res = patientRecordRepository.findAll();
        return res.stream().filter(patientRecord -> matchCriteria(patientRecord, criteria)).collect(Collectors.toList());
    }

    private boolean matchCriteria(PatientRecord patientRecord, PatientSearchCriteria criteria) {
        if(criteria.getSearchKey() != null || criteria.isEmpty()){

            String lowerKey = criteria.getSearchKey().trim().toLowerCase();
            if(!(patientRecord.getNamePatient().toLowerCase().contains(lowerKey) ||
                 patientRecord.getPhoneNumber().toLowerCase().contains(lowerKey))){
                try{
                    int id = Integer.parseInt(lowerKey);
                    if(patientRecord.getId() != id){
                        return false;
                    }
                }
                catch (Exception e){
                    return false;
                }
            }

        }
        if(criteria.getGender() != null){
            if(patientRecord.getGender() != criteria.getGender()){
                return false;
            }
        }
        if(criteria.getDateFrom() != null){
            if(patientRecord.getDob().isBefore(criteria.getDateFrom())){
                return false;
            }
        }
        if(criteria.getDateTo() != null){
            if(patientRecord.getDob().isAfter(criteria.getDateTo())){
                return false;
            }
        }
        return true;
    }

}
