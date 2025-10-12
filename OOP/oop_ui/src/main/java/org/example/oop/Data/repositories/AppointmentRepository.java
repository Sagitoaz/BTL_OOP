package org.example.oop.Data.repositories;

import org.example.oop.Data.models.Appointment;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AppointmentRepository - quản lý lưu/đọc các đối tượng Appointment từ file appointments.txt.
 *
 * Mục đích:
 * - Cung cấp các thao tác CRUD cơ bản cho Appointment: save, findById, findAll, update, delete.
 * - Sử dụng FileManager để thao tác với file dòng văn bản (mỗi dòng một appointment theo định dạng toFileFormat()).
 *
 * Ghi chú chi tiết cho người duy trì:
 * - Định dạng file: mỗi dòng là chuỗi phân cách bởi '|' do Appointment.toFileFormat() tạo ra.
 * - Khi đọc file, các dòng rỗng hoặc bắt đầu bằng '#' được bỏ qua (hỗ trợ comment trong file dữ liệu).
 * - fromFileFormat() giả sử thứ tự các phần chính xác; nếu dữ liệu hỏng có thể ném DateTimeParseException hoặc ArrayIndexOutOfBoundsException.
 * - Các phương thức update/delete thực hiện thao tác "read-modify-write" toàn bộ file: đọc tất cả dòng, thay đổi trong bộ nhớ rồi ghi lại.
 *   - Ưu điểm: đơn giản, dễ hiểu.
 *   - Hạn chế: không tối ưu cho file lớn, có thể tốn bộ nhớ và thời gian I/O.
 * - Concurrency: không có cơ chế khoá file ở mức repository; nếu nhiều tiến trình hoặc luồng cùng tương tác, có thể xảy ra race condition và mất mát dữ liệu.
 *   - Khi cần hỗ trợ đa tiến trình đồng thời, nên bổ sung file lock (FileChannel.lock) hoặc chuyển sang storage hỗ trợ transaction.
 * - Error handling: các IOException khi đọc/ghi được bọc thành RuntimeException để caller biết có lỗi I/O.
 *
 * Lời khuyên vận hành:
 * - Thêm unit test kiểm tra toFileFormat/fromFileFormat để đảm bảo tương thích khi thay đổi model.
 * - Nếu dữ liệu lớn hoặc yêu cầu truy vấn phức tạp, cân nhắc chuyển sang DB (SQLite/Postgres) thay vì file.
 */
public class AppointmentRepository implements DataRepository<Appointment> {
    private static final String FILENAME = "appointments.txt";
    private final FileManager fileManager;

    public AppointmentRepository() {
        this.fileManager = new FileManager();
    }

    @Override
    public Appointment save(Appointment appointment) {
        try {
            if (exists(appointment.getId())) {
                update(appointment);
            } else {
                fileManager.appendLine(FILENAME, appointment.toFileFormat());
            }
            return appointment;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save appointment", e);
        }
    }

    @Override
    public Optional<Appointment> findById(String id) {
        return findAll().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Appointment> findAll() {
        try {
            return fileManager.readLines(FILENAME).stream()
                    .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .map(Appointment::fromFileFormat)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void update(Appointment appointment) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> updated = lines.stream()
                    .map(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return line;
                        }
                        Appointment a = Appointment.fromFileFormat(line);
                        return a.getId().equals(appointment.getId()) ? appointment.toFileFormat() : line;
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update appointment", e);
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
                        return !Appointment.fromFileFormat(line).getId().equals(id);
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, filtered);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete appointment", e);
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
