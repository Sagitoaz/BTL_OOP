package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

public class CustomerDashBoardController {


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
    }
    @FXML
    private void handleAppointmentBookingButton(){
        SceneManager.switchScene(SceneConfig.APPOINTMENT_BOOKING_FXML, SceneConfig.Titles.APPOINTMENT_BOOKING);
    }
    @FXML
    private void handlePaymentHistoryButton(){
        SceneManager.switchScene(SceneConfig.PAYMENT_HISTORY_FXML, SceneConfig.Titles.PAYMENT_HISTORY);
    }


}
