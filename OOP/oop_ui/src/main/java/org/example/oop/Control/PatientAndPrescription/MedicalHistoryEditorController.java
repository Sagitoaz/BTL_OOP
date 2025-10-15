package org.example.oop.Control.PatientAndPrescription;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.oop.Model.PatientAndPrescription.MedicalHistory;
import org.example.oop.Model.PatientAndPrescription.SpectaclePrescription;

public class MedicalHistoryEditorController implements Initializable {

    @FXML
    private TextField patientNameField;
    @FXML
    private TextField conditionField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<MedicalHistory.Status>statusComboBox;
    @FXML
    private TextArea notesArea;


    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle resourceBundle) {
        statusComboBox.getItems().addAll(MedicalHistory.Status.values());
    }

}
