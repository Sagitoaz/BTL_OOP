package org.example.oop.Control.PatientAndPrescription;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.oop.Service.CustomerRecordService;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

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

    private Customer curCustomer = null;

    public void initialize(URL url, ResourceBundle rb) {
        genderComboBox.getItems().addAll(Customer.Gender.values());
        curCustomer = null;


    }



    public void initData(Customer customer){
        curCustomer = customer;
        nameField.setText(curCustomer.getFirstname() + " " + curCustomer.getLastname());
        phoneField.setText(curCustomer.getPhone());
        dobPicker.setValue(curCustomer.getDob());
        genderComboBox.setValue(curCustomer.getGender());
        addressField.setText(curCustomer.getAddress());
        emailField.setText(curCustomer.getEmail());
        notesArea.setText(curCustomer.getNote());
    }


    @FXML
    private void onDeleteButton(ActionEvent event){
        Stage stage = (Stage) nameField.getScene().getWindow();
        if(curCustomer != null){
            deleteCustomerRecord(curCustomer);

        }
        stage.close();
    }
    @FXML
    private void onSaveAndCloseButton(ActionEvent event){
        if(curCustomer == null){
            curCustomer = new Customer();
        }
        String name = nameField.getText();
        String phone = phoneField.getText();
        LocalDate dob = dobPicker.getValue();
        Customer.Gender gender = genderComboBox.getValue();
        String address = addressField.getText();
        String email = emailField.getText();
        String notes = notesArea.getText();

        // Tách tên thành firstName và lastName
        String[] nameParts = name.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        curCustomer.setFirstname(firstName);
        curCustomer.setLastname(lastName);
        curCustomer.setPhone(phone);
        curCustomer.setDob(dob);
        curCustomer.setGender(gender);
        curCustomer.setAddress(address);
        curCustomer.setEmail(email);
        curCustomer.setNote(notes);
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    public Customer getCurCustomer(){
        return curCustomer;
    }
    private void deleteCustomerRecord(Customer pr) {
        if (pr == null) return;
        CustomerRecordService.getInstance().deleteCustomer(pr.getId());
    }



}
