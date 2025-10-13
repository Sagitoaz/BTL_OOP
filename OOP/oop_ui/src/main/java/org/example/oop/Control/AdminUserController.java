package org.example.oop.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.oop.Data.models.*;
import org.example.oop.Data.repositories.UserRepository;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AdminUserController - Quản lý người dùng theo database mới
 *
 * Cập nhật theo database version 2:
 * - User giờ là interface, có 3 implementation: Admin, Employee, Customer
 * - Employee có role: doctor/nurse (thay thế Doctor và Staff cũ)
 * - Customer thay thế Patient
 * - UserRole giờ chỉ có: ADMIN, EMPLOYEE, CUSTOMER
 */
public class AdminUserController implements Initializable {

    // === UI controls ===
    @FXML
    private TableView<UserDisplay> userTable;

    @FXML
    private TableColumn<UserDisplay, Integer> colId;

    @FXML
    private TableColumn<UserDisplay, String> colUsername;

    @FXML
    private TableColumn<UserDisplay, String> colFullName;

    @FXML
    private TableColumn<UserDisplay, String> colEmail;

    @FXML
    private TableColumn<UserDisplay, String> colRole;

    @FXML
    private TableColumn<UserDisplay, Boolean> colActive;

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
    private ComboBox<EmployeeRole> cbEmployeeRole; // Thêm cho Employee

    @FXML
    private TextField txtLicenseNo; // Thêm cho Doctor

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

    private UserRepository userRepository;
    private ObservableList<UserDisplay> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userRepository = new UserRepository();
        userList = FXCollections.observableArrayList();

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Setup comboboxes
        cbRole.setItems(FXCollections.observableArrayList(UserRole.values()));
        cbEmployeeRole.setItems(FXCollections.observableArrayList(EmployeeRole.values()));

        // Load data
        loadUsers();

        // Selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });

