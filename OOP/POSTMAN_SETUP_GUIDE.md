# 🚀 POSTMAN SETUP GUIDE - INVENTORY MANAGEMENT API

# Complete API Testing Setup cho NGÀY 7-8 Development

## 📋 PREREQUISITES - Chuẩn bị

### 1. Start Backend Server

```bash
# Mở terminal tại: c:\BTL_OOP\BTL_OOP\OOP\mini-boot
cd c:\BTL_OOP\BTL_OOP\OOP\mini-boot
mvn clean compile exec:java
```

### 2. Verify Server Running

```
🌐 Server URL: http://localhost:8080
✅ Health Check: GET http://localhost:8080/api/health
```

---

## 🛠️ POSTMAN CONFIGURATION

### 📁 **STEP 1: Create Collection**

1. Mở Postman
2. Click **"New Collection"**
3. Name: **"Inventory Management API"**
4. Description: **"Complete API testing for NGÀY 7-8 development"**

### 🌐 **STEP 2: Setup Environment Variables**

1. Click **"Environments"** → **"Create Environment"**
2. Name: **"Inventory Local"**
3. Add variables:

| Variable      | Initial Value           | Current Value           |
| ------------- | ----------------------- | ----------------------- |
| `base_url`    | `http://localhost:8080` | `http://localhost:8080` |
| `api_prefix`  | `/api`                  | `/api`                  |
| `product_id`  | `1`                     | `1`                     |
| `movement_id` | `1`                     | `1`                     |
| `alert_id`    | `1`                     | `1`                     |

---

## 📂 API ENDPOINTS SETUP

## 🏥 **1. HEALTH CHECK APIs**

### 1.1 Health Check

```
Method: GET
URL: {{base_url}}/api/health
Headers: Content-Type: application/json

Expected Response:
{
    "status": "UP",
    "timestamp": "2024-10-14T10:30:00",
    "uptime": "00:05:23"
}
```

### 1.2 System Info

```
Method: GET
URL: {{base_url}}/api/info
Headers: Content-Type: application/json

Expected Response:
{
    "application": "Mini-Boot Inventory System",
    "version": "1.0.0",
    "endpoints": 21
}
```

---

## 📦 **2. INVENTORY MANAGEMENT APIs**

### 2.1 Get All Inventory (Pagination)

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory
Params:
  page: 0
  size: 10
  category: electronics
  minStock: 5

Headers: Content-Type: application/json

Expected Response:
{
    "content": [
        {
            "id": 1,
            "name": "iPhone 14",
            "category": "electronics",
            "price": 999.99,
            "quantity": 50,
            "minStockLevel": 10,
            "supplier": "Apple Inc"
        }
    ],
    "page": 0,
    "size": 10,
    "total": 25
}
```

### 2.2 Get Product by ID

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory/{{product_id}}
Headers: Content-Type: application/json

Expected Response:
{
    "id": 1,
    "name": "iPhone 14",
    "category": "electronics",
    "price": 999.99,
    "quantity": 50,
    "minStockLevel": 10,
    "supplier": "Apple Inc",
    "createdAt": "2024-10-14T10:00:00",
    "updatedAt": "2024-10-14T10:30:00"
}
```

### 2.3 Create New Product

```
Method: POST
URL: {{base_url}}{{api_prefix}}/inventory
Headers: Content-Type: application/json

Body (raw JSON):
{
    "name": "Samsung Galaxy S24",
    "category": "electronics",
    "price": 899.99,
    "quantity": 30,
    "minStockLevel": 15,
    "supplier": "Samsung Electronics"
}

Expected Response:
{
    "id": 2,
    "name": "Samsung Galaxy S24",
    "category": "electronics",
    "price": 899.99,
    "quantity": 30,
    "minStockLevel": 15,
    "supplier": "Samsung Electronics",
    "createdAt": "2024-10-14T11:00:00"
}
```

### 2.4 Update Product

```
Method: PUT
URL: {{base_url}}{{api_prefix}}/inventory/{{product_id}}
Headers: Content-Type: application/json

Body (raw JSON):
{
    "name": "iPhone 14 Pro",
    "category": "electronics",
    "price": 1099.99,
    "quantity": 45,
    "minStockLevel": 12,
    "supplier": "Apple Inc"
}

Expected Response:
{
    "id": 1,
    "name": "iPhone 14 Pro",
    "category": "electronics",
    "price": 1099.99,
    "quantity": 45,
    "minStockLevel": 12,
    "supplier": "Apple Inc",
    "updatedAt": "2024-10-14T11:15:00"
}
```

