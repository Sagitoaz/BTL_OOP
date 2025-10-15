package org.example.oop.Control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.example.oop.Model.Inventory.InitialStockLine;
import org.example.oop.Model.Inventory.Inventory;
import org.example.oop.Utils.ApiClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AddInventoryController {

     // ===== TAB 1: PRODUCT INFO =====
     @FXML
     private TextField tfSku;
     @FXML
     private TextField tfName;
     @FXML
     private ComboBox<String> cbCategory;
     @FXML
     private TextField tfUnit;
     @FXML
     private TextField tfPriceCost;
     @FXML
     private TextField tfPriceRetail;
     @FXML
     private CheckBox chkActive;
     @FXML
     private TextField tfId;
     @FXML
     private TextField tfCreatedAt;
     @FXML
     private TextArea taNote;
     @FXML
     private Button btnResetProduct;
     @FXML
     private Button btnSaveProduct;

     // ===== TAB 2: INITIAL STOCK =====
     @FXML
     private ComboBox<String> cbInitProduct;
     @FXML
     private Label lblInitTotalQty;
     @FXML
     private Button btnAddLine;
     @FXML
     private Button btnRemoveLine;
     @FXML
     private Button btnImportCsv;

     @FXML
     private TableView<InitialStockLine> tblInitLines;
     @FXML
     private TableColumn<InitialStockLine, String> colBatch;
     @FXML
     private TableColumn<InitialStockLine, LocalDate> colExpiry;
     @FXML
     private TableColumn<InitialStockLine, String> colSerial;
     @FXML
     private TableColumn<InitialStockLine, Integer> colQty;
     @FXML
     private TableColumn<InitialStockLine, String> colLineNote;
     @FXML
     private TableColumn<InitialStockLine, String> colRefid;
     @FXML
     private TableColumn<InitialStockLine, String> colRed;

     @FXML
     private TextField tfInitMovedBy;
     @FXML
     private DatePicker dpInitMovedDate;
     @FXML
     private TextField tfInitMovedTime;
     @FXML
     private Button btnSaveInitialStock;
     @FXML
     private Button btnResetInitialStock;

     // ===== FOOTER =====
     @FXML
     private Label lblStatus;
     @FXML
     private Button btnClose;

     // ===== API CLIENT =====
     private final ApiClient apiClient = ApiClient.getInstance();

     // ===== DATA =====
     private ObservableList<Inventory> allInventories = FXCollections.observableArrayList();
     private ObservableList<InitialStockLine> initialStockLines = FXCollections.observableArrayList();
     private Inventory savedProduct = null;

     @FXML
     public void initialize() {
          try {
               initializeProductTab();
               initializeInitialStockTab();
               loadData();
               setDefaultValues();
               attachNumericGuards(); // <— thêm dòng này
               updateStatus("Ready - Nhập thông tin sản phẩm mới");
          } catch (Exception e) {
               e.printStackTrace();
               updateStatus("Initialization failed: " + e.getMessage());
          }
     }

     private void initializeProductTab() {
          tfId.setEditable(false);
          tfCreatedAt.setEditable(false);
          btnResetProduct.setOnAction(e -> resetProductForm());
          btnSaveProduct.setOnAction(e -> saveProduct());
     }

     private TextFieldTableCell<InitialStockLine, String> commitOnFocusLossStringCell() {
          return new TextFieldTableCell<InitialStockLine, String>(new StringConverter<String>() {
               @Override
               public String toString(String object) {
                    return object;
               }

               @Override
               public String fromString(String string) {
                    return string;
               }
          }) {
               private TextField editor;

               @Override
               public void startEdit() {
                    super.startEdit();
                    if (editor == null) {
                         editor = new TextField(getItem());
                         editor.setOnAction(e -> commitEdit(editor.getText()));
                         editor.focusedProperty().addListener((o, was, is) -> {
                              if (!is)
                                   commitEdit(editor.getText());
                         });
                    }
                    editor.setText(getItem());
                    setText(null);
                    setGraphic(editor);
                    editor.requestFocus();
                    editor.selectAll();
               }

               @Override
               public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
               }

               @Override
               public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                         setText(null);
                         setGraphic(null);
                    } else if (isEditing()) {
                         if (editor != null)
                              editor.setText(item);
                         setText(null);
                         setGraphic(editor);
                    } else {
                         setText(item);
                         setGraphic(null);
                    }
               }
          };
     }

     private TableCell<InitialStockLine, Integer> commitOnFocusLossIntegerCell() {
          return new TableCell<InitialStockLine, Integer>() {
               private TextField editor;

               @Override
               protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                         setText(null);
                         setGraphic(null);
                    } else {
                         setText(item == null ? "0" : item.toString());
                         setGraphic(null);
                    }
               }

               @Override
               public void startEdit() {
                    super.startEdit();
                    if (editor == null) {
                         editor = new TextField(getItem() == null ? "0" : getItem().toString());
                         editor.setOnAction(e -> commitNow());
                         editor.focusedProperty().addListener((o, was, is) -> {
                              if (!is)
                                   commitNow();
                         });
                    }
                    editor.setText(getItem() == null ? "0" : getItem().toString());
                    setText(null);
                    setGraphic(editor);
                    editor.requestFocus();
                    editor.selectAll();
               }

               private void commitNow() {
                    try {
                         int v = Integer.parseInt(editor.getText().trim());
                         if (v < 0)
                              v = 0;
                         super.commitEdit(v);
                         InitialStockLine line = getTableRow() == null ? null : getTableRow().getItem();
                         if (line != null) {
                              line.setQty(v);
                              updateTotalQty();
                         }
                         setText(String.valueOf(v));
                         setGraphic(null);
                    } catch (NumberFormatException ex) {
                         cancelEdit();
                    }
               }

               @Override
               public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() == null ? "0" : getItem().toString());
                    setGraphic(null);
               }
          };
     }

     private void initializeInitialStockTab() {
          // Value factories trỏ trực tiếp vào JavaFX Property
          colBatch.setCellValueFactory(cd -> cd.getValue().batchNoProperty());
          colExpiry.setCellValueFactory(cd -> cd.getValue().expiryDateProperty());
          colSerial.setCellValueFactory(cd -> cd.getValue().serialNoProperty());
          colQty.setCellValueFactory(cd -> cd.getValue().qtyProperty().asObject());
          colLineNote.setCellValueFactory(cd -> cd.getValue().noteProperty());
          colRefid.setCellValueFactory(cd -> cd.getValue().refidProperty());
          colRed.setCellValueFactory(cd -> cd.getValue().redProperty());

          // Cell factories: commit khi mất focus
          colBatch.setCellFactory(c -> commitOnFocusLossStringCell());
          colSerial.setCellFactory(c -> commitOnFocusLossStringCell());
          colLineNote.setCellFactory(c -> commitOnFocusLossStringCell());
          colRefid.setCellFactory(c -> commitOnFocusLossStringCell());
          colRed.setCellFactory(c -> commitOnFocusLossStringCell());
          colQty.setCellFactory(c -> commitOnFocusLossIntegerCell());

          // Sự kiện OnEditCommit để cập nhật model (không bắt buộc, nhưng an toàn)
          colBatch.setOnEditCommit(e -> e.getRowValue().setBatchNo(e.getNewValue() == null ? "" : e.getNewValue()));
          colSerial.setOnEditCommit(e -> e.getRowValue().setSerialNo(e.getNewValue() == null ? "" : e.getNewValue()));
          colLineNote.setOnEditCommit(e -> e.getRowValue().setNote(e.getNewValue() == null ? "" : e.getNewValue()));
          colRefid.setOnEditCommit(e -> e.getRowValue().setRefid(e.getNewValue() == null ? "" : e.getNewValue()));
          colRed.setOnEditCommit(e -> e.getRowValue().setRed(e.getNewValue() == null ? "" : e.getNewValue()));
          colQty.setOnEditCommit(e -> {
               int v = e.getNewValue() == null ? 0 : Math.max(0, e.getNewValue());
               e.getRowValue().setQty(v);
               updateTotalQty();
          });

          // Cột ngày với DatePicker + commit khi đổi/mất focus
          colExpiry.setCellFactory(col -> new TableCell<InitialStockLine, LocalDate>() {
               private DatePicker picker;
               private final StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
                    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    @Override
                    public String toString(LocalDate d) {
                         return d == null ? "" : fmt.format(d);
                    }

                    @Override
                    public LocalDate fromString(String s) {
                         if (s == null || s.isBlank())
                              return null;
                         return LocalDate.parse(s, fmt);
                    }
               };

               @Override
               protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty) {
                         setText(null);
                         setGraphic(null);
                         return;
                    }
                    if (isEditing()) {
                         if (picker != null)
                              picker.setValue(date);
                         setText(null);
                         setGraphic(picker);
                    } else {
                         setText(date == null ? "" : converter.toString(date));
                         setGraphic(null);
                    }
               }

               @Override
               public void startEdit() {
                    super.startEdit();
                    if (picker == null) {
                         picker = new DatePicker(getItem());
                         picker.setConverter(converter);
                         picker.valueProperty().addListener((o, oldV, newV) -> commitEdit(newV));
                         picker.focusedProperty().addListener((o, was, is) -> {
                              if (!is)
                                   commitEdit(picker.getValue());
                         });
                    }
                    picker.setValue(getItem());
                    setText(null);
                    setGraphic(picker);
                    picker.requestFocus();
               }

               @Override
               public void commitEdit(LocalDate newValue) {
                    super.commitEdit(newValue);
                    InitialStockLine line = getTableRow() == null ? null : getTableRow().getItem();
                    if (line != null)
                         line.setExpiryDate(newValue);
                    setText(newValue == null ? "" : converter.toString(newValue));
                    setGraphic(null);
               }

               @Override
               public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() == null ? "" : converter.toString(getItem()));
                    setGraphic(null);
               }
          });

          tblInitLines.setEditable(true);
          colBatch.setEditable(true);
          colSerial.setEditable(true);
          colLineNote.setEditable(true);
          colRefid.setEditable(true);
          colRed.setEditable(true);
          colQty.setEditable(true);
          colExpiry.setEditable(true);

          tblInitLines.setItems(initialStockLines);

          btnAddLine.setOnAction(e -> addNewInitialStockLine());
          btnRemoveLine.setOnAction(e -> removeSelectedInitialStockLine());
          btnImportCsv.setOnAction(e -> importFromCsv());
          btnSaveInitialStock.setOnAction(e -> saveInitialStock());
          btnResetInitialStock.setOnAction(e -> resetInitialStock());
          btnClose.setOnAction(e -> closeWindow());
     }

     private void loadData() {
          updateStatus("🔄 Đang tải dữ liệu sản phẩm...");

          apiClient.getAsync("/api/inventory", response -> {
               if (response.isSuccess()) {
                    try {
                         // Parse JSON response to get inventory list
                         String jsonData = response.getData();
                         allInventories = parseInventoryListFromJson(jsonData);

                         ObservableList<String> productNames = FXCollections.observableArrayList();
                         for (Inventory inv : allInventories) {
                              productNames.add(inv.getName() + " (" + inv.getSku() + ")");
                         }
                         cbInitProduct.setItems(productNames);
                         updateStatus("✅ Đã tải " + allInventories.size() + " sản phẩm");

                    } catch (Exception e) {
                         updateStatus("❌ Lỗi phân tích dữ liệu: " + e.getMessage());
                         e.printStackTrace();
                    }
               } else {
                    updateStatus("❌ Không thể tải dữ liệu sản phẩm: " + response.getErrorMessage());
               }
          }, errorMessage -> {
               updateStatus("❌ Lỗi kết nối API: " + errorMessage);
               // Fallback to empty list
               allInventories = FXCollections.observableArrayList();
               cbInitProduct.setItems(FXCollections.observableArrayList());
          });
     }

     /**
      * Simple JSON parser for inventory list (without external libraries)
      */
     private ObservableList<Inventory> parseInventoryListFromJson(String json) {
          ObservableList<Inventory> inventories = FXCollections.observableArrayList();

          try {
               // Basic JSON parsing for inventory items
               // Assume JSON format: {"items": [...], "totalItems": n}
               if (json.contains("\"items\"")) {
                    int itemsStart = json.indexOf("\"items\":[") + 9;
                    int itemsEnd = json.indexOf("]", itemsStart);

                    if (itemsStart > 8 && itemsEnd > itemsStart) {
                         String itemsJson = json.substring(itemsStart, itemsEnd);
                         String[] items = itemsJson.split("\\},\\s*\\{");

                         for (String item : items) {
                              if (!item.trim().isEmpty()) {
                                   Inventory inventory = parseInventoryFromJson(item);
                                   if (inventory != null) {
                                        inventories.add(inventory);
                                   }
                              }
                         }
                    }
               }

          } catch (Exception e) {
               System.err.println("Error parsing inventory JSON: " + e.getMessage());
          }

          return inventories;
     }

     /**
      * Parse single inventory item from JSON
      */
     private Inventory parseInventoryFromJson(String json) {
          try {
               Inventory inventory = new Inventory();

               // Simple field extraction
               inventory.setId((int) extractLongField(json, "id", 0L));
               inventory.setSku(extractStringField(json, "sku", ""));
               inventory.setName(extractStringField(json, "name", ""));
               inventory.setCategory(extractStringField(json, "category", ""));
               inventory.setQuantity(extractIntField(json, "currentStock", 0));
               inventory.setUnit(extractStringField(json, "unit", ""));
               inventory.setPriceCost(extractIntField(json, "priceCost", 0));
               inventory.setUnitPrice(extractIntField(json, "unitPrice", 0));
               inventory.setActive(true); // Default
               inventory.setType("product");
               inventory.setCreatedAt(LocalDateTime.now());
               inventory.setLastUpdated(LocalDate.now());

               return inventory;

          } catch (Exception e) {
               System.err.println("Error parsing inventory item: " + e.getMessage());
               return null;
          }
     }

     // Simple JSON field extractors
     private String extractStringField(String json, String field, String defaultValue) {
          try {
               String pattern = "\"" + field + "\":\"";
               int start = json.indexOf(pattern);
               if (start == -1)
                    return defaultValue;

               start += pattern.length();
               int end = json.indexOf("\"", start);
               if (end == -1)
                    return defaultValue;

               return json.substring(start, end);
          } catch (Exception e) {
               return defaultValue;
          }
     }

     private long extractLongField(String json, String field, long defaultValue) {
          try {
               String pattern = "\"" + field + "\":";
               int start = json.indexOf(pattern);
               if (start == -1)
                    return defaultValue;

               start += pattern.length();
               int end = json.indexOf(",", start);
               if (end == -1)
                    end = json.indexOf("}", start);
               if (end == -1)
                    return defaultValue;

               String value = json.substring(start, end).trim();
               return Long.parseLong(value);
          } catch (Exception e) {
               return defaultValue;
          }
     }

     private int extractIntField(String json, String field, int defaultValue) {
          try {
               return (int) extractLongField(json, field, defaultValue);
          } catch (Exception e) {
               return defaultValue;
          }
     }

     private void setDefaultValues() {
          chkActive.setSelected(true);
          tfCreatedAt.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          dpInitMovedDate.setValue(LocalDate.now());
          tfInitMovedTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
          tfInitMovedBy.setText("1");
          updateTotalQty();
     }

     // ===== PRODUCT TAB METHODS =====
     @FXML
     private void resetProductForm() {
          tfSku.clear();
          tfName.clear();
          cbCategory.getSelectionModel().clearSelection();
          tfUnit.clear();
          tfPriceCost.clear();
          tfPriceRetail.clear();
          chkActive.setSelected(true);
          tfId.clear();
          taNote.clear();
          savedProduct = null;
          tfCreatedAt.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          updateStatus("Form đã được reset");
     }

     @FXML
     private void saveProduct() {
          try {
               if (!validateProductInput())
                    return;

               // Kiểm tra & parse số nguyên không âm cho giá
               int priceCost = parseNonNegativeIntOrAlert(tfPriceCost, "Giá vốn", 0);
               int priceRetail = parseNonNegativeIntOrAlert(tfPriceRetail, "Giá bán lẻ", 0);

               // Build JSON for API request
               String jsonBody = new ApiClient.JsonBuilder()
                         .add("sku", tfSku.getText().trim())
                         .add("name", tfName.getText().trim())
                         .add("category", cbCategory.getValue())
                         .add("unit", tfUnit.getText().trim())
                         .add("priceCost", priceCost)
                         .add("unitPrice", priceRetail)
                         .add("minStock", 10) // Default min stock
                         .add("maxStock", 1000) // Default max stock
                         .add("description", taNote.getText().trim())
                         .build();

               updateStatus("🔄 Đang lưu sản phẩm...");
               btnSaveProduct.setDisable(true);

               apiClient.postAsync("/api/inventory", jsonBody, response -> {
                    btnSaveProduct.setDisable(false);

                    if (response.isSuccess()) {
                         try {
                              // Parse response to get created inventory
                              Inventory inventory = parseInventoryFromJson(response.getData());
                              if (inventory != null) {
                                   savedProduct = inventory;
                                   tfId.setText(String.valueOf(inventory.getId()));

                                   // Add to local list and dropdown
                                   allInventories.add(inventory);
                                   String productDisplay = inventory.getName() + " (" + inventory.getSku() + ")";
                                   cbInitProduct.getItems().add(productDisplay);
                                   cbInitProduct.setValue(productDisplay);

                                   updateStatus("✅ Đã lưu sản phẩm: " + inventory.getName() + " (ID: "
                                             + inventory.getId() + ")");
                              } else {
                                   updateStatus("✅ Sản phẩm đã được lưu thành công");
                              }
                         } catch (Exception e) {
                              updateStatus("✅ Sản phẩm đã lưu, nhưng có lỗi khi cập nhật giao diện");
                              e.printStackTrace();
                         }
                    } else {
                         updateStatus("❌ Lỗi lưu sản phẩm: " + response.getErrorMessage());

                         // Show detailed error dialog
                         Alert alert = new Alert(Alert.AlertType.ERROR);
                         alert.setTitle("Lỗi lưu sản phẩm");
                         alert.setHeaderText("Không thể lưu sản phẩm mới");
                         alert.setContentText(response.getErrorMessage());
                         alert.showAndWait();
                    }
               }, errorMessage -> {
                    btnSaveProduct.setDisable(false);
                    updateStatus("❌ Lỗi kết nối API: " + errorMessage);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi kết nối");
                    alert.setHeaderText("Không thể kết nối đến server");
                    alert.setContentText(
                              "Chi tiết: " + errorMessage + "\n\nVui lòng kiểm tra kết nối mạng và server backend.");
                    alert.showAndWait();
               });

          } catch (IllegalArgumentException ex) {
               // Đã hiện Alert + focus trong helper → dừng lại
          } catch (Exception e) {
               updateStatus("❌ Lỗi lưu sản phẩm: " + e.getMessage());
               e.printStackTrace();
          }
     }

     private boolean validateProductInput() {
          if (tfSku.getText() == null || tfSku.getText().trim().isEmpty()) {
               updateStatus("❌ SKU không được để trống");
               tfSku.requestFocus();
               return false;
          }
          if (tfName.getText() == null || tfName.getText().trim().isEmpty()) {
               updateStatus("❌ Tên sản phẩm không được để trống");
               tfName.requestFocus();
               return false;
          }
          if (cbCategory.getValue() == null) {
               updateStatus("❌ Vui lòng chọn danh mục");
               cbCategory.requestFocus();
               return false;
          }
          if (tfUnit.getText() == null || tfUnit.getText().trim().isEmpty()) {
               updateStatus("❌ Đơn vị không được để trống");
               tfUnit.requestFocus();
               return false;
          }
          String sku = tfSku.getText().trim();
          for (Inventory inv : allInventories) {
               if (sku.equalsIgnoreCase(inv.getSku())) {
                    updateStatus("❌ SKU đã tồn tại: " + sku);
                    tfSku.requestFocus();
                    return false;
               }
          }
          return true;
     }

     // ===== INITIAL STOCK TAB METHODS =====
     @FXML
     private void addNewInitialStockLine() {
          InitialStockLine newLine = new InitialStockLine();
          newLine.setBatchNo("BATCH" + String.format("%03d", initialStockLines.size() + 1));
          newLine.setQty(1);
          newLine.setExpiryDate(LocalDate.now().plusYears(1));
          newLine.setSerialNo("");
          newLine.setNote("");
          newLine.setRefid("");
          newLine.setRed("");
          initialStockLines.add(newLine);
          updateTotalQty();
          updateStatus("Đã thêm dòng mới - Click vào ô để chỉnh sửa");
          if (tblInitLines != null) {
               tblInitLines.getSelectionModel().select(newLine);
               tblInitLines.scrollTo(newLine);
          }
     }

     @FXML
     private void removeSelectedInitialStockLine() {
          InitialStockLine selected = tblInitLines.getSelectionModel().getSelectedItem();
          if (selected != null) {
               initialStockLines.remove(selected);
               updateTotalQty();
               updateStatus("Đã xóa dòng được chọn");
          } else
               updateStatus("❌ Vui lòng chọn dòng cần xóa");
     }

     @FXML
     private void importFromCsv() {
          try {
               javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
               fileChooser.setTitle("Import Initial Stock from CSV");
               fileChooser.getExtensionFilters().add(
                         new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
               java.io.File selectedFile = fileChooser.showOpenDialog(btnImportCsv.getScene().getWindow());
               if (selectedFile == null)
                    return;

               List<InitialStockLine> importedLines = org.example.oop.Utils.InitialStockCsvUtils
                         .importFromCsv(selectedFile);

               org.example.oop.Utils.InitialStockCsvUtils.ValidationResult validation = org.example.oop.Utils.InitialStockCsvUtils
                         .validateImportedLines(importedLines);

               if (validation.hasErrors()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Import Errors");
                    alert.setHeaderText("Cannot import CSV due to errors:");
                    alert.setContentText(validation.getSummary());
                    alert.showAndWait();
                    return;
               }

               initialStockLines.clear();
               initialStockLines.addAll(importedLines);
               updateTotalQty();

               if (validation.hasWarnings()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Import Warnings");
                    alert.setHeaderText("Import completed with warnings:");
                    alert.setContentText(validation.getSummary());
                    alert.showAndWait();
               } else {
                    updateStatus("✅ Đã import " + importedLines.size() + " dòng từ CSV");
               }
          } catch (Exception e) {
               Alert alert = new Alert(Alert.AlertType.ERROR);
               alert.setTitle("Import Error");
               alert.setHeaderText("Cannot import CSV file:");
               alert.setContentText(e.getMessage());
               alert.showAndWait();
               updateStatus("❌ Lỗi import CSV: " + e.getMessage());
          }
     }

     @FXML
     private void saveInitialStock() {
          try {
               finalizeTableEdits(); // ép commit editor còn mở

               if (!validateInitialStockInput())
                    return;

               Inventory selectedProduct = getSelectedProduct();
               if (selectedProduct == null) {
                    updateStatus("❌ Vui lòng chọn sản phẩm");
                    return;
               }

               // movedBy bắt buộc là integer không âm
               int movedBy = parseNonNegativeIntOrAlert(tfInitMovedBy, "User ID", 0);

               LocalDateTime movedAt = parseMovedAt();
               if (movedAt == null) {
                    updateStatus("❌ Định dạng thời gian không hợp lệ");
                    return;
               }

               final int totalQty = calculateTotalQty();
               final Inventory finalSelectedProduct = selectedProduct;

               if (totalQty <= 0) {
                    updateStatus("❌ Không có dòng nào có số lượng > 0");
                    return;
               }

               // Build JSON for initial stock API call
               String jsonBody = new ApiClient.JsonBuilder()
                         .add("qty", totalQty)
                         .add("note", "Initial stock from UI - " + initialStockLines.size() + " batches")
                         .add("batchNo", "INITIAL-" + System.currentTimeMillis())
                         .build();

               updateStatus("🔄 Đang lưu tồn kho ban đầu...");
               btnSaveInitialStock.setDisable(true);

               String endpoint = "/api/inventory/" + selectedProduct.getId() + "/initial-stock";

               apiClient.postAsync(endpoint, jsonBody, response -> {
                    btnSaveInitialStock.setDisable(false);

                    if (response.isSuccess()) {
                         updateStatus("✅ Đã lưu tồn kho ban đầu: " + totalQty + " đơn vị cho sản phẩm "
                                   + finalSelectedProduct.getName());

                         // Update local inventory quantity
                         finalSelectedProduct.setQuantity(finalSelectedProduct.getQuantity() + totalQty);

                         resetInitialStock();

                         Alert alert = new Alert(Alert.AlertType.INFORMATION);
                         alert.setTitle("Thành công");
                         alert.setHeaderText("Đã lưu tồn kho ban đầu");
                         alert.setContentText(
                                   "Đã thêm " + totalQty + " đơn vị cho sản phẩm " + finalSelectedProduct.getName());
                         alert.showAndWait();

                    } else {
                         updateStatus("❌ Lỗi lưu tồn kho: " + response.getErrorMessage());

                         Alert alert = new Alert(Alert.AlertType.ERROR);
                         alert.setTitle("Lỗi lưu tồn kho");
                         alert.setHeaderText("Không thể lưu tồn kho ban đầu");
                         alert.setContentText(response.getErrorMessage());
                         alert.showAndWait();
                    }
               }, errorMessage -> {
                    btnSaveInitialStock.setDisable(false);
                    updateStatus("❌ Lỗi kết nối API: " + errorMessage);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi kết nối");
                    alert.setHeaderText("Không thể kết nối đến server");
                    alert.setContentText(
                              "Chi tiết: " + errorMessage + "\n\nVui lòng kiểm tra kết nối mạng và server backend.");
                    alert.showAndWait();
               });

          } catch (IllegalArgumentException ex) {
               // Đã hiện Alert & focus trong helper → chỉ dừng lại
          } catch (Exception e) {
               updateStatus("❌ Lỗi lưu tồn kho ban đầu: " + e.getMessage());
               e.printStackTrace();
          }
     }

     @FXML
     private void resetInitialStock() {
          initialStockLines.clear();
          cbInitProduct.getSelectionModel().clearSelection();
          tfInitMovedBy.setText("1");
          dpInitMovedDate.setValue(LocalDate.now());
          tfInitMovedTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
          updateTotalQty();
          updateStatus("Đã reset form tồn kho ban đầu");
     }

     private boolean validateInitialStockInput() {
          if (cbInitProduct.getValue() == null) {
               updateStatus("❌ Vui lòng chọn sản phẩm");
               return false;
          }
          if (initialStockLines.isEmpty()) {
               updateStatus("❌ Vui lòng thêm ít nhất 1 dòng");
               return false;
          }
          if (tfInitMovedBy.getText() == null || tfInitMovedBy.getText().trim().isEmpty()) {
               updateStatus("❌ Vui lòng nhập User ID");
               return false;
          }
          try {
               Integer.parseInt(tfInitMovedBy.getText().trim());
          } catch (NumberFormatException e) {
               updateStatus("❌ User ID phải là số nguyên");
               return false;
          }
          if (dpInitMovedDate.getValue() == null) {
               updateStatus("❌ Vui lòng chọn ngày");
               return false;
          }
          return true;
     }

     private Inventory getSelectedProduct() {
          String selected = cbInitProduct.getValue();
          if (selected == null)
               return null;
          int start = selected.lastIndexOf('(');
          int end = selected.lastIndexOf(')');
          if (start == -1 || end == -1)
               return null;
          String sku = selected.substring(start + 1, end);
          return allInventories.stream().filter(inv -> sku.equals(inv.getSku())).findFirst().orElse(null);
     }

     private LocalDateTime parseMovedAt() {
          try {
               LocalDate date = dpInitMovedDate.getValue();
               LocalTime time = LocalTime.parse(tfInitMovedTime.getText().trim(),
                         DateTimeFormatter.ofPattern("HH:mm:ss"));
               return LocalDateTime.of(date, time);
          } catch (DateTimeParseException e) {
               try {
                    LocalDate date = dpInitMovedDate.getValue();
                    LocalTime time = LocalTime.parse(tfInitMovedTime.getText().trim(),
                              DateTimeFormatter.ofPattern("HH:mm"));
                    return LocalDateTime.of(date, time);
               } catch (DateTimeParseException e2) {
                    return null;
               }
          }
     }

     private void updateTotalQty() {
          try {
               int total = calculateTotalQty();
               lblInitTotalQty.setText(String.valueOf(total));
          } catch (Exception e) {
               updateStatus("❌ Lỗi cập nhật tổng số lượng: " + e.getMessage());
               lblInitTotalQty.setText("0");
          }
     }

     private int calculateTotalQty() {
          return initialStockLines.stream().mapToInt(line -> Math.max(0, line.getQty())).sum();
     }

     private void updateStatus(String message) {
          lblStatus.setText(message);
          System.out.println("📝 Status: " + message);
     }

     @FXML
     private void closeWindow() {
          Stage stage = (Stage) btnClose.getScene().getWindow();
          stage.close();
     }

     // ===== INT VALIDATION HELPERS =====
     private boolean isNonNegativeInteger(String s) {
          return s != null && s.matches("\\d+"); // chỉ chấp nhận 0..9
     }

     // ===== FORCE-COMMIT EDITORS TRƯỚC KHI LƯU =====
     private void finalizeTableEdits() {
          if (tblInitLines == null)
               return;
          // Mẹo toggle visible cột đầu để ép TableView re-render/commit editor đang mở
          if (!tblInitLines.getColumns().isEmpty()) {
               TableColumn<?, ?> c = tblInitLines.getColumns().get(0);
               boolean vis = c.isVisible();
               c.setVisible(!vis);
               c.setVisible(vis);
          }
     }

     // ===== GẮN CẢNH BÁO SỐ NGUYÊN NGAY KHI RỜI Ô (UX MƯỢT HƠN) =====
     private void attachNumericGuards() {
          tfPriceCost.focusedProperty().addListener((o, was, is) -> {
               if (!is) {
                    try {
                         parseNonNegativeIntOrAlert(tfPriceCost, "Giá vốn", 0);
                    } catch (Exception ignore) {
                    }
               }
          });
          tfPriceRetail.focusedProperty().addListener((o, was, is) -> {
               if (!is) {
                    try {
                         parseNonNegativeIntOrAlert(tfPriceRetail, "Giá bán lẻ", 0);
                    } catch (Exception ignore) {
                    }
               }
          });
          tfInitMovedBy.focusedProperty().addListener((o, was, is) -> {
               if (!is) {
                    try {
                         parseNonNegativeIntOrAlert(tfInitMovedBy, "User ID", 0);
                    } catch (Exception ignore) {
                    }
               }
          });
     }

     /**
      * Parse số nguyên không âm từ TextField; nếu rỗng cho phép defaultValue.
      * Hiện Alert + focus và ném IllegalArgumentException nếu không hợp lệ.
      */
     private int parseNonNegativeIntOrAlert(TextField field, String label, int defaultValue) {
          String raw = field.getText() == null ? "" : field.getText().trim();
          if (raw.isEmpty()) {
               field.setText(String.valueOf(defaultValue));
               return defaultValue;
          }
          if (!isNonNegativeInteger(raw)) {
               Alert a = new Alert(Alert.AlertType.ERROR);
               a.setTitle("Lỗi nhập liệu");
               a.setHeaderText("Trường \"" + label + "\" phải là số nguyên không âm");
               a.setContentText("Giá trị hiện tại: \"" + raw + "\"");
               a.showAndWait();
               field.requestFocus();
               throw new IllegalArgumentException(label + " must be non-negative integer");
          }
          try {
               return Integer.parseInt(raw);
          } catch (NumberFormatException ex) {
               Alert a = new Alert(Alert.AlertType.ERROR);
               a.setTitle("Lỗi nhập liệu");
               a.setHeaderText("Giá trị quá lớn cho \"" + label + "\"");
               a.setContentText("Vui lòng nhập số nhỏ hơn.");
               a.showAndWait();
               field.requestFocus();
               throw ex;
          }
     }
}
