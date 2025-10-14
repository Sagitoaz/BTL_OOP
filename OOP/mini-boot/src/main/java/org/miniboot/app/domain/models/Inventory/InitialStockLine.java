package org.miniboot.app.domain.models.Inventory;

import java.time.LocalDate;

public class InitialStockLine {
     private String batchNo = "";
     private LocalDate expiryDate; // có thể null
     private String serialNo = "";
     private int qty = 0; // không âm
     private String note = "";
     private String refId = "";
     private String red = ""; // giữ nguyên nếu bạn đang dùng field này

     public String getBatchNo() {
          return batchNo;
     }

     public void setBatchNo(String v) {
          this.batchNo = v == null ? "" : v;
     }

     public LocalDate getExpiryDate() {
          return expiryDate;
     }

     public void setExpiryDate(LocalDate d) {
          this.expiryDate = d;
     }

     public String getSerialNo() {
          return serialNo;
     }

     public void setSerialNo(String v) {
          this.serialNo = v == null ? "" : v;
     }

     public int getQty() {
          return qty;
     }

     public void setQty(int v) {
          this.qty = Math.max(0, v);
     }

     public String getNote() {
          return note;
     }

     public void setNote(String v) {
          this.note = v == null ? "" : v;
     }

     public String getRefId() {
          return refId;
     }

     public void setRefId(String v) {
          this.refId = v == null ? "" : v;
     }

     public String getRed() {
          return red;
     }

     public void setRed(String v) {
          this.red = v == null ? "" : v;
     }

     @Override
     public String toString() {
          return String.format(
                    "InitialStockLine{batch='%s', expiry=%s, serial='%s', qty=%d, refId='%s', red='%s'}",
                    batchNo, expiryDate, serialNo, qty, refId, red);
     }
}
