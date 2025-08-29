module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;

    opens org.example.oop.View to javafx.fxml;
    exports org.example.oop.View;

    opens org.example.oop to javafx.fxml;
    exports org.example.oop;
}