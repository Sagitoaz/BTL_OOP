package org.example.oop.Control.PaymentControl;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.oop.Service.*;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Inventory.Enum.MoveType;
import org.miniboot.app.domain.models.Inventory.Product;
import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentItem;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.models.Payment.PaymentStatusLog;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller quản lý giao diện Hóa đơn. (Đã cập nhật)
 */
public class InvoiceController implements Initializable {

    // --- Dữ liệu và Repository ---
    private final ObservableList<PaymentItem> invoiceItems = FXCollections.observableArrayList();
    private ApiProductService productService;
    private HttpPaymentService paymentService;
    private HttpPaymentItemService itemService;
    private ApiStockMovementService stockMovementService;
    private HttpPaymentStatusLogService paymentStatusLogService;
    private CustomerRecordService customerService; // <-- THÊM MỚI SERVICE

    private List<Product> allProducts = new ArrayList<>();  // Lưu tất cả sản phẩm

    // Biến tạm để lưu dữ liệu đang chọn
    private Product currentSelectedProduct;
    private Customer currentSelectedCustomer; // <-- THÊM MỚI BIẾN TẠM

    // --- Các thành phần UI (@FXML) ---
    @FXML
    private TextField txtInvoiceCode;
    @FXML
    private DatePicker dpInvoiceDate;
    @FXML
    private TextField txtCashier;
    @FXML
    private TextField txtCustomerName;
    @FXML
    private TextField txtCustomerPhone;
    @FXML
    private Button btnFindCustomer;
    @FXML
    private TextField txtCustomerAge;
    @FXML
    private TextField txtCustomerGender;
    @FXML
    private TextField txtCustomerAddress;
    @FXML
    private TextField txtSkuSearch;
    @FXML
    private Button btnFindProduct;
    @FXML
    private TextField txtProductName;
    @FXML
    private TextField txtProductType;
    @FXML
    private TextField txtProductPrice;
    @FXML
    private TextField txtQuantity;
    @FXML
    private Button btnAddItem;
    @FXML
    private Button btnRemoveRow;
    @FXML
    private Button btnSaveInvoice;
    @FXML
    private Button btnNewInvoice;
    @FXML
    private Button btnPayInvoice;
    @FXML
    private TableView<PaymentItem> tableItems;
    @FXML
    private TableColumn<PaymentItem, String> colName;
    @FXML
    private TableColumn<PaymentItem, Integer> colQuantity;
    @FXML
    private TableColumn<PaymentItem, Integer> colUnitPrice;
    @FXML
    private TableColumn<PaymentItem, Integer> colTotal;
    @FXML
    private TextArea txtInvoiceNote;
    @FXML
    private TextField txtSubtotal;
    @FXML
    private TextField txtDiscountAmount;
    @FXML
    private TextField txtTaxAmount;
    @FXML
    private TextField txtGrandTotal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productService = new ApiProductService();
        paymentService = new HttpPaymentService();
        itemService = new HttpPaymentItemService();
        stockMovementService = new ApiStockMovementService();
        paymentStatusLogService = new HttpPaymentStatusLogService();
        customerService = CustomerRecordService.getInstance(); // <-- KHỞI TẠO SERVICE

        setupTableColumns();
        setupEventListeners();
        setupButtonActions();
        handleNewInvoice();

