# 📋 KẾ HOẠCH TRIỂN KHAI XỬ LÝ LỖI THEO SEQUENCE DIAGRAM

**Ngày tạo:** 8 tháng 11, 2025  
**Branch:** OOP-49  
**Mục tiêu:** Đảm bảo backend và frontend xử lý đầy đủ tất cả các mã lỗi như được định nghĩa trong PlantUML Sequence Diagrams

---

## 🎯 TỔNG QUAN

Sau khi phân tích toàn bộ dự án, tôi nhận thấy:

### ✅ **Đã có sẵn:**
1. **ErrorHandler framework** trong `oop_ui` với mapping đầy đủ HTTP status codes
2. **ApiClient** với xử lý async/sync và error callbacks
3. **ApiResponse wrapper** với type safety
4. **Repository pattern** trong backend với exception handling cơ bản

### ❌ **Còn thiếu:**
1. **Backend controllers** chưa xử lý đầy đủ các trường hợp lỗi như sequence diagram
2. Thiếu **validation layers** cho business rules (422 errors)
3. Thiếu **conflict detection** (409 errors)
4. Thiếu **timeout handling** và **database error mapping** (503, 504, 500)
5. **Frontend controllers** chưa handle hết các error cases
6. Thiếu **Content-Type validation** (415 errors)
7. Chưa có **authentication/authorization checks** (401, 403)

---

## 📊 PHÂN TÍCH SEQUENCE DIAGRAMS

### 1️⃣ **INVENTORY - ADD (AddInventory.puml)**

#### Các mã lỗi cần xử lý:
- ✅ **415 Unsupported Media Type** - Kiểm tra Content-Type
- ✅ **401 Unauthorized** - Xác thực JWT
- ✅ **403 Forbidden** - Kiểm tra quyền ADMIN
- ❌ **400 Bad Request** - Thiếu trường bắt buộc, sai kiểu dữ liệu
- ❌ **422 Unprocessable Entity** - Vi phạm business rules (qty ≥ 0, price ≥ 0)
- ❌ **409 Conflict** - SKU đã tồn tại
- ❌ **503 Service Unavailable** - Database down
- ❌ **504 Gateway Timeout** - Database timeout
- ❌ **500 Internal Server Error** - Database constraint violations
- ✅ **201 Created** - Thành công

#### Vị trí code cần sửa:
**Backend:** `mini-boot/src/main/java/org/miniboot/app/controllers/Inventory/InventoryController.java`

```java
public Function<HttpRequest, HttpResponse> createProduct() {
    return (HttpRequest req) -> {
        // ❌ THIẾU: Content-Type validation
        // ❌ THIẾU: JWT authentication check
        // ❌ THIẾU: Role authorization check (ADMIN only)
        // ❌ THIẾU: Field validation (required fields, data types)
        // ❌ THIẾU: Business rule validation (qty ≥ 0, price ≥ 0)
        // ❌ THIẾU: SKU conflict detection
        // ❌ THIẾU: Database error handling (timeout, connection, constraint)
        
        try {
            Product product = Json.fromBytes(req.body, Product.class);
            Product saved = productRepo.save(product);
            if (saved == null) {
                return HttpResponse.of(500, "text/plain",
                    "Failed to save product to database".getBytes(StandardCharsets.UTF_8));
            }
            return Json.created(saved);
        } catch (Exception e) {
            // ⚠️ CHƯA ĐỦ: Chỉ xử lý chung chung, không phân loại lỗi
            return HttpResponse.of(400, "text/plain",
                ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    };
}
```

---

### 2️⃣ **INVENTORY - UPDATE/DELETE (EditAndDeleteInventory.puml)**

#### Các mã lỗi cần xử lý:
- ✅ **415 Unsupported Media Type**
- ✅ **401 Unauthorized**
- ✅ **403 Forbidden**
- ❌ **400 Bad Request** - Payload sai
- ❌ **404 Not Found** - Product không tồn tại
- ❌ **422 Unprocessable Entity** - Business rules
- ❌ **412 Precondition Failed** - Version conflict (optimistic locking)
- ❌ **409 Conflict** - SKU mới trùng với SKU khác
- ❌ **503/504/500** - Database errors
- ✅ **200 OK** - Thành công

#### Vị trí code cần sửa:
**Backend:** `InventoryController.updateProduct()` và `deleteProduct()`

```java
public Function<HttpRequest, HttpResponse> updateProduct() {
    return (HttpRequest req) -> {
        // ❌ THIẾU: Tất cả validations như createProduct
        // ❌ THIẾU: Check product existence (404)
        // ❌ THIẾU: Version/ETag validation (412)
        // ❌ THIẾU: SKU conflict check với products khác
        
        try {
            Product product = Json.fromBytes(req.body, Product.class);
            if (product.getId() <= 0) {
                return HttpResponse.of(400, "text/plain",
                    "Missing product ID".getBytes(StandardCharsets.UTF_8));
            }
            Product updated = productRepo.save(product);
            return Json.ok(updated);
        } catch (Exception e) {
            return HttpResponse.of(400, "text/plain",
                ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    };
}
```

---

### 3️⃣ **INVENTORY - SEARCH/VIEW (SearchAndViewInventory.puml)**

#### Các mã lỗi cần xử lý:
- ✅ **415 Unsupported Media Type**
- ✅ **401 Unauthorized**
- ✅ **403 Forbidden**
- ❌ **400 Bad Request** - Missing keyword/params
- ❌ **422 Validation Error** - Keyword quá ngắn, ký tự cấm
- ❌ **404 Not Found** - Product by ID không tìm thấy
- ❌ **503/504/500** - Database errors
- ✅ **200 OK** - Success (có thể empty list)

