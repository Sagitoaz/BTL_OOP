package org.miniboot.app.domain.repo.PatientAndPrescription;

import org.miniboot.app.Service.CustomerSearchCriteria;
import org.miniboot.app.Service.mappers.CustomerAndPrescription.CustomerMapper;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.controllers.UserController;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.util.CustomerAndPrescriptionConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgreSQLCustomerRecordRepository implements  CustomerRecordRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLCustomerRecordRepository() {
        this.dbConfig = DatabaseConfig.getInstance();

    }
    public Customer save(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        if(customer.getUsername() == null){
            customer.setUsername(customer.getPhone());
            String hashedPassword = UserController.hashPassword(CustomerAndPrescriptionConfig.DEFAULT_PASSWORD);
            customer.setPassword(hashedPassword);
        }

        Customer savedCustomer;
        if(customer.getId() <= 0){
            System.out.println("📝 Inserting new customer: " + customer.getUsername());
            savedCustomer = insertCustomer(customer).orElse(null);
        } else {
            System.out.println("📝 Updating existing customer: " + customer.getId());
            savedCustomer = updateCustomer(customer).orElse(null);
        }

        if(savedCustomer == null) {
            throw new RuntimeException("Failed to save customer to database");
        }

        return savedCustomer;
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
            pstmt.setString(8, customer.getGender().name());
            pstmt.setString(9, customer.getAddress());
            pstmt.setString(10, customer.getNote());
            pstmt.setTimestamp(11, Timestamp.valueOf(customer.getCreatedAt()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("❌ Insert failed: No rows affected");
                throw new SQLException("Inserting customer failed, no rows affected.");
            }

            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                if(generatedKeys.next()){
                    customer.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Customer inserted successfully with ID: " + customer.getId());
                } else {
                    System.err.println("❌ Insert failed: No ID generated");
                    throw new SQLException("Inserting customer failed, no ID obtained.");
                }
            }
        }
        catch (Exception e){
            System.err.println("❌ Error inserting Customer: " + e.getMessage());
            e.printStackTrace();
            // QUAN TRỌNG: Throw exception để controller biết có lỗi
            throw new RuntimeException("Database insert failed: " + e.getMessage(), e);
        }
        System.out.println("✅ Customer save completed successfully");
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
            pstmt.setInt(12, customer.getId());
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
                    String hashedPassword = UserController.hashPassword(CustomerAndPrescriptionConfig.DEFAULT_PASSWORD);
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

        return customers;
    }
    public List<Customer> findByFilterAll(CustomerSearchCriteria criteria) {
        List<Customer> customers = new ArrayList<>();

        // Xây dựng SQL query động dựa trên criteria có giá trị
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM customers WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        // Thêm điều kiện search key (firstname, lastname, phone, id)
        if (criteria.getSearchKey() != null && !criteria.getSearchKey().trim().isEmpty()) {
            sqlQuery.append(" AND (LOWER(firstname) LIKE LOWER(?) OR LOWER(lastname) LIKE LOWER(?) OR phone LIKE ? OR id = ?)");
            String searchPattern = "%" + criteria.getSearchKey().trim() + "%";
            parameters.add(searchPattern); // firstname
            parameters.add(searchPattern); // lastname
            parameters.add(criteria.getSearchKey().trim()); // phone

            // Thử parse thành int cho id, nếu không được thì set -1
            try {
                parameters.add(Integer.parseInt(criteria.getSearchKey().trim())); // id
            } catch (NumberFormatException e) {
                parameters.add(-1); // ID không hợp lệ
            }
        }

        // Thêm điều kiện gender
        if (criteria.getGender() != null) {
            sqlQuery.append(" AND UPPER(gender) = UPPER(?)");
            parameters.add(criteria.getGender().name());
        }

        // Thêm điều kiện date from
        if (criteria.getDateFrom() != null) {
            sqlQuery.append(" AND dob >= ?");
            parameters.add(Date.valueOf(criteria.getDateFrom()));
        }

        // Thêm điều kiện date to
        if (criteria.getDateTo() != null) {
            sqlQuery.append(" AND dob <= ?");
            parameters.add(Date.valueOf(criteria.getDateTo()));
        }

        System.out.println("🔍 SQL Query: " + sqlQuery.toString());
        System.out.println("🔍 Parameters: " + parameters);

        try (Connection conn = dbConfig.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery.toString());

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Date) {
                    pstmt.setDate(i + 1, (Date) param);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                customers.add(CustomerMapper.mapResultSetToCustomer(rs));
            }

            System.out.println("✅ Found " + customers.size() + " customers matching criteria");

        } catch (Exception e) {
            System.err.println("❌ Error findByFilter Customer: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Trả về empty list thay vì null
        }

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

    @Override
    public Optional<Customer> findByPhone(String phone) {
        String sqlQuery = "SELECT * FROM customers WHERE phone = ? LIMIT 1;";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding customer by phone: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sqlQuery = "SELECT * FROM customers WHERE email = ? LIMIT 1;";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding customer by email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}
