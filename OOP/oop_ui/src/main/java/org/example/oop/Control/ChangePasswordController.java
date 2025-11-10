package org.example.oop.Control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ChangePasswordController extends BaseController {
     @FXML
     private TextField tfAccount;
     @FXML
     private TextField tfCurrent;
     @FXML
     private PasswordField pfNew;
     @FXML
     private TextField tfNew;
     @FXML
     private Label lblStrength;
     @FXML
     private ProgressBar pbStrength;
     @FXML
     private PasswordField pfConfirm;
     @FXML
     private TextField tfConfirm;
     @FXML
     private CheckBox cbShow;
     @FXML
     private Hyperlink linkForgot;
     @FXML
     private Label lblError;
     @FXML
     private Button btnCancel;
     @FXML
     private Button btnSave;

     @FXML
     private void initialize() {
          // 1️⃣ Setup ẩn/hiện password khi tick checkbox
          setupPasswordToggle();

          // 2️⃣ Setup kiểm tra độ mạnh mật khẩu real-time
          setupPasswordStrengthChecker();

          // 3️⃣ Ẩn label error ban đầu
          if (lblError != null) {
               lblError.setVisible(false);
               lblError.setManaged(false);
          }

          // 4️⃣ Ẩn TextField (chỉ hiện PasswordField ban đầu)
          tfNew.setVisible(false);
          tfNew.setManaged(false);
          tfConfirm.setVisible(false);
          tfConfirm.setManaged(false);
          // 5️⃣ Đồng bộ nội dung giữa PasswordField và TextField
          bindpasswordFields();
     }

     private void setupPasswordToggle() {
          cbShow.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
               if (isNowSelected) {
                    tfNew.setVisible(true);
                    tfNew.setManaged(true);
                    pfNew.setVisible(false);
                    pfNew.setManaged(false);
                    tfConfirm.setVisible(true);
                    tfConfirm.setManaged(true);
                    pfConfirm.setVisible(false);
                    pfConfirm.setManaged(false);
               } else {
                    pfNew.setVisible(true);
                    pfNew.setManaged(true);
                    tfNew.setVisible(false);
                    tfNew.setManaged(false);

                    pfConfirm.setVisible(true);
                    pfConfirm.setManaged(true);
                    tfConfirm.setVisible(false);
                    tfConfirm.setManaged(false);
               }
          });
     }

     private void bindpasswordFields() {
          pfNew.textProperty().addListener((obs, oldVal, newVal) -> {
               tfNew.setText(newVal);
          });
          tfNew.textProperty().addListener((obs, oldVal, newVal) -> {
               if (!pfNew.getText().equals(newVal)) {
                    pfNew.setText(newVal);
               }
          });
          pfConfirm.textProperty().addListener((obs, oldVal, newVal) -> {
               tfConfirm.setText(newVal);
          });
          tfConfirm.textProperty().addListener((obs, oldVal, newVal) -> {
               if (!pfConfirm.getText().equals(newVal)) {
                    pfConfirm.setText(newVal);
               }
          });
     }

     private void setupPasswordStrengthChecker() {
          pfNew.textProperty().addListener((obs, oldVal, newVal) -> {
               updatePasswordStrength(newVal);
          });
          tfNew.textProperty().addListener((obs, oldVal, newVal) -> {
               updatePasswordStrength(newVal);
          });
     }

     private void updatePasswordStrength(String password) {
          if (password == null || password.isEmpty()) {
               lblStrength.setText("");
               pbStrength.setProgress(0);
               pbStrength.setStyle("");
               return;
          }
          int score = 0;
          if (password.length() >= 8)
               score++;
          if (password.length() >= 12)
               score++;
          if (password.matches(".*[A-Z].*"))
               score++;
          if (password.matches(".*[a-z].*"))
               score++;
          if (password.matches(".*\\d.*"))
               score++;
          if (password.matches(".*[@#$%^&+=!].*"))
               score++;
          double process = score / 6.0;
          pbStrength.setProgress(process);
          if (score <= 2) {
               lblStrength.setText("Yếu");
               lblStrength.setStyle("-fx-text-fill: #e74c3c;"); // Đỏ
               pbStrength.setStyle("-fx-accent: #e74c3c;");
          } else if (score <= 4) {
               lblStrength.setText("Trung bình");
               lblStrength.setStyle("-fx-text-fill: #f39c12;"); // Cam
               pbStrength.setStyle("-fx-accent: #f39c12;");
          } else {
               lblStrength.setText("Mạnh");
               lblStrength.setStyle("-fx-text-fill: #27ae60;"); // Xanh
               pbStrength.setStyle("-fx-accent: #27ae60;");
          }
     }

     private boolean validateInput() {
          String account = tfAccount.getText();
          String currentPass = tfCurrent.getText();
          String newPass = pfNew.getText();
          String confirmPass = pfConfirm.getText();
          if (account == null || account.trim().isEmpty()) {
               showErrorLabel("Vui lòng nhập tài khoản");
               tfAccount.requestFocus();
               return false;
          }
          if (currentPass == null || currentPass.trim().isEmpty()) {
               showErrorLabel("Vui lòng nhập mật khẩu hiện tại");
               tfCurrent.requestFocus();
               return false;
          }
          if (newPass == null || newPass.trim().isEmpty()) {
               showErrorLabel("Vui lòng nhập mật khẩu mới");
               pfNew.requestFocus();
               return false;
          }
          if (newPass.length() < 8) {
               showErrorLabel("Mật khẩu mới phải có ít nhất 8 ký tự");
               pfNew.requestFocus();
               return false;
          }
          if (newPass.equals(currentPass)) {
               showErrorLabel("Mật khẩu mới phải khác mật khẩu hiện tại");
               pfNew.requestFocus();
               return false;
          }
          if (!newPass.equals(confirmPass)) {
               showErrorLabel("Xác nhận mật khẩu không khớp");
               pfConfirm.requestFocus();
               return false;
          }
          hideErrorLabel();
          return true;
     }

     private void showErrorLabel(String message) {
          lblError.setText(message);
          lblError.setVisible(true);
          lblError.setManaged(true);
     }

     private void hideErrorLabel() {
          lblError.setVisible(false);
          lblError.setManaged(false);
     }

     @FXML
     private void onSave() {
          if (!validateInput()) {
               return;
          }
          String account = tfAccount.getText().trim();
          String currentPassword = tfCurrent.getText();
          String newPassword = pfNew.getText();
          btnSave.setDisable(true);
          btnCancel.setDisable(true);
          executeAsync(
                    () -> {
                         System.out.println("🔄 Changing password for: " + account);

                         boolean success = AuthServiceWrapper.changePasswordByUsername(
                                   account, currentPassword, newPassword);

                         if (success) {
                              System.out.println("✅ Password changed successfully for: " + account);
                              return true;
                         } else {
                              System.err.println("❌ Password change failed for: " + account);
                              throw new RuntimeException("Đổi mật khẩu thất bại");
                         }
                    },
                    success -> {
                         btnSave.setDisable(false);
                         btnCancel.setDisable(false);
                         showSuccess("✅ Đổi mật khẩu thành công!\n\nTài khoản của bạn đã được cập nhật.");
                         clearForm();
                         new Thread(() -> {
                              try {
                                   Thread.sleep(1500);
                                   runOnUIThread(this::closeWindow);
                              } catch (InterruptedException e) {
                                   Thread.currentThread().interrupt();
                              }
                         }).start();
                    },
                    error -> {
                         btnSave.setDisable(false);
                         btnCancel.setDisable(false);
                         String message = error.getMessage();
                         if (message != null && message.contains("không đúng")) {
                              showErrorLabel("Mật khẩu hiện tại không đúng");
                              tfCurrent.requestFocus();
                         } else if (message != null && message.contains("không tìm thấy")) {
                              showErrorLabel("Không tìm thấy tài khoản trong hệ thống");
                              tfAccount.requestFocus();
                         } else {
                              showError("Lỗi: " + (message != null ? message : "Không rõ nguyên nhân"));
                         }
                    });
     }

     private void clearForm() {
          tfAccount.clear();
          tfCurrent.clear();
          pfNew.clear();
          tfNew.clear();
          pfConfirm.clear();
          tfConfirm.clear();
          cbShow.setSelected(false);
          lblStrength.setText("");
          pbStrength.setProgress(0);
          hideErrorLabel();
     }

     private void closeWindow() {
          Stage stage = (Stage) btnSave.getScene().getWindow();
          if (stage != null) {
               stage.close();
          }
     }

     @FXML
     private void onCancel() {
          closeWindow();
     }

     @FXML
     private void onForgot() {
          showWarning("Chức năng Quên mật khẩu đang được phát triển");
     }
}
