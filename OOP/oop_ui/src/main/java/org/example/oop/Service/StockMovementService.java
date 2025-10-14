package org.example.oop.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.oop.Model.Inventory.Enum.MovementType;
import org.example.oop.Model.Inventory.StockMovement;
import org.example.oop.Repository.StockMovementRepository;

public class StockMovementService {
     private final StockMovementRepository movementRepo = new StockMovementRepository();
     private final InventoryService inventoryService = new InventoryService();

     public StockMovement recordPurchase(int productId, int qty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Purchase cần qty > 0");
          return recordCore(productId, +qty, MovementType.PURCHASE,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     public StockMovement recordSale(int productId, int qty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Sale cần qty > 0");
          return recordCore(productId, -qty, MovementType.SALE,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     public StockMovement recordReturnIn(int productId, int qty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Return In cần qty > 0");
          return recordCore(productId, +qty, MovementType.RETURN_IN,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     public StockMovement recordReturnOut(int productId, int qty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Return Out cần qty > 0");
          return recordCore(productId, -qty, MovementType.RETURN_OUT,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     public StockMovement recordConsume(int productId, int qty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Consume cần qty > 0");
          return recordCore(productId, -qty, MovementType.CONSUME,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     public StockMovement recordAdjustment(int productId, int deltaQty, String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (deltaQty == 0)
               throw new IllegalArgumentException("Adjustment cần delta ≠ 0");
          // Cho adjust âm/dương nhưng không để âm kho
          return recordCore(productId, deltaQty, MovementType.ADJUSTMENT,
                    refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
     }

     private StockMovement recordCore(int productId,
               int signedQty,
               MovementType type,
               String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note,
               boolean allowNegativeAfterApply) throws IOException {

          // 1) Validate cơ bản
          if (signedQty == 0)
               throw new IllegalArgumentException("qty không được bằng 0");
          if (!inventoryService.existsActiveProduct(productId)) {
               throw new IllegalArgumentException("Sản phẩm không tồn tại/không hoạt động: " + productId);
          }

          // 2) Tính & áp tồn (có thể ném lỗi nếu âm và không cho phép)
          int oldQty = inventoryService.getOnHand(productId);
          int newQty = oldQty;
          try {
               newQty = inventoryService.applyDelta(productId, signedQty, allowNegativeAfterApply);
          } catch (RuntimeException e) {
               // âm kho hoặc lỗi ghi inventory -> dừng sớm
               throw e;
          }

          // 3) Build movement (movedAt = now, id = nextId())
          StockMovement m = new StockMovement();
          m.setId(movementRepo.nextId());
          m.setProductId(productId);
          m.setQty(signedQty);
          m.setMoveType(type);
          m.setRefTable(refTable);
          m.setRefId(refId);
          m.setBatchNo(batchNo);
          m.setExpiryDate(expiry);
          m.setSerialNo(serialNo);
          m.setMovedAt(LocalDateTime.now());
          m.setMovedBy(movedBy);
          m.setNote(note);

          // 4) Append file (có rollback nếu hỏng)
          try {
               movementRepo.save(m);
               return m;
          } catch (RuntimeException ex) {
               // rollback tồn kho nếu append thất bại
               try {
                    inventoryService.applyDelta(productId, -signedQty, true);
               } catch (Exception ignore) {
               }
               throw ex;
          }
     }

     public List<StockMovement> recordTransfer(int fromProductId, int toProductId, int qty,
               String refTable, Integer refId,
               String batchNo, LocalDate expiry, String serialNo,
               int movedBy, String note) throws IOException {
          if (qty <= 0)
               throw new IllegalArgumentException("Transfer cần qty > 0");
          // OUT nguồn
          StockMovement outMv = null;
          try {
               outMv = recordCore(fromProductId, -qty, MovementType.TRANSFER,
                         refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
          } catch (RuntimeException e) {
               throw e; // không xuất được thì thôi
          }

          // IN đích
          try {
               StockMovement inMv = recordCore(toProductId, +qty, MovementType.TRANSFER,
                         refTable, refId, batchNo, expiry, serialNo, movedBy, note, false);
               return List.of(outMv, inMv);
          } catch (RuntimeException e) {
               // rollback OUT nếu IN hỏng
               try {
                    recordCore(fromProductId, +qty, MovementType.ADJUSTMENT,
                              "SystemRollback", outMv.getId(), batchNo, expiry, serialNo, movedBy,
                              "rollback transfer out", true);
               } catch (Exception ignore) {
               }
               throw e;
          }
     }

     public StockMovement recordMovementByType(int productId, int qty, String moveTypeStr,
               String refTable, Integer refId, String batchNo, LocalDate expiry,
               String serialNo, int movedBy, String note) throws IOException {

          MovementType moveType = MovementType.valueOf(moveTypeStr.toUpperCase());

          switch (moveType) {
               case PURCHASE:
                    return recordPurchase(productId, Math.abs(qty), refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               case SALE:
                    return recordSale(productId, Math.abs(qty), refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               case RETURN_IN:
                    return recordReturnIn(productId, Math.abs(qty), refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               case RETURN_OUT:
                    return recordReturnOut(productId, Math.abs(qty), refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               case CONSUME:
                    return recordConsume(productId, Math.abs(qty), refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               case ADJUSTMENT:
                    return recordAdjustment(productId, qty, refTable, refId,
                              batchNo, expiry, serialNo, movedBy, note);
               default:
                    throw new IllegalArgumentException("Unsupported movement type: " + moveType);
          }
     }

     /**
      * ✅ Cập nhật một movement đã tồn tại
      * 
      * @param movementId  ID của movement cần cập nhật
      * @param productId   ID sản phẩm mới
      * @param qty         Số lượng mới
      * @param moveTypeStr Loại giao dịch mới
      * @param refTable    Bảng tham chiếu mới
      * @param refId       ID tham chiếu mới
      * @param batchNo     Số lô mới
      * @param expiryDate  Ngày hết hạn mới
      * @param serialNo    Số serial mới
      * @param movedBy     Người thực hiện mới
      * @param note        Ghi chú mới
      * @return true nếu cập nhật thành công
      */
     public boolean updateMovement(int movementId, int productId, int qty, String moveTypeStr,
               String refTable, Integer refId, String batchNo, LocalDate expiryDate,
               String serialNo, int movedBy, String note) throws IOException {

          try {
               // ✅ Tìm movement cần cập nhật
               StockMovement existingMovement = movementRepo.findById(movementId);
               if (existingMovement == null) {
                    return false;
               }

               // ✅ Cập nhật thông tin movement
               existingMovement.setProductId(productId);
               existingMovement.setQty(qty);
               existingMovement.setMoveType(MovementType.valueOf(moveTypeStr.toUpperCase()));
               existingMovement.setRefTable(refTable);
               existingMovement.setRefId(refId);
               existingMovement.setBatchNo(batchNo);
               existingMovement.setExpiryDate(expiryDate);
               existingMovement.setSerialNo(serialNo);
               existingMovement.setMovedBy(movedBy);
               existingMovement.setNote(note);
               // Giữ nguyên movedAt (không thay đổi timestamp gốc)

               // ✅ Lưu vào repository
               return movementRepo.update(existingMovement);

          } catch (Exception e) {
               throw new IOException("Error updating movement: " + e.getMessage(), e);
          }
     }
}
