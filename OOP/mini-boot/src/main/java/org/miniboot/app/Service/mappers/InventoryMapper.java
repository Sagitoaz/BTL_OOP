package org.miniboot.app.Service.mappers;

import org.miniboot.app.controllers.dto.inventory.InventoryCreateDTO;
import org.miniboot.app.controllers.dto.inventory.InventoryDTO;
import org.miniboot.app.controllers.dto.inventory.InventoryUpdateDTO;
import org.miniboot.app.domain.models.Inventory.Inventory;

public class InventoryMapper {

     public static InventoryDTO toDTO(Inventory i) {
          InventoryDTO dto = new InventoryDTO();
          dto.id = i.getId();
          dto.sku = i.getSku();
          dto.name = i.getName();
          dto.category = i.getCategory();
          dto.qtyOnHand = i.getQtyOnHand() != null ? i.getQtyOnHand() : i.getQuantity();
          dto.reorderPoint = i.getReorderLevel() != null ? i.getReorderLevel() : 10;
          dto.updatedAt = i.getCreatedAt();
          return dto;
     }

     public static void applyCreate(Inventory target, InventoryCreateDTO src) {
          target.setSku(nullToEmpty(src.sku));
          target.setName(nullToEmpty(src.name));
          target.setCategory(nullToEmpty(src.category));

     }

     public static void applyUpdate(Inventory target, InventoryUpdateDTO src) {
          if (src.name != null)
               target.setName(src.name);
          if (src.category != null)
               target.setCategory(src.category);
     }

     private static String nullToEmpty(String s) {
          return s == null ? "" : s;
     }
}
