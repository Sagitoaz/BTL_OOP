# 📋 TASK 2.2: KẾT NỐI CÁC SCENE VỚI NHAU

**Ngày tạo**: 27/10/2025  
**Thời gian dự kiến**: 1.5 ngày (12 giờ)  
**Độ ưu tiên**: ⭐⭐⭐⭐⭐ CRITICAL (Phải làm trước Dashboard)

---

## 🎯 MỤC TIÊU

Xây dựng hệ thống quản lý navigation tập trung để:
- ✅ Thay thế code FXMLLoader lặp đi lặp lại (~20 chỗ)
- ✅ Chuyển scene với 1 dòng code thay vì 10 dòng
- ✅ Quản lý navigation history (Back/Forward)
- ✅ Truyền dữ liệu giữa các scene dễ dàng
- ✅ Cache scenes để tăng performance
- ✅ Xử lý lỗi tập trung khi load FXML

---

## 📊 HIỆN TRẠNG VÀ VẤN ĐỀ

### ❌ Vấn đề hiện tại:
```
Code navigation hiện tại (lặp lại ~20 lần):
- LoginController.java (line 97, 110, 155)
- SignUpController.java (line 157)
- ForgotPasswordController.java (line 227)
- AppointmentBookingController.java (line 171, 430)
- AppointmentManagementController.java (line 174, 359)
- DoctorScheduleController.java (line 823)
- PaymentController.java (line 260)
- InvoiceController.java (line 386)
- CustomerHubController.java (line 313, 336, 410)
- ... và nhiều nơi khác

Mỗi nơi đều viết:
  FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/..."));
  Parent root = loader.load();
  Stage stage = (Stage) node.getScene().getWindow();
  stage.setScene(new Scene(root));
  stage.show();
```

### ⚠️ Hậu quả:
1. Code lặp lại → khó maintain
2. Hardcode paths → dễ gõ sai
3. Không có cache → load lại mỗi lần
4. Không có history → không thể back
5. Không có error handling nhất quán
6. Khó truyền dữ liệu giữa scenes

---

## 🏗️ KIẾN TRÚC GIẢI PHÁP

### Cấu trúc tổng quan:
```
┌─────────────────────────────────────────────────────────────┐
│                    ALL CONTROLLERS                           │
│  (LoginController, CustomerHubController, etc.)              │
│                                                              │
│  Thay vì viết 10 dòng code → Gọi 1 dòng:                    │
│  SceneManager.switchScene(SceneConfig.CUSTOMER_HUB)         │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
         ┌─────────────────────────┐
         │    SceneManager         │  ← Core Utility (Static)
         │  - Quản lý Stage chính  │
         │  - Cache scenes         │
         │  - Navigation history   │
         │  - Truyền data          │
         └───────────┬─────────────┘
                     │
                     ▼
         ┌─────────────────────────┐
         │    SceneConfig          │  ← Constants
         │  - Tất cả FXML paths    │
         │  - Scene titles         │
         └─────────────────────────┘
```

---

## 📂 BỐ CỤC FILES CẦN TẠO/SỬA

### ⭐ PHẦN 1: FILES MỚI CẦN TẠO (4 files)

#### 1️⃣ SceneManager.java (Core - 250 dòng)
**Đường dẫn**: `oop_ui/src/main/java/org/example/oop/Utils/SceneManager.java`

**Chức năng chính**:
```
├─ Static Variables
│  ├─ Stage primaryStage
│  ├─ Map<String, Parent> cachedScenes
│  ├─ Stack<SceneInfo> navigationHistory
│  └─ Map<String, Object> sceneData
│
├─ Initialization
│  └─ setPrimaryStage(Stage)
│
├─ Basic Navigation (40 dòng)
│  ├─ switchScene(String fxmlPath)
│  ├─ switchScene(String fxmlPath, String title)
│  └─ switchSceneWithData(String fxmlPath, Object data)
│
├─ Data Passing (30 dòng)
│  ├─ setSceneData(String key, Object value)
│  ├─ getSceneData(String key)
│  └─ clearSceneData()
│
├─ Modal Windows (40 dòng)
│  ├─ openModal(String fxmlPath)
│  └─ openWindow(String fxmlPath, boolean modal)
│
├─ Navigation History (40 dòng)
│  ├─ goBack()
│  ├─ goForward()
│  ├─ canGoBack()
│  └─ canGoForward()
│
├─ Cache Management (30 dòng)
│  ├─ clearCache()
│  ├─ preloadScenes(List<String>)
│  └─ removeFromCache(String)
│
└─ Private Helpers (40 dòng)
   ├─ loadFXML(String) - Load và cache FXML
   ├─ addToHistory(String, String) - Lưu history
   └─ handleLoadError(String, Exception) - Xử lý lỗi
```

