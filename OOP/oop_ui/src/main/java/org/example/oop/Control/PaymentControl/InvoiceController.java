package org.example.oop.Control.PaymentControl;

// Import BaseController

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.oop.Control.BaseController;
import org.example.oop.Service.*;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
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

    //  Dữ liệu và Repository 
    private final ObservableList<PaymentItem> invoiceItems = FXCollections.observableArrayList();
    private ApiProductService productService;
    private HttpPaymentService paymentService;
    private HttpPaymentItemService itemService;
    private ApiStockMovementService stockMovementService;
    private HttpPaymentStatusLogService paymentStatusLogService;
    private CustomerRecordService customerService;

    //  LOADING STATUS 
    @FXML
    private HBox loadingStatusContainer;
    @FXML
    private ProgressIndicator statusProgressIndicator;
    @FXML
    private Label loadingStatusLabel;

    private List<Product> allProducts = new ArrayList<>(); // Lưu tất cả sản phẩm

    // Biến tạm để lưu dữ liệu đang chọn
    private Product currentSelectedProduct;
    private Customer currentSelectedCustomer;

    //  Các thành phần UI (@FXML) 
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
    private Button btnPaymentHistory;
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

    /**
     * Handler để mở màn hình Lịch sử thanh toán
     */
    @FXML
    private void handlePaymentHistory() {
        try {
            System.out.println("🔄 Opening Payment History...");
            SceneManager.switchScene(SceneConfig.PAYMENT_HISTORY_FXML, SceneConfig.Titles.PAYMENT_HISTORY);
        } catch (Exception e) {
            System.err.println("❌ Error opening Payment History: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình Lịch sử thanh toán: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productService = new ApiProductService();
        paymentService = HttpPaymentService.getInstance();
        itemService = HttpPaymentItemService.getInstance();
        stockMovementService = new ApiStockMovementService();
        paymentStatusLogService = HttpPaymentStatusLogService.getInstance();
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
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang tải danh sách sản phẩm...");

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
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Tải thành công " + allProducts.size() + " sản phẩm!");
                },
                (error) -> {
                    // Thất bại (chạy trên UI thread)
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    // Sử dụng showAlert từ BaseController
                    showAlert(Alert.AlertType.ERROR, "Lỗi tải sản phẩm",
                            "Không thể tải danh sách sản phẩm: " + error.getMessage());
                });
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
     * HÀM REFACTOR: Dùng runOnUIThread từ BaseController (thay vì
     * Platform.runLater)
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
                            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy",
                                    "Không có khách hàng nào với SĐT này.");
                            this.currentSelectedCustomer = null;
                            clearCustomerFields();
                        } else {
                            Customer foundCustomer = customers.get(0);
                            this.currentSelectedCustomer = foundCustomer;
                            txtCustomerName.setText(foundCustomer.getFullName());
                            txtCustomerAge.setText(String.valueOf(foundCustomer.getAge()));
                            txtCustomerGender.setText(
                                    foundCustomer.getGender() != null ? foundCustomer.getGender().name() : "N/A");
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
                });
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
                        showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy",
                                "Không tìm thấy sản phẩm với SKU này.");
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
                });
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
     *
     * @param createStatusLog true để tạo UNPAID status, false để bỏ qua
     */
    private Payment saveInvoiceLogic(boolean createStatusLog) throws Exception {
        // Đọc dữ liệu từ UI (an toàn vì đang ở luồng UI KHI GỌI,
        // nhưng chúng ta tạo bản sao để truyền vào luồng nền)
        final List<PaymentItem> itemsToSave = new ArrayList<>(invoiceItems);
        final Payment paymentToSave = createPaymentFromUI();
        final int cashierId = safeParseInt(txtCashier.getText());

        //  Logic này chạy trong LUỒNG NỀN 

        // 1. Lưu Payment với ApiResponse handling
        ApiResponse<Payment> paymentResponse = paymentService.create(paymentToSave);
        if (!paymentResponse.isSuccess()) {
            throw new Exception("Không thể tạo hóa đơn: " + paymentResponse.getErrorMessage());
        }
        Payment savedPayment = paymentResponse.getData();
        if (savedPayment == null || savedPayment.getId() == null) {
            throw new Exception("Không thể tạo hóa đơn. Service trả về null.");
        }
        int savedPaymentId = savedPayment.getId();

        // 2. Gán PaymentId và Batch Save Items với ApiResponse handling
        for (PaymentItem item : itemsToSave) {
            item.setPaymentId(savedPaymentId);
        }
        ApiResponse<List<PaymentItem>> itemsResponse = itemService.saveAllPaymentItems(itemsToSave);
        if (!itemsResponse.isSuccess()) {
            throw new Exception("Lỗi Lưu Chi Tiết: " + itemsResponse.getErrorMessage());
        }
        List<PaymentItem> savedItems = itemsResponse.getData();
        if (savedItems == null || savedItems.isEmpty() || savedItems.size() != itemsToSave.size()) {
            // (Tùy chọn: Xóa payment đã tạo nếu bước này lỗi)
            throw new Exception("Lỗi Lưu Chi Tiết: Không thể lưu (batch save) các chi tiết hóa đơn.");
        }

        // 3. Cập nhật Kho (Stock Movements)
        for (PaymentItem item : savedItems) {
            StockMovement movement = new StockMovement();
            movement.setProductId(item.getProductId());
            movement.setQty(-item.getQty());
            movement.setMoveType(MoveType.SALE);
            movement.setRefTable("payments");
            movement.setRefId(savedPaymentId);
            movement.setMovedBy(cashierId);
            movement.setMovedAt(LocalDateTime.now());
            movement.setNote("Bán hàng tự động từ HĐ: " + savedPayment.getCode());
            stockMovementService.createStockMovement(movement);
        }

        // 4. TẠO STATUS LOG = UNPAID với ApiResponse handling (chỉ khi cần)
        if (createStatusLog) {
            PaymentStatusLog unpaidLog = new PaymentStatusLog();
            unpaidLog.setPaymentId(savedPaymentId);
            unpaidLog.setStatus(PaymentStatus.UNPAID);

            ApiResponse<PaymentStatusLog> statusResponse = paymentStatusLogService.updatePaymentStatus(unpaidLog);
            if (!statusResponse.isSuccess()) {
                throw new Exception("Không thể tạo status log: " + statusResponse.getErrorMessage());
            }
        }

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

        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang lưu hóa đơn...");

        executeAsync(
                () -> {
                    try {
                        return saveInvoiceLogic(true); // Tạo UNPAID status
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, // Tác vụ nền
                (savedPayment) -> {
                    // Thành công (chạy trên UI thread)
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Đã lưu hóa đơn " + savedPayment.getCode());
                    showAlert(Alert.AlertType.INFORMATION, "Thành Công",
                            "Đã lưu hóa đơn " + savedPayment.getCode() + " (Trạng thái: UNPAID).");
                    handleNewInvoice();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                },
                (error) -> {
                    // Thất bại (chạy trên UI thread)
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn", error.getMessage());
                    error.printStackTrace();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                });
    }

    /**
     * HÀM REFACTOR: Dùng executeAsync lồng nhau để xử lý chuỗi tác vụ
     * TỐI ƯU: Bỏ qua UNPAID, chỉ tạo PENDING trực tiếp để giảm 1 request
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

        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang lưu hóa đơn...");

        // 2. Tác vụ 1: Lưu Hóa Đơn (không tạo UNPAID status)
        executeAsync(
                () -> {
                    try {
                        return saveInvoiceLogic(false); // Bỏ qua UNPAID status
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, // Tác vụ nền (Step 1)
                (savedPayment) -> {
                    // 3. Thành công Tác vụ 1 (chạy trên UI thread)
                    showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "⏳ Đang chuẩn bị thanh toán...");

                    // Bắt đầu Tác vụ 2: Cập nhật trạng thái PENDING trực tiếp
                    executeAsync(
                            () -> {
                                // Tác vụ nền (Step 2)
                                PaymentStatusLog pendingLog = new PaymentStatusLog();
                                pendingLog.setPaymentId(savedPayment.getId());
                                pendingLog.setStatus(PaymentStatus.PENDING);

                                ApiResponse<PaymentStatusLog> response = paymentStatusLogService
                                        .updatePaymentStatus(pendingLog);
                                if (!response.isSuccess()) {
                                    throw new RuntimeException(
                                            "Không thể cập nhật status PENDING: " + response.getErrorMessage());
                                }
                                return null; // Không cần trả về gì
                            },
                            (nothing) -> {
                                // 4. Thành công Tác vụ 2 (chạy trên UI thread)
                                showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                        "✅ Mở trang thanh toán...");

                                // Mở cửa sổ thanh toán
                                try {
                                    SceneManager.setSceneData("savedPaymentId", String.valueOf(savedPayment.getId()));
                                    SceneManager.switchScene(SceneConfig.PAYMENT_FXML, SceneConfig.Titles.PAYMENT);

                                } catch (Exception ex) {
                                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                            "❌ Lỗi mở trang thanh toán");
                                    showAlert(Alert.AlertType.ERROR, "Lỗi Mở Cửa Sổ Thanh Toán",
                                            "Đã lưu hóa đơn nhưng không thể mở cửa sổ thanh toán: " + ex.getMessage());
                                    ex.printStackTrace();
                                    // Kích hoạt lại nút nếu mở cửa sổ lỗi
                                    btnSaveInvoice.setDisable(false);
                                    btnPayInvoice.setDisable(false);
                                }
                            },
                            (pendingError) -> {
                                // 5. Thất bại Tác vụ 2 (chạy trên UI thread)
                                showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                        "❌ Lỗi: " + pendingError.getMessage());
                                showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật Trạng Thái",
                                        "Đã lưu hóa đơn nhưng không thể cập nhật trạng thái PENDING: "
                                                + pendingError.getMessage());
                                btnSaveInvoice.setDisable(false);
                                btnPayInvoice.setDisable(false);
                            });
                },
                (saveError) -> {
                    // 6. Thất bại Tác vụ 1 (chạy trên UI thread)
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + saveError.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Hóa Đơn", saveError.getMessage());
                    saveError.printStackTrace();
                    btnSaveInvoice.setDisable(false);
                    btnPayInvoice.setDisable(false);
                });
    }

    private void updateTotals() {
        // (Giữ nguyên logic)
        int subtotal = invoiceItems.stream().mapToInt(PaymentItem::getTotalLine).sum();
        int discount = 0;
        try {
            discount = Integer.parseInt(txtDiscountAmount.getText());
        } catch (NumberFormatException e) {
            /* Bỏ qua */
        }

        int tax = 0;
        int grandTotal = subtotal - discount + tax;
        txtSubtotal.setText(String.valueOf(subtotal));
        txtTaxAmount.setText(String.valueOf(tax));
        txtGrandTotal.setText(String.valueOf(grandTotal));
    }

    private Payment createPaymentFromUI() {
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

        //  Khi tạo invoice (chưa thanh toán): paymentMethod=null, amountPaid=null
        return new Payment(0, txtInvoiceCode.getText(), customerId, cashierId, issuedAt,
                subtotal, discount, tax, 0, grandTotal,
                null, null, txtInvoiceNote.getText(), LocalDateTime.now());
    }

    private int safeParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
