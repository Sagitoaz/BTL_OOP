module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;
    requires com.google.gson;

    requires mini.boot;

    opens org.example.oop.View to javafx.fxml;
    exports org.example.oop.View;

    opens org.example.oop.Control to javafx.fxml;
    exports org.example.oop.Control;

    opens org.example.oop to javafx.fxml;
    exports org.example.oop;
    exports org.example.oop.Model;
    opens org.example.oop.Model to javafx.fxml;
}
