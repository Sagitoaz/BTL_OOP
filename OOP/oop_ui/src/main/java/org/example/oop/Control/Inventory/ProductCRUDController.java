package org.example.oop.Control.Inventory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.oop.Service.ApiProductService;
import org.example.oop.Control.BaseController;
import org.example.oop.Model.Inventory.Product; // ✅ Import Product model
import org.example.oop.Model.Inventory.Enum.Category; // ✅ Import Category enum

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ProductCRUDController extends BaseController implements javafx.fxml.Initializable {
     @FXML
     private TableView<Product> productTable;
     @FXML
     private TableColumn<Product, Integer> idColumn;
     @FXML
     private TableColumn<Product, String> skuColumn;
     @FXML
     private TableColumn<Product, String> nameColumn;
     @FXML
     private TableColumn<Product, String> categoryColumn;
     @FXML
     private TableColumn<Product, Integer> quantityColumn;
     @FXML
     private TableColumn<Product, Integer> priceColumn;
     @FXML
     private TableColumn<Product, String> statusColumn;
     // Form fields
     @FXML
     private TextField skuField;
     @FXML
     private TextField nameField;
     @FXML
     private TextField quantityField;
     @FXML
     private TextField unitField;
     @FXML
     private TextField priceField;
     @FXML
     private TextField priceCostField;
     @FXML
     private ComboBox<Category> categoryBox; // ✅ ENUM type
     @FXML
     private ComboBox<String> statusBox; // ✅ STRING type: "Hoạt động" / "Ngừng hoạt động"
     @FXML
     private TextArea noteArea;

     // Filter controls
     @FXML
     private TextField searchField;
     @FXML
     private ComboBox<Category> filterCategoryBox; // ✅ ENUM type (nullable for "All")

     // Buttons
     @FXML
     private Button saveButton;
     @FXML
     private Button deleteButton;
     @FXML
     private Button clearButton;
     @FXML
     private Button refreshButton;
     @FXML
     private Button clearFilterButton;

     // Status & Loading
     @FXML
     private Label statusLabel;
     @FXML
     private ProgressIndicator loadingIndicator;

     // ==================== DATA & SERVICES ====================

     private final ApiProductService productService = new ApiProductService();
     private final ObservableList<Product> productList = FXCollections.observableArrayList();
     private FilteredList<Product> filteredData;
     private SortedList<Product> sortedData;
     private Product selectedProduct = null;

     @Override
     public void initialize(URL url, ResourceBundle rb) {
          setupTable();
          setupFilters();
          setupFormBoxes();
          setupLoadingIndicator();

          // Load data trong background
          loadProductsAsync();
     }

     private void setupTable() {
          idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
          skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
          nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
          quantityColumn.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
          priceColumn.setCellValueFactory(new PropertyValueFactory<>("priceRetail"));
          categoryColumn.setCellValueFactory(cellData -> {
               Category category = cellData.getValue().getCategory();
               return new javafx.beans.property.SimpleStringProperty(
                         category != null ? category.getDisplayName() : "N/A");
          });
          statusColumn.setCellValueFactory(cellData -> {
               boolean isActive = cellData.getValue().isActive();
               return new javafx.beans.property.SimpleStringProperty(
                         isActive ? "Hoạt động" : "Ngừng hoạt động");
          });
          productTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                         selectedProduct = newSelection;
                         if (newSelection != null) {
                              populateForm(newSelection);
                         }
                    });
          // Setup filter pipeline
          filteredData = new FilteredList<>(productList, p -> true);
          sortedData = new SortedList<>(filteredData);
          sortedData.comparatorProperty().bind(productTable.comparatorProperty());
          productTable.setItems(sortedData);
     }

     private void setupFilters() {
          // Filter on text change
          if (searchField != null) {
               searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
          }

          // Filter on category change
          if (filterCategoryBox != null) {
               filterCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
          }
     }

     private void setupFormBoxes() {
          // ✅ Dùng ENUM cho Category
          categoryBox.setItems(FXCollections.observableArrayList(Category.values()));
          categoryBox.setConverter(new javafx.util.StringConverter<Category>() {
               @Override
               public String toString(Category category) {
                    return category != null ? category.getDisplayName() : "";
               }

               @Override
               public Category fromString(String string) {
                    return Category.values()[0]; // Not used
               }
          });

          // Filter category box (với "All" option)
          ObservableList<Category> filterCategories = FXCollections.observableArrayList();
          filterCategories.add(null); // null = "All Categories"
          filterCategories.addAll(Category.values());
          filterCategoryBox.setItems(filterCategories);
          filterCategoryBox.setConverter(new javafx.util.StringConverter<Category>() {
               @Override
               public String toString(Category category) {
                    return category != null ? category.getDisplayName() : "Tất cả danh mục";
               }

               @Override
               public Category fromString(String string) {
                    return null;
               }
          });
          filterCategoryBox.getSelectionModel().selectFirst();

          // ✅ Dùng STRING cho isActive status
          statusBox.setItems(FXCollections.observableArrayList("Hoạt động", "Ngừng hoạt động"));
          statusBox.getSelectionModel().selectFirst(); // Default: "Hoạt động"
     }

     private void setupLoadingIndicator() {
          if (loadingIndicator != null) {
               loadingIndicator.setVisible(false);
               loadingIndicator.setManaged(false);
          }
     }
     // ==================== ASYNC DATA LOADING ====================

     /**
      * Load tất cả products từ API trong background thread
      */
     private void loadProductsAsync() {
          showLoading(true);
          disableButtons(true);
          updateStatus("🔄 Đang tải danh sách sản phẩm...");

          executeAsync(
                    // Background task: Gọi API
                    () -> {
                         try {
                              return productService.getAllProducts();
                         } catch (Exception e) {
                              throw new RuntimeException(e);
                         }
                    },

                    // On success: Update UI (chạy trên UI thread)
                    loadedProducts -> {
                         productList.clear();
                         productList.addAll(loadedProducts);
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("✅ Đã tải " + loadedProducts.size() + " sản phẩm");
                         System.out.println("✅ Loaded " + loadedProducts.size() + " products from API");
                    },

                    // On error: Show error (chạy trên UI thread)
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi tải dữ liệu: " + error.getMessage());
                         showError("Không thể tải danh sách sản phẩm.\n\n" + error.getMessage());
                    });
     }

     @FXML
     private void onSave() {
          try {
               if (!validateForm()) {
                    showWarning("Vui lòng điền đầy đủ các trường bắt buộc!");
                    return;
               }

               if (selectedProduct == null) {
                    // Create new product
                    createProductAsync();
               } else {
                    // Update existing product
                    updateProductAsync();
               }
          } catch (Exception e) {
               showError("Lỗi validate dữ liệu: " + e.getMessage());
          }
     }

     private void createProductAsync() {
          Product newProduct = getFormData();
          showLoading(true);
          disableButtons(true);
          updateStatus("🔄 Đang tạo sản phẩm mới...");

          executeAsync(
                    // Background: POST request
                    () -> {
                         try {
                              return productService.createProduct(newProduct);
                         } catch (Exception e) {
                              throw new RuntimeException(e);
                         }
                    },

                    // Success: Add to table
                    created -> {
                         if (created != null) {
                              productList.add(created);
                              clearForm();
                              productTable.getSelectionModel().select(created);
                              productTable.scrollTo(created);
                              showSuccess("Đã tạo sản phẩm mới: " + created.getName());
                              updateStatus("✅ Đã tạo sản phẩm ID: " + created.getId());
                         }
                         showLoading(false);
                         disableButtons(false);
                    },

                    // Error
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi tạo sản phẩm: " + error.getMessage());
                         showError("Không thể tạo sản phẩm mới.\n\n" + error.getMessage());
                    });
     }

     private void updateProductAsync() {
          updateFormToProduct(selectedProduct);

          // 🔍 DEBUG: Check if ID is preserved
          System.out.println("🔍 DEBUG: Updating product with ID: " + selectedProduct.getId());
          System.out.println("   Name: " + selectedProduct.getName());
          System.out.println("   SKU: " + selectedProduct.getSku());

          if (selectedProduct.getId() <= 0) {
               showError("Lỗi: Sản phẩm không có ID! Không thể cập nhật.");
               return;
          }

          showLoading(true);
          disableButtons(true);
          updateStatus("🔄 Đang cập nhật sản phẩm...");

          executeAsync(
                    // Background: PUT request
                    () -> {
                         try {
                              return productService.updateProduct(selectedProduct);
                         } catch (Exception e) {
                              throw new RuntimeException(e);
                         }
                    },

                    // Success: Refresh table
                    updated -> {
                         if (updated != null) {
                              // Update in list
                              int index = productList.indexOf(selectedProduct);
                              if (index >= 0) {
                                   productList.set(index, updated);
                              }
                              selectedProduct = updated;
                              productTable.refresh();
                              showSuccess("Đã cập nhật sản phẩm: " + updated.getName());
                              updateStatus("✅ Đã cập nhật sản phẩm ID: " + updated.getId());
                         }
                         showLoading(false);
                         disableButtons(false);
                    },

                    // Error
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi cập nhật: " + error.getMessage());
                         showError("Không thể cập nhật sản phẩm.\n\n" + error.getMessage());
                    });
     }

     @FXML
     private void onDelete() {
          if (selectedProduct == null) {
               showWarning("Vui lòng chọn sản phẩm cần xóa!");
               return;
          }

          Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
          confirm.setTitle("Xác nhận xóa");
          confirm.setHeaderText("Xóa sản phẩm?");
          confirm.setContentText("Bạn có chắc muốn xóa sản phẩm: " + selectedProduct.getName() + "?");

          confirm.showAndWait().ifPresent(response -> {
               if (response == ButtonType.OK) {
                    deleteProductAsync(selectedProduct.getId());
               }
          });
     }

     private void deleteProductAsync(int productId) {
          showLoading(true);
          disableButtons(true);
          updateStatus("🔄 Đang xóa sản phẩm...");

          executeAsync(
                    // Background: DELETE request
                    () -> {
                         try {
                              return productService.deleteProduct(productId);
                         } catch (Exception e) {
                              throw new RuntimeException(e);
                         }
                    },

                    // Success: Remove from table
                    deleted -> {
                         if (deleted) {
                              productList.remove(selectedProduct);
                              clearForm();
                              showSuccess("Đã xóa sản phẩm thành công!");
                              updateStatus("✅ Đã xóa sản phẩm ID: " + productId);
                         }
                         showLoading(false);
                         disableButtons(false);
                    },

                    // Error
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi xóa sản phẩm: " + error.getMessage());
                         showError("Không thể xóa sản phẩm.\n\n" + error.getMessage());
                    });
     }

     @FXML
     private void onClear() {
          clearForm();
     }

     @FXML
     private void onRefresh() {
          loadProductsAsync();
     }

     @FXML
     private void onClearFilter() {
          if (searchField != null)
               searchField.clear();
          if (filterCategoryBox != null)
               filterCategoryBox.getSelectionModel().selectFirst();
          applyFilter();
     }

     @FXML
     private void onExport() {
          // TODO: Implement export functionality
          showWarning("Chức năng Export đang được phát triển!");
     }

     @FXML
     private void onImport() {
          // TODO: Implement import functionality
          showWarning("Chức năng Import đang được phát triển!");
     }

     // ==================== FILTER LOGIC ====================

     private void applyFilter() {
          String keyword = searchField != null ? normalizeText(searchField.getText()) : "";

          String selectedCategory = filterCategoryBox != null && filterCategoryBox.getValue() != null
                    ? filterCategoryBox.getValue().getDisplayName()
                    : "All Categories";

          boolean allCategories = selectedCategory == null ||
                    selectedCategory.equals("All Categories");

          filteredData.setPredicate(product -> {
               if (product == null)
                    return false;

               // Filter by keyword (search in name, SKU, category)
               boolean matchKeyword = keyword.isEmpty() ||
                         normalizeText(product.getName()).contains(keyword) ||
                         normalizeText(product.getSku()).contains(keyword) ||
                         normalizeText(product.getCategory() != null ? product.getCategory().getDisplayName() : "")
                                   .contains(keyword);

               // Filter by category
               boolean matchCategory = allCategories ||
                         normalizeText(product.getCategory() != null ? product.getCategory().getDisplayName() : "")
                                   .equals(normalizeText(selectedCategory));

               return matchKeyword && matchCategory;
          });

          updateStatus("Tìm thấy " + filteredData.size() + " sản phẩm");
     }

     private String normalizeText(String text) {
          if (text == null)
               return "";
          String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
          return normalized.replaceAll("\\p{M}+", "").toLowerCase().trim();
     }

     // ==================== FORM HELPERS ====================

     private void populateForm(Product product) {
          if (product == null)
               return;

          skuField.setText(product.getSku());
          nameField.setText(product.getName());
          categoryBox.setValue(product.getCategory()); // ✅ ENUM Category
          quantityField.setText(String.valueOf(product.getQtyOnHand()));
          unitField.setText(product.getUnit());
          priceField.setText(String.valueOf(product.getPriceRetail()));

          if (priceCostField != null) {
               priceCostField.setText(String.valueOf(product.getPriceCost()));
          }

          // ✅ Set boolean isActive → String "Hoạt động"/"Ngừng hoạt động"
          statusBox.setValue(product.isActive() ? "Hoạt động" : "Ngừng hoạt động");

          if (noteArea != null) {
               noteArea.setText(product.getNote());
          }
     }

     private void clearForm() {
          skuField.clear();
          nameField.clear();
          categoryBox.getSelectionModel().clearSelection();
          quantityField.clear();
          unitField.clear();
          priceField.clear();
          if (priceCostField != null)
               priceCostField.clear();
          if (noteArea != null)
               noteArea.clear();
          statusBox.getSelectionModel().selectFirst(); // Default: "Hoạt động"
          selectedProduct = null;
          productTable.getSelectionModel().clearSelection();
     }

     private Product getFormData() {
          Product product = new Product();

          product.setSku(skuField.getText().trim());
          product.setName(nameField.getText().trim());
          product.setCategory(categoryBox.getValue()); // ✅ ENUM Category
          product.setUnit(unitField.getText().trim());
          product.setQtyOnHand(parseInt(quantityField.getText(), 0));
          product.setPriceRetail(parseInt(priceField.getText(), 0));
          product.setPriceCost(priceCostField != null ? parseInt(priceCostField.getText(), 0) : 0);

          // ✅ Convert String → boolean
          product.setActive(statusBox.getValue().equals("Hoạt động"));

          product.setNote(noteArea != null ? noteArea.getText() : "");
          product.setCreatedAt(java.time.LocalDateTime.now());

          return product;
     }

     private void updateFormToProduct(Product product) {
          // 🔒 CRITICAL: Save ID to prevent it from being lost
          int originalId = product.getId();

          product.setSku(skuField.getText().trim());
          product.setName(nameField.getText().trim());
          product.setCategory(categoryBox.getValue()); // ✅ ENUM Category
          product.setUnit(unitField.getText().trim());
          product.setQtyOnHand(parseInt(quantityField.getText(), 0));
          product.setPriceRetail(parseInt(priceField.getText(), 0));
          product.setPriceCost(priceCostField != null ? parseInt(priceCostField.getText(), 0) : 0);

          // ✅ Convert String → boolean
          product.setActive(statusBox.getValue().equals("Hoạt động"));

          product.setNote(noteArea != null ? noteArea.getText() : "");

          // 🔒 RESTORE ID (critical for update operation)
          product.setId(originalId);

          System.out.println("✅ Updated product data, ID preserved: " + product.getId());
     }

     private boolean validateForm() {
          if (skuField.getText() == null || skuField.getText().trim().isEmpty()) {
               skuField.requestFocus();
               return false;
          }
          if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
               nameField.requestFocus();
               return false;
          }
          if (categoryBox.getValue() == null) {
               categoryBox.requestFocus();
               return false;
          }
          if (unitField.getText() == null || unitField.getText().trim().isEmpty()) {
               unitField.requestFocus();
               return false;
          }

          // Validate numbers
          try {
               Integer.parseInt(quantityField.getText().trim());
               Integer.parseInt(priceField.getText().trim());
          } catch (NumberFormatException e) {
               showError("Số lượng và giá phải là số nguyên!");
               return false;
          }

          return true;
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

     private void disableButtons(boolean disable) {
          runOnUIThread(() -> {
               if (saveButton != null)
                    saveButton.setDisable(disable);
               if (deleteButton != null)
                    deleteButton.setDisable(disable);
               if (clearButton != null)
                    clearButton.setDisable(disable);
               if (refreshButton != null)
                    refreshButton.setDisable(disable);
          });
     }

     private void updateStatus(String message) {
          runOnUIThread(() -> {
               if (statusLabel != null) {
                    statusLabel.setText(message);
               }
               System.out.println("📝 Status: " + message);
          });
     }

     private int parseInt(String value, int defaultValue) {
          try {
               return Integer.parseInt(value.trim());
          } catch (NumberFormatException e) {
               return defaultValue;
          }
     }
}
