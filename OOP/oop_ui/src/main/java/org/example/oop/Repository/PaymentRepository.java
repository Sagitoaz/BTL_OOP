package org.example.oop.Repository;

import org.example.oop.Model.PaymentModel.Payment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository quản lý việc truy cập dữ liệu cho Payment (hóa đơn).
 * Chịu trách nhiệm đọc, ghi, và quản lý ID từ file Payment.txt.
 */
public class PaymentRepository {
    private static final String RESOURCE_PATH = "/Data/payment.txt";

    /**
     * Tìm ID lớn nhất hiện có trong file để tạo ID tiếp theo.
     *
     * @return ID tiếp theo có thể sử dụng.
     */
    private int getNextId() {
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(RESOURCE_PATH)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi định dạng ID trong dòng: " + line);
                    }
                }
            }
            return maxId + 1;
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

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getClass().getResource(RESOURCE_PATH).getFile(), true))) {
            writer.write(dataLine);
            writer.newLine();
            return nextId;
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file Payment.txt: " + e.getMessage());
            return -1; // Trả về -1 để báo hiệu lỗi
        }
    }

    /**
     * Tìm tất cả các payment trong hệ thống.
     *
     * @return Danh sách các payment
     */
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(RESOURCE_PATH)))) {
            String line;
            while ((line = br.readLine()) != null) {
                payments.add(Payment.fromDataString(line));
            }
            return payments;
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file Payment.txt: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tìm một payment theo ID.
     *
     * @param id ID của payment cần tìm
     * @return Payment nếu tìm thấy, null nếu không
     */
    public Payment findById(int id) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(RESOURCE_PATH)))) {
            String line;
            while ((line = br.readLine()) != null) {
                Payment payment = Payment.fromDataString(line);
                if (payment.getId() == id) {
                    return payment;
                }
            }
            return null;
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

        List<String> lines = new ArrayList<>();
        boolean found = false;

        // Đọc tất cả các dòng
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(RESOURCE_PATH)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(String.valueOf(payment.getId()))) {
                    lines.add(payment.toDataString());
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file Payment.txt: " + e.getMessage());
            return false;
        }

        if (!found) {
            return false;
        }

        // Ghi lại toàn bộ file
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getClass().getResource(RESOURCE_PATH).getFile()))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi cập nhật file Payment.txt: " + e.getMessage());
            return false;
        }
    }
}