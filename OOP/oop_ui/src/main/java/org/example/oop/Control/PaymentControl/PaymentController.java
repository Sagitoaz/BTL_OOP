package org.example.oop.Control.PaymentControl;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Model.Receipt;
import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Service.HttpPaymentStatusLogService;
import org.miniboot.app.domain.models.Payment.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPaymentMethods();
        setupEventHandlers();
        setupListeners();
        handleReset();
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
        String invoiceId = txtInvoiceId.getText().trim();
        if (invoiceId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mã hóa đơn");
            return;
        }

        try {
            int id = Integer.parseInt(invoiceId.replace("HD", ""));
            currentPayment = (new HttpPaymentService()).getPaymentById(id);
            if (currentPayment == null) {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy", "Không tìm thấy hóa đơn với mã " + invoiceId);
                return;
            }

            PaymentStatus status = (new HttpPaymentStatusLogService()).getCurrentStatusById(currentPayment.getId()).getStatus();

            // Kiểm tra trạng thái thanh toán
            if (status == PaymentStatus.PAID) {
                showAlert(Alert.AlertType.WARNING, "Đã thanh toán", "Hóa đơn này đã được thanh toán trước đó");
                handleReset();
                return;
            } else if (status == PaymentStatus.CANCELLED) {
                showAlert(Alert.AlertType.WARNING, "Đã hủy", "Hóa đơn này đã bị hủy");
                handleReset();
                return;
            } else if (status != PaymentStatus.PENDING) {
                showAlert(Alert.AlertType.WARNING, "Không thể thanh toán",
                        "Hóa đơn này không ở trạng thái chờ thanh toán");
                handleReset();
                return;
            }

            // Chỉ load thông tin chi tiết nếu hóa đơn đang ở trạng thái PENDING
//            currentItems = itemRepository.findByPaymentId(id);
            updatePaymentDisplay();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã hóa đơn không hợp lệ");
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

            currentPayment.setPaymentMethod(cbMethod.getValue());
            currentPayment.setAmountPaid(amountPaid);
            currentPayment.setNote(txtNote.getText());

//            paymentRepository.update(currentPayment);

            (new HttpPaymentStatusLogService()).updatePaymentStatus(new PaymentStatusLog(
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
            // nếu có MIXED, tuỳ nghiệp vụ bạn thêm logic ở đây
        }
        calculateChange();
    }

    private void updatePaymentDisplay() {
        txtTotal.setText(String.format("%,d", currentPayment.getGrandTotal()));
        txtAmountDue.setText(String.format("%,d", currentPayment.getGrandTotal()));
        txtAmountPaid.clear();
        txtChange.clear();
        cbMethod.getSelectionModel().clearSelection();
        txtNote.clear();
    }

    private void printReceipt() {
        try {
            String receiptNumber = "RC" + String.format("%06d", currentPayment.getId());
            Receipt receipt = new Receipt(receiptNumber,
                    currentPayment,
                    currentItems);

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
