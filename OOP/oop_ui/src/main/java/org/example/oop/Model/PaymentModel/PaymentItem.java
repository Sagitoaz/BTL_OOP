package org.example.oop.Model.PaymentModel;

import java.util.Objects;

public class PaymentItem {
    private Integer id;
    private Integer productId;
    private int paymentId;
    private String description;
    private int qty;
    private int unitPrice;
    private int totalLine;

    public PaymentItem(Integer id, Integer productId, int paymentId, String description, int qty, int unitPrice, int totalLine) {
        this.id = id;
        this.productId = productId;
        this.paymentId = paymentId;
        this.description = description;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.totalLine = totalLine;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    // Và các getter để TableView có thể đọc dữ liệu
    public String getDescription() {
        return description;
    }

    public int getQty() {
        return qty;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getTotalLine() {
        return totalLine;
    }

    /**
     * Chuyển đổi đối tượng thành chuỗi dữ liệu, phân tách bằng dấu '|'.
     */
    public String toDataString() {
        return String.join("|",
                id == null ? "" : String.valueOf(id),
                productId == null ? "" : String.valueOf(productId),
                String.valueOf(paymentId),
                description == null ? "" : description,
                String.valueOf(qty),
                String.valueOf(unitPrice),
                String.valueOf(totalLine)
        );
    }

    /**
     * Tạo đối tượng PaymentItem từ một chuỗi dữ liệu.
     */
    public static PaymentItem fromDataString(String line) {
        // Tách chuỗi bằng dấu '|' và giữ lại các chuỗi rỗng ở cuối.
        String[] parts = line.split("\\|", -1);

        // Kiểm tra để đảm bảo chuỗi đầu vào có đủ phần tử
        if (parts.length < 7) {
            throw new IllegalArgumentException("Dữ liệu chuỗi không hợp lệ để tạo đối tượng PaymentItem.");
        }

        // Phân tích từng phần của chuỗi
        Integer id = parts[0].isEmpty() ? null : Integer.parseInt(parts[0]);
        Integer productId = parts[1].isEmpty() ? null : Integer.parseInt(parts[1]);
        int paymentId = Integer.parseInt(parts[2]);
        String description = parts[3].isEmpty() ? null : parts[3];
        int qty = Integer.parseInt(parts[4]);
        int unitPrice = Integer.parseInt(parts[5]);
        int totalLine = Integer.parseInt(parts[6]);

        // Gọi hàm tạo với các giá trị đã phân tích
        return new PaymentItem(id, productId, paymentId, description, qty, unitPrice, totalLine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentItem that = (PaymentItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}