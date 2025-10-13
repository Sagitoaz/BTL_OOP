package org.miniboot.app.Service;

import org.miniboot.app.domain.models.PatientRecord;

import java.time.LocalDate;

public class PatientSearchCriteria {
    private String searchKey;
    private PatientRecord.Gender gender;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public PatientSearchCriteria(String searchKey, PatientRecord.Gender gender
                                 , LocalDate dateFrom, LocalDate dateTo) {
        this.searchKey = searchKey;
        this.gender = gender;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;

    }

    public String getSearchKey() {
        return searchKey;
    }
    public PatientRecord.Gender getGender() {
        return gender;
    }
    public LocalDate getDateFrom() {
        return dateFrom;
    }
    public LocalDate getDateTo() {
        return dateTo;
    }
    public boolean isEmpty() {
        return (searchKey == null || searchKey.isEmpty()) && (gender == null || dateFrom == null || dateTo == null);
    }
}
