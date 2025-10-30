package org.example.oop.Control.PaymentControl;

// Import BaseController

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
import org.example.oop.Control.BaseController;
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
 * Controller quản lý giao diện Hóa đơn.
 * (Đã cập nhật để kế thừa BaseController và sử dụng executeAsync)
 */
// Bước 1: Kế thừa từ BaseController
public class InvoiceController extends BaseController implements Initializable {

    // --- Dữ liệu và Repository ---
    private final ObservableList<PaymentItem> invoiceItems = FXCollections.observableArrayList();
    private ApiProductService productService;
    private HttpPaymentService paymentService;
    private HttpPaymentItemService itemService;
    private ApiStockMovementService stockMovementService;
    private HttpPaymentStatusLogService paymentStatusLogService;
    private CustomerRecordService customerService;

    private List<Product> allProducts = new ArrayList<>();  // Lưu tất cả sản phẩm

    // Biến tạm để lưu dữ liệu đang chọn
    private Product currentSelectedProduct;
    private Customer currentSelectedCustomer;

    // --- Các thành phần UI (@FXML) ---
    // (Tất cả các @FXML giữ nguyên)
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
        customerService = CustomerRecordService.getInstance();

        setupTableColumns();
        setupEventListeners();
        setupButtonActions();
        handleNewInvoice();