### 2.5 Delete Product

```
Method: DELETE
URL: {{base_url}}{{api_prefix}}/inventory/{{product_id}}
Headers: Content-Type: application/json

Expected Response:
{
    "message": "Product deleted successfully",
    "deletedId": 1
}
```

### 2.6 Set Initial Stock

```
Method: POST
URL: {{base_url}}{{api_prefix}}/inventory/{{product_id}}/initial-stock
Headers: Content-Type: application/json

Body (raw JSON):
{
    "quantity": 100,
    "note": "Initial stock setup for new product"
}

Expected Response:
{
    "message": "Initial stock set successfully",
    "productId": 1,
    "quantity": 100,
    "movementId": 15
}
```

### 2.7 Get Product Movements

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory/{{product_id}}/movements
Params:
  page: 0
  size: 10

Expected Response:
{
    "content": [
        {
            "id": 1,
            "productId": 1,
            "type": "PURCHASE",
            "quantity": 50,
            "price": 45000.0,
            "note": "Monthly stock replenishment",
            "createdAt": "2024-10-14T09:00:00"
        }
    ],
    "page": 0,
    "size": 10,
    "total": 5
}
```

### 2.8 API Documentation

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory/docs
Headers: Content-Type: application/json

Expected Response: HTML documentation page
```

---

## 📊 **3. STOCK MOVEMENT APIs**

### 3.1 Get All Movements (with Filters)

```
Method: GET
URL: {{base_url}}{{api_prefix}}/stock-movements
Params:
  page: 0
  size: 10
  productId: 1
  type: PURCHASE

Headers: Content-Type: application/json

Expected Response:
{
    "content": [
        {
            "id": 1,
            "productId": 1,
            "productName": "iPhone 14",
            "type": "PURCHASE",
            "quantity": 50,
            "price": 45000.0,
            "note": "Monthly replenishment",
            "createdAt": "2024-10-14T09:00:00"
        }
    ],
    "page": 0,
    "size": 10,
    "total": 15
}
```

### 3.2 Get Movement by ID

```
Method: GET
URL: {{base_url}}{{api_prefix}}/stock-movements/{{movement_id}}
Headers: Content-Type: application/json

Expected Response:
{
    "id": 1,
    "productId": 1,
    "productName": "iPhone 14",
    "type": "PURCHASE",
    "quantity": 50,
    "price": 45000.0,
    "note": "Monthly replenishment",
    "createdAt": "2024-10-14T09:00:00"
}
```

### 3.3 Create New Movement

```
Method: POST
URL: {{base_url}}{{api_prefix}}/stock-movements
Headers: Content-Type: application/json

Body (raw JSON):
{
    "productId": 1,
    "type": "PURCHASE",
    "quantity": 25,
    "price": 22500.0,
    "note": "Additional stock for weekend sale"
}

Movement Types: PURCHASE | SALE | ADJUSTMENT | DAMAGE | RETURN

Expected Response:
{
    "id": 16,
    "productId": 1,
    "type": "PURCHASE",
    "quantity": 25,
    "price": 22500.0,
    "note": "Additional stock for weekend sale",
    "createdAt": "2024-10-14T11:30:00"
}
```

### 3.4 Get Movements by Product

```
Method: GET
URL: {{base_url}}{{api_prefix}}/stock-movements/product/{{product_id}}
Params:
  page: 0
  size: 5

Expected Response:
{
    "content": [
        {
            "id": 1,
            "productId": 1,
            "type": "PURCHASE",
            "quantity": 50,
            "price": 45000.0,
            "note": "Monthly replenishment",
            "createdAt": "2024-10-14T09:00:00"
        }
    ],
    "page": 0,
    "size": 5,
    "total": 3
}
```

### 3.5 Bulk Create Movements

```
Method: POST
URL: {{base_url}}{{api_prefix}}/stock-movements/bulk
Headers: Content-Type: application/json

Body (raw JSON):
[
    {
        "productId": 1,
        "type": "SALE",
        "quantity": 5,
        "price": 4999.95,
        "note": "Customer A purchase"
    },
    {
        "productId": 2,
        "type": "SALE",
        "quantity": 3,
        "price": 2699.97,
        "note": "Customer B purchase"
    }
]

Expected Response:
{
    "message": "Bulk movements created successfully",
    "createdCount": 2,
    "movements": [17, 18]
}
```

