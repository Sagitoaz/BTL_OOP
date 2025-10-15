//package org.example.oop.Control.PaymentControl;
//
//import javafx.collections.FXCollections;
//import javafx.collections.ListChangeListener;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import org.example.oop.Model.PaymentModel.Payment;
//import org.example.oop.Model.PaymentModel.PaymentItem;
//import org.example.oop.Repository.PaymentItemRepository;
//import org.example.oop.Repository.PaymentRepository;
//
//import java.net.URL;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.ResourceBundle;
//
///**
// * Controller quản lý giao diện Hóa đơn, đã được tái cấu trúc để sử dụng Repository.
// * Chỉ chịu trách nhiệm về logic giao diện.
// */
//public class InvoiceController implements Initializable {
//
//    // --- Các thành phần UI (@FXML) ---
//    @FXML
//    private TextField txtInvoiceCode;
//    @FXML
//    private DatePicker dpInvoiceDate;
//    @FXML
//    private TextField txtCashier;
//    @FXML
//    private TextField txtCustomerName;
//    @FXML
//    private TextField txtCustomerPhone;
//    @FXML
//    private TextField txtSearchProduct;
//    @FXML
//    private TextField txtQuantity;
//    @FXML
//    private Button btnAddItem;
//    @FXML
//    private Button btnRemoveRow;
//    @FXML
//    private Button btnSaveInvoice;
//    @FXML
//    private Button btnNewInvoice;
//    @FXML
//    private Button btnPayInvoice;
//    @FXML
//    private TableView<PaymentItem> tableItems;
//    @FXML
//    private TableColumn<PaymentItem, String> colName;
//    @FXML
//    private TableColumn<PaymentItem, Integer> colQuantity;
//    @FXML
//    private TableColumn<PaymentItem, Integer> colUnitPrice;
//    @FXML
//    private TableColumn<PaymentItem, Integer> colTotal;
//    @FXML
//    private TextArea txtInvoiceNote;
//    @FXML
//    private TextField txtSubtotal;
//    @FXML
//    private TextField txtDiscountAmount;
//    @FXML
//    private TextField txtTaxAmount;
//    @FXML
//    private TextField txtGrandTotal;
//
//    // --- Dữ liệu và Repository ---
//    private final ObservableList<PaymentItem> invoiceItems = FXCollections.observableArrayList();
//    private final PaymentRepository paymentRepository = new PaymentRepository();
//    private final PaymentItemRepository itemRepository = new PaymentItemRepository();
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        setupTableColumns();
//        setupEventListeners();
//        setupButtonActions();
//        handleNewInvoice();
//    }
//
//    /**
//     * Cấu hình các cột của TableView để hiển thị dữ liệu từ PaymentItem.
//     */
//    private void setupTableColumns() {
//        colName.setCellValueFactory(new PropertyValueFactory<>("description"));
//        colQuantity.setCellValueFactory(new PropertyValueFactory<>("qty"));
//        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
//        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalLine"));
//        tableItems.setItems(invoiceItems);
//    }
//
//    /**
//     * Thiết lập các listener để tự động cập nhật giao diện khi dữ liệu thay đổi.
//     */
//    private void setupEventListeners() {
//        invoiceItems.addListener((ListChangeListener<PaymentItem>) c -> updateTotals());
//        txtDiscountAmount.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
//    }
//
//    /**
//     * Gán các phương thức xử lý sự kiện cho từng nút.
//     */
//    private void setupButtonActions() {
//        btnNewInvoice.setOnAction(event -> handleNewInvoice());
//        btnAddItem.setOnAction(event -> handleAddItem());
//        btnRemoveRow.setOnAction(event -> handleRemoveRow());
//        btnSaveInvoice.setOnAction(event -> handleSaveInvoice());
//        btnPayInvoice.setOnAction(event -> handlePayInvoice());
//    }
//
//    @FXML
//    private void handleNewInvoice() {
//        invoiceItems.clear();
//        txtInvoiceCode.clear();
//        txtCashier.clear();
//        txtCustomerName.clear();
//        txtCustomerPhone.clear();
//        txtSearchProduct.clear();
//        txtInvoiceNote.clear();
//        txtDiscountAmount.setText("0");
//        txtQuantity.setText("1");
//        dpInvoiceDate.setValue(LocalDate.now());
//        updateTotals();
//    }
//
//    @FXML
//    private void handleAddItem() {
//        String description = txtSearchProduct.getText();
//        if (description == null || description.trim().isEmpty()) {
//            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng nhập tên sản phẩm.");
//            return;
//        }
//
//        int quantity;
//        try {
//            quantity = Integer.parseInt(txtQuantity.getText());
//            if (quantity <= 0) {
//                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Số lượng phải là một số nguyên dương.");
//                return;
//            }
//        } catch (NumberFormatException e) {
//            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Số lượng không hợp lệ.");
//            return;
//        }
//
//        int unitPrice = getPriceForProduct(description);
//        int totalLine = quantity * unitPrice;
//        PaymentItem newItem = new PaymentItem(null, null, 0, description, quantity, unitPrice, totalLine);
//        invoiceItems.add(newItem);
//        txtSearchProduct.clear();
//        txtQuantity.setText("1");
//        txtSearchProduct.requestFocus();
//    }
//
//    @FXML
//    private void handleRemoveRow() {
//        PaymentItem selectedItem = tableItems.getSelectionModel().getSelectedItem();
//        if (selectedItem != null) {
//            invoiceItems.remove(selectedItem);
//        } else {
//            showAlert(Alert.AlertType.WARNING, "Chưa Chọn Dòng", "Vui lòng chọn một mặt hàng trong bảng để xóa.");
//        }
//    }
//
//    /**
//     * Xử lý lưu hóa đơn. Controller thu thập dữ liệu và giao cho Repository xử lý.
//     */
//    @FXML
//    private void handleSaveInvoice() {
//        if (txtInvoiceCode.getText().isEmpty() || invoiceItems.isEmpty()) {
//            showAlert(Alert.AlertType.ERROR, "Thiếu Thông Tin", "Mã hóa đơn và ít nhất một sản phẩm là bắt buộc.");
//            return;
//        }
//
//        // 1. Tạo đối tượng Payment từ giao diện
//        Payment paymentToSave = createPaymentFromUI();
//
//        // 2. Lưu Payment và lấy ID trả về
//        int savedPaymentId = paymentRepository.save(paymentToSave);
//
//        if (savedPaymentId == -1) {
//            showAlert(Alert.AlertType.ERROR, "Lỗi Lưu Trữ", "Không thể lưu hóa đơn. Vui lòng kiểm tra file log.");
//            return;
//        }
//
//        // 3. Cập nhật paymentId cho các item và lưu chúng
//        for (PaymentItem item : invoiceItems) {
//            item.setPaymentId(savedPaymentId);
//        }
//        itemRepository.saveAll(new ArrayList<>(invoiceItems));
//
//        showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã lưu hóa đơn " + paymentToSave.getCode() + " thành công!");
//        handleNewInvoice();
//    }
//
//    @FXML
//    private void handlePayInvoice() {
//        // TODO: Mở cửa sổ thanh toán và truyền mã hóa đơn sang
//        showAlert(Alert.AlertType.INFORMATION, "Chức Năng Sắp Có", "Chức năng thanh toán sẽ được phát triển.");
//    }
//
//    private void updateTotals() {
//        int subtotal = invoiceItems.stream().mapToInt(PaymentItem::getTotalLine).sum();
//        int discount = 0;
//        try {
//            discount = Integer.parseInt(txtDiscountAmount.getText());
//        } catch (NumberFormatException e) { /* Bỏ qua */ }
//
//        // Thuế VAT 8%
//        int tax = (int) Math.round((subtotal - discount) * 0.08);
//        int grandTotal = subtotal - discount + tax;
//
//        txtSubtotal.setText(String.valueOf(subtotal));
//        txtTaxAmount.setText(String.valueOf(tax));
//        txtGrandTotal.setText(String.valueOf(grandTotal));
//    }
//
//    private Payment createPaymentFromUI() {
//        LocalDate localDate = dpInvoiceDate.getValue();
//        Instant issuedAt = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        int cashierId = 0;
//        try {
//            cashierId = Integer.parseInt(txtCashier.getText());
//        } catch (NumberFormatException ignored) {
//        }
//
//        return new Payment(null, txtInvoiceCode.getText(), null, cashierId, issuedAt,
//                Integer.parseInt(txtSubtotal.getText()), Integer.parseInt(txtDiscountAmount.getText()),
//                Integer.parseInt(txtTaxAmount.getText()), 0, Integer.parseInt(txtGrandTotal.getText()),
//                null, null, txtInvoiceNote.getText(), Instant.now());
//    }
//
//    private int getPriceForProduct(String productName) {
//        switch (productName.toLowerCase().trim()) {
//            case "khám mắt":
//                return 150000;
//            case "cắt kính":
//                return 250000;
//            case "gọng kính titan":
//                return 1200000;
//            case "tròng kính chống tia uv":
//                return 800000;
//            default:
//                return 50000;
//        }
//    }
//
//    private void showAlert(Alert.AlertType type, String title, String content) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }
//}