package org.example.oop.Control;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {

    @FXML private PasswordField pfNew, pfConfirm;
    @FXML private TextField tfCurrent, tfNew, tfConfirm; // TextField để show/hide mật khẩu hiện tại
    @FXML private ProgressBar pbStrength;
    @FXML private Label lblStrength, lblError;
    @FXML private CheckBox cbShow;
    @FXML private Button btnSave, btnCancel;
    @FXML private Hyperlink linkForgot;
    @FXML
    private void initialize() {
        // hiện, ẩn mật khẩu
        tfNew.textProperty().bindBidirectional(pfNew.textProperty());
        tfNew.visibleProperty().bind(cbShow.selectedProperty());
        tfNew.managedProperty().bind(cbShow.selectedProperty());
        pfNew.visibleProperty().bind(cbShow.selectedProperty().not());
        pfNew.managedProperty().bind(cbShow.selectedProperty().not());

        tfConfirm.textProperty().bindBidirectional(pfConfirm.textProperty());
        tfConfirm.visibleProperty().bind(cbShow.selectedProperty());
        tfConfirm.managedProperty().bind(cbShow.selectedProperty());
        pfConfirm.visibleProperty().bind(cbShow.selectedProperty().not());
        pfConfirm.managedProperty().bind(cbShow.selectedProperty().not());

        pfNew.textProperty().addListener((obs, oldV, newV) -> checkPasswordStrength(newV));
    }

    // ====== SỰ KIỆN ======
    @FXML
    private void onSave() {
        lblError.setText("");

        String current = tfCurrent.getText();
        String newPass = pfNew.getText();
        String confirm = pfConfirm.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!newPass.equals(confirm)) {
            lblError.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        if (pbStrength.getProgress() < 0.6) {
            lblError.setText("Mật khẩu quá yếu, vui lòng chọn mật khẩu mạnh hơn.");
            return;
        }

        // TODO: gọi service đổi mật khẩu
        lblError.setText("Đổi mật khẩu thành công!");
        clearFields();
    }

    @FXML
    private void onCancel() {
        clearFields();
        lblError.setText("");
    }

    @FXML
    private void onForgot() {
        lblError.setText("Tính năng quên mật khẩu chưa triển khai.");
    }

    // ====== HÀM PHỤ ======
    private void checkPasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;

        double progress = score / 5.0;
        pbStrength.setProgress(0.2 * score);

        if (score <= 2)       lblStrength.setText("Yếu");
        else if (score == 3)  lblStrength.setText("Trung bình");
        else                  lblStrength.setText("Mạnh");
    }

    private void clearFields() {
        tfCurrent.clear(); // đồng bộ TextField
        pfNew.clear();
        pfConfirm.clear();
        pbStrength.setProgress(0);
        lblStrength.setText("");
    }
}
