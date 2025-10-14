package org.example.oop.Repository;

import org.example.oop.Model.PaymentModel.PaymentItem;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp Repository quản lý việc truy cập dữ liệu cho PaymentItem.
 * Nó trừu tượng hóa việc đọc/ghi dữ liệu từ file văn bản,
 * giúp Controller không cần quan tâm đến cách dữ liệu được lưu trữ.
 */
public class PaymentItemRepository {
    private static final String RESOURCE_PATH = "/Data/payment_item.txt";

    /**
     * Tìm tất cả các PaymentItem thuộc về một hóa đơn (Payment) cụ thể.
     *
     * @param paymentId ID của hóa đơn cần tìm các mặt hàng.
     * @return Danh sách các PaymentItem tìm thấy. Trả về danh sách rỗng nếu có lỗi.
     */
    public List<PaymentItem> findByPaymentId(int paymentId) {
        List<PaymentItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(RESOURCE_PATH)))) {
            String line;
            while ((line = br.readLine()) != null) {
                PaymentItem item = PaymentItem.fromDataString(line);
                if (item.getPaymentId() == paymentId) {
                    items.add(item);
                }
            }
            return items;
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

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getClass().getResource(RESOURCE_PATH).getFile(), true))) {
            for (PaymentItem item : items) {
                writer.write(item.toDataString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file PaymentItem.txt: " + e.getMessage());
        }
    }
}