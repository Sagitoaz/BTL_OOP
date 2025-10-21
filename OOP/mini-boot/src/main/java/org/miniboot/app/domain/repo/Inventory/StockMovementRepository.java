package org.miniboot.app.domain.repo.Inventory;

import java.util.List;
import java.util.Optional;

import org.miniboot.app.domain.models.Inventory.StockMovement;

public interface StockMovementRepository {
     List<StockMovement> findAll();

     Optional<StockMovement> findById(int id);

     boolean deleteById(int id);

     StockMovement save(StockMovement stockmovement);
}
