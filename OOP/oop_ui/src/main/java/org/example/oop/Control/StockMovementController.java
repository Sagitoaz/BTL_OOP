package org.example.oop.Control;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Model.Inventory.StockMovement;
import org.example.oop.Utils.AppConfig;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class StockMovementController {

     // Form fields
     @FXML
     private ComboBox<String> productBox; // List products
     @FXML
     private ComboBox<String> movementTypeBox; // IN, OUT, TRANSFER
     @FXML
     private TextField quantityField;
     @FXML
     private TextField reasonField;
     @FXML
     private TextField locationFromField; // For TRANSFER
     @FXML
     private TextField locationToField; // For TRANSFER

     // Buttons
     @FXML
     private Button saveButton;
     @FXML
     private Button clearButton;

     // Status
     @FXML
     private Label statusLabel;

     // Table
     @FXML
     private TableView<StockMovement> movementTable;
     @FXML
     private TableColumn<StockMovement, Integer> idColumn;
     @FXML
     private TableColumn<StockMovement, Integer> productColumn;
     @FXML
     private TableColumn<StockMovement, String> typeColumn;
     @FXML
     private TableColumn<StockMovement, Integer> qtyBeforeColumn;
     @FXML
     private TableColumn<StockMovement, Integer> qtyChangeColumn;
     @FXML
     private TableColumn<StockMovement, Integer> qtyAfterColumn;
     @FXML
     private TableColumn<StockMovement, String> locationFromColumn;
     @FXML
     private TableColumn<StockMovement, String> locationToColumn;
     @FXML
     private TableColumn<StockMovement, String> reasonColumn;
     @FXML
     private TableColumn<StockMovement, String> movedAtColumn;
     @FXML
     private TableColumn<StockMovement, String> movedByColumn;

     // Data
     private ObservableList<StockMovement> movementList = FXCollections.observableArrayList();
     private ObservableList<InventoryRow> productList = FXCollections.observableArrayList();
     private InventoriesController inventoriesController = new InventoriesController();

     @FXML
     public void initialize() throws IOException {
          initMovementTypeBox();
          loadProducts();
          setupTable();
          loadMovements();
     }

     @FXML
     void onSaveButton(ActionEvent event) {
          try {
               // 1. Validate input
               if (!validateForm()) {
                    showStatus("Vui lòng điền đầy đủ thông tin!", true);
                    return;
               }

               // 2. Lấy thông tin
               String productName = productBox.getValue();
               String type = movementTypeBox.getValue();
               int quantity = Integer.parseInt(quantityField.getText());

               // 3. Tìm product hiện tại
               InventoryRow currentProduct = findProduct(productName);
               if (currentProduct == null) {
                    showStatus("Không tìm thấy sản phẩm!", true);
                    return;
               }

               int quantityBefore = currentProduct.getQuantity();

               // 4. Tính số lượng mới
               int quantityAfter = quantityBefore;
               switch (type) {
                    case "IN":
                         quantityAfter = quantityBefore + quantity; // Nhập: TĂNG
                         break;
                    case "OUT":
                         quantityAfter = quantityBefore - quantity; // Xuất: GIẢM
                         if (quantityAfter < 0) {
                              showError("Không đủ hàng! Hiện có: " + quantityBefore + ", Yêu cầu: " + quantity);
                              return;
                         }
                         break;
                    case "TRANSFER":
                         quantityAfter = quantityBefore; // Chuyển: KHÔNG ĐỔI tổng
                         break;
                    case "ADJUSTMENT":
                         quantityAfter = quantityBefore + quantity; // Điều chỉnh (có thể âm/dương)
                         break;
                    default:
                         showStatus("Loại giao dịch không hợp lệ!", true);
                         return;
               }

               // 5. Tạo movement record
               StockMovement movement = new StockMovement();
               movement.setId(getNextMovementId());
               movement.setProductId(currentProduct.getId());
               movement.setMovementType(type);
               movement.setQuantityBefore(quantityBefore);
               movement.setQuantityChange(quantity);
               movement.setQuantityAfter(quantityAfter);
               movement.setReason(reasonField.getText());
               movement.setMovedAt(LocalDateTime.now());
               movement.setMovedBy("ADMIN"); // TODO: Get from current user session

               // Set location for TRANSFER
               if ("TRANSFER".equals(type)) {
                    movement.setLocationFrom(locationFromField.getText());
                    movement.setLocationTo(locationToField.getText());
               }

               // 6. Add to list
               movementList.add(movement);

               // 7. Cập nhật inventory quantity
               currentProduct.setQuantity(quantityAfter);

               // 8. Refresh UI
               movementTable.refresh();
               clearForm();
               showStatus("Lưu giao dịch thành công!", false);

          } catch (NumberFormatException e) {
               showError("Số lượng không hợp lệ!");
          } catch (Exception e) {
               showError("Lỗi: " + e.getMessage());
          }
     }

     @FXML
     void onClearButton(ActionEvent event) {
          clearForm();
     }

     private void setupTable() {
          // Setup columns
          idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
          productColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getProductId()));
          typeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMovementType()));
          qtyBeforeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantityBefore()));
          qtyChangeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantityChange()));
          qtyAfterColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantityAfter()));
          locationFromColumn
                    .setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLocationFrom()));
          locationToColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLocationTo()));
          reasonColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getReason()));

          movedAtColumn.setCellValueFactory(data -> {
               LocalDateTime dt = data.getValue().getMovedAt();
               String formatted = dt != null ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
               return new ReadOnlyObjectWrapper<>(formatted);
          });

          movedByColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMovedBy()));

          movementTable.setItems(movementList);
     }

     private void initMovementTypeBox() {
          movementTypeBox.setItems(FXCollections.observableArrayList(
                    "IN", "OUT", "TRANSFER", "ADJUSTMENT"));
          movementTypeBox.getSelectionModel().selectFirst();
     }

     private void loadProducts() throws IOException {
          // Load products from inventory
          productList = inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT);

          // Populate productBox with product names
          ObservableList<String> productNames = FXCollections.observableArrayList();
          for (InventoryRow row : productList) {
               productNames.add(row.getName());
          }
          productBox.setItems(productNames);
     }

     private void loadMovements() {
          // TODO: Load movements from file if needed
          // For now, start with empty list
          movementList.clear();
     }

     private InventoryRow findProduct(String productName) {
          for (InventoryRow row : productList) {
               if (row.getName().equals(productName)) {
                    return row;
               }
          }
          return null;
     }

     private boolean validateForm() {
          if (productBox.getValue() == null || productBox.getValue().isEmpty()) {
               return false;
          }
          if (movementTypeBox.getValue() == null || movementTypeBox.getValue().isEmpty()) {
               return false;
          }
          if (quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
               return false;
          }
          try {
               int qty = Integer.parseInt(quantityField.getText().trim());
               if (qty <= 0) {
                    return false;
               }
          } catch (NumberFormatException e) {
               return false;
          }
          return true;
     }

     private void clearForm() {
          productBox.getSelectionModel().clearSelection();
          movementTypeBox.getSelectionModel().selectFirst();
          quantityField.clear();
          reasonField.clear();
          locationFromField.clear();
          locationToField.clear();
     }

     private void showStatus(String message, boolean isError) {
          if (statusLabel != null) {
               statusLabel.setText(message);
               statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
          }
     }

     private void showError(String message) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Lỗi");
          alert.setHeaderText(null);
          alert.setContentText(message);
          alert.showAndWait();

          showStatus(message, true);
     }

     private int getNextMovementId() {
          return movementList.stream()
                    .mapToInt(StockMovement::getId)
                    .max()
                    .orElse(0) + 1;
     }
}