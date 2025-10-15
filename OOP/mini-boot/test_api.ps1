# Test script cho Inventory API
Write-Host "=== TESTING INVENTORY API ===" -ForegroundColor Green

# Test 1: Basic endpoint
Write-Host "`n1. Testing basic inventory endpoint..." -ForegroundColor Yellow
try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory" -Method GET
    Write-Host "✓ SUCCESS: Basic endpoint works" -ForegroundColor Green
    Write-Host "Message: $($response1.message)" -ForegroundColor Cyan
    Write-Host "Available endpoints:" -ForegroundColor Cyan
    $response1.endpoints | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: With query parameters  
Write-Host "`n2. Testing with query parameters..." -ForegroundColor Yellow
try {
    $uri2 = "http://localhost:8080/api/inventory?page=0`&size=10`&category=test"
    $response2 = Invoke-RestMethod -Uri $uri2 -Method GET
    Write-Host "✓ SUCCESS: Query parameters work" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Health check
Write-Host "`n3. Testing health endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/health" -Method GET
    Write-Host "✓ SUCCESS: Health check works" -ForegroundColor Green
    Write-Host "Status: $($health.status), Uptime: $($health.uptimeMs)ms" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get detailed response info
Write-Host "`n4. Testing response details..." -ForegroundColor Yellow
try {
    $webResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/inventory" -Method GET
    Write-Host "✓ SUCCESS: Detailed response" -ForegroundColor Green
    Write-Host "Status Code: $($webResponse.StatusCode)" -ForegroundColor Cyan
    Write-Host "Content Type: $($webResponse.Headers.'Content-Type')" -ForegroundColor Cyan
    Write-Host "Content Length: $($webResponse.Headers.'Content-Length')" -ForegroundColor Cyan
    Write-Host "CORS Headers: $($webResponse.Headers.'Access-Control-Allow-Origin')" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST COMPLETED ===" -ForegroundColor Green
Write-Host "`nTo run this script again, use: .\test_api.ps1" -ForegroundColor White