#### Vị trí code cần sửa:
**Backend:** `InventoryController.searchProductBySku()` và `getProducts()`

```java
public Function<HttpRequest, HttpResponse> searchProductBySku() {
    return (HttpRequest req) -> {
        // ❌ THIẾU: Keyword validation (min length, forbidden chars)
        // ❌ THIẾU: Database timeout handling
        
        try {
            Map<String, List<String>> q = req.query;
            Optional<String> skuOpt = ExtractHelper.extractString(q, "sku");
            
            if (skuOpt.isEmpty()) {
                return HttpResponse.of(400, "text/plain",
                    "Missing sku parameter".getBytes(StandardCharsets.UTF_8));
            }
            
            // ⚠️ CHƯA ĐỦ: Không validate keyword format
            Optional<Product> productOpt = productRepo.findBySku(skuOpt.get());
            
            if (productOpt.isPresent()) {
                return Json.ok(productOpt.get());
            } else {
                return HttpResponse.of(404, "text/plain",
                    "Product not found".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // ⚠️ CHƯA ĐỦ: Không phân biệt timeout vs DB error
            return HttpResponse.of(500, "text/plain",
                ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    };
}
```

---

### 4️⃣ **PATIENT - CREATE/UPDATE (UpdatePatient.puml)**

#### Các mã lỗi cần xử lý:
- ❌ **400 Bad Request** - Thiếu/sai trường bắt buộc
- ❌ **401 Unauthorized** - Token hết hạn
- ❌ **403 Forbidden** - Không có quyền tạo/update patient
- ❌ **404 Not Found** - Patient không tồn tại (update case)
- ❌ **409 Conflict** - CMND/Email/Số bảo hiểm trùng
- ❌ **422 Unprocessable Entity** - Bảo hiểm hết hạn, ngày sinh không hợp lệ
- ❌ **429 Too Many Requests** - Rate limiting
- ❌ **503/504** - Service/Database unavailable
- ✅ **200 OK / 201 Created** - Success

#### Vị trí code cần sửa:
**Backend:** `mini-boot/src/main/java/org/miniboot/app/controllers/PatientAndPrescription/CustomerRecordController.java`

```java
public Function<HttpRequest, HttpResponse> createCustomer() {
    return (HttpRequest req) -> {
        // ❌ THIẾU: Tất cả validations
        // ❌ THIẾU: Check duplicate CMND/Email/Phone
        // ❌ THIẾU: Validate DOB (không được tương lai, hợp lý về tuổi)
        
        try {
            Gson gson = GsonProvider.getGson();
            String jsonBody = new String(req.body, StandardCharsets.UTF_8);
            Customer customerToCreate = gson.fromJson(jsonBody, Customer.class);
            
            Customer savedCustomer = customerRecordRepository.save(customerToCreate);
            
            if (savedCustomer != null && savedCustomer.getId() > 0) {
                String jsonResponse = gson.toJson(savedCustomer);
                return HttpResponse.of(201, "application/json", 
                    jsonResponse.getBytes(StandardCharsets.UTF_8));
            } else {
                return HttpResponse.of(500, "text/plain; charset=utf-8",
                    "Internal Server Error: Failed to create customer".getBytes(StandardCharsets.UTF_8));
            }
        } catch (RuntimeException e) {
            // ⚠️ CHƯA ĐỦ: Chỉ catch RuntimeException, không phân loại
            return HttpResponse.of(500, "text/plain; charset=utf-8",
                ("Database Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return HttpResponse.of(400, "text/plain; charset=utf-8",
                AppConfig.RESPONSE_400.getBytes(StandardCharsets.UTF_8));
        }
    };
}
```

---

### 5️⃣ **PRESCRIPTION - CREATE/UPDATE**

#### Vị trí code cần sửa:
**Backend:** `mini-boot/src/main/java/org/miniboot/app/controllers/PatientAndPrescription/PrescriptionController.java`

Tương tự như Customer, thiếu tất cả validations.

---

## 🛠️ HƯỚNG DẪN TRIỂN KHAI CHI TIẾT

### **BƯỚC 1: Tạo Validation Utilities**

#### File: `mini-boot/src/main/java/org/miniboot/app/util/ValidationUtils.java`

