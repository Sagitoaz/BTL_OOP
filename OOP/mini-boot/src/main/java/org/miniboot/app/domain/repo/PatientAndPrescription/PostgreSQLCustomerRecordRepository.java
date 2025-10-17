package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.auth.PasswordService;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.controllers.UserController;
import org.miniboot.app.domain.models.Customer;
import org.miniboot.app.util.CustomerConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PostgreSQLCustomerRecordRepository implements  CustomerRecordRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLCustomerRecordRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;

    }
    public Customer save(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        if(customer.getUsername() == null){
            customer.setUsername(customer.getPhone());
            String hashedPassword = UserController.hashPassword(CustomerConfig.DEFAULT_PASSWORD);
            customer.setPassword(hashedPassword);
        }
        if(customer.getId() <= 0){
            return insertCustomer(customer).orElse(null);
        }
        else{
            return updateCustomer(customer).orElse(null);
        }
    }
    private Optional<Customer> insertCustomer(Customer customer) {
        String sqlQuery = "INSERT INTO customers (username, password, firstname, lastname, "+
                "phone, email, dob, gender, address, note, created_at) "+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, customer.getUsername());
            pstmt.setString(2, customer.getPassword());
            pstmt.setString(3, customer.getFirstname());
            pstmt.setString(4, customer.getLastname());
            pstmt.setString(5, customer.getPhone());
            pstmt.setString(6, customer.getEmail());
            pstmt.setDate(7, Date.valueOf(customer.getDob()));
            pstmt.setString(8, customer.getGender().toString());
            pstmt.setString(9, customer.getAddress());
            pstmt.setString(10, customer.getNote());
            pstmt.setTimestamp(11, Timestamp.valueOf(customer.getCreatedAt()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("Inserting customer failed, no rows affected.");
            }
            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                if(generatedKeys.next()){
                    customer.setId(generatedKeys.getInt(1));
                }
                else{
                    throw new Exception("Inserting customer failed, no ID obtained.");
                }
            }
        }
        catch (Exception e){
            System.err.println("❌ Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(customer);
    }
    private Optional<Customer> updateCustomer(Customer customer) {
        String sqlQuery = "UPDATE customers SET username=?, password=?, firstname=?, lastname=?, "+
                "phone=?, email=?, dob=?, gender=?, address=?, note=?, created_at=? "+
                "WHERE id=?;";
        try (Connection conn = dbConfig.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setString(1, customer.getUsername());
            pstmt.setString(2, customer.getPassword());
            pstmt.setString(3, customer.getFirstname());
            pstmt.setString(4, customer.getLastname());
            pstmt.setString(5, customer.getPhone());
            pstmt.setString(6, customer.getEmail());
            pstmt.setDate(7, Date.valueOf(customer.getDob()));
            pstmt.setString(8, customer.getGender().name());
            pstmt.setString(9, customer.getAddress());
            pstmt.setString(10, customer.getNote());
            pstmt.setTimestamp(11, Timestamp.valueOf(customer.getCreatedAt()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("Updating customer failed, no rows affected.");
            }
        }
        catch (Exception e){
            System.err.println("❌ Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(customer);
    }
    public void saveAll(List<Customer> customers) {
        String sqlQuery = "INSERT INTO customers (username, password, firstname, lastname, "+
                "phone, email, dob, gender, address, note, created_at) "+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    }


}
