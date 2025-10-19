package org.example.oop.Control.PatientAndPrescription;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.miniboot.app.domain.models.Customer;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddCustomerViewController implements Initializable
{
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private ComboBox<Customer.Gender> genderComboBox;
    @FXML
    private TextField addressField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea notesArea;

    private Customer newPatientRecord = null;

    public void initialize(URL url, ResourceBundle rb) {
        genderComboBox.getItems().addAll(Customer.Gender.values());

    }


    @FXML
    private void onCancelButton(ActionEvent event){
        Stage stage = (Stage) nameField.getScene().getWindow();
        newPatientRecord = null;
        stage.close();
    }
    @FXML
    private void onSaveAndCloseButton(ActionEvent event){
        String name = nameField.getText();
        String phone = phoneField.getText();
        LocalDate dob = dobPicker.getValue();
        Customer.Gender gender = genderComboBox.getValue();
        String address = addressField.getText();
        String email = emailField.getText();
        String notes = notesArea.getText();

        //newPatientRecord = new Customer(222, name, dob, gender, address, phone, email, notes);
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    public Customer getNewPatientRecord(){
        return newPatientRecord;
    }



}
