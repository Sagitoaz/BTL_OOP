module org.example.oop {
    // ========== JavaFX Modules ==========
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // ========== Third-party Libraries ==========
    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;
    requires java.net.http;

    // ========== BCrypt & Jackson ==========
    requires bcrypt;
    requires com.fasterxml.jackson.annotation;

    // ========== Mini-boot Module ==========
    // NOTE: mini-boot is loaded as automatic module from classpath
    // This avoids conflicts with shaded dependencies
    requires mini.boot;
    // Gson is available through mini.boot (shaded)
    requires com.google.gson;

    // ========== Opens for JavaFX FXML ==========
    exports org.example.oop to javafx.graphics;
    opens org.example.oop to javafx.fxml;
    opens org.example.oop.Control to javafx.fxml;
    opens org.example.oop.Control.Employee to javafx.fxml;
    opens org.example.oop.Control.Schedule to javafx.fxml;
    opens org.example.oop.Control.DashBoard to javafx.fxml;
    opens org.example.oop.Control.PatientAndPrescription to javafx.fxml;
    opens org.example.oop.Control.Inventory to javafx.fxml;
    opens org.example.oop.Control.PaymentControl to javafx.fxml;
    opens org.example.oop.Model to javafx.fxml;
}
