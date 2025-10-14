package org.example.oop.Model.Inventory;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model dòng tồn kho ban đầu dùng JavaFX Properties
 * Đảm bảo TableView quan sát được thay đổi và không "mất" khi chuyển ô.
 */
public class InitialStockLine {

     private final StringProperty batchNo = new SimpleStringProperty("");
     private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>(null);
     private final StringProperty serialNo = new SimpleStringProperty("");
     private final IntegerProperty qty = new SimpleIntegerProperty(0);
     private final StringProperty note = new SimpleStringProperty("");
     private final StringProperty refid = new SimpleStringProperty("");
     private final StringProperty red = new SimpleStringProperty("");

     public InitialStockLine() {
     }

     // --- batchNo
     public String getBatchNo() {
          return batchNo.get();
     }

     public void setBatchNo(String v) {
          batchNo.set(v == null ? "" : v);
     }

     public StringProperty batchNoProperty() {
          return batchNo;
     }

     // --- expiryDate
     public LocalDate getExpiryDate() {
          return expiryDate.get();
     }

     public void setExpiryDate(LocalDate d) {
          expiryDate.set(d);
     }

     public ObjectProperty<LocalDate> expiryDateProperty() {
          return expiryDate;
     }

     // --- serialNo
     public String getSerialNo() {
          return serialNo.get();
     }

     public void setSerialNo(String v) {
          serialNo.set(v == null ? "" : v);
     }

     public StringProperty serialNoProperty() {
          return serialNo;
     }

     // --- qty
     public int getQty() {
          return qty.get();
     }

     public void setQty(int v) {
          qty.set(Math.max(0, v));
     }

     public IntegerProperty qtyProperty() {
          return qty;
     }

     // --- note
     public String getNote() {
          return note.get();
     }

     public void setNote(String v) {
          note.set(v == null ? "" : v);
     }

     public StringProperty noteProperty() {
          return note;
     }

     // --- refid
     public String getRefid() {
          return refid.get();
     }

     public void setRefid(String v) {
          refid.set(v == null ? "" : v);
     }

     public StringProperty refidProperty() {
          return refid;
     }

     // --- red
     public String getRed() {
          return red.get();
     }

     public void setRed(String v) {
          red.set(v == null ? "" : v);
     }

     public StringProperty redProperty() {
          return red;
     }

     /**
      * Tạo StockMovement từ dòng này với productId và thông tin người thực hiện
      */
     public StockMovement toStockMovement(int productId, int movedBy, java.time.LocalDateTime movedAt, int movementId) {
          StockMovement movement = new StockMovement();
          movement.setId(movementId);
          movement.setProductId(productId);
          movement.setQty(this.getQty()); // qty dương cho OPENING
          movement.setMoveType(org.example.oop.Model.Inventory.Enum.MovementType.OPENING);
          movement.setRefTable("InitialStock");
          movement.setRefId(null);
          movement.setBatchNo(this.getBatchNo());
          movement.setExpiryDate(this.getExpiryDate());
          movement.setSerialNo(this.getSerialNo());
          movement.setMovedAt(movedAt);
          movement.setMovedBy(movedBy);
          movement.setNote(this.getNote());
          return movement;
     }

     @Override
     public String toString() {
          return String.format("InitialStockLine{batch='%s', expiry=%s, serial='%s', qty=%d, refid='%s', red='%s'}",
                    getBatchNo(), getExpiryDate(), getSerialNo(), getQty(), getRefid(), getRed());
     }
}