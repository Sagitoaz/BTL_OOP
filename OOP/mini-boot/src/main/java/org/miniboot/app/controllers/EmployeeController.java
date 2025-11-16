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
import org.miniboot.app.util.errorvalidation.ValidationUtils;
import org.miniboot.app.util.errorvalidation.DatabaseErrorHandler;
import org.miniboot.app.util.errorvalidation.RateLimiter;

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
          router.get("/employees/password", ec.findPasswordByUsernameOrEmail()); // GET /employees/password?input=abc
          router.put("/employees/change-password", ec.changePassword()); // PUT /employees/change-password
          System.out.println("✅ Mounted EmployeeController with 8 endpoints");
     }

     private Function<HttpRequest, HttpResponse> getAllEmployees() {
          return (HttpRequest req) -> {
               // Step 0: Rate limiting
               HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
               if (rateLimitError != null)
                    return rateLimitError;

               // Step 1: JWT validation
               HttpResponse jwtError = ValidationUtils.validateJWT(req);
               if (jwtError != null)
                    return jwtError;

               try {
                    Map<String, List<String>> q = req.query;
                    Optional<Integer> idOpt = ExtractHelper.extractInt(q, "id");

                    // Nếu có ?id=123 thì lấy 1 employee cụ thể
                    if (idOpt.isPresent()) {
                         try {
                              Optional<Employee> employee = repository.findById(idOpt.get());
                              if (employee.isPresent()) {
                                   return Json.ok(employee.get());
                              } else {
                                   return ValidationUtils.error(404, "NOT_FOUND",
                                             "Employee not found with ID: " + idOpt.get());
                              }
                         } catch (Exception e) {
                              return DatabaseErrorHandler.handleDatabaseException(e);
                         }
                    }

                    // Không có ?id thì lấy tất cả
                    System.out.println("🔄 Fetching all employees...");
                    try {
                         List<Employee> employees = repository.findAll();
                         System.out.println("✅ Found " + employees.size() + " employees");
                         return Json.ok(employees);
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

               } catch (Exception e) {
                    System.err.println("❌ ERROR in getAllEmployees(): " + e.getMessage());
                    e.printStackTrace();
                    return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                              "Error fetching employees: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> createEmployee() {
          return (HttpRequest req) -> {
               // Step 0: Rate limiting
               HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
               if (rateLimitError != null)
                    return rateLimitError;

               // Step 1-2: Standard validations
               HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
               if (contentTypeError != null)
                    return contentTypeError;

               HttpResponse jwtError = ValidationUtils.validateJWT(req);
               if (jwtError != null)
                    return jwtError;

               // Optional: Admin role check
               // HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
               // if (roleError != null) return roleError;

               try {
                    String body = req.bodyText();
                    Map<String, Object> data;
                    try {
                         data = Json.parseMap(body);
                    } catch (Exception e) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Invalid JSON format: " + e.getMessage());
                    }

                    // Validate required fields
                    String username = (String) data.get("username");
                    String password = (String) data.get("password");
                    String firstname = (String) data.get("firstname");
                    String lastname = (String) data.get("lastname");
                    String role = (String) data.get("role");
                    String email = (String) data.get("email");

                    if (username == null || password == null || firstname == null ||
                              lastname == null || role == null || email == null) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Missing required fields");
                    }

                    // Validate username length
                    if (username.length() < 3) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Username must be at least 3 characters");
                    }

                    // Validate password length
                    if (password.length() < 6) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Password must be at least 6 characters");
                    }

                    // Validate role (422 - Unprocessable Entity)
                    String roleLower = role.toLowerCase();
                    if (!roleLower.equals("doctor") && !roleLower.equals("nurse") &&
                              !roleLower.equals("admin") && !roleLower.equals("receptionist")) {
                         return ValidationUtils.error(422, "INVALID_ROLE",
                                   "Invalid role. Must be: doctor, nurse, admin, or receptionist");
                    }

                    // Check duplicate username (409 Conflict)
                    try {
                         if (repository.findByUserName(username).isPresent()) {
                              return ValidationUtils.error(409, "USERNAME_CONFLICT",
                                        "Username already exists: " + username);
                         }
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    // Check duplicate email (409 Conflict)
                    try {
                         if (repository.findByEmail(email).isPresent()) {
                              return ValidationUtils.error(409, "EMAIL_CONFLICT",
                                        "Email already exists: " + email);
                         }
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    // Check duplicate phone if provided (409 Conflict)
                    String phone = (String) data.get("phone");
                    if (phone != null && !phone.trim().isEmpty()) {
                         // TODO: Add repository.findByPhone() check
                    }

                    // Validate license_no for doctors
                    String licenseNo = (String) data.get("licenseNo");
                    if ("doctor".equalsIgnoreCase(role) && (licenseNo == null || licenseNo.trim().isEmpty())) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "License number is required for doctors");
                    }

                    // Validate active status if provided (422)
                    if (data.containsKey("active")) {
                         Object activeObj = data.get("active");
                         if (!(activeObj instanceof Boolean) &&
                                   !"true".equalsIgnoreCase(String.valueOf(activeObj)) &&
                                   !"false".equalsIgnoreCase(String.valueOf(activeObj))) {
                              return ValidationUtils.error(422, "INVALID_STATUS",
                                        "Invalid active status. Must be true or false");
                         }
                    }

                    // Create employee object
                    Employee employee = new Employee();
                    employee.setUsername(username);
                    employee.setPassword(password); // Will be hashed in repository
                    employee.setFirstname(firstname);
                    employee.setLastname(lastname);
                    employee.setRole(role);
                    employee.setLicenseNo(licenseNo);
                    employee.setEmail(email);
                    employee.setPhone(phone);
                    employee.setAvatar((String) data.get("avatar"));
                    employee.setActive(data.containsKey("active") ? (Boolean) data.get("active") : true);

                    // Save to database
                    Employee saved;
                    try {
                         saved = repository.save(employee);
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    System.out.println("✅ Created employee ID: " + saved.getId());
                    return Json.ok(saved);

               } catch (Exception e) {
                    System.err.println("❌ ERROR in createEmployee(): " + e.getMessage());
                    e.printStackTrace();
                    return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                              "Error creating employee: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> updateEmployee() {
          return (HttpRequest req) -> {
               // Step 0: Rate limiting (429)
               HttpResponse rateLimitError = RateLimiter.checkRateLimit(req);
               if (rateLimitError != null)
                    return rateLimitError;

               // Step 1-2: Standard validations
               HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
               if (contentTypeError != null)
                    return contentTypeError;

               // Step 3: JWT validation (401)
               HttpResponse jwtError = ValidationUtils.validateJWT(req);
               if (jwtError != null)
                    return jwtError;

               // Optional: Admin role check (403)
               // HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
               // if (roleError != null) return roleError;

               try {
                    System.out.println("🔄 Updating employee (read ID from body)...");
                    // Parse JSON body -> Map
                    String bodyText = req.bodyText();
                    Map<String, Object> data;
                    try {
                         data = Json.parseMap(bodyText);
                    } catch (Exception e) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Invalid JSON format: " + e.getMessage());
                    }

                    if (data == null || data.isEmpty()) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Body is empty or invalid JSON");
                    }

                    // Lấy ID từ body (bắt buộc)
                    Object rawId = data.get("id");
                    Integer id = null;
                    if (rawId instanceof Number) {
                         id = ((Number) rawId).intValue();
                    } else if (rawId instanceof String) {
                         try {
                              id = Integer.parseInt(((String) rawId).trim());
                         } catch (Exception ignore) {
                         }
                    }
                    if (id == null || id <= 0) {
                         return ValidationUtils.error(400, "BAD_REQUEST",
                                   "Missing or invalid employee ID in body");
                    }

                    // Tìm employee hiện có (404)
                    Optional<Employee> existingOpt;
                    try {
                         existingOpt = repository.findById(id);
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    if (existingOpt.isEmpty()) {
                         return ValidationUtils.error(404, "NOT_FOUND",
                                   "Employee not found with ID: " + id);
                    }
                    Employee employee = existingOpt.get();

                    // Helper mini để lấy String/Boolean an toàn từ Map
                    java.util.function.Function<String, String> getStr = (k) -> {
                         Object v = data.get(k);
                         return (v == null) ? null : String.valueOf(v);
                    };
                    java.util.function.Function<String, Boolean> getBool = (k) -> {
                         Object v = data.get(k);
                         if (v == null)
                              return null;
                         if (v instanceof Boolean)
                              return (Boolean) v;
                         String s = String.valueOf(v).trim().toLowerCase();
                         if ("true".equals(s) || "1".equals(s) || "yes".equals(s))
                              return true;
                         if ("false".equals(s) || "0".equals(s) || "no".equals(s))
                              return false;
                         return null;
                    };

                    // Update các field chỉ khi client gửi lên
                    if (data.containsKey("firstname"))
                         employee.setFirstname(getStr.apply("firstname"));
                    if (data.containsKey("lastname"))
                         employee.setLastname(getStr.apply("lastname"));
                    if (data.containsKey("avatar"))
                         employee.setAvatar(getStr.apply("avatar"));

                    // Validate role if provided (422 - Unprocessable Entity)
                    if (data.containsKey("role")) {
                         String newRole = getStr.apply("role");
                         if (newRole != null) {
                              String roleLower = newRole.toLowerCase();
                              if (!roleLower.equals("doctor") && !roleLower.equals("nurse") &&
                                        !roleLower.equals("admin") && !roleLower.equals("receptionist")) {
                                   return ValidationUtils.error(422, "INVALID_ROLE",
                                             "Invalid role. Must be: doctor, nurse, admin, or receptionist");
                              }
                              employee.setRole(newRole);
                              // Nếu đổi sang doctor thì bắt buộc licenseNo
                              if ("doctor".equalsIgnoreCase(newRole)) {
                                   String lic = data.containsKey("licenseNo") ? getStr.apply("licenseNo")
                                             : employee.getLicenseNo();
                                   if (lic == null || lic.isBlank()) {
                                        return ValidationUtils.error(400, "BAD_REQUEST",
                                                  "License number required for doctors");
                                   }
                              }
                         }
                    }
                    if (data.containsKey("licenseNo"))
                         employee.setLicenseNo(getStr.apply("licenseNo"));

                    // Check duplicate email (409 Conflict)
                    if (data.containsKey("email")) {
                         String newEmail = getStr.apply("email");
                         if (newEmail != null && !newEmail.equals(employee.getEmail())) {
                              try {
                                   if (repository.findByEmail(newEmail).isPresent()) {
                                        return ValidationUtils.error(409, "EMAIL_CONFLICT",
                                                  "Email already exists: " + newEmail);
                                   }
                              } catch (Exception e) {
                                   return DatabaseErrorHandler.handleDatabaseException(e);
                              }
                         }
                         employee.setEmail(newEmail);
                    }

                    // Check duplicate phone if provided (409 Conflict)
                    if (data.containsKey("phone")) {
                         String newPhone = getStr.apply("phone");
                         if (newPhone != null && !newPhone.equals(employee.getPhone())) {
                              // TODO: Add repository.findByPhone() check for conflict
                         }
                         employee.setPhone(newPhone);
                    }

                    // Validate active status (422 - Unprocessable Entity)
                    if (data.containsKey("active")) {
                         Boolean active = getBool.apply("active");
                         if (active == null) {
                              return ValidationUtils.error(422, "INVALID_STATUS",
                                        "Invalid active status value");
                         }
                         employee.setActive(active);
                    }

                    // Lưu với database error handling
                    Employee updated;
                    try {
                         updated = repository.save(employee);
                    } catch (Exception e) {
                         return DatabaseErrorHandler.handleDatabaseException(e);
                    }

                    System.out.println("✅ Updated employee ID: " + updated.getId());
                    return Json.ok(updated);

               } catch (Exception e) {
                    System.err.println("❌ ERROR in updateEmployee(): " + e.getMessage());
                    return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                              "Error updating employee: " + e.getMessage());
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
                    Optional<String> rawOpt = ExtractHelper.extractString(req.query, "role");
                    String raw = rawOpt.orElse("");
                    String role = raw.trim().toLowerCase();
                    if (!role.equals("doctor") && !role.equals("nurse")) {
                         return Json.error(400, "Vai trò không hợp lệ. Chỉ chấp nhận 'doctor' hoặc 'nurse'");
                    }

                    List<Employee> list = repository.findByRole(role);
                    System.out.println("👥 Found " + list.size() + " " + role + "s");
                    return Json.ok(list);

               } catch (Exception e) {
                    System.err.println("❌ Error finding by role: " + e.getMessage());
                    return Json.error(500, "Error finding by role: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> findPasswordByUsernameOrEmail() {
          return (HttpRequest req) -> {
               try {
                    Optional<String> inputOpt = ExtractHelper.extractString(req.query, "input");
                    if (inputOpt.isEmpty() || inputOpt.get().trim().isEmpty()) {
                         return Json.error(400, "Missing input (username or email)");
                    }
                    String input = inputOpt.get().trim();

                    String passwordHash = repository.findPasswordByUsernameOrEmail(input);
                    if (passwordHash != null && !passwordHash.isBlank()) {
                         return Json.ok(Map.of("passwordHash", passwordHash));
                    } else {
                         return Json.error(404, "User not found");
                    }
               } catch (Exception e) {
                    System.err.println("❌ ERROR in findPasswordByUsernameOrEmail(): " + e.getMessage());
                    return Json.error(500, "Error: " + e.getMessage());
               }
          };
     }

     private Function<HttpRequest, HttpResponse> changePassword() {
          return (HttpRequest req) -> {
               try {
                    String bodyText = req.bodyText();
                    Map<String, Object> data = Json.parseMap(bodyText);
                    if (data == null || data.isEmpty()) {
                         return Json.error(400, "Body rỗng hoặc JSON không hợp lệ");
                    }

                    String usernameOrEmail = (String) data.get("usernameOrEmail");
                    String oldPassword = (String) data.get("oldPassword");
                    String newPassword = (String) data.get("newPassword");

                    if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                         return Json.error(400, "Thiếu username hoặc email");
                    }
                    if (oldPassword == null || oldPassword.trim().isEmpty()) {
                         return Json.error(400, "Thiếu mật khẩu hiện tại");
                    }
                    if (newPassword == null || newPassword.trim().isEmpty()) {
                         return Json.error(400, "Thiếu mật khẩu mới");
                    }
                    if (newPassword.length() < 8) {
                         return Json.error(400, "Mật khẩu mới phải có ít nhất 8 ký tự");
                    }

                    String currentPasswordHash = repository.findPasswordByUsernameOrEmail(usernameOrEmail);
                    if (currentPasswordHash == null) {
                         return Json.error(404, "Không tìm thấy người dùng");
                    }

                    at.favre.lib.crypto.bcrypt.BCrypt.Result result = at.favre.lib.crypto.bcrypt.BCrypt.verifyer()
                              .verify(oldPassword.toCharArray(), currentPasswordHash);
                    if (!result.verified) {
                         return Json.error(401, "Mật khẩu hiện tại không đúng");
                    }

                    String newPasswordHash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                              .hashToString(10, newPassword.toCharArray());
                    boolean changed = repository.changePassword(usernameOrEmail, newPasswordHash);

                    if (changed) {
                         System.out.println("✅ Password changed for: " + usernameOrEmail);
                         return Json.ok(Map.of("message", "Đổi mật khẩu thành công"));
                    } else {
                         return Json.error(500, "Không thể đổi mật khẩu");
                    }

               } catch (Exception e) {
                    System.err.println("❌ ERROR in changePassword(): " + e.getMessage());
                    return Json.error(500, "Error: " + e.getMessage());
               }
          };
     }
}