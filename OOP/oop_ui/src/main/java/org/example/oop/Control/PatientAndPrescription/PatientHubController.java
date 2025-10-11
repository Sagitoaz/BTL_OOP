package org.example.oop.Control.PatientAndPrescription;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.oop.Model.PatientAndPrescription.PatientRecord;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PatientHubController  implements Initializable  {

    @FXML
    private ListView<PatientRecord> patientListView;

    private ObservableList<PatientRecord> patientRecordList;

    private List<PatientRecord> patientRecordsData;

    @FXML
    private ComboBox<PatientRecord.Gender> genderFilter;
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
    @FXML
    private Label patientNameLabel;
    @FXML
    private Label patientIdValueLabel;
    @FXML
    private Label patientDobValueLabel;
    @FXML
    private Label patientGenderValueLabel;
    @FXML
    private Label patientAgeValueLabel;
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
        patientRecordList = FXCollections.observableArrayList();
        patientRecordsData = new ArrayList<>();
        // Thêm dữ liệu mẫu
        for(int i = 0; i < 30; i++){
            patientRecordsData.add(
                    new PatientRecord(i + 99, "Duong Tri Dung", 101, LocalDate.of(1990, 1, 1), PatientRecord.Gender.NAM, "123 Main St",
                            "0123456789", "dung@gmail.com", "Healthy"));
            patientRecordsData.add(
                    new PatientRecord(i + 999, "Tran Van Hau", 102, LocalDate.of(2009, 1, 1), PatientRecord.Gender.NỮ, "123 Main St",
                            "0917576767", "hau@gmail.com", "36"));
            patientRecordsData.add(
                    new PatientRecord(i + 9999, "Nguyen Van Toan", 103, LocalDate.of(2005, 1, 1), PatientRecord.Gender.KHÁC, "123 Main St",
                            "0123456789", "dung@gmail.com", "Vy"));
        }
        patientRecordList.addAll(patientRecordsData);
        patientListView.setItems(patientRecordList);
        patientListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {;
            setCurrentPatient(newValue);

        });
        genderFilter.getItems().addAll(PatientRecord.Gender.NAM, PatientRecord.Gender.NỮ , PatientRecord.Gender.KHÁC);

    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = searchField.getText();
        PatientRecord.Gender gender = genderFilter.getValue();
        LocalDate dobFrom = dobFromPicker.getValue();
        LocalDate dobTo = dobToPicker.getValue();
        patientRecordList.clear();
        for (PatientRecord pr : patientRecordsData) {
            if (search != null && !search.isBlank()) {
                if (!pr.getNamePatient().toLowerCase().contains(search.toLowerCase())) {
                    try{
                        int id = Integer.parseInt(search);
                        System.out.println(id);
                        if(pr.getId() != id)
                            continue;

                    }
                    catch (NumberFormatException e){
                        continue;

                    }

                }
                else{

                }
            }
            if (gender != null) {
                if (!pr.getGender().equals(gender)) {
                    continue;
                }
            }
            if (dobFrom != null) {
                if (pr.getDob() == null || pr.getDob().isBefore(dobFrom)) {
                    continue;
                }
            }
            if (dobTo != null) {
                if (pr.getDob() == null || pr.getDob().isAfter(dobTo)) {
                    continue;
                }
            }
            patientRecordList.add(pr);

        }
        patientListView.setItems(patientRecordList);
    }
    @FXML
    private void resetFilters(ActionEvent event) {
        searchField.clear();
        genderFilter.setValue(null);
        genderFilter.setPromptText("Lọc theo giới tính");
        dobFromPicker.setValue(null);
        dobToPicker.setValue(null);
        patientRecordList.clear();
        patientRecordList.addAll(patientRecordsData);
        patientListView.setItems(patientRecordList);
    }

    private void setCurrentPatient(PatientRecord pr){
        if(pr == null){
            patientNameLabel.setText("[CHỌN BỆNH NHÂN]");
            patientIdValueLabel.setText("...");
            patientDobValueLabel.setText("...");
            patientGenderValueLabel.setText("...");
            patientAgeValueLabel.setText("...");
            addressField.setText("");
            phoneField.setText("");
            emailField.setText("");
            notesArea.setText("");


        }
        else{
            patientNameLabel.setText(pr.getNamePatient());
            patientIdValueLabel.setText(String.valueOf(pr.getId()));
            patientDobValueLabel.setText(pr.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            patientGenderValueLabel.setText(pr.getGender().name());
            patientAgeValueLabel.setText(String.valueOf(pr.getAge()));
            addressField.setText(pr.getAddress());
            phoneField.setText(pr.getPhoneNumber());
            emailField.setText(pr.getEmail());
            notesArea.setText(pr.getNotes());
        }

    }

    @FXML
    private void onAddPatientButton(ActionEvent event)throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/PatientAndPrescription/AddPatientView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Thêm Bệnh Nhân Mới");
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
            stage.showAndWait();
            AddPatientViewController controller = loader.getController();
            PatientRecord newPatient = controller.getNewPatientRecord();
            addPatientRecord(newPatient);

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    public void addPatientRecord(PatientRecord pr){
        if(pr == null ) return;
        patientRecordsData.add(pr);
        patientRecordList.add(pr);
        patientListView.setItems(patientRecordList);
    }
    public int getNextPatientId(){
        int nxtId = patientRecordsData.size() + 100;
        return nxtId + 1;
    }


}
