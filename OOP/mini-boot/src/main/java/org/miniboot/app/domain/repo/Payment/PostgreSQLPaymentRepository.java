package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentMethod;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgreSQLPaymentRepository implements PaymentRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLPaymentRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<Payment> getPayments() {
        List<Payment> payments = new ArrayList<>();
        final String sql = """
                SELECT id, code, customer_id, cashier_id, issued_at,
                       subtotal, discount, tax_total, rounding, grand_total,
                       payment_method, amount_paid, note, created_at
                FROM payments
                ORDER BY created_at DESC
                """;

        try (Connection conn = dbConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }

            System.out.println("✅ Found " + payments.size() + " payments in database");
        } catch (SQLException e) {
            System.err.println("❌ Error fetching payments: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public Optional<Payment> getPaymentById(int id) {
        final String sql = """
                SELECT id, code, customer_id, cashier_id, issued_at,
                       subtotal, discount, tax_total, rounding, grand_total,
                       payment_method, amount_paid, note, created_at
                FROM payments
                WHERE id = ?
                """;

        try (Connection conn = dbConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding payment by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Payment savePayment(Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        if (payment.getIssuedAt() == null)
            payment.setIssuedAt(now);
        if (payment.getCreatedAt() == null)
            payment.setCreatedAt(now);

        Integer id = payment.getId();
        boolean isInsert = (id == null || id == 0); // tránh NPE
        return isInsert ? insert(payment) : update(payment);
    }

    private Payment insert(Payment p) {
        final String sql = """
                INSERT INTO payments
                (code, customer_id, cashier_id, issued_at,
                 subtotal, discount, tax_total, rounding, grand_total,
                 payment_method, amount_paid, note, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::payment_method, ?, ?, ?)
                RETURNING id;
                """;
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCode());
            if (p.getCustomerId() == 0)
                ps.setNull(2, Types.INTEGER);
            else
                ps.setInt(2, p.getCustomerId());
            ps.setInt(3, p.getCashierId());
            ps.setTimestamp(4, Timestamp.valueOf(p.getIssuedAt()));
            ps.setInt(5, p.getSubtotal());
            ps.setInt(6, p.getDiscount());
            ps.setInt(7, p.getTaxTotal());
            ps.setInt(8, p.getRounding());
            ps.setInt(9, p.getGrandTotal());
            if (p.getPaymentMethod() == null)
                ps.setNull(10, Types.OTHER, "payment_method");
            else
                ps.setString(10, p.getPaymentMethod().getCode());
            // ✅ Handle amountPaid as nullable Integer
            if (p.getAmountPaid() == null)
                ps.setNull(11, Types.INTEGER);
            else
                ps.setInt(11, p.getAmountPaid());
            ps.setString(12, p.getNote());
            ps.setTimestamp(13, Timestamp.valueOf(p.getCreatedAt()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    p.setId(rs.getInt(1)); // lấy id tự tăng
            }
            return p;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Payment update(Payment p) {
        if (p.getId() == null || p.getId() == 0)
            throw new IllegalArgumentException("Payment.update: id must not be null/0");

        final String sql = """
                UPDATE payments
                SET code = ?, customer_id = ?, cashier_id = ?, issued_at = ?,
                    subtotal = ?, discount = ?, tax_total = ?, rounding = ?, grand_total = ?,
                    payment_method = ?::payment_method, amount_paid = ?, note = ?
                WHERE id = ?
                """;
        try (Connection conn = dbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCode());
            if (p.getCustomerId() == 0)
                ps.setNull(2, Types.INTEGER);
            else
                ps.setInt(2, p.getCustomerId());
            ps.setInt(3, p.getCashierId());
            ps.setTimestamp(4, Timestamp.valueOf(p.getIssuedAt()));
            ps.setInt(5, p.getSubtotal());
            ps.setInt(6, p.getDiscount());
            ps.setInt(7, p.getTaxTotal());
            ps.setInt(8, p.getRounding());
            ps.setInt(9, p.getGrandTotal());
            if (p.getPaymentMethod() == null)
                ps.setNull(10, Types.OTHER, "payment_method");
            else
                ps.setString(10, p.getPaymentMethod().getCode());
            // ✅ Handle amountPaid as nullable Integer
            if (p.getAmountPaid() == null)
                ps.setNull(11, Types.INTEGER);
            else
                ps.setInt(11, p.getAmountPaid());
            ps.setString(12, p.getNote());
            ps.setInt(13, p.getId());

            int rows = ps.executeUpdate();
            if (rows == 0)
                return null;
            return p;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy tất cả các Payment cùng với trạng thái PaymentStatus hiện tại.
     *
     * @return Danh sách các đối tượng PaymentWithStatus.
     */
    public List<PaymentWithStatus> getAllPaymentsWithStatus() {
        // 1. Thay đổi kiểu trả về
        List<PaymentWithStatus> result = new ArrayList<>();

        // 2. Sử dụng SQL hiệu năng hơn với Window Function
        final String sql = """
                WITH RankedStatus AS (
                    -- Đánh số thứ tự status của mỗi payment, mới nhất là 1
                    SELECT
                        payment_id,
                        status,
                        changed_at,
                        ROW_NUMBER() OVER(PARTITION BY payment_id ORDER BY changed_at DESC) as rn
                    FROM
                        payment_status_log
                )
                SELECT
                    p.id, p.code, p.customer_id, p.cashier_id, p.issued_at,
                    p.subtotal, p.discount, p.tax_total, p.rounding, p.grand_total,
                    p.payment_method, p.amount_paid, p.note, p.created_at,
                    rs.status, rs.changed_at AS status_updated_at  -- Lấy status và thời gian cập nhật từ RankedStatus
                FROM
                    payments p
                LEFT JOIN
                    RankedStatus rs ON p.id = rs.payment_id AND rs.rn = 1 -- Chỉ join với status mới nhất
                ORDER BY
                    p.created_at DESC
                """;

        try (Connection conn = dbConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Lấy Payment
                Payment payment = mapResultSetToPayment(rs); // Giả sử bạn có hàm này

                // 3. Xử lý status (QUAN TRỌNG: Phải kiểm tra NULL)
                String statusStr = rs.getString("status");
                PaymentStatus paymentStatus = null; // Mặc định là null nếu không có status

                if (statusStr != null) {
                    paymentStatus = PaymentStatus.valueOf(statusStr);
                }

                // 4. Lấy thời gian cập nhật status
                java.sql.Timestamp statusUpdatedTs = rs.getTimestamp("status_updated_at");
                LocalDateTime statusUpdatedAt = statusUpdatedTs != null ? statusUpdatedTs.toLocalDateTime() : null;

                // 5. Thêm vào kết quả bằng DTO
                result.add(new PaymentWithStatus(payment, paymentStatus, statusUpdatedAt));
            }

            System.out.println("✅ Found " + result.size() + " payments with current status");
        } catch (SQLException e) {
            System.err.println("❌ Error fetching payments with status: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /// helper
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id")); // PK not null
        p.setCode(rs.getString("code"));
        Integer customerId = (Integer) rs.getObject("customer_id");
        p.setCustomerId(customerId == null ? 0 : customerId);
        p.setCashierId(rs.getInt("cashier_id"));

        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null)
            p.setIssuedAt(issuedAt.toLocalDateTime());

        p.setSubtotal(rs.getInt("subtotal"));
        p.setDiscount(rs.getInt("discount"));
        p.setTaxTotal(rs.getInt("tax_total"));
        p.setRounding(rs.getInt("rounding"));
        p.setGrandTotal(rs.getInt("grand_total"));

        String method = rs.getString("payment_method");
        if (method != null)
            p.setPaymentMethod(PaymentMethod.fromCode(method));

        // ✅ Handle amountPaid as nullable Integer when reading from DB
        Integer amountPaid = (Integer) rs.getObject("amount_paid");
        p.setAmountPaid(amountPaid); // Set null if not paid yet
        p.setNote(rs.getString("note"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            p.setCreatedAt(createdAt.toLocalDateTime());
        return p;
    }

}
