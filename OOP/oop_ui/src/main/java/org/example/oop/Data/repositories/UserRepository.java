package org.example.oop.Data.repositories;

import org.example.oop.Data.models.User;
import org.example.oop.Data.models.UserRole;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserRepository - Lớp lưu/đọc dữ liệu người dùng từ file (users.txt).
 *
 * Mục đích:
 * - Cung cấp các thao tác CRUD cơ bản cho đối tượng User: save, findById, findAll, update, delete.
 * - Đọc ghi dòng văn bản (mỗi dòng một user) qua FileManager.
 *
 * Ghi chú chi tiết cho người duy trì:
 * - Định dạng file: mỗi dòng là chuỗi phân cách bởi '|' do User.toFileFormat() tạo ra.
 * - Khi đọc file, các dòng rỗng hoặc bắt đầu bằng '#' được bỏ qua (dùng để comment trong file dữ liệu).
 * - fromFileFormat() có thể ném ngoại lệ nếu dữ liệu không hợp lệ; repository hiện bỏ qua các lỗi đó
 *   bằng cách trả về danh sách rỗng hoặc ném RuntimeException trong các thao tác ghi.
 * - Các phương thức update/delete thực hiện đọc toàn bộ file, chỉnh sửa trong bộ nhớ rồi ghi lại toàn bộ file.
 *   Nếu file lớn, cân nhắc chuyển sang DB hoặc cập nhật theo streaming để giảm bộ nhớ.
 * - Concurrency: hiện không có khóa file; nếu nhiều tiến trình/luồng cùng ghi file, có thể gây mất mát dữ liệu.
 *   Khi cần đồng bộ, thêm cơ chế lock trên file hoặc chuyển sang storage support transaction.
 *
 * Truy vấn tuỳ chỉnh:
 * - findByUsername(): tìm user theo username.
 * - findByRole(): lọc danh sách theo role.
 */
public class UserRepository implements DataRepository<User> {
    private static final String FILENAME = "users.txt";
    private final FileManager fileManager;

    public UserRepository() {
        this.fileManager = new FileManager();
    }

    @Override
    public User save(User user) {
        try {
            if (exists(user.getId())) {
                update(user);
            } else {
                fileManager.appendLine(FILENAME, user.toFileFormat());
            }
            return user;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        try {
            return fileManager.readLines(FILENAME).stream()
                    .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .map(User::fromFileFormat)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void update(User user) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> updated = lines.stream()
                    .map(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return line;
                        }
                        User u = User.fromFileFormat(line);
                        return u.getId().equals(user.getId()) ? user.toFileFormat() : line;
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> filtered = lines.stream()
                    .filter(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return true;
                        }
                        return !User.fromFileFormat(line).getId().equals(id);
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, filtered);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean exists(String id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return findAll().size();
    }

    // Custom queries
    /**
     * Tìm user theo username.
     * Lưu ý: trả về Optional vì có thể không tìm thấy.
     */
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Lấy danh sách người dùng theo role.
     */
    public List<User> findByRole(UserRole role) {
        return findAll().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }
}