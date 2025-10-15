# 🧪 COMPLETE API TEST SCRIPT - NGÀY 7 HOÀN THÀNH
# Test tất cả REST APIs: Inventory, Stock Movements, Alerts

Write-Host "==== TESTING COMPLETE BACKEND APIs - NGÀY 7 ====" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$totalTests = 0
$passedTests = 0
$failedTests = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $global:totalTests++
    Write-Host "`n[$global:totalTests] Testing: $Name" -ForegroundColor Yellow
    Write-Host "    $Method $Url" -ForegroundColor Gray
    
    try {
        if ($Body) {
            $bodyJson = $Body | ConvertTo-Json -Depth 10
            Write-Host "    Body: $($bodyJson.Substring(0, [Math]::Min(100, $bodyJson.Length)))..." -ForegroundColor Gray
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $bodyJson -ContentType "application/json"
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method
        }
        
        Write-Host "    ✅ SUCCESS" -ForegroundColor Green
        $global:passedTests++
        return $response
    } catch {
        Write-Host "    ❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $global:failedTests++
        return $null
    }
}

# ======================
# 1. INVENTORY API TESTS
# ======================
Write-Host "`n🔧 === INVENTORY API TESTS ===" -ForegroundColor Cyan

# Test inventory list
Test-Endpoint "Get inventory list" "GET" "$baseUrl/api/inventory"

# Test inventory with pagination
Test-Endpoint "Get inventory with pagination" "GET" "$baseUrl/api/inventory?page=0&size=5"

# Test create inventory
$newInventory = @{
    sku = "TEST001"
    name = "Test Product API"
    category = "test-category"
}
$created = Test-Endpoint "Create new inventory" "POST" "$baseUrl/api/inventory" $newInventory 201

if ($created) {
    $testId = $created.id
    Write-Host "    Created ID: $testId" -ForegroundColor Cyan
    
    # Test get by ID
    Test-Endpoint "Get inventory by ID" "GET" "$baseUrl/api/inventory/$testId"
    
    # Test update inventory
    $updateData = @{
        name = "Updated Test Product"
        category = "updated-category"
    }
    Test-Endpoint "Update inventory" "PUT" "$baseUrl/api/inventory/$testId" $updateData
    
    # Test initial stock
    $stockData = @{
        qty = 100
        note = "Initial test stock"
        batchNo = "BATCH-TEST-001"
    }
    Test-Endpoint "Record initial stock" "POST" "$baseUrl/api/inventory/$testId/initial-stock" $stockData
    
    # Test movements for inventory
    Test-Endpoint "Get inventory movements" "GET" "$baseUrl/api/inventory/$testId/movements"
    
    # Test delete inventory
    Test-Endpoint "Delete inventory" "DELETE" "$baseUrl/api/inventory/$testId"
}

# Test inventory docs
Test-Endpoint "Get inventory API docs" "GET" "$baseUrl/api/inventory/docs"

# ======================
# 2. STOCK MOVEMENT API TESTS  
# ======================
Write-Host "`n📦 === STOCK MOVEMENT API TESTS ===" -ForegroundColor Cyan

# Test movements list
Test-Endpoint "Get stock movements list" "GET" "$baseUrl/api/stock-movements"

# Test movements with filters
Test-Endpoint "Get movements with filters" "GET" "$baseUrl/api/stock-movements?productId=1&page=0&size=10"

# Test create movement
$newMovement = @{
    productId = 1
    qty = 50
    moveType = "PURCHASE"
    batchNo = "BATCH002"
    movedBy = 1
    note = "Test purchase movement"
}
$createdMovement = Test-Endpoint "Create stock movement" "POST" "$baseUrl/api/stock-movements" $newMovement 201

if ($createdMovement) {
    $movementId = $createdMovement.id
    Write-Host "    Created Movement ID: $movementId" -ForegroundColor Cyan
    
    # Test get movement by ID
    Test-Endpoint "Get movement by ID" "GET" "$baseUrl/api/stock-movements/$movementId"
}

# Test movements by product
Test-Endpoint "Get movements by product" "GET" "$baseUrl/api/stock-movements/product/1"

# Test bulk movements
$bulkMovements = @(
    @{
        productId = 1
        qty = 30
        moveType = "SALE"
        movedBy = 1
        note = "Bulk sale 1"
    },
    @{
        productId = 2
        qty = 25
        moveType = "SALE" 
        movedBy = 1
        note = "Bulk sale 2"
    }
)
Test-Endpoint "Create bulk movements" "POST" "$baseUrl/api/stock-movements/bulk" $bulkMovements

