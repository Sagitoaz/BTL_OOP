package org.miniboot.app.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.miniboot.app.domain.models.User;
import org.miniboot.app.domain.models.Admin;
import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.Customer;
import org.miniboot.app.domain.repo.UserRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hướng dẫn chi tiết cho người bảo trì/đọc code:
 *
 * 1) Tổng quan lớp
 *    - UserController chịu trách nhiệm mount các route liên quan đến User
 *      và thực hiện các thao tác CRUD cơ bản (list, get, create, update, delete).
 *
 * 2) Cách lấy path parameter
 *    - Router sẽ match pattern như "/users/:id" và extract giá trị vào
 *      request.tags map (key = tên biến trong pattern, ví dụ "id").
 *    - Khi cần lấy id từ path, dùng: String id = request.tags.get("id");
 *    - Luôn validate (null/empty) trước khi sử dụng.
 *
 * 3) Cách đọc và parse body
 *    - Đọc toàn bộ body bằng request.bodyText() (UTF-8 string).
 *    - Dùng Json.parseMap(body) để parse thành Map<String,Object>.
 *    - Phải cast từng trường về kiểu mong muốn (String, Boolean, ...).
 *    - Không giả định các trường luôn tồn tại — kiểm tra bằng containsKey hoặc null checks.
 *
 * 4) Quy ước trả về (HTTP + JSON)
 *    - Sử dụng helper Json.ok(...) để trả về 200 và Json.error(status, message) để trả lỗi.
 *    - Các message trả về cho client đã được chuẩn hoá sang tiếng Anh để tránh lỗi font khi in ra terminal.
 *    - Tránh trả thông tin nhạy cảm (như password) trong response; hiện tại hàm trả toàn bộ đối tượng User — nếu User chứa password, cân nhắc loại bỏ hoặc mask field đó trước khi trả.
 *
 * 5) Lưu ý về bảo mật và production
 *    - Hiện tại mật khẩu đang được lưu trực tiếp từ input vào User.password (plain-text).
 *      Trong môi trường thực tế phải băm mật khẩu trước khi lưu (bcrypt/argon2) và không trả password về client.
 *    - ID hiện được tạo bằng timestamp; tốt hơn nên dùng UUID hoặc ID do DB cấp để tránh xung đột.
 *    - Repository hiện đọc/ghi file (UserRepository) — không phù hợp với hệ thống lớn. Nên chuyển sang DB để đảm bảo concurrency, atomicity và performance.
 *
 * 6) Xử lý lỗi và báo lỗi
 *    - Các ngoại lệ bất ngờ được bắt và trả về 500 với thông báo ngắn (không lộ stack trace cho client).
 *    - Nội bộ vẫn in/ghi log (System.out/System.err) cho việc debug; trong production nên dùng logger có levels và không in dữ liệu nhạy cảm.
 *
 * 7) Kiểm tra trùng lặp username
 *    - Trước khi tạo hoặc đổi username, kiểm tra repository.findByUsername để tránh duplicate.
 *    - Khi update username, nếu đổi sang username mới thì cũng kiểm tra trùng lặp.
 *
 * 8) Các điểm nâng cấp (gợi ý)
 *    - Thêm validation chặt chẽ hơn (email format, password strength) trước khi accept create/update.
 *    - Không trả về User object chứa password; tạo DTO/View object để trả về client.
 *    - Sử dụng transactions nếu repository hỗ trợ (DB) để rollback khi lỗi.
 */

/**
 * UserController: Controller xử lý các endpoint về quản lý người dùng
 * - GET /users: List all users
 * - GET /users/:id: Get user by ID
 * - POST /users: Create new user (Admin/Employee/Customer)
 * - PUT /users/:id: Update existing user
 * - DELETE /users/:id: Delete user
 *
 * Đã cập nhật theo database mới:
 * - User giờ là interface với 3 implementation: Admin, Employee, Customer
 * - POST /users yêu cầu trường "userType" để xác định loại user cần tạo
 */
public class UserController {

    private static final UserRepository userRepository = new UserRepository();

