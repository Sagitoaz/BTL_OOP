package org.example.oop;

import javafx.application.Application;
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
        SceneManager.clearCache();
        SceneManager.clearSceneData();
        SceneManager.switchScene(SceneConfig.INVOICE_FXML, SceneConfig.Titles.INVOICE);
    }
}
