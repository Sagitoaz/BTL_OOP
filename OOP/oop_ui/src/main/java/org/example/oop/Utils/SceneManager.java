package org.example.oop.Utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.example.oop.Model.SceneInfo;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class SceneManager {

    private static Stage primaryStage;
    private static final int MAX_CACHE_SIZE = 5;
    private static final int MAX_HISTORY_SIZE = 10;
    private static Map<String , Parent> cachedScenes = new HashMap<>();
    private static Stack<SceneInfo> navigationHistory = new Stack<>();
    private static final Stack<SceneInfo> forwardHistory = new Stack<>();
    private static Map<String, Object> sceneData = new HashMap<>();

    private SceneManager() {
        // Private constructor to prevent instantiation
    }
    public static void setPrimaryStage(Stage primaryStage) {
        SceneManager.primaryStage = primaryStage;
        if (primaryStage.getTitle() == null || primaryStage.getTitle().isEmpty()) {
            primaryStage.setTitle("Eye Clinic");
        }
        primaryStage.setResizable(false);

    }
    // Basic navigation methods
    public static void switchScene(String fxmlPath, String title) {
        runOnFxThread(()->{


            if( primaryStage == null) {
                return;
            }
            primaryStage.setScene(loadFxmlScene(fxmlPath));


            primaryStage.setTitle(title);
            addToHistory(fxmlPath, title);
            forwardHistory.clear();
            primaryStage.show();
            System.out.println("Switching to scene: " + fxmlPath);
        });
    }
    //chuyen scene kem data
    public static void switchSceneWithData(String fxmlPath,String title,String[] key,   Object[] data){
        for(int i = 0; i< key.length; i++){
            if(data[i] != null && key[i] != null){
                setSceneData( key[i], data[i]);
            }

        }

        switchScene(fxmlPath, title);
    }
    // Navigation History

    public static void goBack(){
        runOnFxThread(()->{
            if(navigationHistory.size() <= 1){
                return;
            }
            SceneInfo current = navigationHistory.pop();
            forwardHistory.push(current);
            SceneInfo previous = navigationHistory.peek();


            if( primaryStage == null){
                return;
            }
            primaryStage.setScene(loadFxmlScene(previous.getFxmlPath()));
            primaryStage.setTitle(previous.getTitle());
            primaryStage.show();


        });
    }
    public static void goForward(){
        runOnFxThread(()->{
           if(forwardHistory.isEmpty()){
               return;
           }
           SceneInfo next = forwardHistory.pop();
           navigationHistory.push(next);
           primaryStage.setScene(loadFxmlScene(next.getFxmlPath()));

           if(next.getTitle() != null && !next.getTitle().isEmpty()){
               primaryStage.setTitle(next.getTitle());
           }
           primaryStage.show();
        });
    }

    //Modal Window
    public static void openModalWindow(String fxmlPath, String title, Runnable event) {
        runOnFxThread(()->{

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene =  new Scene(root);
            scene.getProperties().put("fxmlLoader", loader);
            if(scene == null){
                System.err.println("Failed to load scene for: " + fxmlPath);
                return;
            }
            SceneManager.setSceneData("fxmlLoader", loader);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.initOwner(primaryStage);
            stage.initModality(Modality.APPLICATION_MODAL);
            if(event!=null){
                stage.setOnHidden(e->event.run());
            }
            stage.showAndWait();

        });
    }
    public static void reloadCurrentScene() {
        runOnFxThread(()->{
            if(navigationHistory.isEmpty() || primaryStage == null){
                return;
            }
            SceneInfo current = navigationHistory.peek();
            removeFromCache(current.getFxmlPath());
            primaryStage.setScene(loadFxmlScene(current.getFxmlPath()));
            primaryStage.setTitle(current.getTitle());
            primaryStage.show();
        });
    }

    // Cache Management

    public static void clearCache() {
        cachedScenes.clear();
    }
    public static void removeFromCache(String fxmlPath) {
        cachedScenes.remove(fxmlPath);
    }
    public static void addToCache(String fxmlPath, Parent root) {
        if(fxmlPath== null || root== null){
            return;
        }

        // Nếu đã tồn tại trong cache, xóa để thêm mới (refresh cache)
        if(cachedScenes.containsKey(fxmlPath)){
            cachedScenes.remove(fxmlPath);
        }
        else{
            // Nếu cache đầy, xóa phần tử cũ nhất (FIFO)
            if(cachedScenes.size() >= MAX_CACHE_SIZE){
                String firstFxml = cachedScenes.keySet().iterator().next();
                cachedScenes.remove(firstFxml);
            }
        }
        // Lưu vào cachedScenes
        cachedScenes.put(fxmlPath, root);
    }



    //Data Passing
    public static void setSceneData(String key, Object value) {
        if(key == null || value == null){
            return;
        }


        sceneData.put(key, value);
    }
    @SuppressWarnings("unchecked")
    public static <T> T getSceneData(String key) {
        return (T) sceneData.get(key);
    }
    public static void removeSceneData(String key) {
        sceneData.remove(key);
    }
    public static void clearSceneData() {
        sceneData.clear();
    }


    //Helpers
    // load fxml va cache
    private static Scene loadFxmlScene(String fxmlPath) {
        if(fxmlPath == null || fxmlPath.isEmpty()) {
            return null;
        }

        Parent cachedRoot = cachedScenes.get(fxmlPath);
        if(cachedRoot != null) {
            return cachedRoot.getScene();
        }
        try{
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene =  new Scene(root);
            scene.getProperties().put("fxmlLoader", loader);
            addToCache(fxmlPath, root);
            return scene;
        } catch ( IOException e) {
            handleLoadError(fxmlPath, e);
            return null;

        }
    }
    // luu history
    private static void addToHistory(String fxmlPath, String title){
        if(fxmlPath == null || fxmlPath.isEmpty()){
            return;
        }
        if(navigationHistory.size() >= MAX_HISTORY_SIZE){
            navigationHistory.pop() ;
        }
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
