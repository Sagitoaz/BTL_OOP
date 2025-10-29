package org.example.oop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);
        SceneManager.switchScene(SceneConfig.LOGIN_FXML, SceneConfig.LOGIN_FXML);
    }
}
