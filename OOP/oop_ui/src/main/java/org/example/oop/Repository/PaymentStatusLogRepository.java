package org.example.oop.Repository;

import org.example.oop.Model.PaymentModel.PaymentStatus;
import org.example.oop.Model.PaymentModel.PaymentStatusLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaymentStatusLogRepository {
    private static final String RESOURCE_PATH = "/data/payment_status_log.txt";
    }

    public PaymentStatus findCurrentStatus(int paymentId) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            PaymentStatusLog latestLog = null;
            String line;
            while ((line = reader.readLine()) != null) {
                PaymentStatusLog log = PaymentStatusLog.fromDataString(line);
                if (log.getPaymentId() == paymentId) {
                    if (latestLog == null || log.getChangedAt().isAfter(latestLog.getChangedAt())) {
                        latestLog = log;
                    }
                }
            }
            return latestLog != null ? latestLog.getStatus() : PaymentStatus.UNPAID;
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file payment_status_log.txt: " + e.getMessage());
            return PaymentStatus.CANCELLED;
        }
    }

    public List<PaymentStatusLog> findAllByPaymentId(int paymentId) {
        List<PaymentStatusLog> logs = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                PaymentStatusLog log = PaymentStatusLog.fromDataString(line);
                if (log.getPaymentId() == paymentId) {
                    logs.add(log);
                }
            }
            logs.sort(Comparator.comparing(PaymentStatusLog::getChangedAt));
            return logs;
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
        int maxId = 0;
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
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
            return 1;
        }
    }
}