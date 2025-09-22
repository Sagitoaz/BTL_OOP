package org.example.oop.Control;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ChangePasswordController {

    @FXML private PasswordField pfNew, pfConfirm;
    @FXML private TextField tfCurrent, tfAccount, tfNew, tfConfirm; // show/hide
    @FXML private ProgressBar pbStrength;
    @FXML private Label lblStrength, lblError, test;
    @FXML private CheckBox cbShow;
    @FXML private Button btnSave, btnCancel;
    @FXML private Hyperlink linkForgot;

    // ===== CHỈ DÙNG 1 FILE DUY NHẤT =====
    private static final Path USER_FILE = Paths.get("data", "user.txt");

    // Chỉ số cột (đúng với định dạng: id|username|password|role|fullName|province|email|phone|dob|gender)
    private static final int COL_USERNAME = 1;
    private static final int COL_PASSWORD = 2;

    // Trạng thái đổi mật khẩu (để báo lỗi cụ thể)
    private enum ChangePasswordStatus { OK, USER_NOT_FOUND, WRONG_CURRENT, IO_ERROR }

    @FXML
    private void initialize() {
        // Ràng buộc show/hide mật khẩu
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
        String username = safe(tfAccount.getText());
        String current  = safe(tfCurrent.getText());
        String newPass  = safe(pfNew.getText());
        String confirm  = safe(pfConfirm.getText());

        if (username.isEmpty() || current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
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
        ChangePasswordStatus st = changePassword(USER_FILE, username, current, newPass);
        switch (st) {
            case OK -> {
                lblError.setText("Đổi mật khẩu thành công!");
                clearFields();
            }
            case USER_NOT_FOUND -> lblError.setText("Không tìm thấy tài khoản.");
            case WRONG_CURRENT  -> lblError.setText("Mật khẩu hiện tại không đúng.");
            case IO_ERROR       -> lblError.setText("Lỗi đọc/ghi file. Kiểm tra quyền ghi & đường dẫn.");
        }
    }
    @FXML
    private void onCancel() {
        clearFields();
        lblError.setText("");
    }
    @FXML
    private void onForgot(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Không thể mở màn hình Quên mật khẩu.");
        }
    }
    // ====== LOGIC ĐỌC/GHI TRÊN data/user.txt ======
    /** Đổi mật khẩu trên file nhiều cột, giữ nguyên các cột khác */
    private ChangePasswordStatus changePassword(Path path, String username, String current, String newPass) {
        try {
            LinkedHashMap<String, List<String>> map = readAllRecords(path);
            List<String> rec = map.get(username);
            if (rec == null) return ChangePasswordStatus.USER_NOT_FOUND;

            String saved = getCol(rec, COL_PASSWORD);
            if (!Objects.equals(saved, current)) return ChangePasswordStatus.WRONG_CURRENT;

            setCol(rec, COL_PASSWORD, newPass);
            writeAllRecords(path, map.values());
            return ChangePasswordStatus.OK;
        } catch (IOException e) {
            e.printStackTrace();
            return ChangePasswordStatus.IO_ERROR;
        }
    }

    /** Đọc toàn bộ file thành Map<username, list cột>, giữ nguyên thứ tự dòng */
    private LinkedHashMap<String, List<String>> readAllRecords(Path path) throws IOException {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        if (!Files.exists(path)) return map;

        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("\\|", -1); // giữ cột rỗng cuối
            if (parts.length < 3) continue;

            List<String> cols = new ArrayList<>(parts.length);
            for (String p : parts) cols.add(p == null ? "" : p.trim());

            String user = getCol(cols, COL_USERNAME);
            if (user == null || user.isEmpty()) continue;

            map.put(user, cols);
        }
        return map;
    }
    /** Ghi an toàn: file tạm -> move (giữ định dạng nhiều cột) */
    private void writeAllRecords(Path path, Collection<List<String>> records) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");

        List<String> out = new ArrayList<>(records.size() + 1);
        out.add("# id|username|password|role|fullName|province|email|phone|dob|gender");
        for (List<String> r : records) {
            out.add(String.join("|", r));
        }
        Files.write(tmp, out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    // ====== HÀM TIỆN ÍCH / DEBUG ======
    private static String getCol(List<String> cols, int idx) {
        return (idx >= 0 && idx < cols.size()) ? cols.get(idx) : null;
    }
    private static void setCol(List<String> cols, int idx, String value) {
        while (cols.size() <= idx) cols.add("");
        cols.set(idx, value == null ? "" : value);
    }
    // ====== HÀM PHỤ UI ======
    private void checkPasswordStrength(String password) {
        int score = 0;
        if (password != null && password.length() >= 8) score++;
        if (password != null && password.matches(".*[A-Z].*")) score++;
        if (password != null && password.matches(".*[a-z].*")) score++;
        if (password != null && password.matches(".*\\d.*")) score++;
        if (password != null && password.matches(".*[!@#$%^&*()].*")) score++;
        pbStrength.setProgress(0.2 * score);
        if (score <= 2)       lblStrength.setText("Yếu");
        else if (score == 3)  lblStrength.setText("Trung bình");
        else                  lblStrength.setText("Mạnh");
    }
    private void clearFields() {
        tfCurrent.clear();
        pfNew.clear();
        pfConfirm.clear();
        pbStrength.setProgress(0);
        lblStrength.setText("");
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
