package org.example.oop.Control.Schedule;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Service.HttpAppointmentService;
import org.example.oop.Service.HttpDoctorService;
import org.example.oop.Service.HttpDoctorScheduleService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.AppointmentType;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.DoctorSchedule;
import org.miniboot.app.domain.models.TimeSlot;
import org.miniboot.app.domain.models.UserRole;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentBookingController implements Initializable {
    // Constants for calendar view
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    private static final int PIXELS_PER_HOUR = 60;
    
    // Service để gọi API
    private HttpAppointmentService appointmentService;
    private HttpDoctorService doctorService;
    private HttpDoctorScheduleService scheduleService;
    private CustomerRecordService customerService;

    // Data cho UI
    private ObservableList<Customer> patientList;
    private ObservableList<Doctor> doctorList;
    private ObservableList<TimeSlot> availableSlots;
    private ObservableList<Appointment> doctorAgenda;
    private List<DoctorSchedule> cachedWorkingSchedules = new ArrayList<>();

    // Cache customer names để hiển thị trong Doctor Agenda
    private Map<Integer, String> customerNameCache = new HashMap<>();

    // Selected data
    private Customer selectedPatient;
    private Doctor selectedDoctor;
    private LocalDate selectedDate;
    private TimeSlot selectedSlot;

    // Patient search optimization
    private Task<List<Customer>> searchTask;
    private PauseTransition searchDebounce;

    // FXML Controls
    @FXML
    private Tab tabCustomerSelection;
    @FXML
    private TextField patientQuickSearch;
    @FXML
    private Button btnNewPatient;
    @FXML
    private ComboBox<String> cboCurrentUser;
    @FXML
    private TextField txtPatientKeyword;
    @FXML
    private TableView<Customer> tblPatients;
    @FXML
    private Button btnSelectPatient;
    @FXML
    private ComboBox<String> cboDoctor;
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<String> cboVisitType;
    @FXML
    private TextField txtNotes;
    @FXML
    private TableView<TimeSlot> tblAvailableSlots;
    @FXML
    private TableView<Appointment> tblDoctorAgenda;
    @FXML
    private Button btnCheck;
    @FXML
    private Button btnBook;
    @FXML
    private Button btnClear;
    @FXML
    private DatePicker dpQuickJump;
    @FXML
    private ListView<String> lvwDayAgenda;
    @FXML
    private Button btnOpenCalendar;
    
    // Calendar view controls
    @FXML
    private ScrollPane scheduleScrollPane;
    @FXML
    private ScrollPane timeLabelsScrollPane;
    @FXML
    private AnchorPane schedulePane;
    @FXML
    private VBox timeLabelsBox;
    
    @FXML
    private TextField txtPatientName;
    @FXML
    private TextField txtPatientPhone;
    @FXML
    private TextField txtPatientEmail;
    @FXML
    private TextField txtPatientInsurance;
    @FXML
    private TextArea txtPatientNotes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (SceneManager.getSceneData("role") == UserRole.CUSTOMER) {
            tabCustomerSelection.setDisable(true);
            btnNewPatient.setDisable(true);
        }
        System.out.println("AppointmentBookingController initialized");

        // Khởi tạo services with token
        String token = org.example.oop.Utils.SceneManager.getSceneData("authToken");
        appointmentService = new HttpAppointmentService(org.example.oop.Utils.ApiConfig.getBaseUrl(), token);
        doctorService = new HttpDoctorService();
        scheduleService = new HttpDoctorScheduleService(org.example.oop.Utils.ApiConfig.getBaseUrl(), token);
        customerService = CustomerRecordService.getInstance();

        // Khởi tạo data list
        patientList = FXCollections.observableArrayList();
        doctorList = FXCollections.observableArrayList();
        availableSlots = FXCollections.observableArrayList();
        doctorAgenda = FXCollections.observableArrayList();

        // Set up Combobox
        cboVisitType.setItems(FXCollections.observableArrayList(
                "VISIT", "TEST", "FOLLOWUP", "SURGERY"));

        // Setup TableViews
        setupPatientTable();
        setupAvailableSlotsTable();
        setupDoctorAgendaTable();
        setupPatientSearch();
        
        // Setup calendar view
        generateTimeLabels();
        syncScrollPanes();

        // Load initial data
        loadDoctors();

        // Setup listeners
        setupListeners();
    }

    @FXML
    private void handleBackButton() {
        System.out.println("🔙 Back button clicked");
        SceneManager.goBack();
    }

    @FXML
    private void handleForwardButton() {
        SceneManager.goForward();
    }

    @FXML
    private void handleReloadButton() {
        System.out.println("🔄 Reloading Appointment Booking view");
        // SceneManager.reloadScene();
        SceneManager.reloadCurrentScene();
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

        // Load cả agenda và slots nếu đã chọn ngày
        if (selectedDate != null) {
            loadDoctorAgenda(selectedDoctor.getId(), selectedDate);
            loadAvailableSlots(selectedDoctor.getId(), selectedDate);
            loadAndDrawDoctorSchedule(); //  Vẽ lịch làm việc trong tab "Lịch bác sĩ"
        }
    }

    @FXML
    private void handleDateSelection(ActionEvent event) {
        selectedDate = dpDate.getValue();
        if (selectedDate == null) {
            return;
        }

        // Load cả agenda và slots nếu đã chọn bác sĩ
        if (selectedDoctor != null) {
            loadDoctorAgenda(selectedDoctor.getId(), selectedDate);
            loadAvailableSlots(selectedDoctor.getId(), selectedDate);
            loadAndDrawDoctorSchedule(); //  Vẽ lịch làm việc trong tab "Lịch bác sĩ"
        }
    }

    @FXML
    private void onNewPatient(ActionEvent event) {
        try {
            Runnable runnable = () -> {
                System.out.println("✅ CustomerHub closed, reloading patient list...");
                // Reload toàn bộ danh sách bệnh nhân (clear search)
                searchPatientsAsync("");
                // Clear search field để hiển thị tất cả
                txtPatientKeyword.clear();
                System.out.println("✅ Patient list reloaded");
            };
            SceneManager.openModalWindow(SceneConfig.CUSTOMER_HUB_FXML, SceneConfig.Titles.CUSTOMER_HUB, runnable);

        } catch (Exception e) {
            System.err.println("❌ Error opening CustomerHub: " + e.getMessage());
            e.printStackTrace();
            showAlert("Không thể mở màn hình quản lý bệnh nhân.\n" + e.getMessage());
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

        // Gọi API POST /appointments (async)
        Task<Appointment> task = new Task<>() {
            @Override
            protected Appointment call() throws Exception {
                return appointmentService.create(appointment);
            }
        };

        task.setOnSucceeded(e -> {
            Appointment created = task.getValue();
            btnBook.setDisable(false);

            if (created != null) {
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
                showAlert("Đặt lịch thất bại. Vui lòng thử lại.");
            }
        });

        task.setOnFailed(e -> {
            btnBook.setDisable(false);

            Throwable ex = task.getException();
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

        System.out.println("✅ Đã chọn bệnh nhân: " + selected.getFullName());
    }

    @FXML
    private void handleVisitTypeSelection(ActionEvent event) {
        String selectedType = cboVisitType.getValue();

        if (selectedType == null || selectedType.isEmpty()) {
            return;
        }

        // Update UI và notes prompt based on visit type
        switch (selectedType) {
            case "VISIT":
                // Khám bệnh - 30 minutes
                txtNotes.setPromptText("Ghi chú triệu chứng, lý do khám bệnh...");
                txtNotes.setStyle(""); // Reset style
                break;

            case "FOLLOWUP":
                // Tái khám - 20 minutes
                txtNotes.setPromptText("Ghi chú kết quả khám trước, cần theo dõi gì...");
                txtNotes.setStyle(""); // Reset style
                break;

            case "CHECKUP":
                // Khám sức khỏe - 45 minutes
                txtNotes.setPromptText("Ghi chú các chỉ số cần kiểm tra...");
                txtNotes.setStyle(""); // Reset style
                break;

            case "SURGERY":
                // Phẫu thuật/Thủ thuật - urgent
                txtNotes.setPromptText("Mô tả loại phẫu thuật, chuẩn bị cần thiết...");
                txtNotes.setStyle("-fx-border-color: #ff6b6b; -fx-border-width: 2;");

                // Show warning alert
                showAlert("Lưu ý: Phẫu thuật/Thủ thuật cần sắp xếp lịch đặc biệt.\n" +
                        "Vui lòng liên hệ phòng điều phối để xác nhận chi tiết.");
                break;

            default:
                System.out.println("Đã chọn loại: " + selectedType);
        }

        System.out.println("✅ Visit type selected: " + selectedType);
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

        // Get doctor from selected name
        String selectedName = cboDoctor.getValue();
        Doctor doctor = doctorList.stream()
                .filter(d -> d.getFullName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (doctor == null) {
            showAlert("Không tìm thấy bác sĩ");
            return;
        }

        // FIX: Lưu selectedDoctor
        selectedDoctor = doctor;

        // Load available slots
        loadAvailableSlots(doctor.getId(), selectedDate);

    }

    @FXML
    private void onOpenCalendar(ActionEvent event) {
        try {
            System.out.println("🗓️ Opening Doctor Schedule view (with working hours)...");
            SceneManager.setSceneData("selectedDoctor", selectedDoctor);
            SceneManager.setSceneData("selectedDate", selectedDate);
            SceneManager.switchScene(SceneConfig.DOCTOR_SCHEDULE_FXML, SceneConfig.Titles.DOCTOR_SCHEDULE);

        } catch (Exception e) {
            System.err.println("❌ Error opening calendar: " + e.getMessage());
            e.printStackTrace();
            showAlert("Không thể mở lịch tuần.\n" + e.getMessage());
        }
    }

    private void setupListeners() {
        // Listener cho dpQuickJump (DatePicker trong tab Lịch bác sĩ)
        if (dpQuickJump != null) {
            dpQuickJump.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    selectedDate = newDate;
                    System.out.println("📅 Date changed to: " + newDate);
                    loadAndDrawDoctorSchedule();
                }
            });
        }
    }

    private void setupPatientTable() {
        if (tabCustomerSelection.isDisable()) {
            selectedPatient = SceneManager.getSceneData("accountData");
            return;
        }
        TableColumn<Customer, String> nameCol = (TableColumn<Customer, String>) tblPatients.getColumns().get(0);
        TableColumn<Customer, String> phoneCol = (TableColumn<Customer, String>) tblPatients.getColumns().get(1);
        TableColumn<Customer, String> dobCol = (TableColumn<Customer, String>) tblPatients.getColumns().get(2);

        // Chỉ set cellValueFactory
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));

        phoneCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

        dobCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDob() != null ? cellData.getValue().getDob().toString() : ""));

        // Bind data
        tblPatients.setItems(patientList);
    }

    private void setupAvailableSlotsTable() {
        TableColumn<TimeSlot, String> startCol = (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(0);
        TableColumn<TimeSlot, String> endCol = (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(1);
        TableColumn<TimeSlot, String> durationCol = (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns()
                .get(2);
        TableColumn<TimeSlot, String> roomCol = (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(3);
        TableColumn<TimeSlot, String> statusCol = (TableColumn<TimeSlot, String>) tblAvailableSlots.getColumns().get(4);

        // Set cellValueFactory
        startCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStartTime().toString()));

        endCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndTime().toString()));

        durationCol
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration() + " phút"));

        // Cột Phòng - tạm thời để trống
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty("")); // Hoặc "N/A"

        statusCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isAvailable() ? "Trống" : "Đã đặt"));

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
        TableColumn<Appointment, String> timeCol = (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns()
                .get(0);
        TableColumn<Appointment, String> patientCol = (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns()
                .get(1);
        TableColumn<Appointment, String> typeCol = (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns()
                .get(2);
        TableColumn<Appointment, String> statusCol = (TableColumn<Appointment, String>) tblDoctorAgenda.getColumns()
                .get(3);

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

        typeCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentType().toString()));

        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        // Bind data
        tblDoctorAgenda.setItems(doctorAgenda);
    }

    private void setupPatientSearch() {
        // Initialize debounce timer (500ms)
        searchDebounce = new PauseTransition(Duration.millis(500));
        searchDebounce.setOnFinished(event -> {
            String keyword = txtPatientKeyword.getText();
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchPatientsAsync(keyword.trim());
            }
        });

        // Real-time search với debounce
        txtPatientKeyword.textProperty().addListener((obs, oldText, newText) -> {
            // Reset debounce timer mỗi lần user gõ
            searchDebounce.stop();

            if (newText == null || newText.trim().isEmpty()) {
                // Clear results nếu search field empty
                patientList.clear();
            } else if (newText.trim().length() >= 2) {
                // Chỉ search khi nhập >= 2 ký tự
                searchDebounce.playFromStart();
            }
        });

        // Load all patients initially
        System.out.println("✅ Loading all patients initially...");
        searchPatientsAsync("");
    }

    private void searchPatientsAsync(String keyword) {
        // Cancel previous search task if still running
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel();
            System.out.println("🚫 Cancelled previous search task");
        }

        searchTask = new Task<>() {
            @Override
            protected List<Customer> call() {
                System.out.println("🔍 Searching patients with keyword: '" + keyword + "'");

                if (keyword == null || keyword.trim().isEmpty()) {
                    // Load all customers
                    var response = customerService.getAllCustomers();
                    return response.isSuccess() ? response.getData() : new ArrayList<>();
                } else {
                    // Search by keyword
                    var response = customerService.searchCustomers(keyword, null, null, null);
                    return response.isSuccess() ? response.getData() : new ArrayList<>();
                }
            }
        };

        searchTask.setOnSucceeded(e -> {
            if (!searchTask.isCancelled()) {
                List<Customer> results = searchTask.getValue();
                patientList.setAll(results);

                String message = keyword.isEmpty()
                        ? "Tổng số: " + results.size() + " bệnh nhân"
                        : "Tìm thấy " + results.size() + " bệnh nhân";

                System.out.println("✅ " + message);
            }
        });

        searchTask.setOnFailed(e -> {
            if (!searchTask.isCancelled()) {
                String errorMsg = "Lỗi tìm kiếm: " + searchTask.getException().getMessage();
                System.err.println("❌ " + errorMsg);
            }
        });

        searchTask.setOnCancelled(e -> {
            System.out.println("⚠️ Search task was cancelled");
        });

        new Thread(searchTask).start();
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

            // Populate ComboBox với Doctor objects
            cboDoctor.getItems().clear();
            for (Doctor d : doctors) {
                cboDoctor.getItems().add(d.getFullName());
            }

            System.out.println("✅ Đã tải " + doctors.size() + " bác sĩ");
        });

        task.setOnFailed(e -> {
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

            //  Load customer names cho các appointments
            loadCustomerNamesForAppointments(appointments);

            System.out.println("Lịch bác sĩ: " + appointments.size() + " lịch hẹn");
        });

        task.setOnFailed(e -> {
            System.out.println("Lỗi load lịch: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Load customer names cho các appointments và cache lại
    private void loadCustomerNamesForAppointments(List<Appointment> appointments) {
        // Lấy danh sách unique customer IDs
        Set<Integer> customerIds = appointments.stream()
                .map(Appointment::getCustomerId)
                .collect(Collectors.toSet());

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
            System.out.println("Tìm thấy " + availableCount + " slot trống / " + slots.size() + " slots");
        });

        task.setOnFailed(e -> {
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
    
    // ==================== CALENDAR VIEW HELPERS ====================
    
    private void generateTimeLabels() {
        if (timeLabelsBox == null) return;
        
        timeLabelsBox.getChildren().clear();
        timeLabelsBox.setSpacing(0);
        timeLabelsBox.setPadding(new Insets(0, 8, 0, 0));
        
        int totalHours = (int) java.time.Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;
        timeLabelsBox.setPrefHeight(totalHeight);
        timeLabelsBox.setMinHeight(totalHeight);
        timeLabelsBox.setMaxHeight(totalHeight);
        
        LocalTime current = START_TIME;
        while (!current.isAfter(END_TIME)) {
            Label timeLabel = new Label(current.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setMinHeight(PIXELS_PER_HOUR / 2.0);
            timeLabel.setMaxHeight(PIXELS_PER_HOUR / 2.0);
            timeLabel.setPrefHeight(PIXELS_PER_HOUR / 2.0);
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            timeLabelsBox.getChildren().add(timeLabel);
            current = current.plusMinutes(30);
        }
    }
    
    private void syncScrollPanes() {
        if (scheduleScrollPane != null && timeLabelsScrollPane != null) {
            scheduleScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                timeLabelsScrollPane.setVvalue(newVal.doubleValue());
            });
        }
    }
    
    private void loadAndDrawDoctorSchedule() {
        if (selectedDoctor == null || selectedDate == null) return;
        
        System.out.println("📅 Loading working schedule for doctor: " + selectedDoctor.getFullName());
        
        Task<List<DoctorSchedule>> task = new Task<>() {
            @Override
            protected List<DoctorSchedule> call() throws Exception {
                return scheduleService.getDoctorSchedules(selectedDoctor.getId());
            }
        };
        
        task.setOnSucceeded(e -> {
            cachedWorkingSchedules = task.getValue();
            System.out.println("✅ Loaded " + cachedWorkingSchedules.size() + " working schedules");
            drawScheduleView();
        });
        
        task.setOnFailed(e -> {
            System.err.println("❌ Error loading working schedules: " + task.getException().getMessage());
            drawScheduleView(); // Vẽ calendar trống
        });
        
        new Thread(task).start();
    }
    
    private void drawScheduleView() {
        if (schedulePane == null) return;
        
        schedulePane.getChildren().clear();
        
        // Set size
        int totalHours = (int) java.time.Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;
        schedulePane.setPrefHeight(totalHeight);
        schedulePane.setMinHeight(totalHeight);
        
        // Vẽ working hours background (màu xanh lá)
        drawWorkingHoursBackground();
        
        // Vẽ appointments
        drawAppointments();
    }
    
    private void drawWorkingHoursBackground() {
        if (selectedDate == null || cachedWorkingSchedules.isEmpty()) return;
        
        DayOfWeek dayOfWeek = selectedDate.getDayOfWeek();
        
        // Lọc working schedules của ngày này
        List<DoctorSchedule> todaySchedules = cachedWorkingSchedules.stream()
            .filter(ws -> ws.getDayOfWeek() == dayOfWeek)
            .filter(DoctorSchedule::isActive)
            .collect(Collectors.toList());
        
        System.out.println("🟢 Drawing " + todaySchedules.size() + " working hour blocks for " + dayOfWeek);
        
        for (DoctorSchedule schedule : todaySchedules) {
            LocalTime start = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();
            
            // Tính vị trí Y
            double startY = java.time.Duration.between(START_TIME, start).toMinutes() * (PIXELS_PER_HOUR / 60.0);
            double endY = java.time.Duration.between(START_TIME, end).toMinutes() * (PIXELS_PER_HOUR / 60.0);
            double height = endY - startY;
            
            // Vẽ khung màu xanh lá
            Rectangle workBlock = new Rectangle();
            workBlock.setLayoutY(startY);
            workBlock.setWidth(schedulePane.getPrefWidth() > 0 ? schedulePane.getPrefWidth() : 600);
            workBlock.setHeight(height);
            workBlock.setFill(Color.web("#C8E6C9")); // Màu xanh lá nhạt
            workBlock.setStroke(Color.web("#4CAF50")); // Viền xanh lá đậm
            workBlock.setStrokeWidth(1);
            workBlock.setArcWidth(5);
            workBlock.setArcHeight(5);
            
            // Label "GIỜ LÀM VIỆC"
            Label label = new Label("⏰ GIỜ LÀM VIỆC: " + start + " - " + end);
            label.setLayoutY(startY + 10);
            label.setLayoutX(10);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            
            schedulePane.getChildren().addAll(workBlock, label);
        }
    }
    
    private void drawAppointments() {
        if (doctorAgenda.isEmpty()) return;
        
        System.out.println("📋 Drawing " + doctorAgenda.size() + " appointments");
        
        for (Appointment apt : doctorAgenda) {
            LocalDateTime aptTime = apt.getStartTime();
            if (aptTime.toLocalDate().equals(selectedDate)) {
                LocalTime time = aptTime.toLocalTime();
                
                double startY = java.time.Duration.between(START_TIME, time).toMinutes() * (PIXELS_PER_HOUR / 60.0);
                double height = 50; // 30 phút
                
                Rectangle aptBlock = new Rectangle();
                aptBlock.setLayoutY(startY);
                aptBlock.setLayoutX(10);
                aptBlock.setWidth(schedulePane.getPrefWidth() > 0 ? schedulePane.getPrefWidth() - 20 : 580);
                aptBlock.setHeight(height);
                
                // Màu theo trạng thái
                String color = switch (apt.getStatus()) {
                    case SCHEDULED -> "#2196F3"; // Xanh dương
                    case IN_PROGRESS -> "#FF9800"; // Cam
                    case COMPLETED -> "#4CAF50"; // Xanh lá
                    case CANCELLED -> "#F44336"; // Đỏ
                    default -> "#9E9E9E"; // Xám
                };
                
                aptBlock.setFill(Color.web(color));
                aptBlock.setOpacity(0.7);
                aptBlock.setArcWidth(5);
                aptBlock.setArcHeight(5);
                
                String customerName = customerNameCache.getOrDefault(apt.getCustomerId(), "N/A");
                Label aptLabel = new Label(time + " - " + customerName + " (" + apt.getStatus() + ")");
                aptLabel.setLayoutY(startY + 15);
                aptLabel.setLayoutX(20);
                aptLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                
                schedulePane.getChildren().addAll(aptBlock, aptLabel);
            }
        }
    }
}

