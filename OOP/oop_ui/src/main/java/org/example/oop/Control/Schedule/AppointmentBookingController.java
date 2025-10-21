package org.example.oop.Control.Schedule;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.example.oop.Services.CustomerRecordService;
import org.example.oop.Services.HttpAppointmentService;
import org.example.oop.Services.HttpDoctorService;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.AppointmentType;
import org.miniboot.app.domain.models.Customer;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.TimeSlot;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppointmentBookingController implements Initializable {
    // Service để gọi API
    private HttpAppointmentService appointmentService;
    private HttpDoctorService doctorService;
    private CustomerRecordService customerService;

    // Data cho UI
    private ObservableList<Customer> patientList;
    private ObservableList<Doctor> doctorList;
    private ObservableList<TimeSlot> availableSlots;
    private ObservableList<Appointment> doctorAgenda;

    // Cache customer names để hiển thị trong Doctor Agenda
    private Map<Integer, String> customerNameCache = new java.util.HashMap<>();

    // Selected data
    private Customer selectedPatient;
    private Doctor selectedDoctor;
    private LocalDate selectedDate;
    private TimeSlot selectedSlot;

    // FXML Controls
    @FXML private TextField patientQuickSearch;
    @FXML private Button btnNewPatient;
    @FXML private ComboBox<String> cboCurrentUser;
    @FXML private TextField txtPatientKeyword;
    @FXML private TableView<Customer> tblPatients;
    @FXML private Button btnSelectPatient;
    @FXML private ComboBox<String> cboDoctor;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cboVisitType;
    @FXML private TextArea txtNotes;
    @FXML private TableView<TimeSlot> tblAvailableSlots;
    @FXML private TableView<Appointment> tblDoctorAgenda;
    @FXML private Button btnCheck;
    @FXML private Button btnBook;
    @FXML private Button btnClear;
    @FXML private DatePicker dpQuickJump;
    @FXML private ListView<String> lvwDayAgenda;
    @FXML private Button btnOpenCalendar;
    @FXML private Label lblStatus;
    @FXML private ProgressIndicator piLoading;
    @FXML private TextArea txtPatientName;
    @FXML private TextArea txtPatientPhone; 
    @FXML private TextArea txtPatientEmail;
    @FXML private TextArea txtPatientInsurance;
    @FXML private TextArea txtPatientNotes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("AppointmentBookingController initialized");

        // Khởi tạo services
        appointmentService = new HttpAppointmentService();
        doctorService = new HttpDoctorService();
        customerService = CustomerRecordService.getInstance();

        // Khởi tạo data list
        patientList = FXCollections.observableArrayList();
        doctorList = FXCollections.observableArrayList();
        availableSlots = FXCollections.observableArrayList();
        doctorAgenda = FXCollections.observableArrayList();

        // Set up Combobox
        cboVisitType.setItems(FXCollections.observableArrayList(
                "VISIT", "CHECKUP", "FOLLOWUP", "SURGERY"
        ));

        // Setup TableViews
        setupPatientTable();
        setupAvailableSlotsTable();
        setupDoctorAgendaTable();
        setupPatientSearch();

        // Load initial data
        loadDoctors();

        // Setup listeners
        setupListeners();
    }

    @FXML
    private void handleDoctorSelection(ActionEvent event) {
        String selectedName = cboDoctor.getValue();
        if (selectedName == null) {
            return;
        }

        // Tìm Doctor từ doctorList theo tên
        selectedDoctor = doctorList.stream()
                .filter(d -> d.getFullName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (selectedDoctor == null) {
            return;
        }

        // ✅ FIX: Load cả agenda và slots nếu đã chọn ngày
        if (selectedDate != null) {
            loadDoctorAgenda(selectedDoctor.getId(), selectedDate);
            loadAvailableSlots(selectedDoctor.getId(), selectedDate);
        }
    }

    @FXML
    private void handleDateSelection(ActionEvent event) {
        selectedDate = dpDate.getValue();
        if (selectedDate == null) {
            return;
        }

        // ✅ FIX: Load cả agenda và slots nếu đã chọn bác sĩ
        if (selectedDoctor != null) {
            loadDoctorAgenda(selectedDoctor.getId(), selectedDate);
            loadAvailableSlots(selectedDoctor.getId(), selectedDate);
        }
    }

    @FXML
    private void onNewPatient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FXML/PatientAndPrescription/CustomerHub.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm bệnh nhân mới");
            stage.setScene(new Scene(root, 1000, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Reload patient list
            String keyword = txtPatientKeyword.getText();
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchPatientsAsync(keyword.trim());
            }

        } catch (Exception e) {
            System.err.println("❌ Error opening new patient form: " + e.getMessage());
            showAlert("Lỗi mở form thêm bệnh nhân: " + e.getMessage());
        }
    }

    @FXML
    private void onBookAppointment(ActionEvent event) {
        // Validation
        if (selectedPatient == null) {
            showAlert("Vui lòng chọn bệnh nhân");
            return;
        }
        if (selectedDoctor == null) {
            showAlert("Vui lòng chọn bác sĩ");
            return;
        }
        if (selectedDate == null) {
            showAlert("Vui lòng chọn ngày");
            return;
        }
        if (selectedSlot == null) {
            showAlert("Vui lòng chọn khung giờ");
            return;
        }
        if (!selectedSlot.isAvailable()) {
            showAlert("Slot này đã được đặt. Vui lòng chọn slot khác.");
            return;
        }

        // Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setCustomerId(selectedPatient.getId());
        appointment.setDoctorId(selectedDoctor.getId());

        // Parse visit type từ cboVisitType
        String visitType = cboVisitType.getValue();
        if (visitType != null) {
            appointment.setAppointmentType(AppointmentType.valueOf(visitType.toUpperCase()));
        } else {
            appointment.setAppointmentType(AppointmentType.VISIT); // Default
        }

        appointment.setNotes(txtNotes.getText());

        LocalDateTime startTime = LocalDateTime.of(selectedDate, selectedSlot.getStartTime());
        LocalDateTime endTime = LocalDateTime.of(selectedDate, selectedSlot.getEndTime());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        // Disable button để tránh double-click
        btnBook.setDisable(true);
        piLoading.setVisible(true);
        lblStatus.setText("Đang đặt lịch...");

        // Gọi API POST /appointments (async)
        Task<Appointment> task = new Task<>() {
            @Override
            protected Appointment call() throws Exception {
                return appointmentService.create(appointment);
            }
        };

        task.setOnSucceeded(e -> {
            Appointment created = task.getValue();
            piLoading.setVisible(false);
            btnBook.setDisable(false);

            if (created != null) {
                lblStatus.setText("Đặt lịch thành công!");
                showAlert("Đặt lịch thành công!\n" +
                        "Mã hẹn: #" + created.getId() + "\n" +
                        "Bác sĩ: " + selectedDoctor.getFullName() + "\n" +
                        "Thời gian: " + created.getStartTime());

                // Clear form
                onClearForm(null);

                // Reload data
                loadDoctorAgenda(selectedDoctor.getId(), selectedDate);
                loadAvailableSlots(selectedDoctor.getId(), selectedDate);
            } else {
                lblStatus.setText("Đặt lịch thất bại");
                showAlert("Đặt lịch thất bại. Vui lòng thử lại.");
            }
        });

        task.setOnFailed(e -> {
            piLoading.setVisible(false);
            btnBook.setDisable(false);

            Throwable ex = task.getException();
            lblStatus.setText("Lỗi: " + ex.getMessage());
            showAlert("Lỗi đặt lịch:\n" + ex.getMessage() + "\n\nKiểm tra:\n" +
                    "- Server đang xảy ra sự cố\n" +
                    "- Slot có bị trùng");
        });

        new Thread(task).start();
    }

    @FXML
    private void onClearForm(ActionEvent event) {
        txtNotes.clear();
        dpDate.setValue(null);
        cboDoctor.setValue(null);
        cboVisitType.setValue(null);
        selectedPatient = null;
        selectedDoctor = null;
        selectedDate = null;
        selectedSlot = null;
        btnBook.setDisable(true);
        lblStatus.setText("Sẵn sàng");
    }

    @FXML
    private void onSelectPatient(ActionEvent event) {
        Customer selected = tblPatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn bệnh nhân từ danh sách");
            return;
        }

        selectedPatient = selected;

        // Fill thông tin vào TextAreas
        txtPatientName.setText(selected.getFullName());
        txtPatientPhone.setText(selected.getPhone());
        txtPatientEmail.setText(selected.getEmail() != null ? selected.getEmail() : "");
        txtPatientInsurance.setText(""); // Database chưa lưu bảo hiểm
        txtPatientNotes.setText(selected.getNote() != null ? selected.getNote() : "");

        lblStatus.setText("Đã chọn bệnh nhân: " + selected.getFullName());
    }

    @FXML
    private void handleVisitTypeSelection(ActionEvent event) {
        // TODO: Implement visit type selection logic
    }

    @FXML
    private void onCheckSchedule(ActionEvent event) {
        // Validate inputs
        if (selectedPatient == null) {
            showAlert("Vui lòng chọn bệnh nhân");
            return;
        }

        if (cboDoctor.getValue() == null) {
            showAlert("Vui lòng chọn bác sĩ");
            return;
        }

        if (selectedDate == null) {
            showAlert("Vui lòng chọn ngày");
            return;
        }

        // Show loading
        piLoading.setVisible(true);
        lblStatus.setText("Đang tìm slot trống...");

        // Get doctor from selected name
        String selectedName = cboDoctor.getValue();
        Doctor doctor = doctorList.stream()
                .filter(d -> d.getFullName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (doctor == null) {
            showAlert("Không tìm thấy bác sĩ");
            piLoading.setVisible(false);
            return;
        }

        // FIX: Lưu selectedDoctor
        selectedDoctor = doctor;

        // Load available slots
        loadAvailableSlots(doctor.getId(), selectedDate);

    }

    @FXML
    private void onOpenCalendar(ActionEvent event) {
        // TODO: Implement open calendar logic
    }

    private void setupListeners() {

    }

    private void setupPatientTable() {
        TableColumn<Customer, String> nameCol = 
            (TableColumn<Customer, String>) tblPatients.getColumns().get(0);
        TableColumn<Customer, String> phoneCol = 
            (TableColumn<Customer, String>) tblPatients.getColumns().get(1);
        TableColumn<Customer, String> dobCol = 
            (TableColumn<Customer, String>) tblPatients.getColumns().get(2);
        
        // Chỉ set cellValueFactory
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        phoneCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhone()));

        dobCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDob() != null ?
                        cellData.getValue().getDob().toString() : ""));

        // Bind data
        tblPatients.setItems(patientList);
    }

    private void setupAvailableSlotsTable() {
        TableColumn<TimeSlot, String> startCol =
            (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(0);
        TableColumn<TimeSlot, String> endCol = 
            (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(1);
        TableColumn<TimeSlot, String> durationCol = 
            (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(2);
        TableColumn<TimeSlot, String> roomCol = 
            (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(3); // ✅ Phòng
        TableColumn<TimeSlot, String> statusCol = 
            (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(4); // ✅ Fix: index 4
        
        // Set cellValueFactory
        startCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartTime().toString()));

        endCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndTime().toString()));

        durationCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuration() + " phút"));

        // ✅ Cột Phòng - tạm thời để trống
        roomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty("")); // Hoặc "N/A"

        // ✅ Cột Trạng thái - hiện đúng
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isAvailable() ? "Trống" : "Đã đặt"));

        // Bind data
        tblAvailableSlots.setItems(availableSlots);

        // Listener khi chọn slot
        tblAvailableSlots.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSlot = newVal;
                btnBook.setDisable(!newVal.isAvailable()); // Chỉ cho đặt nếu slot trống
            }
        });
    }

    private void setupDoctorAgendaTable() {
        TableColumn<Appointment, String> timeCol =
            (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns().get(0);
        TableColumn<Appointment, String> patientCol = 
            (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns().get(1);
        TableColumn<Appointment, String> typeCol = 
            (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns().get(2);
        TableColumn<Appointment, String> statusCol = 
            (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns().get(3);
        
        // Chỉ set cellValueFactory
        timeCol.setCellValueFactory(cellData -> {
            Appointment apt = cellData.getValue();
            String time = apt.getStartTime().toLocalTime() + " - " + apt.getEndTime().toLocalTime();
            return new SimpleStringProperty(time);
        });

        // Hiển thị tên bệnh nhân từ cache
        patientCol.setCellValueFactory(cellData -> {
            int customerId = cellData.getValue().getCustomerId();
            
            // Check cache first
            if (customerNameCache.containsKey(customerId)) {
                return new SimpleStringProperty(customerNameCache.get(customerId));
            }
            
            // Nếu chưa có trong cache, hiển thị ID tạm
            return new SimpleStringProperty("Bệnh nhân #" + customerId);
        });

        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAppointmentType().toString()));

        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        // Bind data
        tblDoctorAgenda.setItems(doctorAgenda);
    }

    private void setupPatientSearch() {
        txtPatientKeyword.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.trim().length() >= 2) {
                searchPatientsAsync(newText.trim());
            } else if (newText == null || newText.trim().isEmpty()) {
                patientList.clear();
            }
        });
    }

    private void searchPatientsAsync(String keyword) {
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() {
                var response = customerService.searchCustomers(keyword, null, null, null);
                return response.isSuccess() ? response.getData() : new ArrayList<>();
            }
        };

        task.setOnSucceeded(e -> {
            List<Customer> results = task.getValue();
            patientList.setAll(results);
            lblStatus.setText("Tìm thấy " + results.size() + " bệnh nhân");
        });

        task.setOnFailed(e -> {
            lblStatus.setText("Lỗi tìm kiếm: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void loadDoctors() {
        piLoading.setVisible(true);
        lblStatus.setText("Đang tải danh sách bác sĩ...");

        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() throws Exception{
                return doctorService.getAllDoctors();
            }
        };

        task.setOnSucceeded(e -> {
            List<Doctor> doctors = task.getValue();
            doctorList.setAll(doctors);

            // Populate ComboBox với Doctor objects
            cboDoctor.getItems().clear();
            for (Doctor d : doctors) {
                cboDoctor.getItems().add(d.getFullName());
            }

            lblStatus.setText("Đã tải " + doctors.size() + " bác sĩ");
            piLoading.setVisible(false);
        });

        task.setOnFailed(e -> {
            lblStatus.setText("Lỗi tải bác sĩ: " + task.getException().getMessage());
            piLoading.setVisible(false);
            showAlert("Không thể tải danh sách bác sĩ. Kiểm tra server.");
        });

        new Thread(task).start();
    }

    private void loadDoctorAgenda(int doctorId, LocalDate date) {
        System.out.println("🔍 DEBUG loadDoctorAgenda: doctorId=" + doctorId + ", date=" + date);
        
        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                return appointmentService.getByDoctorAndDate(doctorId, date);
            }
        };

        task.setOnSucceeded(e -> {
            List<Appointment> appointments = task.getValue();
            System.out.println("✅ DEBUG: Received " + appointments.size() + " appointments");
            
            doctorAgenda.setAll(appointments);
            
            // ✅ Load customer names cho các appointments
            loadCustomerNamesForAppointments(appointments);
            
            lblStatus.setText("Lịch bác sĩ: " + appointments.size() + " lịch hẹn");
        });

        task.setOnFailed(e -> {
            lblStatus.setText("Lỗi load lịch: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Load customer names cho các appointments và cache lại
    private void loadCustomerNamesForAppointments(List<Appointment> appointments) {
        // Lấy danh sách unique customer IDs
        java.util.Set<Integer> customerIds = appointments.stream()
            .map(Appointment::getCustomerId)
            .collect(java.util.stream.Collectors.toSet());
        
        // Load all customers một lần
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                var response = customerService.getAllCustomers();
                if (response.isSuccess() && response.getData() != null) {
                    for (Customer customer : response.getData()) {
                        if (customerIds.contains(customer.getId())) {
                            customerNameCache.put(customer.getId(), customer.getFullName());
                        }
                    }
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            // Refresh table để hiển thị tên mới
            tblDoctorAgenda.refresh();
        });
        
        new Thread(task).start();
    }

    private void loadAvailableSlots(int doctorId, LocalDate date) {
        piLoading.setVisible(true);
        lblStatus.setText("Đang tìm slot trống...");

        Task<List<TimeSlot>> task = new Task<>() {
            @Override
            protected List<TimeSlot> call() throws Exception {
                // Gọi API GET /doctors/available-slots
                return doctorService.getAvailableSlots(doctorId, date.toString());
            }
        };

        task.setOnSucceeded(e -> {
            List<TimeSlot> slots = task.getValue();
            availableSlots.setAll(slots);

            long availableCount = slots.stream().filter(TimeSlot::isAvailable).count();
            lblStatus.setText("Tìm thấy " + availableCount + " slot trống / " + slots.size() + " slots");
            piLoading.setVisible(false);
        });

        task.setOnFailed(e -> {
            lblStatus.setText("Lỗi: " + task.getException().getMessage());
            piLoading.setVisible(false);
            showAlert("Lỗi tải slots: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
