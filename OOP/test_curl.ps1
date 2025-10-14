# Simple curl-based API test
Write-Host "Testing APIs with curl..." -ForegroundColor Green

# Test POST - Create Product
Write-Host "`n1. Testing POST Create Product..." -ForegroundColor Yellow
$json = '{"name":"Curl Test Product","category":"electronics","price":299.99,"quantity":50,"minStockLevel":10,"supplier":"Curl Supplier"}'
$result = curl -X POST "http://localhost:8080/api/inventory" -H "Content-Type: application/json" -d $json
Write-Host "POST Result:" -ForegroundColor Gray
$result

# Test GET - List Products
Write-Host "`n2. Testing GET All Products..." -ForegroundColor Yellow
$getResult = curl "http://localhost:8080/api/inventory"
Write-Host "GET Result:" -ForegroundColor Gray
$getResult

# Test GET with Pagination
Write-Host "`n3. Testing GET with Pagination..." -ForegroundColor Yellow
$pagedResult = curl "http://localhost:8080/api/inventory?page=0&size=5"
Write-Host "Paged Result:" -ForegroundColor Gray
$pagedResult

Write-Host "`nCurl tests completed!" -ForegroundColor Green