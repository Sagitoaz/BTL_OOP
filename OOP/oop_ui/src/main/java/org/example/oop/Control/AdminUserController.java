package org.example.oop.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.oop.Data.models.User;
import org.example.oop.Data.models.UserRole;
import org.example.oop.Data.repositories.UserRepository;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AdminUserController
 *
 * Mục đích lớp:
 * - Quản lý giao diện admin cho chức năng quản lý người dùng (CRUD) trong ứng dụng JavaFX.
 * - Tương tác trực tiếp với UserRepository để đọc/ghi dữ liệu người dùng.
 *
 * Những điểm quan trọng cần lưu ý khi đọc/maintain code:
 * 1) JavaFX lifecycle:
 *    - Phương thức initialize() được gọi khi FXML được load; ở đây khởi tạo repository,
 *      thiết lập các cột bảng, nạp dữ liệu và đăng listener cho selection model của TableView.
 *
 * 2) Binding TableView/Columns:
 *    - Các TableColumn được cấu hình bằng PropertyValueFactory để ánh xạ tên thuộc tính của model User.
 *    - Đảm bảo tên trong PropertyValueFactory khớp với getter trong class User (ví dụ "username" -> getUsername()).
 *
 * 3) Path dữ liệu và repository:
 *    - UserRepository hiện tại thao tác trên file/nguồn cục bộ (được implement trong project). Với hệ thống lớn,
 *      nên chuyển sang DB.
 *
 * 4) Validation và UX:
 *    - Trước khi Add/Update phải validate các trường bắt buộc (id, username, password, fullName, email, role...).
 *    - Hiện tại mật khẩu được nhập trực tiếp và lưu vào model; trong môi trường production cần băm mật khẩu
 *      trước khi lưu (sử dụng PasswordService/bcrypt/argon2) và không hiển thị mật khẩu trên UI.
 *
 * 5) Security & Data leakage:
 *    - Không hiển thị mật khẩu khi populateFields (đặt rỗng PasswordField).
 *    - Tránh log hoặc showAlert chứa mật khẩu.
 *
 * 6) Concurrency / Threading:
 *    - Các thao tác repository hiện được gọi trực tiếp trong UI thread. Nếu thao tác I/O nặng cần chạy
 *      background task (Task/Service) để tránh block giao diện.
 *
 * 7) Error handling:
 *    - Hiện tại errors được báo bằng showAlert; cân nhắc dùng logger cho debug thông tin chi tiết.
 */
public class AdminUserController implements Initializable {

    // === UI controls (được inject từ FXML) ===
    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> colId;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, String> colFullName;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, UserRole> colRole;

    @FXML
    private TableColumn<User, Boolean> colActive;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private ComboBox<UserRole> cbRole;

    @FXML
    private CheckBox chkActive;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    // Repository và danh sách observable cho TableView
    private UserRepository userRepository;
    private ObservableList<User> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userRepository = new UserRepository();
        userList = FXCollections.observableArrayList();

        // Thiết lập ánh xạ cột -> thuộc tính của model User
        // Lưu ý: tên thuộc tính phải khớp với getter trong User class
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Cấu hình combobox role
        cbRole.setItems(FXCollections.observableArrayList(UserRole.values()));

        // Load dữ liệu ban đầu lên bảng
        loadUsers();

        // Listener khi chọn 1 hàng trong bảng: populate các field tương ứng
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    /**
     * Load users từ repository và gán vào TableView.
     * Chú ý: nếu repository thực hiện I/O nặng, nên gọi trong background thread.
     */
    private void loadUsers() {
        List<User> users = userRepository.findAll();
        userList.clear();
        userList.addAll(users);
        userTable.setItems(userList);
    }

    /**
     * Điền dữ liệu user vào các control form để người dùng có thể sửa.
     * - Không hiển thị mật khẩu (đặt rỗng) để tránh rò rỉ.
     */
    private void populateFields(User user) {
        txtId.setText(user.getId());
        txtUsername.setText(user.getUsername());
        txtPassword.setText(""); // Không hiển thị mật khẩu
        txtFullName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());
        cbRole.setValue(user.getRole());
        chkActive.setSelected(user.isActive());
    }

    /**
     * Xử lý sự kiện Add
     * - Thu thập dữ liệu từ form
     * - Validate các trường bắt buộc
     * - Tạo instance User phù hợp theo role và lưu vào repository
     * - Refresh bảng và clear form
     */
    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String id = txtId.getText().trim();
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            UserRole role = cbRole.getValue();
            boolean active = chkActive.isSelected();

            // Validate các trường bắt buộc
            if (id.isEmpty() || username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || role == null) {
                showAlert("Error", "Please fill all required fields.");
                return;
            }

            // Tạo user instance theo role
            User newUser = createUserInstance(id, username, password, role, email, fullName, phone);
            newUser.setActive(active);

            // Lưu user vào repository
            userRepository.save(newUser);

            // Cập nhật giao diện
            loadUsers();
            clearFields();
            showAlert("Success", "User added successfully.");
        } catch (Exception e) {
            showAlert("Error", "Failed to add user: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện Update
     * - Lấy user đang được chọn
     * - Cập nhật các trường nếu client đã thay đổi
     * - Gọi repository.update (nếu repository hỗ trợ) hoặc save
     */
    @FXML
    void handleUpdate(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to update.");
            return;
        }

        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            UserRole role = cbRole.getValue();
            boolean active = chkActive.isSelected();

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || role == null) {
                showAlert("Error", "Please fill all required fields.");
                return;
            }

            // Cập nhật model
            selectedUser.setUsername(username);
            if (!password.isEmpty()) {
                selectedUser.setPassword(password); // Trong production: hash password trước khi lưu
            }
            selectedUser.setFullName(fullName);
            selectedUser.setEmail(email);
            selectedUser.setPhone(phone);
            selectedUser.setRole(role);
            selectedUser.setActive(active);

            // Gọi repository để cập nhật
            userRepository.update(selectedUser);

            // Refresh UI
            loadUsers();
            clearFields();
            showAlert("Success", "User updated successfully.");
        } catch (Exception e) {
            showAlert("Error", "Failed to update user: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện Delete
     * - Hỏi xác nhận người dùng
     * - Nếu đồng ý, gọi repository.delete và refresh UI
     */
    @FXML
    void handleDelete(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userRepository.delete(selectedUser.getId());
                    loadUsers();
                    clearFields();
                    showAlert("Success", "User deleted successfully.");
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearFields();
    }

    /**
     * Xóa sạch các field trong form và bỏ chọn hàng trong bảng
     */
    private void clearFields() {
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        cbRole.setValue(null);
        chkActive.setSelected(true);
        userTable.getSelectionModel().clearSelection();
    }

    /**
     * Factory method: Tạo instance subclass User theo role
     * - Mục đích để dễ mở rộng nếu từng role có logic/thuộc tính khác nhau
     */
    private User createUserInstance(String id, String username, String password, UserRole role, String email, String fullName, String phone) {
        switch (role) {
            case ADMIN:
                return new org.example.oop.Data.models.Admin(id, username, password, email, fullName, phone);
            case DOCTOR:
                return new org.example.oop.Data.models.Doctor(id, username, password, email, fullName, phone);
            case STAFF:
                return new org.example.oop.Data.models.Staff(id, username, password, email, fullName, phone);
            case PATIENT:
                return new org.example.oop.Data.models.Patient(id, username, password, email, fullName, phone);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    /**
     * Hiển thị Alert dialog (utility)
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
