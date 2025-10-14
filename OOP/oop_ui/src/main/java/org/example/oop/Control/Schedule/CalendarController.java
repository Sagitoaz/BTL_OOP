package org.example.oop.Control.Schedule;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CalendarController implements Initializable {
    
    @FXML private GridPane calendarGrid;
    @FXML private VBox timeLabelColumn;
    @FXML private AnchorPane appointmentPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: Implement initialization logic
        System.out.println("CalendarController initialized");
    }
}
