package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

public class CustomerDashBoardController {
    @FXML
    private MenuButton nameField;
    @FXML
    private Label welcomeText;

    @FXML
    public void initialize() {
        Customer currentCustomer = SceneManager.getSceneData("accountData");
        if(currentCustomer != null){
            String fullName = currentCustomer.getFullName();
            nameField.setText(fullName);
            welcomeText.setText("Welcome, " + fullName + "!");
        } else {
            nameField.setText("Guest");
            welcomeText.setText("Welcome, Guest!");
        }

    }


    @FXML
    private void handleBackButton() {
        // Handle back button action
        SceneManager.goBack();
    }
    @FXML
    private void handleForwardButton(){
        SceneManager.goForward();
    }
    @FXML
    private void handleReloadButton(){
        // Reload page
        SceneManager.reloadCurrentScene();
    }
    @FXML
    private void handleAppointmentBookingButton(){
        SceneManager.switchScene(SceneConfig.APPOINTMENT_BOOKING_FXML, SceneConfig.Titles.APPOINTMENT_BOOKING);
    }
    @FXML
    private void handlePaymentHistoryButton(){
        SceneManager.switchScene(SceneConfig.PAYMENT_HISTORY_FXML, SceneConfig.Titles.PAYMENT_HISTORY);
    }
    @FXML
    private void handleAppointmentManagement(){
        SceneManager.switchScene(SceneConfig.APPOINTMENT_MANAGEMENT_FXML, SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }
    @FXML
    private void handleCustomerDetailViewButton(){
        SceneManager.switchScene(SceneConfig.CUSTOMER_DETAIL_FXML, SceneConfig.Titles.CUSTOMER_DETAIL);
    }



}
