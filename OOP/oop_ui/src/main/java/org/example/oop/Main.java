package org.example.oop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.oop.Utils.SceneConfig;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SceneConfig.DOCTOR_DASHBOARD_FXML));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("PHÒNG KHÁM MẮT - Eye Clinic Management System");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.show();
    }
}
