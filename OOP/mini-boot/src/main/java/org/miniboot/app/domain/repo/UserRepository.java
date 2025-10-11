package org.miniboot.app.domain.repo;

import org.miniboot.app.domain.models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository for managing user data
 *
 * Mô tả chi tiết để người sau dễ đọc hiểu:
 * - Repository này lưu trữ và đọc dữ liệu người dùng từ một file text (pipe-separated).
 * - Mỗi dòng file có định dạng: id|username|password|role|email|fullName|phone|createdAt|active
 *   (createdAt có thể rỗng). Dấu '|' được dùng làm separator.
 * - Thiết kế hiện tại đơn giản, phù hợp demo hoặc ứng dụng nhỏ; với hệ thống thật cần
 *   chuyển sang DB để đảm bảo atomicity, concurrency và hiệu năng.
 *
 * Lưu ý bảo mật và vận hành:
 * - Mật khẩu hiện đang được lưu trong file (theo mã nguồn) — phải băm mật khẩu trước khi lưu trong production.
 * - Hàm findAll đọc toàn bộ file mỗi lần được gọi => không hiệu quả khi số lượng user lớn.
 *   Có thể cache kết quả hoặc dùng DB.
 */
public class UserRepository {
    private static final String USER_FILE = "oop_ui/src/main/resources/Data/users.txt";

    /**
     * Find all users
     *
     * Chú giải:
     * - Đọc toàn bộ file USER_FILE.
     * - Bỏ qua các dòng rỗng hoặc bắt đầu bằng '#'.
     * - Tách dòng theo ký tự '|' và gán vào model User.
     * - Nếu createdAt rỗng thì giữ null, ngược lại parse thành LocalDateTime.
     *
     * Hạn chế:
     * - Đọc toàn bộ file cho mỗi lần gọi => chi phí I/O cao.
     * - Trong môi trường production, nên thay bằng truy vấn DB hoặc tối thiểu là cache.
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    User user = new User();
                    user.setId(parts[0]);
                    user.setUsername(parts[1]);
                    user.setPassword(parts[2]);
                    user.setRole(parts[3]);
                    user.setEmail(parts[4]);
                    user.setFullName(parts[5]);
                    user.setPhone(parts[6]);
                    user.setCreatedAt(parts[7].isEmpty() ? null : LocalDateTime.parse(parts[7]));
                    user.setActive(Boolean.parseBoolean(parts[8]));
                    users.add(user);
                }
            }
        } catch (IOException e) {
            // Ghi log lỗi I/O; không ném tiếp để caller có thể xử lý an toàn
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return users;
    }

    /**
     * Find user by ID
     *
     * Implementation detail:
     * - Hiện tại sử dụng findAll() và stream.filter để tìm theo id.
     * - Điều này đơn giản nhưng không tối ưu: findById sẽ đọc toàn bộ file mỗi lần.
     */
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Find user by username
     *
     * Tương tự findById: không hiệu quả với file lớn vì đọc toàn bộ file.
     */
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Save user (create or update)
     *
     * Behaviour:
     * - Đọc danh sách hiện tại bằng findAll().
     * - Nếu user có id đã tồn tại (so sánh bằng id) thì replace, ngược lại add mới.
     * - Gọi writeUsersToFile để ghi lại toàn bộ file.
     *
     * Lưu ý:
     * - Việc ghi toàn bộ file mỗi lần có thể gây race condition nếu có nhiều luồng cùng ghi.
     *   Cần cơ chế lock hoặc dùng DB cho production.
     */
    public User save(User user) {
        List<User> users = findAll();
        boolean exists = users.stream().anyMatch(u -> u.getId().equals(user.getId()));

        if (exists) {
            // Update existing
            users.replaceAll(u -> u.getId().equals(user.getId()) ? user : u);
        } else {
            // Add new
            users.add(user);
        }

        writeUsersToFile(users);
        return user;
    }

    /**
     * Delete user by ID
     *
     * Behaviour:
     * - Lấy danh sách hiện tại, removeIf user có id khớp, nếu có thay đổi thì ghi lại file.
     * - Trả về true nếu xóa thành công, false nếu không tìm thấy.
     */
    public boolean deleteById(String id) {
        List<User> users = findAll();
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            writeUsersToFile(users);
        }
        return removed;
    }

    /**
     * Ghi danh sách users vào file.
     *
     * Format mỗi dòng: id|username|password|role|email|fullName|phone|createdAt|active
     *
     * Chú ý:
     * - Hàm này ghi đè toàn bộ file USER_FILE.
     * - Nếu user.createdAt == null thì ghi trường rỗng cho createdAt.
     */
    private void writeUsersToFile(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            String line = String.join("|",
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
                    String.valueOf(user.isActive())
            );
            lines.add(line);
        }
        try {
            Files.write(Paths.get(USER_FILE), lines);
        } catch (IOException e) {
            // Ghi log lỗi I/O khi không thể ghi file
            System.err.println("Error writing users file: " + e.getMessage());
        }
    }
}
