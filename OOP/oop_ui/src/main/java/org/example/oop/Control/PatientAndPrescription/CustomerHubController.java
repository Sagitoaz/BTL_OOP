package org.example.oop.Control.PatientAndPrescription;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.oop.Service.PrescriptionService;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.miniboot.app.domain.models.UserRole;
import org.example.oop.Control.BaseController;

import java.util.concurrent.CompletableFuture;

public class CustomerHubController extends BaseController implements Initializable {

    private List<Customer> allCustomers = new ArrayList<>(); //Khởi tạo để tránh NullPointerException
    @FXML
    private ListView<Customer> customerListView;

    private ObservableList<Customer> customerRecordsList;

    private Map<Integer, List<Prescription>> cachedPrescriptions;

    private int MAX_CACHE_SIZE = 10;
    // Selection mode & callback
    private boolean selectionMode = false;
    private Consumer<Customer> onCustomerSelectedCallback;
    private Customer selectedCustomerForCallback;

    @FXML
    private TableView<Prescription> examHistoryTable;

    //  LOADING STATUS 
    @FXML
    private HBox loadingStatusContainer;
    @FXML
    private ProgressIndicator statusProgressIndicator;
    @FXML
    private Label loadingStatusLabel;

    private ObservableList<Prescription> prescriptionRecordsList;

    @FXML
    private TableColumn<Prescription, String> signedAtCollumn;

    @FXML
    private TableColumn<Prescription, Integer> appointmentIdCollumn;

    @FXML
    private TableColumn<Prescription, String> chiefComplaintCollumn;

    @FXML
    private TableColumn<Prescription, String> diagnosisCollumn;

    @FXML
    private ComboBox<Customer.Gender> genderFilter;
    @FXML
    private DatePicker dobFromPicker;
    @FXML
    private DatePicker dobToPicker;
    @FXML
    private TextField searchField;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button resetFilterButton;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerIdValueLabel;
    @FXML
    private Label customerDobValueLabel;
    @FXML
    private Label customerGenderValueLabel;
    @FXML
    private Label customerAgeValueLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea notesArea;

    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button reloadButton;
    @FXML
    private Button editCustomerButton;
    @FXML
    private Button exportButton;

    private PrescriptionService prescriptionService;
    private CompletableFuture<Void> currentPrescriptionTask;
    private boolean isInitializing = true; //Flag để tránh load prescription khi khởi tạo

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if ((UserRole) SceneManager.getSceneData("role") != UserRole.ADMIN) {
            editCustomerButton.setDisable(true);
        }
        if (SceneManager.getSceneData("isModal") != null) {
            backButton.setDisable(true);
            forwardButton.setDisable(true);
            SceneManager.removeSceneData("isModal");
        }
        cachedPrescriptions = new HashMap<>();
        prescriptionService = new PrescriptionService();
        customerRecordsList = FXCollections.observableArrayList();
        prescriptionRecordsList = FXCollections.observableArrayList();

        // Setup TableView columns for Prescription
        setupPrescriptionTable();

