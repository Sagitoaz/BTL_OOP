package org.miniboot.app.auth;

import java.util.*;

/**
 * RolePermissions
 *
 * Lớp này định nghĩa các quyền (Permission) có thể có trong hệ thống và ánh xạ
 * giữa tên role (ví dụ "ADMIN", "DOCTOR", "STAFF", "PATIENT") với tập quyền
 * tương ứng.
 *
 * Ghi chú cho người bảo trì:
 * - Nếu muốn thêm quyền mới: bổ sung enum Permission ở dưới, sau đó ánh xạ các role
 *   trong khối static { ... } để cấp quyền cho role mong muốn.
 * - Quyền được lưu ở dạng EnumSet để tận dụng hiệu quả và an toàn (fast, type-safe).
 * - Role string là case-sensitive theo hiện tại ("ADMIN" khác "admin"). Nếu muốn không phân biệt
 *   hoa thường, chuẩn hóa khi truy vấn hoặc khi khởi tạo ROLE_PERMISSIONS.
 */
public class RolePermissions {

    /**
     * Danh sách các quyền trong hệ thống.
     *
     * Nhóm quyền được chú thích để giúp tìm nhanh (User Management, Appointment Management,...)
     * Thêm quyền mới vào enum này khi cần mở rộng chức năng.
     */
    public enum Permission {
        // User Management
        VIEW_USERS, CREATE_USER, UPDATE_USER, DELETE_USER,

        // Appointment Management
        VIEW_ALL_APPOINTMENTS, VIEW_OWN_APPOINTMENTS,
        CREATE_APPOINTMENT, UPDATE_APPOINTMENT, DELETE_APPOINTMENT,

        // Product/Inventory Management
        VIEW_PRODUCTS, CREATE_PRODUCT, UPDATE_PRODUCT, DELETE_PRODUCT, MANAGE_INVENTORY,

        // Payment Management
        VIEW_ALL_PAYMENTS, VIEW_OWN_PAYMENTS,
        CREATE_PAYMENT, UPDATE_PAYMENT, DELETE_PAYMENT,

        // Patient Records
        VIEW_ALL_RECORDS, VIEW_OWN_RECORDS,
        CREATE_RECORD, UPDATE_RECORD, DELETE_RECORD,

        // System Settings
        MANAGE_SYSTEM_SETTINGS, VIEW_REPORTS, EXPORT_DATA
    }

    // Bản đồ ánh xạ role -> tập quyền (sử dụng EnumSet để hiệu quả và an toàn)
    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // Khối static khởi tạo các quyền cho từng role khi lớp được nạp vào JVM.
        // Thay đổi ở đây sẽ ảnh hưởng tới toàn bộ kiểm tra quyền trong ứng dụng.

        // ADMIN - Full access (tất cả quyền)
        ROLE_PERMISSIONS.put("ADMIN", EnumSet.allOf(Permission.class));

        // DOCTOR - các quyền phù hợp với bác sĩ (xem lịch, tạo/cập nhật hồ sơ khám, xem báo cáo...)
        ROLE_PERMISSIONS.put("DOCTOR", EnumSet.of(
                Permission.VIEW_USERS,
                Permission.VIEW_ALL_APPOINTMENTS,
                Permission.CREATE_APPOINTMENT,
                Permission.UPDATE_APPOINTMENT,
                Permission.VIEW_ALL_RECORDS,
                Permission.CREATE_RECORD,
                Permission.UPDATE_RECORD,
                Permission.VIEW_PRODUCTS,
                Permission.VIEW_ALL_PAYMENTS,
                Permission.VIEW_REPORTS
        ));

        // STAFF - nhân viên tiếp đón/nhân viên phòng khám: nhiều quyền quản lý hơn bệnh nhân nhưng ít hơn ADMIN
        ROLE_PERMISSIONS.put("STAFF", EnumSet.of(
                Permission.VIEW_USERS,
                Permission.CREATE_USER,
                Permission.UPDATE_USER,
                Permission.VIEW_ALL_APPOINTMENTS,
                Permission.CREATE_APPOINTMENT,
                Permission.UPDATE_APPOINTMENT,
                Permission.DELETE_APPOINTMENT,
                Permission.VIEW_PRODUCTS,
                Permission.CREATE_PRODUCT,
                Permission.UPDATE_PRODUCT,
                Permission.MANAGE_INVENTORY,
                Permission.VIEW_ALL_PAYMENTS,
                Permission.CREATE_PAYMENT,
                Permission.UPDATE_PAYMENT,
                Permission.VIEW_ALL_RECORDS
        ));

        // PATIENT - quyền hạn rất hạn chế, chỉ thao tác trên dữ liệu cá nhân
        ROLE_PERMISSIONS.put("PATIENT", EnumSet.of(
                Permission.VIEW_OWN_APPOINTMENTS,
                Permission.CREATE_APPOINTMENT,
                Permission.VIEW_OWN_RECORDS,
                Permission.VIEW_OWN_PAYMENTS,
                Permission.VIEW_PRODUCTS
        ));
    }

    /**
     * Kiểm tra một role có quyền cụ thể hay không.
     * Phức tạp: O(1) truy vấn map và O(1) kiểm tra trong tập quyền.
     *
     * @param role tên role (ví dụ: "ADMIN")
     * @param permission quyền cần kiểm tra
     * @return true nếu role tồn tại và chứa quyền được yêu cầu, false ngược lại
     */
    public static boolean hasPermission(String role, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Lấy tất cả quyền của một role.
     * Trả về một tập quyền (có thể rỗng) — caller không nên thay đổi set trả về.
     *
     * @param role tên role
     * @return tập quyền tương ứng với role (hoặc tập rỗng nếu role không xác định)
     */
    public static Set<Permission> getPermissions(String role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }

    /**
     * Kiểm tra nhiều quyền đồng thời theo logic AND (tất cả phải có).
     *
     * Ví dụ: hasAllPermissions("STAFF", Permission.CREATE_USER, Permission.UPDATE_USER)
     * sẽ trả true nếu STAFF có cả hai quyền.
     *
     * @param role tên role
     * @param permissions danh sách quyền cần kiểm tra
     * @return true nếu role có đầy đủ các quyền truyền vào
     */
    public static boolean hasAllPermissions(String role, Permission... permissions) {
        Set<Permission> rolePerms = getPermissions(role);
        for (Permission perm : permissions) {
            if (!rolePerms.contains(perm)) return false;
        }
        return true;
    }

    /**
     * Kiểm tra nhiều quyền theo logic OR (ít nhất một trong các quyền phải có).
     *
     * Ví dụ: hasAnyPermission("DOCTOR", Permission.VIEW_REPORTS, Permission.MANAGE_SYSTEM_SETTINGS)
     * sẽ trả true nếu DOCTOR có ít nhất một trong hai quyền trên.
     *
     * @param role tên role
     * @param permissions danh sách quyền cần kiểm tra
     * @return true nếu role có ít nhất một quyền trong danh sách
     */
    public static boolean hasAnyPermission(String role, Permission... permissions) {
        Set<Permission> rolePerms = getPermissions(role);
        for (Permission perm : permissions) {
            if (rolePerms.contains(perm)) return true;
        }
        return false;
    }
}