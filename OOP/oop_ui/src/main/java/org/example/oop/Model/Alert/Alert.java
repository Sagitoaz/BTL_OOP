package org.example.oop.Model.Alert;

import java.time.LocalDateTime;

/**
 * 🚨 ALERT MODEL - NGÀY 8 FRONTEND INTEGRATION
 * Model cho hệ thống cảnh báo trong JavaFX UI
 */
public class Alert {

     private int id;
     private int productId;
     private String productName;
     private String alertType;
     private String priority;
     private String message;
     private LocalDateTime createdAt;
     private LocalDateTime resolvedAt;
     private boolean isResolved;
     private int currentStock;
     private int minStock;

     // Constructors
     public Alert() {
          this.isResolved = false;
          this.createdAt = LocalDateTime.now();
     }

     public Alert(int productId, String productName, String alertType, String priority, String message) {
          this();
          this.productId = productId;
          this.productName = productName;
          this.alertType = alertType;
          this.priority = priority;
          this.message = message;
     }

     // Getters and Setters
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

     public boolean isResolved() {
          return isResolved;
     }

     public void setResolved(boolean resolved) {
          isResolved = resolved;
          if (resolved && resolvedAt == null) {
               this.resolvedAt = LocalDateTime.now();
          }
     }

     public int getCurrentStock() {
          return currentStock;
     }

     public void setCurrentStock(int currentStock) {
          this.currentStock = currentStock;
     }

     public int getMinStock() {
          return minStock;
     }

     public void setMinStock(int minStock) {
          this.minStock = minStock;
     }

     // Utility Methods
     public String getPriorityColor() {
          switch (priority != null ? priority.toUpperCase() : "LOW") {
               case "HIGH":
                    return "#ff4444"; // Red
               case "MEDIUM":
                    return "#ff9900"; // Orange
               case "LOW":
               default:
                    return "#ffcc00"; // Yellow
          }
     }

     public String getStatusText() {
          return isResolved ? "Đã giải quyết" : "Đang chờ xử lý";
     }

     public String getFormattedCreatedAt() {
          if (createdAt == null)
               return "";
          return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
     }

     public String getFormattedResolvedAt() {
          if (resolvedAt == null)
               return "";
          return resolvedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
     }

     @Override
     public String toString() {
          return String.format("Alert{id=%d, product='%s', type='%s', priority='%s', resolved=%s}",
                    id, productName, alertType, priority, isResolved);
     }
}