package org.example.oop.Service;

import java.io.IOException;

import org.example.oop.Repository.InventoryRepository;

public class InventoryService {
    private final InventoryRepository inventoryRepo = new InventoryRepository();

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
        inventoryRepo.upsertQty(productId, (int) next);
        return (int) next;
    }

    public boolean existsActiveProduct(int productId) {
        /* ... */
        return true;
    }
}