```java
package org.miniboot.app.util;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

public class ValidationUtils {
    
    /**
     * Validate Content-Type header
     * @return HttpResponse with 415 if invalid, null if valid
     */
    public static HttpResponse validateContentType(HttpRequest req, String expectedType) {
        Map<String, List<String>> headers = req.headers;
        List<String> contentTypes = headers.get("Content-Type");
        
        if (contentTypes == null || contentTypes.isEmpty()) {
            return error(415, "UNSUPPORTED_MEDIA_TYPE", 
                "Content-Type header is required");
        }
        
        String contentType = contentTypes.get(0).toLowerCase();
        if (!contentType.contains(expectedType.toLowerCase())) {
            return error(415, "UNSUPPORTED_MEDIA_TYPE", 
                "Expected Content-Type: " + expectedType + ", got: " + contentType);
        }
        
        return null; // Valid
    }
    
    /**
     * Validate JWT token (placeholder - implement với JWT library)
     * @return HttpResponse with 401 if invalid, null if valid
     */
    public static HttpResponse validateJWT(HttpRequest req) {
        // TODO: Implement JWT validation
        // For now, check Authorization header exists
        Map<String, List<String>> headers = req.headers;
        List<String> authHeaders = headers.get("Authorization");
        
        if (authHeaders == null || authHeaders.isEmpty()) {
            return error(401, "UNAUTHORIZED", 
                "Authorization header is required");
        }
        
        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            return error(401, "UNAUTHORIZED", 
                "Invalid Authorization format. Expected: Bearer <token>");
        }
        
        // TODO: Validate token signature, expiration
        return null; // Valid for now
    }
    
    /**
     * Validate user role (placeholder)
     * @return HttpResponse with 403 if forbidden, null if allowed
     */
    public static HttpResponse validateRole(HttpRequest req, String requiredRole) {
        // TODO: Extract role from JWT token
        // For now, always allow (implement sau khi có JWT)
        return null;
    }
    
    /**
     * Validate required fields
     * @return HttpResponse with 400 if invalid, null if valid
     */
    public static HttpResponse validateRequiredFields(Map<String, Object> data, String... requiredFields) {
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null || 
                data.get(field).toString().trim().isEmpty()) {
                return error(400, "BAD_REQUEST", 
                    "Required field '" + field + "' is missing or empty");
            }
        }
        return null;
    }
    
    /**
     * Validate business rules for Product
     */
    public static HttpResponse validateProductBusinessRules(
        int qtyOnHand, Integer priceCost, Integer priceRetail) {
        
        if (qtyOnHand < 0) {
            return error(422, "VALIDATION_FAILED", 
                "Quantity on hand cannot be negative");
        }
        
        if (priceCost != null && priceCost < 0) {
            return error(422, "VALIDATION_FAILED", 
                "Price cost cannot be negative");
        }
        
        if (priceRetail != null && priceRetail < 0) {
            return error(422, "VALIDATION_FAILED", 
                "Price retail cannot be negative");
        }
        
        if (priceCost != null && priceRetail != null && priceRetail < priceCost) {
            return error(422, "VALIDATION_FAILED", 
                "Price retail should be greater than or equal to price cost");
        }
        
        return null; // Valid
    }
    
    /**
     * Validate search keyword
     */
    public static HttpResponse validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return error(400, "BAD_REQUEST", 
                "Search keyword is required");
        }
        
        if (keyword.trim().length() < 2) {
            return error(422, "VALIDATION_ERROR", 
                "Search keyword must be at least 2 characters");
        }
        
        // Check forbidden characters (SQL injection prevention)
        if (keyword.matches(".*[';\"\\\\].*")) {
            return error(422, "VALIDATION_ERROR", 
                "Search keyword contains forbidden characters");
        }
        
        return null; // Valid
    }
    
    /**
     * Helper: Create error response
     */
    private static HttpResponse error(int status, String errorCode, String message) {
        String json = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
            errorCode, message);
        return HttpResponse.of(status, "application/json", 
            json.getBytes(StandardCharsets.UTF_8));
    }
}
```

---

### **BƯỚC 2: Tạo Database Error Handler**

#### File: `mini-boot/src/main/java/org/miniboot/app/util/DatabaseErrorHandler.java`

```java
package org.miniboot.app.util;

import org.miniboot.app.http.HttpResponse;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.nio.charset.StandardCharsets;

public class DatabaseErrorHandler {
    
    /**
     * Map SQLException to appropriate HTTP response
     */
    public static HttpResponse handleDatabaseException(Exception e) {
        if (e instanceof SQLTimeoutException) {
            return error(504, "TIMEOUT", 
                "Database query timeout. Please try again.");
        }
        
        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            String sqlState = sqlEx.getSQLState();
            
            // PostgreSQL error codes
            // 23505 = Unique violation
            if ("23505".equals(sqlState)) {
                return error(409, "CONFLICT", 
                    "Record already exists (duplicate key)");
            }
            
            // 23503 = Foreign key violation
            if ("23503".equals(sqlState)) {
                return error(422, "VALIDATION_FAILED", 
                    "Cannot delete: record is referenced by other data");
            }
            
            // Connection errors
            if (sqlState != null && sqlState.startsWith("08")) {
                return error(503, "SERVICE_UNAVAILABLE", 
                    "Database connection error. Please try again later.");
            }
            
            // Deadlock
            if ("40P01".equals(sqlState)) {
                return error(500, "DB_ERROR", 
                    "Database deadlock detected. Please retry.");
            }
        }
        
        // Generic database error
        return error(500, "DB_ERROR", 
            "Database error: " + e.getMessage());
    }
    
    /**
     * Check if error is retryable (for client-side retry logic)
     */
    public static boolean isRetryable(Exception e) {
        if (e instanceof SQLTimeoutException) {
            return true;
        }
        
        if (e instanceof SQLException) {
            String sqlState = ((SQLException) e).getSQLState();
            // Connection errors and deadlocks are retryable
            return (sqlState != null && sqlState.startsWith("08")) || 
                   "40P01".equals(sqlState);
        }
        
        return false;
    }
    
    private static HttpResponse error(int status, String errorCode, String message) {
        String json = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
            errorCode, message);
        return HttpResponse.of(status, "application/json", 
            json.getBytes(StandardCharsets.UTF_8));
    }
}
```

---

### **BƯỚC 3: Update InventoryController với đầy đủ error handling**

#### File: `mini-boot/src/main/java/org/miniboot/app/controllers/Inventory/InventoryController.java`

