package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Payment.PaymentItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgreSQLPaymentItemRepository implements PaymentItemRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLPaymentItemRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /*  SELECT  */

    @Override
    public List<PaymentItem> findByPaymentId(int paymentId) {
        final String sql = """
                SELECT id, product_id, payment_id, description, qty, unit_price, total_line
                FROM payment_items
                WHERE payment_id = ?
                ORDER BY id ASC
                """;
        List<PaymentItem> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ findByPaymentId error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<PaymentItem> findById(int paymentItemId) {
        final String sql = """
                SELECT id, product_id, payment_id, description, qty, unit_price, total_line
                FROM payment_items
                WHERE id = ?
                """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ findById error: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /*  SAVE  */

    @Override
    public PaymentItem save(PaymentItem item) {
        Integer id = item.getId();
        boolean insert = (id == null || id == 0);
        return insert ? insert(item) : update(item);
    }

    @Override
    public List<PaymentItem> saveAll(int paymentId, List<PaymentItem> items) {
        List<PaymentItem> out = new ArrayList<>(items.size());
        try (Connection conn = dbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (PaymentItem it : items) {
                    if (it.getPaymentId() == 0) it.setPaymentId(paymentId);
                    out.add(saveTx(conn, it));
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("❌ saveAll error: " + e.getMessage());
            e.printStackTrace();
        }
        return out;
    }

    /*  DELETE  */

    @Override
    public boolean deleteById(int paymentItemId) {
        final String sql = "DELETE FROM payment_items WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentItemId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ deleteById error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int deleteByPaymentId(int paymentId) {
        final String sql = "DELETE FROM payment_items WHERE payment_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ deleteByPaymentId error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<PaymentItem> replaceAllForPayment(int paymentId, List<PaymentItem> newItems) {
        List<PaymentItem> out = new ArrayList<>(newItems.size());
        try (Connection conn = dbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Xoá cũ
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM payment_items WHERE payment_id = ?")) {
                    ps.setInt(1, paymentId);
                    ps.executeUpdate();
                }
                // Thêm mới
                for (PaymentItem it : newItems) {
                    it.setId(null);
                    it.setPaymentId(paymentId);
                    out.add(insertTx(conn, it));
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("❌ replaceAllForPayment error: " + e.getMessage());
            e.printStackTrace();
        }
        return out;
    }

    /*  PRIVATE HELPERS  */

    private PaymentItem insert(PaymentItem it) {
        try (Connection conn = dbConfig.getConnection()) {
            return insertTx(conn, it);
        } catch (SQLException e) {
            System.err.println("❌ insert error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private PaymentItem update(PaymentItem it) {
        try (Connection conn = dbConfig.getConnection()) {
            return updateTx(conn, it);
        } catch (SQLException e) {
            System.err.println("❌ update error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // dùng trong transaction có sẵn
    private PaymentItem saveTx(Connection conn, PaymentItem it) throws SQLException {
        Integer id = it.getId();
        boolean insert = (id == null || id == 0);
        return insert ? insertTx(conn, it) : updateTx(conn, it);
    }

    private PaymentItem insertTx(Connection conn, PaymentItem it) throws SQLException {
        final String sql = """
                INSERT INTO payment_items
                    (product_id, payment_id, description, qty, unit_price, total_line)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (it.getProductId() == null || it.getProductId() == 0) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, it.getProductId());
            ps.setInt(2, it.getPaymentId());
            ps.setString(3, it.getDescription());
            ps.setInt(4, it.getQty());
            ps.setInt(5, it.getUnitPrice());
            ps.setInt(6, it.getTotalLine());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) it.setId(rs.getInt(1));
            }
            return it;
        }
    }

    private PaymentItem updateTx(Connection conn, PaymentItem it) throws SQLException {
        if (it.getId() == null || it.getId() == 0) {
            throw new IllegalArgumentException("PaymentItem.update: id must not be null/0");
        }
        final String sql = """
                UPDATE payment_items
                SET product_id = ?, payment_id = ?, description = ?,
                    qty = ?, unit_price = ?, total_line = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (it.getProductId() == null || it.getProductId() == 0) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, it.getProductId());
            ps.setInt(2, it.getPaymentId());
            ps.setString(3, it.getDescription());
            ps.setInt(4, it.getQty());
            ps.setInt(5, it.getUnitPrice());
            ps.setInt(6, it.getTotalLine());
            ps.setInt(7, it.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ PaymentItem update failed, id=" + it.getId());
                return null;
            }
            return it;
        }
    }

    private PaymentItem map(ResultSet rs) throws SQLException {
        PaymentItem it = new PaymentItem(
                rs.getInt("id"),
                rs.getInt("product_id"),
                rs.getInt("payment_id"),
                rs.getString("description"),
                rs.getInt("qty"),
                rs.getInt("unit_price"),
                rs.getInt("total_line")
        );
        return it;
    }
}
