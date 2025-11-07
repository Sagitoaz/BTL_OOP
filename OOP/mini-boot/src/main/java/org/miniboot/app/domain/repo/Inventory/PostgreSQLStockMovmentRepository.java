package org.miniboot.app.domain.repo.Inventory;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Inventory.StockMovement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgreSQLStockMovmentRepository implements StockMovementRepository {
    private final DatabaseConfig dbConfig;

    public PostgreSQLStockMovmentRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<StockMovement> findAll() {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT sm.id, sm.product_id, sm.qty, sm.move_type, sm.ref_table, sm.ref_id, " +
                "sm.batch_no, sm.expiry_date, sm.serial_no, sm.moved_at, sm.moved_by, sm.note, " +
                "p.name as product_name " +
                "FROM stock_movements sm " +
                "LEFT JOIN Products p ON sm.product_id = p.id " +
                "ORDER BY sm.moved_at DESC";

        System.out.println("🔍 Executing SQL: " + sql);

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Database connection established successfully!");
            System.out.println("Connected to: " + conn.getMetaData().getURL());
            System.out.println("✅ Query executed successfully");

            while (rs.next()) {
                System.out.println("📦 Mapping stock movement ID: " + rs.getInt("id"));
                movements.add(mapRow(rs));
            }

            System.out.println("✅ Found " + movements.size() + " stock movements");

        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR in findAll():");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch stock movements", e);
        }

        return movements;
    }

    @Override
    public Optional<StockMovement> findById(int id) {
        String sql = "SELECT id, product_id, qty, move_type, ref_table, ref_id, " +
                "batch_no, expiry_date, serial_no, moved_at, moved_by, note " +
                "FROM stock_movements WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error finding stock movement by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public StockMovement save(StockMovement movement) {
        return (movement.getId() <= 0) ? insert(movement) : update(movement);
    }

    private StockMovement insert(StockMovement m) {
        String sql = "INSERT INTO stock_movements (product_id, qty, move_type, ref_table, ref_id, " +
                "batch_no, expiry_date, serial_no, moved_at, moved_by, note) " +
                "VALUES (?,?,?::stock_movement_type,?,?,?,?,?,?,?,?) RETURNING id";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, m.getProductId());
            ps.setInt(2, m.getQty());
            ps.setString(3, m.getMoveType().toLowerCase());
            ps.setString(4, m.getRefTable());

            if (m.getRefId() != null) {
                ps.setInt(5, m.getRefId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, m.getBatchNo());

            if (m.getExpiryDate() != null) {
                ps.setDate(7, java.sql.Date.valueOf(m.getExpiryDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }

            ps.setString(8, m.getSerialNo());
            ps.setTimestamp(9, Timestamp.valueOf(
                    m.getMovedAt() != null ? m.getMovedAt() : LocalDateTime.now()));
            ps.setInt(10, m.getMovedBy());
            ps.setString(11, m.getNote());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                m.setId(rs.getInt(1));
                System.out.println("✅ Stock movement created: ID = " + m.getId());

                // ✅ UPDATE product qty_on_hand
                updateProductQuantity(conn, m.getProductId(), m.getQty());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error inserting stock movement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return m;
    }

    private List<StockMovement> insertBatch(List<StockMovement> movements) {
        String sql = "INSERT INTO stock_movements (product_id, qty, move_type, ref_table, ref_id, " +
                "batch_no, expiry_date, serial_no, moved_at, moved_by, note) " +
                "VALUES (?,?,?::stock_movement_type,?,?,?,?,?,?,?,?) RETURNING id";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Thiết lập tự động cho mỗi StockMovement
            for (StockMovement m : movements) {
                ps.setInt(1, m.getProductId());
                ps.setInt(2, m.getQty());
                ps.setString(3, m.getMoveType().toLowerCase());
                ps.setString(4, m.getRefTable());

                if (m.getRefId() != null) {
                    ps.setInt(5, m.getRefId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                ps.setString(6, m.getBatchNo());

                if (m.getExpiryDate() != null) {
                    ps.setDate(7, java.sql.Date.valueOf(m.getExpiryDate()));
                } else {
                    ps.setNull(7, Types.DATE);
                }

                ps.setString(8, m.getSerialNo());
                ps.setTimestamp(9, Timestamp.valueOf(
                        m.getMovedAt() != null ? m.getMovedAt() : LocalDateTime.now()));
                ps.setInt(10, m.getMovedBy());
                ps.setString(11, m.getNote());

                // Thêm câu lệnh vào batch
                ps.addBatch();
            }

            // Thực thi batch
            int[] updateCounts = ps.executeBatch();

            // Kiểm tra và lấy kết quả trả về (id của các stock movement mới tạo)
            for (int i = 0; i < movements.size(); i++) {
                if (updateCounts[i] > 0) {
                    // Lấy id cho mỗi StockMovement mới
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        movements.get(i).setId(rs.getInt(1));  // Gán id cho StockMovement
                        System.out.println("✅ Stock movement created: ID = " + movements.get(i).getId());
                    }
                }
            }

            // ✅ UPDATE product qty_on_hand
            for (StockMovement m : movements) {
                updateProductQuantity(conn, m.getProductId(), m.getQty());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error inserting stock movements: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return movements;
    }

    @Override
    public List<StockMovement> saveAll(List<StockMovement> movements) {
        return insertBatch(movements); // gọi hàm batch insert
    }


    private StockMovement update(StockMovement m) {
        // ⚠️ Cần lấy qty và product_id CŨ để revert, sau đó apply qty MỚI
        String selectSql = "SELECT product_id, qty FROM stock_movements WHERE id = ?";
        String updateSql = "UPDATE stock_movements SET product_id=?, qty=?, move_type=?::stock_movement_type, " +
                "ref_table=?, ref_id=?, batch_no=?, expiry_date=?, serial_no=?, " +
                "moved_at=?, moved_by=?, note=? WHERE id=?";

        try (Connection conn = dbConfig.getConnection()) {

            // 1️⃣ Lấy giá trị CŨ
            int oldProductId = 0;
            int oldQty = 0;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, m.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    oldProductId = rs.getInt("product_id");
                    oldQty = rs.getInt("qty");
                }
            }

            // 2️⃣ UPDATE stock movement
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, m.getProductId());
                ps.setInt(2, m.getQty());
                ps.setString(3, m.getMoveType().toLowerCase());
                ps.setString(4, m.getRefTable());

                if (m.getRefId() != null) {
                    ps.setInt(5, m.getRefId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                ps.setString(6, m.getBatchNo());

                if (m.getExpiryDate() != null) {
                    ps.setDate(7, java.sql.Date.valueOf(m.getExpiryDate()));
                } else {
                    ps.setNull(7, Types.DATE);
                }

                ps.setString(8, m.getSerialNo());
                ps.setTimestamp(9, Timestamp.valueOf(m.getMovedAt()));
                ps.setInt(10, m.getMovedBy());
                ps.setString(11, m.getNote());
                ps.setInt(12, m.getId());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    System.out.println("✅ Stock movement updated: ID = " + m.getId());

                    // 3️⃣ UPDATE product quantities
                    // Revert old change
                    if (oldProductId > 0) {
                        updateProductQuantity(conn, oldProductId, -oldQty); // Hoàn tác
                    }
                    // Apply new change
                    updateProductQuantity(conn, m.getProductId(), m.getQty());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating stock movement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return m;
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM stock_movements WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Stock movement deleted: ID = " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting stock movement: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private StockMovement mapRow(ResultSet rs) throws SQLException {
        try {
            StockMovement m = new StockMovement();
            m.setId(rs.getInt("id"));
            m.setProductId(rs.getInt("product_id"));
            m.setQty(rs.getInt("qty"));
            m.setMoveType(rs.getString("move_type"));
            m.setRefTable(rs.getString("ref_table"));

            Integer refId = rs.getInt("ref_id");
            if (!rs.wasNull()) {
                m.setRefId(refId);
            }

            m.setBatchNo(rs.getString("batch_no"));

            java.sql.Date expiryDate = rs.getDate("expiry_date");
            if (expiryDate != null) {
                m.setExpiryDate(expiryDate.toLocalDate());
            }

            m.setSerialNo(rs.getString("serial_no"));

            Timestamp movedAt = rs.getTimestamp("moved_at");
            if (movedAt != null) {
                m.setMovedAt(movedAt.toLocalDateTime());
            }

            m.setMovedBy(rs.getInt("moved_by"));
            m.setNote(rs.getString("note"));

            // ✅ Set product_name từ JOIN
            try {
                String productName = rs.getString("product_name");
                m.setProductName(productName);
            } catch (SQLException e) {
                // Column không tồn tại (khi query không JOIN), bỏ qua
                m.setProductName(null);
            }

            return m;
        } catch (SQLException e) {
            System.err.println("❌ ERROR mapping stock movement row:");
            System.err.println("   Column error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * ✅ UPDATE qty_on_hand trong Products table khi có stock movement
     *
     * @param conn      Connection (để dùng trong transaction)
     * @param productId ID của sản phẩm
     * @param qtyChange Số lượng thay đổi (+ nhập, - xuất)
     */
    private void updateProductQuantity(Connection conn, int productId, int qtyChange) {
        // ⚠️ IMPORTANT: Cần dùng đúng tên bảng trong database
        // Thử cả 2 cách: Products (nếu table tạo với uppercase) hoặc products (nếu
        // lowercase)
        String sql = "UPDATE Products SET qty_on_hand = qty_on_hand + ? WHERE id = ?";

        System.out.println("🔄 Updating product quantity:");
        System.out.println("   Product ID: " + productId);
        System.out.println("   Quantity Change: " + qtyChange);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyChange);
            ps.setInt(2, productId);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Updated qty_on_hand for Product ID " + productId +
                        " by " + (qtyChange > 0 ? "+" : "") + qtyChange);
            } else {
                System.err.println("⚠️ Product ID " + productId + " not found for qty update");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating product quantity: " + e.getMessage());
            System.err.println("   SQL: " + sql);
            System.err.println("   Product ID: " + productId + ", Qty Change: " + qtyChange);
            e.printStackTrace();
        }
    }

}
