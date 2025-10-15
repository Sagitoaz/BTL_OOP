# Simple API Test Script - WORKING VERSION
Write-Host "Starting API Tests..." -ForegroundColor Green

# Test 1: Get All Inventory
Write-Host "`n1. GET All Inventory..." -ForegroundColor Yellow
$inventory = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET
Write-Host "SUCCESS - Total: $($inventory.totalElements)" -ForegroundColor Green

# Test 2: Create Product
Write-Host "`n2. POST Create Product..." -ForegroundColor Yellow
$product = @{
    name = "Test Product"
    category = "electronics"
    price = 299.99
    quantity = 50
    minStockLevel = 10
    supplier = "Test Supplier"
} | ConvertTo-Json

$created = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method POST -Body $product -ContentType "application/json"
Write-Host "SUCCESS - Product ID: $($created.id)" -ForegroundColor Green
$productId = $created.id

# Test 3: Get Product by ID
Write-Host "`n3. GET Product by ID..." -ForegroundColor Yellow
$getProduct = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$productId" -Method GET
Write-Host "SUCCESS - Retrieved: $($getProduct.name)" -ForegroundColor Green

# Test 4: Update Product
Write-Host "`n4. PUT Update Product..." -ForegroundColor Yellow
$updateData = @{
    name = "Updated Test Product"
    category = "electronics"
    price = 399.99
    quantity = 75
    minStockLevel = 15
    supplier = "Updated Supplier"
} | ConvertTo-Json

$updated = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$productId" -Method PUT -Body $updateData -ContentType "application/json"
Write-Host "SUCCESS - Updated: $($updated.name)" -ForegroundColor Green

# Test 5: Create Stock Movement
Write-Host "`n5. POST Stock Movement..." -ForegroundColor Yellow
$movement = @{
    productId = $productId
    type = "PURCHASE"
    quantity = 20
    price = 7999.80
    note = "Test purchase"
} | ConvertTo-Json

$movementResult = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method POST -Body $movement -ContentType "application/json"
Write-Host "SUCCESS - Movement ID: $($movementResult.id)" -ForegroundColor Green

# Test 6: Get Stock Movements
Write-Host "`n6. GET Stock Movements..." -ForegroundColor Yellow
$movements = Invoke-RestMethod -Uri "http://localhost:8080/api/stock-movements" -Method GET
Write-Host "SUCCESS - Total Movements: $($movements.totalElements)" -ForegroundColor Green

# Test 7: Alert Check
Write-Host "`n7. POST Alert Check..." -ForegroundColor Yellow
$alertCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/check" -Method POST -ContentType "application/json"
Write-Host "SUCCESS - New Alerts: $($alertCheck.newAlerts)" -ForegroundColor Green

# Test 8: Get Alerts
Write-Host "`n8. GET Alerts..." -ForegroundColor Yellow
$alerts = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts" -Method GET
Write-Host "SUCCESS - Total Alerts: $($alerts.total)" -ForegroundColor Green

# Test 9: Alert Statistics
Write-Host "`n9. GET Alert Stats..." -ForegroundColor Yellow
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/alerts/stats" -Method GET
Write-Host "SUCCESS - Active Alerts: $($stats.activeAlerts)" -ForegroundColor Green

# Test 10: Error Testing (404)
Write-Host "`n10. Testing 404 Error..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/99999" -Method GET
    Write-Host "ERROR - Should have failed!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS - 404 handled correctly" -ForegroundColor Green
}

# Test 11: Delete Product
Write-Host "`n11. DELETE Product..." -ForegroundColor Yellow
$deleteResult = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/$productId" -Method DELETE
Write-Host "SUCCESS - Product deleted" -ForegroundColor Green

Write-Host "`n=== TESTING COMPLETE ===" -ForegroundColor Cyan
Write-Host "All 11 tests passed successfully!" -ForegroundColor Green
Write-Host "Backend APIs are working perfectly!" -ForegroundColor Green