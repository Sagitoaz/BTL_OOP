package org.example.oop.Control.PatientAndPrescription;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Service.HttpDoctorService;
import org.example.oop.Service.PrescriptionService;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.CustomerAndPrescription.Prescription;
import org.miniboot.app.domain.models.Doctor;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionEditorController implements Initializable {

    //--Thong tin chinnh--
    @FXML
    private TextField appointmentIdField;
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
    private HttpDoctorService doctorService;

    private boolean isEditMode = false;
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle resourceBundle) {
        prescriptionService = new PrescriptionService();
        currentPrescription = new Prescription();
        doctorService = new HttpDoctorService();
        baseOdCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        baseOsCombo.getItems().addAll(Prescription.Base.UP, Prescription.Base.DOWN, Prescription.Base.IN,
                Prescription.Base.OUT, Prescription.Base.NONE);
        lensTypeCombo.getItems().addAll(Prescription.Lens_Type.values());
        materialCombo.getItems().addAll(Prescription.Material.values());
        if(SceneManager.getSceneData("prescription") != null){
            Prescription prescription = (Prescription) SceneManager.getSceneData("prescription");
            System.out.println("Loaded prescription for editing: ID #" + prescription.getId());
            currentPrescription = prescription;
            loadPrescriptionData(prescription);

        }
        if(SceneManager.getSceneData("appointment") != null){
            Appointment appointment = (Appointment) SceneManager.getSceneData("appointment");
            currentPrescription.setSignedBy(appointment.getDoctorId());
            appointmentIdField.setText(String.valueOf(appointment.getId()));
            currentPrescription.setAppointmentId(appointment.getId());
            currentPrescription.setCustomerId(appointment.getCustomerId());
            currentPrescription.setCreated_at(LocalDate.now());

            currentPrescription.setUpdated_at(LocalDate.now());
            currentPrescription.setSignedAt(LocalDate.now());
            examDatePicker.setValue(appointment.getStartTime().toLocalDate());

        }
        else{

            appointmentIdField.setText(String.valueOf(currentPrescription.getAppointmentId()));
        }

        if(SceneManager.getSceneData("nameCustomer") != null){
            String nameCustomer = (String) SceneManager.getSceneData("nameCustomer");
            patientNameField.setText(nameCustomer);
        }
        if(SceneManager.getSceneData("doctor") != null){
            String nameDoctor = (String) SceneManager.getSceneData("doctor");
            doctorNameField.setText(nameDoctor);
        }
        else{
            Task<List<Doctor>> task = new Task<>() {
                @Override
                protected List<Doctor> call() throws Exception {
                    return doctorService.getAllDoctors();
                }
            };

            task.setOnSucceeded(e -> {
                List<Doctor> doctors = task.getValue();
                for(Doctor doc : doctors){
                    if(doc.getId() == currentPrescription.getSignedBy()){
                        doctorNameField.setText(doc.getFullName());
                        break;
                    }
                }
            });
            task.setOnFailed(e -> {
                System.err.println("❌ Failed to load doctors: " + task.getException().getMessage());
                showAlert("Lỗi", "Không thể tải thông tin bác sĩ:\n" + task.getException().getMessage());
            });
            new Thread(task).start();


        }


    }
