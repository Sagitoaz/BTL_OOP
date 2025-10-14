package org.miniboot.app.Service;

import org.miniboot.app.domain.models.CustomerRecord;

import java.time.LocalDate;

public class CustomerSearchCriteria {
    private String searchKey;
    private CustomerRecord.Gender gender;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public CustomerSearchCriteria(String searchKey, CustomerRecord.Gender gender
                                 , LocalDate dateFrom, LocalDate dateTo) {
        this.searchKey = searchKey;
        this.gender = gender;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;

    }

    public String getSearchKey() {
        return searchKey;
    }
    public CustomerRecord.Gender getGender() {
        return gender;
    }
    public LocalDate getDateFrom() {
        return dateFrom;
    }
    public LocalDate getDateTo() {
        return dateTo;
    }
    public boolean isEmpty() {
        return (searchKey == null || searchKey.isEmpty()) && (gender == null && dateFrom == null && dateTo == null);
    }
}
