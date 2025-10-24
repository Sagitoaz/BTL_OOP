package org.example.oop.Control.PatientAndPrescription;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.example.oop.Service.PrescriptionService;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;

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

    private Prescription currentPrescription;
    private PrescriptionService prescriptionService;
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle resourceBundle) {
        prescriptionService = new PrescriptionService();
        currentPrescription = new Prescription();
        baseOdCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        baseOsCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        lensTypeCombo.getItems().addAll(Prescription.Lens_Type.values());
        materialCombo.getItems().addAll(Prescription.Material.values());
    }
    public void initData(String patientName, int idPatient, Prescription prescription) {
        patientNameField.setText(patientName);
        this.idPatient = idPatient;
        System.out.println(idPatient);
        currentPrescription = prescription;

        // Nếu prescription không null, load dữ liệu vào form
        if (prescription != null) {
            loadPrescriptionData(prescription);
        }
    }

    private void loadPrescriptionData(Prescription prescription) {
        // Thông tin chính
        if (prescription.getSignedAt() != null) {
            examDatePicker.setValue(prescription.getSignedAt());
        }

        chiefComplaintField.setText(prescription.getChiefComplaint() != null ? prescription.getChiefComplaint() : "");
        refractionNotesArea.setText(prescription.getRefractionNotes() != null ? prescription.getRefractionNotes() : "");

        // Thông số mắt trái (OS) - sử dụng valueOf để tránh lỗi null với primitive
        sphOsField.setText(String.valueOf(prescription.getSph_os()));
        cylOsField.setText(String.valueOf(prescription.getCyl_os()));
        axisOsField.setText(String.valueOf(prescription.getAxis_os()));
        vaOsField.setText(prescription.getVa_os() != null ? prescription.getVa_os() : "");
        prismOsField.setText(String.valueOf(prescription.getPrism_os()));
        baseOsCombo.setValue(prescription.getBase_os());
        addOsField.setText(String.valueOf(prescription.getAdd_os()));

        // Thông số mắt phải (OD) - sử dụng valueOf để tránh lỗi null với primitive
        sphOdField.setText(String.valueOf(prescription.getSph_od()));
        cylOdField.setText(String.valueOf(prescription.getCyl_od()));
        axisOdField.setText(String.valueOf(prescription.getAxis_od()));
        vaOdField.setText(prescription.getVa_od() != null ? prescription.getVa_od() : "");
        prismOdField.setText(String.valueOf(prescription.getPrism_od()));
        baseOdCombo.setValue(prescription.getBase_od());
        addOdField.setText(String.valueOf(prescription.getAdd_od()));

        // Thông số chung
        pdField.setText(String.valueOf(prescription.getPd()));
        prescriptionNotesArea.setText(prescription.getNotes() != null ? prescription.getNotes() : "");
        lensTypeCombo.setValue(prescription.getLens_type());
        materialCombo.setValue(prescription.getMaterial());

        // Các tính năng lens - boolean không cần kiểm tra null
        arCheck.setSelected(prescription.hasAntiReflectiveCoating());
        blueLightCheck.setSelected(prescription.hasBlueLightFilter());
        uvCheck.setSelected(prescription.hasUvProtection());
        photochromicCheck.setSelected(prescription.isPhotochromic());

        // Thông tin khám
        assessmentArea.setText(prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "");
        planArea.setText(prescription.getPlan() != null ? prescription.getPlan() : "");

        // Ngày hết hạn (nếu có field này)
        if (prescription.getSignedAt() != null) {
            expiryDatePicker.setValue(prescription.getSignedAt());
        }
    }
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) patientNameField.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void handleSavePrescription() {
        if(currentPrescription.getId() <= 0){
            prescriptionService.createPrescription(currentPrescription);
        }
        else{
            prescriptionService.updatePrescription(currentPrescription);
        }
    }


}
