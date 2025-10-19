package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentMethod;

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

    public static void main(String[] args) {
        System.out.println("🧪 Testing PostgreSQLPaymentRepository...\n");
        PostgreSQLPaymentRepository repo = new PostgreSQLPaymentRepository();

        // Test 1: get all
        List<Payment> all = repo.getPayments();
        System.out.println("Total payments: " + all.size());
        if (!all.isEmpty()) {
            Payment f = all.get(0);
            System.out.println("First payment: ID=" + f.getId() + ", Code=" + f.getCode());
        }

        // Test 2: get by id (nếu có)
        if (!all.isEmpty()) {
            int testId = all.get(0).getId();
            repo.getPaymentById(testId).ifPresentOrElse(
                    p -> System.out.println("Found payment ID=" + p.getId() + ", Code=" + p.getCode()),
                    () -> System.out.println("Not found id=" + testId)
            );
        }

        // Test 3: INSERT
        Payment np = new Payment();
        np.setCode("PMT-" + System.currentTimeMillis());
        np.setCustomerId(0);
        np.setCashierId(1);
        np.setIssuedAt(LocalDateTime.now());
        np.setSubtotal(100000);
        np.setDiscount(5000);
        np.setTaxTotal(10000);
        np.setRounding(0);
        np.setGrandTotal(105000);
        np.setPaymentMethod(PaymentMethod.CASH);
        np.setAmountPaid(105000);
        np.setNote("Test payment insert");
        np.setCreatedAt(LocalDateTime.now());

        Payment inserted = repo.savePayment(np);
        if (inserted != null && inserted.getId() != null && inserted.getId() > 0) {
            System.out.println("✅ Inserted ID=" + inserted.getId() + ", Code=" + inserted.getCode());

            // Test 4: UPDATE
            inserted.setNote("Updated note " + LocalDateTime.now());
            inserted.setAmountPaid(inserted.getAmountPaid() + 10000);
            Payment updated = repo.savePayment(inserted);
            System.out.println(updated != null
                    ? "✅ Updated ID=" + updated.getId() + ", New amount=" + updated.getAmountPaid()
                    : "❌ Update failed");
        } else {
            System.out.println("❌ Insert failed!");
        }
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
                if (rs.next()) return Optional.of(mapResultSetToPayment(rs));
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
        if (payment.getIssuedAt() == null) payment.setIssuedAt(now);
        if (payment.getCreatedAt() == null) payment.setCreatedAt(now);

        Integer id = payment.getId();
        boolean isInsert = (id == null || id == 0);  // tránh NPE
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
            if (p.getCustomerId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, p.getCustomerId());
            ps.setInt(3, p.getCashierId());
            ps.setTimestamp(4, Timestamp.valueOf(p.getIssuedAt()));
            ps.setInt(5, p.getSubtotal());
            ps.setInt(6, p.getDiscount());
            ps.setInt(7, p.getTaxTotal());
            ps.setInt(8, p.getRounding());
            ps.setInt(9, p.getGrandTotal());
            if (p.getPaymentMethod() == null) ps.setNull(10, Types.OTHER, "payment_method");
            else ps.setString(10, p.getPaymentMethod().getCode());
            ps.setInt(11, p.getAmountPaid());
            ps.setString(12, p.getNote());
            ps.setTimestamp(13, Timestamp.valueOf(p.getCreatedAt()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setId(rs.getInt(1)); // lấy id tự tăng
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
            if (p.getCustomerId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, p.getCustomerId());
            ps.setInt(3, p.getCashierId());
            ps.setTimestamp(4, Timestamp.valueOf(p.getIssuedAt()));
            ps.setInt(5, p.getSubtotal());
            ps.setInt(6, p.getDiscount());
            ps.setInt(7, p.getTaxTotal());
            ps.setInt(8, p.getRounding());
            ps.setInt(9, p.getGrandTotal());
            if (p.getPaymentMethod() == null) ps.setNull(10, Types.OTHER, "payment_method");
            else ps.setString(10, p.getPaymentMethod().getCode());
            ps.setInt(11, p.getAmountPaid());
            ps.setString(12, p.getNote());
            ps.setInt(13, p.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) return null;
            return p;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id")); // PK not null
        p.setCode(rs.getString("code"));
        Integer customerId = (Integer) rs.getObject("customer_id");
        p.setCustomerId(customerId == null ? 0 : customerId);
        p.setCashierId(rs.getInt("cashier_id"));

        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null) p.setIssuedAt(issuedAt.toLocalDateTime());

        p.setSubtotal(rs.getInt("subtotal"));
        p.setDiscount(rs.getInt("discount"));
        p.setTaxTotal(rs.getInt("tax_total"));
        p.setRounding(rs.getInt("rounding"));
        p.setGrandTotal(rs.getInt("grand_total"));

        String method = rs.getString("payment_method");
        if (method != null) p.setPaymentMethod(PaymentMethod.fromCode(method));

        p.setAmountPaid(rs.getInt("amount_paid"));
        p.setNote(rs.getString("note"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());
        return p;
    }

}
