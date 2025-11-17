# Loading Overlay - Hướng dẫn sử dụng

## Mô tả
LoadingOverlay là một component hiển thị hiệu ứng loading đẹp mắt khi ứng dụng đang xử lý dữ liệu.

## Tính năng
✅ Hiệu ứng loading với spinner xoay
✅ Có thể tùy chỉnh text và description
✅ Overlay tối màu phía sau
✅ Design đẹp, chuyên nghiệp
✅ Dễ dàng show/hide

## Cách sử dụng

### 1. Chuẩn bị FXML
Bọc nội dung dashboard trong một StackPane:

```xml
<StackPane fx:id="rootPane" prefHeight="800.0" prefWidth="1280.0">
    <!-- Nội dung dashboard của bạn -->
    <AnchorPane>
        <!-- ... -->
    </AnchorPane>
</StackPane>
```

### 2. Trong Controller

#### Import
```java
import org.example.oop.Utils.LoadingOverlay;
import javafx.scene.layout.StackPane;
```

#### Khai báo rootPane
```java
@FXML
private StackPane rootPane;
```

#### Hiển thị loading
```java
// Cách 1: Text mặc định
LoadingOverlay.show(rootPane);

// Cách 2: Custom text
LoadingOverlay.show(rootPane, "Đang tải dữ liệu...");

// Cách 3: Custom text và description
LoadingOverlay.show(rootPane, "Đang tải Dashboard...", "Đang xác thực phiên làm việc");
```

#### Ẩn loading
```java
LoadingOverlay.hide(rootPane);
```

## Ví dụ trong Dashboard

```java
@FXML
public void initialize() {
    // Hiển thị loading
    LoadingOverlay.show(rootPane, "Đang tải Dashboard...", "Vui lòng đợi");

    // Chạy task trong background thread
    new Thread(() -> {
        try {
            // Validate session
            if (!SessionValidator.validateSession()) {
                Platform.runLater(() -> {
                    LoadingOverlay.hide(rootPane);
                    // Show error...
                });
                return;
            }
            
            // Cập nhật loading message
            Platform.runLater(() -> 
                LoadingOverlay.show(rootPane, "Đang tải dữ liệu...", "Đang tải thông tin người dùng")
            );
            
            // Load data
            loadData();
            
            // Cập nhật UI
            Platform.runLater(() -> {
                LoadingOverlay.show(rootPane, "Đang hoàn tất...", "Đang thiết lập giao diện");
                setupUI();
            });
            
            // Delay nhỏ cho mượt mà
            Thread.sleep(300);
            
            // Ẩn loading
            Platform.runLater(() -> LoadingOverlay.hide(rootPane));
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                LoadingOverlay.hide(rootPane);
                // Handle error...
            });
        }
    }).start();
}
```

## Đã áp dụng cho các Dashboard
✅ AdminDashboard - Đã có loading overlay
✅ NurseDashboard - Đã có loading overlay
⚠️ DoctorDashboard - Cần thêm
⚠️ CustomerDashboard - Cần thêm

## Style
Loading overlay có các style sau:
- Background overlay: rgba(0, 0, 0, 0.7)
- Container: White với border-radius 20px
- Progress color: #0EA5E9 (xanh dương)
- Shadow effect: Gaussian shadow

## Lưu ý
- Luôn gọi `LoadingOverlay.hide()` trong `Platform.runLater()` khi ở background thread
- Nên có delay nhỏ (300ms) trước khi hide để mượt mà hơn
- Đảm bảo rootPane được khai báo đúng trong FXML với fx:id="rootPane"

