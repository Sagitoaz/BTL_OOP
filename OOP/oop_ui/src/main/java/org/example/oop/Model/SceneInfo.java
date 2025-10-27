package org.example.oop.Model;

import java.time.LocalDateTime;
import java.util.Map;

public class SceneInfo {

    private String fxmlPath;
    private String title;
    private LocalDateTime timestamp;
    private Map<String, Object> params;
    public SceneInfo(){

    }
    public SceneInfo(String fxmlPath, String title, LocalDateTime timestamp, Map<String, Object> params) {
        this.fxmlPath = fxmlPath;
        this.title = title;
        this.timestamp = timestamp;
        this.params = params;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
    public void setFxmlPath(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