        // Setup listener cho selection - NHƯNG chỉ active sau khi load xong
        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setCurrentCustomer(newValue);
            //Chỉ load prescriptions khi KHÔNG phải lúc khởi tạo
            if (!isInitializing) {
                loadPrescriptionsForCustomer(newValue);
            }
        });

        loadCustomerData();

        // Setup gender filter với promptText
        genderFilter.getItems().addAll(Customer.Gender.values());
        genderFilter.setPromptText("Lọc theo giới tính");
        genderFilter.setButtonCell(new ListCell<Customer.Gender>() {
            @Override
            protected void updateItem(Customer.Gender item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(genderFilter.getPromptText());
                } else {
                    setText(item.toString());
                }
            }
        });
        examHistoryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                Prescription selectedPrescription = examHistoryTable.getSelectionModel().getSelectedItem();
                if (selectedPrescription != null) {

                    SceneManager.removeSceneData("prescription");
                    SceneManager.removeSceneData("nameCustomer");
                    SceneManager.setSceneData("prescription", selectedPrescription);
                    SceneManager.setSceneData("nameCustomer", customerNameLabel.getText());
                    SceneManager.openModalWindow(SceneConfig.PRESCRIPTION_EDITOR_FXML,
                            SceneConfig.Titles.PRESCRIPTION_EDITOR, () -> {
                                // Reload prescriptions after closing editor
                                SceneManager.removeSceneData("prescription");
                                SceneManager.removeSceneData("nameCustomer");
                                if (SceneManager.getSceneData("updatedPrescription") != null) {
                                    Prescription updatedPrescription = (Prescription) SceneManager
                                            .getSceneData("updatedPrescription");
                                    // Cập nhật lại trong bảng
                                    int index = prescriptionRecordsList.indexOf(selectedPrescription);
                                    if (index >= 0) {
                                        prescriptionRecordsList.set(index, updatedPrescription);
                                    }
                                    SceneManager.removeSceneData("updatedPrescription");

                                }
                            });
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
        SceneManager.reloadCurrentScene();
    }

    /**
     * Enable selection mode - khi đóng dialog sẽ trả về selected customer
     * 
     * @param callback Function sẽ được gọi với selected customer
     */
    public void setSelectionMode(Consumer<Customer> callback) {
        this.selectionMode = true;
        this.onCustomerSelectedCallback = callback;
        System.out.println("✅ CustomerHub: Selection mode enabled");
    }

    /**
     * Get selected customer (for manual retrieval)
     */
    public Customer getSelectedCustomer() {
        return selectedCustomerForCallback;
    }

    private void setupPrescriptionTable() {
        // Setup các cột cho bảng prescription
        signedAtCollumn.setCellValueFactory(cellData -> {
            LocalDate signedAt = cellData.getValue().getSignedAt();
            if (signedAt != null) {
                return new SimpleStringProperty(signedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                return new SimpleStringProperty("Chưa ký");
            }
        });

        appointmentIdCollumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getAppointmentId()).asObject());

        chiefComplaintCollumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getChiefComplaint() != null ? cellData.getValue().getChiefComplaint() : ""));

        diagnosisCollumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDiagnosis() != null ? cellData.getValue().getDiagnosis() : ""));

        // Set items cho table
        examHistoryTable.setItems(prescriptionRecordsList);
    }

    private void setCurrentListCustomer() {
        customerListView.setItems(customerRecordsList);
    }

    private void loadCustomerData() {
        showLoadingStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                "⏳ Đang tải dữ liệu bệnh nhân...");
        CustomerRecordService.getInstance().getAllCustomersAsync(
                customers -> {
                    // SUCCESS callback - chạy trong UI Thread
                    customerRecordsList.clear();
                    allCustomers = customers;
                    customerRecordsList.addAll(customers);
                    setCurrentListCustomer();

                    //Đánh dấu hoàn thành khởi tạo - bây giờ cho phép load prescriptions
                    isInitializing = false;
                    showSuccessStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "✅ Tải thành công " + customers.size() + " bệnh nhân!");

                    System.out.println("✅ Loaded " + customers.size() + " customers - Ready for user interaction");
                },
                error -> {
                    // ERROR callback - handle error gracefully
                    System.err.println("❌ Error loading customers: " + error);
                    isInitializing = false; //Vẫn set false ngay cả khi lỗi
                    showErrorStatus(loadingStatusContainer, statusProgressIndicator, loadingStatusLabel,
                            "❌ Lỗi: " + error);
                    // Có thể show alert nếu cần
                    showErrorAlert("Lỗi tải dữ liệu", error);
                });
    }

    private void loadPrescriptionsForCustomer(Customer customer) {
        // Cancel previous task if still running
        if (currentPrescriptionTask != null && !currentPrescriptionTask.isDone()) {
            currentPrescriptionTask.cancel(true);
            System.out.println("🔄 Cancelled previous prescription loading task");
        }

        if (customer == null) {
            prescriptionRecordsList.clear();
            return;
        }
        if (cachedPrescriptions.containsKey(customer.getId())) {
            prescriptionRecordsList.clear();
            prescriptionRecordsList.addAll(cachedPrescriptions.get(customer.getId()));
            if (prescriptionRecordsList.size() > MAX_CACHE_SIZE) {
                // Simple cache eviction: remove oldest entry
                Integer firstKey = cachedPrescriptions.keySet().iterator().next();
                cachedPrescriptions.remove(firstKey);

            }
            return;
        }

        // Create new async task
        currentPrescriptionTask = CompletableFuture.runAsync(() -> {
            try {
                // Check if task was cancelled before starting
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                List<Prescription> prescriptions = prescriptionService.getPrescriptionByCustomer_id(customer.getId())
                        .getData();

                // Check if task was cancelled before updating UI
                if (!Thread.currentThread().isInterrupted() && !currentPrescriptionTask.isCancelled()) {
                    Platform.runLater(() -> {
                        prescriptionRecordsList.clear();
                        if (prescriptions != null) {
                            prescriptionRecordsList.addAll(prescriptions);
                            cachedPrescriptions.put(customer.getId(), prescriptions);
                        } else {
                            cachedPrescriptions.put(customer.getId(), new ArrayList<>());
                        }

                    });
                }
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted() && !currentPrescriptionTask.isCancelled()) {
                    Platform.runLater(() -> {
                        prescriptionRecordsList.clear();
                        System.err.println("❌ Exception loading prescriptions: " + e.getMessage());
                    });
                }
            }
        });
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = searchField.getText();
        Customer.Gender gender = genderFilter.getValue();
        LocalDate dobFrom = dobFromPicker.getValue();
        LocalDate dobTo = dobToPicker.getValue();

        customerRecordsList.clear();
        for (Customer customer : allCustomers) {
            boolean matches = true;

            // Filter by search text
            if (search != null && !search.isEmpty()) {
                String lowerSearch = search.toLowerCase();
                if (!(customer.getFirstname().toLowerCase().contains(lowerSearch) ||
                        customer.getLastname().toLowerCase().contains(lowerSearch) ||
                        (customer.getPhone() != null && customer.getPhone().equalsIgnoreCase(lowerSearch)))) {
                    matches = false;
                    try {
                        Integer searchId = Integer.parseInt(search);
                        if (customer.getId() != searchId) {
                            matches = false;
                        } else {
                            matches = true;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore parse error
                    }
                }
            }
            if (gender != null && customer.getGender() != gender) {
                matches = false;
            }
            if (dobFrom != null && (customer.getDob() == null || customer.getDob().isBefore(dobFrom))) {
                matches = false;
            }
            if (dobTo != null && (customer.getDob() == null || customer.getDob().isAfter(dobTo))) {
                matches = false;
            }
            if (matches) {
                customerRecordsList.add(customer);
            }
        }
        setCurrentListCustomer();
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        searchField.clear();

        // Reset genderFilter đúng cách
        genderFilter.setValue(null);
        genderFilter.getSelectionModel().clearSelection();

        dobFromPicker.setValue(null);
        dobToPicker.setValue(null);

        // Reload all data
        customerRecordsList.clear();
        customerRecordsList.addAll(allCustomers);
        setCurrentListCustomer();
    }

    private void setCurrentCustomer(Customer pr) {
        // Store selected customer for callback
        this.selectedCustomerForCallback = pr;

        // Trigger callback nếu đang ở selection mode
        if (selectionMode && pr != null && onCustomerSelectedCallback != null) {
            System.out.println(
                    "✅ Customer selected in selection mode: " + pr.getFullName() + " (ID: " + pr.getId() + ")");
        }

        if (pr == null) {
            customerNameLabel.setText("[CHỌN BỆNH NHÂN]");
            customerIdValueLabel.setText("...");
            customerDobValueLabel.setText("...");
            customerGenderValueLabel.setText("...");
            customerAgeValueLabel.setText("...");
            addressField.setText("");
            phoneField.setText("");
            emailField.setText("");
            notesArea.setText("");
        } else {
            customerNameLabel.setText(pr.getFullName());
            customerIdValueLabel.setText(String.valueOf(pr.getId()));
            customerDobValueLabel.setText(pr.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            customerGenderValueLabel.setText(pr.getGender().name());

            // Calculate age from date of birth
            int age = pr.getDob() != null ? LocalDate.now().getYear() - pr.getDob().getYear() : 0;
            customerAgeValueLabel.setText(String.valueOf(age));

            addressField.setText(pr.getAddress() != null ? pr.getAddress() : "");
            phoneField.setText(pr.getPhone() != null ? pr.getPhone() : "");
            emailField.setText(pr.getEmail() != null ? pr.getEmail() : "");
            notesArea.setText(pr.getNote() != null ? pr.getNote() : "");
        }
    }

    @FXML
    private void onAddCustomerButton() {

        SceneManager.openModalWindow(SceneConfig.ADD_CUSTOMER_VIEW_FXML, SceneConfig.Titles.ADD_CUSTOMER, null);

        if (SceneManager.getSceneData("newCustomer") != null) {
            Customer newCustomer = (Customer) SceneManager.getSceneData("newCustomer");
            customerRecordsList.add(newCustomer);
            setCurrentListCustomer();
            SceneManager.removeSceneData("newCustomer");

        }

    }

    @FXML
    private void handleEditCustomer(ActionEvent event) {

        if (customerNameLabel.getText().equalsIgnoreCase("[CHỌN BỆNH NHÂN]")) {
            showErrorAlert("Cảnh báo", "Vui lòng chọn bệnh nhân trước khi Chỉnh sửa bệnh nhân");
            return;
        }
        Customer selectedCustomer = customerListView.getSelectionModel().getSelectedItem();
        SceneManager.removeSceneData("selectedCustomer");
        SceneManager.setSceneData("selectedCustomer", selectedCustomer);
        SceneManager.openModalWindow(SceneConfig.ADD_CUSTOMER_VIEW_FXML, SceneConfig.Titles.ADD_CUSTOMER, null);

        if (SceneManager.getSceneData("updatedCustomer") != null) {
            int selectedIndex = customerListView.getSelectionModel().getSelectedIndex();
            Customer updatedPatient = (Customer) SceneManager.getSceneData("updatedCustomer");
            setCurrentCustomer(updatedPatient);
            customerRecordsList.set(selectedIndex, updatedPatient);
            setCurrentListCustomer();
            SceneManager.removeSceneData("updatedCustomer");
        }

    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Cleanup khi controller bị destroy
    public void cleanup() {
        if (currentPrescriptionTask != null && !currentPrescriptionTask.isDone()) {
            currentPrescriptionTask.cancel(true);
        }
    }
    
    @FXML
    private void onExportButton() {
        try {
            if (customerRecordsList == null || customerRecordsList.isEmpty()) {
                showWarning("Không có dữ liệu để xuất!");
                return;
            }
            
            // Prepare headers
            java.util.List<String> headers = java.util.Arrays.asList(
                "ID", "Họ tên", "Ngày sinh", "Giới tính", 
                "Tuổi", "Địa chỉ", "Điện thoại", "Email", "Ghi chú"
            );
            
            // Prepare data
            java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>();
            for (Customer customer : customerRecordsList) {
                int age = customer.getDob() != null ? 
                    java.time.Period.between(customer.getDob(), LocalDate.now()).getYears() : 0;
                    
                java.util.List<Object> row = java.util.Arrays.asList(
                    customer.getId(),
                    customer.getFullName(),
                    customer.getDob() != null ? customer.getDob() : "",
                    customer.getGender() != null ? customer.getGender().toString() : "",
                    age,
                    customer.getAddress() != null ? customer.getAddress() : "",
                    customer.getPhone() != null ? customer.getPhone() : "",
                    customer.getEmail() != null ? customer.getEmail() : "",
                    customer.getNote() != null ? customer.getNote() : ""
                );
                data.add(row);
            }
            
            // Generate filename and path
            String directory = org.example.oop.Utils.ExcelExporter.getDocumentsPath();
            org.example.oop.Utils.ExcelExporter.ensureDirectoryExists(directory);
            String fileName = org.example.oop.Utils.ExcelExporter.generateFileName("DanhSachKhachHang");
            String fullPath = directory + fileName;
            
            // Export to Excel
            org.example.oop.Utils.ExcelExporter.exportToFile(fullPath, "Danh sách khách hàng", headers, data);
            
            showSuccess("Đã xuất danh sách khách hàng ra file:\n" + fileName + "\n\nVị trí: " + fullPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xuất file: " + e.getMessage());
        }
    }

}
