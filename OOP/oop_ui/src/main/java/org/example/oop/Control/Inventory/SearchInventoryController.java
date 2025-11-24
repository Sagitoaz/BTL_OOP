package org.example.oop.Control.Inventory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.example.oop.Control.BaseController;
import org.example.oop.Service.ApiProductService;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Inventory.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn; // ✅ Import Product model
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class SearchInventoryController extends BaseController implements Initializable {
     @FXML
     private TableColumn<Product, String> categoryColumn;
     @FXML
     private TableColumn<Product, Integer> idColumn;
     @FXML
     private TableView<Product> inventoryTable;
     @FXML
     private TableColumn<Product, String> lastUpdatedColumn;
     @FXML
     private TableColumn<Product, String> nameColumn;
     @FXML
     private TableColumn<Product, Integer> priceColumn;
     @FXML
     private TableColumn<Product, Integer> quantityColumn;
     @FXML
     private Button resetButton;
     @FXML
     private Button searchButton;
     @FXML
     private TextField searchTextField;
     @FXML
     private TableColumn<Product, String> unitColumn;
     @FXML
     private DatePicker lastUpdatedPicker;
     @FXML
     private ComboBox<String> categoryBox;
     @FXML
     private ComboBox<String> typeBox;
     @FXML
     private Label itemName;
     @FXML
     TextArea itemDescription;
     @FXML
     private ComboBox<String> statusBox;
     @FXML
     private Button exportButton;

     // Columns thiếu
     @FXML
     private TableColumn<Product, String> skuColumn;
     @FXML
     private TableColumn<Product, String> statusColumn;

     // Quick info fields thiếu
     @FXML
     private Label quickSku;
     @FXML
     private Label quickStatus;
     @FXML
     private Label quickPrice;
     @FXML
     private Label quickType;
     @FXML
     private Label quickCategory;
     @FXML
     private Label quickCostPrice;
     @FXML
     private Label quickQuantity;
     @FXML
     private Label quickUnit;
     @FXML
     private Label quickBatchNumber;
     @FXML
     private Label quickExpiryDate;
     @FXML
     private Label quickserialNo;
     @FXML
     private Label quickCreatedAt;

     // UI components thiếu
     @FXML
     private ImageView itemImage;
     @FXML
     private Label messageLabel;
     @FXML
     private ProgressIndicator loadingIndicator;
     @FXML
     private Label countLabel;

     // Loading status indicator fields
     @FXML
     private HBox loadingStatusContainer;
     @FXML
     private ProgressIndicator statusProgressIndicator;
     @FXML
     private Label loadingStatusLabel;

     @FXML
     private final ApiProductService productService = new ApiProductService();
     private final ObservableList<Product> masterData = FXCollections.observableArrayList();
     private FilteredList<Product> filteredData;

     @FXML
     private void handleBackButton() {
          SceneManager.goBack();
     }

     @FXML
     private void handleForwardButton() {
          SceneManager.goForward();
     }

     @FXML
     private void handleReloadButton() {
          SceneManager.reloadCurrentScene();
     }

     @Override
     public void initialize(URL url, ResourceBundle rb) {
          setupTable();
          setupFilters();
          setupLoadingIndicator();

          // Load data trong background
          loadDataAsync();
     }

     private void setupTable() {
          idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
          skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
          nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

          // ✅ Category - phải dùng custom cell value vì là ENUM
          categoryColumn.setCellValueFactory(cellData -> {
               Product p = cellData.getValue();
               String categoryName = p.getCategoryEnum() != null ? p.getCategoryEnum().getDisplayName() : "";
               return new javafx.beans.property.SimpleStringProperty(categoryName);
          });

          quantityColumn.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand")); // ✅ ĐÚNG: qtyOnHand
          unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
          priceColumn.setCellValueFactory(new PropertyValueFactory<>("priceRetail")); // ✅ ĐÚNG: priceRetail

          // ✅ Status - convert boolean isActive thành text
          statusColumn.setCellValueFactory(cellData -> {
               Product p = cellData.getValue();
               String status = p.isActive() ? "Active" : "Inactive";
               return new javafx.beans.property.SimpleStringProperty(status);
          });
          if (lastUpdatedColumn != null) {
               lastUpdatedColumn.setCellValueFactory(cellData -> {
                    Product p = cellData.getValue();
                    if (p.getCreatedAt() != null) {
                         String date = p.getCreatedAt().format(
                                   DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                         return new javafx.beans.property.SimpleStringProperty(date);
                    }
                    return new javafx.beans.property.SimpleStringProperty("");
               });
          }

          // Selection listener
          inventoryTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                         if (newSelection != null) {
                              updateDetail(newSelection);
                         }
                    });
          filteredData = new FilteredList<>(masterData, p -> true);
          inventoryTable.setItems(filteredData);
     }

     private void setupFilters() {
          // Auto-apply filter on text change
          if (searchTextField != null) {
               searchTextField.textProperty().addListener((obs, o, n) -> applyFilter());
          }
          if (categoryBox != null) {
               categoryBox.valueProperty().addListener((obs, o, n) -> applyFilter());
          }
          if (statusBox != null) {
               statusBox.valueProperty().addListener((obs, o, n) -> applyFilter());
          }
     }

     private void setupLoadingIndicator() {
          if (loadingIndicator != null) {
               loadingIndicator.setVisible(false);
               loadingIndicator.setManaged(false);
          }
     }

     private void loadDataAsync() {
          showLoading(true);
          showLoadingStatus("⏳ Đang tải dữ liệu sản phẩm...");
          updateMessage("🔄 Đang tải dữ liệu...");
          executeAsync(
                    // Background: Load all products
                    () -> {
                         try {
                              return productService.getAllProducts();
                         } catch (Exception e) {
                              throw new RuntimeException("Lỗi khi tải danh sách sản phẩm", e);
                         }
                    },

                    // Success: Update UI
                    loadedProducts -> {
                         masterData.setAll(loadedProducts); // ✅ setAll thay vì clear + addAll

                         setupCategoryBox();
                         setupStatusBox();
                         applyFilter();

                         showLoading(false);
                         showSuccessStatus("✅ Đã tải thành công " + loadedProducts.size() + " sản phẩm!");
                         updateMessage("✅ Đã tải " + loadedProducts.size() + " sản phẩm");
                         updateCountLabel();
                    },
                    error -> {
                         showLoading(false);
                         showErrorStatus("❌ Lỗi: " + error.getMessage());
                         updateMessage("❌ Lỗi tải dữ liệu: " + error.getMessage());
                    });
     }

     private void setupCategoryBox() {
          if (categoryBox == null)
               return;

          ObservableList<String> categories = FXCollections.observableArrayList();
          categories.add("All Categories");

          masterData.stream()
                    .map(p -> p.getCategory() != null ? p.getCategory().toString() : null)
                    .filter(cat -> cat != null && !cat.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .forEach(categories::add);

          categoryBox.setItems(categories);
          categoryBox.getSelectionModel().selectFirst();
     }

     private void setupStatusBox() {
          if (statusBox == null)
               return;

          ObservableList<String> statuses = FXCollections.observableArrayList(
                    "All Status", "Active", "Inactive");
          statusBox.setItems(statuses);
          statusBox.getSelectionModel().selectFirst();
     }

     // ==================== BUTTON HANDLERS ====================

     @FXML
     private void OnClickSearchButton(javafx.event.ActionEvent event) {
          applyFilter();
     }

     @FXML
     private void OnClickResetButton(javafx.event.ActionEvent event) {
          if (searchTextField != null)
               searchTextField.clear();
          if (categoryBox != null)
               categoryBox.getSelectionModel().selectFirst();
          if (statusBox != null)
               statusBox.getSelectionModel().selectFirst();
          if (lastUpdatedPicker != null)
               lastUpdatedPicker.setValue(null);
          applyFilter();
     }

     @FXML
     private void OnClickExportButton(javafx.event.ActionEvent event) {
          try {
               if (filteredData == null || filteredData.isEmpty()) {
                    showWarning("Không có dữ liệu để xuất!");
                    return;
               }

               // Prepare headers
               java.util.List<String> headers = java.util.Arrays.asList(
                    "ID", "SKU", "Tên sản phẩm", "Danh mục", "Số lượng", 
                    "Đơn vị", "Giá bán", "Trạng thái", "Cập nhật lần cuối"
               );

               // Prepare data
               java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>();
               for (Product product : filteredData) {
                    java.util.List<Object> row = java.util.Arrays.asList(
                         product.getId(),
                         product.getSku(),
                         product.getName(),
                         product.getCategoryEnum() != null ? product.getCategoryEnum().getDisplayName() : "",
                         product.getQtyOnHand(),
                         product.getUnit(),
                         product.getPriceRetail(),
                         product.isActive() ? "Hoạt động" : "Ngừng",
                         product.getCreatedAt() != null ? product.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
                    );
                    data.add(row);
               }

               // Generate filename and path
               String directory = org.example.oop.Utils.ExcelExporter.getDocumentsPath();
               org.example.oop.Utils.ExcelExporter.ensureDirectoryExists(directory);
               String fileName = org.example.oop.Utils.ExcelExporter.generateFileName("TimKiemKho");
               String fullPath = directory + fileName;

               // Export to Excel
               org.example.oop.Utils.ExcelExporter.exportToFile(fullPath, "Kết quả tìm kiếm", headers, data);

               showSuccess("Đã xuất kết quả tìm kiếm ra file:\n" + fileName + "\n\nVị trí: " + fullPath);
               
               if (messageLabel != null) {
                    messageLabel.setText("✅ Đã xuất " + filteredData.size() + " sản phẩm");
               }

          } catch (Exception e) {
               e.printStackTrace();
               showError("Lỗi xuất file: " + e.getMessage());
          }
     }

     // ==================== FILTER LOGIC ====================

     private void applyFilter() {
          String keyword = normalizeText(searchTextField != null ? searchTextField.getText() : "");

          String categorySelected = categoryBox != null ? categoryBox.getValue() : "All Categories";
          boolean allCategories = categorySelected == null || categorySelected.equals("All Categories");

          String statusSelected = statusBox != null ? statusBox.getValue() : "All Status";
          boolean allStatuses = statusSelected == null || statusSelected.equals("All Status");

          LocalDate dateSelected = lastUpdatedPicker != null ? lastUpdatedPicker.getValue() : null;

          filteredData.setPredicate(product -> {
               if (product == null)
                    return false;

               // Filter by name keyword
               String name = normalizeText(product.getName() != null ? product.getName() : "");
               String sku = normalizeText(product.getSku() != null ? product.getSku() : "");
               boolean matchKeyword = keyword.isEmpty() || name.contains(keyword) || sku.contains(keyword);

               // Filter by category
               boolean matchCategory = allCategories ||
                         normalizeText(String.valueOf(product.getCategory())).equals(normalizeText(categorySelected));

               // Filter by status
               boolean matchStatus = allStatuses ||
                         (statusSelected.equals("Active") && product.isActive()) ||
                         (statusSelected.equals("Inactive") && !product.isActive());

               // Filter by date
               boolean matchDate = dateSelected == null ||
                         (product.getCreatedAt() != null &&
                                   product.getCreatedAt().toLocalDate().equals(dateSelected));

               return matchKeyword && matchCategory && matchStatus && matchDate;
          });

          updateCountLabel();
     }

     private String normalizeText(String text) {
          if (text == null)
               return "";
          String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
          return normalized.replaceAll("\\p{M}+", "").toLowerCase().trim();
     }

     private void updateDetail(Product product) {
          if (product == null) {
               clearDetail();
               return;
          }
          runOnUIThread(() -> {

               if (quickBatchNumber != null) {
                    quickBatchNumber.setText(product.getBatchNo() != null ? product.getBatchNo() : "");
               }
               if (quickExpiryDate != null) {
                    quickExpiryDate.setText(product.getFormattedExpiryDate());
               }
               if (quickserialNo != null) {
                    quickserialNo.setText(product.getSerialNo() != null ? product.getSerialNo() : "");
               }
               if (quickCreatedAt != null) {
                    if (product.getCreatedAt() != null) {
                         quickCreatedAt.setText(
                                   product.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    } else {
                         quickCreatedAt.setText("");
                    }
               }
               if (itemName != null) {
                    itemName.setText(product.getName());
               }
               if (itemDescription != null) {
                    itemDescription.setText(product.getNote() != null ? product.getNote() : "Không có mô tả");
               }

               if (quickSku != null)
                    quickSku.setText(product.getSku());
               if (quickStatus != null)
                    quickStatus.setText(product.isActive() ? "Active" : "Inactive");
               if (quickCategory != null)
                    quickCategory.setText(product.getCategory() != null ? product.getCategory() : "");
               if (quickUnit != null)
                    quickUnit.setText(product.getUnit());
               if (quickQuantity != null)
                    quickQuantity.setText(String.valueOf(product.getQtyOnHand()));

               if (quickPrice != null) {
                    quickPrice.setText(
                              product.getPriceRetail() != null ? String.format("%,d VND", product.getPriceRetail())
                                        : "0 VND");
               }
               if (quickCostPrice != null) {
                    quickCostPrice
                              .setText(product.getPriceCost() != null ? String.format("%,d VND", product.getPriceCost())
                                        : "0 VND");
               }
          });
     }

     private void clearDetail() {
          runOnUIThread(() -> {
               if (quickBatchNumber != null) {
                    quickBatchNumber.setText("");
               }
               if (quickExpiryDate != null) {
                    quickExpiryDate.setText("");
               }
               if (quickserialNo != null) {
                    quickserialNo.setText("");
               }
               if (quickCreatedAt != null) {
                    quickCreatedAt.setText("");
               }
               if (itemName != null)
                    itemName.setText("");
               if (itemDescription != null)
                    itemDescription.setText("");
               if (quickSku != null)
                    quickSku.setText("");
               if (quickStatus != null)
                    quickStatus.setText("");
               if (quickCategory != null)
                    quickCategory.setText("");
               if (quickUnit != null)
                    quickUnit.setText("");
               if (quickQuantity != null)
                    quickQuantity.setText("");
               if (quickPrice != null)
                    quickPrice.setText("");
               if (quickCostPrice != null)
                    quickCostPrice.setText("");
          });
     }

     // ==================== UI HELPERS ====================

     private void showLoading(boolean show) {
          runOnUIThread(() -> {
               if (loadingIndicator != null) {
                    loadingIndicator.setVisible(show);
                    loadingIndicator.setManaged(show);
               }
          });
     }

     private void updateMessage(String message) {
          runOnUIThread(() -> {
               if (messageLabel != null) {
                    messageLabel.setText(message);
               }
               System.out.println("📝 Message: " + message);
          });
     }

     private void updateCountLabel() {
          runOnUIThread(() -> {
               if (countLabel != null) {
                    int count = filteredData.size();
                    countLabel.setText(count + " sản phẩm");

                    if (messageLabel != null) {
                         if (count == 0) {
                              messageLabel.setText("Không tìm thấy sản phẩm nào");
                         } else {
                              messageLabel.setText("Tìm thấy " + count + " sản phẩm");
                         }
                    }
               }
          });
     }

     // ==================== LOADING STATUS HELPERS ====================

     private void showLoadingStatus(String message) {
          runOnUIThread(() -> {
               if (loadingStatusContainer != null) {
                    loadingStatusContainer.setVisible(true);
                    loadingStatusContainer.setStyle(
                              "-fx-padding: 5px 15px; -fx-background-color: rgba(14,165,233,0.1); -fx-background-radius: 8px; -fx-border-color: #0EA5E9; -fx-border-width: 1px; -fx-border-radius: 8px;");
               }
               if (statusProgressIndicator != null) {
                    statusProgressIndicator.setVisible(true);
               }
               if (loadingStatusLabel != null) {
                    loadingStatusLabel.setText(message);
                    loadingStatusLabel.setStyle("-fx-text-fill: #0EA5E9; -fx-font-weight: 600; -fx-font-size: 13px;");
               }
          });
     }

     private void showSuccessStatus(String message) {
          runOnUIThread(() -> {
               if (statusProgressIndicator != null) {
                    statusProgressIndicator.setVisible(false);
               }
               if (loadingStatusLabel != null) {
                    loadingStatusLabel.setText(message);
                    loadingStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700; -fx-font-size: 13px;");
               }
               if (loadingStatusContainer != null) {
                    loadingStatusContainer.setStyle(
                              "-fx-padding: 5px 15px; -fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 8px; -fx-border-color: #10B981; -fx-border-width: 1px; -fx-border-radius: 8px;");
               }

               // Auto-hide after 2 seconds
               new Thread(() -> {
                    try {
                         Thread.sleep(2000);
                         hideLoadingStatus();
                    } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                    }
               }).start();
          });
     }

     private void showErrorStatus(String message) {
          runOnUIThread(() -> {
               if (statusProgressIndicator != null) {
                    statusProgressIndicator.setVisible(false);
               }
               if (loadingStatusLabel != null) {
                    loadingStatusLabel.setText(message);
                    loadingStatusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700; -fx-font-size: 13px;");
               }
               if (loadingStatusContainer != null) {
                    loadingStatusContainer.setStyle(
                              "-fx-padding: 5px 15px; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8px; -fx-border-color: #EF4444; -fx-border-width: 1px; -fx-border-radius: 8px;");
               }

               // Auto-hide after 3 seconds
               new Thread(() -> {
                    try {
                         Thread.sleep(3000);
                         hideLoadingStatus();
                    } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                    }
               }).start();
          });
     }

     private void hideLoadingStatus() {
          runOnUIThread(() -> {
               if (loadingStatusContainer != null) {
                    loadingStatusContainer.setVisible(false);
               }
          });
     }
}
