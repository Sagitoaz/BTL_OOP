# Tài liệu về Authentic JWT và Data Layer & User Management

## Tổng quan
Dự án này triển khai hệ thống quản lý phòng khám mắt với các thành phần chính bao gồm xác thực người dùng (Authentic JWT) và lớp dữ liệu quản lý người dùng (Data Layer & User Management). Tài liệu này giải thích chi tiết những gì đã được triển khai, điểm đặc biệt, và luồng hoạt động.

## 1. Authentic JWT (Xác thực JWT)

### Những gì đã làm
- **AuthServiceWrapper**: Lớp bọc để gọi các chức năng xác thực từ module `mini-boot` thông qua reflection, tránh phụ thuộc trực tiếp.
- **Các chức năng chính**:
  - `login(String username, String password)`: Đăng nhập và trả về session ID nếu thành công.
  - `logout(String sessionId)`: Đăng xuất.
  - `getCurrentSession(String sessionId)`: Lấy thông tin session hiện tại.
  - `hashPasswordWithSalt(String password)`: Hash mật khẩu với salt.
  - `verifyPassword(String password, String hashedPassword)`: Xác minh mật khẩu.
  - `isPasswordStrong(String password)`: Kiểm tra độ mạnh mật khẩu (có fallback nếu reflection fail).

### Điểm đặc biệt
- **Sử dụng reflection**: Cho phép gọi methods từ module khác mà không cần import trực tiếp, tăng tính linh hoạt và giảm coupling.
- **Fallback mechanisms**: Nếu reflection thất bại (ví dụ module mini-boot không khả dụng), hệ thống vẫn hoạt động với các giá trị an toàn hoặc fallback (như fallback password strength check).
- **Bảo mật**: Hash mật khẩu với salt, không lưu plain text. Xác thực dựa trên session ID thay vì token JWT trực tiếp (do sử dụng AuthService từ mini-boot).

### Luồng hoạt động
1. **Đăng ký (Signup)**:
   - Người dùng nhập thông tin (username, password, email, etc.).
   - Kiểm tra tính hợp lệ (password strong, email format, username unique).
   - Hash password với salt qua `AuthServiceWrapper.hashPasswordWithSalt()`.
   - Tạo đối tượng User (Patient) và lưu qua Data Layer.

2. **Đăng nhập (Login)**:
   - Người dùng nhập username và password.
   - Gọi `AuthServiceWrapper.login()` để xác thực.
   - Nếu thành công, trả về session ID; ngược lại, báo lỗi.
   - Session ID được sử dụng để duy trì trạng thái đăng nhập.

3. **Đăng xuất (Logout)**:
   - Gọi `AuthServiceWrapper.logout(sessionId)` để hủy session.

4. **Xác minh session**:
   - Sử dụng `AuthServiceWrapper.getCurrentSession(sessionId)` để kiểm tra session hợp lệ.

## 2. Data Layer & User Management (Lớp dữ liệu và quản lý người dùng)

### Những gì đã làm
- **Models**:
  - `User`: Abstract class với các thuộc tính chung (id, username, password, role, email, fullName, phone, createdAt, active).
  - Subclasses: `Admin`, `Doctor`, `Staff`, `Patient` – kế thừa User và set role cụ thể.
  - `UserRole`: Enum với các vai trò (ADMIN, DOCTOR, STAFF, PATIENT).

- **Repositories**:
  - `UserRepository`: Triển khai `DataRepository<User>`, cung cấp CRUD operations (save, findById, findAll, update, delete, exists, count).
  - Sử dụng `FileManager` để lưu dữ liệu vào file `users.txt`.

- **Storage**:
  - `FileManager`: Quản lý đọc/ghi file an toàn với thread (ReentrantReadWriteLock).
  - `JsonUtils`: Serialize/deserialize objects thành JSON (sử dụng Gson).
  - `DataRepository`: Interface định nghĩa hợp đồng cho repositories.

### Điểm đặc biệt
- **Thread-safe**: `FileManager` sử dụng read-write lock để cho phép nhiều luồng đọc đồng thời nhưng ghi độc quyền.
- **File-based storage**: Dữ liệu lưu dưới dạng text file với format phân cách bởi '|', dễ đọc và chỉnh sửa thủ công.
- **Inheritance hierarchy**: Models sử dụng kế thừa để tái sử dụng code, với abstract User và subclasses cụ thể.
- **CRUD interface**: `DataRepository` cung cấp hợp đồng chung, dễ mở rộng cho các entity khác (như Appointment, Payment).
- **Serialization support**: `JsonUtils` cho phép chuyển đổi objects thành JSON, hữu ích cho API hoặc debug.

### Luồng hoạt động
1. **Tạo người dùng (Create)**:
   - Tạo instance của subclass (e.g., `Patient`).
   - Gọi `UserRepository.save(user)`: Nếu ID tồn tại, update; ngược lại, append vào file.

2. **Đọc người dùng (Read)**:
   - `findById(String id)`: Đọc file, parse từng dòng, tìm user với ID khớp.
   - `findAll()`: Đọc toàn bộ file, parse tất cả users.
   - `findByUsername(String username)`: Tìm user theo username.

3. **Cập nhật người dùng (Update)**:
   - Đọc toàn bộ file vào memory.
   - Tìm và thay thế dòng tương ứng với user mới.
   - Ghi lại toàn bộ file.

4. **Xóa người dùng (Delete)**:
   - Đọc toàn bộ file.
   - Lọc bỏ dòng có ID khớp.
   - Ghi lại file.

5. **Persistence**:
   - Dữ liệu lưu trong `oop_ui/src/main/resources/Data/users.txt` với format: `id|username|password|role|email|fullName|phone|createdAt|active`.
   - `toFileFormat()`: Chuyển object thành string.
   - `fromFileFormat()`: Parse string thành object (hiện không parse createdAt và active từ file, set mặc định).

## Kết luận
Hệ thống này cung cấp nền tảng vững chắc cho quản lý người dùng và xác thực trong phòng khám mắt. Điểm mạnh là tính linh hoạt (reflection, file-based), bảo mật (hash password, session-based), và dễ mở rộng (interface, inheritance). Để cải thiện, có thể thêm parsing đầy đủ cho createdAt/active, hoặc chuyển sang database cho performance tốt hơn.
