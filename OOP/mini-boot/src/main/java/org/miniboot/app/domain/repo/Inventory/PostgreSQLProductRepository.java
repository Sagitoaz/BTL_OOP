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
import org.miniboot.app.domain.models.Inventory.Product;

public class PostgreSQLProductRepository implements ProductRepository {
     private final DatabaseConfig dbConfig;

     public PostgreSQLProductRepository() {
          this.dbConfig = DatabaseConfig.getInstance();
     }

     @Override
     public List<Product> findAll() {
          List<Product> products = new ArrayList<>();
          String sql = "SELECT id, sku, name, category, unit, price_cost, price_retail, " +
                    "is_active, qty_on_hand, batch_no, expiry_date, serial_no, note, created_at " +
                    "FROM Products WHERE is_active = TRUE ORDER BY name";

          System.out.println("🔍 Executing SQL: " + sql);

          try (Connection conn = dbConfig.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

               System.out.println("✅ Query executed successfully");

               while (rs.next()) {
                    System.out.println("📦 Mapping product ID: " + rs.getInt("id"));
                    products.add(mapRow(rs));
               }

               System.out.println("✅ Found " + products.size() + " products");

          } catch (SQLException e) {
               System.err.println("❌ SQL ERROR in findAll():");
               System.err.println("   Message: " + e.getMessage());
               System.err.println("   SQL State: " + e.getSQLState());
               System.err.println("   Error Code: " + e.getErrorCode());
               e.printStackTrace();
               throw new RuntimeException("Failed to fetch products", e);
          }

          return products;
     }

     @Override
     public Optional<Product> findById(int id) {
          String sql = "SELECT id, sku, name, category, unit, price_cost, price_retail, " +
                    "is_active, qty_on_hand, batch_no, expiry_date, serial_no, note, created_at " +
                    "FROM Products WHERE id = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setInt(1, id);
               ResultSet rs = ps.executeQuery();

               if (rs.next()) {
                    return Optional.of(mapRow(rs));
               }

          } catch (SQLException e) {
               System.err.println("Error finding product: " + e.getMessage());
          }

          return Optional.empty();
     }

     @Override
     public Product save(Product product) {
          return (product.getId() <= 0) ? insert(product) : update(product);
     }

     private Product insert(Product p) {
          // ✅ FIX: Cast category to product_category ENUM type
          String sql = "INSERT INTO Products (sku, name, category, unit, price_cost, price_retail, " +
                    "is_active, qty_on_hand, batch_no, expiry_date, serial_no, note, created_at) " +
                    "VALUES (?,?,?::product_category,?,?,?,?,?,?,?,?,?,?) RETURNING id";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setString(1, p.getSku());
               ps.setString(2, p.getName());
               ps.setString(3, p.getCategory());
               ps.setString(4, p.getUnit());
               setInteger(ps, 5, p.getPriceCost());
               setInteger(ps, 6, p.getPriceRetail());
               ps.setBoolean(7, p.isActive());
               ps.setInt(8, p.getQtyOnHand());
               ps.setString(9, p.getBatchNo());
               setDate(ps, 10, p.getExpiryDate());
               ps.setString(11, p.getSerialNo());
               ps.setString(12, p.getNote());
               ps.setTimestamp(13, Timestamp.valueOf(
                         p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()));

               ResultSet rs = ps.executeQuery();
               if (rs.next()) {
                    p.setId(rs.getInt(1));
                    System.out.println("Product created: ID = " + p.getId());
               }
          } catch (SQLException e) {
               System.err.println("Error inserting product: " + e.getMessage());
               e.printStackTrace();
               return null;
          }

          return p;
     }

     private Product update(Product p) {
          String sql = "UPDATE Products SET sku=?, name=?, category=?::product_category, unit=?, " +
                    "price_cost=?, price_retail=?, is_active=?, qty_on_hand=?, " +
                    "batch_no=?, expiry_date=?, serial_no=?, note=? WHERE id=?";

          System.out.println("🔄 Updating product ID: " + p.getId());
          System.out.println("   SKU: " + p.getSku());
          System.out.println("   Name: " + p.getName());
          System.out.println("   Category: " + p.getCategory());
          System.out.println("   SQL: " + sql);

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setString(1, p.getSku());
               ps.setString(2, p.getName());
               ps.setString(3, p.getCategory());
               ps.setString(4, p.getUnit());
               setInteger(ps, 5, p.getPriceCost());
               setInteger(ps, 6, p.getPriceRetail());
               ps.setBoolean(7, p.isActive());
               ps.setInt(8, p.getQtyOnHand());
               ps.setString(9, p.getBatchNo());
               setDate(ps, 10, p.getExpiryDate());
               ps.setString(11, p.getSerialNo());
               ps.setString(12, p.getNote());
               ps.setInt(13, p.getId());

               System.out.println("📤 Executing UPDATE query...");
               int affected = ps.executeUpdate();
               System.out.println("✅ Rows affected: " + affected);

               if (affected > 0) {
                    System.out.println("✅ Product updated successfully: ID = " + p.getId());
                    return p;
               } else {
                    System.err.println("⚠️ WARNING: No rows affected. Product ID may not exist: " + p.getId());
                    return null;
               }

          } catch (SQLException e) {
               System.err.println("❌ SQL ERROR in update():");
               System.err.println("   Message: " + e.getMessage());
               System.err.println("   SQL State: " + e.getSQLState());
               System.err.println("   Error Code: " + e.getErrorCode());
               e.printStackTrace();
               return null;
          }
     }

     @Override
     public boolean deleteById(int id) {
          String sql = "UPDATE Products SET is_active = FALSE WHERE id = ?";

          try (Connection conn = dbConfig.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setInt(1, id);
               return ps.executeUpdate() > 0;

          } catch (SQLException e) {
               System.err.println("Error deleting product: " + e.getMessage());
          }

          return false;
     }

     private Product mapRow(ResultSet rs) throws SQLException {
          try {
               Product p = new Product();
               p.setId(rs.getInt("id"));
               p.setSku(rs.getString("sku"));
               p.setName(rs.getString("name"));
               p.setCategory(rs.getString("category"));
               p.setUnit(rs.getString("unit"));
               p.setPriceCost(rs.getObject("price_cost", Integer.class));
               p.setPriceRetail(rs.getObject("price_retail", Integer.class));
               p.setActive(rs.getBoolean("is_active"));
               p.setQtyOnHand(rs.getInt("qty_on_hand"));
               p.setBatchNo(rs.getString("batch_no"));

               java.sql.Date expiry = rs.getDate("expiry_date");
               if (expiry != null)
                    p.setExpiryDate(expiry.toLocalDate());

               p.setSerialNo(rs.getString("serial_no"));
               p.setNote(rs.getString("note"));

               Timestamp created = rs.getTimestamp("created_at");
               if (created != null)
                    p.setCreatedAt(created.toLocalDateTime());

               return p;
          } catch (SQLException e) {
               System.err.println("❌ ERROR mapping row:");
               System.err.println("   Column error: " + e.getMessage());
               throw e;
          }
     }

     private void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
          if (value != null) {
               ps.setInt(index, value);
          } else {
               ps.setNull(index, Types.INTEGER);
          }
     }

     private void setDate(PreparedStatement ps, int index, java.time.LocalDate date) throws SQLException {
          if (date != null) {
               ps.setDate(index, java.sql.Date.valueOf(date));
          } else {
               ps.setNull(index, Types.DATE);
          }
     }
}
