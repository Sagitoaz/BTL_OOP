package org.example.oop.Control.Schedule;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.oop.Model.Schedule.Appointment;
import org.example.oop.Model.Schedule.AppointmentType;
import org.example.oop.Model.Schedule.TimeSlot;

public class AppointmentBookingController {
    @FXML private TextField patientQuickSearch;
    @FXML private Button btnNewPatient;
    @FXML private ComboBox<String> cboCurrentUser;

    @FXML private TextField txtPatientKeyword;
    @FXML private TableView<Patient> tblPatients;
    @FXML private Button btnSelectPatient;
    @FXML private TextArea txtPatientName, txtPatientPhone, txtPatientEmail;

    @FXML private ComboBox<Doctor> cboDoctors;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TimeSlot> cboTimeSlot;
    @FXML private ComboBox<AppointmentType> cboAppointmentType;
    @FXML private TextArea txtNotes;
    @FXML private Button btnSubmit;

    @FXML private ListView<Appointment> lstUpcomingAppointments;
}
