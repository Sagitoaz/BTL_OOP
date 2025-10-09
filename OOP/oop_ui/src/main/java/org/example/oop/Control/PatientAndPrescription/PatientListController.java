package org.example.oop.Control.PatientAndPrescription;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.oop.Model.PatientAndPrescription.PatientRecord;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PatientListController implements Initializable {
    @FXML
    private TableView<PatientRecord> patientTable;
    @FXML
    private TableColumn<PatientRecord, Integer> id;
    @FXML
    private TableColumn<PatientRecord, String> nameColumn;
    @FXML
    private TableColumn<PatientRecord, PatientRecord.Gender> genderColumn;
    @FXML
    private TableColumn<PatientRecord, LocalDate> dobColumn;
    @FXML
    private TableColumn<PatientRecord, String> phoneColumn;
    @FXML
    private TableColumn<PatientRecord, String> emailColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> genderFilter;
    @FXML
    private ChoiceBox<String> genderFilter1;
    @FXML
    private ChoiceBox<String> genderFilter2;
    @FXML
    private Button newPatientButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cấu hình các cột
        id.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNamePatient()));
        genderColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getGender()));
        dobColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDob()));
        phoneColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPhoneNumber()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEmail()));

        // Định dạng hiển thị cho cột ngày tháng
        dobColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });

        // Thêm dữ liệu mẫu
        ObservableList<PatientRecord> data = FXCollections.observableArrayList(
                new PatientRecord(1, "John Doe", 101, LocalDate.of(1990, 1, 1),
                        PatientRecord.Gender.MALE, "123 Street", "0123456789", "john@email.com"),
                new PatientRecord(2, "Jane Smith", 102, LocalDate.of(1992, 5, 15),
                        PatientRecord.Gender.FEMALE, "456 Avenue", "0987654321", "jane@email.com")
        );
        patientTable.setItems(data);

        // Cấu hình filter giới tính
        genderFilter.setItems(FXCollections.observableArrayList(
                "TẤT CẢ", "NAM", "NỮ", "KHÁC"
        ));
        genderFilter.setValue("TẤT CẢ");

        // Cấu hình filter ngày sinh từ
        genderFilter1.setItems(FXCollections.observableArrayList(
                "1980", "1990", "2000", "2010"
        ));
        genderFilter1.setValue("1980");

        // Cấu hình filter ngày sinh tới
        genderFilter2.setItems(FXCollections.observableArrayList(
                "1990", "2000", "2010", "2020"
        ));
        genderFilter2.setValue("2020");

    }
}
