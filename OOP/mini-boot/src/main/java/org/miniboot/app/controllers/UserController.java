package org.miniboot.app.controllers;

import org.miniboot.app.domain.models.User;
import org.miniboot.app.domain.repo.UserRepository;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

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
 * - POST /users: Create new user
 * - PUT /users/:id: Update existing user
 * - DELETE /users/:id: Delete user
 *
 * Ghi chú (tiếng Việt):
 * - Các message trả về cho client và logs là tiếng Anh để tránh lỗi font trong terminal.
 * - Các comment giải thích logic quan trọng bằng tiếng Việt để người sau dễ hiểu.
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
     * Trả về danh sách tất cả người dùng.
     * Chú giải: gọi repository.findAll(), trả về 200 với danh sách người dùng.
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
     * Chú giải: ID lấy từ request.tags (Router đã extract path params vào tags).
     */
    private static HttpResponse getUserById(HttpRequest request) {
        try {
            String id = request.tags.get("id");
            if (id == null || id.trim().isEmpty()) {
                return Json.error(400, "Missing user ID");
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                return Json.ok(userOpt.get());
            } else {
                return Json.error(404, "User not found with ID: " + id);
            }
        } catch (Exception e) {
            return Json.error(500, "Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * POST /users
     * Tạo người dùng mới từ JSON body.
     * Body expected: {"username","password","role","email","fullName","phone","active"}
     * Chú giải: validate required fields, check duplicate username, generate id và lưu.
     */
    private static HttpResponse createUser(HttpRequest request) {
        try {
            String body = request.bodyText();
            Map<String, Object> data = Json.parseMap(body);

            String username = (String) data.get("username");
            String password = (String) data.get("password");
            String role = (String) data.get("role");
            String email = (String) data.get("email");
            String fullName = (String) data.get("fullName");
            String phone = (String) data.get("phone");
            Boolean active = data.containsKey("active") ? (Boolean) data.get("active") : null;

            if (username == null || password == null || role == null || email == null || fullName == null) {
                return Json.error(400, "Missing required fields: username, password, role, email, fullName");
            }

            if (userRepository.findByUsername(username).isPresent()) {
                return Json.error(409, "Username already exists: " + username);
            }

            String id = String.valueOf(System.currentTimeMillis());
            User newUser = new User(id, username, password, role, email, fullName, phone, active != null ? active : true);
            userRepository.save(newUser);

            return Json.ok(Map.of("message", "User created successfully", "user", newUser));
        } catch (Exception e) {
            return Json.error(500, "Error creating user: " + e.getMessage());
        }
    }

    /**
     * PUT /users/:id
     * Cập nhật người dùng theo ID với các fields trong body.
     * Chú giải: chỉ cập nhật những trường client cung cấp.
     */
    private static HttpResponse updateUser(HttpRequest request) {
        try {
            String id = request.tags.get("id");
            if (id == null || id.trim().isEmpty()) {
                return Json.error(400, "Missing user ID");
            }

            Optional<User> existingUserOpt = userRepository.findById(id);
            if (!existingUserOpt.isPresent()) {
                return Json.error(404, "User not found with ID: " + id);
            }

            String body = request.bodyText();
            Map<String, Object> data = Json.parseMap(body);

            User user = existingUserOpt.get();

            if (data.containsKey("username")) {
                String newUsername = (String) data.get("username");
                if (newUsername != null && !newUsername.equals(user.getUsername()) && userRepository.findByUsername(newUsername).isPresent()) {
                    return Json.error(409, "Username already exists: " + newUsername);
                }
                user.setUsername(newUsername);
            }
            if (data.containsKey("password")) {
                user.setPassword((String) data.get("password"));
            }
            if (data.containsKey("role")) {
                user.setRole((String) data.get("role"));
            }
            if (data.containsKey("email")) {
                user.setEmail((String) data.get("email"));
            }
            if (data.containsKey("fullName")) {
                user.setFullName((String) data.get("fullName"));
            }
            if (data.containsKey("phone")) {
                user.setPhone((String) data.get("phone"));
            }
            if (data.containsKey("active")) {
                user.setActive((Boolean) data.get("active"));
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
}
