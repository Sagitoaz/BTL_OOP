package org.example.oop.Control.PaymentControl;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ResourceBundle;

import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentMethod;
import org.miniboot.app.domain.models.Payment.PaymentStatus;
import org.miniboot.app.domain.models.Payment.PaymentWithStatus;
import org.miniboot.app.domain.models.UserRole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PaymentHistoryController implements Initializable {
    private final HttpPaymentService paymentService;
    private final ObservableList<PaymentWithStatus> paymentsWithStatus;

    // Dữ liệu tải về từ API sẽ được lưu trữ ở đây
    private List<PaymentWithStatus> allPaymentsWithStatus;

    @FXML
    private TextField txtKeyword;
    @FXML
    private Button btnFilter;
    @FXML
    private ComboBox<PaymentStatus> cbStatus;
    @FXML
    private DatePicker dpFrom, dpTo;
    @FXML
    private TableView<PaymentWithStatus> tablePayments;
    @FXML
    private TableColumn<PaymentWithStatus, String> colPaymentId;
    @FXML
    private TableColumn<PaymentWithStatus, String> colInvoiceId;
    @FXML
    private TableColumn<PaymentWithStatus, LocalDateTime> colCreatedAt;
    @FXML
    private TableColumn<PaymentWithStatus, String> colCustomer;
    @FXML
    private TableColumn<PaymentWithStatus, PaymentMethod> colMethod;
    @FXML
    private TableColumn<PaymentWithStatus, Integer> colAmount;
    @FXML
    private TableColumn<PaymentWithStatus, PaymentStatus> colStatus;
    @FXML
    private TableColumn<PaymentWithStatus, String> colStaff;
    @FXML
    private TableColumn<PaymentWithStatus, String> colNote;

    public PaymentHistoryController() {
        this.paymentService = HttpPaymentService.getInstance();
        this.paymentsWithStatus = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🔵 PaymentHistoryController: Initializing...");
        setupTableColumns();
        setupFilters();
        loadPayments(); // Tải dữ liệu ngay khi khởi tạo controller
        System.out.println("✅ PaymentHistoryController: Initialization complete");
    }

    private void setupTableColumns() {
        colPaymentId.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return new javafx.beans.property.SimpleStringProperty(payment != null ? payment.getCode() : "");
        });

        colInvoiceId.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return new javafx.beans.property.SimpleStringProperty(payment != null ? payment.getCode() : "");
        });

        colCreatedAt.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return payment != null ? new javafx.beans.property.SimpleObjectProperty<>(payment.getIssuedAt()) : null;
        });

        colCustomer.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            Integer customerId = payment != null ? payment.getCustomerId() : 0;
            return new javafx.beans.property.SimpleStringProperty(customerId != null ? "KH" + customerId : "");
        });

        colAmount.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return payment != null ? new javafx.beans.property.SimpleIntegerProperty(payment.getGrandTotal()).asObject()
                    : null;
        });

        colMethod.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return payment != null ? new javafx.beans.property.SimpleObjectProperty<>(payment.getPaymentMethod())
                    : null;
        });

        colStaff.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            int staffId = payment != null ? payment.getCashierId() : 0;
            return new javafx.beans.property.SimpleStringProperty("NV" + staffId);
        });

        colNote.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue().getPayment();
            return payment != null ? new javafx.beans.property.SimpleStringProperty(payment.getNote()) : null;
        });

        colStatus.setCellValueFactory(cellData -> {
            PaymentStatus status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleObjectProperty<>(status);
        });

        // Định dạng hiển thị các cột
        formatDateColumn();
        formatMoneyColumns();
        formatStatusColumn();
    }

    private void formatDateColumn() {
        colCreatedAt.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.atZone(ZoneId.systemDefault()).toLocalDateTime().toString());
                }
            }
        });
    }

    private void formatMoneyColumns() {
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });
    }

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
        // Reload page
        SceneManager.reloadCurrentScene();
    }

    private void formatStatusColumn() {
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(PaymentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.display());
            }
        });
    }

    private void setupFilters() {
        // Thiết lập ComboBox trạng thái
        cbStatus.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
        cbStatus.getItems().add(0, null);
        cbStatus.setValue(null);
        cbStatus.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(PaymentStatus status) {
                return status == null ? "Tất cả" : status.display();
            }

            @Override
            public PaymentStatus fromString(String string) {
                if ("Tất cả".equals(string))
                    return null;
                return PaymentStatus.valueOf(string);
            }
        });

        // Thiết lập xử lý sự kiện tìm kiếm
        txtKeyword.setOnAction(e -> searchPayments());
        btnFilter.setOnAction(e -> searchPayments());
        cbStatus.setOnAction(e -> searchPayments());
        dpFrom.setOnAction(e -> searchPayments());
        dpTo.setOnAction(e -> searchPayments());

        // Thiết lập xử lý double-click để xem chi tiết
        tablePayments.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                PaymentWithStatus selected = tablePayments.getSelectionModel().getSelectedItem();
                if (selected != null)
                    showPaymentDetails(selected.getPayment());
            }
        });
    }

    private void searchPayments() {
        try {
            // Dữ liệu đã tải, chỉ lọc trong bộ nhớ
            List<PaymentWithStatus> filtered = allPaymentsWithStatus;
            System.out.println("🔍 Starting filter with " + filtered.size() + " payments");

            // Lọc theo mã hóa đơn
            String keyword = txtKeyword.getText().trim();
            if (!keyword.isEmpty()) {
                filtered = filtered.stream()
                        .filter(p -> p.getPayment().getCode().toLowerCase().contains(keyword.toLowerCase()))
                        .toList();
                System.out.println("🔍 After keyword filter: " + filtered.size() + " payments");
            }

            // Lọc theo trạng thái (null = Tất cả, không filter)
            PaymentStatus status = cbStatus.getValue();
            if (status != null) {
                filtered = filtered.stream()
                        .filter(p -> p.getStatus() == status)
                        .toList();
                System.out.println("🔍 After status filter (" + status + "): " + filtered.size() + " payments");
            } else {
                System.out.println("🔍 No status filter (showing all)");
            }

            // Cập nhật bảng
            paymentsWithStatus.setAll(filtered);
            System.out.println("✅ Table updated with " + paymentsWithStatus.size() + " payments");

        } catch (Exception e) {
            System.err.println("❌ Error in searchPayments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPayments() {
        System.out.println("⏳ Đang tải lịch sử thanh toán...");
        
        ApiResponse<List<PaymentWithStatus>> response = paymentService.getPaymentsWithStatus();
        
        if (!response.isSuccess()) {
            System.err.println("❌ Lỗi tải lịch sử thanh toán: " + response.getErrorMessage());
            return;
        }
        
        List<PaymentWithStatus> allPayments = response.getData();
        
        if(SceneManager.getSceneData("role") == UserRole.CUSTOMER){
            int customerId = ((Customer)SceneManager.getSceneData("accountData")).getId();
            System.out.println("🔍 Lọc lịch sử thanh toán cho khách hàng ID: " + customerId);
            for(PaymentWithStatus p : allPayments){
                System.out.println("💰 Payment ID: " + p.getPayment().getId() + ", Customer ID: " + p.getPayment().getCustomerId());
            }
            allPayments = allPayments.stream()
                    .filter(p -> p.getPayment().getCustomerId() == customerId)
                    .toList();
        }
        allPaymentsWithStatus = allPayments; // Lưu lại toàn bộ danh sách
        paymentsWithStatus.setAll(allPayments);
        tablePayments.setItems(paymentsWithStatus);
    }

    private void showPaymentDetails(Payment payment) {
        // TODO: Sẽ thêm chức năng xem chi tiết sau
    }
}
