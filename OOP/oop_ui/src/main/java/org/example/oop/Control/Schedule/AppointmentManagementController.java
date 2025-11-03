package org.example.oop.Control.Schedule;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.example.oop.Service.CustomerRecordService;
import org.example.oop.Service.HttpAppointmentService;
import org.example.oop.Service.HttpDoctorService;
import org.example.oop.Utils.SceneConfig;
import org.example.oop.Utils.SceneManager;
import org.miniboot.app.domain.models.*;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppointmentManagementController implements Initializable {
    // Services
    private HttpAppointmentService appointmentService;
    private HttpDoctorService doctorService;
    private CustomerRecordService customerService;

    // Data
    private ObservableList<Appointment> appointmentList;
    private ObservableList<Doctor> doctorList;
    private Appointment selectedAppointment;
    private Appointment originalAppointment; // Để revert changes
    
    // Customer name cache để hiển thị trong table
    private Map<Integer, String> customerNameCache = new HashMap<>();

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
        customerService = CustomerRecordService.getInstance();

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
    private void handleBackButton(){
        System.out.println("🔙 Back button clicked");
        SceneManager.goBack();
    }
    @FXML
    private void handleForwardButton(){
        SceneManager.goForward();
    }

    @FXML
    private void handleReloadButton(){
        System.out.println("🔄 Reloading Appointment Booking view");
        //SceneManager.reloadScene();
        SceneManager.reloadCurrentScene();
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
        try {
            SceneManager.switchScene(SceneConfig.APPOINTMENT_BOOKING_FXML, SceneConfig.APPOINTMENT_BOOKING_FXML);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            showAlert("Lỗi: " + e.getMessage());
        }
    }

    @FXML
    private void onConfirm(ActionEvent event) {
        if (selectedAppointment == null) {
            showAlert("Vui lòng chọn lịch hẹn");
            return;
        }

        if (!showConfirm("Xác nhận lịch hẹn #" + selectedAppointment.getId() + "?")) {
            return;
        }

        selectedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        Task<Appointment> task = new Task<>() {
            @Override
            protected Appointment call() {
                return appointmentService.update(selectedAppointment);
            }
        };

        task.setOnSucceeded(e -> {
            showAlert("Đã xác nhận lịch hẹn");
            loadAppointments();
        });

        task.setOnFailed(e -> {
            showAlert("Lỗi: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        if (selectedAppointment == null) {
            showAlert("Vui lòng chọn lịch hẹn");
            return;
        }

        if (!showConfirm("Hủy lịch hẹn #" + selectedAppointment.getId() + "?")) {
            return;
        }

        selectedAppointment.setStatus(AppointmentStatus.CANCELLED);

        Task<Appointment> task = new Task<>() {
            @Override
            protected Appointment call() {
                return appointmentService.update(selectedAppointment);
            }
        };

        task.setOnSucceeded(e -> {
            showAlert("Đã hủy lịch hẹn");
            loadAppointments();
        });

        task.setOnFailed(e -> {
            showAlert("Lỗi: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        System.out.println("Refreshing...");
        loadAppointments();
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (selectedAppointment == null) {
            showAlert("Không có lịch hẹn để lưu");
            return;
        }

        // Validate
        if (datePicker.getValue() == null) {
            showAlert("Vui lòng chọn ngày");
            return;
        }

        try {
            // Update from form
            LocalDate date = datePicker.getValue();
            LocalTime start = LocalTime.parse(startTimeField.getText());
            LocalTime end = LocalTime.parse(endTimeField.getText());

            selectedAppointment.setStartTime(LocalDateTime.of(date, start));
            selectedAppointment.setEndTime(LocalDateTime.of(date, end));
            selectedAppointment.setStatus(AppointmentStatus.valueOf(statusCombo.getValue()));
            selectedAppointment.setAppointmentType(AppointmentType.valueOf(serviceCombo.getValue().toUpperCase()));
            selectedAppointment.setNotes(noteArea.getText());

            // Call API
            Task<Appointment> task = new Task<>() {
                @Override
                protected Appointment call() {
                    return appointmentService.update(selectedAppointment);
                }
            };

            task.setOnSucceeded(e -> {
                showAlert("Đã lưu thay đổi");
                originalAppointment = cloneAppointment(selectedAppointment);
                loadAppointments();
            });

            task.setOnFailed(e -> {
                showAlert("Lỗi: " + task.getException().getMessage());
            });

            new Thread(task).start();

        } catch (Exception e) {
            showAlert("Lỗi: " + e.getMessage());
        }
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
        if (selectedAppointment == null) {
            showAlert("Vui lòng chọn lịch hẹn");
            return;
        }

        if (!showConfirm("Xóa lịch hẹn #" + selectedAppointment.getId() + "?\nThao tác này không thể hoàn tác!")) {
            return;
        }

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return appointmentService.delete(selectedAppointment.getId());
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert("Đã xóa lịch hẹn");
                selectedAppointment = null;
                originalAppointment = null;
                clearDetailForm();
                loadAppointments();
            } else {
                showAlert("Xóa thất bại");
            }
        });

        task.setOnFailed(e -> {
            showAlert("Lỗi: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void onChoosePatient(ActionEvent event) {
        try {
            System.out.println("🔍 Opening CustomerHub in selection mode...");
            


            Runnable runnable = () -> {
                System.out.println("✅ CustomerHub closed");
                Object controllerObj = ((FXMLLoader)SceneManager.getSceneData("fxmlLoader") ).getController();
                System.out.println("🔍 Retrieved controller: " + controllerObj);
                // Kiểm tra controller type (để tránh ClassCastException)
                if (controllerObj != null) {
                    try {
                        // Dùng reflection để gọi getSelectedCustomer()
                        Method getSelectedMethod =
                                controllerObj.getClass().getMethod("getSelectedCustomer");
                        Customer selectedCustomer = (Customer) getSelectedMethod.invoke(controllerObj);

                        if (selectedCustomer != null) {
                            System.out.println("✅ Auto-selected customer: " + selectedCustomer.getFullName());
                            updatePatientField(selectedCustomer);
                        } else {
                            System.out.println("⚠️ No customer selected");
                        }
                    } catch (Exception ex) {
                        System.err.println("⚠️ Could not get selected customer (reflection failed): " + ex.getMessage());
                        // Fallback: Show manual input dialog
                        showManualCustomerIdDialog();
                    }
                    finally {
                        // Clear temporary data
                        SceneManager.removeSceneData("fxmlLoader");
                    }
                } else {
                    SceneManager.removeSceneData("fxmlLoader");
                    showManualCustomerIdDialog();
                }
            };
            SceneManager.openModalWindow(SceneConfig.CUSTOMER_HUB_FXML, SceneConfig.Titles.CUSTOMER_HUB, runnable);

        } catch (Exception e) {
            System.err.println("❌ Error opening CustomerHub: " + e.getMessage());
            e.printStackTrace();
            showAlert("Không thể mở màn hình chọn bệnh nhân.\n" + e.getMessage());
        }
    }
    
    /**
     * Fallback method: Show manual input dialog nếu auto-selection fail
     */
    private void showManualCustomerIdDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chọn bệnh nhân");
        dialog.setHeaderText("Nhập ID bệnh nhân đã chọn trong CustomerHub:");
        dialog.setContentText("Customer ID:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(id -> {
            try {
                int customerId = Integer.parseInt(id);
                loadCustomerAndUpdate(customerId);
            } catch (NumberFormatException ex) {
                showAlert("ID không hợp lệ. Vui lòng nhập số.");
            }
        });
    }
    
    /**
     * Load customer info và update vào form
     */
    private void loadCustomerAndUpdate(int customerId) {
        System.out.println("🔍 Loading customer #" + customerId);
        
        Task<Customer> task = new Task<>() {
            @Override
            protected Customer call() {
                // Search by ID (convert to string)
                var response = customerService.searchCustomers(String.valueOf(customerId), null, null, null);
                
                if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                    // Find customer with exact ID match
                    return response.getData().stream()
                        .filter(c -> c.getId() == customerId)
                        .findFirst()
                        .orElse(null);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(evt -> {
            Customer customer = task.getValue();
            if (customer != null) {
                updatePatientField(customer);
            } else {
                showAlert("Không tìm thấy bệnh nhân với ID: " + customerId);
            }
        });
        
        task.setOnFailed(evt -> {
            showAlert("Lỗi khi tải thông tin bệnh nhân:\n" + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    /**
     * Update patient field với customer info
     */
    private void updatePatientField(Customer customer) {
        if (selectedAppointment != null) {
            selectedAppointment.setCustomerId(customer.getId());
            
            // Update patient field với format: "Tên (ID: #123)"
            String patientInfo = String.format("%s (ID: #%d)", 
                customer.getFullName(), 
                customer.getId());
            patientField.setText(patientInfo);
            
            System.out.println("✅ Patient updated: " + customer.getFullName() + " (ID: " + customer.getId() + ")");
            
            // Show success message
            showAlert("Đã chọn bệnh nhân: " + customer.getFullName());
        } else {
            showAlert("Vui lòng chọn một lịch hẹn trước khi đổi bệnh nhân.");
        }
    }

    @FXML
    private void onSendSms(ActionEvent event) {
        // TODO: Implement send SMS logic
    }

    @FXML
    private void onSendEmail(ActionEvent event) {
        if (selectedAppointment == null) {
            showAlert("Vui lòng chọn lịch hẹn để gửi email");
            return;
        }
        
        // Get customer info
        int customerId = selectedAppointment.getCustomerId();
        String customerName = customerNameCache.get(customerId);
        if (customerName == null) {
            customerName = "Bệnh nhân #" + customerId;
        }
        
        // Get doctor info
        int doctorId = selectedAppointment.getDoctorId();
        Doctor doctor = doctorList.stream()
                .filter(d -> d.getId() == doctorId)
                .findFirst()
                .orElse(null);
        String doctorName = doctor != null ? doctor.getFullName() : "Bác sĩ #" + doctorId;
        
        // Mock email address (thực tế cần load từ customer data)
        String email = "patient@example.com"; // TODO: Get from customer
        
        // Email subject
        String subject = "Nhắc lịch khám - ABC Eye Clinic";
        
        // Email body
        String body = String.format(
            "Kính gửi %s,\n\n" +
            "Đây là email nhắc lịch khám của quý khách tại ABC Eye Clinic:\n\n" +
            "📋 Mã lịch hẹn: #%d\n" +
            "👤 Bệnh nhân: %s\n" +
            "👨‍⚕️ Bác sĩ: %s\n" +
            "📅 Ngày khám: %s\n" +
            "🕐 Giờ khám: %s - %s\n" +
            "📍 Địa điểm: ABC Eye Clinic\n" +
            "📌 Trạng thái: %s\n\n" +
            "Ghi chú: %s\n\n" +
            "Vui lòng đến đúng giờ để được phục vụ tốt nhất.\n" +
            "Nếu cần hủy hoặc đổi lịch, vui lòng liên hệ: (024) 1234-5678\n\n" +
            "Trân trọng,\n" +
            "ABC Eye Clinic\n" +
            "Website: www.abceyeclinic.vn\n" +
            "Hotline: (024) 1234-5678",
            customerName,
            selectedAppointment.getId(),
            customerName,
            doctorName,
            selectedAppointment.getStartTime().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            ),
            selectedAppointment.getStartTime().toLocalTime().format(
                DateTimeFormatter.ofPattern("HH:mm")
            ),
            selectedAppointment.getEndTime().toLocalTime().format(
                DateTimeFormatter.ofPattern("HH:mm")
            ),
            selectedAppointment.getStatus().toString(),
            selectedAppointment.getNotes() != null ? selectedAppointment.getNotes() : "(Không có)"
        );
        
        // Show confirmation dialog with email preview
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận gửi Email");
        confirmDialog.setHeaderText("Gửi email nhắc lịch đến: " + email);
        
        // Create TextArea for email preview
        TextArea previewArea = new TextArea();
        previewArea.setText("Subject: " + subject + "\n\n" + body);
        previewArea.setWrapText(true);
        previewArea.setEditable(false);
        previewArea.setPrefRowCount(20);
        previewArea.setPrefColumnCount(60);
        
        confirmDialog.getDialogPane().setContent(previewArea);
        confirmDialog.getDialogPane().setPrefWidth(700);
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Actual Email API call here
            // Tạm thời mock success
            
            showAlert("✅ Đã gửi email thành công đến:\n" + email);
            
            // Add to timeline
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            timelineList.getItems().add("📧 Đã gửi email lúc: " + timestamp);
            
            System.out.println("✅ Email sent to " + email + " for appointment #" + selectedAppointment.getId());
        } else {
            System.out.println("⚠️ Email sending cancelled by user");
        }
    }

    @FXML
    private void onSaveNote(ActionEvent event) {
        if (selectedAppointment == null) {
            showAlert("Vui lòng chọn lịch hẹn để thêm ghi chú");
            return;
        }
        
        String extraNote = extraNoteArea.getText();
        
        if (extraNote == null || extraNote.trim().isEmpty()) {
            showAlert("Vui lòng nhập ghi chú trước khi lưu");
            return;
        }
        
        // Append to existing notes với timestamp
        String currentNotes = selectedAppointment.getNotes();
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );
        
        String newNotes;
        if (currentNotes == null || currentNotes.trim().isEmpty()) {
            newNotes = "--- Ghi chú thêm (" + timestamp + ") ---\n" + extraNote.trim();
        } else {
            newNotes = currentNotes + "\n\n--- Ghi chú thêm (" + timestamp + ") ---\n" + extraNote.trim();
        }
        
        selectedAppointment.setNotes(newNotes);
        
        // Update to database
        System.out.println("💾 Saving extra note for appointment #" + selectedAppointment.getId());
        
        Task<Appointment> task = new Task<>() {
            @Override
            protected Appointment call() {
                return appointmentService.update(selectedAppointment);
            }
        };
        
        task.setOnSucceeded(evt -> {
            Appointment updated = task.getValue();
            if (updated != null) {
                // Update main note area trong Details tab
                noteArea.setText(updated.getNotes());
                
                // Clear extra note area
                extraNoteArea.clear();
                
                // Add to timeline
                timelineList.getItems().add("📝 Thêm ghi chú lúc: " + timestamp);
                
                // Update selectedAppointment reference
                selectedAppointment.setNotes(updated.getNotes());
                
                // Refresh table để cập nhật note column
                appointmentTable.refresh();
                
                showAlert("✅ Đã lưu ghi chú thành công");
                
                System.out.println("✅ Extra note saved successfully");
            } else {
                showAlert("❌ Lưu ghi chú thất bại");
            }
        });
        
        task.setOnFailed(evt -> {
            System.err.println("❌ Error saving note: " + task.getException().getMessage());
            showAlert("Lỗi khi lưu ghi chú:\n" + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }

    @FXML
    private void onFirstPage(ActionEvent event) {
        currentPage = 1;
        loadAppointments();
    }

    @FXML
    private void onPrevPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            loadAppointments();
        }
    }

    @FXML
    private void onNextPage(ActionEvent event) {
        if (currentPage < totalPages) {
            currentPage++;
            loadAppointments();
        }
    }

    @FXML
    private void onLastPage(ActionEvent event) {
        currentPage = totalPages;
        loadAppointments();
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
            int customerId = cellData.getValue().getCustomerId();
            // Lấy customer name từ cache
            String customerName = customerNameCache.get(customerId);
            if (customerName != null) {
                return new SimpleStringProperty(customerName + " (#" + customerId + ")");
            } else {
                return new SimpleStringProperty("Bệnh nhân #" + customerId);
            }
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
                if(SceneManager.getSceneData("role") == UserRole.CUSTOMER){
                    Customer customer = SceneManager.getSceneData("accountData");
                    int customerId = customer.getId();
                    return appointmentService.getAppointmentsFiltered(
                            doctorId,
                            customerId,
                            status,
                            fromDate,
                            toDate,
                            search
                    );
                }
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
            
            // ✅ Load customer names cho tất cả appointments
            loadCustomerNamesForAppointments(appointments);
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
        
        // Load customer name (check cache first)
        int customerId = appointment.getCustomerId();
        if (customerNameCache.containsKey(customerId)) {
            // Use cached name
            String customerName = customerNameCache.get(customerId);
            patientField.setText(customerName + " (ID: #" + customerId + ")");
        } else {
            // Load async
            patientField.setText("Đang tải... #" + customerId);
            loadCustomerNameAsync(customerId);
        }

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
    
    /**
     * Load customer names cho tất cả appointments trong list
     */
    private void loadCustomerNamesForAppointments(List<Appointment> appointments) {
        // Collect unique customer IDs chưa có trong cache
        Set<Integer> customerIdsToLoad = new HashSet<>();
        for (Appointment apt : appointments) {
            int customerId = apt.getCustomerId();
            if (!customerNameCache.containsKey(customerId)) {
                customerIdsToLoad.add(customerId);
            }
        }
        
        if (customerIdsToLoad.isEmpty()) {
            System.out.println("✅ All customer names already cached");
            return;
        }
        
        System.out.println("🔍 Loading " + customerIdsToLoad.size() + " customer names...");
        
        // Load từng customer async (có thể optimize bằng batch API sau)
        for (Integer customerId : customerIdsToLoad) {
            loadCustomerNameAsync(customerId);
        }
    }
    
    /**
     * Load customer name async và cache
     */
    private void loadCustomerNameAsync(int customerId) {
        Task<Customer> task = new Task<>() {
            @Override
            protected Customer call() {
                // Search by ID
                var response = customerService.searchCustomers(String.valueOf(customerId), null, null, null);
                
                if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                    return response.getData().stream()
                        .filter(c -> c.getId() == customerId)
                        .findFirst()
                        .orElse(null);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(evt -> {
            Customer customer = task.getValue();
            if (customer != null) {
                // Cache name
                customerNameCache.put(customerId, customer.getFullName());
                
                // Update patientField nếu vẫn đang show customer này
                if (selectedAppointment != null && selectedAppointment.getCustomerId() == customerId) {
                    patientField.setText(customer.getFullName() + " (ID: #" + customerId + ")");
                }
                
                // Refresh table để cập nhật customer name
                appointmentTable.refresh();
                
                System.out.println("✅ Loaded customer name: " + customer.getFullName() + " (ID: " + customerId + ")");
            }
        });
        
        task.setOnFailed(evt -> {
            System.err.println("❌ Failed to load customer #" + customerId);
        });
        
        new Thread(task).start();
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

    private void clearDetailForm() {
        txtId.clear();
        datePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        patientField.clear();
        doctorCombo.setValue(null);
        serviceCombo.setValue(null);
        statusCombo.setValue(null);
        noteArea.clear();
        saveBtn.setDisable(true);
        revertBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }
}
