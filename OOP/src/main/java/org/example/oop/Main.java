package org.example.oop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.oop.Control.HelloApplication;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/FXML/SearchInventory/SearchInventoryView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("OOP");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
