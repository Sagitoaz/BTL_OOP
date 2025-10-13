package org.example.oop.Repository;


import org.example.oop.Model.PaymentModel.PaymentItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp Repository quản lý việc truy cập dữ liệu cho PaymentItem.
 * Nó trừu tượng hóa việc đọc/ghi dữ liệu từ file văn bản,
 * giúp Controller không cần quan tâm đến cách dữ liệu được lưu trữ.
 */
public class PaymentItemRepository {

    // Đường dẫn tới file dữ liệu. Trong thực tế, nên đưa vào file cấu hình.
    private final Path filePath = Paths.get("src/main/resources/data/payment_item.txt");

    /**
     * Constructor đảm bảo file dữ liệu tồn tại khi repository được khởi tạo.
     */
    public PaymentItemRepository() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            // Trong ứng dụng thực tế, nên sử dụng một framework ghi log chuyên nghiệp
            System.err.println("Lỗi nghiêm trọng khi khởi tạo file PaymentItem.txt: " + e.getMessage());
        }
    }

    /**
     * Tìm tất cả các PaymentItem thuộc về một hóa đơn (Payment) cụ thể.
     *
     * @param paymentId ID của hóa đơn cần tìm các mặt hàng.
     * @return Danh sách các PaymentItem tìm thấy. Trả về danh sách rỗng nếu có lỗi.
     */
    public List<PaymentItem> findByPaymentId(int paymentId) {
        try {
            return Files.lines(filePath)
                    .map(PaymentItem::fromDataString)
                    .filter(item -> item.getPaymentId() == paymentId)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file PaymentItem.txt: " + e.getMessage());
            return Collections.emptyList(); // An toàn hơn là trả về null
        }
    }

    /**
     * Lưu một danh sách các PaymentItem vào cuối file.
     *
     * @param items Danh sách các mặt hàng cần lưu.
     */
    public void saveAll(List<PaymentItem> items) {
        if (items == null || items.isEmpty()) {
            return; // Không làm gì nếu danh sách rỗng
        }

        List<String> lines = items.stream()
                .map(PaymentItem::toDataString)
                .collect(Collectors.toList());
        try {
            // Ghi tất cả các dòng vào cuối file, tạo file nếu chưa có
            Files.write(filePath, lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file PaymentItem.txt: " + e.getMessage());
        }
    }
}