package org.example.oop.Control.Schedule;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.example.oop.Control.Schedule.CalendarController.TOTAL_HOURS;
import org.example.oop.Control.SessionStorage;
import org.example.oop.Service.HttpAppointmentService;
import org.example.oop.Service.HttpDoctorService;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.Doctor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DoctorScheduleController implements Initializable {

    private static class WorkingSchedule {
        Set<DayOfWeek> workingDays = new HashSet<>();
        Set<String> shifts = new HashSet<>();

        public Set<DayOfWeek> getWorkingDays() { return workingDays; }
        public void setWorkingDays(Set<DayOfWeek> days) { this.workingDays = days; }
        public Set<String> getShifts() { return shifts; }
        public void setShifts(Set<String> shifts) { this.shifts = shifts; }
    }

    private Map<Integer, WorkingSchedule> doctorSchedules = new HashMap<>();

    // SERVICES
    private HttpDoctorService doctorService;
    private HttpAppointmentService appointmentService;

    // DATA
    private ObservableList<Doctor> doctorList;
    private ObservableList<Appointment> appointmentList;
    private Doctor currentDoctor; // Bác sĩ đang xem lịch
    private LocalDate selectedDate;
    private LocalDate weekStart; // Ngày đầu tuần (Monday) cho Week View
    private boolean isDayView = true; // true = Day View, false = Week View
    private boolean isAdmin = false; // Role: true = Admin (edit), false = Doctor (view only) - will be set from session

    // CONSTANTS
    private static final LocalTime START_TIME = LocalTime.of(8, 0);   // 8:00 AM
    private static final LocalTime END_TIME = LocalTime.of(17, 0);    // 5:00 PM (17:00)
    private static final int SLOT_DURATION = 30; // 30 phút
    private static final int PIXELS_PER_HOUR = 60; // Chiều cao 1 giờ = 60px

    // FXML controls
    @FXML private ComboBox<String> cboDoctorSelect; // Chọn bác sĩ
    @FXML private DatePicker datePicker;
    @FXML private ToggleButton dayViewBtn;
    @FXML private ToggleButton weekViewBtn;
    @FXML private Button todayBtn;
    @FXML private TextField searchField;
    @FXML private Button addScheduleBtn;

    // Working hours checkboxes
    @FXML private CheckBox mondayChk;
    @FXML private CheckBox tuesdayChk;
    @FXML private CheckBox wednesdayChk;
    @FXML private CheckBox thursdayChk;
    @FXML private CheckBox fridayChk;
    @FXML private CheckBox saturdayChk;
    @FXML private CheckBox sundayChk;

    // Shift checkboxes
    @FXML private CheckBox morningShiftChk;
    @FXML private CheckBox afternoonShiftChk;
    @FXML private CheckBox eveningShiftChk;
    @FXML private Button applyShiftBtn;

    // Filter checkboxes
    @FXML private CheckBox emptyChk;
    @FXML private CheckBox bookedChk;
    @FXML private CheckBox inProgressChk;
    @FXML private CheckBox completedChk;
    @FXML private CheckBox cancelledChk;

    // Conflict list
    @FXML private ListView<String> conflictList;

    // Schedule display
    @FXML private VBox timeLabelsBox; // Time labels container
    @FXML private ScrollPane scheduleScrollPane; // ✅ Main schedule scroll pane
    @FXML private AnchorPane schedulePane;

    // Bottom buttons
    @FXML private Button saveBtn;
    @FXML private Button exportPdfBtn;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    // INITIALIZATION

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ DoctorScheduleController initialized");

        // 1. Khởi tạo services
        doctorService = new HttpDoctorService();
        appointmentService = new HttpAppointmentService();

        // 2. Khởi tạo data lists
        doctorList = FXCollections.observableArrayList();
        appointmentList = FXCollections.observableArrayList();

        // 3. Set ngày mặc định = hôm nay
        selectedDate = LocalDate.now();
        weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); // ✅ Init weekStart
        datePicker.setValue(selectedDate);

        // 4. Setup ToggleGroup cho Day/Week view
        ToggleGroup viewGroup = new ToggleGroup();
        dayViewBtn.setToggleGroup(viewGroup);
        weekViewBtn.setToggleGroup(viewGroup);
        dayViewBtn.setSelected(true); // Mặc định Day View

        // 5. ✅ Sinh time labels (8:00, 8:30, ..., 20:00)
        generateTimeLabels();
        
        // 6. ✅ Setup phân quyền (TODO: lấy từ session user)
        setupPermissions();

        // 7. Setup listeners
        setupListeners();

        // 8. Load dữ liệu ban đầu
        loadInitialData();
    }
    
    // Sinh time labels động (8:00, 8:30, 9:00, ..., 20:00)
    private void generateTimeLabels() {
        if (timeLabelsBox == null) return;
        
        timeLabelsBox.getChildren().clear();
        timeLabelsBox.setSpacing(0);
        timeLabelsBox.setPadding(new Insets(0, 8, 0, 0));
        
        // Tính tổng chiều cao (8:00 - 17:00 = 9 giờ = 540px)
        int totalHours = (int) Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;
        timeLabelsBox.setPrefHeight(totalHeight);
        timeLabelsBox.setMinHeight(totalHeight);
        timeLabelsBox.setMaxHeight(totalHeight); // ✅ Prevent overflow
        
        System.out.println("✅ TimeLabels: totalHeight = " + totalHeight + "px (" + totalHours + " hours)");
        
        LocalTime current = START_TIME;
        while (!current.isAfter(END_TIME)) {
            Label timeLabel = new Label(current.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setMinHeight(PIXELS_PER_HOUR / 2.0); // 30px cho mỗi 30 phút
            timeLabel.setMaxHeight(PIXELS_PER_HOUR / 2.0);
            timeLabel.setPrefHeight(PIXELS_PER_HOUR / 2.0);
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            timeLabelsBox.getChildren().add(timeLabel);
            
            current = current.plusMinutes(30);
        }
    }
    private void setupPermissions() {
        // Get role từ session user
        try {
            String userRole = SessionStorage.getCurrentUserRole();
            
            if (userRole != null) {
                // Check if user is ADMIN or EMPLOYEE (EMPLOYEE includes doctors/nurses with edit rights)
                isAdmin = userRole.equalsIgnoreCase("ADMIN") || userRole.equalsIgnoreCase("EMPLOYEE");
                System.out.println("🔐 User role: " + userRole + " | Can edit: " + isAdmin);
            } else {
                // Nếu không có session, mặc định là view-only
                isAdmin = false;
                System.out.println("⚠️ No session found, permission set to view-only");
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting user role: " + e.getMessage());
            isAdmin = false; // Fallback to view-only nếu lỗi
        }
        
        boolean canEdit = isAdmin;
        
        // Disable các controls nếu không phải admin
        if (mondayChk != null) mondayChk.setDisable(!canEdit);
        if (tuesdayChk != null) tuesdayChk.setDisable(!canEdit);
        if (wednesdayChk != null) wednesdayChk.setDisable(!canEdit);
        if (thursdayChk != null) thursdayChk.setDisable(!canEdit);
        if (fridayChk != null) fridayChk.setDisable(!canEdit);
        if (saturdayChk != null) saturdayChk.setDisable(!canEdit);
        if (sundayChk != null) sundayChk.setDisable(!canEdit);
        
        if (morningShiftChk != null) morningShiftChk.setDisable(!canEdit);
        if (afternoonShiftChk != null) afternoonShiftChk.setDisable(!canEdit);
        if (eveningShiftChk != null) eveningShiftChk.setDisable(!canEdit);
        if (applyShiftBtn != null) applyShiftBtn.setDisable(!canEdit);
        
        if (saveBtn != null) saveBtn.setDisable(!canEdit);
        if (addScheduleBtn != null) addScheduleBtn.setDisable(!canEdit);
        
        System.out.println("✅ Permissions: " + (canEdit ? "ADMIN (edit)" : "DOCTOR (view only)"));
    }

    private void setupListeners() {
        // ComboBox doctor selector listener
        if (cboDoctorSelect != null) {
            cboDoctorSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !doctorList.isEmpty()) {
                    // Tìm doctor theo tên
                    currentDoctor = doctorList.stream()
                        .filter(d -> d.getFullName().equals(newVal))
                        .findFirst()
                        .orElse(null);
                    
                    if (currentDoctor != null) {
                        System.out.println("✅ Switched to doctor: " + currentDoctor.getFullName());
                        loadAppointments();
                    }
                }
            });
        }
        
        // DatePicker listener
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                selectedDate = newDate;
                // ✅ Update weekStart khi đổi ngày
                weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                refreshScheduleView();
            }
        });

        // Search field listener
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterAppointments(newText);
        });

        // Filter checkboxes listeners
        if (emptyChk != null) emptyChk.selectedProperty().addListener((obs, old, val) -> applyFilters());
        if (bookedChk != null) bookedChk.selectedProperty().addListener((obs, old, val) -> applyFilters());
        if (inProgressChk != null) inProgressChk.selectedProperty().addListener((obs, old, val) -> applyFilters());
        if (completedChk != null) completedChk.selectedProperty().addListener((obs, old, val) -> applyFilters());
        if (cancelledChk != null) cancelledChk.selectedProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void loadInitialData() {
        // Load danh sách bác sĩ
        Task<List<Doctor>> loadDoctorsTask = new Task<>() {
            @Override
            protected List<Doctor> call() {
                return doctorService.getAllDoctors();
            }
        };

        loadDoctorsTask.setOnSucceeded(e -> {
            List<Doctor> doctors = loadDoctorsTask.getValue();
            doctorList.setAll(doctors);

            // ✅ Populate ComboBox với tên bác sĩ
            if (cboDoctorSelect != null) {
                cboDoctorSelect.getItems().clear();
                for (Doctor d : doctors) {
                    cboDoctorSelect.getItems().add(d.getFullName());
                }
            }

            if (!doctors.isEmpty()) {
                // Chọn bác sĩ đầu tiên (hoặc lấy từ session user)
                currentDoctor = doctors.get(0);
                System.out.println("✅ Selected doctor: " + currentDoctor.getFullName());
                
                // ✅ Set ComboBox selection
                if (cboDoctorSelect != null) {
                    cboDoctorSelect.getSelectionModel().select(currentDoctor.getFullName());
                }

                // Load appointments của bác sĩ này
                loadAppointments();
            }
        });

        loadDoctorsTask.setOnFailed(e -> {
            System.err.println("❌ Failed to load doctors");
            showError("Lỗi", "Không thể tải danh sách bác sĩ");
        });

        new Thread(loadDoctorsTask).start();
    }

    // VIEW CONTROLS

    @FXML
    private void onDayView(ActionEvent event) {
        isDayView = true;
        dayViewBtn.setSelected(true);
        weekViewBtn.setSelected(false);
        refreshScheduleView();
    }

    @FXML
    private void onWeekView(ActionEvent event) {
        isDayView = false;
        dayViewBtn.setSelected(false);
        weekViewBtn.setSelected(true);
        refreshScheduleView();
    }

    @FXML
    private void onToday(ActionEvent event) {
        selectedDate = LocalDate.now();
        datePicker.setValue(selectedDate);
        refreshScheduleView();
    }

    // LOAD DATA

    private void loadAppointments() {
        if (currentDoctor == null) return;

        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() {
                if (isDayView) {
                    // Load appointments cho 1 ngày
                    return appointmentService.getByDoctorAndDateRange(
                            currentDoctor.getId(),
                            selectedDate,
                            selectedDate
                    );
                } else {
                    // Load appointments cho cả tuần
                    LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate endOfWeek = startOfWeek.plusDays(6);

                    return appointmentService.getByDoctorAndDateRange(
                            currentDoctor.getId(),
                            startOfWeek,
                            endOfWeek
                    );
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Appointment> appointments = task.getValue();
            appointmentList.setAll(appointments);
            System.out.println("✅ Loaded " + appointments.size() + " appointments");

            // Vẽ lại schedule
            renderSchedule();

            // Check conflicts
            detectConflicts();
        });

        task.setOnFailed(e -> {
            System.err.println("❌ Failed to load appointments");
            showError("Lỗi", "Không thể tải danh sách lịch hẹn");
        });

        new Thread(task).start();
    }

    private void refreshScheduleView() {
        loadAppointments();
    }

    // RENDER SCHEDULE

    private void renderSchedule() {
        // Clear existing content
        schedulePane.getChildren().clear();
        
        // ✅ Ensure schedulePane không có layout issues
        schedulePane.setLayoutX(0);
        schedulePane.setLayoutY(0);

        if (isDayView) {
            renderDayView();
        } else {
            renderWeekView();
        }
    }

    private void renderDayView() {
        // Tính chiều cao tổng (8:00 - 17:00 = 9 giờ = 540px)
        int totalHours = (int) Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;

        schedulePane.setPrefHeight(totalHeight);
        schedulePane.setMinHeight(totalHeight);
        
        System.out.println("✅ DayView: totalHeight = " + totalHeight + "px (" + totalHours + " hours)");

        // 1. Vẽ grid lines (mỗi 30 phút)
        drawTimeGrid(totalHeight);

        // 2. Vẽ appointments
        // Use prefWidth từ FXML (880px) thay vì getPrefWidth() có thể = 0
        double contentWidth = schedulePane.getPrefWidth() > 0 ? schedulePane.getPrefWidth() : 880;
        
        for (Appointment apt : appointmentList) {
            // Chỉ vẽ appointment trong ngày đã chọn
            if (apt.getStartTime().toLocalDate().equals(selectedDate)) {
                drawAppointmentBlock(apt, 0, contentWidth);
            }
        }
    }

    private void renderWeekView() {
        // Week view: 7 cột (Thứ 2 - CN)
        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int totalHours = (int) Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;
        schedulePane.setPrefHeight(totalHeight);
        schedulePane.setMinHeight(totalHeight);

        double contentWidth = schedulePane.getPrefWidth() > 0 ? schedulePane.getPrefWidth() : 880;
        double columnWidth = contentWidth / 7.0;

        // 1. Vẽ grid
        drawTimeGrid(totalHeight);

        // 2. Vẽ vertical lines cho 7 ngày
        for (int i = 1; i < 7; i++) {
            Rectangle line = new Rectangle(columnWidth * i, 0, 1, totalHeight);
            line.setFill(Color.LIGHTGRAY);
            schedulePane.getChildren().add(line);
        }

        // 3. Vẽ appointments cho từng ngày
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate currentDay = startOfWeek.plusDays(dayOffset);
            double xStart = columnWidth * dayOffset;

            for (Appointment apt : appointmentList) {
                if (apt.getStartTime().toLocalDate().equals(currentDay)) {
                    drawAppointmentBlock(apt, xStart, columnWidth);
                }
            }
        }
    }

    private void drawTimeGrid(double totalHeight) {
        // Vẽ horizontal lines mỗi 30 phút
        LocalTime currentTime = START_TIME;
        int lineIndex = 0;

        while (currentTime.isBefore(END_TIME) || currentTime.equals(END_TIME)) {
            double y = lineIndex * (PIXELS_PER_HOUR / 2.0); // 30 phút = 30px

            Rectangle line = new Rectangle(0, y, schedulePane.getPrefWidth(), 1);
            line.setFill(currentTime.getMinute() == 0 ? Color.GRAY : Color.LIGHTGRAY);
            schedulePane.getChildren().add(line);

            currentTime = currentTime.plusMinutes(30);
            lineIndex++;
        }
    }

    private void drawAppointmentBlock(Appointment apt, double xStart, double width) {
        // Tính vị trí Y dựa trên thời gian
        LocalTime aptStartTime = apt.getStartTime().toLocalTime();
        LocalTime aptEndTime = apt.getEndTime().toLocalTime();

        double startY = calculateYPosition(aptStartTime);
        double endY = calculateYPosition(aptEndTime);
        double height = endY - startY;

        // Phát hiện overlap và shift X position
        int overlapCount = countOverlappingAppointments(apt, xStart, startY, endY);
        double adjustedX = xStart + (overlapCount * 10); // Shift 10px cho mỗi overlap
        double adjustedWidth = Math.max(width - (overlapCount * 10) - 4, 80); // Giảm width nhưng min 80px

        // Tạo block (VBox chứa thông tin appointment)
        VBox block = new VBox(5);
        block.setLayoutX(adjustedX);
        block.setLayoutY(startY);
        block.setPrefWidth(adjustedWidth);
        block.setPrefHeight(height);
        block.setPadding(new Insets(5));
        
        // Lưu appointment ID để detect overlap sau này
        block.setUserData(apt.getId());

        // Set màu theo status
        String styleClass = getStyleClassForStatus(apt.getStatus());
        block.setStyle(getStyleForStatus(apt.getStatus()) + " -fx-border-color: white; -fx-border-width: 1;");

        // Thêm thông tin
        Label timeLabel = new Label(aptStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - " + aptEndTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 11px;");

        Label typeLabel = new Label(apt.getAppointmentType().toString());
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        Label notesLabel = new Label(apt.getNotes() != null ? apt.getNotes() : "");
        notesLabel.setWrapText(true);
        notesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px;");
        notesLabel.setMaxHeight(30);

        block.getChildren().addAll(timeLabel, typeLabel, notesLabel);

        // Click event
        block.setOnMouseClicked(e -> onAppointmentClicked(apt));

        schedulePane.getChildren().add(block);
    }
    
    // Đếm số appointment đã vẽ mà overlap với appointment hiện tại
    private int countOverlappingAppointments(Appointment apt, double xStart, double startY, double endY) {
        int count = 0;
        
        for (Node node : schedulePane.getChildren()) {
            if (node instanceof VBox && node.getUserData() instanceof Integer) {
                int existingId = (int) node.getUserData();
                
                // Skip chính nó
                if (existingId == apt.getId()) continue;
                
                // Check overlap về position
                double existingX = node.getLayoutX();
                double existingY = node.getLayoutY();
                double existingHeight = ((VBox) node).getPrefHeight();
                double existingEndY = existingY + existingHeight;
                
                // Overlap nếu: cùng khoảng X và Y giao nhau
                boolean sameColumn = Math.abs(existingX - xStart) < 15; // Trong cùng cột (tolerance 15px)
                boolean yOverlap = !(endY <= existingY || startY >= existingEndY);
                
                if (sameColumn && yOverlap) {
                    count++;
                }
            }
        }
        
        return count;
    }

    private double calculateYPosition(LocalTime time) {
        long minutesFromStart = Duration.between(START_TIME, time).toMinutes();
        return (minutesFromStart / 60.0) * PIXELS_PER_HOUR;
    }

    private String getStyleForStatus(AppointmentStatus status) {
        switch (status) {
            case SCHEDULED:
                return "-fx-background-color: #FFA726; -fx-background-radius: 5;"; // Orange
            case CONFIRMED:
                return "-fx-background-color: #42A5F5; -fx-background-radius: 5;"; // Blue
            case IN_PROGRESS:
                return "-fx-background-color: #66BB6A; -fx-background-radius: 5;"; // Green
            case COMPLETED:
                return "-fx-background-color: #9E9E9E; -fx-background-radius: 5;"; // Gray
            case CANCELLED:
                return "-fx-background-color: #EF5350; -fx-background-radius: 5;"; // Red
            default:
                return "-fx-background-color: #BDBDBD; -fx-background-radius: 5;"; // Light Gray
        }
    }

    private String getStyleClassForStatus(AppointmentStatus status) {
        return "appointment-" + status.name().toLowerCase();
    }

    private void onAppointmentClicked(Appointment apt) {
        // Hiển thị chi tiết appointment (có thể mở dialog)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết lịch hẹn");
        alert.setHeaderText("Appointment #" + apt.getId());
        alert.setContentText(
                "Bệnh nhân ID: " + apt.getCustomerId() + "\n" +
                        "Loại: " + apt.getAppointmentType() + "\n" +
                        "Thời gian: " + apt.getStartTime() + " - " + apt.getEndTime() + "\n" +
                        "Trạng thái: " + apt.getStatus() + "\n" +
                        "Ghi chú: " + (apt.getNotes() != null ? apt.getNotes() : "Không có")
        );
        alert.showAndWait();
    }

    // WORKING HOURS SETUP

    @FXML
    private void onApplyShift(ActionEvent event) {
        List<DayOfWeek> selectedDays = getSelectedWorkingDays();
        List<String> selectedShifts = getSelectedShifts();

        if (selectedDays.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng chọn ít nhất một ngày làm việc");
            return;
        }

        if (selectedShifts.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng chọn ít nhất một ca làm việc");
            return;
        }

        // Save to memory
        WorkingSchedule schedule = new WorkingSchedule();
        schedule.setWorkingDays(new HashSet<>(selectedDays));
        schedule.setShifts(new HashSet<>(selectedShifts));

        if (currentDoctor != null) {
            doctorSchedules.put(currentDoctor.getId(), schedule);

            String message = String.format(
                    "Đã lưu lịch làm việc cho BS. %s:\nNgày: %s\nCa: %s",
                    currentDoctor.getFullName(),
                    selectedDays,
                    selectedShifts
            );

            showInfo("Thành công", message);
        }

        refreshScheduleView();
    }

    private List<DayOfWeek> getSelectedWorkingDays() {
        List<DayOfWeek> days = new ArrayList<>();

        if (mondayChk.isSelected()) days.add(DayOfWeek.MONDAY);
        if (tuesdayChk.isSelected()) days.add(DayOfWeek.TUESDAY);
        if (wednesdayChk.isSelected()) days.add(DayOfWeek.WEDNESDAY);
        if (thursdayChk.isSelected()) days.add(DayOfWeek.THURSDAY);
        if (fridayChk.isSelected()) days.add(DayOfWeek.FRIDAY);
        if (saturdayChk.isSelected()) days.add(DayOfWeek.SATURDAY);
        if (sundayChk.isSelected()) days.add(DayOfWeek.SUNDAY);

        return days;
    }

    private List<String> getSelectedShifts() {
        List<String> shifts = new ArrayList<>();

        if (morningShiftChk.isSelected()) shifts.add("Sáng (8:00-12:00)");
        if (afternoonShiftChk.isSelected()) shifts.add("Chiều (13:00-17:00)");
        if (eveningShiftChk.isSelected()) shifts.add("Tối (17:00-20:00)");

        return shifts;
    }

    // CONFLICT DETECTION

    private void detectConflicts() {
        List<String> conflicts = new ArrayList<>();

        // Sort appointments theo thời gian bắt đầu
        List<Appointment> sortedApts = appointmentList.stream()
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .collect(Collectors.toList());

        // Check overlap
        for (int i = 0; i < sortedApts.size() - 1; i++) {
            Appointment current = sortedApts.get(i);
            Appointment next = sortedApts.get(i + 1);

            if (current.getEndTime().isAfter(next.getStartTime())) {
                String conflict = String.format(
                        "⚠️ Xung đột: #%d (%s-%s) và #%d (%s-%s)",
                        current.getId(),
                        current.getStartTime().toLocalTime(),
                        current.getEndTime().toLocalTime(),
                        next.getId(),
                        next.getStartTime().toLocalTime(),
                        next.getEndTime().toLocalTime()
                );
                conflicts.add(conflict);
            }
        }

        // Update conflict ListView
        conflictList.setItems(FXCollections.observableArrayList(conflicts));

        if (!conflicts.isEmpty()) {
            System.out.println("⚠️ Found " + conflicts.size() + " conflicts");
        }
    }

    // FILTER & SEARCH

    private void filterAppointments(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            renderSchedule();
            return;
        }

        // Filter appointments chứa keyword trong notes
        List<Appointment> filtered = appointmentList.stream()
                .filter(apt -> apt.getNotes() != null &&
                        apt.getNotes().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // TODO: Re-render chỉ filtered appointments
        System.out.println("🔍 Filtered: " + filtered.size() + " appointments");
    }

    private void applyFilters() {
        Set<AppointmentStatus> selectedStatuses = new HashSet<>();

        if (bookedChk != null && bookedChk.isSelected()) {
            selectedStatuses.add(AppointmentStatus.SCHEDULED);
            selectedStatuses.add(AppointmentStatus.CONFIRMED);
        }
        if (inProgressChk != null && inProgressChk.isSelected()) {
            selectedStatuses.add(AppointmentStatus.IN_PROGRESS);
        }
        if (completedChk != null && completedChk.isSelected()) {
            selectedStatuses.add(AppointmentStatus.COMPLETED);
        }
        if (cancelledChk != null && cancelledChk.isSelected()) {
            selectedStatuses.add(AppointmentStatus.CANCELLED);
        }

        // If no filter selected, show all
        if (selectedStatuses.isEmpty()) {
            renderSchedule();
            return;
        }

        // Filter appointments
        List<Appointment> filteredList = appointmentList.stream()
                .filter(apt -> selectedStatuses.contains(apt.getStatus()))
                .collect(Collectors.toList());

        // Render filtered
        schedulePane.getChildren().clear();

        if (isDayView) {
            drawTimeGrid(TOTAL_HOURS * PIXELS_PER_HOUR);
            for (Appointment apt : filteredList) {
                if (apt.getStartTime().toLocalDate().equals(selectedDate)) {
                    drawAppointmentBlock(apt, 0, schedulePane.getPrefWidth());
                }
            }
        } else {
            // Week view
            drawTimeGrid(TOTAL_HOURS * PIXELS_PER_HOUR);
            for (Appointment apt : filteredList) {
                LocalDate aptDate = apt.getStartTime().toLocalDate();
                if (!aptDate.isBefore(weekStart) && !aptDate.isAfter(weekStart.plusDays(6))) {
                    int dayIndex = (int) ChronoUnit.DAYS.between(weekStart, aptDate);
                    double colWidth = schedulePane.getPrefWidth() / 7.0;
                    double x = dayIndex * colWidth;
                    drawAppointmentBlock(apt, x, colWidth);
                }
            }
        }
    }

    // ADD SCHEDULE

    @FXML
    private void onAddSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FXML/Schedule/AppointmentBooking.fxml")
            );
            Parent root = loader.load();

            Scene scene = addScheduleBtn.getScene();
            scene.setRoot(root);

        } catch (Exception e) {
            showInfo("Lỗi", e.getMessage());
        }
    }

    // SAVE/EXPORT

    @FXML
    private void onSave(ActionEvent event) {
        if (currentDoctor == null) {
            showWarning("Không thể lưu", "Vui lòng chọn bác sĩ trước");
            return;
        }
        
        if (!isAdmin) {
            showWarning("Không có quyền", "Bạn không có quyền chỉnh sửa lịch làm việc");
            return;
        }
        
        System.out.println("💾 Saving working schedule for Doctor #" + currentDoctor.getId());
        
        // Collect working days from checkboxes
        Set<DayOfWeek> workingDays = new HashSet<>();
        if (mondayChk != null && mondayChk.isSelected()) workingDays.add(DayOfWeek.MONDAY);
        if (tuesdayChk != null && tuesdayChk.isSelected()) workingDays.add(DayOfWeek.TUESDAY);
        if (wednesdayChk != null && wednesdayChk.isSelected()) workingDays.add(DayOfWeek.WEDNESDAY);
        if (thursdayChk != null && thursdayChk.isSelected()) workingDays.add(DayOfWeek.THURSDAY);
        if (fridayChk != null && fridayChk.isSelected()) workingDays.add(DayOfWeek.FRIDAY);
        if (saturdayChk != null && saturdayChk.isSelected()) workingDays.add(DayOfWeek.SATURDAY);
        if (sundayChk != null && sundayChk.isSelected()) workingDays.add(DayOfWeek.SUNDAY);
        
        // Collect shifts from checkboxes
        Set<String> shifts = new HashSet<>();
        if (morningShiftChk != null && morningShiftChk.isSelected()) shifts.add("MORNING");
        if (afternoonShiftChk != null && afternoonShiftChk.isSelected()) shifts.add("AFTERNOON");
        if (eveningShiftChk != null && eveningShiftChk.isSelected()) shifts.add("EVENING");
        
        // Validate at least one working day
        if (workingDays.isEmpty()) {
            showWarning("Lưu thất bại", "Vui lòng chọn ít nhất một ngày làm việc");
            return;
        }
        
        // Validate at least one shift
        if (shifts.isEmpty()) {
            showWarning("Lưu thất bại", "Vui lòng chọn ít nhất một ca làm việc");
            return;
        }
        
        // Create/update working schedule
        WorkingSchedule schedule = doctorSchedules.getOrDefault(currentDoctor.getId(), new WorkingSchedule());
        schedule.setWorkingDays(workingDays);
        schedule.setShifts(shifts);
        doctorSchedules.put(currentDoctor.getId(), schedule);
        
        // Log saved data
        System.out.println("✅ Working days: " + workingDays.stream()
            .map(d -> d.toString().substring(0, 3))
            .collect(Collectors.joining(", ")));
        System.out.println("✅ Shifts: " + String.join(", ", shifts));
        
        // TODO: Gọi API backend để lưu vào database khi có endpoint
        // doctorService.saveWorkingSchedule(currentDoctor.getId(), schedule);
        
        showInfo("Lưu thành công", 
            "Đã lưu lịch làm việc của BS. " + currentDoctor.getFullName() + "\n" +
            "Ngày làm: " + workingDays.size() + " ngày/tuần\n" +
            "Ca làm: " + shifts.size() + " ca/ngày");
    }

    @FXML
    private void onExportPdf(ActionEvent event) {
        if (currentDoctor == null) {
            showWarning("Không thể xuất PDF", "Vui lòng chọn bác sĩ trước");
            return;
        }
        
        System.out.println("📄 Exporting schedule to PDF for Doctor #" + currentDoctor.getId());
        
        try {
            // Prepare file name with timestamp
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = String.format("Schedule_%s_%s.txt", 
                currentDoctor.getFullName().replace(" ", "_"), 
                timestamp);
            
            // Build schedule content
            StringBuilder content = new StringBuilder();
            content.append("=".repeat(60)).append("\n");
            content.append("         LỊCH LÀM VIỆC BÁC SĨ\n");
            content.append("=".repeat(60)).append("\n\n");
            
            content.append("Bác sĩ: ").append(currentDoctor.getFullName()).append("\n");
            content.append("Mã bác sĩ: #").append(currentDoctor.getId()).append("\n");
            if (currentDoctor.getLicenseNo() != null && !currentDoctor.getLicenseNo().isEmpty()) {
                content.append("Giấy phép hành nghề: ").append(currentDoctor.getLicenseNo()).append("\n");
            }
            content.append("Ngày xuất: ").append(LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
            
            content.append("-".repeat(60)).append("\n");
            content.append("THỜI GIAN LÀM VIỆC:\n");
            content.append("-".repeat(60)).append("\n");
            
            // Get working schedule
            WorkingSchedule schedule = doctorSchedules.get(currentDoctor.getId());
            if (schedule != null && !schedule.workingDays.isEmpty()) {
                content.append("Ngày làm việc:\n");
                List<DayOfWeek> sortedDays = schedule.workingDays.stream()
                    .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                    .collect(Collectors.toList());
                
                for (DayOfWeek day : sortedDays) {
                    String dayName = getDayNameVietnamese(day);
                    content.append("  • ").append(dayName).append("\n");
                }
                content.append("\n");
                
                if (!schedule.shifts.isEmpty()) {
                    content.append("Ca làm việc:\n");
                    for (String shift : schedule.shifts) {
                        String shiftName = getShiftNameVietnamese(shift);
                        content.append("  • ").append(shiftName).append("\n");
                    }
                }
            } else {
                content.append("Chưa có lịch làm việc được thiết lập.\n");
            }
            
            content.append("\n").append("-".repeat(60)).append("\n");
            content.append("LỊCH HẸN TRONG TUẦN:\n");
            content.append("-".repeat(60)).append("\n");
            
            // Get appointments for current week
            if (appointmentList != null && !appointmentList.isEmpty()) {
                List<Appointment> doctorAppointments = appointmentList.stream()
                    .filter(apt -> apt.getDoctorId() == currentDoctor.getId())
                    .sorted(Comparator.comparing(Appointment::getStartTime))
                    .collect(Collectors.toList());
                
                if (!doctorAppointments.isEmpty()) {
                    for (Appointment apt : doctorAppointments) {
                        content.append(String.format("• %s %s - %s | Bệnh nhân #%d | %s\n",
                            apt.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            apt.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            apt.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            apt.getCustomerId(),
                            apt.getStatus().toString()));
                    }
                } else {
                    content.append("Không có lịch hẹn nào trong tuần này.\n");
                }
            } else {
                content.append("Không có lịch hẹn nào.\n");
            }
            
            content.append("\n").append("=".repeat(60)).append("\n");
            content.append("Hết\n");
            content.append("=".repeat(60)).append("\n");
            
            // Write to file (Desktop location)
            String desktopPath = System.getProperty("user.home") + "\\Desktop\\";
            java.io.File desktopDir = new java.io.File(desktopPath);
            
            // Create Desktop directory if not exists (shouldn't be needed but just in case)
            if (!desktopDir.exists()) {
                desktopDir.mkdirs();
            }
            
            String fullPath = desktopPath + fileName;
            java.nio.file.Path filePath = Paths.get(fullPath);
            
            // Write with explicit charset UTF-8
            Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));
            
            System.out.println("✅ Schedule exported to: " + fullPath);
            showInfo("Xuất file thành công", 
                "Đã xuất lịch làm việc ra file:\n" + fileName + "\n\nVị trí: " + fullPath);
            
            // TODO: Nếu cần PDF thực sự, dùng iText library:
            // com.itextpdf:itextpdf:5.5.13.3
            // hoặc Apache PDFBox
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting schedule: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = "Không thể xuất lịch làm việc:\n\n" + 
                            "Lỗi: " + e.getClass().getSimpleName() + "\n" +
                            "Chi tiết: " + e.getMessage() + "\n\n" +
                            "Đường dẫn dự kiến: " + System.getProperty("user.home") + "\\Desktop\\" + 
                            currentDoctor.getFullName().replace(" ", "_") + "_*.txt";
            
            showError("Lỗi xuất file", errorMsg);
        }
    }
    
    // Helper methods for Vietnamese names
    private String getDayNameVietnamese(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Thứ Hai";
            case TUESDAY: return "Thứ Ba";
            case WEDNESDAY: return "Thứ Tư";
            case THURSDAY: return "Thứ Năm";
            case FRIDAY: return "Thứ Sáu";
            case SATURDAY: return "Thứ Bảy";
            case SUNDAY: return "Chủ Nhật";
            default: return day.toString();
        }
    }
    
    private String getShiftNameVietnamese(String shift) {
        switch (shift.toUpperCase()) {
            case "MORNING": return "Ca sáng (8:00 - 12:00)";
            case "AFTERNOON": return "Ca chiều (13:00 - 17:00)";
            case "EVENING": return "Ca tối (18:00 - 21:00)";
            default: return shift;
        }
    }

    // UNDO/REDO

    private Stack<Object> undoStack = new Stack<>();
    private Stack<Object> redoStack = new Stack<>();

    @FXML
    private void onUndo(ActionEvent event) {
        if (!undoStack.isEmpty()) {
            // TODO: Implement undo logic
            showInfo("Hoàn tác", "Đã hoàn tác thao tác");
        }
    }

    @FXML
    private void onRedo(ActionEvent event) {
        if (!redoStack.isEmpty()) {
            // TODO: Implement redo logic
            showInfo("Làm lại", "Đã làm lại thao tác");
        }
    }

    //HELPER METHODS

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}