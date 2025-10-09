package org.example.oop.Model.PaymentModel;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    /**
     * Tìm trạng thái mới nhất của một payment từ danh sách lịch sử.
     *
     * @param paymentId ID của payment cần tìm.
     * @param allLogs   Danh sách TẤT CẢ các bản ghi log đã đọc từ file.
     * @return Trạng thái mới nhất, hoặc Optional.empty() nếu không tìm thấy.
     */
    public Optional<PaymentStatus> findCurrentStatus(int paymentId, List<PaymentStatusLog> allLogs) {
        return allLogs.stream()
                // 1. Lọc ra các log của payment này
                .filter(log -> log.getPaymentId() == paymentId)
                // 2. Tìm log có changedAt lớn nhất (mới nhất)
                .max(Comparator.comparing(PaymentStatusLog::getChangedAt))
                // 3. Lấy ra status từ log đó
                .map(PaymentStatusLog::getStatus);
    }
}


/* Giả sử bạn đã đọc toàn bộ file payment_status_log.txt vào một List tên là 'statusLogs'
PaymentService service = new PaymentService();
Optional<PaymentStatus> currentStatus = service.findCurrentStatus(3, statusLogs); // Tìm status của payment có ID = 3

if (currentStatus.isPresent()) {
        System.out.println("Trạng thái hiện tại: " + currentStatus.get()); // Sẽ in ra FAILED
        }

*/