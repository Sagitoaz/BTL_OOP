package org.example.oop.Control.PatientAndPrescription;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import org.w3c.dom.Text;

public class PrescriptionEditorController implements Initializable {

    //--Thong tin chinnh--
    private int idPatient;
    @FXML
    private TextField patientNameField;
    @FXML
    private  TextField doctorNameField;
    @FXML
    private DatePicker examDatePicker;
    @FXML
    private TextField chiefComplaintField;
    @FXML
    private TextArea refractionNotesArea;
    //--Thong so kham mat--
    //--Thong so mat trai
    @FXML
    private TextField sphOsField;
    @FXML
    private TextField cylOsField;
    @FXML
    private TextField axisOsField;
    @FXML
    private TextField vaOsField;
    @FXML
    private TextField prismOsField;
    @FXML
    private ComboBox<Prescription.Base> baseOsCombo;
    @FXML
    private TextField addOsField;
    //--Thong so mat phai
    @FXML
    private TextField sphOdField;
    @FXML
    private TextField cylOdField;
    @FXML
    private TextField axisOdField;
    @FXML
    private TextField vaOdField;
    @FXML
    private TextField prismOdField;
    @FXML
    private ComboBox<Prescription.Base> baseOdCombo;
    @FXML
    private TextField addOdField;

    //Thong so chung
    @FXML
    private TextField pdField;
    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private TextArea prescriptionNotesArea;
    @FXML
    private ComboBox<Prescription.Lens_Type> lensTypeCombo;
    @FXML
    private CheckBox arCheck;
    @FXML
    private CheckBox blueLightCheck;
    @FXML
    private CheckBox uvCheck;
    @FXML
    private CheckBox photochromicCheck;
    @FXML
    private ComboBox<Prescription.Material> materialCombo;

    //--Thong tin kham--
    @FXML
    private TextArea assessmentArea;
    @FXML
    private TextArea planArea;
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle resourceBundle) {
        baseOdCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        baseOsCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        lensTypeCombo.getItems().addAll(Prescription.Lens_Type.values());
        materialCombo.getItems().addAll(Prescription.Material.values());
    }
    public void initData(String patientName, int idPatient){
        patientNameField.setText(patientName);
        this.idPatient = idPatient;
        System.out.println(idPatient);
    }


}
