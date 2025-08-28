package org.example.oop.Control;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class UserFileService {

    private static final int COL_USERNAME = 1; // 0=id, 1=username, 2=password, ...
    private static final int COL_PASSWORD = 2;

    private final Path usersPath;

    public UserFileService(Path usersPath) {
        this.usersPath = Objects.requireNonNull(usersPath);
    }

    /** Liệt kê username đã load để debug */
    public synchronized String debugListUsers() {
        try {
            Map<String, UserRecord> map = readAllRecords();
            return String.join(", ", map.keySet());
        } catch (IOException e) {
            return "(error reading file)";
        }
    }

    /** Đổi mật khẩu: username tồn tại & currentPassword khớp mới ghi */
    public synchronized boolean changePassword(String username, String currentPassword, String newPassword) {
        try {
            Map<String, UserRecord> map = readAllRecords();
            UserRecord rec = map.get(username);
            if (rec == null) return false; // không có user

            String saved = rec.getPassword();
            if (!Objects.equals(saved, currentPassword)) return false; // sai mật khẩu hiện tại

            rec.setPassword(newPassword);
            writeAllRecords(map.values());
            return true;
        } catch (IOException e) {
            e.printStackTrace(); // hoặc logger
            return false;
        }
    }

    /** Lấy mật khẩu đang lưu (phục vụ validate nếu cần) */
    public synchronized Optional<String> getPassword(String username) {
        try {
            Map<String, UserRecord> map = readAllRecords();
            UserRecord rec = map.get(username);
            return Optional.ofNullable(rec == null ? null : rec.getPassword());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /* ===== Kiểu dữ liệu nội bộ giữ nguyên các cột ===== */

    private static final class UserRecord {
        private final List<String> cols; // giữ nguyên số cột & thứ tự

        private UserRecord(List<String> cols) {
            this.cols = cols;
        }

        String getUsername() {
            return getColSafe(COL_USERNAME);
        }

        String getPassword() {
            return getColSafe(COL_PASSWORD);
        }

        void setPassword(String newPass) {
            setColSafe(COL_PASSWORD, newPass);
        }

        String toLine() {
            return String.join("|", cols);
        }

        private String getColSafe(int idx) {
            return idx >= 0 && idx < cols.size() ? cols.get(idx) : null;
        }

        private void setColSafe(int idx, String value) {
            // Bảo đảm đủ kích thước list
            while (cols.size() <= idx) cols.add("");
            cols.set(idx, value == null ? "" : value);
        }
    }

    /* ===== Helpers đọc / ghi file theo định dạng nhiều cột ===== */

    /** Đọc toàn bộ bản ghi, key = username */
    private Map<String, UserRecord> readAllRecords() throws IOException {
        Map<String, UserRecord> map = new LinkedHashMap<>();
        if (!Files.exists(usersPath)) return map;

        for (String raw : Files.readAllLines(usersPath, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // Giữ cột trống ở cuối (split với limit = -1)
            String[] parts = line.split("\\|", -1);
            // Cần tối thiểu 3 cột: id | username | password
            if (parts.length < 3) continue;

            // Trim từng cột
            List<String> cols = new ArrayList<>(parts.length);
            for (String p : parts) cols.add(p == null ? "" : p.trim());

            String username = cols.get(COL_USERNAME);
            if (username == null || username.isEmpty()) continue;

            map.put(username, new UserRecord(cols));
        }
        return map;
    }

    /** Ghi lại toàn bộ records, giữ nguyên các cột (chỉ thay password nếu đã cập nhật) */
    private void writeAllRecords(Collection<UserRecord> records) throws IOException {
        Path parent = usersPath.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path tmp = usersPath.resolveSibling(usersPath.getFileName() + ".tmp");

        List<String> out = new ArrayList<>(records.size() + 1);
        // Nếu bạn muốn giữ header thì bỏ comment dưới đây.
        out.add("# id|username|password|role|fullName|province|email|phone|dob|gender");

        for (UserRecord r : records) {
            out.add(r.toLine());
        }

        Files.write(tmp, out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        try {
            Files.move(tmp, usersPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, usersPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
