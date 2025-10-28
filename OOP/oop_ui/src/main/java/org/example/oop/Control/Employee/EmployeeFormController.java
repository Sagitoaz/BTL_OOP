package org.example.oop.Control.Employee;

import org.example.oop.Control.BaseController;
import org.example.oop.Service.HttpEmployeeService;
import org.miniboot.app.domain.models.Employee;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeFormController extends BaseController {

     // Các trường nhập liệu cho thông tin nhân viên
     @FXML
     private TextField usernameField; // Tên đăng nhập
     @FXML
     private PasswordField passwordField; // Mật khẩu
     @FXML
     private TextField firstnameField; // Họ và tên đệm
     @FXML
     private TextField lastnameField; // Tên
     @FXML
     private TextField emailField; // Email
     @FXML
     private TextField phoneField; // Số điện thoại
     @FXML
     private ComboBox<String> roleComboBox; // Vai trò
     @FXML
     private TextField licenseNoField; // Số giấy phép (nếu là bác sĩ)
     @FXML
     private TextField avatarField; // URL ảnh đại diện
     @FXML
     private CheckBox activeCheckBox; // Trạng thái hoạt động
     @FXML
     private Button saveButton; // Nút Save
     @FXML
     private Button cancelButton; // Nút Cancel

     private final HttpEmployeeService employeeService = new HttpEmployeeService();

     @FXML
     public void initialize() {
          // Đảm bảo các sự kiện nút được xử lý
          saveButton.setOnAction(event -> handleSave());
          cancelButton.setOnAction(event -> handleCancel());

          // Gán các giá trị mặc định cho ComboBox vai trò
          roleComboBox.getItems().setAll("doctor", "nurse");

          // Ẩn trường LicenseNo nếu không phải là bác sĩ
          licenseNoField.setVisible(false);
          licenseNoField.setManaged(false);
          roleComboBox.setOnAction(e -> handleRoleChange());
     }

     @FXML
     private void handleRoleChange() {
          // Hiển thị trường LicenseNo nếu người dùng chọn bác sĩ
          if ("doctor".equals(roleComboBox.getValue())) {
               licenseNoField.setVisible(true);
               licenseNoField.setManaged(true);
          } else {
               licenseNoField.setVisible(false);
               licenseNoField.setManaged(false);
          }
     }

     @FXML
     private void handleSave() {
          // Lấy thông tin từ các trường nhập liệu
          String username = usernameField.getText();
          String password = passwordField.getText();
          String firstname = firstnameField.getText();
          String lastname = lastnameField.getText();
          String email = emailField.getText();
          String phone = phoneField.getText();
          String role = roleComboBox.getValue();
          String licenseNo = licenseNoField.getText();
          String avatar = avatarField.getText();
          boolean isActive = activeCheckBox.isSelected();

          // Kiểm tra thông tin đầu vào
          if (username.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()
                    || role == null) {
               showError("Vui lòng nhập đầy đủ thông tin.");
               return;
          }

          // Tạo một đối tượng Employee mới từ thông tin nhập vào
          Employee newEmployee = new Employee();
          newEmployee.setUsername(username);
          newEmployee.setPassword(password); // Lưu ý: Mật khẩu cần mã hóa (hash)
          newEmployee.setFirstname(firstname);
          newEmployee.setLastname(lastname);
          newEmployee.setEmail(email);
          newEmployee.setPhone(phone);
          newEmployee.setRole(role); // Vai trò (doctor/nurse)
          newEmployee.setLicenseNo("doctor".equals(role) ? licenseNo : null); // Chỉ cần số giấy phép nếu là bác sĩ
          newEmployee.setAvatar(avatar);
          newEmployee.setActive(isActive);

          // Gọi API để thêm nhân viên
          executeAsync(
                    () -> {
                         try {
                              return employeeService.createEmployee(newEmployee); // Gọi phương thức tạo nhân viên
                         } catch (Exception e) {
                              showError("Lỗi thêm nhân viên: " + e.getMessage());
                              return null;
                         }
                    },
                    this::handleSaveResponse);
     }

     private void handleSaveResponse(Employee savedEmployee) {
          if (savedEmployee != null) {
               showSuccess("Nhân viên đã được thêm thành công.");
               closeWindow();
          } else {
               showError("Không thể thêm nhân viên.");
          }
     }

     @FXML
     private void handleCancel() {
          // Đóng cửa sổ khi nhấn nút "Cancel"
          closeWindow();
     }

     private void closeWindow() {
          Stage stage = (Stage) cancelButton.getScene().getWindow();
          stage.close();
     }

     // Các phương thức xử lý sự kiện cho hover
     @FXML
     private void onButtonHover() {
          cancelButton.setStyle("-fx-background-color: #FFDD57;");
     }

     @FXML
     private void onButtonExit() {
          cancelButton.setStyle("-fx-background-color: #FFFFFF;");
     }
}