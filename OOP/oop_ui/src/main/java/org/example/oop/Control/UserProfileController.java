package org.example.oop.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.oop.Data.models.User;
import org.example.oop.Data.repositories.UserRepository;

public class UserProfileController {

    // --- Các trường liên quan tới UI ---
    @FXML
    private TextField fullNameField; // ô nhập họ tên đầy đủ của người dùng

    @FXML
    private TextField emailField; // ô nhập email

    @FXML
    private TextField phoneField; // ô nhập số điện thoại

    @FXML
    private TextField addressField; // ô địa chỉ (có thể không được dùng nếu model không có trường địa chỉ)

    @FXML
    private Button saveButton; // nút lưu thay đổi

    @FXML
    private Button cancelButton; // nút hủy (phục hồi dữ liệu cũ)

    @FXML
    private Label messageLabel; // nhãn hiển thị thông báo trạng thái (lỗi / thành công)

    // --- Trạng thái trong controller ---
    private User currentUser; // đối tượng user hiện tại đang được chỉnh sửa trong giao diện

    /**
     * Thiết lập người dùng hiện tại cho controller.
     *
     * Ghi chú:
     * - Hàm này được gọi từ bên ngoài (ví dụ controller cha) để truyền thông tin user cần hiển thị.
     * - Sau khi gán, controller sẽ gọi loadUserData() để đổ dữ liệu lên giao diện.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    /**
     * Đổ dữ liệu từ currentUser lên các trường trong UI.
     *
     * Lưu ý quan trọng:
     * - Phải kiểm tra currentUser != null trước khi truy xuất các thuộc tính.
     * - Nếu một trường trên giao diện không tồn tại trong model (ví dụ address),
     *   cần xử lý tương ứng hoặc cập nhật model để chứa thông tin đó.
     */
    private void loadUserData() {
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            // Address might not be in User model, assume it's there or add
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút Lưu.
     *
     * Flow chính:
     * 1. Kiểm tra currentUser tồn tại.
     * 2. Lấy dữ liệu từ các trường UI, trim giá trị để loại khoảng trắng thừa.
     * 3. Kiểm tra hợp lệ cơ bản (ví dụ: tên và email không được rỗng).
     * 4. Cập nhật đối tượng User và gọi repository để lưu thay đổi.
     * 5. Cập nhật messageLabel để báo thành công/ lỗi cho người dùng.
     *
     * Gợi ý kiểm tra thêm:
     * - Có thể thêm kiểm tra định dạng email, độ dài số điện thoại, hoặc thông báo lỗi chi tiết hơn.
     */
    @FXML
    void onSave(ActionEvent event) {
        if (currentUser == null) return;

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Full name and email are required");
            return;
        }

        // Cập nhật giá trị vào model
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        // Lưu thay đổi qua repository (lưu vào file / DB tùy triển khai UserRepository)
        UserRepository repo = new UserRepository();
        repo.update(currentUser);

        // Thông báo cho người dùng biết đã lưu thành công
        messageLabel.setText("Profile updated successfully");
    }

    /**
     * Xử lý khi người dùng nhấn Hủy: phục hồi dữ liệu gốc từ currentUser.
     *
     * Ghi chú:
     * - Phương thức này không thao tác với repository, chỉ cập nhật lại UI từ model hiện có.
     * - Nếu muốn hủy hoàn toàn và tải lại từ nguồn dữ liệu (DB/file), cần gọi lại repository
     *   để fetch user mới nhất trước khi gọi loadUserData().
     */
    @FXML
    void onCancel(ActionEvent event) {
        loadUserData();
        messageLabel.setText("");
    }
}
