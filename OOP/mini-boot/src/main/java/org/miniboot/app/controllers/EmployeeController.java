package org.miniboot.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.repo.Employee.PostgreSQLEmployeeRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.ExtractHelper;
import org.miniboot.app.util.Json;

public class EmployeeController {
     private final PostgreSQLEmployeeRepository repository;

     public EmployeeController(PostgreSQLEmployeeRepository repository) {
          this.repository = repository;
     }

     public static void mount(Router router, PostgreSQLEmployeeRepository repository) {
          EmployeeController ec = new EmployeeController(repository);

          router.get("/employees", ec.getAllEmployees()); // GET /employees hoặc /employees?id=1
          router.post("/employees", ec.createEmployee());
          router.put("/employees", ec.updateEmployee()); // PUT /employees?id=1
          router.delete("/employees", ec.deleteEmployee()); // DELETE /employees?id=1
          router.get("/employees/search", ec.searchEmployees()); // GET /employees/search?keyword=john
          router.get("/employees/role", ec.getEmployeesByRole()); // GET /employees/role?role=doctor

          System.out.println("✅ Mounted EmployeeController with 6 endpoints");
     }

     private Function<HttpRequest, HttpResponse> getAllEmployees() {
          return (HttpRequest req) -> {
               try {
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    // Nếu có ?id=123 thì lấy 1 employee cụ thể
                    if (idOpt.isPresent()) {
                         Optional<Employee> employee = repository.findById(idOpt.get());
                         if (employee.isPresent()) {
                              return Json.ok(employee.get());
                         } else {
                              return Json.error(404, "Employee not found with ID: " + idOpt.get());
                         }
                    }

                    // Không có ?id thì lấy tất cả
                    System.out.println("🔄 Fetching all employees...");
                    List<Employee> employees = repository.findAll();
                    System.out.println("✅ Found " + employees.size() + " employees");
                    return Json.ok(employees);

               } catch (Exception e) {
                    System.err.println("❌ ERROR in getAllEmployees(): " + e.getMessage());
                    e.printStackTrace();
                    return Json.error(500, "Error fetching employees: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> createEmployee() {
          return (HttpRequest req) -> {
               try {
                    String body = req.bodyText();
                    Map<String, Object> data = Json.parseMap(body);

                    // Validate required fields
                    String username = (String) data.get("username");
                    String password = (String) data.get("password");
                    String firstname = (String) data.get("firstname");
                    String lastname = (String) data.get("lastname");
                    String role = (String) data.get("role");
                    String email = (String) data.get("email");

                    if (username == null || password == null || firstname == null ||
                              lastname == null || role == null || email == null) {
                         return Json.error(400, "Missing required fields");
                    }

                    // Validate username length
                    if (username.length() < 3) {
                         return Json.error(400, "Username must be at least 3 characters");
                    }

                    // Validate password length
                    if (password.length() < 6) {
                         return Json.error(400, "Password must be at least 6 characters");
                    }

                    // Check duplicate username
                    if (repository.findByUserName(username).isPresent()) {
                         return Json.error(409, "Username already exists: " + username);
                    }

                    // Check duplicate email
                    if (repository.findByEmail(email).isPresent()) {
                         return Json.error(409, "Email already exists: " + email);
                    }

                    // Validate license_no for doctors
                    String licenseNo = (String) data.get("licenseNo");
                    if ("doctor".equalsIgnoreCase(role) && (licenseNo == null || licenseNo.trim().isEmpty())) {
                         return Json.error(400, "License number is required for doctors");
                    }

                    // Create employee object
                    Employee employee = new Employee();
                    employee.setUsername(username);
                    employee.setPassword(password); // Will be hashed in repository
                    employee.setFirstname(firstname);
                    employee.setLastname(lastname);
                    employee.setEmployeeRole(role);
                    employee.setLicenseNo(licenseNo);
                    employee.setEmail(email);
                    employee.setPhone((String) data.get("phone"));
                    employee.setAvatar((String) data.get("avatar"));
                    employee.setActive(data.containsKey("active") ? (Boolean) data.get("active") : true);

                    // Save to database
                    Employee saved = repository.save(employee);

                    System.out.println("✅ Created employee ID: " + saved.getId());
                    return Json.ok(Map.of(
                              "message", "Employee created successfully",
                              "employee", saved));

               } catch (Exception e) {
                    System.err.println("❌ ERROR in createEmployee(): " + e.getMessage());
                    e.printStackTrace();
                    return Json.error(500, "Error creating employee: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> updateEmployee() {
          return (HttpRequest req) -> {
               try {
                    // Lấy id từ query param: PUT /employees?id=123
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    if (idOpt.isEmpty()) {
                         return Json.error(400, "Missing employee ID in query parameter");
                    }

                    int id = idOpt.get();
                    Optional<Employee> existingOpt = repository.findById(id);

                    if (existingOpt.isEmpty()) {
                         return Json.error(404, "Employee not found with ID: " + id);
                    }

                    Employee employee = existingOpt.get();

                    // Parse update data
                    String body = req.bodyText();
                    Map<String, Object> data = Json.parseMap(body);

                    // Update fields (chỉ update những field được gửi lên)
                    if (data.containsKey("firstname")) {
                         employee.setFirstname((String) data.get("firstname"));
                    }
                    if (data.containsKey("lastname")) {
                         employee.setLastname((String) data.get("lastname"));
                    }
                    if (data.containsKey("role")) {
                         String newRole = (String) data.get("role");
                         employee.setEmployeeRole(newRole);

                         // Validate license_no nếu đổi sang doctor
                         if ("doctor".equalsIgnoreCase(newRole)) {
                              String licenseNo = data.containsKey("licenseNo") ? (String) data.get("licenseNo")
                                        : employee.getLicenseNo();
                              if (licenseNo == null || licenseNo.trim().isEmpty()) {
                                   return Json.error(400, "License number required for doctors");
                              }
                         }
                    }
                    if (data.containsKey("licenseNo")) {
                         employee.setLicenseNo((String) data.get("licenseNo"));
                    }
                    if (data.containsKey("email")) {
                         String newEmail = (String) data.get("email");
                         // Check duplicate email (nếu khác email hiện tại)
                         if (!newEmail.equals(employee.getEmail())) {
                              if (repository.findByEmail(newEmail).isPresent()) {
                                   return Json.error(409, "Email already exists: " + newEmail);
                              }
                         }
                         employee.setEmail(newEmail);
                    }
                    if (data.containsKey("phone")) {
                         employee.setPhone((String) data.get("phone"));
                    }
                    if (data.containsKey("avatar")) {
                         employee.setAvatar((String) data.get("avatar"));
                    }
                    if (data.containsKey("active")) {
                         employee.setActive((Boolean) data.get("active"));
                    }

                    // Save updates
                    Employee updated = repository.save(employee);

                    System.out.println("✅ Updated employee ID: " + updated.getId());
                    return Json.ok(Map.of(
                              "message", "Employee updated successfully",
                              "employee", updated));

               } catch (NumberFormatException e) {
                    return Json.error(400, "Invalid employee ID format");
               } catch (Exception e) {
                    System.err.println("❌ ERROR in updateEmployee(): " + e.getMessage());
                    return Json.error(500, "Error updating employee: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> deleteEmployee() {
          return (HttpRequest req) -> {
               try {
                    // Lấy id từ query param: DELETE /employees?id=123
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    if (idOpt.isEmpty()) {
                         return Json.error(400, "Missing employee ID in query parameter");
                    }

                    int id = idOpt.get();
                    boolean deleted = repository.deleteById(id);

                    if (deleted) {
                         System.out.println("✅ Deleted employee ID: " + id);
                         return Json.ok(Map.of("message", "Employee deleted successfully"));
                    } else {
                         return Json.error(404, "Employee not found with ID: " + id);
                    }

               } catch (NumberFormatException e) {
                    return Json.error(400, "Invalid employee ID format");
               } catch (Exception e) {
                    System.err.println("❌ ERROR in deleteEmployee(): " + e.getMessage());
                    return Json.error(500, "Error deleting employee: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> searchEmployees() {
          return (HttpRequest req) -> {
               try {
                    // Lấy keyword từ query param: GET /employees/search?keyword=john
                    Map<String, List<String>> q = req.query;
                    Optional<String> keywordOpt = ExtractHelper.extractString(q, "keyword");

                    // Nếu không có keyword hoặc keyword rỗng, trả về phản hồi rỗng
                    if (keywordOpt.isEmpty() || keywordOpt.get().trim().isEmpty()) {
                         return Json.ok(List.of()); // Trả về danh sách rỗng
                    }

                    String keyword = keywordOpt.get().trim();
                    List<Employee> employees = repository.search(keyword);

                    // Nếu không tìm thấy kết quả, trả về danh sách rỗng
                    if (employees.isEmpty()) {
                         return Json.ok(List.of());
                    }

                    System.out.println("🔍 Search '" + keyword + "' found " + employees.size() + " results");
                    return Json.ok(employees);

               } catch (Exception e) {
                    System.err.println("❌ ERROR in searchEmployees(): " + e.getMessage());
                    return Json.error(500, "Error searching employees: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> getEmployeesByRole() {
          return (HttpRequest req) -> {
               try {
                    // Lấy role từ query param: GET /employees/role?role=doctor
                    Map<String, List<String>> q = req.query;
                    Optional<String> roleOpt = ExtractHelper.extractString(q, "role");

                    if (roleOpt.isEmpty() || roleOpt.get().trim().isEmpty()) {
                         return Json.error(400, "Missing role parameter");
                    }

                    String role = roleOpt.get();
                    if (!role.equalsIgnoreCase("doctor") && !role.equalsIgnoreCase("nurse")) {
                         return Json.error(400, "Invalid role. Must be 'doctor' or 'nurse'");
                    }

                    List<Employee> employees = repository.findByRole(role);
                    System.out.println("👥 Found " + employees.size() + " " + role + "s");
                    return Json.ok(employees);

               } catch (Exception e) {
                    System.err.println("❌ ERROR in getEmployeesByRole(): " + e.getMessage());
                    return Json.error(500, "Error fetching employees by role: " + e.getMessage());
               }
          };
     }
}