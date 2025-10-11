package org.example.oop.Data.models;

/**
 * PaymentMethod - các phương thức thanh toán được hệ thống hỗ trợ.
 *
 * Ghi chú cho người duy trì:
 * - Giá trị enum dùng để lưu/so sánh trong logic nghiệp vụ và khi lưu vào file/DB.
 * - Khi bổ sung phương thức thanh toán mới (ví dụ QR, COD), cập nhật enum này và nơi xử lý
 *   liên quan (biên lai, báo cáo, UI chọn phương thức).
 * - Nếu cần hiển thị tên thân thiện trên UI, thêm trường displayName hoặc dùng resource bundle.
 */
public enum PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    E_WALLET,
    OTHER
}
