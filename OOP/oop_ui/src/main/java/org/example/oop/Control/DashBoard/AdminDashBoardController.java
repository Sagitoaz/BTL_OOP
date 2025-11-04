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

}
