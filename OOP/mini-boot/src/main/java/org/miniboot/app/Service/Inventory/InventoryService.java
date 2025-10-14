package org.miniboot.app.Service.Inventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.Service.common.PageResult;
import org.miniboot.app.domain.models.Inventory.Inventory;
import org.miniboot.app.domain.repo.Inventory.InventoryRepository;

public class InventoryService {
    private final InventoryRepository inventoryRepo;

    public InventoryService() {
        this.inventoryRepo = new InventoryRepository();
    }

    public InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    // lấy tồn hiện tại (0 nếu không có)
    public int getOnHand(int productId) {
        return inventoryRepo.getQty(productId);
    }

    // tăng/giảm tồn, chặn âm kho nếu required
    // return newQty
    public int applyDelta(int productId, int delta, boolean allowNegative) throws IOException {
        int current = getOnHand(productId);
        long next = (long) current + delta; // chống overflow
        if (!allowNegative && next < 0) {
            throw new IllegalStateException("Số lượng tồn kho âm: " + next);
        }
        // ✅ FIX: upsertQty nhận delta, không phải giá trị tuyệt đối
        inventoryRepo.upsertQty(productId, delta);
        return (int) next;
    }

    public PageResult<Inventory> search(String q, String category, boolean lowStock, int page, int size, String sort) {
        try {
            // Load all inventories from repository
            List<Inventory> allItems = inventoryRepo.loadInventory("/TestData/inventory_9cols.txt");

            // Apply filters
            List<Inventory> filtered = allItems.stream()
                    .filter(item -> {
                        // Search filter
                        if (q != null && !q.isBlank()) {
                            String query = q.toLowerCase();
                            if (!item.getName().toLowerCase().contains(query) &&
                                    !item.getSku().toLowerCase().contains(query)) {
                                return false;
                            }
                        }

                        // Category filter
                        if (category != null && !category.isBlank()) {
                            if (!category.equalsIgnoreCase(item.getCategory())) {
                                return false;
                            }
                        }

                        // Low stock filter
                        if (lowStock) {
                            return item.getQuantity() <= 10; // Consider low stock as <= 10
                        }

                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());

            // Sort (simple implementation)
            if ("name".equals(sort)) {
                filtered.sort(java.util.Comparator.comparing(Inventory::getName));
            } else if ("-name".equals(sort)) {
                filtered.sort(java.util.Comparator.comparing(Inventory::getName).reversed());
            } else if ("quantity".equals(sort)) {
                filtered.sort(java.util.Comparator.comparing(Inventory::getQuantity));
            }

            // Pagination
            long totalElements = filtered.size();
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<Inventory> pageItems = start < filtered.size() ? filtered.subList(start, end) : new ArrayList<>();

            return new PageResult<>(pageItems, page, size, totalElements);
        } catch (Exception e) {
            return new PageResult<>(new ArrayList<>(), page, size, 0);
        }
    }

    public Optional<Inventory> findById(int id) {
        try {
            List<Inventory> allItems = inventoryRepo.loadInventory("/TestData/inventory_9cols.txt");
            return allItems.stream()
                    .filter(item -> item.getId() == id)
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Inventory create(Inventory inventory) {
        try {
            List<Inventory> allItems = inventoryRepo.loadInventory("/TestData/inventory_9cols.txt");
            inventoryRepo.AddInventory(allItems, inventory);
            inventoryRepo.saveInventory(allItems, "/TestData/inventory_9cols.txt");
            return inventory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create inventory: " + e.getMessage(), e);
        }
    }

    public Inventory update(Inventory inventory) {
        try {
            List<Inventory> allItems = inventoryRepo.loadInventory("/TestData/inventory_9cols.txt");
            boolean updated = inventoryRepo.updateInventory(allItems, inventory.getId(), inventory);
            if (!updated) {
                throw new RuntimeException("Inventory not found: " + inventory.getId());
            }
            inventoryRepo.saveInventory(allItems, "/TestData/inventory_9cols.txt");
            return inventory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update inventory: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(int id) {
        try {
            List<Inventory> allItems = inventoryRepo.loadInventory("/TestData/inventory_9cols.txt");
            boolean deleted = inventoryRepo.deleteInventory(allItems, id);
            if (deleted) {
                inventoryRepo.saveInventory(allItems, "/TestData/inventory_9cols.txt");
            }
            return deleted;
        } catch (Exception e) {
            return false;
        }
    }

    public void recalculateOnHand(int productId) {
        // For now, just update the quantity based on movements
        // In a real system, this would sum up all movements for the product
        System.out.println("Recalculating on-hand quantity for product: " + productId);
    }

    public boolean existsActiveProduct(int productId) {
        /* ... */
        return true;
    }
}
