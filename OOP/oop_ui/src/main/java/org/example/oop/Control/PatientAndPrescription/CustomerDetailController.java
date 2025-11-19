package org.example.oop.Control.PatientAndPrescription;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.example.oop.Service.PrescriptionService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerDetailController {

    @FXML
    private Label customerIdLabel;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label dobLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;

    @FXML
    private TableView prescriptionsTable;
    @FXML
    private TableColumn<Prescription, String> prescriptionIdColumn;
    @FXML
    private TableColumn<Prescription, LocalDate> dateIssuedColumn;
    @FXML
    private TableColumn<Prescription, String> chiefComplaintColumn;
    @FXML
    private TableColumn<Prescription, String> diagnosisColumn;


    @FXML
    private Button editCustomerButton;


    @FXML
    public void initialize() {
        Customer customer = SceneManager.getSceneData("accountData");
        customerIdLabel.setText(String.valueOf(customer.getId()));
        fullNameLabel.setText(customer.getFullName());
        genderLabel.setText(customer.getGender().toString());
        dobLabel.setText(customer.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        phoneLabel.setText(customer.getPhone());
        emailLabel.setText(customer.getEmail());
        prescriptionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateIssuedColumn.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        dateIssuedColumn.setCellFactory(column -> new TableCell<Prescription, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        chiefComplaintColumn.setCellValueFactory(new PropertyValueFactory<>("chiefComplaint"));
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        setupTableColumns();
    }





    private void setupTableColumns() {
        PrescriptionService prescriptionService = new PrescriptionService();
        System.out.println("Loading prescriptions for customer ID: " + customerIdLabel.getText());
        prescriptionService.getPrescriptionByCustomer_idAsync( prescriptions -> {;
            prescriptionsTable.setItems(FXCollections.observableList(prescriptions));


        }, error -> {
            // Xử lý lỗi nếu có
            System.err.println("Error retrieving prescriptions: ");
            CustomerHubController.showErrorAlert("Error retrieving prescriptions", error);
        }, Integer.parseInt(customerIdLabel.getText()));

    }

    private void loadPrescriptionHistory() {
        SceneManager.reloadCurrentScene();

    }

    @FXML
    private void handleBackButton() {
        SceneManager.goBack();
    }

    @FXML
    private void handleReloadButton() {
        loadPrescriptionHistory();
        System.out.println("🔄 Đã tải lại dữ liệu");
    }

    @FXML
    private void handleEditCustomer() {
        SceneManager.openModalWindow(SceneConfig.ADD_CUSTOMER_VIEW_FXML, SceneConfig.Titles.ADD_CUSTOMER, null);

    }
    @FXML
    private void handleChangePassword(){
        SceneManager.openModalWindow(SceneConfig.CHANGE_PASSWORD_FXML, SceneConfig.Titles.CHANGE_PASSWORD, null);
    }



}
