package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Payment - đại diện cho một giao dịch thanh toán trong hệ thống.
 *
 * Ghi chú cho người duy trì:
 * - Trường issuedAt/createdAt là LocalDateTime, dùng để lưu thời điểm phát sinh và thời điểm ghi nhận.
 * - Các trường tiền tệ lưu dưới dạng int/double tùy mục đích; cân nhắc đơn vị và làm tròn khi hiển thị/khấu trừ.
 * - paymentMethod là enum PaymentMethod; nếu mở rộng phương thức thanh toán, cập nhật enum và nơi parse.
 *
 * Định dạng lưu file:
 * - toFileFormat() trả về: id|code|customerId|cashierId|issuedAt|subtotal|discount|taxTotal|rounding|grandTotal|paymentMethod|amountPaid|note|createdAt
 * - fromFileFormat() giả sử file có đủ 14 phần và dùng LocalDateTime.parse/Integer.parseDouble/... để parse.
 *
 * Lưu ý khi sửa đổi:
 * - Xử lý ngoại lệ khi đọc file thực tế (NumberFormatException, DateTimeParseException, ArrayIndexOutOfBoundsException).
 * - Nếu cần hỗ trợ tiền tệ phức tạp, cân nhắc dùng BigDecimal thay vì double để tránh lỗi làm tròn.
 */
public class Payment {
    private String id;
    private String code;
    private String customerId;
    private String cashierId;
    private LocalDateTime issuedAt;
    private int subtotal;
    private int discount;
    private int taxTotal;
    private int rounding;
    private int grandTotal;
    private PaymentMethod paymentMethod;
    private double amountPaid;
    private String note;
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a new Payment instance.
     */
    public Payment(String id, String code, String customerId, String cashierId,
                   LocalDateTime issuedAt, int subtotal, int discount, int taxTotal,
                   int rounding, int grandTotal, PaymentMethod paymentMethod,
                   double amountPaid, String note, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.issuedAt = issuedAt;
        this.subtotal = subtotal;
        this.discount = discount;
        this.taxTotal = taxTotal;
        this.rounding = rounding;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.note = note;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(int taxTotal) {
        this.taxTotal = taxTotal;
    }

    public int getRounding() {
        return rounding;
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public int getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(int grandTotal) {
        this.grandTotal = grandTotal;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Convert to file format: id|code|customerId|cashierId|issuedAt|subtotal|discount|taxTotal|rounding|grandTotal|paymentMethod|amountPaid|note|createdAt
    public String toFileFormat() {
        return String.join("|",
                id, code, customerId, cashierId, issuedAt.toString(),
                String.valueOf(subtotal), String.valueOf(discount), String.valueOf(taxTotal),
                String.valueOf(rounding), String.valueOf(grandTotal), paymentMethod.name(),
                String.valueOf(amountPaid), note, createdAt.toString()
        );
    }

    // Parse from file format
    public static Payment fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        return new Payment(
                parts[0], parts[1], parts[2], parts[3], LocalDateTime.parse(parts[4]),
                Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                Integer.parseInt(parts[8]), Integer.parseInt(parts[9]), PaymentMethod.valueOf(parts[10]),
                Double.parseDouble(parts[11]), parts[12], LocalDateTime.parse(parts[13])
        );
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", customerId='" + customerId + '\'' +
                ", cashierId='" + cashierId + '\'' +
                ", issuedAt=" + issuedAt +
                ", subtotal=" + subtotal +
                ", discount=" + discount +
                ", taxTotal=" + taxTotal +
                ", rounding=" + rounding +
                ", grandTotal=" + grandTotal +
                ", paymentMethod=" + paymentMethod +
                ", amountPaid=" + amountPaid +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
