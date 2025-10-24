package org.example.oop.Control.PatientAndPrescription;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Services.PatientAndPrescription.CustomerRecordService;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CustomerHubController implements Initializable {

    @FXML
    private ListView<Customer> customerListView;

    private ObservableList<Customer> customerRecordsList;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        customerRecordsList = FXCollections.observableArrayList();
        loadCustomerData();
        // Setup listener cho selection
        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setCurrentCustomer(newValue);
        });

        // Setup gender filter với promptText
        genderFilter.getItems().addAll(Customer.Gender.values());
        genderFilter.setPromptText("Lọc theo giới tính");
        genderFilter.setButtonCell(new ListCell<Customer.Gender>() {
            @Override
            protected void updateItem(Customer.Gender item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    // Nếu không có item nào được chọn, hiển thị prompt text
                    setText(genderFilter.getPromptText());
                } else {
                    // Ngược lại, hiển thị tên của item
                    setText(item.toString());
                }
            }
        });

        // Load data bất đồng bộ để tránh chặn UI
        //loadCustomerData();
    }

    private void loadCustomerData() {
        CustomerRecordService.getInstance().getAllCustomersAsync(
            customers -> {
                // SUCCESS callback - chạy trong UI Thread
                customerRecordsList.clear();
                customerRecordsList.addAll(customers);
                customerListView.setItems(customerRecordsList);
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
                customerListView.setItems(customerRecordsList);
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
            addCustomerRecord(newCustomer);

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
            updateCustomerRecord(updatedPatient);

        } catch (IOException e) {
            System.err.println("Error opening Add Customer dialog: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể mở cửa sổ thêm bệnh nhân: " + e.getMessage());
        }
    }

    private void addCustomerRecord(Customer pr) {
        if (pr == null) return;

        CustomerRecordService.getInstance().createCustomerAsync(pr,
            createdCustomer -> {

                System.out.println("✅ Customer created successfully: " + createdCustomer.getFullName());
                Platform.runLater(() -> {

                });
            },
            error -> {
                // ERROR callback
                System.err.println("❌ Error creating customer: " + error);
                Platform.runLater(() -> {
                    showErrorAlert("Lỗi tạo bệnh nhân", "Không thể tạo bệnh nhân: " + error);
                });
            }
        );
    }
    private void updateCustomerRecord(Customer pr) {
        if (pr == null) return;
        System.out.println(pr.getId());

        CustomerRecordService.getInstance().updateCustomerAsync(pr,
                updatedCustomer -> {

                    System.out.println("✅ Customer updated successfully: " + updatedCustomer.getFullName());
                    Platform.runLater(() -> {

                    });
                },
                error -> {
                    // ERROR callback

                    System.err.println("❌ Error updating customer: " + error);
                    Platform.runLater(() -> {
                        showErrorAlert("Lỗi update bệnh nhân", "Không thể update bệnh nhân: " + error);
                    });
                }
        );
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
            prescriptionEditorController.initData(customerNameLabel.getText(), Integer.parseInt(customerIdValueLabel.getText()));
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
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
