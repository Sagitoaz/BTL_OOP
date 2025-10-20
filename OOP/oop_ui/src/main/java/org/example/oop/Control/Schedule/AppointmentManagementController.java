package org.example.oop.Control.Schedule;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.oop.Services.HttpAppointmentService;
import org.example.oop.Services.HttpDoctorService;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.Doctor;

public class AppointmentManagementController implements Initializable {
    // Services
    private HttpAppointmentService appointmentService;
    private HttpDoctorService doctorService;

    // Data
    private ObservableList<Appointment> appointmentList;
    private ObservableList<Doctor> doctorList;
    private Appointment selectedAppointment;
    private Appointment originalAppointment; // Để revert changes

    // Pagination
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalAppointments = 0;
    private int totalPages = 1;

    // Filter State
    private LocalDate filterFromDate;
    private LocalDate filterToDate;
    private Integer filterDoctorId;
    private String filterStatus;
    private String searchKeyword;

    // Top filter controls
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> doctorFilter;
    @FXML private ComboBox<String> roomFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField qSearch;
    @FXML private Button applyFilterBtn;
    @FXML private Button resetFilterBtn;
    @FXML private Button createBtn;
    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;
    @FXML private MenuButton moreActionsBtn;

    // Table
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private Button refreshBtn;

    // Detail panel
    @FXML private TextField txtId;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField patientField;
    @FXML private Button choosePatientBtn;
    @FXML private ComboBox<String> doctorCombo;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private ComboBox<String> roomCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea noteArea;
    @FXML private Button saveBtn;
    @FXML private Button revertBtn;
    @FXML private Button deleteBtn;

    // Timeline tab
    @FXML private ListView<String> timelineList;
    @FXML private Button sendSmsBtn;
    @FXML private Button sendEmailBtn;

    // Extra notes tab
    @FXML private TextArea extraNoteArea;
    @FXML private Button saveNoteBtn;

    // Pagination
    @FXML private Label lblSummary;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Label lblPage;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("AppointmentManagementController initialized");

        // Khởi tạo services
        appointmentService = new HttpAppointmentService();
        doctorService = new HttpDoctorService();

        // Khởi tạo data lists
        appointmentList = FXCollections.observableArrayList();
        doctorList = FXCollections.observableArrayList();

        // Setup table
        setupAppointmentTable();

        // Setup filter comboboxes
        setupFilterControls();

        // Setup detail form comboboxes
        setupDetailControls();

