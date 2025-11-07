package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

public class NurseDashBoardController {
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
    private void openCustomerHub(){
        SceneManager.switchScene(SceneConfig.CUSTOMER_HUB_FXML, SceneConfig.Titles.CUSTOMER_HUB);
    }
    @FXML
    private void openPayment(){
        SceneManager.switchScene(SceneConfig.INVOICE_FXML, SceneConfig.Titles.INVOICE);
    }

    @FXML
    private void openProfile(){
        SceneManager.switchScene(SceneConfig.EMPLOYEE_DETAIL_FXML,SceneConfig.Titles.EMPLOYEE_DETAIL);
    }
    @FXML
    private void openInventory(){
        SceneManager.switchScene(SceneConfig.PRODUCT_CRUD_VIEW_FXML, SceneConfig.Titles.PRODUCT_CRUD);
    }
}
