package org.example.oop.Control.PaymentControl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.oop.Control.BaseController;
import org.example.oop.Service.ApiStockMovementService;
import org.example.oop.Service.HttpPaymentItemService;
import org.example.oop.Service.HttpPaymentService;
import org.example.oop.Service.HttpPaymentStatusLogService;
import org.example.oop.Utils.ApiResponse;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Inventory.Enum.MoveType;
import org.miniboot.app.domain.models.Inventory.StockMovement;
import org.miniboot.app.domain.models.Payment.*;
import org.miniboot.app.domain.models.UserRole;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentHistoryController extends BaseController implements Initializable {
    private final HttpPaymentService paymentService;
    private final HttpPaymentStatusLogService statusLogService;
    private final HttpPaymentItemService paymentItemService;
    private final ApiStockMovementService stockMovementService;
    private final ObservableList<PaymentWithStatus> paymentsWithStatus;

    // Dữ liệu tải về từ API sẽ được lưu trữ ở đây
    private List<PaymentWithStatus> allPaymentsWithStatus;

    //  LOADING STATUS
    @FXML
    private HBox loadingStatusContainer;
    @FXML
    private ProgressIndicator statusProgressIndicator;
    @FXML
    private Label loadingStatusLabel;

    @FXML
    private TextField txtKeyword;
    @FXML
    private Button btnFilter;
    @FXML
    private ComboBox<PaymentStatus> cbStatus;
    @FXML
    private DatePicker dpFrom, dpTo;
    @FXML
    private Button btnExport;
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
        this.statusLogService = HttpPaymentStatusLogService.getInstance();
        this.paymentItemService = HttpPaymentItemService.getInstance();
        this.stockMovementService = new ApiStockMovementService();
        this.paymentsWithStatus = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🔵 PaymentHistoryController: Initializing...");

        // Khởi tạo TableView với ObservableList trước
        tablePayments.setItems(paymentsWithStatus);

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
                if (selected != null) {
                    handlePaymentClick(selected);
                }
            }
        });
    }

    /**
     * Xử lý khi click vào payment
     * - Nếu status là UNPAID: hiện dialog chọn Hủy hóa đơn hoặc Thanh toán
     * - Nếu status là PENDING: hiện dialog chọn Hủy hóa đơn hoặc Thanh toán
     * - Các status khác: xem chi tiết (tương lai)
     */
    private void handlePaymentClick(PaymentWithStatus paymentWithStatus) {
        Payment payment = paymentWithStatus.getPayment();
        PaymentStatus status = paymentWithStatus.getStatus();

        if (status == PaymentStatus.UNPAID || status == PaymentStatus.PENDING) {
            showPaymentActionDialog(payment, status);
        } else {
            showPaymentDetails(payment);
        }
    }

    /**
     * Hiển thị dialog cho payment chưa thanh toán hoặc đang chờ xử lý
     * Cho phép chọn: Hủy hóa đơn hoặc Thanh toán
     */
    private void showPaymentActionDialog(Payment payment, PaymentStatus currentStatus) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        // Tùy chỉnh tiêu đề dựa vào trạng thái
        if (currentStatus == PaymentStatus.UNPAID) {
            alert.setTitle("Thanh toán chưa hoàn tất");
        } else if (currentStatus == PaymentStatus.PENDING) {
            alert.setTitle("Đang chờ thanh toán");
        }

        alert.setHeaderText("Hóa đơn: " + payment.getCode());
        alert.setContentText("Vui lòng chọn hành động:");

        ButtonType btnPay = new ButtonType("Thanh toán");
        ButtonType btnCancelInvoice = new ButtonType("Hủy hóa đơn");
        ButtonType btnClose = new ButtonType("Đóng", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnPay, btnCancelInvoice, btnClose);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnCancelInvoice) {
                // Hủy hóa đơn - cập nhật status thành CANCELLED
                handleCancelPayment(payment);
            } else if (response == btnPay) {
                // Chuyển sang scene thanh toán (và cập nhật status nếu cần)
                handleGoToPayment(payment, currentStatus);
            }
        });
    }

    /**
     * Hủy thanh toán - cập nhật status thành CANCELLED và hoàn trả hàng về kho
     */
    private void handleCancelPayment(Payment payment) {
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang hủy hóa đơn...");

        executeAsync(
                () -> {
                    // Task chạy trên background thread
                    try {
                        System.out.println("🔄 Đang hủy thanh toán: " + payment.getCode() + " (ID: " + payment.getId() + ")");

                        // Bước 1: Lấy danh sách payment items
                        System.out.println("📦 Bước 1: Lấy danh sách payment items...");
                        ApiResponse<List<PaymentItem>> itemsResponse = paymentItemService.getPaymentItemsByPaymentId(payment.getId());

                        List<PaymentItem> paymentItems = new ArrayList<>();

                        if (itemsResponse.isSuccess()) {
                            paymentItems = itemsResponse.getData();
                            if (paymentItems != null) {
                                System.out.println("✅ Tìm thấy " + paymentItems.size() + " sản phẩm trong hóa đơn");
                            } else {
                                System.out.println("⚠️ Không có sản phẩm trong hóa đơn (data is null)");
                                paymentItems = new ArrayList<>();
                            }
                        } else {
                            System.err.println("⚠️ Không thể lấy payment items: " + itemsResponse.getErrorMessage());
                            System.out.println("ℹ️ Tiếp tục hủy hóa đơn mà không hoàn trả hàng");
                        }

                        // Bước 2: Tạo stock movements để hoàn trả hàng về kho (nếu có items)
                        if (!paymentItems.isEmpty()) {
                            System.out.println("📦 Bước 2: Tạo stock movements để hoàn trả hàng...");
                            List<StockMovement> returnMovements = new ArrayList<>();

                            int userId = payment.getCashierId();

                            for (PaymentItem item : paymentItems) {
                                StockMovement movement = new StockMovement();
                                movement.setProductId(item.getProductId());
                                movement.setQty(item.getQty());
                                movement.setMoveType(MoveType.RETURN_IN);
                                movement.setRefTable("payments");
                                movement.setRefId(payment.getId());
                                movement.setMovedAt(LocalDateTime.now());
                                movement.setMovedBy(userId);
                                movement.setNote("Hoàn trả do hủy hóa đơn: " + payment.getCode());

                                returnMovements.add(movement);
                                System.out.println("  ➕ Product ID: " + item.getProductId() + ", Qty: +" + item.getQty());
                            }

                            try {
                                List<StockMovement> createdMovements = stockMovementService.createListStockMovement(returnMovements);
                                System.out.println("✅ Đã tạo " + createdMovements.size() + " stock movements để hoàn trả hàng");
                            } catch (Exception e) {
                                System.err.println("❌ Lỗi khi tạo stock movements: " + e.getMessage());
                                throw new RuntimeException("Không thể hoàn trả hàng về kho: " + e.getMessage());
                            }
                        } else {
                            System.out.println("ℹ️ Không có sản phẩm cần hoàn trả");
                        }

                        // Bước 3: Cập nhật status payment thành CANCELLED
                        System.out.println("📝 Bước 3: Cập nhật status payment thành CANCELLED...");
                        ApiResponse<org.miniboot.app.domain.models.Payment.PaymentStatusLog> response =
                                statusLogService.updatePaymentStatus(payment.getId(), PaymentStatus.CANCELLED);

                        if (!response.isSuccess()) {
                            throw new RuntimeException("Không thể cập nhật trạng thái thanh toán: " + response.getErrorMessage());
                        }

                        return paymentItems; // Trả về danh sách items
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                (paymentItems) -> {
                    // Success callback - chạy trên UI thread
                    System.out.println("✅ Đã hủy thanh toán thành công");

                    String message;
                    if (paymentItems != null && !paymentItems.isEmpty()) {
                        message = "Đã hủy hóa đơn " + payment.getCode() + " và hoàn trả " +
                                paymentItems.size() + " sản phẩm về kho";
                    } else {
                        message = "Đã hủy hóa đơn " + payment.getCode();
                    }

                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ " + message);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", message);

                    // Reload lại danh sách
                    loadPayments();
                },
                (error) -> {
                    // Error callback - chạy trên UI thread
                    System.err.println("❌ Exception khi hủy thanh toán: " + error.getMessage());
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi hủy thanh toán: " + error.getMessage());
                }
        );
    }

    /**
     * Chuyển sang scene thanh toán
     * Nếu payment đang ở status UNPAID, sẽ cập nhật thành PENDING trước
     */
    private void handleGoToPayment(Payment payment, PaymentStatus currentStatus) {
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang chuẩn bị thanh toán...");

        executeAsync(
                () -> {
                    // Task chạy trên background thread
                    System.out.println("🔄 Chuyển sang scene thanh toán cho: " + payment.getCode() + " (ID: " + payment.getId() + ")");

                    // Nếu payment đang UNPAID, cập nhật sang PENDING trước khi thanh toán
                    if (currentStatus == PaymentStatus.UNPAID) {
                        System.out.println("📝 Cập nhật status từ UNPAID sang PENDING...");
                        ApiResponse<org.miniboot.app.domain.models.Payment.PaymentStatusLog> response =
                                statusLogService.updatePaymentStatus(payment.getId(), PaymentStatus.PENDING);

                        if (!response.isSuccess()) {
                            throw new RuntimeException("Không thể cập nhật trạng thái thanh toán: " + response.getErrorMessage());
                        }
                        System.out.println("✅ Đã cập nhật status sang PENDING");
                    }

                    return null;
                },
                (nothing) -> {
                    // Success callback - chạy trên UI thread
                    try {
                        showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                "✅ Mở trang thanh toán...");

                        // Lưu payment ID vào SceneData
                        SceneManager.setSceneData("savedPaymentId", String.valueOf(payment.getId()));

                        // Clear cache to ensure Payment screen loads fresh data
                        SceneManager.removeFromCache(SceneConfig.PAYMENT_FXML);

                        // Chuyển scene
                        SceneManager.switchScene(SceneConfig.PAYMENT_FXML, SceneConfig.Titles.PAYMENT);
                    } catch (Exception ex) {
                        System.err.println("❌ Lỗi khi chuyển scene: " + ex.getMessage());
                        showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                                "❌ Lỗi mở trang thanh toán");
                        showAlert(Alert.AlertType.ERROR, "Lỗi",
                                "Không thể chuyển sang trang thanh toán: " + ex.getMessage());
                    }
                },
                (error) -> {
                    // Error callback - chạy trên UI thread
                    System.err.println("❌ Lỗi: " + error.getMessage());
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Lỗi",
                            "Không thể cập nhật trạng thái thanh toán: " + error.getMessage());
                }
        );
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

            // Lọc theo ngày từ (dpFrom)
            if (dpFrom.getValue() != null) {
                LocalDateTime fromDateTime = dpFrom.getValue().atStartOfDay();
                filtered = filtered.stream()
                        .filter(p -> p.getPayment().getIssuedAt() != null &&
                                !p.getPayment().getIssuedAt().isBefore(fromDateTime))
                        .toList();
                System.out.println("🔍 After 'from date' filter: " + filtered.size() + " payments");
            }

            // Lọc theo ngày đến (dpTo)
            if (dpTo.getValue() != null) {
                LocalDateTime toDateTime = dpTo.getValue().atTime(23, 59, 59);
                filtered = filtered.stream()
                        .filter(p -> p.getPayment().getIssuedAt() != null &&
                                !p.getPayment().getIssuedAt().isAfter(toDateTime))
                        .toList();
                System.out.println("🔍 After 'to date' filter: " + filtered.size() + " payments");
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
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang tải lịch sử thanh toán...");

        executeAsync(
                () -> {
                    // Task chạy trên background thread
                    try {
                        ApiResponse<List<PaymentWithStatus>> response = paymentService.getPaymentsWithStatus();

                        if (!response.isSuccess()) {
                            throw new RuntimeException("Không thể tải lịch sử thanh toán: " + response.getErrorMessage());
                        }

                        List<PaymentWithStatus> allPayments = response.getData();

                        if (allPayments == null) {
                            System.err.println("❌ Dữ liệu trả về null");
                            allPayments = List.of();
                        }

                        System.out.println("📊 Tổng số hóa đơn: " + allPayments.size());

                        // Lọc theo role nếu là customer
                        if (SceneManager.getSceneData("role") == UserRole.CUSTOMER) {
                            Object accountData = SceneManager.getSceneData("accountData");
                            if (accountData instanceof Customer) {
                                int customerId = ((Customer) accountData).getId();
                                System.out.println("🔍 Lọc lịch sử thanh toán cho khách hàng ID: " + customerId);

                                allPayments = allPayments.stream()
                                        .filter(p -> p.getPayment() != null && p.getPayment().getCustomerId() == customerId)
                                        .toList();

                                System.out.println("📊 Số hóa đơn sau khi lọc: " + allPayments.size());
                            }
                        }

                        return allPayments;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                (allPayments) -> {
                    // Success callback - chạy trên UI thread
                    // Lưu lại toàn bộ danh sách và cập nhật bảng
                    allPaymentsWithStatus = allPayments;
                    paymentsWithStatus.clear();
                    paymentsWithStatus.addAll(allPayments);

                    System.out.println("✅ Đã tải " + paymentsWithStatus.size() + " hóa đơn");
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Đã tải " + paymentsWithStatus.size() + " hóa đơn");
                },
                (error) -> {
                    // Error callback - chạy trên UI thread
                    System.err.println("❌ Exception khi tải lịch sử thanh toán: " + error.getMessage());
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi tải lịch sử thanh toán: " + error.getMessage());
                }
        );
    }

    private void showPaymentDetails(Payment payment) {
    }

    @FXML
    private void onExport() {
        try {
            if (paymentsWithStatus == null || paymentsWithStatus.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Không có dữ liệu", "Không có dữ liệu để xuất!");
                return;
            }

            // Prepare headers
            java.util.List<String> headers = java.util.Arrays.asList(
                    "Mã phiếu", "Mã HĐ", "Ngày giờ", "Khách hàng",
                    "Phương thức", "Số tiền", "Trạng thái", "Nhân viên", "Ghi chú"
            );

            // Prepare data
            java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>();
            for (PaymentWithStatus pws : paymentsWithStatus) {
                Payment payment = pws.getPayment();
                if (payment == null) continue;

                java.util.List<Object> row = java.util.Arrays.asList(
                        payment.getCode() != null ? payment.getCode() : "",
                        payment.getCode() != null ? payment.getCode() : "",
                        payment.getIssuedAt() != null ? payment.getIssuedAt() : "",
                        payment.getCustomerId() != null ? "KH" + payment.getCustomerId() : "",
                        payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "",
                        payment.getGrandTotal(),
                        pws.getStatus() != null ? pws.getStatus().toString() : "",
                        "NV" + payment.getCashierId(),
                        payment.getNote() != null ? payment.getNote() : ""
                );
                data.add(row);
            }

            // Generate filename and path
            String directory = org.example.oop.Utils.ExcelExporter.getDocumentsPath();
            org.example.oop.Utils.ExcelExporter.ensureDirectoryExists(directory);
            String fileName = org.example.oop.Utils.ExcelExporter.generateFileName("LichSuThanhToan");
            String fullPath = directory + fileName;

            // Export to Excel
            org.example.oop.Utils.ExcelExporter.exportToFile(fullPath, "Lịch sử thanh toán", headers, data);

            showAlert(Alert.AlertType.INFORMATION, "Xuất file thành công!",
                    "Đã xuất lịch sử thanh toán ra file:\n" + fileName + "\n\nVị trí: " + fullPath);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xuất file: " + e.getMessage());
        }
    }
}
