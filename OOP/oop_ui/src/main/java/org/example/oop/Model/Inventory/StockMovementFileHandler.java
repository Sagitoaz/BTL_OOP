package org.example.oop.Model.Inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Class xử lý đọc/ghi file stock_movement.txt
 */
public class StockMovementFileHandler {

     /**
      * Đọc tất cả stock movements từ file
      * 
      * @param filePath đường dẫn tới file stock_movement.txt
      * @return danh sách StockMovement
      * @throws IOException nếu có lỗi đọc file
      */
     public static List<StockMovement> readFromFile(String filePath) throws IOException {
          List<StockMovement> movements = new ArrayList<>();
          File file = new File(filePath);

          // Nếu file không tồn tại, trả về list rỗng
          if (!file.exists()) {
               return movements;
          }

          try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

               String line;
               while ((line = reader.readLine()) != null) {
                    StockMovement movement = StockMovement.fromFileString(line);
                    if (movement != null) {
                         movements.add(movement);
                    }
               }
          }

          return movements;
     }

     /**
      * Ghi tất cả stock movements vào file
      * 
      * @param filePath  đường dẫn tới file stock_movement.txt
      * @param movements danh sách StockMovement cần ghi
      * @throws IOException nếu có lỗi ghi file
      */
     public static void writeToFile(String filePath, List<StockMovement> movements) throws IOException {
          File file = new File(filePath);

          // Tạo thư mục cha nếu chưa tồn tại
          File parentDir = file.getParentFile();
          if (parentDir != null && !parentDir.exists()) {
               parentDir.mkdirs();
          }

          try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

               for (StockMovement movement : movements) {
                    writer.write(movement.toFileString());
                    writer.newLine();
               }
          }
     }

     /**
      * Thêm một stock movement vào cuối file
      * 
      * @param filePath đường dẫn tới file stock_movement.txt
      * @param movement StockMovement cần thêm
      * @throws IOException nếu có lỗi ghi file
      */
     public static void appendToFile(String filePath, StockMovement movement) throws IOException {
          File file = new File(filePath);

          // Tạo thư mục cha nếu chưa tồn tại
          File parentDir = file.getParentFile();
          if (parentDir != null && !parentDir.exists()) {
               parentDir.mkdirs();
          }

          try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

               writer.write(movement.toFileString());
               writer.newLine();
          }
     }

     /**
      * Lấy ID lớn nhất từ file để tạo ID mới
      * 
      * @param filePath đường dẫn tới file stock_movement.txt
      * @return ID lớn nhất, hoặc 0 nếu file rỗng
      * @throws IOException nếu có lỗi đọc file
      */
     public static int getMaxId(String filePath) throws IOException {
          List<StockMovement> movements = readFromFile(filePath);
          return movements.stream()
                    .mapToInt(StockMovement::getId)
                    .max()
                    .orElse(0);
     }

     /**
      * Lọc movements theo productId
      * 
      * @param filePath  đường dẫn tới file stock_movement.txt
      * @param productId ID sản phẩm cần lọc
      * @return danh sách StockMovement của sản phẩm
      * @throws IOException nếu có lỗi đọc file
      */
     public static List<StockMovement> getByProductId(String filePath, int productId) throws IOException {
          List<StockMovement> movements = readFromFile(filePath);
          List<StockMovement> result = new ArrayList<>();

          for (StockMovement movement : movements) {
               if (movement.getProductId() == productId) {
                    result.add(movement);
               }
          }

          return result;
     }

     /**
      * Tính tổng số lượng hiện tại của một sản phẩm từ lịch sử movements
      * 
      * @param filePath  đường dẫn tới file stock_movement.txt
      * @param productId ID sản phẩm
      * @return tổng số lượng hiện tại (sum of all qty)
      * @throws IOException nếu có lỗi đọc file
      */
     public static int getCurrentQuantity(String filePath, int productId) throws IOException {
          List<StockMovement> movements = getByProductId(filePath, productId);
          return movements.stream()
                    .mapToInt(StockMovement::getQty)
                    .sum();
     }
}