---

## 🚨 **4. ALERT SYSTEM APIs**

### 4.1 Get Active Alerts

```
Method: GET
URL: {{base_url}}{{api_prefix}}/alerts
Params:
  priority: HIGH
  resolved: false

Headers: Content-Type: application/json

Expected Response:
{
    "alerts": [
        {
            "id": 1,
            "productId": 1,
            "productName": "iPhone 14",
            "alertType": "LOW_STOCK",
            "priority": "HIGH",
            "message": "Stock level (8) is below minimum (10)",
            "currentStock": 8,
            "minStock": 10,
            "createdAt": "2024-10-14T10:45:00",
            "isResolved": false
        }
    ],
    "total": 5
}
```

### 4.2 Manual Alert Check

```
Method: POST
URL: {{base_url}}{{api_prefix}}/alerts/check
Headers: Content-Type: application/json

Expected Response:
{
    "message": "Alert check completed",
    "newAlerts": 3,
    "totalAlerts": 8,
    "alerts": [
        {
            "id": 2,
            "productId": 3,
            "productName": "iPad Air",
            "alertType": "LOW_STOCK",
            "priority": "MEDIUM",
            "message": "Stock level (7) is below threshold (10)",
            "currentStock": 7,
            "minStock": 10,
            "createdAt": "2024-10-14T11:00:00"
        }
    ]
}
```

### 4.3 Resolve Alert

```
Method: PUT
URL: {{base_url}}{{api_prefix}}/alerts/{{alert_id}}/resolve
Headers: Content-Type: application/json

Expected Response:
{
    "message": "Alert resolved successfully",
    "alertId": 1,
    "resolvedAt": "2024-10-14T11:45:00"
}
```

### 4.4 Get Alert Statistics

```
Method: GET
URL: {{base_url}}{{api_prefix}}/alerts/stats
Headers: Content-Type: application/json

Expected Response:
{
    "totalAlerts": 15,
    "activeAlerts": 8,
    "resolvedAlerts": 7,
    "byPriority": {
        "HIGH": 3,
        "MEDIUM": 4,
        "LOW": 1
    },
    "byType": {
        "LOW_STOCK": 8
    }
}
```

### 4.5 Get Alerts by Priority

```
Method: GET
URL: {{base_url}}{{api_prefix}}/alerts/priority/HIGH
Headers: Content-Type: application/json

Priority Levels: HIGH | MEDIUM | LOW

Expected Response:
{
    "priority": "HIGH",
    "alerts": [
        {
            "id": 1,
            "productId": 1,
            "productName": "iPhone 14",
            "alertType": "LOW_STOCK",
            "priority": "HIGH",
            "message": "Critical stock level (3) - immediate restocking required",
            "currentStock": 3,
            "minStock": 10,
            "createdAt": "2024-10-14T10:45:00"
        }
    ],
    "count": 3
}
```

### 4.6 Alert Documentation

```
Method: GET
URL: {{base_url}}{{api_prefix}}/alerts/docs
Headers: Content-Type: application/json

Expected Response: HTML documentation page
```

---

## 🧪 **5. ERROR TESTING SCENARIOS**

### 5.1 Test 404 - Product Not Found

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory/999
Headers: Content-Type: application/json

Expected Response (404):
{
    "error": "Product not found",
    "productId": 999,
    "timestamp": "2024-10-14T12:00:00"
}
```

### 5.2 Test 400 - Invalid Request Body

```
Method: POST
URL: {{base_url}}{{api_prefix}}/inventory
Headers: Content-Type: application/json

Body (invalid JSON):
{
    "name": "",
    "price": -100
}

Expected Response (400):
{
    "error": "Invalid request data",
    "details": [
        "Product name cannot be empty",
        "Price must be positive"
    ],
    "timestamp": "2024-10-14T12:05:00"
}
```

### 5.3 Test 500 - Server Error Simulation

```
Method: GET
URL: {{base_url}}{{api_prefix}}/inventory/error-test
Headers: Content-Type: application/json