**Logic chính cần implement**:
- **Cache**: Check Map trước khi load FXML, giới hạn max 15 scenes
- **History**: Push/Pop stack khi chuyển scene
- **Error**: Try-catch + show Alert dialog khi load failed
- **Threading**: Đảm bảo UI updates trên JavaFX Application Thread

---

#### 2️⃣ SceneConfig.java (Constants - 50 dòng)
**Đường dẫn**: `oop_ui/src/main/java/org/example/oop/Utils/SceneConfig.java`

**Nội dung**: Định nghĩa tất cả FXML paths
```
public class SceneConfig {
    // Authentication
    public static final String LOGIN = "/FXML/Login.fxml";
    public static final String SIGNUP = "/FXML/Signup.fxml";
    public static final String FORGOT_PASSWORD = "/FXML/ResetPassword.fxml";
    public static final String CHANGE_PASSWORD = "/FXML/ChangePassword.fxml";
    
    // Main
    public static final String DASHBOARD = "/FXML/Dashboard.fxml";
    public static final String HOME = "/FXML/HomeView.fxml";
    
    // Schedule Module (3 paths)
    // Patient Module (3 paths)
    // Payment Module (4 paths)
    // Inventory Module (2 paths)
    // Employee Module (2 paths)
    
    // Helper method
    public static String getTitle(String scenePath) { ... }
    
    // Preload list
    public static final List<String> PRELOAD_SCENES = Arrays.asList(...);
}
```

**Danh sách paths cần định nghĩa**: ~15-20 constants

---

#### 3️⃣ SceneInfo.java (Model - 30 dòng)
**Đường dẫn**: `oop_ui/src/main/java/org/example/oop/Model/SceneInfo.java`

**Chức năng**: Lưu thông tin về mỗi scene trong history

**Fields cần có**:
```
- String fxmlPath
- String title
- LocalDateTime timestamp
- Map<String, Object> params

+ Constructor, Getters, Setters
```

---

#### 4️⃣ NavigationController.java (Optional - 100 dòng)
**Đường dẫn**: `oop_ui/src/main/java/org/example/oop/Control/NavigationController.java`

**Chức năng**: Helper cho Dashboard navigation (load scene vào content area thay vì replace toàn màn hình)

**Methods chính**:
```
- NavigationController(Pane contentArea, VBox menuContainer)
- handleMenuClick(String sceneName, Button menuButton)
- highlightActiveMenu(Button button)
- loadSceneIntoContentArea(String fxmlPath)
- checkPermission(String requiredRole)
- setupBackButton(Button backButton)
```

**Note**: Optional - chỉ cần khi làm Dashboard với sidebar

---

### ⚠️ PHẦN 2: FILES CẦN SỬA (~20 files)

#### Group 1: Main & Authentication (4 files)

**1. Main.java**
```
TRƯỚC:
  FXMLLoader fxmlLoader = new FXMLLoader(...);
  Scene scene = new Scene(fxmlLoader.load());
  stage.setScene(scene);

SAU:
  SceneManager.setPrimaryStage(stage);
  SceneManager.switchScene(SceneConfig.LOGIN);  // hoặc CUSTOMER_HUB
```

**2. LoginController.java** (3 chỗ: line 97, 110, 155)
```
Line 97 (Forgot Password): 
  SceneManager.switchScene(SceneConfig.FORGOT_PASSWORD);

Line 110 (Sign Up):
  SceneManager.switchScene(SceneConfig.SIGNUP);

Line 155 (After login success):
  SceneManager.switchScene(SceneConfig.DASHBOARD);
```

**3. SignUpController.java** (line 157)
```
SAU: SceneManager.switchScene(SceneConfig.LOGIN);
```

**4. ForgotPasswordController.java** (line 227)
```
SAU: SceneManager.switchScene(SceneConfig.LOGIN);
```

---

#### Group 2: Schedule Module (3 files)

**5. AppointmentBookingController.java** (line 171, 430)
```
Line 171: SceneManager.switchScene(SceneConfig.APPOINTMENT_MANAGEMENT);
Line 430: SceneManager.goBack(); // hoặc switch về management
```