```java
public Function<HttpRequest, HttpResponse> createProduct() {
    return (HttpRequest req) -> {
        // STEP 1: Validate Content-Type (415)
        HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
        if (contentTypeError != null) return contentTypeError;
        
        // STEP 2: Validate JWT (401)
        HttpResponse authError = ValidationUtils.validateJWT(req);
        if (authError != null) return authError;
        
        // STEP 3: Validate Role - ADMIN only (403)
        HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
        if (roleError != null) return roleError;
        
        try {
            // STEP 4: Parse JSON (400)
            Product product;
            try {
                product = Json.fromBytes(req.body, Product.class);
            } catch (Exception e) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "Invalid JSON format: " + e.getMessage());
            }
            
            // STEP 5: Validate required fields (400)
            if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "SKU is required");
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "Name is required");
            }
            
            // STEP 6: Validate business rules (422)
            HttpResponse businessRuleError = ValidationUtils.validateProductBusinessRules(
                product.getQtyOnHand(), 
                product.getPriceCost(), 
                product.getPriceRetail()
            );
            if (businessRuleError != null) return businessRuleError;
            
            // STEP 7: Check SKU conflict (409)
            try {
                Optional<Product> existing = productRepo.findBySku(product.getSku());
                if (existing.isPresent()) {
                    return ValidationUtils.error(409, "INVENTORY_CONFLICT", 
                        "Product with SKU '" + product.getSku() + "' already exists");
                }
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            
            // STEP 8: Save to database (503/504/500)
            Product saved;
            try {
                saved = productRepo.save(product);
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            
            if (saved == null) {
                return ValidationUtils.error(500, "DB_ERROR", 
                    "Failed to save product to database");
            }
            
            // STEP 9: Success (201)
            return Json.created(saved);
            
        } catch (Exception e) {
            // STEP 10: Catch-all for unexpected errors (500)
            System.err.println("❌ Unexpected error in createProduct: " + e.getMessage());
            e.printStackTrace();
            return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred");
        }
    };
}
```

Tương tự cho `updateProduct()`:

```java
public Function<HttpRequest, HttpResponse> updateProduct() {
    return (HttpRequest req) -> {
        // Validations giống createProduct
        HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
        if (contentTypeError != null) return contentTypeError;
        
        HttpResponse authError = ValidationUtils.validateJWT(req);
        if (authError != null) return authError;
        
        HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
        if (roleError != null) return roleError;
        
        try {
            Product product = Json.fromBytes(req.body, Product.class);
            
            // Validate ID (400)
            if (product.getId() <= 0) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "Product ID is required for update");
            }
            
            // Check existence (404)
            Optional<Product> existing;
            try {
                existing = productRepo.findById(product.getId());
                if (existing.isEmpty()) {
                    return ValidationUtils.error(404, "NOT_FOUND", 
                        "Product with ID " + product.getId() + " not found");
                }
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            
            // Validate business rules
            HttpResponse businessRuleError = ValidationUtils.validateProductBusinessRules(
                product.getQtyOnHand(), 
                product.getPriceCost(), 
                product.getPriceRetail()
            );
            if (businessRuleError != null) return businessRuleError;
            
            // Check SKU conflict with OTHER products (409)
            if (product.getSku() != null && 
                !product.getSku().equals(existing.get().getSku())) {
                try {
                    Optional<Product> skuConflict = productRepo.findBySku(product.getSku());
                    if (skuConflict.isPresent() && 
                        skuConflict.get().getId() != product.getId()) {
                        return ValidationUtils.error(409, "SKU_CONFLICT", 
                            "SKU '" + product.getSku() + "' is already used by another product");
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }
            }
            
            // TODO: Implement version/ETag check for optimistic locking (412)
            
            // Update
            Product updated;
            try {
                updated = productRepo.save(product);
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            
            if (updated == null) {
                return ValidationUtils.error(500, "DB_ERROR", 
                    "Failed to update product");
            }
            
            return Json.ok(updated);
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in updateProduct: " + e.getMessage());
            e.printStackTrace();
            return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred");
        }
    };
}
```

---

### **BƯỚC 4: Update CustomerRecordController**

Áp dụng tương tự cho `createCustomer()` và `updateCustomer()`:

```java
public Function<HttpRequest, HttpResponse> createCustomer() {
    return (HttpRequest req) -> {
        // Validations
        HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
        if (contentTypeError != null) return contentTypeError;
        
        HttpResponse authError = ValidationUtils.validateJWT(req);
        if (authError != null) return authError;
        
        HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
        if (roleError != null) return roleError;
        
        try {
            Gson gson = GsonProvider.getGson();
            String jsonBody = new String(req.body, StandardCharsets.UTF_8);
            Customer customerToCreate = gson.fromJson(jsonBody, Customer.class);
            
            // Validate required fields (400)
            if (customerToCreate.getFirstname() == null || 
                customerToCreate.getFirstname().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "First name is required");
            }
            if (customerToCreate.getLastname() == null || 
                customerToCreate.getLastname().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "Last name is required");
            }
            if (customerToCreate.getPhone() == null || 
                customerToCreate.getPhone().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", 
                    "Phone is required");
            }
            
            // Validate business rules (422)
            if (customerToCreate.getDob() != null && 
                customerToCreate.getDob().isAfter(LocalDate.now())) {
                return ValidationUtils.error(422, "VALIDATION_FAILED", 
                    "Date of birth cannot be in the future");
            }
            
            if (customerToCreate.getDob() != null && 
                customerToCreate.getDob().isBefore(LocalDate.now().minusYears(150))) {
                return ValidationUtils.error(422, "VALIDATION_FAILED", 
                    "Invalid date of birth");
            }
            
            // Check duplicates (409)
            // TODO: Implement findByPhone, findByEmail in repository
            // if (phone exists) return 409 CONFLICT
            // if (email exists) return 409 CONFLICT
            
            // Save
            Customer savedCustomer;
            try {
                savedCustomer = customerRecordRepository.save(customerToCreate);
            } catch (Exception e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            
            if (savedCustomer != null && savedCustomer.getId() > 0) {
                String jsonResponse = gson.toJson(savedCustomer);
                return HttpResponse.of(201, "application/json", 
                    jsonResponse.getBytes(StandardCharsets.UTF_8));
            } else {
                return ValidationUtils.error(500, "DB_ERROR", 
                    "Failed to create customer");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in createCustomer: " + e.getMessage());
            e.printStackTrace();
            return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred");
        }
    };
}
```

