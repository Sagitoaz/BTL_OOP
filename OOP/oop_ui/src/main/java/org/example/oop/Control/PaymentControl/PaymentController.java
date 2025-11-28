package org.example.oop.Control.PaymentControl;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.oop.Control.BaseController;
import org.example.oop.Model.Receipt;
import org.example.oop.Service.HttpPaymentItemService;
import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Service.HttpPaymentStatusLogService;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Payment.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentController extends BaseController implements Initializable {
    //  LOADING STATUS
    @FXML
    private HBox loadingStatusContainer;
    @FXML
    private ProgressIndicator statusProgressIndicator;
    @FXML
    private Label loadingStatusLabel;

    @FXML
    private TextField txtInvoiceId;
    @FXML
    private TextField txtTotal;
    @FXML
    private TextField txtAmountDue;
    @FXML
    private ComboBox<PaymentMethod> cbMethod;
    @FXML
    private TextField txtAmountPaid;
    @FXML
    private TextField txtChange;
    @FXML
    private TextArea txtNote;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnLoadInvoice;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnConfirmAndPrint;

    private Payment currentPayment;
    private List<PaymentItem> currentItems;

    // Khai báo các service
    private HttpPaymentService paymentService;
    private HttpPaymentStatusLogService statusLogService;
    private HttpPaymentItemService itemService;

    @FXML
    private void handleBackButton() {
        // Clear Invoice cache to ensure fresh load when going back
        SceneManager.removeFromCache(SceneConfig.INVOICE_FXML);
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
        // <-- Khởi tạo các service với getInstance()
        paymentService = HttpPaymentService.getInstance();
        statusLogService = HttpPaymentStatusLogService.getInstance();
        itemService = HttpPaymentItemService.getInstance();

        setupPaymentMethods();
        setupEventHandlers();
        setupListeners();
        handleReset();
        if (SceneManager.getSceneData("savedPaymentId") != null) {
            String paymentId = (String) SceneManager.getSceneData("savedPaymentId");
            initData(paymentId);
            SceneManager.removeSceneData("paymentId");
        }
    }

    public void initData(String paymentId) {
        txtInvoiceId.setText(paymentId);
        handleLoadInvoice(); // Tự động tải hóa đơn

        // Vô hiệu hóa việc sửa mã HĐ vì nó đã được truyền vào
        txtInvoiceId.setEditable(false);
        btnLoadInvoice.setDisable(true);
    }

    private void setupPaymentMethods() {
        cbMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        cbMethod.setValue(PaymentMethod.CASH);
    }

    private void setupEventHandlers() {
        btnLoadInvoice.setOnAction(event -> handleLoadInvoice());
        btnReset.setOnAction(event -> handleReset());
        btnConfirm.setOnAction(event -> handleConfirmPayment(false));
        btnConfirmAndPrint.setOnAction(event -> handleConfirmPayment(true));
    }

    private void setupListeners() {
        txtAmountPaid.textProperty().addListener((obs, old, text) -> {
            if (!text.matches("\\d*")) {
                txtAmountPaid.setText(text.replaceAll("[^\\d]", ""));
            } else {
                calculateChange();
            }
        });

        cbMethod.getSelectionModel().selectedItemProperty().addListener((obs, old, method) -> {
            if (method != null) {
                handlePaymentMethodChange(method);
            }
        });
    }

    private void handleLoadInvoice() {
        String invoiceIdStr = txtInvoiceId.getText().trim();
        if (invoiceIdStr.isEmpty()) {
            showWarning("Vui lòng nhập mã hóa đơn (ID)");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(invoiceIdStr);
        } catch (NumberFormatException e) {
            showError("Mã hóa đơn (ID) không hợp lệ");
            return;
        }

        // Hiển thị loading
        btnLoadInvoice.setDisable(true);
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang tải thông tin hóa đơn...");

        // BỌC CÁC LỆNH GỌI API TRONG executeAsync
        // TỐI ƯU: Giảm từ 3 requests xuống còn 2 requests
        executeAsync(
                // TÁC VỤ NỀN (BACKGROUND THREAD)
                () -> {
                    // 1. Lấy payment + status trong 1 request duy nhất
                    ApiResponse<PaymentWithStatus> paymentWithStatusResponse = paymentService.getPaymentWithStatusById(id);
                    if (!paymentWithStatusResponse.isSuccess()) {
                        throw new RuntimeException("Không thể lấy thông tin hóa đơn: " + paymentWithStatusResponse.getErrorMessage());
                    }

                    PaymentWithStatus paymentWithStatus = paymentWithStatusResponse.getData();
                    if (paymentWithStatus == null) {
                        throw new RuntimeException("Không tìm thấy hóa đơn với ID " + id);
                    }

                    // 2. Lấy payment và status từ object kết hợp
                    Payment payment = paymentWithStatus.getPayment();
                    PaymentStatus status = paymentWithStatus.getStatus();

                    // 3. Kiểm tra trạng thái
                    if (status == PaymentStatus.PAID) {
                        throw new RuntimeException("Hóa đơn này đã được thanh toán trước đó");
                    } else if (status == PaymentStatus.CANCELLED) {
                        throw new RuntimeException("Hóa đơn này đã bị hủy");
                    } else if (status != PaymentStatus.PENDING) {
                        throw new RuntimeException("Hóa đơn này không ở trạng thái chờ thanh toán. Trạng thái hiện tại: " + status);
                    }

                    // 4. Gán biến global
                    currentPayment = payment;

                    // 5. Lấy items
                    ApiResponse<List<PaymentItem>> itemsResponse = itemService.getPaymentItemsByPaymentId(id);
                    if (!itemsResponse.isSuccess()) {
                        throw new RuntimeException("Không thể lấy items: " + itemsResponse.getErrorMessage());
                    }
                    currentItems = itemsResponse.getData();

                    return true;
                },

                //  KHI THÀNH CÔNG (UI THREAD) 
                (success) -> {
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Đã tải hóa đơn " + currentPayment.getCode());

                    // Cập nhật giao diện với dữ liệu đã tải
                    updatePaymentDisplay();
                    btnLoadInvoice.setDisable(false);
                },

                //  KHI CÓ LỖI (UI THREAD) 
                (error) -> {
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());

                    // Nếu là lỗi nghiệp vụ ta tự ném ra (RuntimeException)
                    if (error instanceof RuntimeException) {
                        showWarning(error.getMessage());
                    } else {
                        handleError(error);
                    }
                    handleReset();
                    btnLoadInvoice.setDisable(false);
                }
        );
    }

    private void handleReset() {
        txtInvoiceId.clear();
        txtTotal.clear();
        txtAmountDue.clear();
        txtAmountPaid.clear();
        txtChange.clear();
        txtNote.clear();
        cbMethod.setValue(PaymentMethod.CASH); // Reset về CASH thay vì clear
        currentPayment = null;
        currentItems = null;

        txtInvoiceId.setEditable(true);
        btnLoadInvoice.setDisable(false);
        txtInvoiceId.requestFocus();
    }

    private void handleConfirmPayment(boolean shouldPrint) {
        //  1. VALIDATION (Chạy trên UI thread) 
        if (currentPayment == null) {
            showWarning("Vui lòng tải hóa đơn trước khi thanh toán");
            return;
        }
        if (cbMethod.getValue() == null) {
            showWarning("Vui lòng chọn phương thức thanh toán");
            return;
        }
        if (txtAmountPaid.getText().isEmpty()) {
            showWarning("Vui lòng nhập số tiền thanh toán");
            return;
        }

        Integer amountPaid;
        try {
            amountPaid = Integer.parseInt(txtAmountPaid.getText());
            if (amountPaid < currentPayment.getGrandTotal()) {
                showError("Số tiền thanh toán phải lớn hơn hoặc bằng tổng tiền");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số tiền không hợp lệ");
            return;
        }

        // Cập nhật thông tin vào đối tượng local
        currentPayment.setPaymentMethod(cbMethod.getValue());
        currentPayment.setAmountPaid(amountPaid);
        currentPayment.setNote(txtNote.getText());

        // Vô hiệu hóa các nút
        btnConfirm.setDisable(true);
        btnConfirmAndPrint.setDisable(true);
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang xử lý thanh toán...");

        //  2. GỌI API (Chạy trên Background thread)
        executeAsync(
                //  TÁC VỤ NỀN (BACKGROUND THREAD) 
                () -> {
                    // 1. GỬI CẬP NHẬT LÊN SERVER với ApiResponse handling
                    Payment updatedPayment = null;
                    try {
                        ApiResponse<Payment> updateResponse = paymentService.updatePayment(currentPayment);
                        if (!updateResponse.isSuccess()) {
                            throw new RuntimeException("Không thể cập nhật payment: " + updateResponse.getErrorMessage());
                        }
                        updatedPayment = updateResponse.getData();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (updatedPayment == null) {
                        throw new RuntimeException("Không thể cập nhật thông tin thanh toán. Vui lòng thử lại.");
                    }

                    // 2. Cập nhật trạng thái với ApiResponse handling
                    ApiResponse<PaymentStatusLog> statusResponse = statusLogService.updatePaymentStatus(new PaymentStatusLog(
                            null,
                            currentPayment.getId(),
                            LocalDateTime.now(),
                            PaymentStatus.PAID)
                    );
                    if (!statusResponse.isSuccess()) {
                        throw new RuntimeException("Không thể cập nhật trạng thái: " + statusResponse.getErrorMessage());
                    }

                    return null;
                },

                //  KHI THÀNH CÔNG (UI THREAD) 
                (nothing) -> {
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Thanh toán thành công!");

                    if (shouldPrint) {
                        printReceipt();
                    }

                    showSuccess("Đã thanh toán hóa đơn thành công");
                    btnConfirm.setDisable(false);
                    btnConfirmAndPrint.setDisable(false);
                    handleReset();
                },

                //  KHI CÓ LỖI (UI THREAD)
                (error) -> {
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    handleError(error);
                    btnConfirm.setDisable(false);
                    btnConfirmAndPrint.setDisable(false);
                }
        );
    }

    private void calculateChange() {
        if (currentPayment == null || txtAmountPaid.getText().isEmpty()) {
            txtChange.clear();
            return;
        }

        try {
            Integer paid = Integer.parseInt(txtAmountPaid.getText());
            Integer total = currentPayment.getGrandTotal();
            Integer change = paid - total;
            txtChange.setText(String.format("%,d", change));
        } catch (NumberFormatException e) {
            txtChange.clear();
        }
    }

    private void handlePaymentMethodChange(PaymentMethod method) {
        if (currentPayment == null) return;

        switch (method) {
            case CASH -> {
                txtAmountPaid.setEditable(true);
                txtAmountPaid.clear();
                txtAmountPaid.requestFocus();
            }
            case CARD, TRANSFER -> {
                txtAmountPaid.setText(String.valueOf(currentPayment.getGrandTotal()));
                txtAmountPaid.setEditable(false);
            }
        }
        calculateChange();
    }

    private void updatePaymentDisplay() {
        txtTotal.setText(String.format("%,d", currentPayment.getGrandTotal()));
        txtAmountDue.setText(String.format("%,d", currentPayment.getGrandTotal()));
        txtAmountPaid.clear();
        txtChange.clear();
        cbMethod.setValue(PaymentMethod.CASH);
        txtNote.clear();
    }

    private void printReceipt() {

        String receiptNumber = "RC" + String.format("%06d", currentPayment.getId());

        Receipt receipt = new Receipt(currentPayment, currentItems);
        SceneManager.setSceneData("receiptData", receipt);
        SceneManager.openModalWindow(SceneConfig.RECEIPT_FXML, SceneConfig.Titles.RECEIPT, null);
    }
}