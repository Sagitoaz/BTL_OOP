package org.example.oop.Model.Inventory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StockMovementService {

     private final String filePath;

     /**
      * Constructor với đường dẫn file mặc định
      */
     public StockMovementService() {
          this.filePath = "data/stock_movement.txt";
     }

     /**
      * Constructor với đường dẫn file tùy chỉnh
      */
     public StockMovementService(String filePath) {
          this.filePath = filePath;
     }

     /**
      * Lấy tất cả stock movements
      * 
      * @return List<StockMovement>
      */
     public List<StockMovement> getAllMovements() {
          try {
               return StockMovementFileHandler.readFromFile(filePath);
          } catch (IOException e) {
               System.err.println("Lỗi đọc file: " + e.getMessage());
               return new ArrayList<>();
          }
     }

     /**
      * Lấy movement theo ID
      * 
      * @param id ID của movement
      * @return StockMovement hoặc null nếu không tìm thấy
      */
     public StockMovement getMovementById(int id) {
          List<StockMovement> movements = getAllMovements();
          for (StockMovement m : movements) {
               if (m.getId() == id) {
                    return m;
               }
          }
          return null;
     }

     /**
      * Lấy tất cả movements của một sản phẩm
      * 
      * @param productId ID sản phẩm
      * @return List<StockMovement>
      */
     public List<StockMovement> getMovementsByProductId(int productId) {
          try {
               return StockMovementFileHandler.getByProductId(filePath, productId);
          } catch (IOException e) {
               System.err.println("Lỗi đọc file: " + e.getMessage());
               return new ArrayList<>();
          }
     }

     /**
      * Lọc movements theo loại (moveType)
      * 
      * @param moveType loại giao dịch (purchase, sale, return_in, etc.)
      * @return List<StockMovement>
      */
     public List<StockMovement> getMovementsByType(String moveType) {
          return getAllMovements().stream()
                    .filter(m -> moveType.equalsIgnoreCase(m.getMoveType()))
                    .collect(Collectors.toList());
     }

     /**
      * Lọc movements trong khoảng thời gian
      * 
      * @param startDate ngày bắt đầu
      * @param endDate   ngày kết thúc
      * @return List<StockMovement>
      */
     public List<StockMovement> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
          return getAllMovements().stream()
                    .filter(m -> m.getMovedAt() != null &&
                              !m.getMovedAt().isBefore(startDate) &&
                              !m.getMovedAt().isAfter(endDate))
                    .collect(Collectors.toList());
     }

     /**
      * Lấy các movements nhập kho (qty > 0)
      * 
      * @return List<StockMovement>
      */
     public List<StockMovement> getIncomingMovements() {
          return getAllMovements().stream()
                    .filter(StockMovement::isInMovement)
                    .collect(Collectors.toList());
     }

     /**
      * Lấy các movements xuất kho (qty < 0)
      * 
      * @return List<StockMovement>
      */
     public List<StockMovement> getOutgoingMovements() {
          return getAllMovements().stream()
                    .filter(StockMovement::isOutMovement)
                    .collect(Collectors.toList());
     }

     /**
      * Tính tổng số lượng hiện tại của một sản phẩm
      * 
      * @param productId ID sản phẩm
      * @return tổng số lượng
      */
     public int getCurrentQuantity(int productId) {
          try {
               return StockMovementFileHandler.getCurrentQuantity(filePath, productId);
          } catch (IOException e) {
               System.err.println("Lỗi đọc file: " + e.getMessage());
               return 0;
          }
     }

     /**
      * Thêm movement mới
      * 
      * @param movement StockMovement cần thêm
      * @return true nếu thành công
      */
     public boolean addMovement(StockMovement movement) {
          try {
               // Tự động set ID nếu chưa có
               if (movement.getId() <= 0) {
                    int nextId = StockMovementFileHandler.getMaxId(filePath) + 1;
                    movement.setId(nextId);
               }

               // Tự động set thời gian nếu chưa có
               if (movement.getMovedAt() == null) {
                    movement.setMovedAt(LocalDateTime.now());
               }

               // Kiểm tra valid
               if (!movement.isValid()) {
                    System.err.println("Movement không hợp lệ!");
                    return false;
               }

               StockMovementFileHandler.appendToFile(filePath, movement);
               return true;
          } catch (IOException e) {
               System.err.println("Lỗi ghi file: " + e.getMessage());
               return false;
          }
     }

     /**
      * Thêm movement và cập nhật số lượng inventory
      * 
      * @param movement  StockMovement cần thêm
      * @param inventory Inventory cần cập nhật
      * @return true nếu thành công
      */
     public boolean addMovementAndUpdateInventory(StockMovement movement, Inventory inventory) {
          // Kiểm tra productId khớp
          if (movement.getProductId() != inventory.getId()) {
               System.err.println("ProductId không khớp!");
               return false;
          }

          // Kiểm tra số lượng đủ không (nếu là xuất kho)
          if (movement.isOutMovement()) {
               int currentQty = inventory.getQuantity();
               int requestQty = Math.abs(movement.getQty());
               if (currentQty < requestQty) {
                    System.err.println("Không đủ hàng! Hiện có: " + currentQty + ", Yêu cầu: " + requestQty);
                    return false;
               }
          }

          // Thêm movement
          if (!addMovement(movement)) {
               return false;
          }

          // Cập nhật inventory quantity
          int newQty = inventory.getQuantity() + movement.getQty();
          inventory.setQuantity(newQty);

          return true;
     }

     /**
      * Xóa tất cả movements (dùng cho testing)
      * 
      * @return true nếu thành công
      */
     public boolean clearAllMovements() {
          try {
               StockMovementFileHandler.writeToFile(filePath, new ArrayList<>());
               return true;
          } catch (IOException e) {
               System.err.println("Lỗi ghi file: " + e.getMessage());
               return false;
          }
     }

     /**
      * Lưu toàn bộ movements vào file (ghi đè)
      * 
      * @param movements danh sách movements
      * @return true nếu thành công
      */
     public boolean saveAllMovements(List<StockMovement> movements) {
          try {
               StockMovementFileHandler.writeToFile(filePath, movements);
               return true;
          } catch (IOException e) {
               System.err.println("Lỗi ghi file: " + e.getMessage());
               return false;
          }
     }

     /**
      * Lấy thống kê tổng hợp
      * 
      * @return String thống kê
      */
     public String getStatistics() {
          List<StockMovement> movements = getAllMovements();
          int totalMovements = movements.size();
          int inMovements = (int) movements.stream().filter(StockMovement::isInMovement).count();
          int outMovements = (int) movements.stream().filter(StockMovement::isOutMovement).count();

          return String.format("Tổng movements: %d | Nhập: %d | Xuất: %d",
                    totalMovements, inMovements, outMovements);
     }
}
