package org.miniboot.app.domain.repo.Inventory;

import org.miniboot.app.domain.models.Inventory.StockMovement;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository {
    List<StockMovement> findAll();

    Optional<StockMovement> findById(int id);

    boolean deleteById(int id);

    StockMovement save(StockMovement stockmovement);

    List<StockMovement> saveAll(List<StockMovement> movements);
}
