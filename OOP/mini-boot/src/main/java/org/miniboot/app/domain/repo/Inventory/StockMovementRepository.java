package org.miniboot.app.domain.repo.Inventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.miniboot.app.AppConfig;
import org.miniboot.app.domain.models.Inventory.StockMovement;

public class StockMovementRepository {
     private final InventoryRepository inventories = new InventoryRepository();

     public List<StockMovement> loadAll() {
          List<StockMovement> list = new ArrayList<>();

          // ✅ Use resource stream instead of File
          try {
               var resource = getClass().getResourceAsStream(AppConfig.STOCK_TEST_DATA_TXT);
               if (resource == null) {
                    System.err.println("❌ Resource not found: " + AppConfig.STOCK_TEST_DATA_TXT);
                    return list;
               }

               try (BufferedReader br = new BufferedReader(
                         new InputStreamReader(resource, StandardCharsets.UTF_8))) {

                    String line;
                    int lineNumber = 0;
                    while ((line = br.readLine()) != null) {
                         lineNumber++;
                         if (line.isBlank()) {
                              continue;
                         }

                         try {
                              StockMovement movement = StockMovement.fromDataString(line);
                              list.add(movement);
                              System.out.println("✅ Loaded movement " + lineNumber + ": ID=" + movement.getId());
                         } catch (Exception e) {
                              System.err.println("❌ Error parsing line " + lineNumber + ": " + line);
                              System.err.println("Error: " + e.getMessage());
                         }
                    }

                    System.out.println("✅ Total loaded movements: " + list.size());
               }

          } catch (IOException e) {
               System.err.println("❌ IOException loading stock movements: " + e.getMessage());
          } catch (Exception e) {
               System.err.println("❌ Unexpected error loading stock movements: " + e.getMessage());
          }

          return list;
     }

     public synchronized void save(StockMovement m) {
          File file = AppConfig.getStockDataFile();
          file.getParentFile().mkdirs();

          // Nếu file chưa tồn tại, ghi header (nếu bạn muốn)
          if (!file.exists()) {
               try (PrintWriter pw = new PrintWriter(
                         new OutputStreamWriter(new FileOutputStream(file, false),
                                   java.nio.charset.StandardCharsets.UTF_8))) {
                    // pw.println("id|product_id|qty|move_type|ref_table|ref_id|batch_no|expiry_date|serial_no|moved_at|moved_by|note");
               } catch (IOException e) {
                    e.printStackTrace();
               }
          }

          System.out.println("💾 Saving to: " + file.getAbsolutePath());

          try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true),
                              java.nio.charset.StandardCharsets.UTF_8))) {
               writer.println(m.toDataString());
               writer.flush();
          } catch (IOException e) {
               e.printStackTrace();
          }

          System.out.println("✅ Saved. exists=" + file.exists() + ", size=" + file.length() + " bytes");
     }

     public int nextId() {
          return loadAll().stream().mapToInt(StockMovement::getId).max().orElse(0) + 1;
     }

     public List<StockMovement> findByProduct(int productId) {
          List<StockMovement> out = new ArrayList<>();
          for (StockMovement m : loadAll())
               if (m.getProductId() == productId)
                    out.add(m);
          return out;
     }

     public List<StockMovement> findByDateRange(LocalDate from, LocalDate to) {
          List<StockMovement> out = new ArrayList<>();
          for (StockMovement m : loadAll()) {
               LocalDate d = m.getMovedAt().toLocalDate();
               if ((d.isEqual(from) || d.isAfter(from)) && (d.isEqual(to) || d.isBefore(to))) {
                    out.add(m);
               }
          }
          return out;
     }

     public List<StockMovement> findByBatchOrSerial(String batchNo, String serialNo) {
          List<StockMovement> out = new ArrayList<>();
          for (StockMovement m : loadAll()) {
               boolean ok = (batchNo != null && batchNo.equals(m.getBatchNo()))
                         || (serialNo != null && serialNo.equals(m.getSerialNo()));
               if (ok)
                    out.add(m);
          }
          return out;
     }

     public List<StockMovement> findByRef(String refTable, Integer refId) {
          List<StockMovement> out = new ArrayList<>();
          for (StockMovement m : loadAll()) {
               boolean ok = Objects.equals(refTable, m.getRefTable())
                         && Objects.equals(refId, m.getRefId());
               if (ok)
                    out.add(m);
          }
          return out;
     }

     /**
      * ✅ Tìm movement theo ID
      */
     public StockMovement findById(int id) {
          return loadAll().stream()
                    .filter(m -> m.getId() == id)
                    .findFirst()
                    .orElse(null);
     }

     /**
      * ✅ Cập nhật movement (ghi đè toàn bộ file)
      * Note: Đây là implementation đơn giản, trong thực tế nên tối ưu hơn
      */
     public synchronized boolean update(StockMovement updatedMovement) {
          try {
               // 1. Load tất cả movements
               List<StockMovement> allMovements = loadAll();

               // 2. Tìm và cập nhật movement
               boolean found = false;
               for (int i = 0; i < allMovements.size(); i++) {
                    if (allMovements.get(i).getId() == updatedMovement.getId()) {
                         allMovements.set(i, updatedMovement);
                         found = true;
                         break;
                    }
               }

               if (!found) {
                    return false;
               }

               // 3. Ghi đè toàn bộ file
               File file = AppConfig.getStockDataFile();
               file.getParentFile().mkdirs();

               try (PrintWriter writer = new PrintWriter(
                         new OutputStreamWriter(new FileOutputStream(file, false),
                                   StandardCharsets.UTF_8))) {

                    for (StockMovement m : allMovements) {
                         writer.println(m.toDataString());
                    }
                    writer.flush();
               }

               System.out.println("✅ Updated movement ID: " + updatedMovement.getId());
               return true;

          } catch (Exception e) {
               System.err.println("❌ Error updating movement: " + e.getMessage());
               e.printStackTrace();
               return false;
          }
     }
}
