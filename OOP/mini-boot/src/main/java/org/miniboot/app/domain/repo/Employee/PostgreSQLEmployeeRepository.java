package org.miniboot.app.domain.repo.Employee;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.controllers.UserController;
import org.miniboot.app.domain.models.Employee;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PostgreSQLEmployeeRepository implements EmployeeRepository {
     private final DatabaseConfig dbConfig;

     public PostgreSQLEmployeeRepository() {
          this.dbConfig = DatabaseConfig.getInstance();
     }

     @Override
     public List<Employee> findAll() {
          List<Employee> employees = new ArrayList<>();
          String sql = "SELECT * FROM employees ORDER BY id";
          System.out.println("🔍 Executing SQL: " + sql);
          try (Connection conn = dbConfig.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
               System.out.println("✅ Query executed successfully");
               while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
               }
          } catch (SQLException e) {
               System.err.println("❌ SQL ERROR in findAll():");
               System.err.println("   Message: " + e.getMessage());
               System.err.println("   SQL State: " + e.getSQLState());
               System.err.println("   Error Code: " + e.getErrorCode());
               e.printStackTrace();
               throw new RuntimeException(e);
          }
          return employees;
     }

     @Override
     public Optional<Employee> findById(int id) {
          String sql = "SELECT * FROM employees where id = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);) {
               pstmt.setInt(1, id);
               ResultSet rs = pstmt.executeQuery();
               if (rs.next()) {
                    return Optional.of(mapResultSetToEmployee(rs));
               }
          } catch (SQLException e) {
               System.err.println("❌ Error finding employee by ID: " + e.getMessage());
          }
          return Optional.empty();
     }

     @Override
     public Optional<Employee> findByUserName(String username) {
          String sql = "SELECT * FROM Employees WHERE username = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, username);
               ResultSet rs = pstmt.executeQuery();

               if (rs.next()) {
                    return Optional.of(mapResultSetToEmployee(rs));
               }

          } catch (SQLException e) {
               System.err.println("❌ Error finding by username: " + e.getMessage());
          }

          return Optional.empty();
     }

     @Override
     public Optional<Employee> findByEmail(String email) {
          String sql = "SELECT * FROM Employees WHERE email = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, email);
               ResultSet rs = pstmt.executeQuery();

               if (rs.next()) {
                    return Optional.of(mapResultSetToEmployee(rs));
               }

          } catch (SQLException e) {
               System.err.println("❌ Error finding by email: " + e.getMessage());
          }

          return Optional.empty();
     }

     @Override
     public Employee save(Employee employee) {
          if (employee.getId() == 0) {
               return insert(employee);
          } else {
               return update(employee);
          }
     }

     private Employee insert(Employee employee) {
          String sql = "INSERT INTO Employees (username, password, firstname, lastname, " +
                    "avatar, role, license_no, email, phone, is_active, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               // Hash password trước khi lưu
               String hashedPassword = hashPassword(employee.getPassword());

               pstmt.setString(1, employee.getUsername());
               pstmt.setString(2, hashedPassword);
               pstmt.setString(3, employee.getFirstname());
               pstmt.setString(4, employee.getLastname());
               pstmt.setString(5, employee.getAvatar());
               pstmt.setString(6, employee.getEmployeeRole());
               pstmt.setString(7, employee.getLicenseNo());
               pstmt.setString(8, employee.getEmail());
               pstmt.setString(9, employee.getPhone());
               pstmt.setBoolean(10, employee.isActive());

               ResultSet rs = pstmt.executeQuery();
               if (rs.next()) {
                    employee.setId(rs.getInt("id"));
                    System.out.println("✅ Inserted employee ID: " + employee.getId());
               }

          } catch (SQLException e) {
               System.err.println("❌ Error inserting employee: " + e.getMessage());
               throw new RuntimeException("Failed to insert employee", e);
          }

          return employee;
     }

     private Employee update(Employee employee) {
          String sql = "UPDATE Employees SET firstname = ?, lastname = ?, " +
                    "avatar = ?, role = ?, license_no = ?, email = ?, " +
                    "phone = ?, is_active = ? WHERE id = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, employee.getFirstname());
               pstmt.setString(2, employee.getLastname());
               pstmt.setString(3, employee.getAvatar());
               pstmt.setString(4, employee.getEmployeeRole());
               pstmt.setString(5, employee.getLicenseNo());
               pstmt.setString(6, employee.getEmail());
               pstmt.setString(7, employee.getPhone());
               pstmt.setBoolean(8, employee.isActive());
               pstmt.setInt(9, employee.getId());

               int rowsAffected = pstmt.executeUpdate();
               System.out.println("✅ Updated employee ID: " + employee.getId());

          } catch (SQLException e) {
               System.err.println("❌ Error updating employee: " + e.getMessage());
               throw new RuntimeException("Failed to update employee", e);
          }

          return employee;
     }

     @Override
     public boolean deleteById(int id) {
          String sql = "UPDATE Employees SET is_active = FALSE WHERE id = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setInt(1, id);
               int rowsAffected = pstmt.executeUpdate();

               if (rowsAffected > 0) {
                    System.out.println("✅ Deleted employee ID: " + id);
                    return true;
               }

          } catch (SQLException e) {
               System.err.println("❌ Error deleting employee: " + e.getMessage());
          }

          return false;
     }

     public List<Employee> search(String keyword) {
          List<Employee> employees = new ArrayList<>();
          String sql = "SELECT * FROM Employees WHERE is_active = TRUE AND " +
                    "(username ILIKE ? OR firstname ILIKE ? OR lastname ILIKE ? OR email ILIKE ?)";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               String pattern = "%" + keyword + "%";
               pstmt.setString(1, pattern);
               pstmt.setString(2, pattern);
               pstmt.setString(3, pattern);
               pstmt.setString(4, pattern);

               ResultSet rs = pstmt.executeQuery();
               while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
               }

          } catch (SQLException e) {
               System.err.println("❌ Error searching employees: " + e.getMessage());
          }

          return employees;
     }

     public List<Employee> findByRole(String role) {
          List<Employee> employees = new ArrayList<>();
          String sql = "SELECT * FROM Employees WHERE role = ? AND is_active = TRUE";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, role);
               ResultSet rs = pstmt.executeQuery();

               while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
               }

          } catch (SQLException e) {
               System.err.println("❌ Error finding by role: " + e.getMessage());
          }

          return employees;
     }

     private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
          Employee emp = new Employee();
          emp.setId(rs.getInt("id"));
          emp.setUsername(rs.getString("username"));
          // KHÔNG set password vì đã hash
          emp.setFirstname(rs.getString("firstname"));
          emp.setLastname(rs.getString("lastname"));
          emp.setAvatar(rs.getString("avatar"));
          emp.setEmployeeRole(rs.getString("role"));
          emp.setLicenseNo(rs.getString("license_no"));
          emp.setEmail(rs.getString("email"));
          emp.setPhone(rs.getString("phone"));
          emp.setActive(rs.getBoolean("is_active"));
          Timestamp timestamp = rs.getTimestamp("created_at");
          if (timestamp != null) {
               emp.setCreatedAt(timestamp.toLocalDateTime());
          }
          return emp;
     }

     private String hashPassword(String plainPassword) {
          return BCrypt.withDefaults().hashToString(10, plainPassword.toCharArray());
     }
}
