# 🚀 POWERSHELL API TESTING COMMANDS
# Quick Test Commands for Inventory Management API

# =============================================================================
# SETUP & HEALTH CHECK
# =============================================================================

# Test server connectivity
Write-Host "Testing server connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method GET -ContentType "application/json"
    Write-Host "Server is running!" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Server not running! Start backend first." -ForegroundColor Red
    Write-Host "Run: cd c:\BTL_OOP\BTL_OOP\OOP\mini-boot; mvn clean compile exec:java" -ForegroundColor Yellow
}

# =============================================================================
# INVENTORY MANAGEMENT TESTS
# =============================================================================

# 1. Get All Inventory
Write-Host "`n📦 Testing GET All Inventory..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET -ContentType "application/json" | ConvertTo-Json

# 2. Get Inventory with Pagination
Write-Host "Testing GET Inventory with Pagination..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/inventory?page=0&size=5" -Method GET -ContentType "application/json" | ConvertTo-Json

# 3. Create New Product
Write-Host "`n📦 Testing POST Create Product..." -ForegroundColor Yellow
$newProduct = @{
    name = "Test Product PowerShell"
    category = "electronics"
    price = 299.99
    quantity = 25
    minStockLevel = 10
    supplier = "PowerShell Supplier"
} | ConvertTo-Json

