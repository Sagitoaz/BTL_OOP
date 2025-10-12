package org.example.oop.Data.models;

/**
 * ProductCategory - phân loại sản phẩm trong kho.
 *
 * Ghi chú cho người duy trì:
 * - Các giá trị đại diện cho nhóm sản phẩm như gọng kính, tròng kính, dụng cụ, vật tư, dịch vụ.
 * - Nếu mở rộng danh mục, cập nhật các chỗ lọc/hiển thị trong UI và nơi lưu/đọc dữ liệu.
 */
public enum ProductCategory {
    FRAME,
    LENS,
    CONTACT_LENS,
    MACHINE,
    CONSUMABLE,
    SERVICE
}
