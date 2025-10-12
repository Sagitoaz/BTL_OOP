package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Admin - đại diện cho người dùng quản trị trong hệ thống.
 * Quản trị viên có quyền cao nhất và có thể thực hiện các thao tác hệ thống
 * như quản lý người dùng, cấu hình hệ thống, và tạo báo cáo.
 * Lớp này kế thừa User và mặc định gán role là ADMIN.
 */

/*
  Ghi chú cho người duy trì:
  - Admin mở rộng User với các hành vi dành riêng cho quản trị viên.
  - Các phương thức hiện tại là khung (placeholder) để triển khai nghiệp vụ quản trị.
  - Nếu cần thêm thuộc tính riêng cho Admin (ví dụ adminLevel), hãy khai báo và đồng bộ
    với nơi tạo/lưu dữ liệu tương ứng.
*/
public class Admin extends User {

    /**
     * Tạo đối tượng Admin mới.
     * Gọi constructor của lớp cha và gán role = ADMIN.
     *
     * Lưu ý vận hành:
     * - id nên là duy nhất trong hệ thống.
     * - username dùng để đăng nhập; đảm bảo tính duy nhất và hợp lệ.
     * - Không lưu mật khẩu ở dạng plain text ở tầng lưu trữ trong môi trường thực tế.
     */
    public Admin(int id, String username, String password,
                 String email, String fullName, String phone) {
        super(id, username, password, UserRole.ADMIN, email, fullName, phone);
    }

    /**
     * Khung chức năng quản lý người dùng dành cho Admin.
     *
     * Hướng dẫn triển khai:
     * - Thực hiện validate dữ liệu trước khi thay đổi user.
     * - Gọi repository/service để lưu thay đổi và xử lý transaction nếu cần.
     * - Ghi audit log cho các hành động quản trị để phục vụ truy vết.
     */
    public void manageUsers() {
        // TODO: Triển khai logic quản lý người dùng (tạo, cập nhật, vô hiệu hóa, thay đổi vai trò...)
        System.out.println("Admin " + getFullName() + " is managing users.");
    }

    /**
     * Khung chức năng tạo báo cáo hệ thống.
     *
     * Hướng dẫn triển khai:
     * - Tách logic xuất báo cáo ra service riêng.
     * - Hỗ trợ các định dạng xuất (CSV, PDF) và kiểm soát truy cập khi xem/lưu báo cáo.
     */
    public void generateReports() {
        // TODO: Triển khai logic tạo báo cáo
        System.out.println("Admin " + getFullName() + " is generating reports.");
    }

    /**
     * Khung chức năng cấu hình hệ thống.
     *
     * Lưu ý:
     * - Thay đổi cấu hình có thể ảnh hưởng rộng; cần kiểm soát truy cập và audit.
     * - Khi triển khai, cân nhắc dùng pattern Command/Service để hỗ trợ rollback nếu cần.
     */
    public void configureSystem() {
        // TODO: Triển khai logic cấu hình hệ thống
        System.out.println("Admin " + getFullName() + " is configuring the system.");
    }
}