**6. AppointmentManagementController.java** (line 174, 359)
```
Line 174: SceneManager.openModal(SceneConfig.APPOINTMENT_BOOKING);
Line 359: SceneManager.openModal(SceneConfig.DOCTOR_SCHEDULE);
```

**7. DoctorScheduleController.java** (line 823)
```
SAU: SceneManager.openWindow(SceneConfig.APPOINTMENT_MANAGEMENT, false);
```

---

#### Group 3: Payment Module (2 files)

**8. PaymentController.java** (line 260)
```
SAU: SceneManager.openModal(SceneConfig.RECEIPT);
```

**9. InvoiceController.java** (line 386)
```
SAU: SceneManager.switchScene(SceneConfig.PAYMENT);
```

---

#### Group 4: Patient Module (1 file)

**10. CustomerHubController.java** (line 313, 336, 410)
```
Line 313: SceneManager.openModal(SceneConfig.ADD_CUSTOMER); // mở form Add
Line 336: SceneManager.openModal(SceneConfig.ADD_CUSTOMER); // mở form Edit
        + setSceneData("customerId", selectedCustomer.getId());
Line 410: SceneManager.switchScene(SceneConfig.PRESCRIPTION_EDITOR);
        + setSceneData("customerId", selectedCustomer.getId());
```

---

#### Group 5: Các modules khác (nếu có FXMLLoader)

**Cách tìm**: Grep search `FXMLLoader` trong project → Replace tất cả

---

## 🔄 FLOW HOẠT ĐỘNG

### Flow 1: Khởi động App
```
Main.java
  └─> setPrimaryStage(stage)
  └─> switchScene(LOGIN)
      └─> loadFXML("/FXML/Login.fxml")
      └─> Cache vào Map
      └─> Set scene vào stage
      └─> Add vào history
```

### Flow 2: Login thành công
```
LoginController.handleLogin()
  └─> Validate credentials
  └─> if success:
      └─> SessionStorage.setCurrentUser(...)
      └─> SceneManager.switchScene(DASHBOARD)
          └─> Check cache có DASHBOARD chưa
          └─> Load (hoặc lấy từ cache)
          └─> Switch scene
```

### Flow 3: Truyền dữ liệu giữa scenes
```
CustomerHubController (chọn customer ID=123)
  └─> SceneManager.setSceneData("customerId", 123)
  └─> SceneManager.switchScene(PRESCRIPTION_EDITOR)

PrescriptionEditorController.initialize()
  └─> Integer id = (Integer) SceneManager.getSceneData("customerId")
  └─> Load prescriptions của customer 123
```

### Flow 4: Mở Modal Dialog
```
AppointmentManagementController
  └─> User click "Thêm lịch hẹn"
  └─> SceneManager.openModal(APPOINTMENT_BOOKING)
      └─> Tạo Stage mới
      └─> initModality(APPLICATION_MODAL)
      └─> showAndWait() // Block cho đến khi đóng
  └─> Modal đóng → tự động về parent scene
```

### Flow 5: Back Navigation
```
User click "Quay lại"
  └─> SceneManager.goBack()
      └─> Pop scene từ navigationHistory
      └─> Push scene hiện tại vào forwardHistory
      └─> Load scene trước đó
      └─> Restore scene data
```

---

## 📝 CHECKLIST IMPLEMENTATION

### Phase 1: Core Setup (3 giờ)
- [ ] Tạo `SceneManager.java` (250 dòng)
  - [ ] Basic structure + static variables
  - [ ] setPrimaryStage() method
  - [ ] switchScene() methods (3 overloads)
  - [ ] loadFXML() helper with caching
  - [ ] handleLoadError() helper

- [ ] Tạo `SceneConfig.java` (50 dòng)
  - [ ] Define all FXML path constants (~20 paths)
  - [ ] getTitle() method
  - [ ] PRELOAD_SCENES list

- [ ] Tạo `SceneInfo.java` (30 dòng)
  - [ ] Fields: fxmlPath, title, timestamp, params
  - [ ] Constructor, getters, setters

- [ ] Test: Main.java → Login scene

---

### Phase 2: Basic Navigation (2 giờ)
- [ ] Update `Main.java`
  - [ ] Thêm SceneManager.setPrimaryStage(stage)
  - [ ] Đổi thành switchScene(LOGIN) hoặc CUSTOMER_HUB

