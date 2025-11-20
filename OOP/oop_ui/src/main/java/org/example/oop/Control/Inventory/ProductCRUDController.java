package org.example.oop.Control.Inventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.oop.Service.ApiProductService;
import org.example.oop.Control.BaseController;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Inventory.Product; // ✅ Import Product model từ mini-boot
import org.miniboot.app.domain.models.Inventory.Enum.Category; // ✅ Import Category enum từ mini-boot
import org.miniboot.app.domain.models.UserRole;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductCRUDController extends BaseController implements javafx.fxml.Initializable {
     // ==================== TABLE COLUMNS ====================
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

     // ==================== FORM FIELDS ====================
     @FXML
     private TextField skuField;
     @FXML
     private TextField nameField;
     @FXML
     private TextField quantityField;
     @FXML
     private TextField unitField;
     @FXML
     private TextField priceCostField;
     @FXML
     private TextField priceRetailField; // ✅ Changed from priceField
     @FXML
     private ComboBox<Category> categoryBox;
     @FXML
     private ComboBox<String> statusBox;
     @FXML
     private TextField batchNoField; // ✅ Added
     @FXML
     private TextField expiryDateField; // ✅ Added (TextField instead of DatePicker)
     @FXML
     private TextField serialNoField; // ✅ Added
     @FXML
     private TextArea noteArea;

     // ==================== FILTER CONTROLS ====================
     @FXML
     private TextField searchField;
     @FXML
     private ComboBox<Category> filterCategoryBox;

     // ==================== BUTTONS ====================
     @FXML
     private Button saveButton;
     @FXML
     private Button deleteButton;
     @FXML
     private Button clearButton;
     @FXML
     private Button clearFilterButton;
     @FXML
     private Button exportButton;
     @FXML
     private Button importButton;
     @FXML
     private Button addNewButton;

     // ==================== LABELS ====================
     @FXML
     private Label statusLabel;
     @FXML
     private Label formTitleLabel;
     @FXML
     private Label recordCountLabel;
     @FXML
     private Label totalValueLabel;
     @FXML
     private Label lowStockLabel;
     @FXML
     private Label lastUpdateLabel;

     // ==================== LOADING STATUS ====================
     @FXML
     private HBox loadingStatusContainer;
     @FXML
     private ProgressIndicator statusProgressIndicator;
     @FXML
     private Label loadingStatusLabel;

     // ==================== DATA & SERVICES ====================

     private final ApiProductService productService = new ApiProductService();
     private final ObservableList<Product> productList = FXCollections.observableArrayList();
     private FilteredList<Product> filteredData;
     private SortedList<Product> sortedData;
     private Product selectedProduct = null;

     @Override
     public void initialize(URL url, ResourceBundle rb) {
          if (SceneManager.getSceneData("role") != UserRole.ADMIN) {
               addNewButton.setDisable(true);
          }
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
               Category category = cellData.getValue().getCategoryEnum(); // ✅ Sử dụng getCategoryEnum()
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
                              updateFormTitle("Chỉnh sửa sản phẩm");
                         } else {
                              updateFormTitle("Thêm sản phẩm mới");
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
          // loadingIndicator removed from FXML - no action needed
     }

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
     // ==================== ASYNC DATA LOADING ====================

     /**
      * Load tất cả products từ API trong background thread
      */
     private void loadProductsAsync() {
          showLoading(true);
          disableButtons(true);
          updateStatus("🔄 Đang tải danh sách sản phẩm...");
          showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                    "⏳ Đang tải dữ liệu sản phẩm...");

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
                         showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                   "✅ Tải thành công " + loadedProducts.size() + " sản phẩm!");
                         updateStatistics(loadedProducts);
                         System.out.println("✅ Loaded " + loadedProducts.size() + " products from API");
                    },

                    // On error: Show error (chạy trên UI thread)
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi tải dữ liệu: " + error.getMessage());
                         showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                   "❌ Lỗi: " + error.getMessage());
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

                    // Error - with detailed parsing
                    error -> {
                         showLoading(false);
                         disableButtons(false);

                         ErrorInfo errorInfo = parseError(error);

                         // Display user-friendly message based on error code
                         String title;
                         String message;

                         switch (errorInfo.statusCode) {
                              case 401: // Unauthorized
                                   title = "❌ Chưa xác thực";
                                   message = "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Đăng nhập lại\n" +
                                             "- Kiểm tra token xác thực\n" +
                                             "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                   break;

                              case 403: // Forbidden
                                   title = "❌ Không có quyền truy cập";
                                   message = "Bạn không có quyền thực hiện thao tác này.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Liên hệ quản trị viên để được cấp quyền\n" +
                                             "- Kiểm tra lại vai trò của bạn trong hệ thống";
                                   break;

                              case 415: // Unsupported Media Type
                                   title = "❌ Định dạng không được hỗ trợ";
                                   message = "Dữ liệu gửi lên không đúng định dạng yêu cầu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Đây là lỗi kỹ thuật. Vui lòng liên hệ IT.";
                                   break;

                              case 429: // Too Many Requests
                                   title = "❌ Quá nhiều yêu cầu";
                                   message = "Bạn đã gửi quá nhiều yêu cầu trong thời gian ngắn.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Chờ 1 phút trước khi thử lại\n" +
                                             "- Tránh spam các thao tác";
                                   break;

                              case 409: // Conflict
                                   title = "❌ Dữ liệu bị trùng lặp";
                                   message = "SKU đã tồn tại trong hệ thống.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng sử dụng SKU khác hoặc cập nhật sản phẩm hiện có.";
                                   break;

                              case 422: // Validation Failed
                                   title = "❌ Dữ liệu không hợp lệ";
                                   message = "Dữ liệu vi phạm quy tắc nghiệp vụ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra:\n" +
                                             "- Số lượng phải >= 0\n" +
                                             "- Giá cost và retail phải >= 0\n" +
                                             "- Giá retail nên >= giá cost";
                                   break;

                              case 400: // Bad Request
                                   title = "❌ Yêu cầu không hợp lệ";
                                   message = "Dữ liệu gửi lên không đúng định dạng.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra tất cả các trường bắt buộc.";
                                   break;

                              case 404: // Not Found
                                   title = "❌ Sản phẩm không tồn tại";
                                   message = "Sản phẩm không tồn tại.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra lại thông tin sản phẩm.";
                                   break;

                              case 503: // Service Unavailable
                                   title = "❌ Máy chủ không khả dụng";
                                   message = "Không thể kết nối đến cơ sở dữ liệu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Kiểm tra kết nối mạng\n" +
                                             "- Thử lại sau 1-2 phút\n" +
                                             "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                   break;

                              case 504: // Gateway Timeout
                                   title = "❌ Hết thời gian chờ";
                                   message = "Máy chủ xử lý quá lâu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Thử lại ngay\n" +
                                             "- Kiểm tra tốc độ mạng\n" +
                                             "- Liên hệ IT nếu lỗi lặp lại";
                                   break;

                              case 500: // Internal Server Error
                                   title = "❌ Lỗi máy chủ";
                                   message = "Đã xảy ra lỗi không mong muốn trên máy chủ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng liên hệ quản trị viên.";
                                   break;

                              default:
                                   title = "❌ Lỗi không xác định";
                                   message = "Không thể tạo sản phẩm.\n\n" +
                                             "Mã lỗi: " + errorInfo.statusCode + "\n" +
                                             "Chi tiết: " + errorInfo.message;
                         }

                         updateStatus("❌ " + errorInfo.errorCode + ": " + errorInfo.message);
                         showError(title + "\n\n" + message);
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

                    // Error - with detailed parsing
                    error -> {
                         showLoading(false);
                         disableButtons(false);

                         ErrorInfo errorInfo = parseError(error);

                         // Display user-friendly message based on error code
                         String title;
                         String message;

                         switch (errorInfo.statusCode) {
                              case 409: // Conflict
                                   title = "❌ Dữ liệu bị trùng lặp";
                                   message = "SKU đã tồn tại trong hệ thống.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng sử dụng SKU khác hoặc cập nhật sản phẩm hiện có.";
                                   break;

                              case 422: // Validation Failed
                                   title = "❌ Dữ liệu không hợp lệ";
                                   message = "Dữ liệu vi phạm quy tắc nghiệp vụ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra:\n" +
                                             "- Số lượng phải >= 0\n" +
                                             "- Giá cost và retail phải >= 0\n" +
                                             "- Giá retail nên >= giá cost";
                                   break;

                              case 400: // Bad Request
                                   title = "❌ Yêu cầu không hợp lệ";
                                   message = "Dữ liệu gửi lên không đúng định dạng.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra tất cả các trường bắt buộc.";
                                   break;

                              case 503: // Service Unavailable
                                   title = "❌ Máy chủ không khả dụng";
                                   message = "Không thể kết nối đến cơ sở dữ liệu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Kiểm tra kết nối mạng\n" +
                                             "- Thử lại sau 1-2 phút\n" +
                                             "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                   break;

                              case 504: // Gateway Timeout
                                   title = "❌ Hết thời gian chờ";
                                   message = "Máy chủ xử lý quá lâu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Thử lại ngay\n" +
                                             "- Kiểm tra tốc độ mạng\n" +
                                             "- Liên hệ IT nếu lỗi lặp lại";
                                   break;

                              case 500: // Internal Server Error
                                   title = "❌ Lỗi máy chủ";
                                   message = "Đã xảy ra lỗi không mong muốn trên máy chủ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng liên hệ quản trị viên.";
                                   break;

                              case 404: // Not Found
                                   title = "❌ Sản phẩm không tồn tại";
                                   message = "Sản phẩm không tồn tại .\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra lại thông tin sản phẩm.";
                                   break;

                              default:
                                   title = "❌ Lỗi không xác định";
                                   message = "Không thể tạo sản phẩm.\n\n" +
                                             "Mã lỗi: " + errorInfo.statusCode + "\n" +
                                             "Chi tiết: " + errorInfo.message;
                         }

                         updateStatus("❌ " + errorInfo.errorCode + ": " + errorInfo.message);
                         showError(title + "\n\n" + message);
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

                    // Error - with detailed parsing
                    error -> {
                         showLoading(false);
                         disableButtons(false);

                         ErrorInfo errorInfo = parseError(error);

                         // Display user-friendly message based on error code
                         String title;
                         String message;

                         switch (errorInfo.statusCode) {
                              case 401: // Unauthorized
                                   title = "❌ Chưa xác thực";
                                   message = "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Đăng nhập lại\n" +
                                             "- Kiểm tra token xác thực\n" +
                                             "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                   break;

                              case 403: // Forbidden
                                   title = "❌ Không có quyền truy cập";
                                   message = "Bạn không có quyền thực hiện thao tác này.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Liên hệ quản trị viên để được cấp quyền\n" +
                                             "- Kiểm tra lại vai trò của bạn trong hệ thống";
                                   break;

                              case 415: // Unsupported Media Type
                                   title = "❌ Định dạng không được hỗ trợ";
                                   message = "Dữ liệu gửi lên không đúng định dạng yêu cầu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Đây là lỗi kỹ thuật. Vui lòng liên hệ IT.";
                                   break;

                              case 429: // Too Many Requests
                                   title = "❌ Quá nhiều yêu cầu";
                                   message = "Bạn đã gửi quá nhiều yêu cầu trong thời gian ngắn.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Chờ 1 phút trước khi thử lại\n" +
                                             "- Tránh spam các thao tác";
                                   break;

                              case 409: // Conflict
                                   title = "❌ Dữ liệu bị trùng lặp";
                                   message = "SKU đã tồn tại trong hệ thống.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng sử dụng SKU khác hoặc cập nhật sản phẩm hiện có.";
                                   break;

                              case 422: // Validation Failed
                                   title = "❌ Dữ liệu không hợp lệ";
                                   message = "Dữ liệu vi phạm quy tắc nghiệp vụ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra:\n" +
                                             "- Số lượng phải >= 0\n" +
                                             "- Giá cost và retail phải >= 0\n" +
                                             "- Giá retail nên >= giá cost";
                                   break;

                              case 400: // Bad Request
                                   title = "❌ Yêu cầu không hợp lệ";
                                   message = "Dữ liệu gửi lên không đúng định dạng.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra tất cả các trường bắt buộc.";
                                   break;

                              case 404: // Not Found
                                   title = "❌ Sản phẩm không tồn tại";
                                   message = "Sản phẩm không tồn tại.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng kiểm tra lại thông tin sản phẩm.";
                                   break;

                              case 503: // Service Unavailable
                                   title = "❌ Máy chủ không khả dụng";
                                   message = "Không thể kết nối đến cơ sở dữ liệu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Kiểm tra kết nối mạng\n" +
                                             "- Thử lại sau 1-2 phút\n" +
                                             "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                   break;

                              case 504: // Gateway Timeout
                                   title = "❌ Hết thời gian chờ";
                                   message = "Máy chủ xử lý quá lâu.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng:\n" +
                                             "- Thử lại ngay\n" +
                                             "- Kiểm tra tốc độ mạng\n" +
                                             "- Liên hệ IT nếu lỗi lặp lại";
                                   break;

                              case 500: // Internal Server Error
                                   title = "❌ Lỗi máy chủ";
                                   message = "Đã xảy ra lỗi không mong muốn trên máy chủ.\n\n" +
                                             "Chi tiết: " + errorInfo.message + "\n\n" +
                                             "Vui lòng liên hệ quản trị viên.";
                                   break;

                              default:
                                   title = "❌ Lỗi không xác định";
                                   message = "Không thể cập nhật sản phẩm.\n\n" +
                                             "Mã lỗi: " + errorInfo.statusCode + "\n" +
                                             "Chi tiết: " + errorInfo.message;
                         }

                         updateStatus("❌ " + errorInfo.errorCode + ": " + errorInfo.message);
                         showError(title + "\n\n" + message);
                    });
     }

     @FXML
     private void onClear() {
          clearForm();
     }

     @FXML
     private void onAddInventory() {
          SceneManager.openModalWindow(SceneConfig.ADD_INVENTORY_VIEW_FXML, SceneConfig.Titles.ADD_INVENTORY, null);
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
               boolean matchKeyword = keyword.isEmpty() ||
                         normalizeText(product.getName()).contains(keyword) ||
                         normalizeText(product.getSku()).contains(keyword) ||
                         normalizeText(
                                   product.getCategoryEnum() != null ? product.getCategoryEnum().getDisplayName() : "")
                                   .contains(keyword);
               boolean matchCategory = allCategories ||
                         normalizeText(
                                   product.getCategoryEnum() != null ? product.getCategoryEnum().getDisplayName() : "")
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

     private void populateForm(Product product) {
          if (product == null)
               return;

          skuField.setText(product.getSku());
          nameField.setText(product.getName());
          categoryBox.setValue(product.getCategoryEnum());
          quantityField.setText(String.valueOf(product.getQtyOnHand()));
          unitField.setText(product.getUnit());
          priceRetailField.setText(String.valueOf(product.getPriceRetail()));

          if (priceCostField != null) {
               priceCostField.setText(String.valueOf(product.getPriceCost()));
          }
          statusBox.setValue(product.isActive() ? "Hoạt động" : "Ngừng hoạt động");
          if (noteArea != null) {
               noteArea.setText(product.getNote());
          }
          if (batchNoField != null) {
               batchNoField.setText(product.getBatchNo() != null ? product.getBatchNo() : "");
          }
          if (expiryDateField != null) {
               expiryDateField.setText(product.getExpiryDate() != null ? product.getExpiryDate().toString() : "");
          }
          if (serialNoField != null) {
               serialNoField.setText(product.getSerialNo() != null ? product.getSerialNo() : "");
          }
     }

     private void clearForm() {
          skuField.clear();
          nameField.clear();
          categoryBox.getSelectionModel().clearSelection();
          quantityField.clear();
          unitField.clear();
          priceRetailField.clear();
          if (priceCostField != null)
               priceCostField.clear();
          if (noteArea != null)
               noteArea.clear();
          if (batchNoField != null)
               batchNoField.clear();
          if (expiryDateField != null)
               expiryDateField.clear();
          if (serialNoField != null)
               serialNoField.clear();

          statusBox.getSelectionModel().selectFirst(); // Default: "Hoạt động"
          selectedProduct = null;
          productTable.getSelectionModel().clearSelection();

          updateFormTitle("Thêm sản phẩm mới");
     }

     private Product getFormData() {
          Product product = new Product();

          product.setSku(skuField.getText().trim());
          product.setName(nameField.getText().trim());
          product.setCategoryEnum(categoryBox.getValue());
          product.setUnit(unitField.getText().trim());
          product.setQtyOnHand(parseInt(quantityField.getText(), 0));
          product.setPriceRetail(parseInt(priceRetailField.getText(), 0));
          product.setPriceCost(priceCostField != null ? parseInt(priceCostField.getText(), 0) : 0);
          product.setActive(statusBox.getValue().equals("Hoạt động"));
          product.setNote(noteArea != null ? noteArea.getText() : "");
          product.setCreatedAt(java.time.LocalDateTime.now());
          return product;
     }

     private void updateFormToProduct(Product product) {
          int originalId = product.getId();

          product.setSku(skuField.getText().trim());
          product.setName(nameField.getText().trim());
          product.setCategoryEnum(categoryBox.getValue()); // ✅ ENUM Category → String
          product.setUnit(unitField.getText().trim());
          product.setQtyOnHand(parseInt(quantityField.getText(), 0));
          product.setPriceRetail(parseInt(priceRetailField.getText(), 0));
          product.setPriceCost(priceCostField != null ? parseInt(priceCostField.getText(), 0) : 0);
          product.setActive(statusBox.getValue().equals("Hoạt động"));
          product.setNote(noteArea != null ? noteArea.getText() : "");
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
               Integer.parseInt(priceRetailField.getText().trim());
          } catch (NumberFormatException e) {
               showError("Số lượng và giá phải là số nguyên!");
               return false;
          }

          return true;
     }

     // ==================== UI HELPERS ====================

     private void showLoading(boolean show) {
          runOnUIThread(() -> {
               // loadingIndicator removed from FXML
               System.out.println("📝 Loading: " + show);
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
               // refreshButton removed from FXML
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

     /**
      * Update form title label
      */
     private void updateFormTitle(String title) {
          runOnUIThread(() -> {
               if (formTitleLabel != null) {
                    formTitleLabel.setText(title);
               }
          });
     }

     /**
      * Update statistics labels based on loaded products
      */
     private void updateStatistics(java.util.List<Product> products) {
          runOnUIThread(() -> {
               // Record count
               if (recordCountLabel != null) {
                    recordCountLabel.setText("Tổng số: " + products.size() + " sản phẩm");
               }

               // Total value (sum of priceRetail * qtyOnHand)
               if (totalValueLabel != null) {
                    int totalValue = products.stream()
                              .mapToInt(p -> p.getPriceRetail() * p.getQtyOnHand())
                              .sum();
                    totalValueLabel.setText("Tổng giá trị: " + String.format("%,d", totalValue) + " đ");
               }

               // Low stock count (qtyOnHand < 10)
               if (lowStockLabel != null) {
                    long lowStockCount = products.stream()
                              .filter(p -> p.getQtyOnHand() < 10)
                              .count();
                    lowStockLabel.setText("Sắp hết: " + lowStockCount + " sản phẩm");
               }

               // Last update time
               if (lastUpdateLabel != null) {
                    lastUpdateLabel.setText("Cập nhật: " + java.time.LocalDateTime.now()
                              .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
               }
          });
     }

     private int parseInt(String value, int defaultValue) {
          try {
               return Integer.parseInt(value.trim());
          } catch (NumberFormatException e) {
               return defaultValue;
          }
     }

     /**
      * Parse error message from Exception
      * Extract JSON error response if available
      */
     private ErrorInfo parseError(Throwable error) {
          String rawMessage = error.getMessage();
          if (rawMessage == null) {
               return new ErrorInfo(0, "UNKNOWN_ERROR", "Unknown error occurred");
          }

          // Try to extract HTTP status code
          int statusCode = 0;
          if (rawMessage.matches(".*\\b(\\d{3})\\b.*")) {
               java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(\\d{3})\\b");
               java.util.regex.Matcher matcher = pattern.matcher(rawMessage);
               if (matcher.find()) {
                    statusCode = Integer.parseInt(matcher.group(1));
               }
          }

          // Try to extract JSON message
          String errorCode = "ERROR";
          String message = rawMessage;

          try {
               // Check if response contains JSON
               int jsonStart = rawMessage.indexOf("{");
               int jsonEnd = rawMessage.lastIndexOf("}");

               if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String json = rawMessage.substring(jsonStart, jsonEnd + 1);

                    // Simple JSON parsing (without external library)
                    if (json.contains("\"error\":")) {
                         int errorStart = json.indexOf("\"error\":\"") + 9;
                         int errorEnd = json.indexOf("\"", errorStart);
                         if (errorEnd > errorStart) {
                              errorCode = json.substring(errorStart, errorEnd);
                         }
                    }

                    if (json.contains("\"message\":")) {
                         int msgStart = json.indexOf("\"message\":\"") + 11;
                         int msgEnd = json.indexOf("\"", msgStart);
                         if (msgEnd > msgStart) {
                              message = json.substring(msgStart, msgEnd);
                         }
                    }
               }
          } catch (Exception e) {
               // JSON parsing failed, use raw message
               System.err.println("⚠️ Failed to parse error JSON: " + e.getMessage());
          }

          return new ErrorInfo(statusCode, errorCode, message);
     }

     /**
      * Inner class to hold parsed error information
      */
     private static class ErrorInfo {
          final int statusCode;
          final String errorCode;
          final String message;

          ErrorInfo(int statusCode, String errorCode, String message) {
               this.statusCode = statusCode;
               this.errorCode = errorCode;
               this.message = message;
          }
     }
}
