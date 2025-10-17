package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.config.DatabaseConfig;

public class PostgreSQLCustomerRecordRepository{
    private final DatabaseConfig dbConfig;
    public PostgreSQLCustomerRecordRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
}
