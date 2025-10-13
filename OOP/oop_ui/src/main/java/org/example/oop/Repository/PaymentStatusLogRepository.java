package org.example.oop.Repository;

import org.example.oop.Model.PaymentModel.PaymentStatus;
import org.example.oop.Model.PaymentModel.PaymentStatusLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentStatusLogRepository {
    private final Path filePath = Paths.get("src/main/resources/data/payment_status_log.txt");

    public PaymentStatusLogRepository() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi tạo file payment_status_log.txt: " + e.getMessage());
        }
    }

    public PaymentStatus findCurrentStatus(int paymentId) {
        try {
            return Files.lines(filePath)
                    .map(PaymentStatusLog::fromDataString)
                    .filter(log -> log.getPaymentId() == paymentId)
                    .max(Comparator.comparing(PaymentStatusLog::getChangedAt))
                    .map(PaymentStatusLog::getStatus)
                    .orElse(PaymentStatus.UNPAID); // Mặc định là chưa thanh toán nếu không tìm thấy
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file payment_status_log.txt: " + e.getMessage());
            return PaymentStatus.CANCELLED; // Trường hợp lỗi đọc file, đánh dấu là đã hủy
        }
    }

    public List<PaymentStatusLog> findAllByPaymentId(int paymentId) {
        try {
            return Files.lines(filePath)
                    .map(PaymentStatusLog::fromDataString)
                    .filter(log -> log.getPaymentId() == paymentId)
                    .sorted(Comparator.comparing(PaymentStatusLog::getChangedAt))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file payment_status_log.txt: " + e.getMessage());
            return List.of();
        }
    }

    public boolean save(PaymentStatusLog log) {
        if (log.getId() == null) {
            log = new PaymentStatusLog(getNextId(), log.getPaymentId(),
                    Instant.now(), log.getStatus());
        }

        String line = log.toDataString();
        try {
            Files.write(filePath, (line + "\n").getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file payment_status_log.txt: " + e.getMessage());
            return false;
        }
    }

    private int getNextId() {
        try {
            return Files.lines(filePath)
                    .map(line -> line.split("\\|")[0])
                    .filter(idStr -> !idStr.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0) + 1;
        } catch (IOException e) {
            return 1;
        }
    }
}