        // Tải sản phẩm bất đồng bộ (đã refactor)
        loadAllProductsAsync();
    }

    /**
     * HÀM REFACTOR: Dùng executeAsync từ BaseController
     */
    private void loadAllProductsAsync() {
        btnFindProduct.setDisable(true); // Vô hiệu hóa nút trong khi tải

        executeAsync(
                () -> {
                    try {
                        return productService.getAllProducts();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, // Tác vụ nền
                (loadedProducts) -> {
                    // Thành công (chạy trên UI thread)
                    allProducts = loadedProducts;
                    System.out.println("Loaded all products (async): " + allProducts.size());
                    btnFindProduct.setDisable(false); // Kích hoạt lại nút
                },
                (error) -> {
                    // Thất bại (chạy trên UI thread)
                    // Sử dụng showAlert từ BaseController
                    showAlert(Alert.AlertType.ERROR, "Lỗi tải sản phẩm", "Không thể tải danh sách sản phẩm: " + error.getMessage());
                }
        );
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalLine"));
        tableItems.setItems(invoiceItems);
    }

    private void setupEventListeners() {
        invoiceItems.addListener((ListChangeListener<PaymentItem>) c -> updateTotals());
        txtDiscountAmount.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
    }

    private void setupButtonActions() {
        btnNewInvoice.setOnAction(event -> handleNewInvoice());
        btnAddItem.setOnAction(event -> handleAddItem());
        btnRemoveRow.setOnAction(event -> handleRemoveRow());
        btnSaveInvoice.setOnAction(event -> handleSaveInvoice());
        btnPayInvoice.setOnAction(event -> handlePayInvoice());
        btnFindCustomer.setOnAction(event -> handleFindCustomer());
        btnFindProduct.setOnAction(event -> handleFindProduct());
    }

    @FXML
    private void handleNewInvoice() {
        // (Giữ nguyên logic)
        invoiceItems.clear();
        String timestampCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmmss"));
        txtInvoiceCode.setText("HD-" + timestampCode);
        txtCashier.clear();
        clearCustomerFields();
        txtCustomerPhone.clear();
        txtSkuSearch.clear();
        txtProductName.clear();
        txtProductType.clear();
        txtProductPrice.clear();
        txtInvoiceNote.clear();
        txtDiscountAmount.setText("0");
        txtQuantity.setText("1");
        dpInvoiceDate.setValue(LocalDate.now());
        currentSelectedProduct = null;
        currentSelectedCustomer = null;
    }

    /**
     * HÀM REFACTOR: Dùng runOnUIThread từ BaseController (thay vì Platform.runLater)
     */
    @FXML
    private void handleFindCustomer() {
        String phoneNumber = txtCustomerPhone.getText();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Số điện thoại để tìm.");
            return;
        }

        btnFindCustomer.setDisable(true);

        customerService.searchCustomersAsync(phoneNumber, null, null, null,
                (customers) -> {
                    // Dùng runOnUIThread từ BaseController
                    runOnUIThread(() -> {
                        if (customers == null || customers.isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy", "Không có khách hàng nào với SĐT này.");
                            this.currentSelectedCustomer = null;
                            clearCustomerFields();
                        } else {
                            Customer foundCustomer = customers.get(0);
                            this.currentSelectedCustomer = foundCustomer;
                            txtCustomerName.setText(foundCustomer.getFullName());
                            txtCustomerAge.setText(String.valueOf(foundCustomer.getAge()));
                            txtCustomerGender.setText(foundCustomer.getGender() != null ? foundCustomer.getGender().name() : "N/A");
                            txtCustomerAddress.setText(foundCustomer.getAddress());
                        }
                        btnFindCustomer.setDisable(false);
                    });
                },
                (errorMsg) -> {
                    // Dùng runOnUIThread từ BaseController
                    runOnUIThread(() -> {
                        showAlert(Alert.AlertType.ERROR, "Lỗi API", "Lỗi khi tìm khách hàng: " + errorMsg);
                        this.currentSelectedCustomer = null;
                        clearCustomerFields();
                        btnFindCustomer.setDisable(false);
                    });
                }
        );
    }

    private void clearCustomerFields() {
        txtCustomerName.clear();
        txtCustomerAge.clear();
        txtCustomerGender.clear();
        txtCustomerAddress.clear();
    }

    /**
     * HÀM REFACTOR: Dùng executeAsync để tìm kiếm sản phẩm trong danh sách (đã tải)
     */
    @FXML
    private void handleFindProduct() {
        String sku = txtSkuSearch.getText();
        if (sku.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập SKU sản phẩm.");
            return;
        }

        btnFindProduct.setDisable(true);

        executeAsync(
                () -> findProductBySku(sku), // Tác vụ nền
                (product) -> {
                    // Thành công (chạy trên UI thread)
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
                    btnFindProduct.setDisable(false);
                },
                (error) -> {
                    // Thất bại (chạy trên UI thread)
                    showAlert(Alert.AlertType.ERROR, "Lỗi tìm sản phẩm", "Có lỗi xảy ra khi tìm kiếm sản phẩm.");
                    btnFindProduct.setDisable(false);
                }
        );
    }

    // Hàm tìm sản phẩm (giữ nguyên)
    private Product findProductBySku(String sku) {
        for (Product product : allProducts) {
            if (product.getSku() != null && product.getSku().equals(sku)) {
                return product;
            }
        }
        return null;
    }

    @FXML
    private void handleAddItem() {
        // (Giữ nguyên logic)
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
        // (Giữ nguyên logic)
        PaymentItem selectedItem = tableItems.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            invoiceItems.remove(selectedItem);
        } else {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn Dòng", "Vui lòng chọn một mặt hàng trong bảng để xóa.");
        }
    }

    /**
     * HÀM MỚI (HELPER): Chứa logic blocking để lưu hóa đơn.
     * Hàm này sẽ được gọi bởi executeAsync trong một luồng nền.
     */
    private Payment saveInvoiceLogic() throws Exception {
        // Đọc dữ liệu từ UI (an toàn vì đang ở luồng UI KHI GỌI,
        // nhưng chúng ta tạo bản sao để truyền vào luồng nền)
        final List<PaymentItem> itemsToSave = new ArrayList<>(invoiceItems);
        final Payment paymentToSave = createPaymentFromUI();
        final int cashierId = safeParseInt(txtCashier.getText());

        // --- Logic này chạy trong LUỒNG NỀN ---

        // 1. Lưu Payment
        Payment savedPayment = paymentService.create(paymentToSave);
        if (savedPayment == null) {
            throw new Exception("Không thể tạo hóa đơn. Service trả về null.");
        }
        int savedPaymentId = savedPayment.getId();

        // 2. Gán PaymentId và Batch Save Items
        for (PaymentItem item : itemsToSave) {
            item.setPaymentId(savedPaymentId);
        }
        List<PaymentItem> savedItems = itemService.saveAllPaymentItems(itemsToSave);
        if (savedItems == null || savedItems.isEmpty() || savedItems.size() != itemsToSave.size()) {
            // (Tùy chọn: Xóa payment đã tạo nếu bước này lỗi)
            throw new Exception("Lỗi Lưu Chi Tiết: Không thể lưu (batch save) các chi tiết hóa đơn.");
        }

        // 3. Cập nhật Kho (Stock Movements)
        for (PaymentItem item : savedItems) {
            StockMovement movement = new StockMovement();
            movement.setProductId(item.getProductId());
            movement.setQty(item.getQty());
            movement.setMoveType(MoveType.SALE);
            movement.setRefTable("payments");
            movement.setRefId(savedPaymentId);
            movement.setMovedBy(cashierId);
            movement.setMovedAt(LocalDateTime.now());
            movement.setNote("Bán hàng tự động từ HĐ: " + savedPayment.getCode());
            stockMovementService.createStockMovement(movement);
        }

        // 4. TẠO STATUS LOG = UNPAID
        PaymentStatusLog unpaidLog = new PaymentStatusLog();
        unpaidLog.setPaymentId(savedPaymentId);
        unpaidLog.setStatus(PaymentStatus.valueOf("UNPAID"));
        paymentStatusLogService.updatePaymentStatus(unpaidLog);

        return savedPayment; // Trả về payment đã lưu thành công
    }

    /**
     * HÀM REFACTOR: Dùng executeAsync và hàm helper saveInvoiceLogic
     */
    @FXML
    private void handleSaveInvoice() {
        // Kiểm tra dữ liệu cơ bản trên luồng UI
        if (txtInvoiceCode.getText().isEmpty() || invoiceItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Thiếu Thông Tin", "Mã hóa đơn và ít nhất một sản phẩm là bắt buộc.");
            return;
        }

        // Vô hiệu hóa các nút
        btnSaveInvoice.setDisable(true);
        btnPayInvoice.setDisable(true);

        executeAsync(
                () -> {
                    try {
                        return saveInvoiceLogic();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, // Tác vụ nền
                (savedPayment) -> {
                    // Thành công (chạy trên UI thread)
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã lưu hóa đơn " + savedPayment.getCode() + " (Trạng thái: UNPAID).");
                    handleNewInvoice();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                },
                (error) -> {
                    // Thất bại (chạy trên UI thread)
                    showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn", error.getMessage());
                    error.printStackTrace();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                }
        );
    }

    /**
     * HÀM REFACTOR: Dùng executeAsync lồng nhau để xử lý chuỗi tác vụ
     * (Lưu HĐ -> Thành công -> Cập nhật PENDING -> Thành công -> Mở cửa sổ)
     */
    @FXML
    private void handlePayInvoice() {
        // 1. Kiểm tra dữ liệu trên luồng UI
        if (txtInvoiceCode.getText().isEmpty() || invoiceItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Thiếu Thông Tin", "Mã hóa đơn và ít nhất một sản phẩm là bắt buộc.");
            return;
        }

        btnSaveInvoice.setDisable(true);
        btnPayInvoice.setDisable(true);

        // 2. Tác vụ 1: Lưu Hóa Đơn
        executeAsync(
                () -> {
                    try {
                        return saveInvoiceLogic();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, // Tác vụ nền (Step 1)
                (savedPayment) -> {
                    // 3. Thành công Tác vụ 1 (chạy trên UI thread)
                    // Bắt đầu Tác vụ 2: Cập nhật trạng thái PENDING
                    executeAsync(
                            () -> {
                                // Tác vụ nền (Step 2)
                                PaymentStatusLog pendingLog = new PaymentStatusLog();
                                pendingLog.setPaymentId(savedPayment.getId());
                                pendingLog.setStatus(PaymentStatus.PENDING);
                                paymentStatusLogService.updatePaymentStatus(pendingLog);
                                return null; // Không cần trả về gì
                            },
                            (nothing) -> {
                                // 4. Thành công Tác vụ 2 (chạy trên UI thread)
                                // Mở cửa sổ thanh toán
                                try {
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/PaymentFXML/Payment.fxml"));
                                    Scene scene = new Scene(fxmlLoader.load());
                                    PaymentController paymentController = fxmlLoader.getController();
                                    paymentController.initData(String.valueOf(savedPayment.getId()));

                                    Stage paymentStage = new Stage();
                                    paymentStage.setTitle("Thanh toán Hóa đơn: " + savedPayment.getCode());
                                    paymentStage.setScene(scene);
                                    paymentStage.centerOnScreen();
                                    paymentStage.show();

                                    Stage currentStage = (Stage) btnPayInvoice.getScene().getWindow();
                                    currentStage.close();

                                } catch (Exception ex) {
                                    showAlert(Alert.AlertType.ERROR, "Lỗi Mở Cửa Sổ Thanh Toán", "Đã lưu hóa đơn nhưng không thể mở cửa sổ thanh toán: " + ex.getMessage());
                                    ex.printStackTrace();
                                    // Kích hoạt lại nút nếu mở cửa sổ lỗi
                                    btnSaveInvoice.setDisable(false);
                                    btnPayInvoice.setDisable(false);
                                }
                            },
                            (pendingError) -> {
                                // 5. Thất bại Tác vụ 2 (chạy trên UI thread)
                                showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật Trạng Thái", "Đã lưu hóa đơn nhưng không thể cập nhật trạng thái PENDING: " + pendingError.getMessage());
                                btnSaveInvoice.setDisable(false);
                                btnPayInvoice.setDisable(false);
                            }
                    );
                },
                (saveError) -> {
                    // 6. Thất bại Tác vụ 1 (chạy trên UI thread)
                    showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn", saveError.getMessage());
                    saveError.printStackTrace();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                }
        );
    }

    private void updateTotals() {
        // (Giữ nguyên logic)
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

    private Payment createPaymentFromUI() {
        // (Giữ nguyên logic)
        LocalDate localDate = dpInvoiceDate.getValue();
        LocalDateTime issuedAt = (localDate != null) ? localDate.atStartOfDay() : LocalDateTime.now();
        int cashierId = 0;
        try {
            cashierId = Integer.parseInt(txtCashier.getText());
        } catch (NumberFormatException ignored) {
        }
        int customerId = (currentSelectedCustomer != null) ? currentSelectedCustomer.getId() : 0;

        int subtotal = safeParseInt(txtSubtotal.getText());
        int discount = safeParseInt(txtDiscountAmount.getText());
        int tax = safeParseInt(txtTaxAmount.getText());
        int grandTotal = safeParseInt(txtGrandTotal.getText());

        return new Payment(0, txtInvoiceCode.getText(), customerId, cashierId, issuedAt,
                subtotal, discount, tax, 0, grandTotal,
                null, 0, txtInvoiceNote.getText(), LocalDateTime.now());
    }

    private int safeParseInt(String text) {
        // (Giữ nguyên logic)
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}