    // Mount routes vào Router
    public static void mount(Router router) {
        router.get("/users", UserController::getAllUsers, true);
        router.get("/users/:id", UserController::getUserById, true);
        router.post("/users", UserController::createUser, true);
        router.put("/users/:id", UserController::updateUser, true);
        router.delete("/users/:id", UserController::deleteUser, true);
    }

    /**
     * GET /users
     * Trả về danh sách tất cả người dùng từ cả 3 bảng (admins, employees, customers)
     */
    private static HttpResponse getAllUsers(HttpRequest request) {
        try {
            List<User> users = userRepository.findAll();
            return Json.ok(users);
        } catch (Exception e) {
            return Json.error(500, "Error retrieving user list: " + e.getMessage());
        }
    }

    /**
     * GET /users/:id
     * Lấy thông tin user theo ID (path param)
     */
    private static HttpResponse getUserById(HttpRequest request) {
        try {
            String id = request.tags.get("id");
            if (id == null || id.trim().isEmpty()) {
                return Json.error(400, "Missing user ID");
            }

            Optional<User> userOpt = userRepository.findById(id);
            return userOpt.map(Json::ok)
                    .orElseGet(() -> Json.error(404, "User not found with ID: " + id));
        } catch (Exception e) {
            return Json.error(500, "Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * POST /users
     * Tạo người dùng mới từ JSON body.
     * Body expected: {
     *   "userType": "admin" | "employee" | "customer",
     *   "username": "...",
     *   "password": "...",
     *   ... (các trường khác tùy theo userType)
     * }
     */
    private static HttpResponse createUser(HttpRequest request) {
        try {
            String body = request.bodyText();
            Map<String, Object> data = Json.parseMap(body);

            String userType = (String) data.get("userType");
            String username = (String) data.get("username");
            String password = (String) data.get("password");

            if (userType == null || username == null || password == null) {
                return Json.error(400, "Missing required fields: userType, username, password");
            }

            // Check duplicate username
            if (userRepository.findByUsername(username).isPresent()) {
                return Json.error(409, "Username already exists: " + username);
            }

            String id = String.valueOf(System.currentTimeMillis());
            User newUser;

            switch (userType.toLowerCase()) {
                case "admin":
                    newUser = createAdmin(id, username, password, data);
                    break;
                case "employee":
                    newUser = createEmployee(id, username, password, data);
                    break;
                case "customer":
                    newUser = createCustomer(id, username, password, data);
                    break;
                default:
                    return Json.error(400, "Invalid userType. Must be: admin, employee, or customer");
            }

            userRepository.save(newUser);
            return Json.ok(Map.of("message", "User created successfully", "user", newUser));
        } catch (Exception e) {
            return Json.error(500, "Error creating user: " + e.getMessage());
        }
    }

    private static Admin createAdmin(String id, String username, String password, Map<String, Object> data) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setUsername(username);
        admin.setPassword(hashPassword(password));
        admin.setEmail((String) data.get("email"));
        admin.setActive(data.containsKey("active") ? (Boolean) data.get("active") : true);
        return admin;
    }

    private static Employee createEmployee(String id, String username, String password, Map<String, Object> data) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername(username);
        employee.setPassword(hashPassword(password));
        employee.setFirstname((String) data.get("firstname"));
        employee.setLastname((String) data.get("lastname"));
        employee.setAvatar((String) data.get("avatar"));
        employee.setEmployeeRole((String) data.getOrDefault("role", "nurse"));
        employee.setLicenseNo((String) data.get("licenseNo"));
        employee.setEmail((String) data.get("email"));
        employee.setPhone((String) data.get("phone"));
        employee.setActive(data.containsKey("active") ? (Boolean) data.get("active") : true);
        employee.setCreatedAt(LocalDateTime.now());
        return employee;
    }

    private static Customer createCustomer(String id, String username, String password, Map<String, Object> data) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUsername(username);
        customer.setPassword(hashPassword(password));
        customer.setFirstname((String) data.get("firstname"));
        customer.setLastname((String) data.get("lastname"));
        customer.setPhone((String) data.get("phone"));
        customer.setEmail((String) data.get("email"));

        // Parse dob if provided
        if (data.containsKey("dob") && data.get("dob") != null) {
            customer.setDob(LocalDate.parse((String) data.get("dob")));
        }

        customer.setGender((String) data.get("gender"));
        customer.setAddress((String) data.get("address"));
        customer.setNote((String) data.get("note"));
        customer.setCreatedAt(LocalDateTime.now());
        return customer;
    }