---

### **BƯỚC 5: Update PrescriptionController**

Tương tự như trên, thêm đầy đủ validations.

---

### **BƯỚC 6: Update Repository Layer**

#### File: `mini-boot/src/main/java/org/miniboot/app/domain/repo/PatientAndPrescription/CustomerRecordRepository.java`

Thêm methods để check duplicates:

```java
public interface CustomerRecordRepository {
    Customer save(Customer customer);
    void saveAll(List<Customer> customers);
    List<Customer> findAll();
    List<Customer> findByFilterAll(CustomerSearchCriteria criteria);
    boolean deleteById(int id);
    boolean existsById(int id);
    long count();
    
    // ✅ Thêm mới
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
}
```

#### File: `PostgreSQLCustomerRecordRepository.java`

```java
@Override
public Optional<Customer> findByPhone(String phone) {
    String sqlQuery = "SELECT * FROM customers WHERE phone = ? LIMIT 1;";
    try (Connection conn = dbConfig.getConnection()){
        PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
        pstmt.setString(1, phone);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
        }
    } catch (Exception e){
        System.err.println("❌ Error finding customer by phone: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Database find failed: " + e.getMessage(), e);
    }
    return Optional.empty();
}

@Override
public Optional<Customer> findByEmail(String email) {
    String sqlQuery = "SELECT * FROM customers WHERE email = ? LIMIT 1;";
    try (Connection conn = dbConfig.getConnection()){
        PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
        }
    } catch (Exception e){
        System.err.println("❌ Error finding customer by email: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Database find failed: " + e.getMessage(), e);
    }
    return Optional.empty();
}
```

---

### **BƯỚC 7: Update Frontend Controllers**

#### File: `oop_ui/src/main/java/org/example/oop/Control/Inventory/ProductCRUDController.java`

Trong các async callbacks, handle đầy đủ các error cases:

```java
private void createProductAsync() {
    Product newProduct = getFormData();
    showLoading(true);
    disableButtons(true);
    updateStatus("🔄 Đang tạo sản phẩm mới...");

    executeAsync(
        // Background: POST request
        () -> {
            try {
                return productService.createProduct(newProduct);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },

        // Success: Add to table
        created -> {
            if (created != null) {
                productList.add(created);
                clearForm();
                productTable.getSelectionModel().select(created);
                productTable.scrollTo(created);
                showSuccess("Đã tạo sản phẩm mới: " + created.getName());
                updateStatus("✅ Đã tạo sản phẩm ID: " + created.getId());
            }
            showLoading(false);
            disableButtons(false);
        },

        // Error - IMPROVE THIS PART
        error -> {
            showLoading(false);
            disableButtons(false);
            
            // ✅ Parse error response để hiển thị message phù hợp
            String errorMsg = parseErrorMessage(error.getMessage());
            
            if (error.getMessage().contains("409") || 
                error.getMessage().contains("CONFLICT")) {
                showError("❌ SKU đã tồn tại\n\nSản phẩm với SKU này đã có trong hệ thống. Vui lòng sử dụng SKU khác.");
            } else if (error.getMessage().contains("422") || 
                       error.getMessage().contains("VALIDATION")) {
                showError("❌ Dữ liệu không hợp lệ\n\n" + errorMsg);
            } else if (error.getMessage().contains("503")) {
                showError("❌ Máy chủ đang bảo trì\n\nHệ thống tạm thời không khả dụng. Vui lòng thử lại sau 5 phút.");
            } else if (error.getMessage().contains("504") || 
                       error.getMessage().contains("timeout")) {
                showError("❌ Hết thời gian chờ\n\nKết nối quá chậm. Vui lòng kiểm tra mạng và thử lại.");
            } else {
                showError("❌ Không thể tạo sản phẩm\n\n" + errorMsg);
            }
            
            updateStatus("❌ Lỗi: " + errorMsg);
        }
    );
}

// Helper method
private String parseErrorMessage(String rawError) {
    // Parse JSON error response nếu có
    try {
        if (rawError.contains("{") && rawError.contains("message")) {
            // Extract "message" field from JSON
            int start = rawError.indexOf("\"message\":\"") + 11;
            int end = rawError.indexOf("\"", start);
            if (start > 0 && end > start) {
                return rawError.substring(start, end);
            }
        }
    } catch (Exception e) {
        // Ignore parse error
    }
    return rawError;
}
```

Tương tự cho `updateProductAsync()` và các methods khác.

---

### **BƯỚC 8: Update Service Layer trong Frontend**

#### File: `oop_ui/src/main/java/org/example/oop/Service/ApiProductService.java`

Improve error handling trong `createProduct()`:

