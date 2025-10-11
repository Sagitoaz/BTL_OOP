package org.example.oop.Control.PatientAndPrescription;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
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
    private TableColumn<PatientRecord, String> addressColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<PatientRecord.Gender> genderFilter;
    @FXML
    private DatePicker dateFromFilter;
    @FXML
    private DatePicker dateToFilter;


    ObservableList<PatientRecord> data;
    ObservableList<PatientRecord> dataView;
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Cấu hình các cột
        id.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNamePatient()));
        genderColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getGender()));
        dobColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDob()));
        phoneColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPhoneNumber()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEmail()));
        addressColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getAddress()));

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
        data = FXCollections.observableArrayList(
                new PatientRecord(1, "John Doe", 101, LocalDate.of(1990, 1, 1),
                        PatientRecord.Gender.NAM, "123 Street", "0123456789", "john@email.com"),
                new PatientRecord(2, "Jane Smith", 102, LocalDate.of(1992, 5, 15),
                        PatientRecord.Gender.NỮ, "456 Avenue", "0987654321", "jane@email.com")
        );
        dataView = FXCollections.observableArrayList(data);

        patientTable.setItems(dataView);

        // Cấu hình filter giới tính




    }

}
