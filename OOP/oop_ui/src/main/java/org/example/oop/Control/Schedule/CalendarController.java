package org.example.oop.Control.Schedule;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.example.oop.Service.HttpAppointmentService;
import org.example.oop.Service.HttpDoctorService;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.Appointment;
import org.miniboot.app.domain.models.AppointmentStatus;
import org.miniboot.app.domain.models.Doctor;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * CalendarController - Hiển thị lịch tuần dạng calendar view
 * 
 * Chức năng:
 * - Hiển thị lịch từ Thứ 2 - Chủ nhật
 * - Timeline từ 8:00 - 20:00
 * - Appointments hiển thị dạng blocks trên grid
 * - Chọn bác sĩ để xem lịch
 * - Navigate tuần trước/sau
 */
public class CalendarController implements Initializable {
    
    // SERVICES
    private HttpAppointmentService appointmentService;
    private HttpDoctorService doctorService;
    
    // DATA
    private List<Doctor> doctorList;
    private List<Appointment> appointmentList;
    private Doctor selectedDoctor;
    private LocalDate currentWeekStart; // Thứ 2 của tuần hiện tại
    
    // CONSTANTS
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(20, 0);
    private static final int SLOT_DURATION = 30; // 30 phút
    public static final int PIXELS_PER_HOUR = 60; // 1 giờ = 60px
    public static final int TOTAL_HOURS = 12; // 8:00-20:00 = 12 giờ
    private static final int GRID_HEIGHT = TOTAL_HOURS * PIXELS_PER_HOUR; // 720px
    
    // FXML CONTROLS
    @FXML private GridPane calendarGrid;
    @FXML private VBox timeLabelColumn;
    @FXML private AnchorPane appointmentPane;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private DatePicker weekDatePicker;
    @FXML private Button prevWeekBtn;
    @FXML private Button nextWeekBtn;
    @FXML private Button todayBtn;
    @FXML private Label weekRangeLabel;
    
    // INITIALIZATION
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(SceneManager.getSceneData("selectedDoctor") != null || SceneManager.getSceneData("selectedDate") != null){
            selectedDoctor = SceneManager.getSceneData("selectedDoctor");
            LocalDate selectedDate = SceneManager.getSceneData("selectedDate");
            if (selectedDoctor != null && selectedDate != null) {
                System.out.println("✅ Pre-selecting doctor: " + selectedDoctor.getFullName() +
                        ", date: " + selectedDate);

                // Pass data to calendar
                selectDoctorAndDate(selectedDoctor, selectedDate);
            } else if (selectedDoctor != null) {
                // Chỉ có doctor, date = today
                System.out.println("✅ Pre-selecting doctor: " + selectedDoctor.getFullName());
                selectDoctorAndDate(selectedDoctor, LocalDate.now());
            } else if (selectedDate != null) {
                // Chỉ có date, không có doctor
                System.out.println("✅ Jumping to date: " + selectedDate);
                selectDoctorAndDate(null, selectedDate);
            }
            SceneManager.removeSceneData("selectedDoctor");
            SceneManager.removeSceneData("selectedDate");
        }
        System.out.println("✅ CalendarController initialized");
        
        // 1. Khởi tạo services
        appointmentService = new HttpAppointmentService();
        doctorService = new HttpDoctorService();
        
        // 2. Khởi tạo data
        doctorList = new ArrayList<>();
        appointmentList = new ArrayList<>();
        
        // 3. Set tuần hiện tại (Thứ 2 của tuần này)
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // 4. Setup UI
        setupTimeLabels();
        setupGridLines();
        
        // 5. Setup listeners
        setupListeners();
        
