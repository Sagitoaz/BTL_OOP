module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;

    opens org.example.oop.Control to javafx.fxml;
    exports org.example.oop.Control;
}