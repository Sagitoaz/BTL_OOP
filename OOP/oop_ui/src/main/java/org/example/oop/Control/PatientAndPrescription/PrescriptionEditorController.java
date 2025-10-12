package org.example.oop.Control.PatientAndPrescription;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.oop.Model.PatientAndPrescription.SpectaclePrescription;
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
    private ComboBox<SpectaclePrescription.Base> baseOsCombo;
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
    private ComboBox<SpectaclePrescription.Base> baseOdCombo;
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
    private ComboBox<SpectaclePrescription.Lens_Type> lensTypeCombo;
    @FXML
    private CheckBox arCheck;
    @FXML
    private CheckBox blueLightCheck;
    @FXML
    private CheckBox uvCheck;
    @FXML
    private CheckBox photochromicCheck;
    @FXML
    private ComboBox<SpectaclePrescription.Material> materialCombo;

    //--Thong tin kham--
    @FXML
    private TextArea assessmentArea;
    @FXML
    private TextArea planArea;
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle resourceBundle) {
        baseOdCombo.getItems().addAll(SpectaclePrescription.Base.UP, SpectaclePrescription.Base.DOWN, SpectaclePrescription.Base.IN,
                SpectaclePrescription.Base.OUT, SpectaclePrescription.Base.NONE);
        baseOsCombo.getItems().addAll(SpectaclePrescription.Base.UP, SpectaclePrescription.Base.DOWN, SpectaclePrescription.Base.IN,
                SpectaclePrescription.Base.OUT, SpectaclePrescription.Base.NONE);
        lensTypeCombo.getItems().addAll(SpectaclePrescription.Lens_Type.values());
        materialCombo.getItems().addAll(SpectaclePrescription.Material.values());
    }
    public void initData(String patientName, int idPatient){
        patientNameField.setText(patientName);
        this.idPatient = idPatient;
        System.out.println(idPatient);
    }


}