        // Gọi API để tải tất cả sản phẩm vào bộ nhớ
        loadAllProducts();
    }

    // Hàm này sẽ gọi API 1 lần để lấy tất cả sản phẩm
    private void loadAllProducts() {
        try {
            // Lấy tất cả sản phẩm từ API
            allProducts = productService.getAllProducts();
            System.out.println("Loaded all products: " + allProducts.size());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải sản phẩm", "Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Cấu hình các cột của TableView để hiển thị dữ liệu từ PaymentItem.
     */
    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalLine"));
        tableItems.setItems(invoiceItems);
    }

    /**
     * Thiết lập các listener để tự động cập nhật giao diện khi dữ liệu thay đổi.
     */
    private void setupEventListeners() {
        invoiceItems.addListener((ListChangeListener<PaymentItem>) c -> updateTotals());
        txtDiscountAmount.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
    }

    /**
     * Gán các phương thức xử lý sự kiện cho từng nút.
     */
    private void setupButtonActions() {
        btnNewInvoice.setOnAction(event -> handleNewInvoice());
        btnAddItem.setOnAction(event -> handleAddItem());
        btnRemoveRow.setOnAction(event -> handleRemoveRow());
        btnSaveInvoice.setOnAction(event -> handleSaveInvoice());
        btnPayInvoice.setOnAction(event -> handlePayInvoice());

        // --- GÁN HÀNH ĐỘNG CHO NÚT MỚI ---
        btnFindCustomer.setOnAction(event -> handleFindCustomer());
        btnFindProduct.setOnAction(event -> handleFindProduct());
    }

    @FXML
    private void handleNewInvoice() {
        invoiceItems.clear();

        // ========================================================
        // TỰ ĐỘNG TẠO MÃ HÓA ĐƠN
        // ========================================================
        String timestampCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        txtInvoiceCode.setText("HD-" + timestampCode);
        // ========================================================

        txtCashier.clear();

        // Xóa thông tin khách hàng
        txtCustomerName.clear();
        txtCustomerPhone.clear();
        txtCustomerAge.clear();
        txtCustomerGender.clear();
        txtCustomerAddress.clear();

        // Xóa thông tin sản phẩm
        txtSkuSearch.clear();
        txtProductName.clear();
        txtProductType.clear();
        txtProductPrice.clear();

        txtInvoiceNote.clear();
        txtDiscountAmount.setText("0");
        txtQuantity.setText("1");
        dpInvoiceDate.setValue(LocalDate.now());

        // Reset biến tạm
        currentSelectedProduct = null;
        currentSelectedCustomer = null; // <-- CẬP NHẬT: RESET KHÁCH HÀNG
    }

    /**
     * HÀM CẬP NHẬT: Xử lý tìm kiếm khách hàng bằng SĐT
     */
    @FXML
    private void handleFindCustomer() {
        String phoneNumber = txtCustomerPhone.getText();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Số điện thoại để tìm.");
            return;
        }

        // Gọi API bất đồng bộ
        // Chúng ta dùng searchKey cho SĐT, các trường khác để null
        customerService.searchCustomersAsync(phoneNumber, null, null, null,
                // 1. Xử lý khi thành công (onSuccess)
                (customers) -> {
                    // Quan trọng: Mọi cập nhật UI phải chạy trên luồng JavaFX
                    Platform.runLater(() -> {
                        if (customers == null || customers.isEmpty()) {
                            // Không tìm thấy
                            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy", "Không có khách hàng nào với SĐT này.");
                            this.currentSelectedCustomer = null;
                            clearCustomerFields(); // Xóa các trường thông tin
                        } else {
                            // Tìm thấy -> Lấy khách hàng đầu tiên
                            Customer foundCustomer = customers.get(0);
                            this.currentSelectedCustomer = foundCustomer;

                            // Điền thông tin vào UI
                            txtCustomerName.setText(foundCustomer.getFullName());
                            txtCustomerAge.setText(String.valueOf(foundCustomer.getAge()));
                            txtCustomerGender.setText(foundCustomer.getGender() != null ? foundCustomer.getGender().name() : "N/A");
                            txtCustomerAddress.setText(foundCustomer.getAddress());
                        }
                    });
                },
                // 2. Xử lý khi có lỗi (onError)
                (errorMsg) -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Lỗi API", "Lỗi khi tìm khách hàng: " + errorMsg);
                        this.currentSelectedCustomer = null;
                        clearCustomerFields();
                    });
                }
        );
    }

    /**
     * Hàm hỗ trợ: Xóa các trường thông tin khách hàng
     */
    private void clearCustomerFields() {
        txtCustomerName.clear();
        txtCustomerAge.clear();
        txtCustomerGender.clear();
        txtCustomerAddress.clear();
        // Ghi chú: Không xóa txtCustomerPhone để người dùng có thể sửa lại
    }

    /**
     * HÀM MỚI: Xử lý tìm kiếm sản phẩm
     */
    @FXML
    private void handleFindProduct() {
        String sku = txtSkuSearch.getText();
        if (sku.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập SKU sản phẩm.");
            return;
        }

        // Tìm sản phẩm trong danh sách đã tải về từ API
        Product product = findProductBySku(sku);

        if (product != null) {
            currentSelectedProduct = product;
            txtProductName.setText(product.getName());
            txtProductType.setText(product.getCategory());
            txtProductPrice.setText(String.valueOf(product.getPriceCost()));
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy", "Không tìm thấy sản phẩm với SKU này.");
            currentSelectedProduct = null;
            txtProductName.clear();
            txtProductType.clear();
            txtProductPrice.clear();
        }
    }

    // Hàm tìm sản phẩm trong danh sách đã tải về từ API
    private Product findProductBySku(String sku) {
        for (Product product : allProducts) {
            if (product.getSku() != null && product.getSku().equals(sku)) {
                return product;
            }
        }
        return null;
    }

    /**
     * HÀM ĐÃ CẬP NHẬT: Chỉ thêm sản phẩm đã tìm
     */
    @FXML
    private void handleAddItem() {
        if (currentSelectedProduct == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Bạn cần nhấn 'Tìm SP' trước khi thêm.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText());
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Số lượng phải lớn hơn 0.");
                return;
            }
            if (quantity > currentSelectedProduct.getQtyOnHand()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Kho không đủ tài nguyên.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Số lượng không hợp lệ.");
            return;
        }

        PaymentItem newItem = new PaymentItem(null,
                currentSelectedProduct.getId(),
                0, // paymentId
                currentSelectedProduct.getName(),
                quantity,
                currentSelectedProduct.getPriceCost(),
                quantity * currentSelectedProduct.getPriceCost());

        invoiceItems.add(newItem);

        // Xóa các trường thông tin sản phẩm
        txtSkuSearch.clear();
        txtProductName.clear();
        txtProductType.clear();
        txtProductPrice.clear();
        txtQuantity.setText("1");
        currentSelectedProduct = null;
        txtSkuSearch.requestFocus();
    }

    @FXML
    private void handleRemoveRow() {
        PaymentItem selectedItem = tableItems.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            invoiceItems.remove(selectedItem);
        } else {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn Dòng", "Vui lòng chọn một mặt hàng trong bảng để xóa.");
        }
    }

    /**
     * HÀM (INTERNAL) ĐÃ CẬP NHẬT:
     * Sử dụng batch-save cho cả PaymentItems VÀ StockMovements
     */
    private Payment saveInvoiceInternal() {
        if (txtInvoiceCode.getText().isEmpty() || invoiceItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Thiếu Thông Tin", "Mã hóa đơn và ít nhất một sản phẩm là bắt buộc.");
            return null;
        }

        Payment paymentToSave = createPaymentFromUI();
        int cashierId = 0;
        try {
            cashierId = Integer.parseInt(txtCashier.getText());
        } catch (NumberFormatException ignored) {
        }

        try {
            // 2. Lưu Payment
            Payment savedPayment = paymentService.create(paymentToSave);
            if (savedPayment == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn", "Không thể tạo hóa đơn. Service trả về null.");
                return null;
            }
            int savedPaymentId = savedPayment.getId();

            // 3. Gán PaymentId và Batch Save Items
            for (PaymentItem item : invoiceItems) {
                item.setPaymentId(savedPaymentId);
            }
            List<PaymentItem> savedItems = itemService.saveAllPaymentItems(invoiceItems);

            // 3c. Kiểm tra kết quả batch save
            if (savedItems == null || savedItems.isEmpty() || savedItems.size() != invoiceItems.size()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Chi Tiết", "Không thể lưu (batch save) các chi tiết hóa đơn.");
                // TODO: Cân nhắc "rollback" (xóa) 'savedPayment' đã tạo ở bước 2
                return null;
            }

            // ========================================================
            // <-- CẬP NHẬT: SỬ DỤNG BATCH SAVE CHO STOCK MOVEMENT
            // ========================================================

            // 4. Tạo danh sách Stock Movements
            List<StockMovement> movementsToSave = new ArrayList<>();

            // Dùng savedItems (danh sách đã được xác nhận từ CSDL)
            for (PaymentItem item : savedItems) {
                StockMovement movement = new StockMovement();
                movement.setProductId(item.getProductId());
                movement.setQty(item.getQty()); // Số lượng bán ra
                movement.setMoveType(MoveType.SALE);
                movement.setRefTable("payments");
                movement.setRefId(savedPaymentId);
                movement.setMovedBy(cashierId);
                movement.setMovedAt(LocalDateTime.now());
                movement.setNote("Bán hàng tự động từ HĐ: " + savedPayment.getCode());
                movementsToSave.add(movement);
            }

            // 4b. Gọi API để lưu toàn bộ danh sách movements một lần
//            List<StockMovement> savedMovements = stockMovementService.createBulkStockMovements(movementsToSave); //
//
//            // 4c. Kiểm tra kết quả
//            if (savedMovements == null || savedMovements.isEmpty() || savedMovements.size() != movementsToSave.size()) {
//                showAlert(Alert.AlertType.ERROR, "Lỗi Ghi Kho", "Không thể lưu (batch save) các cập nhật kho.");
//                // TODO: Rollback
//                return null;
//            }
            // ========================================================
            // KẾT THÚC CẬP NHẬT
            // ========================================================

            // 5. TẠO STATUS LOG = UNPAID
            PaymentStatusLog unpaidLog = new PaymentStatusLog();
            unpaidLog.setPaymentId(savedPaymentId);
            unpaidLog.setStatus(PaymentStatus.valueOf("UNPAID"));
            paymentStatusLogService.updatePaymentStatus(unpaidLog);

            return savedPayment; // Trả về payment đã lưu thành công

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn/Kho/Status", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * HÀM ĐÃ CẬP NHẬT: Chỉ lưu hóa đơn với trạng thái UNPAID
     */
    @FXML
    private void handleSaveInvoice() {
        Payment savedPayment = saveInvoiceInternal();

        if (savedPayment != null) {
            showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã lưu hóa đơn " + savedPayment.getCode() + " (Trạng thái: UNPAID).");
            handleNewInvoice();
        }
    }

    /**
     * HÀM ĐÃ CẬP NHẬT: Lưu hóa đơn VÀ thêm status PENDING
     */
    @FXML
    private void handlePayInvoice() {
        // 1. Thực hiện toàn bộ quy trình lưu (bao gồm cả status UNPAID)
        Payment savedPayment = saveInvoiceInternal();

        // 2. Nếu lưu thành công, "thêm nữa" status PENDING và mở cửa sổ mới
        if (savedPayment != null) {
            try {
                // Thêm trạng thái PENDING
                PaymentStatusLog pendingLog = new PaymentStatusLog();
                pendingLog.setPaymentId(savedPayment.getId());
                pendingLog.setStatus(PaymentStatus.PENDING);
                paymentStatusLogService.updatePaymentStatus(pendingLog);

                // ========================================================
                // Mở cửa sổ thanh toán mới
                // ========================================================

                // 1. Tải FXML
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/PaymentFXML/Payment.fxml"));
                Scene scene = new Scene(fxmlLoader.load());

                // 2. Lấy controller của cửa sổ MỚI
                PaymentController paymentController = fxmlLoader.getController();

                // 3. Truyền ID của hóa đơn vừa tạo sang
                // <-- Gửi ID (dạng String) chứ không gửi Code
                paymentController.initData(String.valueOf(savedPayment.getId()));

                // 4. Tạo và hiển thị cửa sổ (Stage) mới
                Stage paymentStage = new Stage();
                paymentStage.setTitle("Thanh toán Hóa đơn: " + savedPayment.getCode());
                paymentStage.setScene(scene);
                paymentStage.centerOnScreen();
                paymentStage.show();

                // 5. Đóng cửa sổ TẠO HÓA ĐƠN (cửa sổ hiện tại)
                // <-- Lấy Stage hiện tại từ một FXML node bất kỳ, ví dụ btnPayInvoice
                Stage currentStage = (Stage) btnPayInvoice.getScene().getWindow();
                currentStage.close();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Mở Cửa Sổ Thanh Toán", "Đã lưu hóa đơn nhưng không thể mở cửa sổ thanh toán: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateTotals() {
        int subtotal = invoiceItems.stream().mapToInt(PaymentItem::getTotalLine).sum();
        int discount = 0;
        try {
            discount = Integer.parseInt(txtDiscountAmount.getText());
        } catch (NumberFormatException e) { /* Bỏ qua */ }

        int tax = 0;
        int grandTotal = subtotal - discount + tax;
        txtSubtotal.setText(String.valueOf(subtotal));
        txtTaxAmount.setText(String.valueOf(tax));
        txtGrandTotal.setText(String.valueOf(grandTotal));
    }

    /**
     * HÀM ĐÃ CẬP NHẬT: Lấy ID khách hàng từ biến tạm
     */
    private Payment createPaymentFromUI() {
        LocalDate localDate = dpInvoiceDate.getValue();
        LocalDateTime issuedAt = (localDate != null) ? localDate.atStartOfDay() : LocalDateTime.now();
        int cashierId = 0;
        try {
            cashierId = Integer.parseInt(txtCashier.getText());
        } catch (NumberFormatException ignored) {
        }

        // Lấy Customer ID từ biến tạm
        int customerId = (currentSelectedCustomer != null) ? currentSelectedCustomer.getId() : 0; // <-- CẬP NHẬT Ở ĐÂY

        return new Payment(0, txtInvoiceCode.getText(), customerId, cashierId, issuedAt, // <-- TRUYỀN customerId VÀO ĐÂY
                Integer.parseInt(txtSubtotal.getText()), Integer.parseInt(txtDiscountAmount.getText()),
                Integer.parseInt(txtTaxAmount.getText()), 0, Integer.parseInt(txtGrandTotal.getText()),
                null, 0, txtInvoiceNote.getText(), LocalDateTime.now());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}