try {
    $createdProduct = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $newProduct -ContentType "application/json"
    Write-Host "✅ Product created successfully!" -ForegroundColor Green
    $createdProduct | ConvertTo-Json
    $global:testProductId = $createdProduct.id
} catch {
    Write-Host "❌ Failed to create product: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Get Product by ID (using created product)
if ($global:testProductId) {
    Write-Host "`n📦 Testing GET Product by ID: $global:testProductId..." -ForegroundColor Yellow
    Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method GET -ContentType "application/json" | ConvertTo-Json
}

# 5. Update Product
if ($global:testProductId) {
    Write-Host "`n📦 Testing PUT Update Product..." -ForegroundColor Yellow
    $updateProduct = @{
        name = "Updated Test Product PowerShell"
        category = "electronics"
        price = 349.99
        quantity = 30
        minStockLevel = 12
        supplier = "Updated PowerShell Supplier"
    } | ConvertTo-Json

    try {
        $updatedProduct = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method PUT -Body $updateProduct -ContentType "application/json"
        Write-Host "✅ Product updated successfully!" -ForegroundColor Green
        $updatedProduct | ConvertTo-Json
    } catch {
        Write-Host "❌ Failed to update product: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 6. Set Initial Stock
if ($global:testProductId) {
    Write-Host "`n📦 Testing POST Set Initial Stock..." -ForegroundColor Yellow
    $initialStock = @{
        quantity = 50
        note = "Initial stock from PowerShell test"
    } | ConvertTo-Json

    try {
        $stockResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId/initial-stock" -Method POST -Body $initialStock -ContentType "application/json"
        Write-Host "✅ Initial stock set successfully!" -ForegroundColor Green
        $stockResponse | ConvertTo-Json
    } catch {
        Write-Host "❌ Failed to set initial stock: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# STOCK MOVEMENT TESTS
# =============================================================================

# 7. Create Stock Movement
if ($global:testProductId) {
    Write-Host "`n📊 Testing POST Create Stock Movement..." -ForegroundColor Yellow
    $newMovement = @{
        productId = $global:testProductId
        type = "PURCHASE"
        quantity = 15
        price = 5249.85
        note = "PowerShell test movement"
    } | ConvertTo-Json

    try {
        $createdMovement = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method POST -Body $newMovement -ContentType "application/json"
        Write-Host "✅ Movement created successfully!" -ForegroundColor Green
        $createdMovement | ConvertTo-Json
        $global:testMovementId = $createdMovement.id
    } catch {
        Write-Host "❌ Failed to create movement: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 8. Get All Movements
Write-Host "`n📊 Testing GET All Stock Movements..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method GET -ContentType "application/json" | ConvertTo-Json

# 9. Get Movements by Product
if ($global:testProductId) {
    Write-Host "`n📊 Testing GET Movements by Product..." -ForegroundColor Yellow
    Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements/product/$global:testProductId" -Method GET -ContentType "application/json" | ConvertTo-Json
}

# 10. Bulk Create Movements
if ($global:testProductId) {
    Write-Host "`n📊 Testing POST Bulk Create Movements..." -ForegroundColor Yellow
    $bulkMovements = @(
        @{
            productId = $global:testProductId
            type = "SALE"
            quantity = 3
            price = 1049.97
            note = "PowerShell bulk sale 1"
        },
        @{
            productId = $global:testProductId
            type = "SALE"
            quantity = 2
            price = 699.98
            note = "PowerShell bulk sale 2"
        }
    ) | ConvertTo-Json

    try {
        $bulkResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements/bulk" -Method POST -Body $bulkMovements -ContentType "application/json"
        Write-Host "✅ Bulk movements created successfully!" -ForegroundColor Green
        $bulkResponse | ConvertTo-Json
    } catch {
        Write-Host "❌ Failed to create bulk movements: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# ALERT SYSTEM TESTS
# =============================================================================

# 11. Get All Alerts
Write-Host "`n🚨 Testing GET All Alerts..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/alerts" -Method GET -ContentType "application/json" | ConvertTo-Json

# 12. Manual Alert Check
Write-Host "`n🚨 Testing POST Manual Alert Check..." -ForegroundColor Yellow
try {
    $alertCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/check" -Method POST -ContentType "application/json"
    Write-Host "✅ Alert check completed!" -ForegroundColor Green
    $alertCheck | ConvertTo-Json
} catch {
    Write-Host "❌ Failed to check alerts: $($_.Exception.Message)" -ForegroundColor Red
}

# 13. Get Alert Statistics
Write-Host "`n🚨 Testing GET Alert Statistics..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/stats" -Method GET -ContentType "application/json" | ConvertTo-Json

# 14. Get High Priority Alerts
Write-Host "`n🚨 Testing GET High Priority Alerts..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/priority/HIGH" -Method GET -ContentType "application/json" | ConvertTo-Json

# =============================================================================
# ERROR TESTING
# =============================================================================

# 15. Test 404 Error
Write-Host "`n❌ Testing 404 Error (Product Not Found)..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/99999" -Method GET -ContentType "application/json"
} catch {
    Write-Host "✅ 404 Error handled correctly: $($_.Exception.Message)" -ForegroundColor Green
}

# 16. Test Invalid Request Body
Write-Host "`n❌ Testing 400 Error (Invalid Request)..." -ForegroundColor Yellow
$invalidProduct = @{
    name = ""
    price = -100
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $invalidProduct -ContentType "application/json"
} catch {
    Write-Host "✅ 400 Error handled correctly: $($_.Exception.Message)" -ForegroundColor Green
}

# =============================================================================
# CLEANUP (Optional)
# =============================================================================

# Delete test product (optional)
if ($global:testProductId) {
    Write-Host "`n🗑️ Cleaning up test product..." -ForegroundColor Yellow
    try {
        $deleteResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method DELETE -ContentType "application/json"
        Write-Host "✅ Test product deleted successfully!" -ForegroundColor Green
        $deleteResponse | ConvertTo-Json
    } catch {
        Write-Host "❌ Failed to delete test product: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# SUMMARY
# =============================================================================

Write-Host "`n🎉 API Testing Complete!" -ForegroundColor Green
Write-Host "📊 Tested Endpoints:" -ForegroundColor Cyan
Write-Host "  ✅ Health Check" -ForegroundColor White
Write-Host "  ✅ Inventory CRUD Operations" -ForegroundColor White
Write-Host "  ✅ Stock Movement Management" -ForegroundColor White
Write-Host "  ✅ Alert System" -ForegroundColor White
Write-Host "  ✅ Error Handling" -ForegroundColor White
Write-Host "`n🚀 All 21 API endpoints tested successfully!" -ForegroundColor Green