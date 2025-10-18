package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.Service.mappers.CustomerAndPrescription.CustomerMapper;
import org.miniboot.app.auth.PasswordService;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.controllers.UserController;
import org.miniboot.app.domain.models.Customer;
import org.miniboot.app.util.CustomerConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
            System.err.println("❌ Error inserting Customer: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
        System.out.println("OK");
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
            System.err.println("❌ Error updating Customer: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
        System.out.println("OK");
        return Optional.of(customer);
    }
    public void saveAll(List<Customer> customers) {
        String sqlQuery = "INSERT INTO customers (username, password, firstname, lastname, "+
                "phone, email, dob, gender, address, note, created_at) "+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            for(Customer customer : customers){
                LocalDateTime now = LocalDateTime.now();
                customer.setCreatedAt(now);
                if(customer.getUsername() == null){
                    customer.setUsername(customer.getPhone());
                    String hashedPassword = UserController.hashPassword(CustomerConfig.DEFAULT_PASSWORD);
                    customer.setPassword(hashedPassword);
                }
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
                pstmt.addBatch();
            }

            int[] affectedRows = pstmt.executeBatch();
            if (affectedRows.length == 0) {
                throw new Exception("save customer failed, no rows affected.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error save Custommer: " + e.getMessage());
            e.printStackTrace();
            return;

        }
        System.out.println("OK");

    }
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sqlQuery = "SELECT * FROM customers;";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                customers.add(CustomerMapper.mapResultSetToCustomer(rs));
            }
        }
        catch (Exception e){
            System.err.println("❌ Error findAll Custommer: " + e.getMessage());
            e.printStackTrace();
            return null;

        }
        System.out.println("OK");
        return customers;
    }
    public List<Customer> findByFilterAll(CustomerSearchCriteria criteria) {
        List<Customer> customers = new ArrayList<>();
        String sqlQuery = "SELECT * FROM customers "+
                "WHERE (LOWER(firstname) LIKE LOWER('%'||?||'%') OR LOWER(lastname) LIKE LOWER('%'||?||'%') "+
                "OR phone LIKE (?) OR id = ?) "+
                "AND (gender = ? OR ? IS NULL) "+
                "AND (dob >= ? OR ? IS NULL) "+
                "AND (dob <= ? OR ? IS NULL);";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setString(1, criteria.getSearchKey());
            pstmt.setString(2, criteria.getSearchKey());
            pstmt.setString(3, criteria.getSearchKey());
            if(criteria.getSearchKey() != null){
                try{
                    pstmt.setInt(4, Integer.parseInt(criteria.getSearchKey()));
                }
                catch (NumberFormatException e){
                    pstmt.setNull(4, Types.INTEGER);
                }
            }
            else{
                pstmt.setNull(4, Types.INTEGER);
            }
            if(criteria.getGender() != null){
                pstmt.setString(5, criteria.getGender().name());
                pstmt.setString(6, criteria.getGender().name());
            }
            else{
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
            }
            if(criteria.getDateFrom() != null){
                pstmt.setDate(7, Date.valueOf(criteria.getDateFrom()));
                pstmt.setDate(8, Date.valueOf(criteria.getDateFrom()));
            }
            else{
                pstmt.setNull(7, Types.DATE);
                pstmt.setNull(8, Types.DATE);
            }
            if(criteria.getDateTo() != null){
                pstmt.setDate(9, Date.valueOf(criteria.getDateTo()));
                pstmt.setDate(10, Date.valueOf(criteria.getDateTo()));
            }
            else{
                pstmt.setNull(9, Types.DATE);
                pstmt.setNull(10, Types.DATE);
            }
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                customers.add(CustomerMapper.mapResultSetToCustomer(rs));
            }
        } catch (Exception e) {
            System.err.println("❌ Error findByFilter Custommer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        System.out.println("OK");
        return customers;
    }
    public boolean deleteById(int id) {
        String sqlQuery = "DELETE FROM customers WHERE id = ?;";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
        catch (Exception e){
            System.err.println("❌ Error delete Customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean existsById(int id){
        String sqlQuery = "SELECT COUNT(*) FROM customers WHERE id = ?;";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                int count = rs.getInt(1);
                return count > 0;
            }
        }
        catch (Exception e){
            System.err.println("❌ Error exist Customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }
    public long count(){
        String sqlQuery = "SELECT COUNT(*) FROM customers;";
        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getLong(1);
            }
        }
        catch (Exception e){
            System.err.println("❌ Error count Customer: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
        return 0;
    }



}
