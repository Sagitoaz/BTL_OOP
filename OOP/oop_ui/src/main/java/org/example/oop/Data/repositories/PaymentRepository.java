package org.example.oop.Data.repositories;

import org.example.oop.Data.models.Payment;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PaymentRepository - quản lý lưu/đọc các đối tượng Payment từ file payments.txt.
 *
 * Mục đích:
 * - Cung cấp các thao tác CRUD cơ bản cho Payment: save, findById, findAll, update, delete.
 * - Sử dụng FileManager để thao tác file dạng dòng văn bản (mỗi dòng một payment theo định dạng toFileFormat()).
 *
 * Ghi chú chi tiết cho người duy trì:
 * - Định dạng file: mỗi dòng là chuỗi phân cách bởi '|' do Payment.toFileFormat() tạo ra.
 * - Khi đọc file, các dòng rỗng hoặc bắt đầu bằng '#' được bỏ qua (hỗ trợ comment trong file dữ liệu).
 * - fromFileFormat() giả sử dữ liệu đúng định dạng; nếu dữ liệu hỏng có thể ném NumberFormatException hoặc DateTimeParseException.
 * - Các thao tác update/delete thực hiện read-modify-write toàn bộ file: đọc toàn bộ dòng, thay đổi trong bộ nhớ rồi ghi lại.
 *   - Cách tiếp cận này đơn giản nhưng không tối ưu cho file lớn.
 * - Concurrency: hiện không có cơ chế khoá file; nếu nhiều tiến trình/luồng cùng ghi file, có thể xảy ra race condition hoặc ghi đè dữ liệu.
 *   - Nếu cần, thêm file lock (FileChannel.lock) hoặc chuyển sang DB để hỗ trợ transaction.
 * - Error handling: IOException được bọc thành RuntimeException để caller biết có lỗi I/O; cân nhắc log chi tiết khi triển khai thực tế.
 *
 * Lời khuyên:
 * - Thêm unit tests cho toFileFormat/fromFileFormat để đảm bảo thay đổi model không phá vỡ dữ liệu.
 * - Nếu cần hiệu năng hoặc tính nhất quán cao, thay file bằng DB (SQLite/Postgres).
 */
public class PaymentRepository implements DataRepository<Payment> {
    private static final String FILENAME = "payments.txt";
    private final FileManager fileManager;

    public PaymentRepository() {
        this.fileManager = new FileManager();
    }

    @Override
    public Payment save(Payment payment) {
        try {
            if (exists(payment.getId())) {
                update(payment);
            } else {
                fileManager.appendLine(FILENAME, payment.toFileFormat());
            }
            return payment;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(String id) {
        return findAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        try {
            return fileManager.readLines(FILENAME).stream()
                    .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .map(Payment::fromFileFormat)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void update(Payment payment) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> updated = lines.stream()
                    .map(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return line;
                        }
                        Payment p = Payment.fromFileFormat(line);
                        return p.getId().equals(payment.getId()) ? payment.toFileFormat() : line;
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update payment", e);
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
                        return !Payment.fromFileFormat(line).getId().equals(id);
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, filtered);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete payment", e);
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
}
