package org.miniboot.app.domain.repo.Payment;

import org.miniboot.app.domain.models.Payment.PaymentItem;

import java.util.List;
import java.util.Optional;

public interface PaymentItemRepository {

    // Tìm theo payment
    List<PaymentItem> findByPaymentId(int paymentId);

    // Tìm theo id dòng
    Optional<PaymentItem> findById(int paymentItemId);

    // Lưu 1 dòng: insert (id null/0) hoặc update (id > 0)
    PaymentItem save(PaymentItem item);

    // Lưu nhiều dòng trong 1 lần (thường dùng khi tạo/sửa hóa đơn)
    // Yêu cầu: gán paymentId cho tất cả item (nếu thiếu), trả về danh sách đã có id
    List<PaymentItem> saveAll(int paymentId, List<PaymentItem> items);

    // Xoá 1 dòng, trả true nếu có bản ghi bị ảnh hưởng
    boolean deleteById(int paymentItemId);

    // Xoá tất cả dòng thuộc 1 payment, trả về số dòng xoá
    int deleteByPaymentId(int paymentId);

    // (tuỳ chọn) Thay thế toàn bộ items của 1 payment trong 1 transaction
    // Xoá hết items cũ, insert items mới và trả danh sách mới (đã có id)
    default List<PaymentItem> replaceAllForPayment(int paymentId, List<PaymentItem> newItems) {
        // Interface default method: triển khai mặc định có thể ném UnsupportedOperationException,
        // còn implement JDBC có thể override để làm transactionally
        throw new UnsupportedOperationException("Not implemented");
    }
}
