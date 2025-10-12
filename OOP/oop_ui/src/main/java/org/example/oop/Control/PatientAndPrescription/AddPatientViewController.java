package org.example.oop.Control.PatientAndPrescription;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.oop.Model.PatientAndPrescription.PatientRecord;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;

public class AddPatientViewController implements Initializable
{
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private ComboBox<PatientRecord.Gender> genderComboBox;
    @FXML
    private TextField addressField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea notesArea;

    private PatientRecord newPatientRecord = null;

    public void initialize(URL url, ResourceBundle rb) {
        genderComboBox.getItems().addAll(PatientRecord.Gender.NAM, PatientRecord.Gender.NỮ , PatientRecord.Gender.KHÁC);

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
        PatientRecord.Gender gender = genderComboBox.getValue();
        String address = addressField.getText();
        String email = emailField.getText();
        String notes = notesArea.getText();

        newPatientRecord = new PatientRecord(222, name, dob, gender, address, phone, email, notes);
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    public PatientRecord getNewPatientRecord(){
        return newPatientRecord;
    }



}
