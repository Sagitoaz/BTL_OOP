package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Payment.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgreSQLPaymentStatusLogRepository implements PaymentStatusLogRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLPaymentStatusLogRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public PaymentStatus setCurrentPaymentStatus(int paymentId, PaymentStatus targetStatus) {
        if (targetStatus == null) {
            System.err.println("❌ setCurrentPaymentStatus: targetStatus is null");
            return null;
        }

        // 1) Lấy trạng thái hiện tại
        PaymentStatus current = getCurrentPaymentStatus(paymentId);

        // 2) Nếu đang ở trạng thái kết thúc (PAID/CANCELLED) → không cho đổi nữa
        if (isTerminal(current)) {
            System.out.println("🚫 Payment " + paymentId + " is terminal (" + current + "), skip change to " + targetStatus);
            return current; // trả về hiện trạng, không chèn log mới
        }

        // 3) Idempotent: nếu đặt lại đúng trạng thái hiện tại → bỏ qua
        if (current == targetStatus) {
            System.out.println("ℹ️ Payment " + paymentId + " already in status " + targetStatus + ", no-op");
            return current;
        }

        // 4) Ghi log trạng thái mới
        final String sql = """
                INSERT INTO payment_status_log (payment_id, status, changed_at)
                VALUES (?, ?::payment_status, CURRENT_TIMESTAMP)
                RETURNING status
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            ps.setString(2, targetStatus.name()); // enum JAVA trùng 'UNPAID','PENDING','PAID','CANCELLED'

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String s = rs.getString(1);
                    // Nếu bạn có fromCode thì dùng, còn không valueOf là đủ
                    try {
                        return PaymentStatus.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        try {
                            return PaymentStatus.fromCode(s);
                        } catch (Throwable ignore) {
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ setCurrentPaymentStatus error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PaymentStatus getCurrentPaymentStatus(int paymentId) {
        final String sql = """
                SELECT status
                FROM payment_status_log
                WHERE payment_id = ?
                ORDER BY changed_at DESC, id DESC
                LIMIT 1
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String s = rs.getString("status");
                    try {
                        return PaymentStatus.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        try {
                            return PaymentStatus.fromCode(s);
                        } catch (Throwable ignore) {
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ getCurrentPaymentStatus error: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // chưa có log nào
    }

    // Trạng thái kết thúc: không cho chuyển tiếp nữa
    private boolean isTerminal(PaymentStatus s) {
        return s == PaymentStatus.PAID || s == PaymentStatus.CANCELLED;
    }
}
