package org.example.oop;

import java.io.IOException;

import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);
        SceneManager.clearCache();
        SceneManager.clearSceneData();
        SceneManager.switchScene(SceneConfig.LOGIN_FXML, SceneConfig.Titles.LOGIN);
    }
}