```java
public Product createProduct(Product product) throws Exception {
    System.out.println("🔄 Creating product: " + product.getName());
    HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + "/products").toURL().openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    conn.setConnectTimeout(CONNECT_TIMEOUT);
    conn.setReadTimeout(READ_TIMEOUT);
    conn.setDoOutput(true);

    String jsonBody = gson.toJson(product);
    System.out.println("📤 Sending JSON: " + jsonBody);

    try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
    }

    int responseCode = conn.getResponseCode();
    String responseBody = readResponse(conn);

    System.out.println("📥 Response Code: " + responseCode);
    System.out.println("📥 Response Body: " + responseBody);

    // ✅ IMPROVE: Handle specific error codes
    switch (responseCode) {
        case 201: // Created
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new Exception("Server returned empty response");
            }
            Product created = gson.fromJson(responseBody, Product.class);
            if (created == null) {
                throw new Exception("Failed to parse server response");
            }
            System.out.println("✅ Product created with ID: " + created.getId());
            return created;
            
        case 400: // Bad Request
            throw new Exception("400 BAD_REQUEST: " + responseBody);
            
        case 409: // Conflict
            throw new Exception("409 CONFLICT: " + responseBody);
            
        case 422: // Validation Failed
            throw new Exception("422 VALIDATION_FAILED: " + responseBody);
            
        case 500: // Internal Server Error
            throw new Exception("500 INTERNAL_SERVER_ERROR: " + responseBody);
            
        case 503: // Service Unavailable
            throw new Exception("503 SERVICE_UNAVAILABLE: " + responseBody);
            
        case 504: // Gateway Timeout
            throw new Exception("504 GATEWAY_TIMEOUT: " + responseBody);
            
        default:
            throw new Exception("HTTP " + responseCode + ": " + responseBody);
    }
}
```

---

## 📝 TESTING PLAN

### **Test Case 1: Inventory - Add Product**

#### Test 1.1: Success Case (201)
```
Input:
POST /products
Content-Type: application/json
Body: {
  "sku": "LENS-001",
  "name": "Single Vision Lens",
  "category": "LENS",
  "qtyOnHand": 100,
  "priceCost": 50000,
  "priceRetail": 100000
}

Expected: 201 Created
Response: {
  "id": 1,
  "sku": "LENS-001",
  ...
}
```

#### Test 1.2: Missing SKU (400)
```
Input:
POST /products
Body: {
  "name": "Single Vision Lens",
  "category": "LENS"
}

Expected: 400 Bad Request
Response: {
  "error": "BAD_REQUEST",
  "message": "SKU is required"
}
```

#### Test 1.3: Negative Quantity (422)
```
Input:
POST /products
Body: {
  "sku": "LENS-002",
  "name": "Lens",
  "qtyOnHand": -10
}

Expected: 422 Unprocessable Entity
Response: {
  "error": "VALIDATION_FAILED",
  "message": "Quantity on hand cannot be negative"
}
```

#### Test 1.4: Duplicate SKU (409)
```
Input:
POST /products (lần 2 với cùng SKU)
Body: {
  "sku": "LENS-001",  // Đã tồn tại
  "name": "Another Lens"
}

Expected: 409 Conflict
Response: {
  "error": "INVENTORY_CONFLICT",
  "message": "Product with SKU 'LENS-001' already exists"
}
```

#### Test 1.5: Database Down (503)
```
Setup: Stop database server
Input: POST /products (any valid data)

Expected: 503 Service Unavailable
Response: {
  "error": "SERVICE_UNAVAILABLE",
  "message": "Database connection error. Please try again later."
}
```

#### Test 1.6: Missing Content-Type (415)
```
Input:
POST /products
(không có Content-Type header)

Expected: 415 Unsupported Media Type
Response: {
  "error": "UNSUPPORTED_MEDIA_TYPE",
  "message": "Content-Type header is required"
}
```

---

### **Test Case 2: Inventory - Update Product**

#### Test 2.1: Success (200)
```
Input:
PUT /products
Body: {
  "id": 1,
  "sku": "LENS-001",
  "name": "Updated Name",
  "qtyOnHand": 150
}

Expected: 200 OK
```

#### Test 2.2: Product Not Found (404)
```
Input:
PUT /products
Body: {
  "id": 9999,  // Không tồn tại
  "name": "Test"
}

Expected: 404 Not Found
Response: {
  "error": "NOT_FOUND",
  "message": "Product with ID 9999 not found"
}
```

#### Test 2.3: SKU Conflict (409)
```
Setup: 
- Product 1: SKU="LENS-001"
- Product 2: SKU="LENS-002"

Input:
PUT /products
Body: {
  "id": 2,
  "sku": "LENS-001"  // Trùng với Product 1
}

Expected: 409 Conflict
Response: {
  "error": "SKU_CONFLICT",
  "message": "SKU 'LENS-001' is already used by another product"
}
```

---

### **Test Case 3: Inventory - Search**

#### Test 3.1: Success with results (200)
```
Input:
GET /products/search?sku=LENS

Expected: 200 OK
Response: {
  "id": 1,
  "sku": "LENS-001",
  ...
}
```

#### Test 3.2: Success with no results (200)
```
Input:
GET /products/search?sku=NOTFOUND

Expected: 200 OK (empty result, không phải 404)
Response: {}  hoặc null
```

#### Test 3.3: Missing Keyword (400)
```
Input:
GET /products/search
(không có query param)

Expected: 400 Bad Request
```

#### Test 3.4: Keyword Too Short (422)
```
Input:
GET /products/search?sku=L
(chỉ 1 ký tự)

Expected: 422 Validation Error
Response: {
  "error": "VALIDATION_ERROR",
  "message": "Search keyword must be at least 2 characters"
}
```

#### Test 3.5: Forbidden Characters (422)
```
Input:
GET /products/search?sku=LENS';DROP TABLE--

Expected: 422 Validation Error
Response: {
  "error": "VALIDATION_ERROR",
  "message": "Search keyword contains forbidden characters"
}
```

---

### **Test Case 4: Customer - Create**

#### Test 4.1: Success (201)
```
Input:
POST /customers
Body: {
  "firstname": "Nguyen",
  "lastname": "Van A",
  "phone": "0901234567",
  "email": "nva@example.com",
  "dob": "1990-01-01",
  "gender": "MALE"
}

Expected: 201 Created
```