- [ ] Update Authentication Controllers (4 files)
  - [ ] LoginController.java (3 chỗ)
  - [ ] SignUpController.java (1 chỗ)
  - [ ] ForgotPasswordController.java (1 chỗ)

- [ ] Test: Login flow → Signup → Forgot Password → Back to Login

---

### Phase 3: Replace Module Controllers (3 giờ)
- [ ] Update Schedule Module (3 files)
  - [ ] AppointmentBookingController.java
  - [ ] AppointmentManagementController.java
  - [ ] DoctorScheduleController.java

- [ ] Update Payment Module (2 files)
  - [ ] PaymentController.java
  - [ ] InvoiceController.java

- [ ] Update Patient Module (1 file)
  - [ ] CustomerHubController.java

- [ ] Update Inventory Module (nếu có navigation)

- [ ] Test: Navigate giữa các modules

---

### Phase 4: Advanced Features (2 giờ)
- [ ] Implement Data Passing
  - [ ] setSceneData(), getSceneData(), clearSceneData()

- [ ] Implement Modal Windows
  - [ ] openModal() method
  - [ ] openWindow() method

- [ ] Implement Navigation History
  - [ ] goBack() method
  - [ ] goForward() method
  - [ ] canGoBack(), canGoForward()

- [ ] Implement Cache Management
  - [ ] clearCache()
  - [ ] preloadScenes()

- [ ] Test: Truyền data, open modals, back navigation

---

### Phase 5: Polish & Testing (2 giờ)
- [ ] Add comprehensive error handling
  - [ ] Try-catch trong tất cả methods
  - [ ] Show Alert dialog khi load failed
  - [ ] Log errors

- [ ] Test error scenarios
  - [ ] File FXML không tồn tại
  - [ ] Controller class not found
  - [ ] Memory leak test (cache nhiều scenes)

- [ ] Performance optimization
  - [ ] Kiểm tra cache size
  - [ ] Preload frequently used scenes

- [ ] Code cleanup
  - [ ] Xóa tất cả FXMLLoader code cũ
  - [ ] Remove unused imports
  - [ ] Format code

---

## 📊 TIMELINE CHI TIẾT

### Sáng Ngày 1 (4 giờ): Core Setup
- **08:00 - 09:30**: Tạo SceneManager.java skeleton + basic methods
- **09:30 - 10:30**: Implement loadFXML() với cache logic
- **10:30 - 11:00**: Tạo SceneConfig.java + SceneInfo.java
- **11:00 - 12:00**: Update Main.java và test Login scene

### Chiều Ngày 1 (4 giờ): Basic Navigation
- **13:00 - 14:00**: Update LoginController (3 chỗ)
- **14:00 - 15:00**: Update SignUp + ForgotPassword controllers
- **15:00 - 16:00**: Test authentication flow
- **16:00 - 17:00**: Update Schedule module controllers (3 files)

### Sáng Ngày 2 (4 giờ): Replace Controllers + Advanced Features
- **08:00 - 09:00**: Update Payment + Patient module controllers
- **09:00 - 10:00**: Implement Data Passing methods
- **10:00 - 11:00**: Implement Modal Windows
- **11:00 - 12:00**: Implement Navigation History

### Chiều Ngày 2 (2 giờ): Testing & Polish
- **13:00 - 14:00**: Comprehensive testing
- **14:00 - 15:00**: Error handling + code cleanup

---

## 💡 USAGE PATTERNS (Cách dùng)

### Pattern 1: Simple Switch (90% cases)
```java
// Chỉ cần 1 dòng
SceneManager.switchScene(SceneConfig.CUSTOMER_HUB);
```

### Pattern 2: Switch with Data
```java
// Truyền ID
SceneManager.setSceneData("customerId", 123);
SceneManager.switchScene(SceneConfig.PRESCRIPTION_EDITOR);

// Trong PrescriptionEditorController.initialize():
Integer id = (Integer) SceneManager.getSceneData("customerId");
```

### Pattern 3: Modal Dialog
```java
// Mở form Add/Edit
SceneManager.openModal(SceneConfig.EMPLOYEE_FORM);

// Check refresh sau khi đóng modal
if ((Boolean) SceneManager.getSceneData("refreshNeeded")) {
    refreshTable();
    SceneManager.removeSceneData("refreshNeeded");
}
```

