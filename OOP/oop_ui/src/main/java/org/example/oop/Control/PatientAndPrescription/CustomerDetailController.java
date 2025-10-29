package org.example.oop.Control.PatientAndPrescription;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.example.oop.Utils.SceneManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomerDetailController {

    @FXML
    private Label customerIdLabel;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label dobLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;

    @FXML
    private Button editCustomerButton;
    @FXML
    private Button deleteCustomerButton;
    @FXML
    private Button addPrescriptionButton;



    @FXML
    public void initialize() {
        setupTableColumns();
    }



    private void populateCustomerDetails() {

    }

    private void setupTableColumns() {

    }

    private void loadPrescriptionHistory() {

    }

    @FXML
    private void handleBackButton() {
        SceneManager.goBack();
    }

    @FXML
    private void handleReloadButton() {
        loadPrescriptionHistory();
        System.out.println("🔄 Đã tải lại dữ liệu");
    }

    @FXML
    private void handleEditCustomer() {

    }



}
