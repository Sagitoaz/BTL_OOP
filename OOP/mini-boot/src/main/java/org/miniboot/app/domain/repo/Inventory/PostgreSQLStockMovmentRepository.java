package org.miniboot.app.domain.repo.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.Inventory.StockMovement;

public class PostgreSQLStockMovmentRepository implements StockMovementRepository {
     private final DatabaseConfig dbConfig;

     public PostgreSQLStockMovmentRepository() {
          this.dbConfig = DatabaseConfig.getInstance();
     }

     @Override
     public List<StockMovement> findAll() {
          List<StockMovement> movements = new ArrayList<>();
          // ✅ JOIN với Products để lấy product_name
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
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?) RETURNING id";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setInt(1, m.getProductId());
               ps.setInt(2, m.getQty());
               ps.setString(3, m.getMoveType());
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
               }
          } catch (SQLException e) {
               System.err.println("❌ Error inserting stock movement: " + e.getMessage());
               e.printStackTrace();
               return null;
          }

          return m;
     }

     private StockMovement update(StockMovement m) {
          String sql = "UPDATE stock_movements SET product_id=?, qty=?, move_type=?, " +
                    "ref_table=?, ref_id=?, batch_no=?, expiry_date=?, serial_no=?, " +
                    "moved_at=?, moved_by=?, note=? WHERE id=?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setInt(1, m.getProductId());
               ps.setInt(2, m.getQty());
               ps.setString(3, m.getMoveType());
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

}
