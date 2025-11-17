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

    // ========== Exports ==========
    exports org.example.oop;
    exports org.example.oop.Control;
    exports org.example.oop.Control.Employee;
    exports org.example.oop.Control.Schedule;
    exports org.example.oop.Control.DashBoard;
    exports org.example.oop.Control.PatientAndPrescription;
    exports org.example.oop.Control.Inventory;
    exports org.example.oop.Control.PaymentControl;
    exports org.example.oop.Model;
    exports org.example.oop.Service;
    exports org.example.oop.Utils;
    exports org.example.oop.config;

    // ========== Opens for JavaFX FXML ==========
    opens org.example.oop to javafx.fxml;
    opens org.example.oop.Control to javafx.fxml;
    opens org.example.oop.Control.Employee to javafx.fxml;
    opens org.example.oop.Control.Schedule to javafx.fxml;
    opens org.example.oop.Control.DashBoard to javafx.fxml;
    opens org.example.oop.Control.PatientAndPrescription to javafx.fxml;
    opens org.example.oop.Control.Inventory to javafx.fxml;
    opens org.example.oop.Control.PaymentControl to javafx.fxml;
    opens org.example.oop.Model to javafx.fxml;
    opens org.example.oop.Utils to javafx.fxml;
}
