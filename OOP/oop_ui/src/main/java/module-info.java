module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;
    requires com.google.gson;

    requires mini.boot;
    requires java.net.http;

    opens org.example.oop.View to javafx.fxml;
    exports org.example.oop.View;

    opens org.example.oop.Control to javafx.fxml;
    exports org.example.oop.Control;
    
    opens org.example.oop.Control.Schedule to javafx.fxml;
    exports org.example.oop.Control.Schedule;

    opens org.example.oop to javafx.fxml;
    exports org.example.oop;
    exports org.example.oop.Model;
    opens org.example.oop.Model to javafx.fxml;
    exports org.example.oop.Data.models;
    exports org.example.oop.Data.repositories;
    exports org.example.oop.Data.storage;
    opens org.example.oop.Data.models to javafx.fxml;
    exports org.example.oop.Services;
    opens org.example.oop.Services to javafx.fxml;
}
