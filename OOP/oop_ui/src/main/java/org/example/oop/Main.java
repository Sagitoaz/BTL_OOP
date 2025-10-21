package org.example.oop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/PaymentFXML/Payment.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Bộ pc");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
