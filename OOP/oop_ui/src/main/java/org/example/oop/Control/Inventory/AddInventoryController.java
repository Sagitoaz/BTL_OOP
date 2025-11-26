package org.example.oop.Control.Inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.oop.Control.BaseController;
import org.miniboot.app.domain.models.Inventory.Product;
import org.example.oop.Service.ApiProductService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller cho màn hình Add Inventory (Add New Product)
 * Extends BaseController để sử dụng executeAsync và các utilities
 */
public class AddInventoryController extends BaseController {

     //  FXML FIELDS: Identity 
     @FXML
     private TextField tfSku;
     @FXML
     private TextField tfName;
     @FXML
     private ComboBox<String> cbCategory;
     @FXML
     private TextField tfUnit;

     //  FXML FIELDS: Pricing 
     @FXML
     private TextField tfPriceCost;
     @FXML
     private TextField tfPriceRetail;

     //  FXML FIELDS: Stock 
     @FXML
     private TextField tfQtyOnHand;

     //  FXML FIELDS: Batch/Expiry/Serial 
     @FXML
     private TextField tfBatchNo;
     @FXML
     private DatePicker dpExpiryDate;
     @FXML
     private TextField tfSerialNo;

     //  FXML FIELDS: Note 
     @FXML
     private TextArea taNote;

     //  FXML FIELDS: Status & Meta 
     @FXML
     private CheckBox chkActive;
     @FXML
     private TextField tfId;
     @FXML
     private TextField tfCreatedAt;

     //  FXML FIELDS: Action Buttons 
     @FXML
     private Button btnResetProduct;
     @FXML
     private Button btnClose;
     @FXML
     private Button btnSaveProduct;
     @FXML
     private Label lblStatus;

     //  SERVICES 
     private final ApiProductService productService = new ApiProductService();

     //  INITIALIZE 
     @FXML
     public void initialize() {
          setupCategoryComboBox();
          setupButtonActions();
          resetForm();
          updateStatus("📝 Sẵn sàng thêm sản phẩm mới");
     }

     /**
      * Thiết lập ComboBox cho Category với các giá trị từ Database
      */
     private void setupCategoryComboBox() {
          cbCategory.setItems(FXCollections.observableArrayList(
                    "frame", // Gọng kính
                    "lens", // Tròng kính
                    "contact_lens", // Kính áp tròng
                    "machine", // Máy móc
                    "consumable", // Vật tư tiêu hao
                    "service" // Dịch vụ
          ));

          // Set default value
          cbCategory.getSelectionModel().selectFirst();
     }

     /**
      * Gắn các action cho buttons
      */
     private void setupButtonActions() {
          btnSaveProduct.setOnAction(e -> onSaveProduct());
          btnResetProduct.setOnAction(e -> onResetProduct());
          btnClose.setOnAction(e -> onClose());
     }

     //  BUTTON HANDLERS 

     /**
      * Xử lý khi nhấn nút "Save Product"
      * Sử dụng BaseController.executeAsync để chạy API call trong background
      */
     @FXML
     private void onSaveProduct() {
          // Validate form trước khi save
          if (!validateForm()) {
               return; // showError đã được gọi trong validateForm()
          }

          // Build product object từ form data
          Product newProduct = buildProductFromForm();

          // Disable buttons và show loading
          disableButtons(true);
          updateStatus("🔄 Đang lưu sản phẩm...");

          // Sử dụng BaseController.executeAsync để chạy API call
          executeAsync(
                    // Background task: Gọi API POST /products
                    () -> {
                         try {
                              return productService.createProduct(newProduct);
                         } catch (Exception e) {
                              throw new RuntimeException("Không thể tạo sản phẩm: " + e.getMessage(), e);
                         }
                    },

                    // Success callback: Update UI và hiển thị thông báo
                    createdProduct -> {
                         disableButtons(false);

                         // CHECK NULL: Server có thể trả về success nhưng không có body
                         if (createdProduct == null) {
                              updateStatus("✅ Đã lưu sản phẩm thành công (server không trả về dữ liệu)");
                              showSuccess("Sản phẩm đã được tạo thành công!\n\n" +
                                        "SKU: " + newProduct.getSku() + "\n" +
                                        "Tên: " + newProduct.getName() + "\n\n" +
                                        "Lưu ý: Server không trả về ID sản phẩm.");
                              resetForm();
                              return;
                         }

                         updateStatus("✅ Đã lưu sản phẩm: " + createdProduct.getName());

                         // Hiển thị thông báo thành công
                         showSuccess("Sản phẩm đã được tạo thành công!\n\n" +
                                   "ID: " + createdProduct.getId() + "\n" +
                                   "SKU: " + createdProduct.getSku() + "\n" +
                                   "Tên: " + createdProduct.getName());

                         // Reset form để thêm sản phẩm mới
                         resetForm();
                    },

                    // Error callback: Hiển thị lỗi
                    error -> {
                         disableButtons(false);
                         updateStatus("❌ Lỗi: " + error.getMessage());
                         showError("Không thể tạo sản phẩm!\n\n" + error.getMessage());
                    });
     }

