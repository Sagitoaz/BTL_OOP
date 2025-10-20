package org.example.oop.Control.Schedule;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.example.oop.Services.HttpAppointmentService;
import org.example.oop.Services.HttpDoctorService;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.Doctor;
import org.miniboot.app.domain.models.TimeSlot;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class DoctorScheduleController implements Initializable {

    // SERVICES
    private HttpDoctorService doctorService;
    private HttpAppointmentService appointmentService;

    // DATA
    private ObservableList<Doctor> doctorList;
    private ObservableList<Appointment> appointmentList;
    private Doctor currentDoctor; // Bác sĩ đang xem lịch
    private LocalDate selectedDate;
    private boolean isDayView = true; // true = Day View, false = Week View

    // CONSTANTS
    private static final LocalTime START_TIME = LocalTime.of(8, 0);  // 8:00 AM
    private static final LocalTime END_TIME = LocalTime.of(20, 0);   // 8:00 PM
    private static final int SLOT_DURATION = 30; // 30 phút
    private static final int PIXELS_PER_HOUR = 60; // Chiều cao 1 giờ = 60px

    // FXML controls
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
        datePicker.setValue(selectedDate);

        // 4. Setup ToggleGroup cho Day/Week view
        ToggleGroup viewGroup = new ToggleGroup();
        dayViewBtn.setToggleGroup(viewGroup);
        weekViewBtn.setToggleGroup(viewGroup);
        dayViewBtn.setSelected(true); // Mặc định Day View

        // 5. Setup listeners
        setupListeners();

        // 6. Load dữ liệu ban đầu
        loadInitialData();
    }

    private void setupListeners() {
        // DatePicker listener
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                selectedDate = newDate;
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

            if (!doctors.isEmpty()) {
                // Chọn bác sĩ đầu tiên (hoặc lấy từ session user)
                currentDoctor = doctors.get(0);
                System.out.println("✅ Selected doctor: " + currentDoctor.getFullName());

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

        if (isDayView) {
            renderDayView();
        } else {
            renderWeekView();
        }
    }

    private void renderDayView() {
        // Tính chiều cao tổng (8:00 - 20:00 = 12 giờ = 12 * 60px)
        int totalHours = (int) java.time.Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;

        schedulePane.setPrefHeight(totalHeight);

        // 1. Vẽ grid lines (mỗi 30 phút)
        drawTimeGrid(totalHeight);

        // 2. Vẽ appointments
        for (Appointment apt : appointmentList) {
            // Chỉ vẽ appointment trong ngày đã chọn
            if (apt.getStartTime().toLocalDate().equals(selectedDate)) {
                drawAppointmentBlock(apt, 0, schedulePane.getPrefWidth());
            }
        }
    }

    private void renderWeekView() {
        // Week view: 7 cột (Thứ 2 - CN)
        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int totalHours = (int) java.time.Duration.between(START_TIME, END_TIME).toHours();
        double totalHeight = totalHours * PIXELS_PER_HOUR;
        schedulePane.setPrefHeight(totalHeight);

        double columnWidth = schedulePane.getPrefWidth() / 7.0;

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

        // Tạo block (VBox chứa thông tin appointment)
        VBox block = new VBox(5);
        block.setLayoutX(xStart + 2);
        block.setLayoutY(startY);
        block.setPrefWidth(width - 4);
        block.setPrefHeight(height);
        block.setPadding(new Insets(5));

        // Set màu theo status
        String styleClass = getStyleClassForStatus(apt.getStatus());
        block.setStyle(getStyleForStatus(apt.getStatus()));

        // Thêm thông tin
        Label timeLabel = new Label(aptStartTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " - " + aptEndTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label typeLabel = new Label(apt.getAppointmentType().toString());
        typeLabel.setStyle("-fx-text-fill: white;");

        Label notesLabel = new Label(apt.getNotes() != null ? apt.getNotes() : "");
        notesLabel.setWrapText(true);
        notesLabel.setStyle("-fx-text-fill: white;");

        block.getChildren().addAll(timeLabel, typeLabel, notesLabel);

        // Click event
        block.setOnMouseClicked(e -> onAppointmentClicked(apt));

        schedulePane.getChildren().add(block);
    }

    private double calculateYPosition(LocalTime time) {
        long minutesFromStart = java.time.Duration.between(START_TIME, time).toMinutes();
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
        // Lấy các ngày đã chọn
        List<DayOfWeek> selectedDays = getSelectedWorkingDays();

        // Lấy các ca đã chọn
        List<String> selectedShifts = getSelectedShifts();

        if (selectedDays.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng chọn ít nhất một ngày làm việc");
            return;
        }

        if (selectedShifts.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng chọn ít nhất một ca làm việc");
            return;
        }

        // TODO: Gọi API để lưu working hours vào database
        // Hiện tại chỉ hiển thị thông báo
        String message = "Áp dụng ca làm việc:\n" +
                "Ngày: " + selectedDays + "\n" +
                "Ca: " + selectedShifts;

        showInfo("Thành công", message);

        // Refresh lại schedule
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
        // Lấy các filter đã chọn
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

        // Nếu không chọn filter nào → hiển thị tất cả
        if (selectedStatuses.isEmpty()) {
            renderSchedule();
            return;
        }

        // Filter và re-render
        // TODO: Implement filtered rendering
    }

    // ADD SCHEDULE

    @FXML
    private void onAddSchedule(ActionEvent event) {
        // TODO: Mở dialog để thêm working hours mới
        showInfo("Thông báo", "Chức năng đang phát triển");
    }

    // SAVE/EXPORT

    @FXML
    private void onSave(ActionEvent event) {
        // TODO: Lưu thay đổi vào database
        showInfo("Thông báo", "Đã lưu lịch làm việc");
    }

    @FXML
    private void onExportPdf(ActionEvent event) {
        // TODO: Xuất PDF lịch làm việc
        showInfo("Thông báo", "Chức năng xuất PDF đang phát triển");
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