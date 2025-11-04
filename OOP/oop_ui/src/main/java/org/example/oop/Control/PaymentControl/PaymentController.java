package org.example.oop.Control.PaymentControl;

// 1. IMPORT BaseController

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.oop.Control.BaseController;
import org.example.oop.Model.Receipt;
import org.example.oop.Service.HttpPaymentItemService;
import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Service.HttpPaymentStatusLogService;
import org.miniboot.app.domain.models.Payment.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

// 3. KẾ THỪA TỪ BaseController
public class PaymentController extends BaseController implements Initializable {
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

    // <-- Khai báo các service
    private HttpPaymentService paymentService;
    private HttpPaymentStatusLogService statusLogService;
    private HttpPaymentItemService itemService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // <-- Khởi tạo các service
        paymentService = new HttpPaymentService();
        statusLogService = new HttpPaymentStatusLogService();
        itemService = new HttpPaymentItemService();

        setupPaymentMethods();
        setupEventHandlers();
        setupListeners();
        handleReset();
    }

    // ========================================================
    // ✅ HÀM MỚI: Dùng để nhận dữ liệu từ InvoiceController
    // ========================================================
    public void initData(String paymentId) {
        txtInvoiceId.setText(paymentId);
        handleLoadInvoice(); // Tự động tải hóa đơn

        // Vô hiệu hóa việc sửa mã HĐ vì nó đã được truyền vào
        txtInvoiceId.setEditable(false);
        btnLoadInvoice.setDisable(true);
    }
    // ========================================================

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
            // 4. SỬ DỤNG phương thức kế thừa
            showWarning("Vui lòng nhập mã hóa đơn (ID)");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(invoiceIdStr);
        } catch (NumberFormatException e) {
            // 4. SỬ DỤNG phương thức kế thừa
            showError("Mã hóa đơn (ID) không hợp lệ");
            return;
        }

        // (Tùy chọn: Thêm logic hiển thị loading...)
        // loadingSpinner.setVisible(true);

        // 5. BỌC CÁC LỆNH GỌI API TRONG executeAsync
        executeAsync(
                // --------- TÁC VỤ NỀN (BACKGROUND THREAD) ---------
                () -> {
                    // 1. Lấy payment
                    Payment payment = paymentService.getPaymentById(id);
                    if (payment == null) {
                        // Ném lỗi để onError xử lý
                        throw new RuntimeException("Không tìm thấy hóa đơn với ID " + id);
                    }

                    // 2. Lấy trạng thái
                    PaymentStatus status = statusLogService.getCurrentStatusById(payment.getId()).getStatus();

                    // 3. Kiểm tra trạng thái
                    if (status == PaymentStatus.PAID) {
                        throw new RuntimeException("Hóa đơn này đã được thanh toán trước đó");
                    } else if (status == PaymentStatus.CANCELLED) {
                        throw new RuntimeException("Hóa đơn này đã bị hủy");
                    } else if (status != PaymentStatus.PENDING) {
                        throw new RuntimeException("Hóa đơn này không ở trạng thái chờ thanh toán. Trạng thái hiện tại: " + status);
                    }

                    // 4. Gán biến global (vẫn an toàn vì onSuccess sẽ đọc sau)
                    currentPayment = payment;
                    currentItems = itemService.getAllPaymentItems(Optional.of(id), Optional.empty());

                    return true; // Trả về true nếu mọi thứ thành công
                },

                // --------- KHI THÀNH CÔNG (UI THREAD) ---------
                (success) -> {
                    // (Tùy chọn: Ẩn loading)
                    // loadingSpinner.setVisible(false);

                    // Cập nhật giao diện với dữ liệu đã tải
                    updatePaymentDisplay();
                },

                // --------- KHI CÓ LỖI (UI THREAD) ---------
                (error) -> {
                    // (Tùy chọn: Ẩn loading)
                    // loadingSpinner.setVisible(false);

                    // Nếu là lỗi nghiệp vụ ta tự ném ra (RuntimeException)
                    if (error instanceof RuntimeException) {
                        showWarning(error.getMessage());
                    } else {
                        // Nếu là lỗi kết nối, mạng, ... dùng hàm handleError chung
                        handleError(error);
                    }
                    handleReset(); // Reset form khi có lỗi
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
        // --- 1. VALIDATION (Chạy trên UI thread) ---
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

        // (Tùy chọn: Hiển thị loading...)

        // --- 2. GỌI API (Chạy trên Background thread) ---
        executeAsync(
                // --------- TÁC VỤ NỀN (BACKGROUND THREAD) ---------
                () -> {
                    // 1. GỬI CẬP NHẬT LÊN SERVER
                    Payment updatedPayment = paymentService.updatePayment(currentPayment);

                    if (updatedPayment == null) {
                        throw new RuntimeException("Không thể cập nhật thông tin thanh toán. Vui lòng thử lại.");
                    }

                    // 2. Cập nhật trạng thái
                    statusLogService.updatePaymentStatus(new PaymentStatusLog(
                            null,
                            currentPayment.getId(),
                            LocalDateTime.now(),
                            PaymentStatus.PAID)
                    );
                },

                // --------- KHI THÀNH CÔNG (UI THREAD) ---------
                () -> {
                    // (Tùy chọn: Ẩn loading...)

                    if (shouldPrint) {
                        printReceipt();
                    }

                    showSuccess("Đã thanh toán hóa đơn thành công");
                    handleReset();
                }
                // Khi lỗi, hàm handleError chung của BaseController sẽ tự động được gọi
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
        try {
            Receipt receipt = new Receipt(currentPayment, currentItems);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PaymentFXML/Receipt.fxml"));
            Scene scene = new Scene(loader.load());

            ReceiptController controller = loader.getController();
            controller.displayReceipt(receipt);

            Stage stage = new Stage();
            stage.setTitle("In biên lai - " + currentPayment.getCode());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // 4. SỬ DỤNG phương thức kế thừa
            showError("Không thể tạo biên lai: " + e.getMessage());
        }
    }
}