     /**
      * Xử lý khi nhấn nút "Reset"
      * Xóa toàn bộ form và đặt về giá trị mặc định
      */
     @FXML
     private void onResetProduct() {
          Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
          confirm.setTitle("Xác nhận");
          confirm.setHeaderText("Reset Form");
          confirm.setContentText("Bạn có chắc muốn xóa toàn bộ dữ liệu đã nhập?");

          confirm.showAndWait().ifPresent(response -> {
               if (response == ButtonType.OK) {
                    resetForm();
                    updateStatus("🔄 Form đã được reset");
               }
          });
     }

     /**
      * Xử lý khi nhấn nút "Close"
      * Đóng cửa sổ hoặc quay lại màn hình trước
      */
     @FXML
     private void onClose() {
          // Check nếu có dữ liệu chưa lưu
          if (hasUnsavedData()) {
               Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
               confirm.setTitle("Xác nhận");
               confirm.setHeaderText("Đóng cửa sổ");
               confirm.setContentText("Bạn có dữ liệu chưa lưu. Bạn có chắc muốn đóng?");

               confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                         closeWindow();
                    }
               });
          } else {
               closeWindow();
          }
     }

     //  FORM VALIDATION 

     /**
      * Validate toàn bộ form trước khi save
      * 
      * @return true nếu form hợp lệ, false nếu có lỗi
      */
     private boolean validateForm() {
          // 1. Validate SKU (bắt buộc)
          if (tfSku.getText() == null || tfSku.getText().trim().isEmpty()) {
               showError("Vui lòng nhập mã SKU!");
               tfSku.requestFocus();
               return false;
          }

          // 2. Validate Name (bắt buộc)
          if (tfName.getText() == null || tfName.getText().trim().isEmpty()) {
               showError("Vui lòng nhập tên sản phẩm!");
               tfName.requestFocus();
               return false;
          }

          // 3. Validate Category (bắt buộc)
          if (cbCategory.getValue() == null) {
               showError("Vui lòng chọn danh mục sản phẩm!");
               cbCategory.requestFocus();
               return false;
          }

          // 4. Validate Unit (bắt buộc)
          if (tfUnit.getText() == null || tfUnit.getText().trim().isEmpty()) {
               showError("Vui lòng nhập đơn vị tính!");
               tfUnit.requestFocus();
               return false;
          }

          // 5. Validate Price Cost (phải là số nguyên)
          if (!tfPriceCost.getText().trim().isEmpty()) {
               try {
                    Integer.parseInt(tfPriceCost.getText().trim());
               } catch (NumberFormatException e) {
                    showError("Giá nhập phải là số nguyên!");
                    tfPriceCost.requestFocus();
                    return false;
               }
          }

          // 6. Validate Price Retail (bắt buộc, phải là số nguyên)
          if (tfPriceRetail.getText() == null || tfPriceRetail.getText().trim().isEmpty()) {
               showError("Vui lòng nhập giá bán lẻ!");
               tfPriceRetail.requestFocus();
               return false;
          }
          try {
               Integer.parseInt(tfPriceRetail.getText().trim());
          } catch (NumberFormatException e) {
               showError("Giá bán lẻ phải là số nguyên!");
               tfPriceRetail.requestFocus();
               return false;
          }

          // 7. Validate Quantity (bắt buộc, phải là số nguyên, >= 0)
          if (tfQtyOnHand.getText() == null || tfQtyOnHand.getText().trim().isEmpty()) {
               showError("Vui lòng nhập số lượng tồn kho!");
               tfQtyOnHand.requestFocus();
               return false;
          }
          try {
               int qty = Integer.parseInt(tfQtyOnHand.getText().trim());
               if (qty < 0) {
                    showError("Số lượng tồn kho không thể âm!");
                    tfQtyOnHand.requestFocus();
                    return false;
               }
          } catch (NumberFormatException e) {
               showError("Số lượng tồn kho phải là số nguyên!");
               tfQtyOnHand.requestFocus();
               return false;
          }

          // 8. Validate Price (bắt buộc phải > 0)
          Integer priceCost = parseIntOrNull(tfPriceCost.getText());
          Integer priceRetail = parseIntOrNull(tfPriceRetail.getText());

          if (priceRetail != null && priceRetail <= 0) {
               showError("Giá bán lẻ phải lớn hơn 0!");
               tfPriceRetail.requestFocus();
               return false;
          }

          if (priceCost != null && priceCost < 0) {
               showError("Giá nhập không thể âm!");
               tfPriceCost.requestFocus();
               return false;
          }

          // 9. Warning: Price Retail nên >= Price Cost
          if (priceCost != null && priceRetail != null && priceRetail < priceCost) {
               Alert warning = new Alert(Alert.AlertType.CONFIRMATION);
               warning.setTitle("Cảnh báo giá");
               warning.setHeaderText("Giá bán thấp hơn giá nhập!");
               warning.setContentText(String.format(
                         "Giá nhập: %,d đ\n" +
                         "Giá bán: %,d đ\n\n" +
                         "Bạn có chắc muốn tiếp tục?",
                         priceCost, priceRetail));

               if (warning.showAndWait().get() != ButtonType.OK) {
                    tfPriceRetail.requestFocus();
                    return false;
               }
          }

          // 10. Validate Expiry Date (không được trong quá khứ)
          if (dpExpiryDate.getValue() != null) {
               if (dpExpiryDate.getValue().isBefore(LocalDate.now())) {
                    showError("Ngày hết hạn không thể là ngày trong quá khứ!");
                    dpExpiryDate.requestFocus();
                    return false;
               }
          }

          return true;
     }

     //  DATA BINDING 

     /**
      * Build Product object từ form data
      * 
      * @return Product object với dữ liệu từ form
      */
     private Product buildProductFromForm() {
          Product product = new Product();

          // Identity
          product.setSku(tfSku.getText().trim());
          product.setName(tfName.getText().trim());
          product.setCategoryCode(cbCategory.getValue()); // Set as String
          product.setUnit(tfUnit.getText().trim());

          // Pricing
          product.setPriceCost(parseIntOrNull(tfPriceCost.getText()));
          product.setPriceRetail(parseIntOrNull(tfPriceRetail.getText()));

          // Stock
          product.setQtyOnHand(parseInt(tfQtyOnHand.getText(), 0));

          // Batch/Expiry/Serial
          product.setBatchNo(tfBatchNo.getText().trim().isEmpty() ? null : tfBatchNo.getText().trim());
          product.setExpiryDate(dpExpiryDate.getValue());
          product.setSerialNo(tfSerialNo.getText().trim().isEmpty() ? null : tfSerialNo.getText().trim());

          // Note
          product.setNote(taNote.getText().trim().isEmpty() ? null : taNote.getText().trim());

          // Status
          product.setActive(chkActive.isSelected());

          // Created At (auto)
          product.setCreatedAt(LocalDateTime.now());

          return product;
     }

     /**
      * Reset form về giá trị mặc định
      */
     private void resetForm() {
          // Identity
          tfSku.clear();
          tfName.clear();
          cbCategory.getSelectionModel().selectFirst(); // Default: frame
          tfUnit.clear();

          // Pricing
          tfPriceCost.clear();
          tfPriceRetail.clear();

          // Stock
          tfQtyOnHand.setText("0"); // Default: 0

          // Batch/Expiry/Serial
          tfBatchNo.clear();
          dpExpiryDate.setValue(null);
          tfSerialNo.clear();

          // Note
          taNote.clear();

          // Status
          chkActive.setSelected(true); // Default: Active

          // Meta (read-only)
          tfId.clear();
          tfCreatedAt.clear();

          // Focus on first field
          tfSku.requestFocus();
     }

     /**
      * Check xem form có dữ liệu chưa lưu không
      * 
      * @return true nếu có dữ liệu chưa lưu
      */
     private boolean hasUnsavedData() {
          return !tfSku.getText().trim().isEmpty() ||
                    !tfName.getText().trim().isEmpty() ||
                    !tfPriceCost.getText().trim().isEmpty() ||
                    !tfPriceRetail.getText().trim().isEmpty() ||
                    !tfQtyOnHand.getText().equals("0");
     }

     //  UI HELPERS 

     /**
      * Disable/Enable tất cả buttons
      */
     private void disableButtons(boolean disable) {
          runOnUIThread(() -> {
               btnSaveProduct.setDisable(disable);
               btnResetProduct.setDisable(disable);
               btnClose.setDisable(disable);
          });
     }

     /**
      * Update status label
      */
     private void updateStatus(String message) {
          runOnUIThread(() -> {
               if (lblStatus != null) {
                    lblStatus.setText(message);
               }
               System.out.println("📝 Status: " + message);
          });
     }

     /**
      * Đóng cửa sổ hiện tại
      */
     private void closeWindow() {
          if (btnClose.getScene() != null && btnClose.getScene().getWindow() != null) {
               btnClose.getScene().getWindow().hide();
          }
     }

     //  UTILITY METHODS 

     /**
      * Parse String to Integer, return null if empty or invalid
      */
     private Integer parseIntOrNull(String value) {
          if (value == null || value.trim().isEmpty()) {
               return null;
          }
          try {
               return Integer.parseInt(value.trim());
          } catch (NumberFormatException e) {
               return null;
          }
     }

     /**
      * Parse String to Integer, return defaultValue if empty or invalid
      */
     private int parseInt(String value, int defaultValue) {
          if (value == null || value.trim().isEmpty()) {
               return defaultValue;
          }
          try {
               return Integer.parseInt(value.trim());
          } catch (NumberFormatException e) {
               return defaultValue;
          }
     }
}
