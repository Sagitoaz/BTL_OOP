package org.example.oop.Control.PatientAndPrescription;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.scene.control.*;
import org.example.oop.Service.CustomerRecordService;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomerHubController implements Initializable {

    @FXML
    private ListView<Customer> customerListView;

    private ObservableList<Customer> customerRecordsList;
    
    // Selection mode & callback
    private boolean selectionMode = false;
    private Consumer<Customer> onCustomerSelectedCallback;
    private Customer selectedCustomerForCallback;

    @FXML
    private TableView<Prescription> examHistoryTable;

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

    private PrescriptionService prescriptionService;
    private CompletableFuture<Void> currentPrescriptionTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        prescriptionService = new PrescriptionService();
        customerRecordsList = FXCollections.observableArrayList();
        prescriptionRecordsList = FXCollections.observableArrayList();

        // Setup TableView columns for Prescription
        setupPrescriptionTable();

        loadCustomerData();
        // Setup listener cho selection
        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setCurrentCustomer(newValue);
            loadPrescriptionsForCustomer(newValue);
        });

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
    }
    
    /**
     * Enable selection mode - khi đóng dialog sẽ trả về selected customer
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

        appointmentIdCollumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getAppointmentId()).asObject());

        chiefComplaintCollumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getChiefComplaint() != null ?
                cellData.getValue().getChiefComplaint() : ""));

        diagnosisCollumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDiagnosis() != null ?
                cellData.getValue().getDiagnosis() : ""));

        // Set items cho table
        examHistoryTable.setItems(prescriptionRecordsList);
    }

    private void setCurrentListCustomer() {
        customerListView.setItems(customerRecordsList);
    }
    private void loadCustomerData() {
        CustomerRecordService.getInstance().getAllCustomersAsync(
            customers -> {
                // SUCCESS callback - chạy trong UI Thread
                customerRecordsList.clear();
                customerRecordsList.addAll(customers);
                setCurrentListCustomer();
                System.out.println("✅ Loaded " + customers.size() + " customers");
            },
            error -> {
                // ERROR callback - handle error gracefully
                System.err.println("❌ Error loading customers: " + error);
                // Có thể show alert nếu cần
                showErrorAlert("Lỗi tải dữ liệu", error);
            }
        );
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

        // Create new async task
        currentPrescriptionTask = CompletableFuture.runAsync(() -> {
            try {
                // Check if task was cancelled before starting
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                List<Prescription> prescriptions = prescriptionService.getPrescriptionByCustomer_id(customer.getId()).getData();

                // Check if task was cancelled before updating UI
                if (!Thread.currentThread().isInterrupted() && !currentPrescriptionTask.isCancelled()) {
                    Platform.runLater(() -> {
                        prescriptionRecordsList.clear();

                        prescriptionRecordsList.addAll(prescriptions);


                        System.out.println("✅ Loaded " + prescriptions.size() + " prescriptions for customer: " + customer.getFullName());
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

        CustomerRecordService.getInstance().searchCustomersAsync(
            search,
            gender,
            dobFrom,
            dobTo,
            customers -> {
                customerRecordsList.clear();
                customerRecordsList.addAll(customers);
                setCurrentListCustomer();
            },
            error -> {
                System.err.println("❌ Error searching customers: " + error);
                showErrorAlert("Lỗi tìm kiếm", error);
            }
        );
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
        loadCustomerData();
    }

    private void setCurrentCustomer(Customer pr) {
        // Store selected customer for callback
        this.selectedCustomerForCallback = pr;
        
        // Trigger callback nếu đang ở selection mode
        if (selectionMode && pr != null && onCustomerSelectedCallback != null) {
            System.out.println("✅ Customer selected in selection mode: " + pr.getFullName() + " (ID: " + pr.getId() + ")");
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
    private void onAddCustomerButton(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/AddCustomerView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Thêm Bệnh Nhân Mới");
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
            stage.showAndWait();
            AddCustomerViewController controller = loader.getController();
            Customer newCustomer = controller.getCurCustomer();
            customerRecordsList.add(newCustomer);
            setCurrentListCustomer();

        } catch (IOException e) {
            System.err.println("Error opening Add Customer dialog: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể mở cửa sổ thêm bệnh nhân: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditCustomer(ActionEvent event) {
        try {
            if (customerNameLabel.getText().equalsIgnoreCase("[CHỌN BỆNH NHÂN]")) {
                showErrorAlert("Cảnh báo", "Vui lòng chọn bệnh nhân trước khi Chỉnh sửa bệnh nhân");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/AddCustomerView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa Bệnh Nhân ");
            stage.setScene(new Scene(loader.load()));
            AddCustomerViewController controller = loader.getController();

            controller.initData(customerListView.getSelectionModel().getSelectedItem());
            stage.centerOnScreen();
            stage.showAndWait();
            Customer updatedPatient = controller.getCurCustomer();
            if(updatedPatient != null){
                int selectedIndex = customerListView.getSelectionModel().getSelectedIndex();
                customerRecordsList.set(selectedIndex, updatedPatient);
                setCurrentListCustomer();
            }


        } catch (IOException e) {
            System.err.println("Error opening Add Customer dialog: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể mở cửa sổ thêm bệnh nhân: " + e.getMessage());
        }
    }


    @FXML
    private void onAddNewPrescription(ActionEvent event) {
        try {
            // Kiểm tra xem có đang trỏ đến bệnh nhân nào không
            if (customerNameLabel.getText().equalsIgnoreCase("[CHỌN BỆNH NHÂN]")) {
                showErrorAlert("Cảnh báo", "Vui lòng chọn bệnh nhân trước khi tạo đơn thuốc");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/PrescriptionEditor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Đơn khám bệnh nhân");
            stage.setScene(new Scene(loader.load()));
            PrescriptionEditorController prescriptionEditorController = loader.getController();
            prescriptionEditorController.initData(customerNameLabel.getText(), Integer.parseInt(customerIdValueLabel.getText()), null);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error opening Prescription Editor: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể mở cửa sổ đơn thuốc: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditPrescription(){

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

}
