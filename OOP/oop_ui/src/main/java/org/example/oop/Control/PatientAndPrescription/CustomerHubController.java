package org.example.oop.Control.PatientAndPrescription;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Data.models.Customer;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CustomerHubController implements Initializable  {

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
    //--Tổng quan--
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

        // Thêm dữ liệu mẫu


        customerListView.setItems(customerRecordsList);
        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {;
            setCurrentCustomer(newValue);

        });
        genderFilter.getItems().addAll(Customer.Gender.NAM, Customer.Gender.NỮ , Customer.Gender.KHÁC);

    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = searchField.getText();
        Customer.Gender gender = genderFilter.getValue();
        LocalDate dobFrom = dobFromPicker.getValue();
        LocalDate dobTo = dobToPicker.getValue();
        customerRecordsList.clear();
        // Loc filter
        customerListView.setItems(customerRecordsList);
    }
    @FXML
    private void resetFilters(ActionEvent event) {
        searchField.clear();
        genderFilter.setValue(null);
        genderFilter.setPromptText("Lọc theo giới tính");
        dobFromPicker.setValue(null);
        dobToPicker.setValue(null);
        customerRecordsList.clear();
        //Add find all
        customerListView.setItems(customerRecordsList);
    }

    private void setCurrentCustomer(Customer pr){
        if(pr == null){
            customerNameLabel.setText("[CHỌN BỆNH NHÂN]");
            customerIdValueLabel.setText("...");
            customerDobValueLabel.setText("...");
            customerGenderValueLabel.setText("...");
            customerAgeValueLabel.setText("...");
            addressField.setText("");
            phoneField.setText("");
            emailField.setText("");
            notesArea.setText("");


        }
        else{
            customerNameLabel.setText(pr.getNamePatient());
            customerIdValueLabel.setText(String.valueOf(pr.getId()));
            customerDobValueLabel.setText(pr.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            customerGenderValueLabel.setText(pr.getGender().name());
            customerAgeValueLabel.setText(String.valueOf(pr.getAge()));
            addressField.setText(pr.getAddress());
            phoneField.setText(pr.getPhoneNumber());
            emailField.setText(pr.getEmail());
            notesArea.setText(pr.getNotes());
        }

    }

    @FXML
    private void onAddCustomerButton(ActionEvent event)throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/AddCustomerView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Thêm Bệnh Nhân Mới");
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
            stage.showAndWait();
            AddCustomerViewController controller = loader.getController();
            Customer newPatient = controller.getNewPatientRecord();
            addCustomerRecord(newPatient);

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    public void addCustomerRecord(Customer pr){
        if(pr == null ) return;
        //goi api save
        customerRecordsList.add(pr);
        customerListView.setItems(customerRecordsList);
    }

    @FXML
    private void onAddNewPrescription(ActionEvent event)throws IOException {
        try{
            // Kiem tra xem co dang tro den benh nhan nao khong
            if(customerNameLabel.getText().equalsIgnoreCase("[CHỌN BỆNH NHÂN]")){
                return;
            }
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/PrescriptionEditor.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Đơn khám bệnh nhân");
            stage.setScene(new Scene(loader.load()));
            PrescriptionEditorController prescriptionEditorController = loader.getController();
            prescriptionEditorController.initData(customerNameLabel.getText(), Integer.parseInt( customerIdValueLabel.getText()));
            stage.centerOnScreen();
            stage.showAndWait();
        }
        catch (IOException e) {
            e.printStackTrace();

        }
    }


}