### Pattern 4: Back Navigation
```java
@FXML
void handleBack(ActionEvent event) {
    if (SceneManager.canGoBack()) {
        SceneManager.goBack();
    } else {
        SceneManager.switchScene(SceneConfig.DASHBOARD);
    }
}
```

### Pattern 5: Preload Scenes
```java
// Trong Main.start() sau khi show stage
SceneManager.preloadScenes(SceneConfig.PRELOAD_SCENES);
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. Thread Safety
```
⚠️ LUÔN update UI trên JavaFX Application Thread

✅ ĐÚNG:
Platform.runLater(() -> {
    SceneManager.switchScene(SceneConfig.DASHBOARD);
});

❌ SAI:
new Thread(() -> {
    SceneManager.switchScene(...); // CRASH!
}).start();
```

### 2. Memory Management
```
- Giới hạn cache: MAX_CACHE_SIZE = 15
- Clear cache khi logout:
  SceneManager.clearCache();
  SceneManager.clearSceneData();
```

### 3. Error Handling
```
- Luôn wrap navigation trong try-catch
- Show user-friendly error messages
- Log errors cho debugging
```

### 4. Data Cleanup
```
- Xóa sensitive data sau khi dùng:
  Integer id = (Integer) SceneManager.getSceneData("customerId");
  // ... use id ...
  SceneManager.removeSceneData("customerId");
```

### 5. Avoid Circular Dependencies
```
❌ Scene A → Scene B → Scene A → Scene B (infinite loop)
✅ Scene A → Scene B → goBack() to A
```

---

## 🎯 KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành Task 2.2:

### ✅ Code Quality
- Code ngắn gọn: **1 dòng** thay vì **10 dòng**
- Dễ maintain: Sửa 1 chỗ thay vì 20+ chỗ
- Nhất quán: Cùng cách navigation cho toàn app
- Professional: Theo best practices

### ✅ User Experience
- Performance tốt hơn: Scene caching
- Navigation mượt mà: Back/Forward
- Responsive: Loading không block UI

### ✅ Developer Experience
- Dễ debug: Centralized logging
- Dễ test: Mock SceneManager
- Dễ extend: Thêm tính năng mới dễ dàng

### ✅ Files Created/Updated
**Created**: 3-4 files (SceneManager, SceneConfig, SceneInfo, NavigationController*)
**Updated**: ~20 controller files với code đơn giản hơn

---

## 📚 TÀI LIỆU THAM KHẢO

### JavaFX Core
- Scene: https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/Scene.html
- Stage: https://openjfx.io/javadoc/17/javafx.graphics/javafx/stage/Stage.html
- FXMLLoader: https://openjfx.io/javadoc/17/javafx.fxml/javafx/fxml/FXMLLoader.html

### Design Patterns
- Singleton Pattern (SceneManager)
- Factory Pattern (Scene creation)
- Command Pattern (Navigation)

### Best Practices
- JavaFX Thread Rules: https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm

---

## 🤝 TROUBLESHOOTING

### Lỗi 1: NullPointerException - primaryStage is null
```
Nguyên nhân: Chưa gọi setPrimaryStage()
Giải pháp: Gọi trong Main.start() TRƯỚC KHI dùng switchScene()
```

### Lỗi 2: Not on FX application thread
```
Nguyên nhân: Gọi từ background thread
Giải pháp: Wrap trong Platform.runLater()
```

### Lỗi 3: IOException - Location not set
```
Nguyên nhân: Đường dẫn FXML sai
Giải pháp: Check path trong SceneConfig
```

### Lỗi 4: Memory Leak
```
Nguyên nhân: Cache quá nhiều
Giải pháp: Giới hạn MAX_CACHE_SIZE hoặc clearCache()
```

---

## 🎉 KẾT LUẬN

Task 2.2 là **nền tảng quan trọng** cho toàn bộ ứng dụng:
- ✅ Navigation **nhất quán** và **dễ maintain**
- ✅ Code **clean** và **professional**
- ✅ Dễ **mở rộng** thêm tính năng
- ✅ Sẵn sàng cho **Dashboard integration** (Task 2.1)

**Thời gian**: 1.5 ngày (12 giờ)  
**Độ khó**: ⭐⭐⭐☆☆ (Trung bình)  
**Ưu tiên**: ⭐⭐⭐⭐⭐ (CRITICAL)

---

**📅 Last updated**: 27/10/2025  
**👤 Người tạo**: GitHub Copilot  
**📁 Dự án**: Eye Clinic Management System

