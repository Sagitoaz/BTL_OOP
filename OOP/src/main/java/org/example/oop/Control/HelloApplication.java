package org.example.oop.Control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Chỉ load scene, KHÔNG inject gì thêm
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/FXML/ChangePassword.fxml")
        );
        Scene scene = new Scene(loader.load(), 1280, 800);
        stage.setTitle("Change Password");
        stage.setScene(scene);
        stage.show();
    }
}
