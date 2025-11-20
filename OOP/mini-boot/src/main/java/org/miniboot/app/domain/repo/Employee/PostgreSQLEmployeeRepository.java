package org.miniboot.app.domain.repo.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.auth.PasswordService;
import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Employee;

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
               System.err.println("❌ SQL ERROR in findAll(): " + e.getMessage());
               e.printStackTrace();
               throw new RuntimeException(e);
          }
          return employees;
     }

     @Override
     public Optional<Employee> findById(int id) {
          String sql = "SELECT * FROM employees WHERE id = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setInt(1, id);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
                         return Optional.of(mapResultSetToEmployee(rs));
               }
          } catch (SQLException e) {
               System.err.println("❌ Error finding employee by ID: " + e.getMessage());
          }
          return Optional.empty();
     }

     @Override
     public String findPasswordByUsernameOrEmail(String username) {
          String sql = "SELECT password FROM employees WHERE username = ? or email = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setString(1, username);
               pstmt.setString(2, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
                         return rs.getString("password");
               }
          } catch (SQLException e) {
               System.err.println("❌ Error finding employee by ID: " + e.getMessage());
          }
          return null;
     }

     @Override
     public Optional<Employee> findByUserName(String username) {
          String sql = "SELECT * FROM employees WHERE username = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setString(1, username);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
                         return Optional.of(mapResultSetToEmployee(rs));
               }
          } catch (SQLException e) {
               System.err.println("❌ Error finding by username: " + e.getMessage());
          }
          return Optional.empty();
     }

     @Override
     public Optional<Employee> findByEmail(String email) {
          String sql = "SELECT * FROM employees WHERE email = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setString(1, email);
               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
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

     /** INSERT EMPLOYEE */
     private Employee insert(Employee employee) {
          String sql = """
                        INSERT INTO employees (
                            username, password, firstname, lastname, gender,
                            avatar, role, license_no, email, phone, is_active
                        )
                        VALUES (?, ?, ?, ?, ?::gender_enum, ?, ?::employee_role, ?, ?, ?, ?)
                        RETURNING id
                    """;
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               requireNonBlank(employee.getUsername(), "username");
               requireNonBlank(employee.getPassword(), "password");
               requireNonBlank(employee.getFirstname(), "firstname");
               requireNonBlank(employee.getLastname(), "lastname");
               requireNonBlank(employee.getRole(), "role");

               String roleLower = employee.getRole().toLowerCase();
               if ("doctor".equals(roleLower)) {
                    requireNonBlank(employee.getLicenseNo(), "license_no (bắt buộc với bác sĩ)");
               }

               // ✅ Hash password using SHA-256 with salt (same format as signup)
               String hashedPassword = PasswordService.hashPasswordWithSalt(employee.getPassword());

               pstmt.setString(1, employee.getUsername());
               pstmt.setString(2, hashedPassword);
               pstmt.setString(3, employee.getFirstname());
               pstmt.setString(4, employee.getLastname());

               // Gender - parameter 5
               if (employee.getGender() != null && !employee.getGender().isBlank())
                    pstmt.setString(5, employee.getGender().toUpperCase());
               else
                    pstmt.setNull(5, java.sql.Types.VARCHAR);

               // Avatar - parameter 6
               if (employee.getAvatar() != null && !employee.getAvatar().isBlank())
                    pstmt.setString(6, employee.getAvatar());
               else
                    pstmt.setNull(6, java.sql.Types.VARCHAR);

               // Role - parameter 7
               pstmt.setString(7, roleLower);

               // License No - parameter 8
               if (employee.getLicenseNo() != null && !employee.getLicenseNo().isBlank())
                    pstmt.setString(8, employee.getLicenseNo());
               else
                    pstmt.setNull(8, java.sql.Types.VARCHAR);

               // Email - parameter 9
               if (employee.getEmail() != null && !employee.getEmail().isBlank())
                    pstmt.setString(9, employee.getEmail());
               else
                    pstmt.setNull(9, java.sql.Types.VARCHAR);

               // Phone - parameter 10
               if (employee.getPhone() != null && !employee.getPhone().isBlank())
                    pstmt.setString(10, employee.getPhone());
               else
                    pstmt.setNull(10, java.sql.Types.VARCHAR);

               // Active - parameter 11
               pstmt.setBoolean(11, employee.isActive());

               try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
                         employee.setId(rs.getInt("id"));
               }
               return employee;

          } catch (SQLException e) {
               System.err.println("❌ SQLSTATE=" + e.getSQLState() + " CODE=" + e.getErrorCode());
               System.err.println("❌ MESSAGE=" + e.getMessage());
               throw new RuntimeException("Failed to insert employee", e);
          }
     }

     private static void requireNonBlank(String s, String field) {
          if (s == null || s.isBlank())
               throw new IllegalArgumentException(field + " không được để trống");
     }

     /** UPDATE EMPLOYEE */
     private Employee update(Employee employee) {
          String sql = """
                        UPDATE employees
                        SET
                            firstname = ?,
                            lastname = ?,
                            gender = ?::gender_enum,
                            avatar = ?,
                            role = ?::employee_role,
                            license_no = ?,
                            email = ?,
                            phone = ?,
                            is_active = ?
                        WHERE id = ?
                    """;
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

               if (employee.getFirstname() == null)
                    employee.setFirstname("");
               if (employee.getLastname() == null)
                    employee.setLastname("");

               pstmt.setString(1, employee.getFirstname());
               pstmt.setString(2, employee.getLastname());

               // Gender - parameter 3
               if (employee.getGender() != null && !employee.getGender().isBlank())
                    pstmt.setString(3, employee.getGender().toUpperCase());
               else
                    pstmt.setNull(3, java.sql.Types.VARCHAR);

               // Avatar - parameter 4
               if (employee.getAvatar() != null && !employee.getAvatar().isBlank())
                    pstmt.setString(4, employee.getAvatar());
               else
                    pstmt.setNull(4, java.sql.Types.VARCHAR);

               // Role - parameter 5
               pstmt.setString(5, employee.getRole().toLowerCase());

               // License No - parameter 6
               if (employee.getLicenseNo() != null && !employee.getLicenseNo().isBlank())
                    pstmt.setString(6, employee.getLicenseNo());
               else
                    pstmt.setNull(6, java.sql.Types.VARCHAR);

               // Email - parameter 7
               if (employee.getEmail() != null && !employee.getEmail().isBlank())
                    pstmt.setString(7, employee.getEmail());
               else
                    pstmt.setNull(7, java.sql.Types.VARCHAR);

               // Phone - parameter 8
               if (employee.getPhone() != null && !employee.getPhone().isBlank())
                    pstmt.setString(8, employee.getPhone());
               else
                    pstmt.setNull(8, java.sql.Types.VARCHAR);

               // Active - parameter 9
               pstmt.setBoolean(9, employee.isActive());

               // ID - parameter 10
               pstmt.setInt(10, employee.getId());

               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected == 0) {
                    throw new SQLException("Không tìm thấy employee ID=" + employee.getId());
               }

               System.out.println("✅ Updated employee ID: " + employee.getId());
               return employee;

          } catch (SQLException e) {
               System.err.println("❌ Error updating employee: " + e.getMessage());
               throw new RuntimeException("Failed to update employee", e);
          }
     }

     /** SOFT DELETE (is_active = FALSE) */
     @Override
     public boolean deleteById(int id) {
          String sql = "UPDATE employees SET is_active = FALSE WHERE id = ?";
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

     /** SEARCH */
     public List<Employee> search(String keyword) {
          List<Employee> employees = new ArrayList<>();
          String sql = """
                        SELECT *
                        FROM employees
                        WHERE is_active = TRUE
                          AND (username ILIKE ? OR firstname ILIKE ? OR lastname ILIKE ? OR email ILIKE ?)
                    """;
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               String pattern = "%" + keyword + "%";
               pstmt.setString(1, pattern);
               pstmt.setString(2, pattern);
               pstmt.setString(3, pattern);
               pstmt.setString(4, pattern);
               try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                         employees.add(mapResultSetToEmployee(rs));
                    }
               }
          } catch (SQLException e) {
               System.err.println("❌ Error searching employees: " + e.getMessage());
          }
          return employees;
     }

     /** FIND BY ROLE (doctor/nurse) */
     public List<Employee> findByRole(String role) {
          List<Employee> employees = new ArrayList<>();
          String sql = """
                        SELECT *
                        FROM employees
                        WHERE is_active = TRUE
                          AND role::text = ?
                        ORDER BY id
                    """;

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setString(1, role.toLowerCase());
               try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                         employees.add(mapResultSetToEmployee(rs));
                    }
               }
          } catch (SQLException e) {
               System.err.println("❌ Error finding by role: " + e.getMessage());
          }
          return employees;
     }

     /** MAP DATABASE ROW TO MODEL */
     private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
          Employee emp = new Employee();
          emp.setId(rs.getInt("id"));
          emp.setUsername(rs.getString("username"));
          emp.setFirstname(rs.getString("firstname"));
          emp.setLastname(rs.getString("lastname"));
          emp.setGender(rs.getString("gender"));
          emp.setAvatar(rs.getString("avatar"));
          emp.setRole(rs.getString("role"));
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

     /** CHANGE PASSWORD */
     @Override
     public boolean changePassword(String usernameOrEmail, String newPasswordHash) {
          String sql = "UPDATE employees SET password = ? WHERE username = ? OR email = ?";
          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
               pstmt.setString(1, newPasswordHash);
               pstmt.setString(2, usernameOrEmail);
               pstmt.setString(3, usernameOrEmail);
               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected > 0) {
                    System.out.println("✅ Password changed for user: " + usernameOrEmail);
                    return true;
               }
          } catch (SQLException e) {
               System.err.println("❌ Error changing password: " + e.getMessage());
          }
          return false;
     }
}

