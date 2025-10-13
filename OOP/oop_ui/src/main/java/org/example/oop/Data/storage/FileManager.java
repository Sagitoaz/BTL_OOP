package org.example.oop.Data.storage;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FileManager - lớp cung cấp các thao tác đọc/ghi file an toàn theo luồng cho thư mục data/.
 *
 * Mục đích:
 * - Đơn giản hóa thao tác đọc/ghi file dạng text (mỗi dòng một bản ghi) trong project.
 * - Bảo đảm tính toàn vẹn dữ liệu khi có nhiều luồng đọc/ghi bằng cơ chế ReentrantReadWriteLock.
 *
 * Tính năng chính:
 * - Tự động tạo thư mục data/ nếu chưa tồn tại.
 * - Hỗ trợ đọc toàn bộ dòng của một file, ghi danh sách dòng (ghi đè) và thêm một dòng mới (append).
 * - Sử dụng lock đọc/ghi: cho phép nhiều luồng đọc đồng thời, nhưng ghi là độc quyền.
 *
 * Lưu ý vận hành và bảo trì:
 * - Các phương thức ném IOException để caller quyết định cách xử lý; repository hiện bọc IOException thành RuntimeException.
 * - Nếu cần hỗ trợ multi-process (nhiều tiến trình cùng truy cập file), ReentrantReadWriteLock không đủ —
 *   cần cơ chế khoá file ở hệ điều hành (FileChannel.lock) hoặc chuyển sang DB.
 * - Nếu file dữ liệu lớn, tránh đọc toàn bộ file vào bộ nhớ trong vài phương thức; cân nhắc streaming hoặc DB.
 */
public class FileManager {
    /**
     * Đường dẫn thư mục lưu dữ liệu (tương đối với thư mục project).
     */
    private static final String DATA_DIR = "oop_ui/src/main/resources/Data/";

    /**
     * Read-write lock để đảm bảo thread-safety. Nhiều reader có thể cùng đọc, writer độc quyền.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructor: khởi tạo FileManager và tạo thư mục data/ nếu chưa tồn tại.
     */
    public FileManager() {
        createDataDirIfNotExists();
    }

    /**
     * Tạo thư mục data/ nếu chưa tồn tại.
     * Nếu không thể tạo, ném RuntimeException để báo lỗi cấu hình môi trường.
     */
    private void createDataDirIfNotExists() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Cannot create data directory", e);
        }
    }

    /**
     * Đọc tất cả dòng từ file chỉ định.
     * - Dùng read lock để cho phép nhiều luồng đọc cùng lúc.
     * - Nếu file không tồn tại, trả về danh sách rỗng.
     *
     * @param filename tên file (không bao gồm thư mục data/)
     * @return danh sách các dòng trong file
     * @throws IOException khi có lỗi I/O
     */
    public List<String> readLines(String filename) throws IOException {
        lock.readLock().lock();
        try {
            Path path = Paths.get(DATA_DIR + filename);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            return Files.readAllLines(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Ghi danh sách dòng vào file (ghi đè nếu file đã tồn tại).
     * - Dùng write lock để đảm bảo ghi là thao tác độc quyền.
     * - Tạo file nếu chưa tồn tại.
     *
     * @param filename tên file (không bao gồm thư mục data/)
     * @param lines danh sách dòng sẽ ghi
     * @throws IOException khi có lỗi I/O
     */
    public void writeLines(String filename, List<String> lines) throws IOException {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(DATA_DIR + filename);
            Files.write(path, lines, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Thêm một dòng mới vào cuối file (append).
     * - Dùng write lock vì append cũng là thao tác ghi.
     * - Tạo file nếu chưa tồn tại.
     *
     * @param filename tên file (không bao gồm thư mục data/)
     * @param line dòng sẽ thêm (không cần tự thêm newline)
     * @throws IOException khi có lỗi I/O
     */
    public void appendLine(String filename, String line) throws IOException {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(DATA_DIR + filename);
            Files.write(path, (line + "\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } finally {
            lock.writeLock().unlock();
        }
    }
}