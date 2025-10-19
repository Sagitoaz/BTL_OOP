package org.example.oop.Control.PaymentControl;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.oop.Model.PaymentModel.Payment;
import org.example.oop.Model.PaymentModel.PaymentItem;
import org.example.oop.Model.PaymentModel.Receipt;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReceiptController implements Initializable {
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
        initializeTable();
    }

    private void initializeTable() {
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
        lblReceiptNo.setText(receipt.getReceiptNumber());
        lblDate.setText(formatDateTime(payment.getIssuedAt()));
        lblCashier.setText(String.valueOf(payment.getCashierId())); // TODO: Get cashier name
        lblCustomer.setText(payment.getCustomerId() == null ? "Khách lẻ" : String.valueOf(payment.getCustomerId()));

        // Hiển thị danh sách items
        tableReceiptItems.getItems().setAll(receipt.getItems());

        // Hiển thị thông tin thanh toán
        lblTotal.setText(String.format("%,d", payment.getGrandTotal()));
        lblAmountPaid.setText(String.format("%,d", payment.getAmountPaid().intValue()));

        // Tính tiền thừa
        Integer change = payment.getAmountPaid() - payment.getGrandTotal();
        lblChange.setText(String.format("%,d", change.intValue()));
    }

    private String formatDateTime(Instant dateTime) {
        return DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(dateTime);
    }
}
