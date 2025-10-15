//package org.example.oop.Control.PaymentControl;
//
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import org.example.oop.Model.PaymentModel.*;
//import org.example.oop.Repository.*;
//import org.example.oop.Utils.AlertUtils;
//
//import java.net.URL;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.ResourceBundle;
//
//public class PaymentHistoryController implements Initializable {
//    @FXML
//    private TextField txtKeyword;
//    @FXML
//    private Button btnFilter;
//    @FXML
//    private ComboBox<PaymentStatus> cbStatus;
//    @FXML
//    private DatePicker dpFrom, dpTo;
//
//    @FXML
//    private TableView<Payment> tablePayments;
//    @FXML
//    private TableColumn<Payment, String> colPaymentId;
//    @FXML
//    private TableColumn<Payment, String> colInvoiceId;
//    @FXML
//    private TableColumn<Payment, Instant> colCreatedAt;
//    @FXML
//    private TableColumn<Payment, String> colCustomer;
//    @FXML
//    private TableColumn<Payment, PaymentMethod> colMethod;
//    @FXML
//    private TableColumn<Payment, Integer> colAmount;
//    @FXML
//    private TableColumn<Payment, PaymentStatus> colStatus;
//    @FXML
//    private TableColumn<Payment, String> colStaff;
//    @FXML
//    private TableColumn<Payment, String> colNote;
//
//    // Chi tiết hóa đơn sẽ được thêm sau
//    private final PaymentRepository paymentRepository;
//    private final PaymentStatusLogRepository statusLogRepository;
//    private final ObservableList<Payment> payments;
//
//    public PaymentHistoryController() {
//        this.paymentRepository = new PaymentRepository();
//        this.statusLogRepository = new PaymentStatusLogRepository();
//        this.payments = FXCollections.observableArrayList();
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        setupTableColumns();
//        setupFilters();
//        loadPayments();
//    }
//
//    private void setupTableColumns() {
//        // Thiết lập các cột cho bảng hóa đơn
//        colPaymentId.setCellValueFactory(new PropertyValueFactory<>("code"));
//        colInvoiceId.setCellValueFactory(new PropertyValueFactory<>("code")); // Tạm thời dùng code
//        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("issuedAt"));
//        colCustomer.setCellValueFactory(cellData -> {
//            Payment payment = cellData.getValue();
//            Integer customerId = payment.getCustomerId();
//            // TODO: Lấy tên khách hàng từ CustomerRepository
//            return new javafx.beans.property.SimpleStringProperty(
//                    customerId != null ? "KH" + customerId : "");
//        });
//        colAmount.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
//        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
//        colStaff.setCellValueFactory(cellData -> {
//            Payment payment = cellData.getValue();
//            int staffId = payment.getCashierId();
//            // TODO: Lấy tên nhân viên từ StaffRepository
//            return new javafx.beans.property.SimpleStringProperty("NV" + staffId);
//        });
//        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
//        colStatus.setCellValueFactory(cellData -> {
//            Payment payment = cellData.getValue();
//            PaymentStatus status = statusLogRepository.findCurrentStatus(payment.getId());
//            return new javafx.beans.property.SimpleObjectProperty<>(status);
//        });
//
//        // Định dạng hiển thị các cột
//        formatDateColumn();
//        formatMoneyColumns();
//        formatStatusColumn();
//
//        // Chi tiết sẽ được thêm sau
//    }
//
//    private void formatDateColumn() {
//        colCreatedAt.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(Instant item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.atZone(ZoneId.systemDefault())
//                            .toLocalDateTime().toString());
//                }
//            }
//        });
//    }
//
//    private void formatMoneyColumns() {
//        colAmount.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(Integer item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(String.format("%,d", item));
//                }
//            }
//        });
//    }
//
//    private void formatStatusColumn() {
//        colStatus.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(PaymentStatus item, boolean empty) {
//                super.updateItem(item, empty);
//                setText(empty || item == null ? null : item.display());
//            }
//        });
//    }
//
//    private void setupFilters() {
//        // Thiết lập ComboBox trạng thái
//        cbStatus.setItems(FXCollections.observableArrayList(PaymentStatus.values()));
//        cbStatus.getItems().add(0, null);
//        cbStatus.setValue(null);
//        cbStatus.setConverter(new javafx.util.StringConverter<>() {
//            @Override
//            public String toString(PaymentStatus status) {
//                return status == null ? "Tất cả" : status.display();
//            }
//
//            @Override
//            public PaymentStatus fromString(String string) {
//                if ("Tất cả".equals(string))
//                    return null;
//                return PaymentStatus.valueOf(string);
//            }
//        });
//
//        // Thiết lập xử lý sự kiện tìm kiếm
//        txtKeyword.setOnAction(e -> searchPayments());
//        btnFilter.setOnAction(e -> searchPayments());
//        cbStatus.setOnAction(e -> searchPayments());
//        dpFrom.setOnAction(e -> searchPayments());
//        dpTo.setOnAction(e -> searchPayments());
//
//        // Thiết lập xử lý double-click để xem chi tiết
//        tablePayments.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                Payment selected = tablePayments.getSelectionModel().getSelectedItem();
//                if (selected != null)
//                    showPaymentDetails(selected);
//            }
//        });
//    }
//
//    private void searchPayments() {
//        try {
//            List<Payment> filtered = paymentRepository.findAll();
//
//            // Lọc theo mã hóa đơn
//            String keyword = txtKeyword.getText().trim();
//            if (!keyword.isEmpty()) {
//                filtered = filtered.stream()
//                        .filter(p -> p.getCode().toLowerCase().contains(keyword.toLowerCase()))
//                        .toList();
//            }
//
//            // Lọc theo trạng thái
//            PaymentStatus status = cbStatus.getValue();
//            if (status != null) {
//                filtered = filtered.stream()
//                        .filter(p -> statusLogRepository.findCurrentStatus(p.getId()) == status)
//                        .toList();
//            }
//
//            // Cập nhật bảng
//            payments.setAll(filtered);
//
//        } catch (Exception e) {
//            AlertUtils.showError("Lỗi", "Không thể tìm kiếm hóa đơn", e.getMessage());
//        }
//    }
//
//    private void loadPayments() {
//        try {
//            List<Payment> allPayments = paymentRepository.findAll();
//            payments.setAll(allPayments);
//            tablePayments.setItems(payments);
//        } catch (Exception e) {
//            AlertUtils.showError("Lỗi", "Không thể tải dữ liệu hóa đơn", e.getMessage());
//        }
//    }
//
//    private void showPaymentDetails(Payment payment) {
//        // TODO: Sẽ thêm chức năng xem chi tiết sau
//        AlertUtils.showError("Thông báo", "Chức năng đang được phát triển",
//                "Xem chi tiết hóa đơn " + payment.getCode());
//    }
//}