    /**
     * PUT /users/:id
     * Cập nhật người dùng theo ID với các fields trong body.
     * Chỉ cập nhật những trường client cung cấp.
     */
    private static HttpResponse updateUser(HttpRequest request) {
        try {
            String id = request.tags.get("id");
            if (id == null || id.trim().isEmpty()) {
                return Json.error(400, "Missing user ID");
            }

            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isEmpty()) {
                return Json.error(404, "User not found with ID: " + id);
            }

            String body = request.bodyText();
            Map<String, Object> data = Json.parseMap(body);
            User user = existingUserOpt.get();

            // Update common fields
            if (data.containsKey("username")) {
                String newUsername = (String) data.get("username");
                if (newUsername != null && !newUsername.equals(user.getUsername())
                        && userRepository.findByUsername(newUsername).isPresent()) {
                    return Json.error(409, "Username already exists: " + newUsername);
                }
                user.setUsername(newUsername);
            }
            if (data.containsKey("password")) {
                user.setPassword(hashPassword((String) data.get("password")));
            }
            if (data.containsKey("email")) {
                user.setEmail((String) data.get("email"));
            }
            if (data.containsKey("active")) {
                user.setActive((Boolean) data.get("active"));
            }

            // Update specific fields based on user type
            if (user instanceof Admin) {
                // Admin has no additional fields to update
            } else if (user instanceof Employee) {
                Employee emp = (Employee) user;
                if (data.containsKey("firstname")) emp.setFirstname((String) data.get("firstname"));
                if (data.containsKey("lastname")) emp.setLastname((String) data.get("lastname"));
                if (data.containsKey("avatar")) emp.setAvatar((String) data.get("avatar"));
                if (data.containsKey("role")) emp.setEmployeeRole((String) data.get("role"));
                if (data.containsKey("licenseNo")) emp.setLicenseNo((String) data.get("licenseNo"));
                if (data.containsKey("phone")) emp.setPhone((String) data.get("phone"));
            } else if (user instanceof Customer) {
                Customer cust = (Customer) user;
                if (data.containsKey("firstname")) cust.setFirstname((String) data.get("firstname"));
                if (data.containsKey("lastname")) cust.setLastname((String) data.get("lastname"));
                if (data.containsKey("phone")) cust.setPhone((String) data.get("phone"));
                if (data.containsKey("dob") && data.get("dob") != null) {
                    cust.setDob(LocalDate.parse((String) data.get("dob")));
                }
                if (data.containsKey("gender")) cust.setGender((String) data.get("gender"));
                if (data.containsKey("address")) cust.setAddress((String) data.get("address"));
                if (data.containsKey("note")) cust.setNote((String) data.get("note"));
            }

            userRepository.save(user);
            return Json.ok(Map.of("message", "User updated successfully", "user", user));
        } catch (Exception e) {
            return Json.error(500, "Error updating user: " + e.getMessage());
        }
    }

    /**
     * DELETE /users/:id
     * Xóa người dùng theo ID
     */
    private static HttpResponse deleteUser(HttpRequest request) {
        try {
            String id = request.tags.get("id");
            if (id == null || id.trim().isEmpty()) {
                return Json.error(400, "Missing user ID");
            }

            boolean deleted = userRepository.deleteById(id);
            if (deleted) {
                return Json.ok(Map.of("message", "User deleted successfully"));
            } else {
                return Json.error(404, "User not found with ID: " + id);
            }
        } catch (Exception e) {
            return Json.error(500, "Error deleting user: " + e.getMessage());
        }
    }

    private static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }
}
