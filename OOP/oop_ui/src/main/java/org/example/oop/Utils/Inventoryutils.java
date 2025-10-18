package org.example.oop.Utils;

import java.util.List;
import java.util.stream.Collectors;

import org.example.oop.Model.Inventory.Product;

public class Inventoryutils {
    /**
     * Calculate total stock value
     * Tính tổng giá trị tồn kho (quantity * price_cost)
     * 
     * @param inventories - Danh sách inventory
     * @return Tổng giá trị tồn kho
     */
    public static double calculateTotalValue(List<Product> inventories) {
        if (inventories == null) {
            return 0;
        }
        return inventories.stream()
                .filter(inv -> inv.getPriceCost() != null)
                .mapToDouble(inv -> inv.getQuantity() * inv.getPriceCost())
                .sum();
    }

    /**
     * Get low stock items
     * Lọc các sản phẩm sắp hết hàng (quantity <= reorderLevel)
     */
    public static List<Product> getLowStockItems(List<Product> inventories) {
        if (inventories == null) {
            return new java.util.ArrayList<>();
        }
        return inventories.stream()
                .filter(inv -> inv.getReorderLevel() != null && inv.getQuantity() <= inv.getReorderLevel())
                .collect(Collectors.toList());
    }

    /**
     * Get out of stock items
     * Lọc các sản phẩm hết hàng (quantity == 0)
     */
    public static List<Product> getOutOfStockItems(List<Product> inventories) {
        if (inventories == null) {
            return new java.util.ArrayList<>();
        }
        return inventories.stream()
                .filter(inv -> inv.getQuantity() == 0)
                .collect(Collectors.toList());
    }

    /**
     * Format currency (VND)
     * Format số tiền thành chuỗi hiển thị (1500000 → "1,500,000 đ")
     */
    public static String formatCurrency(double amount) {
        return String.format("%,.0f đ", amount);
    }

    /**
     * Parse SKU to get category code
     * Lấy mã category từ SKU (MED-0001 → MED)
     */
    public static String getCategoryFromSku(String sku) {
        if (sku == null || !sku.contains("-")) {
            return "UNKNOWN";
        }
        return sku.split("-")[0];
    }

    /**
     * Filter by category
     * Lọc inventory theo category
     */
    public static List<Product> filterByCategory(List<Product> inventories, String category) {
        if (inventories == null || category == null) {
            return new java.util.ArrayList<>();
        }
        return inventories.stream()
                .filter(inv -> category.equalsIgnoreCase(inv.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * Search by name
     * Tìm kiếm inventory theo tên (không phân biệt hoa thường)
     */
    public static List<Product> searchByName(List<Product> inventories, String keyword) {
        if (inventories == null || keyword == null || keyword.trim().isEmpty()) {
            return inventories;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return inventories.stream()
                .filter(inv -> inv.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