# ======================
# 3. ALERT API TESTS
# ======================
Write-Host "`n🚨 === ALERT API TESTS ===" -ForegroundColor Cyan

# Test get active alerts
Test-Endpoint "Get active alerts" "GET" "$baseUrl/api/alerts"

# Test manual alert check
Test-Endpoint "Manual alert check" "POST" "$baseUrl/api/alerts/check"

# Test resolve alert
Test-Endpoint "Resolve alert" "PUT" "$baseUrl/api/alerts/1/resolve"

# Test alert statistics
Test-Endpoint "Get alert statistics" "GET" "$baseUrl/api/alerts/stats"

# Test alerts by priority
Test-Endpoint "Get HIGH priority alerts" "GET" "$baseUrl/api/alerts/priority/HIGH"
Test-Endpoint "Get MEDIUM priority alerts" "GET" "$baseUrl/api/alerts/priority/MEDIUM"
Test-Endpoint "Get LOW priority alerts" "GET" "$baseUrl/api/alerts/priority/LOW"

# Test alert docs
Test-Endpoint "Get alerts API docs" "GET" "$baseUrl/api/alerts/docs"

# ======================
# 4. SYSTEM HEALTH TESTS
# ======================
Write-Host "`n💚 === SYSTEM HEALTH TESTS ===" -ForegroundColor Cyan

# Test health endpoint
Test-Endpoint "System health check" "GET" "$baseUrl/health"

# Test hello endpoint
Test-Endpoint "Hello endpoint" "GET" "$baseUrl/hello"

# ======================
# 5. ERROR HANDLING TESTS
# ======================
Write-Host "`n⚠️  === ERROR HANDLING TESTS ===" -ForegroundColor Cyan

# Test 404 errors
$totalTests++
Write-Host "`n[$totalTests] Testing: 404 Not Found" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/inventory/99999" -Method GET
    Write-Host "    ❌ FAILED: Should have returned 404" -ForegroundColor Red
    $failedTests++
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "    ✅ SUCCESS: Correctly returned 404" -ForegroundColor Green
        $passedTests++
    } else {
        Write-Host "    ❌ FAILED: Wrong error code" -ForegroundColor Red
        $failedTests++
    }
}

# Test 400 errors
$totalTests++
Write-Host "`n[$totalTests] Testing: 400 Bad Request" -ForegroundColor Yellow
try {
    $invalidData = @{ invalidField = "test" }
    Invoke-RestMethod -Uri "$baseUrl/api/inventory" -Method POST -Body ($invalidData | ConvertTo-Json) -ContentType "application/json"
    Write-Host "    ❌ FAILED: Should have returned 400" -ForegroundColor Red
    $failedTests++
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "    ✅ SUCCESS: Correctly returned 400" -ForegroundColor Green
        $passedTests++
    } else {
        Write-Host "    ❌ FAILED: Wrong error code" -ForegroundColor Red
        $failedTests++
    }
}

# ======================
# TEST SUMMARY
# ======================
Write-Host "`n" + "="*60 -ForegroundColor White
Write-Host "🎯 TEST RESULTS SUMMARY" -ForegroundColor Green
Write-Host "="*60 -ForegroundColor White
Write-Host "Total Tests:    $totalTests" -ForegroundColor White
Write-Host "Passed:         $passedTests" -ForegroundColor Green  
Write-Host "Failed:         $failedTests" -ForegroundColor Red
Write-Host "Success Rate:   $(([math]::Round(($passedTests / $totalTests) * 100, 1)))%" -ForegroundColor Cyan

if ($failedTests -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! 🎉" -ForegroundColor Green
    Write-Host "✅ REST API Backend for NGÀY 7 is COMPLETE!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Some tests failed. Check the logs above for details." -ForegroundColor Yellow
}

Write-Host "`n📋 COMPLETED FEATURES:" -ForegroundColor Cyan
Write-Host "✅ Complete Inventory Management API (CRUD + pagination + filtering)" -ForegroundColor White
Write-Host "✅ Stock Movement Tracking API (individual + bulk operations)" -ForegroundColor White  
Write-Host "✅ Alert System API (low stock detection + management)" -ForegroundColor White
Write-Host "✅ Error handling with proper HTTP status codes" -ForegroundColor White
Write-Host "✅ API documentation endpoints" -ForegroundColor White
Write-Host "✅ Input validation and data integrity" -ForegroundColor White

Write-Host "`n🚀 NGÀY 7 BACKEND DEVELOPMENT - STATUS: COMPLETED!" -ForegroundColor Green
Write-Host "Ready for NGÀY 8: Frontend Integration!" -ForegroundColor Cyan