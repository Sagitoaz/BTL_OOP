package org.example.oop.Repository;

import org.example.oop.Model.PaymentModel.Payment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

/**
 * Repository quản lý việc truy cập dữ liệu cho Payment (hóa đơn).
 * Chịu trách nhiệm đọc, ghi, và quản lý ID từ file Payment.txt.
 */
public class PaymentRepository {

    private final Path filePath = Paths.get("src/main/resources/data/payment.txt");

    public PaymentRepository() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng khi khởi tạo file Payment.txt: " + e.getMessage());
        }
    }

    /**
     * Tìm ID lớn nhất hiện có trong file để tạo ID tiếp theo.
     *
     * @return ID tiếp theo có thể sử dụng.
     */
    private int getNextId() {
        try {
            return Files.lines(filePath)
                    .map(line -> line.split("\\|")[0]) // Lấy phần ID từ mỗi dòng
                    .filter(idStr -> !idStr.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0) + 1; // Nếu file rỗng, bắt đầu từ 1
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file Payment.txt để lấy ID: " + e.getMessage());
            return 1; // Trả về 1 nếu có lỗi
        }
    }

    /**
     * Lưu một đối tượng Payment vào file.
     * Tự động gán ID mới và ghi vào cuối file.
     *
     * @param payment Đối tượng Payment cần lưu (có thể chưa có ID).
     * @return ID của Payment vừa được lưu.
     */
    public int save(Payment payment) {
        int nextId = getNextId();
        payment.setId(nextId); // Gán ID mới cho đối tượng

        String dataLine = payment.toDataString();

        try {
            Files.write(filePath, Collections.singletonList(dataLine), StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
            return nextId;
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file Payment.txt: " + e.getMessage());
            return -1; // Trả về -1 để báo hiệu lỗi
        }
    }

    /**
     * Tìm một payment theo ID.
     *
     * @param id ID của payment cần tìm
     * @return Payment nếu tìm thấy, null nếu không
     */
    public Payment findById(int id) {
        try {
            return Files.lines(filePath)
                    .map(Payment::fromDataString)
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file Payment.txt: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cập nhật một payment đã có trong hệ thống.
     *
     * @param payment Payment cần cập nhật (phải có ID)
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean update(Payment payment) {
        if (payment.getId() == null) {
            return false;
        }

        try {
            // Đọc tất cả các dòng
            List<String> lines = Files.readAllLines(filePath);

            // Tìm và thay thế dòng cần cập nhật
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\\|");
                if (parts[0].equals(String.valueOf(payment.getId()))) {
                    lines.set(i, payment.toDataString());
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }

            // Ghi lại toàn bộ file
            Files.write(filePath, lines);
            return true;

        } catch (IOException e) {
            System.err.println("Lỗi khi cập nhật file Payment.txt: " + e.getMessage());
            return false;
        }
    }
}