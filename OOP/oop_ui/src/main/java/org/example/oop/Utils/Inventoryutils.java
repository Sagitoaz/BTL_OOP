package org.example.oop.Utils;

import java.util.List;
import java.util.stream.Collectors;

import org.example.oop.Model.Inventory.Inventory;
import org.example.oop.Model.Inventory.InventoryRow;

public class InventoryUtils {
    /**
     * Convert Inventory → InventoryRow
     * Chuyển đổi model Inventory thành InventoryRow để hiển thị trên TableView
     * 
     * @param inv - Inventory object cần convert
     * @return InventoryRow object hoặc null nếu inv == null
     */
    public static InventoryRow toInventoryRow(Inventory inv) {
        if (inv == null) {
            return null;
        }
        String stockStatus;
        if (inv.getQuantity() == 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (inv.getReorderLevel() != null && inv.getQuantity() <= inv.getReorderLevel()) {
            stockStatus = "LOW_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }
        String status = inv.isActive() ? "ACTIVE" : "INACTIVE";
        java.time.LocalDate lastUpdatedDate = inv.getLastUpdated() != null
                ? inv.getLastUpdated().toLocalDate()
                : null;

        // Tạo InventoryRow với constructor đầy đủ
        return new InventoryRow(
                inv.getId(), // id
                inv.getSku(), // sku
                inv.getName(), // name
                inv.getType(),
                inv.getCategory(),
                inv.getQuantity(), // qua
                inv.getUnit(), // unit
                inv.getUnitPrice(), // unit
                lastUpdatedDate, // lastUpda
                inv.getSupplier(), // supplier
                status, // stat
                inv.getReorderLevel(),
                stockStatus);
    } // stockStatus

    public static List<InventoryRow> toInventoryRows(List<Inventory> inventories) {
        if (inventories == null) {
            return new java.util.ArrayList<>();
        }
        return inventories.stream()
                .map(InventoryUtils::toInventoryRow)
                .collect(Collectors.toList());
    }

    /**
     * Calculate total stock value
     * Tính tổng giá trị tồn kho (quantity * price_cost)
     * 
     * @param inventories - Danh sách inventory
     * @return Tổng giá trị tồn kho
     */
    public static double calculateTotalValue(List<Inventory> inventories) {
        if (inventories == null) {
            return 0;
        }
        return inventories.stream()
                .filter(inv -> inv.getPrice_cost() != null)
                .mapToDouble(inv -> inv.getQuantity() * inv.getPrice_cost())
                .sum();
    }

    /**
     * Get low stock items
     * Lọc các sản phẩm sắp hết hàng (quantity <= reorderLevel)
     */
    public static List<Inventory> getLowStockItems(List<Inventory> inventories) {
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
    public static List<Inventory> getOutOfStockItems(List<Inventory> inventories) {
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
    public static List<Inventory> filterByCategory(List<Inventory> inventories, String category) {
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
    public static List<Inventory> searchByName(List<Inventory> inventories, String keyword) {
        if (inventories == null || keyword == null || keyword.trim().isEmpty()) {
            return inventories;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return inventories.stream()
                .filter(inv -> inv.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
