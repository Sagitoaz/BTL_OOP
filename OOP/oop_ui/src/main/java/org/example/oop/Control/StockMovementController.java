package org.example.oop.Control;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.example.oop.Model.Inventory.StockMovement;
import org.example.oop.Repository.StockMovementRepository;
import org.example.oop.Service.InventoryService;
import org.example.oop.Service.StockMovementService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class StockMovementController {

     // ===== Header & Filter Section =====
     @FXML
     private Label statsLabel;
     @FXML
     private ComboBox<String> filterProductBox;
     @FXML
     private ComboBox<String> filterMoveTypeBox;
     @FXML
     private DatePicker filterDateFrom;
     @FXML
     private DatePicker filterDateTo;

     // ===== Left Panel - Record New Movement =====
     @FXML
     private ComboBox<String> productBox;
     @FXML
     private Label currentQtyLabel;

     @FXML
     private ComboBox<String> moveTypeBox;
     @FXML
     private TextField qtyField;

     @FXML
     private ComboBox<String> refTableBox;
     @FXML
     private TextField refIdField;
     @FXML
     TextField noteField;
     @FXML
     private TextField batchNoField;
     @FXML
     private DatePicker expiryDatePicker;

     @FXML
     private TextField serialNoField;
     @FXML
     private TextField movedbyField1;
     @FXML
     private DatePicker movedatDatePicker1;
     @FXML
     private Button saveButton;
     @FXML
     private Button clearButton;

     @FXML
     private Label statusLabel;

     // ===== Right Panel - Movement History Table =====
     @FXML
     private TableView<StockMovement> movementTable;

     @FXML
     private TableColumn<StockMovement, Integer> idColumn;
     @FXML
     private TableColumn<StockMovement, Integer> productIdColumn;
     @FXML
     private TableColumn<StockMovement, Integer> qtyColumn;
     @FXML
     private TableColumn<StockMovement, String> moveTypeColumn;
     @FXML
     private TableColumn<StockMovement, String> refTableColumn;
     @FXML
     private TableColumn<StockMovement, Integer> refIdColumn;
     @FXML
     private TableColumn<StockMovement, String> batchNoColumn;
     @FXML
     private TableColumn<StockMovement, String> expiryDateColumn;
     @FXML
     private TableColumn<StockMovement, String> serialNoColumn;
     @FXML
     private TableColumn<StockMovement, String> movedAtColumn;
     @FXML
     private TableColumn<StockMovement, String> movedByColumn;
     @FXML
     private TableColumn<StockMovement, String> noteColumn;
     @FXML
     private TableColumn<StockMovement, Void> actionsColumn;

     // ===== Bottom - Footer =====
     @FXML
     private Label footerStatusLabel;
     @FXML
     private Label totalMovementsLabel;
     @FXML
     private Label selectedProductLabel;

     private final StockMovementService movementService = new StockMovementService();
     private final InventoryService inventoryService = new InventoryService();
     private final StockMovementRepository movementRepo = new StockMovementRepository();
     private StockMovement selectMovement = null;

     private final ObservableList<StockMovement> masterData = FXCollections.observableArrayList();
     private final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

     // ====== Event Handlers (declared in FXML) ======
     @FXML
     private void onFilterButton() {
          // TODO: implement filter logic
     }

     @FXML
     private void onResetFilterButton() {
          // TODO: reset filters
     }

     @FXML
     private void onSaveButton() {
          try {
               if (productBox.getValue() == null || productBox.getValue().trim().isEmpty()) {
                    statsLabel.setText("❌ Vui lòng chọn sản phẩm");
                    return;
               }
               if (moveTypeBox.getValue() == null) {
                    statusLabel.setText("❌ Vui lòng chọn loại giao dịch");
                    return;
               }

               if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
                    statusLabel.setText("❌ Vui lòng nhập số lượng");
                    return;
               }
               int productId = Integer.parseInt(productBox.getValue().split(" - ")[0]);
               String moveType = moveTypeBox.getValue().toUpperCase();
               int qty = Integer.parseInt(qtyField.getText());
               String refTable = refTableBox.getValue();
               Integer refId = refIdField.getText().trim().isEmpty() ? null : Integer.parseInt(refIdField.getText());
               String batchNo = batchNoField.getText().trim().isEmpty() ? null : batchNoField.getText();
               LocalDate expiryDate = expiryDatePicker.getValue();
               String serialNo = serialNoField.getText().trim().isEmpty() ? null : serialNoField.getText();
               int movedBy = Integer.parseInt(movedbyField1.getText());
               LocalDateTime movedAt = movedatDatePicker1.getValue().atStartOfDay();
               String note = noteField.getText();
               StockMovement movement = movementService.recordMovementByType(
                         productId, qty, moveType, refTable, refId,
                         batchNo, expiryDate, serialNo, movedBy, note);

               statusLabel.setText("✅ Đã lưu movement ID: " + movement.getId());
               // clearForm();
               loadData(); // Refresh table

          } catch (NumberFormatException e) {
               statusLabel.setText("❌ Lỗi định dạng số: " + e.getMessage());
          } catch (Exception e) {
               statusLabel.setText("❌ Lỗi lưu dữ liệu: " + e.getMessage());
               e.printStackTrace();
          }
     }

     @FXML
     private void onClearButton() {
          // TODO: clear input form
     }

     @FXML
     private void onRefreshButton() {
          try {
               loadData();
          } catch (IOException e) {
               e.printStackTrace();
               statusLabel.setText("Lỗi refresh dữ liệu: " + e.getMessage());
          }
     }

     @FXML
     private void onExportButton() {
          // TODO: export table data
     }

     // ====== IMPORTANT: Ensure initialize is discovered by FXMLLoader ======
     @FXML
     public void initialize() {
          System.out.println("🚀 StockMovementController initializing...");
          try {
               initTable();
               loadData();
          } catch (Exception e) {
               System.err.println("❌ Initialization error: " + e.getMessage());
               e.printStackTrace();
               if (statusLabel != null)
                    statusLabel.setText("Initialization failed: " + e.getMessage());
          }
     }

     private void loadData() throws IOException {
          System.out.println("🔄 Loading stock movement data...");

          // Move type combo for left panel
          var moveTypes = FXCollections.observableArrayList(
                    "purchase", "sale", "return_in", "return_out",
                    "adjustment", "consume", "transfer");
          moveTypeBox.setItems(moveTypes);

          // Filter move type
          filterMoveTypeBox.setItems(moveTypes);

          // Load table data
          try {
               masterData.clear();
               ObservableList<StockMovement> loadedData = movementRepo.loadAll();
               masterData.addAll(loadedData);
               System.out.println("✅ Loaded movements: " + masterData.size());

               // Debug: print first few records
               for (int i = 0; i < Math.min(3, masterData.size()); i++) {
                    StockMovement m = masterData.get(i);
                    System.out.println("  Movement " + (i + 1) + ": ID=" + m.getId() + ", ProductID=" + m.getProductId()
                              + ", Qty=" + m.getQty());
               }

          } catch (Exception e) {
               System.err.println("❌ Error loading stock movements: " + e.getMessage());
               e.printStackTrace();
               if (statusLabel != null) {
                    statusLabel.setText("Error loading data: " + e.getMessage());
               }
          }

          // Optional: update footer
          if (totalMovementsLabel != null) {
               totalMovementsLabel.setText("Total: " + masterData.size());
          }

          // Bind items (safe to set here; initTable will set factories)
          movementTable.setItems(masterData);

          System.out.println("📊 Table items set: " + movementTable.getItems().size());
     }

     private void initTable() {
          System.out.println(
                    "✅ StockMovementController.initTable(): Setting up table with " + masterData.size() + " items");

          idColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
          productIdColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProductId()));
          qtyColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getQty()));

          // MovementType -> String (enum name), safe-null
          moveTypeColumn.setCellValueFactory(d -> {
               Object moveType = d.getValue().getMoveType();
               String moveTypeStr;
               if (moveType == null) {
                    moveTypeStr = "";
               } else if (moveType instanceof Enum<?>) {
                    moveTypeStr = ((Enum<?>) moveType).name();
               } else {
                    moveTypeStr = moveType.toString();
               }
               return new ReadOnlyObjectWrapper<>(moveTypeStr);
          });

          refTableColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(nz(d.getValue().getRefTable())));
          refIdColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getRefId()));
          batchNoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(nz(d.getValue().getBatchNo())));

          expiryDateColumn.setCellValueFactory(d -> {
               var date = d.getValue().getExpiryDate();
               String formattedDate = (date != null) ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
               return new ReadOnlyObjectWrapper<>(formattedDate);
          });

          serialNoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(nz(d.getValue().getSerialNo())));

          movedAtColumn.setCellValueFactory(d -> {
               var t = d.getValue().getMovedAt();
               return new ReadOnlyObjectWrapper<>(t == null ? "" : t.format(DT));
          });

          movedByColumn
                    .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue().getMovedBy())));

          noteColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(nz(d.getValue().getNote())));

          // Product name column: để tạm rỗng (tránh phụ thuộc hàm service cụ thể)
          // Nếu bạn có InventoryService.getNameById(int), hãy mở comment dưới:
          // productNameColumn.setCellValueFactory(d ->
          // new ReadOnlyObjectWrapper<>(safeGetProductName(d.getValue().getProductId()))
          // );

          movementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
               selectMovement = newSel;
               if (selectedProductLabel != null) {
                    selectedProductLabel.setText(newSel == null ? "" : "Selected ID: " + newSel.getId());
               }
          });

          System.out.println("✅ StockMovementController.initTable(): Table setup complete!");
     }

     // ====== Helpers ======
     private static String nz(String s) {
          return (s == null) ? "" : s;
     }

     @SuppressWarnings("unused")
     private String safeGetProductName(int productId) {
          try {
               // Ví dụ nếu có hàm:
               // return inventoryService.getNameById(productId);
               return "";
          } catch (Exception e) {
               return "";
          }
     }
}
