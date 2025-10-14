package org.miniboot.app.controllers.dto.inventory;

import java.time.LocalDate;

public class InitialStockLineDTO {
     public String batchNo;
     public LocalDate expiryDate; // có thể null
     public String serialNo;
     public int qty; // >0
     public String note;
}
