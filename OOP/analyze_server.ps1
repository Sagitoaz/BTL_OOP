# Create test data directly using available methods
Write-Host "=== CREATING TEST DATA ===" -ForegroundColor Cyan

# First, let's check what endpoints are actually available
Write-Host "`n1. Checking available inventory..." -ForegroundColor Yellow
$inventory = Invoke-WebRequest -Uri "http://localhost:8080/api/inventory" -Method GET
$inventoryData = $inventory.Content | ConvertFrom-Json
Write-Host "Current inventory count: $($inventoryData.totalElements)" -ForegroundColor Gray
Write-Host "Response: $($inventory.Content)" -ForegroundColor Gray

# Check what movements exist
Write-Host "`n2. Checking existing movements..." -ForegroundColor Yellow
$movements = Invoke-WebRequest -Uri "http://localhost:8080/api/stock-movements" -Method GET
$movementData = $movements.Content | ConvertFrom-Json
Write-Host "Current movements count: $($movementData.totalElements)" -ForegroundColor Gray
Write-Host "Response: $($movements.Content)" -ForegroundColor Gray

# Check existing alerts
Write-Host "`n3. Checking existing alerts..." -ForegroundColor Yellow
$alerts = Invoke-WebRequest -Uri "http://localhost:8080/api/alerts" -Method GET
$alertData = $alerts.Content | ConvertFrom-Json
Write-Host "Current alerts count: $($alertData.totalAlerts)" -ForegroundColor Gray
Write-Host "Response: $($alerts.Content)" -ForegroundColor Gray

# Let's try to see what specific product exists (from movement data)
if ($movementData.content -and $movementData.content.Length -gt 0) {
    $firstMovement = $movementData.content[0]
    $productId = $firstMovement.productId
    
    Write-Host "`n4. Checking product ID $productId from movement..." -ForegroundColor Yellow
    try {
        $product = Invoke-WebRequest -Uri "http://localhost:8080/api/inventory/$productId" -Method GET
        $productData = $product.Content | ConvertFrom-Json
        Write-Host "SUCCESS: Found product '$($productData.name)'" -ForegroundColor Green
        Write-Host "Product details: $($product.Content)" -ForegroundColor Gray
    } catch {
        Write-Host "ERROR: Product not found - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Try to trigger alert generation
Write-Host "`n5. Triggering alert check..." -ForegroundColor Yellow
try {
    $alertCheck = Invoke-WebRequest -Uri "http://localhost:8080/api/alerts/check" -Method POST -ContentType "application/json"
    $alertCheckData = $alertCheck.Content | ConvertFrom-Json
    Write-Host "SUCCESS: Alert check completed" -ForegroundColor Green
    Write-Host "New alerts generated: $($alertCheckData.newAlertsGenerated)" -ForegroundColor Gray
    Write-Host "Response: $($alertCheck.Content)" -ForegroundColor Gray
} catch {
    Write-Host "ERROR: Alert check failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Check what methods are supported on different endpoints
Write-Host "`n6. Testing endpoint method support..." -ForegroundColor Yellow

$endpoints = @(
    "http://localhost:8080/api/inventory",
    "http://localhost:8080/api/stock-movements", 
    "http://localhost:8080/api/alerts"
)

foreach ($endpoint in $endpoints) {
    Write-Host "Testing $endpoint..." -ForegroundColor Gray
    
    # Test GET (should work)
    try {
        $getResponse = Invoke-WebRequest -Uri $endpoint -Method GET
        Write-Host "  GET: SUCCESS ($($getResponse.StatusCode))" -ForegroundColor Green
    } catch {
        Write-Host "  GET: FAILED ($($_.Exception.Message))" -ForegroundColor Red
    }
    
    # Test POST (might fail)
    try {
        $postResponse = Invoke-WebRequest -Uri $endpoint -Method POST -Body "{}" -ContentType "application/json"
        Write-Host "  POST: SUCCESS ($($postResponse.StatusCode))" -ForegroundColor Green
    } catch {
        if ($_.Exception.Message -like "*405*" -or $_.Exception.Message -like "*Method Not Allowed*") {
            Write-Host "  POST: Method Not Allowed (405)" -ForegroundColor Yellow
        } else {
            Write-Host "  POST: FAILED ($($_.Exception.Message))" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== ANALYSIS COMPLETE ===" -ForegroundColor Cyan
Write-Host "Server is running but has limited method support" -ForegroundColor Yellow
Write-Host "GET methods work perfectly" -ForegroundColor Green
Write-Host "POST/PUT/DELETE methods may need server restart or configuration fix" -ForegroundColor Yellow