package org.example.oop.Control.DashBoard;

import javafx.fxml.FXML;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.UserRole;

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
    @FXML
    private void handleOpenInventory(){
        SceneManager.switchScene(SceneConfig.PRODUCT_CRUD_VIEW_FXML, SceneConfig.Titles.PRODUCT_CRUD);

    }
    @FXML
    private void handleOpenPayment(){
        SceneManager.switchScene(SceneConfig.INVOICE_FXML, SceneConfig.Titles.INVOICE);

    }
    @FXML
    private  void handleOpenEmployeeManagement(){
        SceneManager.switchScene(SceneConfig.EMPLOYEE_MANAGEMENT_FXML, SceneConfig.Titles.EMPLOYEE_MANAGEMENT);

    }
    @FXML
    private  void handleOpenDoctorSchedule(){
        SceneManager.switchScene(SceneConfig.DOCTOR_SCHEDULE_FXML, SceneConfig.Titles.DOCTOR_SCHEDULE);
    }
    @FXML
    private void handleOpenWarehouse(){
        SceneManager.switchScene(SceneConfig.SEARCH_INVENTORY_VIEW_FXML, SceneConfig.Titles.SEARCH_INVENTORY);
    }
    @FXML
    private void handleOpenStockMovement(){
        SceneManager.switchScene(SceneConfig.STOCK_MOVEMENT_VIEW_FXML, SceneConfig.Titles.STOCK_MOVEMENT);
    }

}