Expected Response (500):
{
    "error": "Internal server error",
    "message": "Simulated error for testing",
    "timestamp": "2024-10-14T12:10:00"
}
```

---

## ⚡ **6. PERFORMANCE TESTING**

### 6.1 Response Time Test

1. Select any endpoint
2. Click **"Send"**
3. Check **Response Time** trong results
4. **Target**: < 2000ms (2 seconds)
5. **Optimal**: < 1000ms (1 second)

### 6.2 Concurrent Requests Test

1. Create multiple tabs với same request
2. Click **"Send"** simultaneously
3. Check all responses succeed
4. Verify data consistency

---

## 📝 **7. POSTMAN COLLECTION EXPORT**

### Create Complete Collection:

1. **Right-click collection** → **"Export"**
2. Choose **"Collection v2.1"** format
3. Save as: **"Inventory_Management_API.postman_collection.json"**

### Include Environment:

1. **Right-click environment** → **"Export"**
2. Save as: **"Inventory_Local.postman_environment.json"**

### Share với team:

```
📁 Files to share:
- Inventory_Management_API.postman_collection.json
- Inventory_Local.postman_environment.json
- POSTMAN_SETUP_GUIDE.md (this file)
```

---

## 🔧 **8. TROUBLESHOOTING**

### Common Issues:

#### 🚫 **Connection Refused**

```
Error: connect ECONNREFUSED 127.0.0.1:8080
Solution:
1. Check backend server is running
2. Verify URL: http://localhost:8080
3. Check firewall settings
```

#### 🚫 **404 Not Found**

```
Error: Cannot GET /api/inventory
Solution:
1. Check endpoint spelling
2. Verify server mounted routes correctly
3. Check API documentation
```

#### 🚫 **500 Server Error**

```
Error: Internal Server Error
Solution:
1. Check server console logs
2. Verify request body format
3. Check database connectivity
```

#### 🚫 **Timeout**

```
Error: Request timeout
Solution:
1. Increase Postman timeout settings
2. Check server performance
3. Verify network connectivity
```

---

## ✅ **9. TESTING CHECKLIST**

### Essential Tests:

- [ ] Health Check - Server is running
- [ ] Get All Inventory - Basic retrieval
- [ ] Create Product - Data creation
- [ ] Update Product - Data modification
- [ ] Delete Product - Data removal
- [ ] Create Movement - Stock tracking
- [ ] Generate Alerts - Alert system
- [ ] Resolve Alert - Alert management
- [ ] Error Handling - 404, 400, 500 responses
- [ ] Performance - Response time < 2s

### Advanced Tests:

- [ ] Pagination - Large datasets
- [ ] Filtering - Search functionality
- [ ] Bulk Operations - Multiple movements
- [ ] Concurrent Requests - Multi-user simulation
- [ ] Data Validation - Input sanitization
- [ ] Error Recovery - Network failures

---

## 🎯 **EXPECTED RESULTS**

### ✅ **Success Indicators:**

- All 21 endpoints respond correctly
- Response times < 2 seconds
- Proper JSON format responses
- Error codes match expected values
- Data persistence works correctly

### 📊 **Performance Benchmarks:**

- **GET requests**: < 500ms average
- **POST/PUT requests**: < 1000ms average
- **Bulk operations**: < 2000ms
- **Error responses**: < 200ms

### 🔐 **Data Integrity:**

- Created data persists across requests
- Updates reflect immediately
- Deletions remove data completely
- Movements update inventory quantities
- Alerts generate correctly for low stock

---

## 🚀 **AUTOMATION TIPS**

### Collection Runner:

1. Select collection → **"Run"**
2. Choose environment
3. Set iterations: 1-5
4. Enable **"Save responses"**
5. Check **"Keep variable values"**

### Pre-request Scripts:

```javascript
// Auto-generate timestamps
pm.environment.set("timestamp", new Date().toISOString());

// Random product ID for testing
pm.environment.set("random_id", Math.floor(Math.random() * 100));
```

### Test Scripts:

```javascript
// Basic status check
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

// Response time check
pm.test("Response time is less than 2000ms", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});

// JSON format check
pm.test("Response is JSON", function () {
  pm.response.to.be.json;
});
```

---

**🎉 Happy Testing! Tất cả 21 API endpoints đã sẵn sàng cho comprehensive testing với Postman! 🎉**