//    public void initData(Appointment appointment) {
//        String patientName = appointment..getFullName();
//        patientNameField.setText(patientName);
//        this.idPatient = idPatient;
//        System.out.println(idPatient);
//        currentPrescription = prescription;
//
//        // Nếu prescription không null, load dữ liệu vào form
//        if (prescription != null) {
//            loadPrescriptionData(prescription);
//        }
//    }

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
    }
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) patientNameField.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void handleSavePrescription() {
        try {
            // Validate dữ liệu cơ bản
            if (appointmentIdField.getText().isEmpty()) {
                showAlert("Lỗi", "Thiếu thông tin ID Buổi khám");
                return;
            }

            // Cập nhật thông tin cơ bản


            // Cập nhật thông tin khám
            currentPrescription.setChiefComplaint(chiefComplaintField.getText());
            currentPrescription.setRefractionNotes(refractionNotesArea.getText());

            // Cập nhật thông số mắt phải (OD)
            currentPrescription.setSph_od(parseDoubleOrZero(sphOdField.getText()));
            currentPrescription.setCyl_od(parseDoubleOrZero(cylOdField.getText()));
            currentPrescription.setAxis_od(parseIntOrZero(axisOdField.getText()));
            currentPrescription.setVa_od(vaOdField.getText());
            currentPrescription.setPrism_od(parseDoubleOrZero(prismOdField.getText()));
            currentPrescription.setBase_od(baseOdCombo.getValue() != null ? baseOdCombo.getValue() : Prescription.Base.NONE);
            currentPrescription.setAdd_od(parseDoubleOrZero(addOdField.getText()));

            // Cập nhật thông số mắt trái (OS)
            currentPrescription.setSph_os(parseDoubleOrZero(sphOsField.getText()));
            currentPrescription.setCyl_os(parseDoubleOrZero(cylOsField.getText()));
            currentPrescription.setAxis_os(parseIntOrZero(axisOsField.getText()));
            currentPrescription.setVa_os(vaOsField.getText());
            currentPrescription.setPrism_os(parseDoubleOrZero(prismOsField.getText()));
            currentPrescription.setBase_os(baseOsCombo.getValue() != null ? baseOsCombo.getValue() : Prescription.Base.NONE);
            currentPrescription.setAdd_os(parseDoubleOrZero(addOsField.getText()));

            // Cập nhật thông số chung
            currentPrescription.setPd(parseDoubleOrZero(pdField.getText()));
            currentPrescription.setNotes(prescriptionNotesArea.getText());
            currentPrescription.setLens_type(lensTypeCombo.getValue());
            currentPrescription.setMaterial(materialCombo.getValue());

            // Cập nhật các tính năng lens
            currentPrescription.setHasAntiReflectiveCoating(arCheck.isSelected());
            currentPrescription.setHasBlueLightFilter(blueLightCheck.isSelected());
            currentPrescription.setHasUvProtection(uvCheck.isSelected());
            currentPrescription.setPhotochromic(photochromicCheck.isSelected());

            // Cập nhật chẩn đoán và kế hoạch
            currentPrescription.setDiagnosis(assessmentArea.getText());
            currentPrescription.setPlan(planArea.getText());

            // Gọi API để lưu
            if (currentPrescription.getId() <= 0) {
                // Tạo mới prescription
                System.out.println("💾 Creating new prescription for appointment #" + currentPrescription.getAppointmentId());
                var response = prescriptionService.createPrescription(currentPrescription);

                if (response.isSuccess()) {
                    System.out.println("✅ Prescription created successfully with ID: " + response.getData().getId());
                    showAlert("Thành công", "Đã tạo đơn khám thành công!\nMã đơn: #" + response.getData().getId());

                    // Đóng cửa sổ sau khi lưu thành công
                    handleCancel();
                } else {
                    System.err.println("❌ Failed to create prescription: " + response.getErrorMessage());
                    showAlert("Lỗi", "Không thể tạo đơn khám:\n" + response.getErrorMessage());
                }
            } else {
                // Cập nhật prescription hiện tại
                System.out.println("💾 Updating prescription #" + currentPrescription.getId());
                var response = prescriptionService.updatePrescription(currentPrescription);

                if (response.isSuccess()) {
                    Prescription updatedPrescription = response.getData();
                    SceneManager.removeSceneData("updatedPrescription");
                    SceneManager.setSceneData("updatedPrescription", updatedPrescription);
                    System.out.println("✅ Prescription updated successfully");
                    showAlert("Thành công", "Đã cập nhật đơn khám thành công!");

                    // Đóng cửa sổ sau khi lưu thành công
                    handleCancel();
                } else {
                    System.err.println("❌ Failed to update prescription: " + response.getErrorMessage());
                    showAlert("Lỗi", "Không thể cập nhật đơn khám:\n" + response.getErrorMessage());
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Invalid number format: " + e.getMessage());
            showAlert("Lỗi", "Dữ liệu số không hợp lệ. Vui lòng kiểm tra lại các trường số.");
        } catch (Exception e) {
            System.err.println("❌ Error saving prescription: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra khi lưu đơn khám:\n" + e.getMessage());
        }
    }

    /**
     * Parse String to double, return 0.0 if empty or invalid
     */
    private double parseDoubleOrZero(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parse String to int, return 0 if empty or invalid
     */
    private int parseIntOrZero(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
