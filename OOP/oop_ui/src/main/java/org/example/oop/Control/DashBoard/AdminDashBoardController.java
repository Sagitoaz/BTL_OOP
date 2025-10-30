package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
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
}
