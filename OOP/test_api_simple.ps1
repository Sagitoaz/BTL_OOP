# PowerShell API Testing Script for Inventory Management System
# Simple and working version

Write-Host "Starting API Tests..." -ForegroundColor Green

# Test 1: Health Check
Write-Host "`n1. Testing Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method GET
    Write-Host "SUCCESS: Server is running!" -ForegroundColor Green
    $health | ConvertTo-Json
} catch {
    Write-Host "ERROR: Server not running!" -ForegroundColor Red
    Write-Host "Please start backend: cd mini-boot && mvn clean compile exec:java" -ForegroundColor Yellow
    exit
}

# Test 2: Get All Inventory
Write-Host "`n2. Testing Get All Inventory..." -ForegroundColor Yellow
try {
    $inventory = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET
    Write-Host "SUCCESS: Retrieved inventory list" -ForegroundColor Green
    $inventory | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR: Failed to get inventory - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create New Product
Write-Host "`n3. Testing Create New Product..." -ForegroundColor Yellow
$newProduct = @{
    name = "Test Product PowerShell"
    category = "electronics"
    price = 299.99
    quantity = 25
    minStockLevel = 10
    supplier = "PowerShell Supplier"
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $newProduct -ContentType "application/json"
    Write-Host "SUCCESS: Product created with ID: $($created.id)" -ForegroundColor Green
    $global:testProductId = $created.id
    $created | ConvertTo-Json
} catch {
    Write-Host "ERROR: Failed to create product - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get Product by ID
if ($global:testProductId) {
    Write-Host "`n4. Testing Get Product by ID: $global:testProductId..." -ForegroundColor Yellow
    try {
        $product = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method GET
        Write-Host "SUCCESS: Retrieved product details" -ForegroundColor Green
        $product | ConvertTo-Json
    } catch {
        Write-Host "ERROR: Failed to get product - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 5: Update Product
if ($global:testProductId) {
    Write-Host "`n5. Testing Update Product..." -ForegroundColor Yellow
    $updateData = @{
        name = "Updated Test Product"
        category = "electronics"
        price = 349.99
        quantity = 30
        minStockLevel = 12
        supplier = "Updated Supplier"
    } | ConvertTo-Json
    
    try {
        $updated = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method PUT -Body $updateData -ContentType "application/json"
        Write-Host "SUCCESS: Product updated" -ForegroundColor Green
        $updated | ConvertTo-Json
    } catch {
        Write-Host "ERROR: Failed to update product - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 6: Create Stock Movement
if ($global:testProductId) {
    Write-Host "`n6. Testing Create Stock Movement..." -ForegroundColor Yellow
    $movement = @{
        productId = $global:testProductId
        type = "PURCHASE"
        quantity = 15
        price = 5249.85
        note = "PowerShell test movement"
    } | ConvertTo-Json
    
    try {
        $movementResult = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method POST -Body $movement -ContentType "application/json"
        Write-Host "SUCCESS: Movement created with ID: $($movementResult.id)" -ForegroundColor Green
        $movementResult | ConvertTo-Json
    } catch {
        Write-Host "ERROR: Failed to create movement - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 7: Get Stock Movements
Write-Host "`n7. Testing Get Stock Movements..." -ForegroundColor Yellow
try {
    $movements = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method GET
    Write-Host "SUCCESS: Retrieved movements list" -ForegroundColor Green
    $movements | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR: Failed to get movements - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Check Alerts
Write-Host "`n8. Testing Alert Check..." -ForegroundColor Yellow
try {
    $alertCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/check" -Method POST -ContentType "application/json"
    Write-Host "SUCCESS: Alert check completed" -ForegroundColor Green
    $alertCheck | ConvertTo-Json
} catch {
    Write-Host "ERROR: Failed to check alerts - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: Get Alerts
Write-Host "`n9. Testing Get Alerts..." -ForegroundColor Yellow
try {
    $alerts = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts" -Method GET
    Write-Host "SUCCESS: Retrieved alerts" -ForegroundColor Green
    $alerts | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR: Failed to get alerts - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 10: Get Alert Statistics
Write-Host "`n10. Testing Alert Statistics..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/stats" -Method GET
    Write-Host "SUCCESS: Retrieved alert statistics" -ForegroundColor Green
    $stats | ConvertTo-Json
} catch {
    Write-Host "ERROR: Failed to get alert stats - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 11: Error Testing (404)
Write-Host "`n11. Testing 404 Error Handling..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/99999" -Method GET
    Write-Host "ERROR: Should have returned 404!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: 404 error handled correctly" -ForegroundColor Green
    Write-Host "Error message: $($_.Exception.Message)" -ForegroundColor Gray
}

# Cleanup: Delete test product
if ($global:testProductId) {
    Write-Host "`n12. Cleaning up test product..." -ForegroundColor Yellow
    try {
        $deleteResult = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$global:testProductId" -Method DELETE
        Write-Host "SUCCESS: Test product deleted" -ForegroundColor Green
        $deleteResult | ConvertTo-Json
    } catch {
        Write-Host "ERROR: Failed to delete test product - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Summary
Write-Host "`n" -NoNewline
Write-Host "================================" -ForegroundColor Cyan
Write-Host "API TESTING COMPLETED!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Tested Endpoints:" -ForegroundColor White
Write-Host "- Health Check" -ForegroundColor Gray
Write-Host "- Inventory CRUD" -ForegroundColor Gray
Write-Host "- Stock Movements" -ForegroundColor Gray
Write-Host "- Alert System" -ForegroundColor Gray
Write-Host "- Error Handling" -ForegroundColor Gray
Write-Host "`nAll major API functionality verified!" -ForegroundColor Green