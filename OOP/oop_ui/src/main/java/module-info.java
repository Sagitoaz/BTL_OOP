module org.example.oop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires java.desktop;
    requires com.google.gson;
    requires com.fasterxml.jackson.annotation;

    requires transitive mini.boot;
    requires javafx.base;
    requires java.net.http;
    requires jdk.compiler;
    requires bcrypt;
    // các requires khác bạn đã dùng...
    // requires org.controlsfx.controls; v.v.

    // Nếu FXML có controller ở đây:
    opens org.example.oop.Control.Employee to javafx.fxml;

    exports org.example.oop.Control.Employee;
    // Nếu còn các controller khác:
    // opens org.example.oop.Control to javafx.fxml;

    // Nếu bạn dùng JSON / reflection cho models trong UI, cũng nên mở:
    // opens org.miniboot.app.domain.models to javafx.base,
    // com.fasterxml.jackson.databind;

    // export gói public API (nếu cần):
    exports org.example.oop; // nơi có Main

    opens org.example.oop.Control to javafx.fxml;

    exports org.example.oop.Control;

    opens org.example.oop.Control.Schedule to javafx.fxml;

    exports org.example.oop.Control.Schedule;

    opens org.example.oop.Control.DashBoard to javafx.fxml;

    exports org.example.oop.Control.DashBoard;

    opens org.example.oop to javafx.fxml;

    exports org.example.oop.Model;

    opens org.example.oop.Model to javafx.fxml;

    // exports org.example.oop.Data.models;
    // exports org.example.oop.Data.storage;

    // opens org.example.oop.Data.models to javafx.fxml;

    // ✅ API Services cho Inventory (package: org.example.oop.Service)
    exports org.example.oop.Service;

    opens org.example.oop.Service to javafx.fxml;

    // ✅ Utils cho GsonProvider
    exports org.example.oop.Utils;

    opens org.example.oop.Utils to javafx.fxml;

    opens org.example.oop.Control.PatientAndPrescription to javafx.fxml;

    exports org.example.oop.Control.PatientAndPrescription;

    // ✅ Inventory Controllers
    exports org.example.oop.Control.Inventory;

    opens org.example.oop.Control.Inventory to javafx.fxml;

    exports org.example.oop.Control.PaymentControl;

    opens org.example.oop.Control.PaymentControl to javafx.fxml;

}
