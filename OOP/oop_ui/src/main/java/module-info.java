module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;
    requires com.google.gson;

    requires mini.boot;
    requires javafx.base;

    // HTTP client for REST API calls
    requires java.net.http;
    requires org.example.oop;

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

    opens org.example.oop.Control.PatientAndPrescription to javafx.fxml;
    exports org.example.oop.Control.PatientAndPrescription;
//    exports org.example.oop.Model.PaymentModel;
//    opens org.example.oop.Model.PaymentModel to javafx.fxml;
//    exports org.example.oop.Control.PaymentControl;
//    opens org.example.oop.Control.PaymentControl to javafx.fxml;
}
