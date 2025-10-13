package org.example.oop.Data.Repositories;

import org.example.oop.Model.Schedule.Appointment;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.time.LocalDate;
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
    public Optional<Appointment> findById(int id) {
        return findAll().stream()
                .filter(a -> a.getId() == id)
                .findFirst();
    }

    public List<Appointment> findByDoctorAndDate(int doctorId, LocalDate date) {
        return findAll().stream().filter(a -> a.getDoctorId() == doctorId)
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDateRange(LocalDate from, LocalDate to) {
        return appointments.stream()
                .filter(a -> {
                    LocalDate apptDate = a.getStartTime().toLocalDate();
                    return !apptDate.isBefore(from) && !apptDate.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointments.stream().filter(a -> a.getAppointmentStatus() == status).collect(Collectors.toList());
    }

    public void save(Appointment appointment) {
        appointment.validate();
        if (appointment.getId() == 0) {
            appointment.setId(nextId++);
            appointment.setCreatedAt(LocalDateTime.now());
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        appointments.add(appointment);
        saveToFile();
    }

    public void update(Appointment appointment) {
        appointment.validate();
        appointment.setUpdatedAt(LocalDateTime.now());

        int index = findIndexById(appointment.getId());
        if (index >= 0) {
            appointments.set(index, appointment);
            saveToFile();
        } else {
            throw new IllegalArgumentException("Appointment not found: " + appointment.getId());
        }
    }

    public void delete(int id) {
        appointments.removeIf(a -> a.getId() == id);
        saveToFile();
    }

    private int findIndexById(int id) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void loadFromFile() {
        try {
            if (Files.exists(Paths.get(DATA_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        appointments.add(Appointment.fromDataString(line));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get(DATA_FILE).getParent());
            List<String> lines = appointments.stream().map(Appointment::toDataString).toList();

            Files.write(Paths.get(DATA_FILE), lines);
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    public ArrayList<Appointment> findConflicts() {
        ArrayList<Appointment> conflicts = new ArrayList<>();
        for (int i = 0; i < appointments.size(); i++) {
            for (int j = i + 1; j < appointments.size(); j++) {
                Appointment a1 = appointments.get(i);
                Appointment a2 = appointments.get(j);
                if (a1.getDoctorId() == a2.getDoctorId() && hasTimeOverlap(a1, a2)) {
                    if (!conflicts.contains(a1)) conflicts.add(a1);
                    if (!conflicts.contains(a2)) conflicts.add(a2);
                }
            }
        }
        return conflicts;
    }

    private boolean hasTimeOverlap(Appointment a1, Appointment a2) {
        return a1.getStartTime().isBefore(a2.getEndTime()) &&
                a2.getStartTime().isBefore(a1.getEndTime());
    }
}