        // Load initial data
        loadDoctors(); // Load doctors cho filter & combobox
        loadAppointments(); // Load page 1
    }

    @FXML
    private void onApplyFilter(ActionEvent event) {
        System.out.println("Applying filters...");
        currentPage = 1; // Reset về trang đầu
        loadAppointments();
    }

    @FXML
    private void onResetFilter(ActionEvent event) {
        System.out.println("Resetting filters...");
        doctorFilter.setValue("Tất cả");
        statusFilter.setValue("Tất cả");
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusDays(7));
        qSearch.clear();
        currentPage = 1;
        loadAppointments();
    }

    @FXML
    private void onCreate(ActionEvent event) {
        // TODO: Implement create logic
    }

    @FXML
    private void onConfirm(ActionEvent event) {
        // TODO: Implement confirm logic
    }

    @FXML
    private void onCancel(ActionEvent event) {
        // TODO: Implement cancel logic
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        System.out.println("Refreshing...");
        loadAppointments();
    }

    @FXML
    private void onSave(ActionEvent event) {
        // TODO: Implement save logic
    }

    @FXML
    private void onRevert(ActionEvent event) {
        if (originalAppointment != null) {
            loadAppointmentDetail(originalAppointment);
            showAlert("Đã hoàn tác thay đổi");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        // TODO: Implement delete logic
    }

    @FXML
    private void onChoosePatient(ActionEvent event) {
        // TODO: Implement choose patient logic
    }

    @FXML
    private void onSendSms(ActionEvent event) {
        // TODO: Implement send SMS logic
    }

    @FXML
    private void onSendEmail(ActionEvent event) {
        // TODO: Implement send email logic
    }

    @FXML
    private void onSaveNote(ActionEvent event) {
        // TODO: Implement save note logic
    }

    @FXML
    private void onFirstPage(ActionEvent event) {
        // TODO: Implement first page logic
    }

    @FXML
    private void onPrevPage(ActionEvent event) {
        // TODO: Implement previous page logic
    }

    @FXML
    private void onNextPage(ActionEvent event) {
        // TODO: Implement next page logic
    }

    @FXML
    private void onLastPage(ActionEvent event) {
        // TODO: Implement last page logic
    }

    private void setupAppointmentTable() {
        TableColumn<Appointment, String> colId =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(0);
        TableColumn<Appointment, String> colTime =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(1);
        TableColumn<Appointment, String> colDate =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(2);
        TableColumn<Appointment, String> colPatient =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(3);
        TableColumn<Appointment, String> colDoctor =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(4);
        TableColumn<Appointment, String> colService =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(5);
        TableColumn<Appointment, String> colRoom =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(6);
        TableColumn<Appointment, String> colStatus =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(7);
        TableColumn<Appointment, String> colNote =
                (TableColumn<Appointment, String>) appointmentTable.getColumns().get(8);

        // Set cell value factories
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        colTime.setCellValueFactory(cellData -> {
            Appointment apt = cellData.getValue();
            String time = apt.getStartTime().toLocalTime() + " - " + apt.getEndTime().toLocalTime();
            return new SimpleStringProperty(time);
        });

        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartTime().toLocalDate().toString()));

        colPatient.setCellValueFactory(cellData -> {
            // Tạm thời hiển thị Customer ID (chờ Customer module)
            int customerId = cellData.getValue().getCustomerId();
            return new SimpleStringProperty("Bệnh nhân #" + customerId);
        });

        colDoctor.setCellValueFactory(cellData -> {
            int doctorId = cellData.getValue().getDoctorId();
            // Tìm doctor trong doctorList
            Doctor doctor = doctorList.stream()
                    .filter(d -> d.getId() == doctorId)
                    .findFirst()
                    .orElse(null);
            return new SimpleStringProperty(doctor != null ? doctor.getFullName() : "Bác sĩ #" + doctorId);
        });

        colService.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAppointmentType().toString()));

        colRoom.setCellValueFactory(cellData ->
                new SimpleStringProperty("-")); // Không có room

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colNote.setCellValueFactory(cellData -> {
            String notes = cellData.getValue().getNotes();
            return new SimpleStringProperty(notes != null ? notes : "");
        });

        // Bind data
        appointmentTable.setItems(appointmentList);

        // Selection listener -> load detail vào panel bên phải
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAppointmentDetail(newVal);
            }
        });
    }

    private void setupFilterControls() {
        // Status filter ComboBox
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả",
                "SCHEDULED",    // Đã đặt
                "CONFIRMED",    // Đã xác nhận
                "COMPLETED",    // Hoàn thành
                "CANCELLED",    // Đã hủy
                "NO_SHOW"       // Không đến
        ));
        statusFilter.setValue("Tất cả");

        // Doctor filter
        doctorFilter.setItems(FXCollections.observableArrayList("Tất cả"));
        doctorFilter.setValue("Tất cả");

        // Room filter
        roomFilter.setItems(FXCollections.observableArrayList("Tất cả", "Phòng 1", "Phòng 2"));
        roomFilter.setValue("Tất cả");

        // Date pickers: Mặc định from = hôm nay, to = hôm nay + 7 days
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusDays(7));
    }

    private void setupDetailControls() {
        // Status ComboBox trong detail panel
        statusCombo.setItems(FXCollections.observableArrayList(
                "SCHEDULED",
                "CONFIRMED",
                "COMPLETED",
                "CANCELLED",
                "NO_SHOW"
        ));

        // Service/AppointmentType ComboBox
        serviceCombo.setItems(FXCollections.observableArrayList(
                "VISIT",      // Khám
                "CHECKUP",    // Tái khám
                "FOLLOWUP",   // Theo dõi
                "SURGERY"     // Phẫu thuật
        ));

        // Doctor ComboBox
        doctorCombo.setItems(FXCollections.observableArrayList());

        // Room ComboBox (tạm thời)
        roomCombo.setItems(FXCollections.observableArrayList("Phòng 1", "Phòng 2", "Phòng 3"));

        // Disable save/revert/delete buttons ban đầu
        saveBtn.setDisable(true);
        revertBtn.setDisable(true);
        deleteBtn.setDisable(true);

        // Patient field read-only
        patientField.setEditable(false);

        // ID field read-only
        txtId.setEditable(false);
    }

    private void loadDoctors() {
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() throws Exception {
                return doctorService.getAllDoctors();
            }
        };

        task.setOnSucceeded(e -> {
            List<Doctor> doctors = task.getValue();
            doctorList.setAll(doctors);

            doctorFilter.getItems().clear();
            doctorFilter.getItems().add("Tất cả");
            for (Doctor d : doctors) {
                doctorFilter.getItems().add(d.getFullName());
            }
            doctorFilter.setValue("Tất cả");

            doctorCombo.getItems().clear();
            for (Doctor d : doctors) {
                doctorCombo.getItems().add(d.getFullName());
            }

            System.out.println("✅ Loaded " + doctors.size() + " doctors");
        });

        task.setOnFailed(e -> {
            System.err.println("❌ Error loading doctors: " + task.getException().getMessage());
            showAlert("Không thể tải danh sách bác sĩ");
        });

        new Thread(task).start();
    }

    private void loadAppointments() {
        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                // Parse filter values từ UI
                Integer doctorId = null;
                if (doctorFilter.getValue() != null && !doctorFilter.getValue().equals("Tất cả")) {
                    String doctorName = doctorFilter.getValue();
                    // Tìm doctor ID từ name
                    Doctor doctor = doctorList.stream()
                            .filter(d -> d.getFullName().equals(doctorName))
                            .findFirst()
                            .orElse(null);
                    if (doctor != null) {
                        doctorId = doctor.getId();
                    }
                }

                String status = null;
                if (statusFilter.getValue() != null && !statusFilter.getValue().equals("Tất cả")) {
                    status = statusFilter.getValue();
                }

                LocalDate fromDate = fromDatePicker.getValue();
                LocalDate toDate = toDatePicker.getValue();
                String search = qSearch.getText();

                // Call API với filters
                return appointmentService.getAppointmentsFiltered(
                        doctorId,
                        null,  // customerId (chưa có UI filter cho customer)
                        status,
                        fromDate,
                        toDate,
                        search
                );
            }
        };

        task.setOnSucceeded(e -> {
            List<Appointment> appointments = task.getValue();
            appointmentList.setAll(appointments);

            // Update pagination info
            totalAppointments = appointments.size();
            lblSummary.setText("Tổng: " + totalAppointments + " lịch hẹn");

            System.out.println("✅ Loaded " + appointments.size() + " appointments");
        });

        task.setOnFailed(e -> {
            System.err.println("❌ Error loading appointments: " + task.getException().getMessage());
            showAlert("Không thể tải danh sách lịch hẹn");
        });

        new Thread(task).start();
    }

    // Load chi tiết appointment vào panel bên phải
    private void loadAppointmentDetail(Appointment appointment) {
        selectedAppointment = appointment;
        originalAppointment = cloneAppointment(appointment); // Để revert sau

        // Fill form
        txtId.setText(String.valueOf(appointment.getId()));
        datePicker.setValue(appointment.getStartTime().toLocalDate());
        startTimeField.setText(appointment.getStartTime().toLocalTime().toString());
        endTimeField.setText(appointment.getEndTime().toLocalTime().toString());
        patientField.setText("Bệnh nhân #" + appointment.getCustomerId());

        // Find doctor name
        Doctor doctor = doctorList.stream()
                .filter(d -> d.getId() == appointment.getDoctorId())
                .findFirst()
                .orElse(null);
        if (doctor != null) {
            doctorCombo.setValue(doctor.getFullName());
        }

        serviceCombo.setValue(appointment.getAppointmentType().toString());
        statusCombo.setValue(appointment.getStatus().toString());
        noteArea.setText(appointment.getNotes() != null ? appointment.getNotes() : "");

        // Enable buttons
        saveBtn.setDisable(false);
        revertBtn.setDisable(false);
        deleteBtn.setDisable(false);

        // Load timeline (nếu cần)
        loadTimeline(appointment);
    }

    // Clone appointment để có thể revert changes
    private Appointment cloneAppointment(Appointment original) {
        Appointment clone = new Appointment();
        clone.setId(original.getId());
        clone.setCustomerId(original.getCustomerId());
        clone.setDoctorId(original.getDoctorId());
        clone.setAppointmentType(original.getAppointmentType());
        clone.setStartTime(original.getStartTime());
        clone.setEndTime(original.getEndTime());
        clone.setStatus(original.getStatus());
        clone.setNotes(original.getNotes());
        clone.setCreatedAt(original.getCreatedAt());
        clone.setUpdatedAt(original.getUpdatedAt());
        return clone;
    }

    // Load timeline (tạm thời giả lập)
    private void loadTimeline(Appointment appointment) {
        timelineList.getItems().clear();
        timelineList.getItems().add("🕐 Tạo lúc: " + appointment.getCreatedAt());
        if (appointment.getUpdatedAt() != null) {
            timelineList.getItems().add("✏️ Cập nhật: " + appointment.getUpdatedAt());
        }
        timelineList.getItems().add("📋 Trạng thái: " + appointment.getStatus());
    }

    // Helper methods
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
