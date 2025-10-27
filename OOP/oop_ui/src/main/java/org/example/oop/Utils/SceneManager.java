package org.example.oop.Utils;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.example.oop.Model.SceneInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SceneManager {

    private static Stage stage;
    private static Map<String , Parent> cachedScenes = new HashMap<>();
    private static Stack<SceneInfo> navigationHistory = new Stack<>();




}
