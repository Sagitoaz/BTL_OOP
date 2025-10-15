# 📋 INVENTORY MANAGEMENT REST API - NGÀY 7 HOÀN THÀNH

## 🎯 TỔNG QUAN

REST API hoàn chỉnh cho quản lý kho với đầy đủ CRUD operations, pagination, filtering và stock management.

## 🚀 KHỞI ĐỘNG SERVER

```bash
cd "c:\BTL_OOP\BTL_OOP\OOP\mini-boot"
mvn compile exec:java -Dexec.mainClass="org.miniboot.app.ServerMain"
```

Server sẽ chạy trên: `http://localhost:8080`

## 📊 API ENDPOINTS

### 1. **GET /api/inventory** - Danh sách inventory với pagination

```bash
# Basic list
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET

# With pagination
$uri = "http://localhost:8080/api/inventory?page=0&size=10"
Invoke-RestMethod -Uri $uri -Method GET

# With filtering
$uri = "http://localhost:8080/api/inventory?category=electronics&lowStock=true"
Invoke-RestMethod -Uri $uri -Method GET
```

**Response:**

```json
{
  "content": [
    {
      "id": 1,
      "sku": "PROD001",
      "name": "Product 1",
      "category": "electronics",
      "qtyOnHand": 50,
      "reorderPoint": 10,
      "updatedAt": "2025-10-14T02:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. **GET /api/inventory/{id}** - Lấy inventory theo ID

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/1" -Method GET
```

**Response:**

```json
{
  "id": 1,
  "sku": "PROD001",
  "name": "Product 1",
  "category": "electronics",
  "qtyOnHand": 50,
  "reorderPoint": 10,
  "updatedAt": "2025-10-14T02:30:00"
}
```

### 3. **POST /api/inventory** - Tạo mới inventory

```bash
$body = @{
    sku = "PROD002"
    name = "New Product"
    category = "electronics"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $body -ContentType "application/json"
```

**Request Body:**

```json
{
  "sku": "PROD002",
  "name": "New Product",
  "category": "electronics"
}
```

**Response:** Status 201 Created

```json
{
  "id": 2,
  "sku": "PROD002",
  "name": "New Product",
  "category": "electronics",
  "qtyOnHand": 0,
  "reorderPoint": 10,
  "updatedAt": "2025-10-14T02:35:00"
}
```

### 4. **PUT /api/inventory/{id}** - Cập nhật inventory

```bash
$body = @{
    name = "Updated Product Name"
    category = "updated-category"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/1" -Method PUT -Body $body -ContentType "application/json"
```

**Request Body:**

```json
{
  "name": "Updated Product Name",
  "category": "updated-category"
}
```

### 5. **DELETE /api/inventory/{id}** - Xóa inventory

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/1" -Method DELETE
```

**Response:**

```json
{
  "deleted": true,
  "id": 1
}
```

### 6. **GET /api/inventory/{id}/movements** - Lấy lịch sử stock movements

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/1/movements" -Method GET
```

**Response:**

```json
{
  "movements": [
    {
      "id": 1,
      "type": "OPENING",
      "qty": 0,
      "note": "No movements yet"
    }
  ]
}
```

### 7. **POST /api/inventory/{id}/initial-stock** - Nhập kho đầu tiên

```bash
$body = @{
    qty = 100
    note = "Initial inventory stock"
    batchNo = "BATCH001"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/1/initial-stock" -Method POST -Body $body -ContentType "application/json"
```

**Request Body:**

```json
{
  "qty": 100,
  "note": "Initial inventory stock",
  "batchNo": "BATCH001"
}
```

### 8. **GET /api/inventory/docs** - API Documentation

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/docs" -Method GET
```

## 🧪 SCRIPT TEST HOÀN CHỈNH

Tạo file `test_complete_api.ps1`:

```powershell
# Test Complete Inventory API
Write-Host "=== TESTING COMPLETE INVENTORY API ===" -ForegroundColor Green

# Test 1: List inventories
Write-Host "`n1. Testing inventory list..." -ForegroundColor Yellow
try {
    $list = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET
    Write-Host "✓ SUCCESS: Got inventory list" -ForegroundColor Green
    Write-Host "Total items: $($list.totalElements)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create new inventory
Write-Host "`n2. Testing create inventory..." -ForegroundColor Yellow
try {
    $createBody = @{
        sku = "TEST001"
        name = "Test Product"
        category = "test"
    } | ConvertTo-Json

    $created = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $createBody -ContentType "application/json"
    Write-Host "✓ SUCCESS: Created inventory ID $($created.id)" -ForegroundColor Green
    $testId = $created.id
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get by ID
Write-Host "`n3. Testing get by ID..." -ForegroundColor Yellow
if ($testId) {
    try {
        $item = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$testId" -Method GET
        Write-Host "✓ SUCCESS: Got item '$($item.name)'" -ForegroundColor Green
    } catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 4: Update inventory
Write-Host "`n4. Testing update inventory..." -ForegroundColor Yellow
if ($testId) {
    try {
        $updateBody = @{
            name = "Updated Test Product"
            category = "updated-test"
        } | ConvertTo-Json

        $updated = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$testId" -Method PUT -Body $updateBody -ContentType "application/json"
        Write-Host "✓ SUCCESS: Updated to '$($updated.name)'" -ForegroundColor Green
    } catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 5: Record initial stock
Write-Host "`n5. Testing initial stock..." -ForegroundColor Yellow
if ($testId) {
    try {
        $stockBody = @{
            qty = 50
            note = "Test initial stock"
        } | ConvertTo-Json

        $stock = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$testId/initial-stock" -Method POST -Body $stockBody -ContentType "application/json"
        Write-Host "✓ SUCCESS: Added initial stock" -ForegroundColor Green
    } catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 6: Get movements
Write-Host "`n6. Testing stock movements..." -ForegroundColor Yellow
if ($testId) {
    try {
        $movements = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$testId/movements" -Method GET
        Write-Host "✓ SUCCESS: Got movements" -ForegroundColor Green
        Write-Host "Movements count: $($movements.movements.Count)" -ForegroundColor Cyan
    } catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 7: Delete inventory
Write-Host "`n7. Testing delete inventory..." -ForegroundColor Yellow
if ($testId) {
    try {
        $deleted = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$testId" -Method DELETE
        Write-Host "✓ SUCCESS: Deleted inventory" -ForegroundColor Green
    } catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 8: API Documentation
Write-Host "`n8. Testing API docs..." -ForegroundColor Yellow
try {
    $docs = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/docs" -Method GET
    Write-Host "✓ SUCCESS: Got API documentation" -ForegroundColor Green
    Write-Host "Title: $($docs.title)" -ForegroundColor Cyan
    Write-Host "Version: $($docs.version)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TESTING COMPLETED ===" -ForegroundColor Green
```

## 📈 STATUS CODES

- **200 OK** - Request thành công
- **201 Created** - Tạo mới thành công
- **400 Bad Request** - Dữ liệu không hợp lệ
- **404 Not Found** - Không tìm thấy resource
- **500 Internal Server Error** - Lỗi server

## 🎉 HOÀN THÀNH NGÀY 7

✅ **REST API Inventory Management đã hoàn thành với:**

- CRUD operations đầy đủ (Create, Read, Update, Delete)
- Pagination và filtering
- Stock movement tracking
- Initial stock recording
- Error handling chuẩn HTTP status codes
- API documentation endpoint
- Validation đầu vào
- JSON response format chuẩn

🚀 **Sẵn sàng cho production!**
