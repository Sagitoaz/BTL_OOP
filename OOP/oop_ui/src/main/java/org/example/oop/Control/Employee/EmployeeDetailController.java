package org.example.oop.Control.Employee;

import org.miniboot.app.domain.models.Employee;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EmployeeDetailController {

    @FXML
    private Label fullNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label licenseLabel; // Chỉ hiển thị nếu là bác sĩ

    private Employee employee;

    public void setEmployeeDetails(Employee employee) {
        this.employee = employee;
        updateUI();
    }

    private void updateUI() {
        if (employee != null) {
            fullNameLabel.setText(employee.getFirstname() + " " + employee.getLastname());
            roleLabel.setText(employee.getRole());
            emailLabel.setText(employee.getEmail());
            phoneLabel.setText(employee.getPhone());

            // Hiển thị thông tin giấy phép chỉ khi là bác sĩ
            if ("doctor".equalsIgnoreCase(employee.getRole())) {
                licenseLabel.setText(employee.getLicenseNo());
            } else {
                licenseLabel.setText("");
            }
        }
    }
}
