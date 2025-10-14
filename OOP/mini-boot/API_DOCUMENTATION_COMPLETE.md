# 📚 COMPLETE REST API DOCUMENTATION - NGÀY 7

# Inventory Management System - Backend API Reference

## 🎯 Overview

Comprehensive REST API for Inventory Management System with full CRUD operations, stock movement tracking, and alert management.

**Base URL:** `http://localhost:8080`
**Content-Type:** `application/json`

---

## 📦 1. INVENTORY MANAGEMENT API

### Base Endpoint: `/api/inventory`

#### 1.1 List All Inventory Items

```http
GET /api/inventory
GET /api/inventory?page=0&size=10&category=electronics&minStock=5
```

**Query Parameters:**

- `page` (optional): Page number (default: 0)
- `size` (optional): Items per page (default: 10)
- `category` (optional): Filter by category
- `minStock` (optional): Filter items with stock >= value

**Response:**

```json
{
  "items": [
    {
      "id": 1,
      "sku": "SKU001",
      "name": "Product Name",
      "category": "electronics",
      "currentStock": 150,
      "minStock": 10,
      "maxStock": 500,
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ],
  "totalItems": 45,
  "totalPages": 5,
  "currentPage": 0,
  "pageSize": 10
}
```

#### 1.2 Get Inventory Item by ID

```http
GET /api/inventory/{id}
```

**Response:**

```json
{
  "id": 1,
  "sku": "SKU001",
  "name": "Product Name",
  "category": "electronics",
  "description": "Product description",
  "currentStock": 150,
  "minStock": 10,
  "maxStock": 500,
  "createdAt": "2024-01-01T10:00:00Z"
}
```

#### 1.3 Create New Inventory Item

```http
POST /api/inventory
```

**Request Body:**

```json
{
  "sku": "SKU002",
  "name": "New Product",
  "category": "electronics",
  "description": "Product description",
  "minStock": 5,
  "maxStock": 100
}
```

**Response:** `201 Created` with created item

#### 1.4 Update Inventory Item

```http
PUT /api/inventory/{id}
```

**Request Body:**

```json
{
  "name": "Updated Product Name",
  "category": "updated-category",
  "description": "Updated description",
  "minStock": 10,
  "maxStock": 200
}
```

**Response:** `200 OK` with updated item

#### 1.5 Delete Inventory Item

```http
DELETE /api/inventory/{id}
```

**Response:** `204 No Content`

#### 1.6 Record Initial Stock

```http
POST /api/inventory/{id}/initial-stock
```

**Request Body:**

```json
{
  "qty": 100,
  "batchNo": "BATCH001",
  "note": "Initial stock entry"
}
```

**Response:** `200 OK` with success message

#### 1.7 Get Stock Movements for Item

```http
GET /api/inventory/{id}/movements
GET /api/inventory/{id}/movements?page=0&size=5&type=PURCHASE
```

**Response:**

```json
{
  "movements": [
    {
      "id": 1,
      "productId": 1,
      "qty": 50,
      "moveType": "PURCHASE",
      "batchNo": "BATCH001",
      "movedAt": "2024-01-01T14:30:00Z",
      "movedBy": 1,
      "note": "Purchase from supplier"
    }
  ],
  "totalItems": 15,
  "currentPage": 0
}
```

#### 1.8 API Documentation

```http
GET /api/inventory/docs
```

**Response:** Complete API documentation in JSON format

---

## 📋 2. STOCK MOVEMENT API

### Base Endpoint: `/api/stock-movements`

#### 2.1 List All Stock Movements

```http
GET /api/stock-movements
GET /api/stock-movements?page=0&size=10&productId=1&type=SALE
```

**Query Parameters:**

- `page`, `size`: Pagination
- `productId`: Filter by product ID
- `type`: Filter by movement type (PURCHASE, SALE, ADJUSTMENT, DAMAGE, RETURN)

**Response:**

```json
{
  "movements": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Product Name",
      "qty": 25,
      "moveType": "SALE",
      "batchNo": "BATCH001",
      "movedAt": "2024-01-02T09:15:00Z",
      "movedBy": 1,
      "movedByName": "John Doe",
      "note": "Customer purchase"
    }
  ],
  "totalItems": 128,
  "totalPages": 13,
  "currentPage": 0
}
```

#### 2.2 Get Movement by ID

```http
GET /api/stock-movements/{id}
```

**Response:** Single movement object

#### 2.3 Create Stock Movement

```http
POST /api/stock-movements
```

**Request Body:**

```json
{
  "productId": 1,
  "qty": 50,
  "moveType": "PURCHASE",
  "batchNo": "BATCH002",
  "movedBy": 1,
  "note": "Purchase from supplier ABC"
}
```

**Response:** `201 Created` with movement details

#### 2.4 Get Movements by Product

```http
GET /api/stock-movements/product/{productId}
GET /api/stock-movements/product/{productId}?page=0&size=5
```

**Response:** Paginated movements for specific product

#### 2.5 Create Bulk Movements

