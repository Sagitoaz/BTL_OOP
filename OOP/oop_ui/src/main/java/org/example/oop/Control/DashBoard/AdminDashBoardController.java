package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

public class AdminDashBoardController {

    @FXML
    private void handleBackButton(){
        SceneManager.goBack();
    }
    @FXML
    private void handleForwardButton(){
        SceneManager.goForward();
    }
    @FXML
    private void handleReloadButton(){
        SceneManager.reloadCurrentScene();
    }

    @FXML
    private void handleOpenCustomerHub(){
        SceneManager.switchScene(SceneConfig.CUSTOMER_HUB_FXML, SceneConfig.Titles.CUSTOMER_HUB);
    }
    @FXML
    private void handleOpenAppointmentManagement(){
        SceneManager.switchScene(SceneConfig.APPOINTMENT_MANAGEMENT_FXML, SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }
}
