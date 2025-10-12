package org.example.oop.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import org.example.oop.Model.Inventory.StockMovement;
import org.example.oop.Utils.AppConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StockMovementRepository {

     public ObservableList<StockMovement> loadAll() {
          ObservableList<StockMovement> list = FXCollections.observableArrayList();

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

     public ObservableList<StockMovement> findByProduct(int productId) {
          ObservableList<StockMovement> out = FXCollections.observableArrayList();
          for (StockMovement m : loadAll())
               if (m.getProductId() == productId)
                    out.add(m);
          return out;
     }

     public ObservableList<StockMovement> findByDateRange(LocalDate from, LocalDate to) {
          ObservableList<StockMovement> out = FXCollections.observableArrayList();
          for (StockMovement m : loadAll()) {
               LocalDate d = m.getMovedAt().toLocalDate();
               if ((d.isEqual(from) || d.isAfter(from)) && (d.isEqual(to) || d.isBefore(to))) {
                    out.add(m);
               }
          }
          return out;
     }

     public ObservableList<StockMovement> findByBatchOrSerial(String batchNo, String serialNo) {
          ObservableList<StockMovement> out = FXCollections.observableArrayList();
          for (StockMovement m : loadAll()) {
               boolean ok = (batchNo != null && batchNo.equals(m.getBatchNo()))
                         || (serialNo != null && serialNo.equals(m.getSerialNo()));
               if (ok)
                    out.add(m);
          }
          return out;
     }

     public ObservableList<StockMovement> findByRef(String refTable, Integer refId) {
          ObservableList<StockMovement> out = FXCollections.observableArrayList();
          for (StockMovement m : loadAll()) {
               boolean ok = Objects.equals(refTable, m.getRefTable())
                         && Objects.equals(refId, m.getRefId());
               if (ok)
                    out.add(m);
          }
          return out;
     }
}