```http
POST /api/stock-movements/bulk
```

**Request Body:**

```json
[
  {
    "productId": 1,
    "qty": 30,
    "moveType": "SALE",
    "movedBy": 1,
    "note": "Bulk sale order #1001"
  },
  {
    "productId": 2,
    "qty": 15,
    "moveType": "SALE",
    "movedBy": 1,
    "note": "Bulk sale order #1002"
  }
]
```

**Response:** `201 Created` with array of created movements

---

## 🚨 3. ALERT MANAGEMENT API

### Base Endpoint: `/api/alerts`

#### 3.1 Get Active Alerts

```http
GET /api/alerts
GET /api/alerts?page=0&size=10
```

**Response:**

```json
{
  "alerts": [
    {
      "id": 1,
      "productId": 5,
      "productName": "Low Stock Item",
      "currentStock": 3,
      "minStock": 10,
      "alertType": "LOW_STOCK",
      "priority": "HIGH",
      "message": "Stock level critically low",
      "createdAt": "2024-01-02T08:00:00Z",
      "isResolved": false
    }
  ],
  "totalAlerts": 8,
  "unresolvedCount": 5
}
```

#### 3.2 Manual Alert Check

```http
POST /api/alerts/check
```

**Response:**

```json
{
  "message": "Alert check completed",
  "newAlertsCreated": 3,
  "alertsChecked": 45
}
```

#### 3.3 Resolve Alert

```http
PUT /api/alerts/{alertId}/resolve
```

**Response:**

```json
{
  "message": "Alert resolved successfully",
  "resolvedAt": "2024-01-02T11:30:00Z"
}
```

#### 3.4 Get Alert Statistics

```http
GET /api/alerts/stats
```

**Response:**

```json
{
  "totalAlerts": 25,
  "activeAlerts": 8,
  "resolvedAlerts": 17,
  "criticalAlerts": 2,
  "alertsByPriority": {
    "HIGH": 3,
    "MEDIUM": 4,
    "LOW": 1
  },
  "alertsByType": {
    "LOW_STOCK": 6,
    "OUT_OF_STOCK": 2
  }
}
```

#### 3.5 Get Alerts by Priority

```http
GET /api/alerts/priority/{priority}
```

**Priority values:** `HIGH`, `MEDIUM`, `LOW`
**Response:** Filtered alerts by priority level

#### 3.6 Alert API Documentation

```http
GET /api/alerts/docs
```

---

## 🏥 4. SYSTEM HEALTH API

#### 4.1 Health Check

```http
GET /health
```

**Response:**

```json
{
  "status": "UP",
  "timestamp": "2024-01-02T12:00:00Z",
  "services": {
    "database": "UP",
    "alertSystem": "UP"
  }
}
```

#### 4.2 Hello Endpoint

```http
GET /hello
```

**Response:**

```json
{
  "message": "Hello from Inventory Management API!",
  "version": "1.0.0",
  "timestamp": "2024-01-02T12:00:00Z"
}
```

---

## ⚠️ 5. ERROR HANDLING

### Standard HTTP Status Codes:

- `200 OK`: Successful GET, PUT operations
- `201 Created`: Successful POST operations
- `204 No Content`: Successful DELETE operations
- `400 Bad Request`: Invalid request data
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server-side errors

### Error Response Format:

```json
{
  "error": "Not Found",
  "message": "Inventory item with ID 999 not found",
  "timestamp": "2024-01-02T12:00:00Z",
  "path": "/api/inventory/999"
}
```

---

## 🧪 6. TESTING

### Run Complete Test Suite:

```powershell
.\test_complete_api_day7.ps1
```

### Individual API Testing:

```bash
# Test inventory API
curl -X GET http://localhost:8080/api/inventory

# Test stock movements
curl -X GET http://localhost:8080/api/stock-movements

# Test alerts
curl -X GET http://localhost:8080/api/alerts

# Health check
curl -X GET http://localhost:8080/health
```

---

## 🚀 7. DEPLOYMENT

### Start Server:

```bash
cd mini-boot
mvn spring-boot:run
# OR
java -jar target/mini-boot-1.0-SNAPSHOT.jar
```

### Server Configuration:

- **Port:** 8080 (configurable)
- **CORS:** Enabled for all origins
- **Logging:** Console output with timestamps
- **Threading:** Multi-threaded request handling

---

## ✅ 8. COMPLETION STATUS

### NGÀY 7 - BACKEND DEVELOPMENT: ✅ COMPLETED

**Implemented Features:**

- ✅ Complete Inventory CRUD API with pagination & filtering
- ✅ Stock Movement tracking with bulk operations
- ✅ Alert Management system with priority levels
- ✅ Comprehensive error handling
- ✅ API documentation endpoints
- ✅ Input validation & data integrity
- ✅ Health monitoring endpoints
- ✅ Complete test coverage

**Next Phase:** NGÀY 8 - Frontend Integration

- JavaFX UI integration with REST APIs
- Real-time alert notifications
- Data synchronization
- User experience enhancements
