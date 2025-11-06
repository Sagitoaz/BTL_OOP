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
}
