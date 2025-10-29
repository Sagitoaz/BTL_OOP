package org.example.oop.Control.PaymentControl;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Model.Receipt;
import org.example.oop.Service.HttpPaymentItemService;
import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Service.HttpPaymentStatusLogService;
import org.example.oop.Utils.AlertUtils;
import org.miniboot.app.domain.models.Payment.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
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
            AlertUtils.showWarning("Thiếu thông tin", null, "Vui lòng nhập mã hóa đơn (ID)");
            return;
        }

        try {
            int id = Integer.parseInt(invoiceIdStr);

            currentPayment = paymentService.getPaymentById(id);
            if (currentPayment == null) {
                AlertUtils.showError("Không tìm thấy", null, "Không tìm thấy hóa đơn với ID " + id);
                return;
            }

            PaymentStatus status = statusLogService.getCurrentStatusById(currentPayment.getId()).getStatus();

            // Kiểm tra trạng thái thanh toán
            if (status == PaymentStatus.PAID) {
                AlertUtils.showWarning("Đã thanh toán", null, "Hóa đơn này đã được thanh toán trước đó");
                handleReset();
                return;
            } else if (status == PaymentStatus.CANCELLED) {
                AlertUtils.showWarning("Đã hủy", null, "Hóa đơn này đã bị hủy");
                handleReset();
                return;
            } else if (status != PaymentStatus.PENDING) {
                AlertUtils.showWarning("Không thể thanh toán", null,
                        "Hóa đơn này không ở trạng thái chờ thanh toán. Trạng thái hiện tại: " + status);
                handleReset();
                return;
            }

            currentItems = itemService.getAllPaymentItems(Optional.of(id), Optional.empty());
            updatePaymentDisplay();

        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi", null, "Mã hóa đơn (ID) không hợp lệ");
        }
    }

    private void handleReset() {
        txtInvoiceId.clear();
        txtTotal.clear();
        txtAmountDue.clear();
        txtAmountPaid.clear();
        txtChange.clear();
        txtNote.clear();
        cbMethod.getSelectionModel().clearSelection();
        currentPayment = null;
        currentItems = null;

        txtInvoiceId.setEditable(true);
        btnLoadInvoice.setDisable(false);
        txtInvoiceId.requestFocus();
    }

    private void handleConfirmPayment(boolean shouldPrint) {
        if (currentPayment == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng tải hóa đơn trước khi thanh toán");
            return;
        }
        if (cbMethod.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn phương thức thanh toán");
            return;
        }
        if (txtAmountPaid.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số tiền thanh toán");
            return;
        }

        try {
            Integer amountPaid = Integer.parseInt(txtAmountPaid.getText());
            if (amountPaid < currentPayment.getGrandTotal()) {
                showAlert(Alert.AlertType.ERROR, "Số tiền không đủ",
                        "Số tiền thanh toán phải lớn hơn hoặc bằng tổng tiền");
                return;
            }

            // 1. Cập nhật thông tin vào đối tượng local
            currentPayment.setPaymentMethod(cbMethod.getValue());
            currentPayment.setAmountPaid(amountPaid);
            currentPayment.setNote(txtNote.getText());

            // 2. GỬI CẬP NHẬT LÊN SERVER
            Payment updatedPayment = paymentService.updatePayment(currentPayment);

            if (updatedPayment == null) {
                // Có lỗi xảy ra khi cập nhật, không tiếp tục
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật thông tin thanh toán. Vui lòng thử lại.");
                return;
            }

            // 3. Cập nhật trạng thái (CHỈ SAU KHI update payment thành công)
            statusLogService.updatePaymentStatus(new PaymentStatusLog(
                    null,
                    currentPayment.getId(),
                    LocalDateTime.now(),
                    PaymentStatus.PAID)
            );

            if (shouldPrint) {
                printReceipt();
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thanh toán hóa đơn thành công");
            handleReset();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ");
        }
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
            String receiptNumber = "RC" + String.format("%06d", currentPayment.getId());
            Receipt receipt = new Receipt(receiptNumber, currentPayment, currentItems);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PaymentFXML/Receipt.fxml"));
            Scene scene = new Scene(loader.load());

            ReceiptController controller = loader.getController();
            controller.displayReceipt(receipt);

            Stage stage = new Stage();
            stage.setTitle("In biên lai - " + receiptNumber);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo biên lai");
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}