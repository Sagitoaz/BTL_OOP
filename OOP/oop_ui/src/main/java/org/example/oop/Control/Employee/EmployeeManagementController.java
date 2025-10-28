package org.example.oop.Control.Employee;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.miniboot.app.domain.models.Employee;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class EmployeeManagementController extends BaseController implements Initializable {

    // ====== Top bar labels ======
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label statusLabel;
    // ====== Quick stats ======
    @FXML
    private Label totalCountLabel;
    @FXML
    private Label doctorCountLabel;
    @FXML
    private Label nurseCountLabel;
    @FXML
    private Label activeCountLabel;

    // ====== Filters & Actions ======
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterComboBox; // [Tất cả, doctor, nurse]
    @FXML
    private ComboBox<String> statusFilterComboBox; // [Tất cả, Hoạt động, Không hoạt động]
    @FXML
    private Button refreshButton;
    @FXML
    private Button addButton;

    // ====== Table ======
    @FXML
    private TableView<Employee> employeeTableView;
    @FXML
    private TableColumn<Employee, Integer> idColumn;
    @FXML
    private TableColumn<Employee, String> usernameColumn;
    @FXML
    private TableColumn<Employee, String> fullNameColumn;
    @FXML
    private TableColumn<Employee, String> roleColumn;
    @FXML
    private TableColumn<Employee, String> licenseColumn;
    @FXML
    private TableColumn<Employee, String> emailColumn;
    @FXML
    private TableColumn<Employee, String> phoneColumn;
    @FXML
    private TableColumn<Employee, Boolean> activeColumn;

    // ====== Delete Button ======
    @FXML
    private Button deleteButton;

    private final HttpEmployeeService service = new HttpEmployeeService();

    private final ObservableList<Employee> master = FXCollections.observableArrayList();
    private FilteredList<Employee> filtered;
    private SortedList<Employee> sorted;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        bindTableSorting();
        subtitleLabel.setText("Quản lý thông tin bác sĩ và nhân viên phòng khám");
        statusLabel.setText("Sẵn sàng");
        loadEmployees();
        employeeTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Nếu là double-click
                Employee selectedEmployee = employeeTableView.getSelectionModel().getSelectedItem();
                if (selectedEmployee != null) {
                    openEmployeeDetailView(selectedEmployee); // Gọi hàm mở trang chi tiết
                }
            }
        });
    }
    private void openEmployeeDetailView(Employee employee) {
        try {
            // Mở cửa sổ chi tiết nhân viên, có thể sử dụng một phương thức tương ứng hoặc mở một cửa sổ FXML mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/EmployeeDetail.fxml")); // Đường dẫn đến FXML của trang chi tiết
            Parent root = loader.load();

            // Truyền dữ liệu qua controller của EmployeeDetail
            EmployeeDetailController controller = loader.getController();
            controller.setEmployeeDetails(employee); // Giả sử bạn có phương thức setEmployeeDetails để thiết lập dữ liệu cho trang chi tiết

            // Hiển thị cửa sổ chi tiết nhân viên
            Stage stage = new Stage();
            stage.setTitle("Chi tiết nhân viên");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi mở cửa sổ chi tiết nhân viên: " + e.getMessage());
        }
    }

    // ===================== UI Wiring =====================

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        fullNameColumn.setCellValueFactory(cell -> {
            Employee e = cell.getValue();
            String full = safe(e.getFirstname()) + " " + safe(e.getLastname());
            return new javafx.beans.property.SimpleStringProperty(full.trim());
        });

        activeColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                boolean ok = Boolean.TRUE.equals(active);
                badge.setText(ok ? "Hoạt động" : "Không hoạt động");
                badge.setStyle(ok
                        ? "-fx-background-color:#e8f8f2; -fx-text-fill:#27ae60; -fx-padding:4 8; -fx-background-radius:6;"
                        : "-fx-background-color:#fdecea; -fx-text-fill:#c0392b; -fx-padding:4 8; -fx-background-radius:6;");
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Xử lý sự kiện click vào dòng bảng
        employeeTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Nếu là double click
                Employee selectedEmployee = employeeTableView.getSelectionModel().getSelectedItem();
                if (selectedEmployee != null) {
                    onEdit(selectedEmployee);
                }
            }
        });

        filtered = new FilteredList<>(master, e -> true);
        sorted = new SortedList<>(filtered);
        employeeTableView.setItems(sorted);
    }

    private void bindTableSorting() {
        sorted.comparatorProperty().bind(employeeTableView.comparatorProperty());
    }

    private void setupFilters() {
        // Debounce tìm kiếm
        searchDebounce.setOnFinished(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldV, newV) -> searchDebounce.playFromStart());

        roleFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applyFilters());
        statusFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applyFilters());

        // Mặc định chọn "Tất cả"
        if (roleFilterComboBox.getItems().isEmpty()) {
            roleFilterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "doctor", "nurse"));
        }
        if (statusFilterComboBox.getItems().isEmpty()) {
            statusFilterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Không hoạt động"));
        }
        roleFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.getSelectionModel().selectFirst();
    }

    // ===================== Data Loading =====================

    private void loadEmployees() {
        statusLabel.setText("Đang tải dữ liệu...");
        executeAsync(
                () -> {
                    try {
                        return service.getAllEmployee();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of(); // Return an empty list on error
                    }
                },
                list -> {
                    master.setAll((Collection<? extends Employee>) list);
                    applyFilters();
                    updateCounters((List<Employee>) list);
                    statusLabel.setText("Đã tải " + list.size() + " bản ghi");
                });
    }

    private void updateCounters(List<Employee> list) {
        long total = list.size();
        long doctors = list.stream().filter(e -> "doctor".equalsIgnoreCase(e.getRole())).count();
        long nurses = list.stream().filter(e -> "nurse".equalsIgnoreCase(e.getRole())).count();
        long activeEmployees = list.stream().filter(e -> Boolean.TRUE.equals(e.isActive())).count(); // Tính số người
        totalCountLabel.setText(String.valueOf(total));
        doctorCountLabel.setText(String.valueOf(doctors));
        nurseCountLabel.setText(String.valueOf(nurses));
        activeCountLabel.setText(String.valueOf(activeEmployees));
    }

    private void applyFilters() {
        final String kw = norm(searchField.getText());
        final String roleSel = roleFilterComboBox.getSelectionModel().getSelectedItem();
        final String statusSel = statusFilterComboBox.getSelectionModel().getSelectedItem();

        Predicate<Employee> p = e -> true;
        if (kw != null && !kw.isBlank()) {
            p = p.and(e -> containsKw(e, kw));
        }
        if (roleSel != null && !"Tất cả".equals(roleSel)) {
            p = p.and(e -> roleSel.equalsIgnoreCase(e.getRole()));
        }
        if (statusSel != null && !"Tất cả".equals(statusSel)) {
            boolean needActive = "Hoạt động".equals(statusSel);
            p = p.and(e -> Boolean.TRUE.equals(e.isActive()) == needActive);
        }
        filtered.setPredicate(p);

        int shown = filtered.size();
        statusLabel.setText("Lọc dữ liệu xong");
    }

    private boolean containsKw(Employee e, String kw) {
        String full = (safe(e.getFirstname()) + " " + safe(e.getLastname())).trim();
        return norm(e.getUsername()).contains(kw)
                || norm(e.getEmail()).contains(kw)
                || norm(e.getPhone()).contains(kw)
                || norm(full).contains(kw);
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // ===================== Button Actions =====================

    @FXML
    private void handleRefresh() {
        loadEmployees();
    }

    @FXML
    private void handleAdd() {
        showWarning(
                "Chức năng thêm nhân viên sẽ mở form riêng. Tạm thời bạn có thể gọi API createEmployee() trong service.");
    }

    private void onEdit(Employee e) {
        showWarning(
                "Sửa nhân viên #" + e.getId() + " — bạn có thể mở dialog edit và gọi updateEmployee(e) từ service.");
    }

    // ===================== Delete Action =====================

    @FXML
    private void handleDelete() {
        Employee selectedEmployee = employeeTableView.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xóa nhân viên");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn chắc chắn muốn xóa nhân viên #" + selectedEmployee.getId() + " ("
                    + selectedEmployee.getUsername() + ")?");
            confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    statusLabel.setText("Đang xóa #" + selectedEmployee.getId() + "...");
                    executeAsync(
                            () -> {
                                try {
                                    return service.deleteEmployee(selectedEmployee.getId());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    return false;
                                }
                            },
                            ok -> {
                                if (Boolean.TRUE.equals(ok)) {
                                    master.remove(selectedEmployee);
                                    applyFilters();
                                    updateCounters(master);
                                    showSuccess("Đã xóa nhân viên #" + selectedEmployee.getId());
                                } else {
                                    showError("Xóa thất bại (server trả về false)");
                                }
                                statusLabel.setText("Sẵn sàng");
                            });
                }
            });
        } else {
            showWarning("Chưa chọn nhân viên để xóa");
        }
    }

    // ===================== Hover Effects =====================
    public void onButtonHover(javafx.scene.input.MouseEvent e) {
        Node n = (Node) e.getSource();
        n.setStyle("-fx-opacity:0.9; -fx-translate-y:-1;");
    }

    public void onButtonExit(javafx.scene.input.MouseEvent e) {
        Node n = (Node) e.getSource();
        n.setStyle(""); // trở về style mặc định theo FXML/CSS
    }

    public void onTableRowClick(MouseEvent mouseEvent) {
    }
}
