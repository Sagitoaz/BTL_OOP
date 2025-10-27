package org.example.oop.Utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.oop.Model.SceneInfo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class SceneManager {

    private static Stage primaryStage;
    private static Map<String , Parent> cachedScenes = new HashMap<>();
    private static Stack<SceneInfo> navigationHistory = new Stack<>();
    private static final Stack<SceneInfo> forwardHistory = new Stack<>();
    private static Map<String, Object> sceneData = new HashMap<>();

    private SceneManager() {
        // Private constructor to prevent instantiation
    }
    public static void setPrimaryStage(Stage primaryStage) {
        primaryStage = SceneManager.primaryStage;
        if (primaryStage.getTitle() == null || primaryStage.getTitle().isEmpty()) {
            primaryStage.setTitle("Eye Clinic");
        }
        primaryStage.setResizable(false);

    }
    // Basic navigation methods
    public static void switchScene(String fxmlPath, String title) {
        runOnFxThread(()->{

            Parent root = loadFxml(fxmlPath);
            if(root == null || primaryStage == null) {
                return;
            }
            primaryStage.setScene(root.getScene());
            primaryStage.setTitle(title);
            addToHistory(fxmlPath, title);
            forwardHistory.clear();
            primaryStage.show();
        });
    }
    //chuyen scene kem data
    public static void switchSceneWithData(String fxmlPath,String title,  Object data){
        if (data != null) {
            setSceneData("_payload", data);
        }
        switchScene(fxmlPath, title);
    }

    //Data Passing
    public static void setSceneData(String key, Object value) {
        sceneData.put(key, value);
    }
    public static Object getSceneData(String key) {
        return sceneData.get(key);
    }
    public static void clearSceneData() {
        sceneData.clear();
    }


    //Helpers
    // load fxml va cache
    private static Parent loadFxml(String fxmlPath) {
        if(fxmlPath == null || fxmlPath.isEmpty()) {
            return null;
        }
        Parent cachedRoot = cachedScenes.get(fxmlPath);
        if(cachedRoot != null) {
            return cachedRoot;
        }
        try{
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            cachedScenes.put(fxmlPath, root);
            return root;
        } catch ( IOException e) {
            handleLoadError(fxmlPath, e);
            return null;

        }
    }
    // luu history
    private static void addToHistory(String fxmlPath, String title){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setFxmlPath(fxmlPath);
        sceneInfo.setTitle(title);
        sceneInfo.setTimestamp(LocalDateTime.now());
        sceneInfo.setParams(new HashMap<>(sceneData));
        navigationHistory.push(sceneInfo);
    }
    // xu li loi
    private static void handleLoadError(String fxmlPath, Exception e) {
        System.err.println(fxmlPath + ": " + e.getMessage());
        e.printStackTrace();
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Error");
            alert.setHeaderText("Failed to load FXML");
            alert.setContentText("Path: " + fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        });
    }
    // util: đảm bảo chạy trên JavaFX Application Thread
    private static void runOnFxThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }


}
