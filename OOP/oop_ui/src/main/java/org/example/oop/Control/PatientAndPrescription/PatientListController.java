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
    private TextField searchField;
    @FXML
    private ChoiceBox<String> genderFilter;
    @FXML
    private ChoiceBox<String> dobFromFilter;
    @FXML
    private ChoiceBox<String> dobToFilter;
    @FXML
    private Button applyFilterButton;

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
        genderFilter.setItems(FXCollections.observableArrayList(
                "NAM", "NỮ", "KHÁC", "HỦY BỎ"
        ));


        // Cấu hình filter ngày sinh từ
        dobFromFilter.setItems(FXCollections.observableArrayList(
                "1980", "1990", "2000", "2010", "HỦY BỎ"
        ));


        // Cấu hình filter ngày sinh tới
        dobToFilter.setItems(FXCollections.observableArrayList(
                "1990", "2000", "2010", "2020", "HỦY BỎ"
        ));

        genderFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleGenderFilterInput(newValue);
            }
        });
        dobFromFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleDobFromFilterInput(newValue);
            }
        });

        dobToFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleDobToFilterInput(newValue);
            }
        });

    }
    @FXML
    public void handleApplyFilterButton(ActionEvent event) {
        PatientRecord.Gender gender = null;
        try{
            gender = PatientRecord.Gender.valueOf(genderFilter.getValue().toString().toUpperCase());
        }
        catch(IllegalArgumentException ex){

        }
        dataView.clear();
        System.out.println("LỌC ");
        System.out.println(data.size());
        for(int i = 0; i < data.size(); i++){
            System.out.println(data.get(i).getGender().name());
            if(gender == null || data.get(i).getGender() == gender){
                dataView.add(data.get(i));
            }
        }
        patientTable.setItems(dataView);

    }
    public void handleGenderFilterInput(String input) {
        if(input.equals("HỦY BỎ")) {
            genderFilter.getSelectionModel().clearSelection();
            genderFilter.setValue("Giới Tính");
        }
        System.out.println(input);
    }

    public void handleDobFromFilterInput(String input) {
        if(input.equals("HỦY BỎ")) {
            dobFromFilter.getSelectionModel().clearSelection();
            dobFromFilter.setValue("Sinh Từ");
        }
    }
    public void handleDobToFilterInput(String input) {
        if(input.equals("HỦY BỎ")){
            dobToFilter.getSelectionModel().clearSelection();
            dobToFilter.setValue("Sinh Tới");
        }

    }
}
