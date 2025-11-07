package org.example.oop.Control.Inventory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.example.oop.Control.BaseController;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.example.oop.Service.ApiProductService;
import org.example.oop.Service.ApiStockMovementService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class StockMovementController extends BaseController {

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
     private TextField productField;
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
     private TextField noteField;
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
     private Button filterButton;
     @FXML
     private Button resetFilterButton;

     // ===== Edit Mode Controls =====
     @FXML
     private Label modeLabel;
     @FXML
     private Label statusLabel;

     // ===== Right Panel - Movement History Table =====
     @FXML
     private TableView<StockMovement> movementTable;
     @FXML
     private TableColumn<StockMovement, Integer> idColumn;
     @FXML
     private TableColumn<StockMovement, String> productNameColumn;
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

     // ===== Bottom - Footer =====
     @FXML
     private Label footerStatusLabel;
     @FXML
     private Label totalMovementsLabel;
     @FXML
     private Label selectedProductLabel;

     // ===== Services - Gọi API backend =====
     private final ApiStockMovementService stockMovementService = new ApiStockMovementService();
     private final ApiProductService productService = new ApiProductService();

     // ===== Data - ObservableList tự động update UI =====
     private final ObservableList<StockMovement> allMovements = FXCollections.observableArrayList();
     private final ObservableList<StockMovement> filteredMovements = FXCollections.observableArrayList();
     private final ObservableList<Product> allProducts = FXCollections.observableArrayList();

     // ===== Mode State - Phân biệt ADD/EDIT =====
     private boolean isEditMode = false;
     private StockMovement editingMovement = null;
     private Integer selectedProductId = null;

    @FXML
    private void handleBackButton(){
        SceneManager.goBack();
    }
    @FXML
    private void handleForwardButton(){
        SceneManager.goForward();
    }
    @FXML
    private void handleReloadButton(){
        SceneManager.reloadCurrentScene();
    }

     // ====================================================================
     // INITIALIZATION
     // ====================================================================

     /**
      * Initialize - Entry point khi controller được load
      * Thứ tự: Table → ComboBoxes → Load Data → Event Handlers
      */
     @FXML
     public void initialize() {
          System.out.println("🚀 StockMovementController initializing...");
          try {
               initializeTable(); // 1. Setup table columns
               initializeComboBoxes(); // 2. Setup dropdown options
               loadProductsAsync(); // 3. Load danh sách products
               loadDataAsync(); // 4. Load stock movements
               setupEventHandlers(); // 5. Setup click events
               enterAddMode(); // 6. Mặc định là chế độ ADD
          } catch (Exception e) {
               System.err.println("❌ Initialization error: " + e.getMessage());
               e.printStackTrace();
               if (statusLabel != null) {
                    statusLabel.setText("❌ Initialization failed");
               }
          }
     }

     /**
      * Setup các TableColumn - bind với StockMovement properties
      */
     private void initializeTable() {
          System.out.println("📋 Setting up table columns...");

          // ID Column
          idColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId())
                              .asObject());

          // Product ID Column
          productIdColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getProductId())
                              .asObject());

          // Product Name Column - Từ JOIN với Products table
          productNameColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));

          // Quantity Column
          qtyColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQty())
                              .asObject());

          // Move Type Column
          moveTypeColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMoveType()));

          // Reference Table Column
          refTableColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRefTable()));

          // Reference ID Column - Handle null
          refIdColumn.setCellValueFactory(cellData -> {
               Integer refId = cellData.getValue().getRefId();
               int value = (refId != null) ? refId : 0; // Default to 0 if null
               return new javafx.beans.property.SimpleIntegerProperty(value).asObject();
          });

          // Batch Number Column
          batchNoColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBatchNo()));

          // Expiry Date Column - Format LocalDate → String
          expiryDateColumn.setCellValueFactory(cellData -> {
               LocalDate date = cellData.getValue().getExpiryDate();
               String formatted = (date != null) ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
               return new javafx.beans.property.SimpleStringProperty(formatted);
          });

          // Serial Number Column
          serialNoColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSerialNo()));

          // Moved At Column - Format LocalDateTime → String
          movedAtColumn.setCellValueFactory(cellData -> {
               var datetime = cellData.getValue().getMovedAt();
               String formatted = (datetime != null)
                         ? datetime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                         : "";
               return new javafx.beans.property.SimpleStringProperty(formatted);
          });

          // Moved By Column
          movedByColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(cellData.getValue().getMovedBy())));

          // Note Column
          noteColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNote()));

          // Bind data vào table
          movementTable.setItems(filteredMovements);

          System.out.println("✅ Table setup completed");
     }

     /**
      * Setup các ComboBox với options
      */
     private void initializeComboBoxes() {
          System.out.println("🔧 Setting up combo boxes...");

          // Move Type options
          if (moveTypeBox != null) {
               moveTypeBox.getItems().addAll(
                         "PURCHASE", // Nhập hàng
                         "SALE", // Xuất bán
                         "RETURN_IN", // Trả hàng nhập
                         "RETURN_OUT", // Trả hàng xuất
                         "ADJUSTMENT", // Điều chỉnh
                         "CONSUME", // Tiêu hao
                         "TRANSFER" // Chuyển kho
               );
          }

          // Reference Table options
          if (refTableBox != null) {
               refTableBox.getItems().addAll(
                         "invoices", "appointments", "purchase_orders", "adjustments", "manual");
          }

          // Filter Move Type
          if (filterMoveTypeBox != null) {
               filterMoveTypeBox.getItems().add("All");
               filterMoveTypeBox.getItems().addAll(
                         "PURCHASE", "SALE", "RETURN_IN", "RETURN_OUT", "ADJUSTMENT", "CONSUME", "TRANSFER");
               filterMoveTypeBox.getSelectionModel().selectFirst();
          }

          System.out.println("✅ Combo boxes setup completed");
     }

     /**
      * Setup event handlers - Double-click, text change, etc.
      */
     private void setupEventHandlers() {
          System.out.println("🖱️ Setting up event handlers...");

          // Double-click row để edit
          movementTable.setRowFactory(tv -> {
               TableRow<StockMovement> row = new TableRow<>();
               row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                         enterEditMode(row.getItem());
                    }
               });
               return row;
          });

          // Product field - Update current qty khi blur
          if (productField != null) {
               productField.setPromptText("Nhập ID sản phẩm");
               productField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                         updateCurrentQty();
                    }
               });
          }

          System.out.println("✅ Event handlers setup completed");
     }

     // ====================================================================
     // DATA LOADING
     // ====================================================================

     /**
      * Load danh sách products từ API
      * Dùng cho filter dropdown
      */
     private void loadProductsAsync() {
          System.out.println("📦 Loading products...");

          executeAsync(
                    () -> {
                         try {
                              return productService.getAllProducts();
                         } catch (Exception e) {
                              e.printStackTrace(); // 🐛 Print full stack trace
                              throw new RuntimeException("Lỗi khi tải products", e);
                         }
                    },
                    (List<Product> products) -> {
                         allProducts.setAll(products);
                         setupProductFilter();
                         System.out.println("✅ Loaded " + products.size() + " products");
                    },
                    error -> {
                         System.err.println("❌ Error loading products: " + error.getMessage());
                         error.printStackTrace(); // 🐛 Print full error
                    });
     }

     /**
      * Load danh sách stock movements từ API
      * ✅ Dùng BaseController.executeAsync() - tự động xử lý background thread
      */
     private void loadDataAsync() {
          if (statusLabel != null) {
               statusLabel.setText("🔄 Đang tải dữ liệu...");
          }

          executeAsync(
                    // Background task
                    () -> {
                         try {
                              return stockMovementService.getAllStockMovements();
                         } catch (Exception e) {
                              throw new RuntimeException("Lỗi khi tải stock movements", e);
                         }
                    },

                    // Success callback - chạy trên UI thread
                    (List<StockMovement> movements) -> {
                         allMovements.setAll(movements);
                         filteredMovements.setAll(allMovements);
                         updateStats();

                         if (statusLabel != null) {
                              statusLabel.setText("✅ Đã tải " + movements.size() + " movements");
                         }
                         System.out.println("✅ Loaded " + movements.size() + " stock movements");

                         // 🐛 DEBUG: In ra movement đầu tiên để kiểm tra
                         if (!movements.isEmpty()) {
                              StockMovement first = movements.get(0);
                              System.out.println("🐛 First movement: ID=" + first.getId() +
                                        ", ProductID=" + first.getProductId() +
                                        ", ProductName='" + first.getProductName() + "'" +
                                        ", Qty=" + first.getQty() +
                                        ", MoveType=" + first.getMoveType());
                         }
                    },

                    // Error callback - BaseController tự động show alert
                    error -> {
                         if (statusLabel != null) {
                              statusLabel.setText("❌ Lỗi: " + error.getMessage());
                         }
                    });
     }

     // ====================================================================
     // CRUD OPERATIONS
     // ====================================================================

     /**
      * CREATE - Tạo stock movement mới
      */
     private void createMovementAsync() {
          if (!validateForm())
               return;

          StockMovement movement = buildMovementFromForm();
          if (statusLabel != null) {
               statusLabel.setText("🔄 Đang lưu...");
          }

          executeAsync(
                    () -> {
                         try {
                              return stockMovementService.createStockMovement(movement);
                         } catch (Exception e) {
                              throw new RuntimeException("Lỗi khi tạo stock movement", e);
                         }
                    },
                    (StockMovement saved) -> {
                         if (statusLabel != null) {
                              statusLabel.setText("✅ Đã lưu ID: " + saved.getId());
                         }
                         clearForm();
                         loadDataAsync(); // Reload stock movements
                         loadProductsAsync(); // ✅ Reload products để cập nhật qty_on_hand
                         showSuccess("Đã tạo stock movement thành công!");
                    },
                    error -> {
                         if (statusLabel != null) {
                              statusLabel.setText("❌ Lỗi: " + error.getMessage());
                         }
                    });
     }

     /**
      * UPDATE - Cập nhật stock movement
      */
     private void updateMovementAsync() {
          if (!validateForm())
               return;

          StockMovement movement = buildMovementFromForm();
          movement.setId(editingMovement.getId());

          if (statusLabel != null) {
               statusLabel.setText("🔄 Đang cập nhật...");
          }

          executeAsync(
                    () -> {
                         try {
                              return stockMovementService.updateStockMovement(movement);
                         } catch (Exception e) {
                              throw new RuntimeException("Lỗi khi cập nhật", e);
                         }
                    },
                    (StockMovement updated) -> {
                         if (statusLabel != null) {
                              statusLabel.setText("✅ Đã cập nhật ID: " + updated.getId());
                         }
                         exitEditMode();
                         loadDataAsync(); // Reload stock movements
                         loadProductsAsync(); // ✅ Reload products để cập nhật qty_on_hand
                         showSuccess("Đã cập nhật thành công!");
                    },
                    error -> {
                         if (statusLabel != null) {
                              statusLabel.setText("❌ Lỗi: " + error.getMessage());
                         }
                    });
     }

     /**
      * DELETE - Xóa movement
      * Chưa có button, nhưng code sẵn để reference
      */
     @SuppressWarnings("unused")
     private void deleteMovementAsync(int movementId) {
          showAlert(AlertType.CONFIRMATION, "Xác nhận xóa",
                    "Bạn có chắc muốn xóa movement ID " + movementId + "?");

          executeAsync(
                    () -> {
                         try {
                              return stockMovementService.deleteStockMovement(movementId);
                         } catch (Exception e) {
                              throw new RuntimeException("Lỗi khi xóa", e);
                         }
                    },
                    (Boolean success) -> {
                         if (success) {
                              loadDataAsync();
                              showSuccess("Đã xóa thành công!");
                         } else {
                              showError("Không thể xóa movement");
                         }
                    });
     }

     // ====================================================================
     // BUTTON EVENT HANDLERS
     // ====================================================================

     @FXML
     private void onSaveButton() {
          if (isEditMode) {
               updateMovementAsync();
          } else {
               createMovementAsync();
          }
     }

     @FXML
     private void onClearButton() {
          if (isEditMode) {
               exitEditMode();
          } else {
               clearForm();
          }
     }

     @FXML
     private void onFilterButton() {
          applyFilters();
     }

     @FXML
     private void onResetFilterButton() {
          if (filterProductBox != null)
               filterProductBox.getSelectionModel().selectFirst();
          if (filterMoveTypeBox != null)
               filterMoveTypeBox.getSelectionModel().selectFirst();
          if (filterDateFrom != null)
               filterDateFrom.setValue(null);
          if (filterDateTo != null)
               filterDateTo.setValue(null);

          filteredMovements.setAll(allMovements);
          updateStats();

          if (statusLabel != null) {
               statusLabel.setText("✅ Filter reset");
          }
     }

     /**
      * Button: Refresh - Reload data từ server
      */
     @FXML
     private void onRefreshButton() {
          System.out.println("🔄 Refreshing data...");
          loadDataAsync(); // Reload stock movements

          if (statusLabel != null) {
               statusLabel.setText("🔄 Refreshing...");
          }
     }

     /**
      * Button: Export - Export data to Excel/CSV (placeholder)
      */
     @FXML
     private void onExportButton() {
          System.out.println("📤 Export button clicked");

          if (statusLabel != null) {
               statusLabel.setText("⚠️ Export feature coming soon...");
          }

          // TODO: Implement export to Excel/CSV functionality
          // For now, just show a message
          System.out.println("Export functionality not yet implemented");
     }

     // ====================================================================
     // FILTER LOGIC
     // ====================================================================

     /**
      * Apply filters theo product, moveType, dateRange
      * Dùng Java Stream API để filter
      */
     private void applyFilters() {
          System.out.println("🔍 Applying filters...");

          String selectedProduct = (filterProductBox != null && filterProductBox.getValue() != null)
                    ? filterProductBox.getValue()
                    : "All Products";
          String selectedMoveType = (filterMoveTypeBox != null && filterMoveTypeBox.getValue() != null)
                    ? filterMoveTypeBox.getValue()
                    : "All";
          LocalDate dateFrom = (filterDateFrom != null) ? filterDateFrom.getValue() : null;
          LocalDate dateTo = (filterDateTo != null) ? filterDateTo.getValue() : null;

          List<StockMovement> filtered = allMovements.stream()
                    .filter(m -> {
                         // Filter by product
                         if (!selectedProduct.equals("All Products")) {
                              try {
                                   int productId = Integer.parseInt(selectedProduct.split(" - ")[0]);
                                   if (m.getProductId() != productId)
                                        return false;
                              } catch (Exception e) {
                                   /* ignore */ }
                         }

                         // Filter by move type
                         if (!selectedMoveType.equals("All")) {
                              if (m.getMoveType() == null || !m.getMoveType().equals(selectedMoveType)) {
                                   return false;
                              }
                         }

                         // Filter by date range
                         if (dateFrom != null && m.getMovedAt() != null) {
                              if (m.getMovedAt().toLocalDate().isBefore(dateFrom))
                                   return false;
                         }
                         if (dateTo != null && m.getMovedAt() != null) {
                              if (m.getMovedAt().toLocalDate().isAfter(dateTo))
                                   return false;
                         }

                         return true;
                    })
                    .collect(Collectors.toList());

          filteredMovements.setAll(filtered);
          updateStats();

          if (statusLabel != null) {
               statusLabel.setText("✅ Tìm thấy " + filtered.size() + " movements");
          }
     }

     // ====================================================================
     // HELPER METHODS
     // ====================================================================

     /**
      * Setup product filter dropdown
      */
     private void setupProductFilter() {
          if (filterProductBox != null) {
               filterProductBox.getItems().clear();
               filterProductBox.getItems().add("All Products");

               allProducts.stream()
                         .map(p -> p.getId() + " - " + p.getName())
                         .forEach(filterProductBox.getItems()::add);

               filterProductBox.getSelectionModel().selectFirst();
          }
     }

     /**
      * Tính toán và hiển thị thống kê: Total, In, Out
      */
     private void updateStats() {
          if (statsLabel == null)
               return;

          int total = filteredMovements.size();

          // Đếm movements IN
          int in = (int) filteredMovements.stream()
                    .filter(m -> {
                         String type = m.getMoveType();
                         return type != null && (type.equals("PURCHASE") ||
                                   type.equals("RETURN_IN") ||
                                   type.contains("IN"));
                    })
                    .count();

          // Đếm movements OUT
          int out = (int) filteredMovements.stream()
                    .filter(m -> {
                         String type = m.getMoveType();
                         return type != null && (type.equals("SALE") ||
                                   type.equals("RETURN_OUT") ||
                                   type.equals("CONSUME") ||
                                   type.equals("TRANSFER") ||
                                   type.contains("OUT"));
                    })
                    .count();

          statsLabel.setText(String.format("Total: %d | In: %d | Out: %d", total, in, out));

          if (totalMovementsLabel != null) {
               totalMovementsLabel.setText("Total: " + total);
          }
     }

     /**
      * Update số lượng hiện tại của product
      */
     private void updateCurrentQty() {
          if (productField == null || currentQtyLabel == null)
               return;

          try {
               String input = productField.getText().trim();
               if (input.isEmpty()) {
                    currentQtyLabel.setText("Qty: -");
                    selectedProductId = null;
                    return;
               }

               int productId = Integer.parseInt(input);
               selectedProductId = productId;

               Product found = allProducts.stream()
                         .filter(p -> p.getId() == productId)
                         .findFirst()
                         .orElse(null);

               if (found != null) {
                    currentQtyLabel.setText("Qty: " + (int) found.getQtyOnHand());
                    productField.setText(productId + " - " + found.getName());
               } else {
                    currentQtyLabel.setText("Qty: Not found");
               }
          } catch (NumberFormatException e) {
               currentQtyLabel.setText("Qty: Invalid ID");
               selectedProductId = null;
          }
     }

     // ====================================================================
     // FORM OPERATIONS
     // ====================================================================

     /**
      * Validate form trước khi save
      */
     private boolean validateForm() {
          if (selectedProductId == null || selectedProductId <= 0) {
               showWarning("Vui lòng chọn sản phẩm hợp lệ");
               return false;
          }

          if (moveTypeBox == null || moveTypeBox.getValue() == null) {
               showWarning("Vui lòng chọn loại giao dịch");
               return false;
          }

          if (qtyField == null || qtyField.getText().isBlank()) {
               showWarning("Vui lòng nhập số lượng");
               return false;
          }

          int qty;
          try {
               qty = Integer.parseInt(qtyField.getText());
               if (qty == 0) {
                    showWarning("Số lượng phải khác 0");
                    return false;
               }
          } catch (NumberFormatException e) {
               showWarning("Số lượng không hợp lệ");
               return false;
          }

          // Validate expiry date (không được trong quá khứ)
          if (expiryDatePicker != null && expiryDatePicker.getValue() != null) {
               if (expiryDatePicker.getValue().isBefore(LocalDate.now())) {
                    showWarning("Ngày hết hạn không thể là ngày trong quá khứ!");
                    return false;
               }
          }

          // Warning: Kiểm tra OUT movement với số lượng tồn kho
          String moveType = moveTypeBox.getValue();
          if (moveType != null && (moveType.equals("OUT") || moveType.equals("ADJUST_DOWN")
                    || moveType.equals("RETURN_TO_VENDOR"))) {
               Product selectedProduct = allProducts.stream()
                         .filter(p -> p.getId() == selectedProductId)
                         .findFirst()
                         .orElse(null);

               if (selectedProduct != null) {
                    int currentQty = selectedProduct.getQtyOnHand();
                    int outQty = Math.abs(qty); // OUT movements có qty âm

                    if (outQty > currentQty) {
                         Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                         confirm.setTitle("Cảnh báo tồn kho");
                         confirm.setHeaderText("Số lượng xuất vượt quá tồn kho!");
                         confirm.setContentText(String.format(
                                   "Sản phẩm: %s\n" +
                                             "Tồn kho hiện tại: %d\n" +
                                             "Số lượng xuất: %d\n\n" +
                                             "Bạn có chắc muốn tiếp tục?",
                                   selectedProduct.getName(), currentQty, outQty));

                         if (confirm.showAndWait().get() != ButtonType.OK) {
                              return false;
                         }
                    }
               }
          }

          return true;
     }

     /**
      * Build StockMovement object từ form
      */
     private StockMovement buildMovementFromForm() {
          StockMovement m = new StockMovement();

          m.setProductId(selectedProductId);
          m.setMoveType(moveTypeBox.getValue());
          m.setQty(Integer.parseInt(qtyField.getText()));

          if (refTableBox != null && refTableBox.getValue() != null) {
               m.setRefTable(refTableBox.getValue());
          }

          if (refIdField != null && !refIdField.getText().isBlank()) {
               try {
                    m.setRefId(Integer.parseInt(refIdField.getText()));
               } catch (NumberFormatException e) {
                    /* ignore */ }
          }

          if (batchNoField != null)
               m.setBatchNo(batchNoField.getText());
          if (expiryDatePicker != null)
               m.setExpiryDate(expiryDatePicker.getValue());
          if (serialNoField != null)
               m.setSerialNo(serialNoField.getText());

          if (movedbyField1 != null && !movedbyField1.getText().isBlank()) {
               try {
                    m.setMovedBy(Integer.parseInt(movedbyField1.getText()));
               } catch (NumberFormatException e) {
                    m.setMovedBy(1);
               }
          } else {
               m.setMovedBy(1);
          }

          if (movedatDatePicker1 != null && movedatDatePicker1.getValue() != null) {
               m.setMovedAt(movedatDatePicker1.getValue().atStartOfDay());
          } else {
               m.setMovedAt(java.time.LocalDateTime.now());
          }

          if (noteField != null)
               m.setNote(noteField.getText());

          return m;
     }

     /**
      * Clear form
      */
     private void clearForm() {
          if (productField != null)
               productField.clear();
          if (currentQtyLabel != null)
               currentQtyLabel.setText("Qty: -");
          if (moveTypeBox != null)
               moveTypeBox.getSelectionModel().clearSelection();
          if (qtyField != null)
               qtyField.clear();
          if (refTableBox != null)
               refTableBox.getSelectionModel().clearSelection();
          if (refIdField != null)
               refIdField.clear();
          if (batchNoField != null)
               batchNoField.clear();
          if (expiryDatePicker != null)
               expiryDatePicker.setValue(null);
          if (serialNoField != null)
               serialNoField.clear();
          if (movedbyField1 != null)
               movedbyField1.clear();
          if (movedatDatePicker1 != null)
               movedatDatePicker1.setValue(null);
          if (noteField != null)
               noteField.clear();

          selectedProductId = null;
     }

     /**
      * Populate form từ movement (dùng khi edit)
      */
     private void populateFormFromMovement(StockMovement m) {
          if (m == null)
               return;

          selectedProductId = m.getProductId();

          if (productField != null) {
               productField.setText(String.valueOf(m.getProductId()));
               updateCurrentQty();
          }

          if (moveTypeBox != null)
               moveTypeBox.setValue(m.getMoveType());
          if (qtyField != null)
               qtyField.setText(String.valueOf(m.getQty()));
          if (refTableBox != null && m.getRefTable() != null)
               refTableBox.setValue(m.getRefTable());
          if (refIdField != null && m.getRefId() > 0)
               refIdField.setText(String.valueOf(m.getRefId()));
          if (batchNoField != null)
               batchNoField.setText(m.getBatchNo());
          if (expiryDatePicker != null)
               expiryDatePicker.setValue(m.getExpiryDate());
          if (serialNoField != null)
               serialNoField.setText(m.getSerialNo());
          if (movedbyField1 != null)
               movedbyField1.setText(String.valueOf(m.getMovedBy()));
          if (movedatDatePicker1 != null && m.getMovedAt() != null) {
               movedatDatePicker1.setValue(m.getMovedAt().toLocalDate());
          }
          if (noteField != null)
               noteField.setText(m.getNote());
     }

     // ====================================================================
     // MODE SWITCHING - ADD vs EDIT
     // ====================================================================

     private void enterAddMode() {
          isEditMode = false;
          editingMovement = null;

          if (modeLabel != null)
               modeLabel.setText("➕ ADD MODE");
          if (saveButton != null)
               saveButton.setText("Lưu");
          if (clearButton != null)
               clearButton.setText("Làm mới");

          clearForm();
     }

     private void enterEditMode(StockMovement movement) {
          isEditMode = true;
          editingMovement = movement;

          populateFormFromMovement(movement);

          if (modeLabel != null)
               modeLabel.setText("📝 EDIT MODE - ID: " + movement.getId());
          if (saveButton != null)
               saveButton.setText("Update Movement");
          if (clearButton != null)
               clearButton.setText("Cancel");
          if (statusLabel != null)
               statusLabel.setText("Editing movement ID: " + movement.getId());
     }

     private void exitEditMode() {
          enterAddMode();
          if (statusLabel != null)
               statusLabel.setText("Cancelled edit mode");
     }
}
