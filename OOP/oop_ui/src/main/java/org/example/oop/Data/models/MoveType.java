package org.example.oop.Data.models;

/**
 * MoveType - loại giao dịch kho (inventory movement).
 *
 * Ghi chú cho người duy trì:
 * - Các giá trị biểu diễn các loại di chuyển hàng: nhập mua, bán, trả lại, điều chỉnh, tiêu hao, chuyển kho.
 * - Nếu mở rộng chức năng quản kho, đảm bảo cập nhật logic xử lý tương ứng (tồn kho, báo cáo, lịch sử giao dịch).
 * - Giá trị enum được lưu/so sánh trong file/DB; không đổi tên các giá trị nếu không muốn phá vỡ dữ liệu cũ.
 */
public enum MoveType {
    PURCHASE,
    SALE,
    RETURN_IN,
    RETURN_OUT,
    ADJUSTMENT,
    CONSUME,
    TRANSFER
}