#### Test 4.2: Duplicate Phone (409)
```
Input:
POST /customers (lần 2)
Body: {
  "firstname": "Tran",
  "lastname": "Van B",
  "phone": "0901234567"  // Đã tồn tại
}

Expected: 409 Conflict
Response: {
  "error": "CONFLICT",
  "message": "Phone number already exists"
}
```

#### Test 4.3: Future DOB (422)
```
Input:
POST /customers
Body: {
  "firstname": "Test",
  "lastname": "User",
  "phone": "0909999999",
  "dob": "2030-01-01"  // Tương lai
}

Expected: 422 Unprocessable Entity
Response: {
  "error": "VALIDATION_FAILED",
  "message": "Date of birth cannot be in the future"
}
```

---

### **Test Case 5: Frontend Error Display**

#### Test 5.1: Network Error
```
Setup: Turn off backend server
Action: Click "Create Product" button
Expected:
- Alert xuất hiện
- Title: "Lỗi kết nối"
- Message: "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."
- Status label: "❌ Lỗi: Network error..."
```

#### Test 5.2: Validation Error (422)
```
Action: Nhập số lượng = -5, click Save
Expected:
- Alert xuất hiện
- Title: "Không đáp ứng quy tắc"
- Message: "Dữ liệu không đáp ứng quy tắc nghiệp vụ..."
- Chi tiết: "Quantity on hand cannot be negative"
```

#### Test 5.3: Conflict Error (409)
```
Action: Nhập SKU đã tồn tại, click Save
Expected:
- Alert xuất hiện
- Title: "Dữ liệu bị xung đột"
- Message: "❌ SKU đã tồn tại\n\nSản phẩm với SKU này đã có trong hệ thống..."
```

---

## 🎯 CHECKLIST TRIỂN KHAI

### Backend (mini-boot)

#### Core Utilities
- [ ] Tạo `ValidationUtils.java`
  - [ ] `validateContentType()`
  - [ ] `validateJWT()`
  - [ ] `validateRole()`
  - [ ] `validateRequiredFields()`
  - [ ] `validateProductBusinessRules()`
  - [ ] `validateSearchKeyword()`
  
- [ ] Tạo `DatabaseErrorHandler.java`
  - [ ] `handleDatabaseException()`
  - [ ] `isRetryable()`
  - [ ] Map PostgreSQL error codes

#### Inventory Module
- [ ] Update `InventoryController.createProduct()`
  - [ ] 415 Content-Type check
  - [ ] 401 JWT validation
  - [ ] 403 Role check
  - [ ] 400 Required fields
  - [ ] 422 Business rules
  - [ ] 409 SKU conflict
  - [ ] 503/504/500 Database errors
  
- [ ] Update `InventoryController.updateProduct()`
  - [ ] Same as create +
  - [ ] 404 Product not found
  - [ ] 409 SKU conflict with other products
  - [ ] 412 Version conflict (optional)
  
- [ ] Update `InventoryController.deleteProduct()`
  - [ ] Same validations
  - [ ] 404 Not found
  - [ ] 422 Foreign key constraint
  
- [ ] Update `InventoryController.searchProductBySku()`
  - [ ] 400 Missing keyword
  - [ ] 422 Keyword validation
  - [ ] 404 Not found (for specific ID)
  - [ ] 503/504/500 Database errors

#### Customer Module
- [ ] Update `CustomerRecordController.createCustomer()`
  - [ ] All validations như Inventory
  - [ ] 409 Duplicate phone/email
  - [ ] 422 DOB validation
  
- [ ] Update `CustomerRecordController.updateCustomer()`
  - [ ] Same as create
  - [ ] 404 Customer not found
  
- [ ] Update `CustomerRecordRepository`
  - [ ] Add `findByPhone()`
  - [ ] Add `findByEmail()`
  - [ ] Implement in `PostgreSQLCustomerRecordRepository`

#### Prescription Module
- [ ] Update `PrescriptionController.createPrescription()`
  - [ ] All validations
  - [ ] 422 Medical data validation
  
- [ ] Update `PrescriptionController.updatePrescription()`
  - [ ] Same as create
  - [ ] 404 Not found

### Frontend (oop_ui)

#### Inventory UI
- [ ] Update `ProductCRUDController.createProductAsync()`
  - [ ] Parse error JSON
  - [ ] Show specific alerts for 409, 422, 503, 504
  - [ ] User-friendly messages
  
- [ ] Update `ProductCRUDController.updateProductAsync()`
  - [ ] Same as create
  
- [ ] Update `ProductCRUDController.deleteProductAsync()`
  - [ ] Handle 422 (foreign key constraint)

#### Customer UI
- [ ] Update `CustomerHubController` error handling
  - [ ] Handle all error codes
  - [ ] Show alerts
  
- [ ] Update `CustomerDetailController`
  - [ ] Error display

#### Prescription UI
- [ ] Update `PrescriptionEditorController.handleSavePrescription()`
  - [ ] Improve error handling
  - [ ] Parse server errors
  - [ ] Show specific messages

#### Service Layer
- [ ] Update `ApiProductService`
  - [ ] Improve `createProduct()` error handling
  - [ ] Improve `updateProduct()`
  - [ ] Improve `deleteProduct()`
  
- [ ] Update `CustomerRecordService`
  - [ ] Same improvements
  
- [ ] Update `PrescriptionService`
  - [ ] Same improvements

### Testing
- [ ] Test Inventory - Add
  - [ ] Success (201)
  - [ ] Bad Request (400)
  - [ ] Validation Failed (422)
  - [ ] Conflict (409)
  - [ ] Unsupported Media Type (415)
  - [ ] Service Unavailable (503)
  