        // 6. Load data
        loadDoctors();
    }
    
    private void setupListeners() {
        // DatePicker listener
        if (weekDatePicker != null) {
            weekDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    currentWeekStart = newDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    updateWeekRangeLabel();
                    loadAppointments();
                }
            });
        }
        
        // Doctor ComboBox listener
        if (doctorComboBox != null) {
            doctorComboBox.setOnAction(e -> {
                int selectedIndex = doctorComboBox.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < doctorList.size()) {
                    selectedDoctor = doctorList.get(selectedIndex);
                    loadAppointments();
                }
            });
        }
    }

    private void setupTimeLabels() {
        timeLabelColumn.getChildren().clear();
        
        LocalTime currentTime = START_TIME;
        while (currentTime.isBefore(END_TIME) || currentTime.equals(END_TIME)) {
            Label timeLabel = new Label(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setPrefHeight(PIXELS_PER_HOUR / 2.0); // 30px cho mỗi 30 phút
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setPadding(new Insets(0, 8, 0, 0));
            
            timeLabelColumn.getChildren().add(timeLabel);
            
            currentTime = currentTime.plusMinutes(30);
        }
    }
    
    private void setupGridLines() {
        // Set grid height
        calendarGrid.setPrefHeight(GRID_HEIGHT);
        
        // Vẽ horizontal lines (mỗi 30 phút)
        for (int i = 0; i <= TOTAL_HOURS * 2; i++) {
            double y = i * (PIXELS_PER_HOUR / 2.0);
            
            for (int col = 0; col < 7; col++) {
                Pane cell = new Pane();
                cell.setPrefHeight(PIXELS_PER_HOUR / 2.0);
                
                // Border
                cell.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0.5;");
                
                // Darker line mỗi giờ
                if (i % 2 == 0) {
                    cell.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 0.5;");
                }
                
                GridPane.setRowIndex(cell, i);
                GridPane.setColumnIndex(cell, col);
                calendarGrid.getChildren().add(cell);
            }
        }
    }

    private void loadDoctors() {
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() {
                return doctorService.getAllDoctors();
            }
        };
        
        task.setOnSucceeded(e -> {
            doctorList = task.getValue();
            
            if (doctorComboBox != null) {
                doctorComboBox.getItems().clear();
                for (Doctor doc : doctorList) {
                    doctorComboBox.getItems().add(doc.getFullName());
                }
                
                if (!doctorList.isEmpty()) {
                    doctorComboBox.getSelectionModel().selectFirst();
                    selectedDoctor = doctorList.get(0);
                    loadAppointments();
                }
            } else {
                // Nếu chưa có ComboBox, chọn doctor đầu tiên
                if (!doctorList.isEmpty()) {
                    selectedDoctor = doctorList.get(0);
                    loadAppointments();
                }
            }
        });
        
        task.setOnFailed(e -> {
            showError("Lỗi", "Không thể tải danh sách bác sĩ");
        });
        
        new Thread(task).start();
    }
    
    private void loadAppointments() {
        if (selectedDoctor == null) return;
        
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        
        Task<List<Appointment>> task = new Task<>() {
            @Override
            protected List<Appointment> call() {
                return appointmentService.getByDoctorAndDateRange(
                    selectedDoctor.getId(),
                    currentWeekStart,
                    weekEnd
                );
            }
        };
        
        task.setOnSucceeded(e -> {
            appointmentList = task.getValue();
            System.out.println("✅ Loaded " + appointmentList.size() + " appointments");
            
            // Update week range label
            updateWeekRangeLabel();
            
            // Render appointments
            renderAppointments();
        });
        
        task.setOnFailed(e -> {
            showError("Lỗi", "Không thể tải danh sách lịch hẹn");
        });
        
        new Thread(task).start();
    }

    private void renderAppointments() {
        // Clear existing appointments
        appointmentPane.getChildren().clear();
        
        // Sort appointments by start time
        appointmentList.sort(Comparator.comparing(Appointment::getStartTime));
        
        for (Appointment apt : appointmentList) {
            drawAppointmentBlock(apt);
        }
    }
    
    private void drawAppointmentBlock(Appointment apt) {
        LocalDateTime startTime = apt.getStartTime();
        LocalDateTime endTime = apt.getEndTime();
        
        // Tính ngày trong tuần (0=Monday, 6=Sunday)
        int dayOfWeek = startTime.getDayOfWeek().getValue() - 1; // 0-6
        
        // Tính vị trí X (cột nào)
        double columnWidth = calendarGrid.getWidth() / 7.0;
        if (columnWidth <= 0) columnWidth = 100; // Default nếu chưa render
        
        double x = dayOfWeek * columnWidth;
        
        // Tính vị trí Y (hàng nào)
        double startY = calculateYPosition(startTime.toLocalTime());
        double endY = calculateYPosition(endTime.toLocalTime());
        double height = endY - startY;
        
        // Tạo block
        VBox block = new VBox(3);
        block.setLayoutX(x + 2);
        block.setLayoutY(startY);
        block.setPrefWidth(columnWidth - 4);
        block.setPrefHeight(height);
        block.setPadding(new Insets(4));
        
        // Style theo status
        block.setStyle(getStyleForStatus(apt.getStatus()));
        
        // Thêm thông tin
        Label timeLabel = new Label(
            startTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
            " - " +
            endTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 10px;");
        
        Label typeLabel = new Label(apt.getAppointmentType().toString());
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px;");
        
        Label customerLabel = new Label("Bệnh nhân #" + apt.getCustomerId());
        customerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px;");
        
        block.getChildren().addAll(timeLabel, typeLabel, customerLabel);
        
        // Click event
        block.setOnMouseClicked(e -> showAppointmentDetail(apt));
        
        // Hover effect
        block.setOnMouseEntered(e -> block.setStyle(getStyleForStatus(apt.getStatus()) + "-fx-opacity: 0.8;"));
        block.setOnMouseExited(e -> block.setStyle(getStyleForStatus(apt.getStatus())));
        
        appointmentPane.getChildren().add(block);
    }
    
    private double calculateYPosition(LocalTime time) {
        long minutesFromStart = java.time.Duration.between(START_TIME, time).toMinutes();
        return (minutesFromStart / 60.0) * PIXELS_PER_HOUR;
    }
    
    private String getStyleForStatus(AppointmentStatus status) {
        String baseStyle = "-fx-background-radius: 5; -fx-cursor: hand; ";
        switch (status) {
            case SCHEDULED:
                return baseStyle + "-fx-background-color: #FFA726;"; // Orange
            case CONFIRMED:
                return baseStyle + "-fx-background-color: #42A5F5;"; // Blue
            case IN_PROGRESS:
                return baseStyle + "-fx-background-color: #66BB6A;"; // Green
            case COMPLETED:
                return baseStyle + "-fx-background-color: #9E9E9E;"; // Gray
            case CANCELLED:
                return baseStyle + "-fx-background-color: #EF5350;"; // Red
            default:
                return baseStyle + "-fx-background-color: #BDBDBD;";
        }
    }

    @FXML
    private void onPrevWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        if (weekDatePicker != null) {
            weekDatePicker.setValue(currentWeekStart);
        }
        updateWeekRangeLabel();
        loadAppointments();
    }
    
    @FXML
    private void onNextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        if (weekDatePicker != null) {
            weekDatePicker.setValue(currentWeekStart);
        }
        updateWeekRangeLabel();
        loadAppointments();
    }
    
    @FXML
    private void onToday() {
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weekDatePicker != null) {
            weekDatePicker.setValue(currentWeekStart);
        }
        updateWeekRangeLabel();
        loadAppointments();
    }
    
    private void updateWeekRangeLabel() {
        if (weekRangeLabel != null) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            weekRangeLabel.setText(
                "Tuần: " + currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter)
            );
        }
    }
    
    /**
     * Public method để pre-select doctor và jump to specific week
     * Called from AppointmentBookingController khi navigate to Calendar
     * 
     * @param doctor Doctor to select (có thể null)
     * @param date Date to jump to
     */
    public void selectDoctorAndDate(Doctor doctor, LocalDate date) {
        System.out.println("🗓️ CalendarController.selectDoctorAndDate() called");
        System.out.println("   Doctor: " + (doctor != null ? doctor.getFullName() : "null"));
        System.out.println("   Date: " + date);
        
        // 1. Select doctor nếu có
        if (doctor != null && doctorList != null) {
            for (int i = 0; i < doctorList.size(); i++) {
                if (doctorList.get(i).getId() == doctor.getId()) {
                    selectedDoctor = doctor;
                    
                    if (doctorComboBox != null) {
                        doctorComboBox.getSelectionModel().select(i);
                        System.out.println("✅ Doctor selected in ComboBox: " + doctor.getFullName());
                    }
                    break;
                }
            }
        }
        
        // 2. Jump to week containing date
        if (date != null) {
            currentWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            if (weekDatePicker != null) {
                weekDatePicker.setValue(date);
                System.out.println("✅ DatePicker updated to: " + date);
            }
            
            updateWeekRangeLabel();
        }
        
        // 3. Reload appointments with new selection
        loadAppointments();
        
        System.out.println("✅ Calendar pre-selection completed");
    }
    
    // ==================== APPOINTMENT DETAIL ====================
    
    private void showAppointmentDetail(Appointment apt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết lịch hẹn");
        alert.setHeaderText("Appointment #" + apt.getId());
        
        String content = String.format(
            "Bệnh nhân ID: %d\n" +
            "Bác sĩ ID: %d\n" +
            "Loại: %s\n" +
            "Thời gian: %s - %s\n" +
            "Trạng thái: %s\n" +
            "Ghi chú: %s",
            apt.getCustomerId(),
            apt.getDoctorId(),
            apt.getAppointmentType(),
            apt.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            apt.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            apt.getStatus(),
            apt.getNotes() != null ? apt.getNotes() : "Không có"
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
