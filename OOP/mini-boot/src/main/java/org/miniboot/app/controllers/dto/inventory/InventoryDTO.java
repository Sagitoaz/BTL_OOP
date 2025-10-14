package org.miniboot.app.controllers.dto.inventory;

import java.time.LocalDateTime;

public class InventoryDTO {
     public int id;
     public String sku;
     public String name;
     public String category;
     public int qtyOnHand;
     public int reorderPoint;
     public LocalDateTime updatedAt;
}