- [ ] Test Inventory - Update
  - [ ] Success (200)
  - [ ] Not Found (404)
  - [ ] SKU Conflict (409)
  - [ ] Version Conflict (412) - optional
  
- [ ] Test Inventory - Search
  - [ ] Success with results
  - [ ] Success with empty
  - [ ] Missing keyword (400)
  - [ ] Invalid keyword (422)
  
- [ ] Test Customer - Create
  - [ ] Success (201)
  - [ ] Duplicate phone (409)
  - [ ] Duplicate email (409)
  - [ ] Invalid DOB (422)
  
- [ ] Test Frontend Error Display
  - [ ] Network error alert
  - [ ] Validation error (422)
  - [ ] Conflict error (409)
  - [ ] Service unavailable (503)
  - [ ] Timeout (504)

---

## 📚 TÀI LIỆU THAM KHẢO

### HTTP Status Codes
- **400 Bad Request:** Thiếu tham số, sai định dạng JSON
- **401 Unauthorized:** Token không hợp lệ/hết hạn
- **403 Forbidden:** Không có quyền truy cập
- **404 Not Found:** Resource không tồn tại
- **409 Conflict:** Dữ liệu trùng lặp (SKU, phone, email)
- **412 Precondition Failed:** Version/ETag conflict
- **415 Unsupported Media Type:** Content-Type sai
- **422 Unprocessable Entity:** Vi phạm business rules
- **429 Too Many Requests:** Rate limiting
- **500 Internal Server Error:** Lỗi server không xác định
- **503 Service Unavailable:** Database down/maintenance
- **504 Gateway Timeout:** Database timeout

### PostgreSQL Error Codes
- **23505:** Unique violation → 409 Conflict
- **23503:** Foreign key violation → 422 Unprocessable Entity
- **08xxx:** Connection errors → 503 Service Unavailable
- **40P01:** Deadlock → 500 (retryable)

### Files cần chỉnh sửa
```
mini-boot/
├── src/main/java/org/miniboot/app/
│   ├── util/
│   │   ├── ValidationUtils.java          [CREATE]
│   │   └── DatabaseErrorHandler.java     [CREATE]
│   ├── controllers/
│   │   ├── Inventory/
│   │   │   └── InventoryController.java  [UPDATE]
│   │   └── PatientAndPrescription/
│   │       ├── CustomerRecordController.java [UPDATE]
│   │       └── PrescriptionController.java   [UPDATE]
│   └── domain/repo/PatientAndPrescription/
│       ├── CustomerRecordRepository.java        [UPDATE]
│       └── PostgreSQLCustomerRecordRepository.java [UPDATE]

oop_ui/
├── src/main/java/org/example/oop/
│   ├── Control/
│   │   ├── Inventory/
│   │   │   └── ProductCRUDController.java [UPDATE]
│   │   └── PatientAndPrescription/
│   │       ├── CustomerHubController.java    [UPDATE]
│   │       ├── CustomerDetailController.java [UPDATE]
│   │       └── PrescriptionEditorController.java [UPDATE]
│   └── Service/
│       ├── ApiProductService.java     [UPDATE]
│       ├── CustomerRecordService.java [UPDATE]
│       └── PrescriptionService.java   [UPDATE]
```

---

## 💡 LƯU Ý QUAN TRỌNG

### 1. **JWT Authentication**
- Hiện tại chỉ placeholder trong `ValidationUtils.validateJWT()`
- Cần implement JWT library (jjwt hoặc auth0-java-jwt)
- Parse token từ header `Authorization: Bearer <token>`
- Validate signature, expiration, issuer
- Extract user ID và role từ claims

### 2. **Optimistic Locking (412 Precondition Failed)**
- Cần thêm `version` field vào Product/Customer models
- Client gửi `If-Match: <etag>` hoặc `version` field
- Server check version trước khi update
- Return 412 nếu version không khớp (concurrent modification)

### 3. **Rate Limiting (429 Too Many Requests)**
- Implement trong middleware/filter layer
- Track số requests per IP/user trong time window
- Return 429 khi vượt giới hạn
- Add header `Retry-After: <seconds>`

### 4. **Database Connection Pooling**
- Hiện tại mỗi request tạo connection mới
- Cần implement connection pool (HikariCP)
- Set timeout cho connection acquisition
- Giúp handle 503/504 errors tốt hơn

### 5. **Logging**
- Thêm structured logging (SLF4J + Logback)
- Log tất cả errors với stack trace
- Log request ID để trace
- Log duration của database queries

### 6. **Error Response Format**
- Chuẩn hóa JSON error response:
```json
{
  "error": "ERROR_CODE",
  "message": "User-friendly message",
  "details": {
    "field": "sku",
    "constraint": "unique"
  },
  "timestamp": "2025-11-08T10:30:00Z",
  "path": "/products"
}
```

### 7. **Frontend Retry Logic**
- Implement automatic retry cho 503/504
- Exponential backoff (1s, 2s, 4s)
- Max 3 retries
- Show retry count trong UI

---

## 🚀 DEPLOYMENT CHECKLIST

- [ ] Test tất cả endpoints với Postman/curl
- [ ] Test frontend với tất cả error scenarios
- [ ] Update API documentation với error responses
- [ ] Add error monitoring (Sentry/Rollbar)
- [ ] Set up database connection pooling
- [ ] Configure timeouts (connection, read, write)
- [ ] Enable CORS with proper error handling
- [ ] Add request logging middleware
- [ ] Set up health check endpoints
- [ ] Document error codes cho frontend team

---

**Kết thúc tài liệu**  
*Cập nhật lần cuối: 8 tháng 11, 2025*  
*Người soạn: GitHub Copilot*