        // Role change listener - show/hide employee role field
        cbRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEmployee = newVal == UserRole.EMPLOYEE;
            cbEmployeeRole.setVisible(isEmployee);
            cbEmployeeRole.setManaged(isEmployee);
            txtLicenseNo.setVisible(isEmployee);
            txtLicenseNo.setManaged(isEmployee);
        });
    }

    private void loadUsers() {
        List<UserDisplay> displayList = new ArrayList<>();

        // Load từ tất cả các repository (Admin, Employee, Customer)
        // Vì UserRepository cũ chỉ load User, cần refactor để load cả 3 loại
        // Tạm thời dùng cách này:
        List<User> users = userRepository.findAll();
        for (User user : users) {
            displayList.add(new UserDisplay(user));
        }

        userList.clear();
        userList.addAll(displayList);
        userTable.setItems(userList);
    }

    private void populateFields(UserDisplay userDisplay) {
        User user = userDisplay.getOriginalUser();

        txtId.setText(String.valueOf(user.getId()));
        txtUsername.setText(user.getUsername());
        txtPassword.setText(""); // Không hiển thị password
        txtEmail.setText(user.getEmail());

        if (user instanceof Admin) {
            cbRole.setValue(UserRole.ADMIN);
            txtFullName.setText("Admin");
            txtPhone.setText("");
            cbEmployeeRole.setVisible(false);
            txtLicenseNo.setVisible(false);
        } else if (user instanceof Employee) {
            Employee emp = (Employee) user;
            cbRole.setValue(UserRole.EMPLOYEE);
            txtFullName.setText(emp.getFullName());
            txtPhone.setText(emp.getPhone());
            cbEmployeeRole.setValue(emp.getRole());
            cbEmployeeRole.setVisible(true);
            txtLicenseNo.setText(emp.getLicenseNo() != null ? emp.getLicenseNo() : "");
            txtLicenseNo.setVisible(true);
        } else if (user instanceof Customer) {
            Customer cust = (Customer) user;
            cbRole.setValue(UserRole.CUSTOMER);
            txtFullName.setText(cust.getFullName());
            txtPhone.setText(cust.getPhone());
            cbEmployeeRole.setVisible(false);
            txtLicenseNo.setVisible(false);
        }

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
            String idStr = txtId.getText().trim();
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                showAlert("Error", "ID must be a valid integer.");
                return;
            }

            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String email = txtEmail.getText().trim();
            UserRole role = cbRole.getValue();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || role == null) {
                showAlert("Error", "Please fill all required fields.");
                return;
            }

            User newUser = createUserInstance(id, username, password, email, role);
            if (newUser == null) {
                return;
            }

            userRepository.save(newUser);
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
        UserDisplay selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user to update.");
            return;
        }

        try {
            User user = selected.getOriginalUser();
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String email = txtEmail.getText().trim();

            if (username.isEmpty() || email.isEmpty()) {
                showAlert("Error", "Please fill all required fields.");
                return;
            }

            user.setUsername(username);
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setEmail(email);

            // Update specific fields based on type
            if (user instanceof Employee) {
                Employee emp = (Employee) user;
                String[] names = txtFullName.getText().trim().split(" ", 2);
                emp.setFirstname(names.length > 0 ? names[0] : "");
                emp.setLastname(names.length > 1 ? names[1] : "");
                emp.setPhone(txtPhone.getText().trim());
                if (cbEmployeeRole.getValue() != null) {
                    emp.setRole(cbEmployeeRole.getValue());
                }
                emp.setLicenseNo(txtLicenseNo.getText().trim());
            } else if (user instanceof Customer) {
                Customer cust = (Customer) user;
                String[] names = txtFullName.getText().trim().split(" ", 2);
                cust.setFirstname(names.length > 0 ? names[0] : "");
                cust.setLastname(names.length > 1 ? names[1] : "");
                cust.setPhone(txtPhone.getText().trim());
            }

            user.setActive(chkActive.isSelected());

            userRepository.update(user);
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
        UserDisplay selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
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
                    userRepository.delete(selected.getId());
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
        cbEmployeeRole.setValue(null);
        txtLicenseNo.setText("");
        chkActive.setSelected(true);
        userTable.getSelectionModel().clearSelection();
    }

    /**
     * Tạo user instance theo role - CẬP NHẬT theo database mới
     */
    private User createUserInstance(int id, String username, String password, String email, UserRole role) {
        switch (role) {
            case ADMIN:
                return new Admin(id, username, password, email, true);

            case EMPLOYEE:
                EmployeeRole empRole = cbEmployeeRole.getValue();
                if (empRole == null) {
                    showAlert("Error", "Please select employee role (Doctor/Nurse).");
                    return null;
                }
                String fullName = txtFullName.getText().trim();
                String[] names = fullName.split(" ", 2);
                String firstname = names.length > 0 ? names[0] : "";
                String lastname = names.length > 1 ? names[1] : "";
                String phone = txtPhone.getText().trim();

                Employee emp = new Employee(id, username, password, firstname, lastname, empRole, email, phone);
                if (empRole == EmployeeRole.DOCTOR) {
                    emp.setLicenseNo(txtLicenseNo.getText().trim());
                }
                return emp;

            case CUSTOMER:
                String custFullName = txtFullName.getText().trim();
                String[] custNames = custFullName.split(" ", 2);
                String custFirstname = custNames.length > 0 ? custNames[0] : "";
                String custLastname = custNames.length > 1 ? custNames[1] : "";
                String custPhone = txtPhone.getText().trim();

                return new Customer(id, username, password, custFirstname, custLastname, custPhone, email);

            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wrapper class để hiển thị User trong TableView
     */
    public static class UserDisplay {
        private final User user;

        public UserDisplay(User user) {
            this.user = user;
        }

        public int getId() {
            return user.getId();
        }

        public String getUsername() {
            return user.getUsername();
        }

        public String getFullName() {
            if (user instanceof Employee) {
                return ((Employee) user).getFullName();
            } else if (user instanceof Customer) {
                return ((Customer) user).getFullName();
            } else if (user instanceof Admin) {
                return "Admin";
            }
            return "";
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getRole() {
            if (user instanceof Admin) return "Admin";
            if (user instanceof Employee) {
                Employee emp = (Employee) user;
                return "Employee (" + emp.getRole().getValue() + ")";
            }
            if (user instanceof Customer) return "Customer";
            return "";
        }

        public boolean isActive() {
            return user.isActive();
        }

        public User getOriginalUser() {
            return user;
        }
    }
}
