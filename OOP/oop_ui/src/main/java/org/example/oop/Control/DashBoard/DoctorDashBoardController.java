package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

public class DoctorDashBoardController {
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
    private void openSchedule(){
        SceneManager.switchScene(SceneConfig.CALENDAR_FXML,SceneConfig.Titles.CALENDAR);
    }
    @FXML
    private void openProfile(){
        SceneManager.switchScene(SceneConfig.EMPLOYEE_DETAIL_FXML,SceneConfig.Titles.EMPLOYEE_DETAIL);
    }
    @FXML
    private void onOpenCustomerHub(){
        SceneManager.switchScene(SceneConfig.CUSTOMER_HUB_FXML, SceneConfig.Titles.CUSTOMER_HUB);
    }
    @FXML
    private void onOpenAppointmentManagement() {
        SceneManager.switchScene(SceneConfig.APPOINTMENT_MANAGEMENT_FXML, SceneConfig.Titles.APPOINTMENT_MANAGEMENT);
    }
}
