package org.miniboot.app.domain.models.Inventory;

import java.time.LocalDateTime;

public class Alert {
     private int id;
     private int productId;
     private String productName;
     private String alertType; // LOW_STOCK, EXPIRED, etc.
     private String priority; // HIGH, MEDIUM, LOW
     private String message;
     private LocalDateTime createdAt;
     private LocalDateTime resolvedAt;
     private boolean active;

     // Constructors
     public Alert() {
     }

     public Alert(int productId, String alertType, String priority, String message) {
          this.productId = productId;
          this.alertType = alertType;
          this.priority = priority;
          this.message = message;
          this.createdAt = LocalDateTime.now();
          this.active = true;
     }

     // Getters & Setters
     public int getId() {
          return id;
     }

     public void setId(int id) {
          this.id = id;
     }

     public int getProductId() {
          return productId;
     }

     public void setProductId(int productId) {
          this.productId = productId;
     }

     public String getProductName() {
          return productName;
     }

     public void setProductName(String productName) {
          this.productName = productName;
     }

     public String getAlertType() {
          return alertType;
     }

     public void setAlertType(String alertType) {
          this.alertType = alertType;
     }

     public String getPriority() {
          return priority;
     }

     public void setPriority(String priority) {
          this.priority = priority;
     }

     public String getMessage() {
          return message;
     }

     public void setMessage(String message) {
          this.message = message;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }

     public void setCreatedAt(LocalDateTime createdAt) {
          this.createdAt = createdAt;
     }

     public LocalDateTime getResolvedAt() {
          return resolvedAt;
     }

     public void setResolvedAt(LocalDateTime resolvedAt) {
          this.resolvedAt = resolvedAt;
     }

     public boolean isActive() {
          return active;
     }

     public void setActive(boolean active) {
          this.active = active;
     }

     @Override
     public String toString() {
          return String.format("Alert{id=%d, product=%s, type=%s, priority=%s}",
                    id, productName, alertType, priority);
     }
}