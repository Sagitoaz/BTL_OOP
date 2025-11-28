package org.example.oop.Control.PaymentControl;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.example.oop.Control.BaseController;
import org.example.oop.Model.Receipt;
import org.example.oop.Utils.PDFExporter;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentItem;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class ReceiptController extends BaseController implements Initializable {
    @FXML
    private AnchorPane loadingOverlay;

    @FXML
    private Label lblReceiptNo;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblCashier;
    @FXML
    private Label lblCustomer;

    @FXML
    private TableView<PaymentItem> tableReceiptItems;
    @FXML
    private TableColumn<PaymentItem, Integer> colIndex;
    @FXML
    private TableColumn<PaymentItem, String> colItemName;
    @FXML
    private TableColumn<PaymentItem, Integer> colQty;
    @FXML
    private TableColumn<PaymentItem, Integer> colUnitPrice;
    @FXML
    private TableColumn<PaymentItem, Integer> colAmount;

    @FXML
    private Label lblTotal;
    @FXML
    private Label lblAmountPaid;
    @FXML
    private Label lblChange;

    private Receipt receipt;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hideLoading();
        initializeTable();
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        }
    }

    private void initializeTable() {
        if (SceneManager.getSceneData("receiptData") != null) {
            System.out.println("Loading receipt data into receipt view...");
            Receipt receipt = SceneManager.getSceneData("receiptData");
            displayReceipt(receipt);
        }

        // Thiết lập cột số thứ tự
        colIndex.setCellValueFactory(
                data -> new SimpleIntegerProperty(tableReceiptItems.getItems().indexOf(data.getValue()) + 1)
                        .asObject());

        // Thiết lập các cột còn lại
        colItemName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalLine"));

        // Format số tiền
        colUnitPrice.setCellFactory(tc -> new TableCell<PaymentItem, Integer>() {
            @Override
            protected void updateItem(Integer price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", price));
                }
            }
        });

        colAmount.setCellFactory(tc -> new TableCell<PaymentItem, Integer>() {
            @Override
            protected void updateItem(Integer amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", amount));
                }
            }
        });

    }

    public void displayReceipt(Receipt receipt) {
        this.receipt = receipt;

        Payment payment = receipt.getPayment();

        // Hiển thị thông tin chung
        lblReceiptNo.setText(receipt.getPayment().getCode());
        lblDate.setText(payment.getIssuedAt().toString());
        lblCashier.setText(String.valueOf(payment.getCashierId()));

        lblCustomer.setText(payment.getCustomerId() == null ? "Khách lẻ" : String.valueOf(payment.getCustomerId()));

        System.out.println("Receipt has " + receipt.getItems().size() + " items.");

        // Hiển thị danh sách items
        tableReceiptItems.getItems().setAll(receipt.getItems());

        // Hiển thị thông tin thanh toán
        lblTotal.setText(String.format("%,d", payment.getGrandTotal()));
        lblAmountPaid.setText(String.format("%,d", payment.getAmountPaid()));

        // Tính tiền thừa
        Integer change = payment.getAmountPaid() - payment.getGrandTotal();
        lblChange.setText(String.format("%,d", change));
        SceneManager.removeSceneData("receiptData");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(dateTime);
    }

    @FXML
    private void onExportPdf() {
        if (receipt == null) {
            showWarning("Không có dữ liệu\n" + "Không có hóa đơn để xuất PDF.");
            return;
        }

        try {
            Payment payment = receipt.getPayment();

            // Tạo tên file với mã hóa đơn
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "HoaDon_" + payment.getCode() + "_" + timestamp;

            // Xuất PDF với method mới (hỗ trợ tiếng Việt và format đẹp)
            boolean success = PDFExporter.exportReceipt(payment, receipt.getItems(), fileName);

            if (success) {
                showSuccess("Thành công\n" + "Đã xuất hóa đơn PDF thành công!");
            } else {
                showError("Lỗi\n" + "Không thể xuất hóa đơn PDF. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi\n" + "Có lỗi xảy ra khi xuất PDF: " + e.getMessage());
        }
    }
}
