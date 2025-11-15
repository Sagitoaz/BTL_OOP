# 📋 KẾ HOẠCH TRIỂN KHAI XỬ LÝ LỖI THEO SEQUENCE DIAGRAM

**Ngày tạo:** 8 tháng 11, 2025  
**Cập nhật:** 11 tháng 11, 2025  
**Branch:** OOP-49  
**Mục tiêu:** Đảm bảo backend và frontend xử lý đầy đủ tất cả các mã lỗi như được định nghĩa trong PlantUML Sequence Diagrams

---

## 🎯 TỔNG QUAN DỰ ÁN

Sau khi quét toàn bộ source code thực tế của dự án, tôi đã xác định rõ:

### ✅ **ĐÃ TRIỂN KHAI (Verified from source code):**
1. **ValidationUtils.java** - Đã có sẵn với các methods:
   - `validateContentType()` - Kiểm tra Content-Type header
   - `validateJWT()` - Placeholder cho JWT authentication
   - `validateRole()` - Placeholder cho role-based authorization
   - `validateProductBusinessRules()` - Kiểm tra qty, price >= 0
   - `error()` - Tạo JSON error response chuẩn

2. **DatabaseErrorHandler.java** - Đã có sẵn:
   - `handleDatabaseException()` - Map SQLException sang HTTP codes
   - `isRetryable()` - Check lỗi có thể retry
   - Support PostgreSQL error codes (23505, 23503, 08xxx, 40P01)

3. **InventoryController.java** - Đã implement đầy đủ error handling:
   - `createProduct()` - Có đầy đủ validations (415, 401, 403, 400, 422, 409, 503/504/500)
   - `updateProduct()` - Có đầy đủ validations + check existence (404)
   - `searchProductBySku()` - Có basic validation (400, 404, 500)

4. **CustomerRecordController.java** - Đã implement một phần:
   - `createCustomer()` - Có validations (415, 401, 403, 400, 422)
   - Có comment TODO cho duplicate checking (409)

5. **ApiProductService.java** (Frontend) - Đã có retry mechanism:
   - `getAllProducts()` - Retry 3 lần với timeout handling
   - `createProduct()`, `updateProduct()`, `deleteProduct()` - Basic error handling

6. **ProductCRUDController.java** (Frontend) - Có async error callbacks:
   - `createProductAsync()`, `updateProductAsync()`, `deleteProductAsync()` - Có error handlers

### ❌ **CẦN BỔ SUNG:**
1. **CustomerRecordRepository** - Chưa có methods `findByPhone()`, `findByEmail()`
2. **CustomerRecordController** - Chưa implement duplicate checking (409)
3. **ValidationUtils** - Chưa có `validateSearchKeyword()` cho search validation
4. **InventoryController.searchProductBySku()** - Chưa validate keyword (422)
5. **Frontend error parsing** - Chưa parse JSON error response để hiển thị message cụ thể
6. **PrescriptionController** - Chưa có error handling
7. **JWT & Role validation** - Chỉ là placeholder, chưa implement thực sự

---

## 📊 PHÂN TÍCH SEQUENCE DIAGRAMS VÀ TRẠNG THÁI TRIỂN KHAI

### 1️⃣ **INVENTORY - ADD (AddInventory.puml)**

#### **Yêu cầu từ Sequence Diagram:**

Theo file `UML/Sequence/Inventory/AddInventory.puml`:

**Flow chính:**
1. ✅ 415 UNSUPPORTED_MEDIA_TYPE - Kiểm tra Content-Type = application/json
2. ✅ 401 UNAUTHORIZED - Xác minh JWT (Authorization: Bearer ...)
3. ✅ 403 FORBIDDEN - Kiểm tra quyền (vai trò: ADMIN)
4. ✅ 400 BAD_REQUEST - Kiểm tra trường bắt buộc & kiểu dữ liệu
5. ✅ 422 UNPROCESSABLE_ENTITY - Kiểm tra quy tắc nghiệp vụ (số lượng ≥ 0, giá ≥ 0)
6. ✅ 409 CONFLICT - Check trùng SKU/ID (SELECT theo SKU/ID)
7. ✅ 503 SERVICE_UNAVAILABLE - Dịch vụ không khả dụng (DB hỏng/đang bảo trì)
8. ✅ 504 GATEWAY_TIMEOUT - Hết thời gian chờ (Quá hạn truy vấn)
9. ✅ 500 INTERNAL_SERVER_ERROR - Lỗi CSDL (deadlock/constraint)
10. ✅ 201 CREATED - Thêm thành công

**Error codes trong diagram:**
- ✅ `"KHÔNG_HỖ_TRỢ_DỮ_LIỆU"` (415)
- ✅ `"CHƯA_XÁC_THỰC"` (401)
- ✅ `"KHÔNG_CÓ_QUYỀN"` (403)
- ✅ `"YÊU_CẦU_KHÔNG_HỢP_LỆ"` (400)
- ✅ `"DỮ_LIỆU_KHÔNG_HỢP_LỆ"` (422)
- ✅ `"HÀNG_TỒN_KHO_TRÙNG"` (409)
- ✅ `"DỊCH_VỤ_KHÔNG_KHẢ_DỤNG"` (503)
- ✅ `"HẾT_THỜI_GIAN_CHỜ"` (504)
- ✅ `"LỖI_CSDL"` (500)
- ✅ `"ĐÃ_TẠO"` (201)

#### **Trạng thái triển khai:**
**Backend `InventoryController.createProduct()`** - ✅ **ĐÃ HOÀN THÀNH**

Code hiện tại trong file `InventoryController.java` (lines 127-195) đã implement đầy đủ theo diagram:

```java
public Function<HttpRequest, HttpResponse> createProduct() {
    return (HttpRequest req) -> {
        // STEP 1: ✅ Validate Content-Type (415)
        HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
        if (contentTypeError != null) return contentTypeError;

        // STEP 2: ✅ Validate JWT (401)
        HttpResponse authError = ValidationUtils.validateJWT(req);
        if (authError != null) return authError;

        // STEP 3: ✅ Validate Role - ADMIN only (403)
        HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
        if (roleError != null) return roleError;

        try {
            // STEP 4: ✅ Parse JSON (400)
            Product product = Json.fromBytes(req.body, Product.class);

            // STEP 5: ✅ Validate required fields (400)
            if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", "SKU is required");
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", "Name is required");
            }

            // STEP 6: ✅ Validate business rules (422)
            HttpResponse businessRuleError = ValidationUtils.validateProductBusinessRules(
                product.getQtyOnHand(), product.getPriceCost(), product.getPriceRetail()
            );
            if (businessRuleError != null) return businessRuleError;

            // STEP 7: ✅ Check SKU conflict (409)
            Optional<Product> existing = productRepo.findBySku(product.getSku());
            if (existing.isPresent()) {
                return ValidationUtils.error(409, "INVENTORY_CONFLICT",
                    "Product with SKU '" + product.getSku() + "' already exists");
            }

            // STEP 8: ✅ Save to database (503/504/500)
            Product saved = productRepo.save(product);
            if (saved == null) {
                return ValidationUtils.error(500, "DB_ERROR", "Failed to save product");
            }

            // STEP 9: ✅ Success (201)
            return Json.created(saved);
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
    };
}
```

✅ **Kết luận**: Backend createProduct đã implement đầy đủ theo sequence diagram

---

### 2️⃣ **INVENTORY - UPDATE/DELETE (EditAndDeleteInventory.puml)**

#### Trạng thái hiện tại:
**Backend `InventoryController.updateProduct()`** - ✅ **ĐÃ HOÀN THÀNH**

Code hiện tại tương tự `createProduct()` nhưng có thêm:
- ✅ Check product ID (400)
- ✅ Check product existence (404)  
- ✅ Check SKU conflict với products khác (409)
- ✅ All other validations giống createProduct

**Backend `InventoryController.deleteProduct()`** - ⚠️ **CẦN KIỂM TRA**
- Cần verify implementation trong code

**Frontend `ProductCRUDController`** - ⚠️ **CẦN CẢI THIỆN ERROR HANDLING**

Code hiện tại (lines 318-329):
```java
// Error callback trong createProductAsync()
error -> {
    showLoading(false);
    disableButtons(false);
    updateStatus("❌ Lỗi tạo sản phẩm: " + error.getMessage());
    showError("Không thể tạo sản phẩm mới.\n\n" + error.getMessage());
}
```

❌ **VẤN ĐỀ**: Hiển thị error message chung chung, không parse JSON response từ backend

---

### 3️⃣ **INVENTORY - SEARCH/VIEW (SearchAndViewInventory.puml)**

#### Trạng thái hiện tại:
**Backend `InventoryController.searchProductBySku()`** - ⚠️ **CHƯA ĐẦY ĐỦ**

Code hiện tại (lines 88-117):
```java
public Function<HttpRequest, HttpResponse> searchProductBySku() {
    return (HttpRequest req) -> {
        try {
            Optional<String> skuOpt = ExtractHelper.extractString(q, "sku");
            
            if (skuOpt.isEmpty()) {
                return HttpResponse.of(400, "text/plain",
                    "Missing sku parameter".getBytes(StandardCharsets.UTF_8));
            }
            
            String sku = skuOpt.get();
            // ❌ THIẾU: Validate keyword length (min 2 chars)
            // ❌ THIẾU: Validate forbidden characters (SQL injection)
            
            Optional<Product> productOpt = productRepo.findBySku(sku);
            
            if (productOpt.isPresent()) {
                return Json.ok(productOpt.get());
            } else {
                return HttpResponse.of(404, "text/plain",
                    "Product not found".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // ❌ CHƯA ĐỦ: Không phân biệt timeout vs database error
            return HttpResponse.of(500, "text/plain",
                ("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    };
}
```

❌ **THIẾU**: 
- Validate keyword length (422)
- Check forbidden characters (422)
- Proper database error handling (503/504)

---

### 4️⃣ **PATIENT - CREATE/UPDATE (UpdatePatient.puml)**

#### Trạng thái hiện tại:
**Backend `CustomerRecordController.createCustomer()`** - ⚠️ **CHƯA ĐẦY ĐỦ**

Code hiện tại (lines 42-113):
```java
public Function<HttpRequest, HttpResponse> createCustomer() {
    return (HttpRequest req) -> {
        // ✅ Validate Content-Type (415)
        HttpResponse contentTypeError = ValidationUtils.validateContentType(req, "application/json");
        if (contentTypeError != null) return contentTypeError;

        // ✅ Validate JWT (401)
        HttpResponse authError = ValidationUtils.validateJWT(req);
        if (authError != null) return authError;

        // ✅ Validate Role (403)
        HttpResponse roleError = ValidationUtils.validateRole(req, "ADMIN");
        if (roleError != null) return roleError;

        try {
            Customer customerToCreate = gson.fromJson(jsonBody, Customer.class);

            // ✅ Validate required fields (400)
            if (customerToCreate.getFirstname() == null || 
                customerToCreate.getFirstname().trim().isEmpty()) {
                return ValidationUtils.error(400, "BAD_REQUEST", "First name is required");
            }
            // ... tương tự cho lastname, phone

            // ✅ Validate business rules (422)
            if (customerToCreate.getDob() != null &&
                customerToCreate.getDob().isAfter(LocalDate.now())) {
                return ValidationUtils.error(422, "VALIDATION_FAILED",
                    "Date of birth cannot be in the future");
            }

            // ❌ THIẾU: Check duplicates (409)
            // TODO: Implement findByPhone, findByEmail in repository
            // if (phone exists) return 409 CONFLICT
            // if (email exists) return 409 CONFLICT

            // ✅ Save with database error handling
            Customer savedCustomer = customerRecordRepository.save(customerToCreate);
            
            return HttpResponse.of(201, "application/json", ...);
        } catch (Exception e) {
            return DatabaseErrorHandler.handleDatabaseException(e);
        }
    };
}
```

❌ **THIẾU**:
- `CustomerRecordRepository.findByPhone()` - Chưa có method
- `CustomerRecordRepository.findByEmail()` - Chưa có method  
- Duplicate checking logic (409) trong controller

**Repository hiện tại** - File `CustomerRecordRepository.java` (lines 1-27):
```java
public interface CustomerRecordRepository {
    Customer save(Customer customer);
    void saveAll(List<Customer> customers);
    List<Customer> findAll();
    List<Customer> findByFilterAll(CustomerSearchCriteria criteria);
    boolean deleteById(int id);
    boolean existsById(int id);
    long count();
    
    // ❌ THIẾU:
    // Optional<Customer> findByPhone(String phone);
    // Optional<Customer> findByEmail(String email);
}
```

---

### 5️⃣ **PRESCRIPTION - CREATE/UPDATE**

⚠️ **CHƯA PHÂN TÍCH** - Cần kiểm tra file `PrescriptionController.java`

---

## 🛠️ HƯỚNG DẪN TRIỂN KHAI CHI TIẾT

### **NHIỆM VỤ 1: Bổ sung ValidationUtils.validateSearchKeyword()**

**File:** `mini-boot/src/main/java/org/miniboot/app/util/errorvalidation/ValidationUtils.java`

**Vị trí:** Thêm method mới sau method `validateProductBusinessRules()` (sau line 100)

**Code cần thêm:**

```java
    /**
     * Validate search keyword
     * @param keyword Search keyword from user
     * @return HttpResponse with error if invalid, null if valid
     */
    public static HttpResponse validateSearchKeyword(String keyword) {
        // Check null or empty
        if (keyword == null || keyword.trim().isEmpty()) {
            return error(400, "BAD_REQUEST",
                    "Search keyword is required");
        }

        // Check minimum length
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
```

**Giải thích:**
- Line 1-3: Check keyword null hoặc empty → 400 BAD_REQUEST
- Line 4-6: Check độ dài tối thiểu 2 ký tự → 422 VALIDATION_ERROR
- Line 7-9: Check ký tự nguy hiểm (';"\) → 422 VALIDATION_ERROR
- Line 10: Return null nếu valid

---

### **NHIỆM VỤ 2: Cập nhật InventoryController.searchProductBySku()**

**File:** `mini-boot/src/main/java/org/miniboot/app/controllers/Inventory/InventoryController.java`

**Vị trí:** Method `searchProductBySku()` (lines 88-117)

**Cách sửa:**

**Bước 1:** Tìm đoạn code:
```java
String sku = skuOpt.get();
System.out.println("🔍 Searching product by SKU: " + sku);

Optional<Product> productOpt = productRepo.findBySku(sku);
```

**Bước 2:** Thay thế bằng:
```java
String sku = skuOpt.get();

// THÊM: Validate keyword (422)
HttpResponse keywordError = ValidationUtils.validateSearchKeyword(sku);
if (keywordError != null) return keywordError;

System.out.println("🔍 Searching product by SKU: " + sku);

// THÊM: Handle database errors properly
Optional<Product> productOpt;
try {
    productOpt = productRepo.findBySku(sku);
} catch (Exception e) {
    return DatabaseErrorHandler.handleDatabaseException(e);
}
```

**Giải thích từng dòng:**
1. `ValidationUtils.validateSearchKeyword(sku)` - Validate keyword trước khi search
2. `if (keywordError != null) return keywordError` - Return ngay nếu invalid
3. Wrap `findBySku()` trong try-catch để handle database timeout/connection errors
4. `DatabaseErrorHandler.handleDatabaseException(e)` - Map SQLException sang HTTP codes (503/504/500)

---

### **NHIỆM VỤ 3: Thêm methods vào CustomerRecordRepository**

#### **Bước 3A: Cập nhật Interface**

**File:** `mini-boot/src/main/java/org/miniboot/app/domain/repo/PatientAndPrescription/CustomerRecordRepository.java`

**Vị trí:** Sau method `count()` (line 23)

**Code cần thêm:**

```java
    /**
     * Find customer by phone number
     * @param phone Phone number to search
     * @return Optional containing customer if found
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Find customer by email address
     * @param email Email to search
     * @return Optional containing customer if found
     */
    Optional<Customer> findByEmail(String email);
```

#### **Bước 3B: Implement trong PostgreSQLCustomerRecordRepository**

**File:** `mini-boot/src/main/java/org/miniboot/app/domain/repo/PatientAndPrescription/PostgreSQLCustomerRecordRepository.java`

**Vị trí:** Thêm vào cuối class, trước dấu `}`

**Code cần thêm:**

```java
    @Override
    public Optional<Customer> findByPhone(String phone) {
        String sqlQuery = "SELECT * FROM customers WHERE phone = ? LIMIT 1;";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding customer by phone: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sqlQuery = "SELECT * FROM customers WHERE email = ? LIMIT 1;";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(CustomerMapper.mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding customer by email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database find failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
```

**Giải thích:**
- `PreparedStatement` - Prevent SQL injection
- `try-with-resources` - Tự động đóng connection
- `ResultSet rs` - Dữ liệu trả về từ database
- `rs.next()` - Check có record nào không
- `CustomerMapper.mapResultSetToCustomer()` - Convert ResultSet sang Customer object
- `throw new RuntimeException()` - Throw exception để DatabaseErrorHandler xử lý

---

### **NHIỆM VỤ 4: Cập nhật CustomerRecordController.createCustomer()**

**File:** `mini-boot/src/main/java/org/miniboot/app/controllers/PatientAndPrescription/CustomerRecordController.java`

**Vị trí:** Tìm comment `// TODO: Implement findByPhone, findByEmail` (line 88-90)

**Bước 1:** Xóa 3 dòng comment TODO

**Bước 2:** Thay thế bằng code:

```java
                // Check phone duplicate (409)
                try {
                    Optional<Customer> existingPhone = customerRecordRepository.findByPhone(
                            customerToCreate.getPhone());
                    if (existingPhone.isPresent()) {
                        return ValidationUtils.error(409, "PHONE_CONFLICT",
                                "Phone number '" + customerToCreate.getPhone() + 
                                "' is already registered");
                    }
                } catch (Exception e) {
                    return DatabaseErrorHandler.handleDatabaseException(e);
                }

                // Check email duplicate (409) - only if email is provided
                if (customerToCreate.getEmail() != null && 
                    !customerToCreate.getEmail().trim().isEmpty()) {
                    try {
                        Optional<Customer> existingEmail = customerRecordRepository.findByEmail(
                                customerToCreate.getEmail());
                        if (existingEmail.isPresent()) {
                            return ValidationUtils.error(409, "EMAIL_CONFLICT",
                                    "Email '" + customerToCreate.getEmail() + 
                                    "' is already registered");
                        }
                    } catch (Exception e) {
                        return DatabaseErrorHandler.handleDatabaseException(e);
                    }
                }
```

**Giải thích từng block:**

**Block 1: Check phone duplicate**
- Line 1-3: Gọi `findByPhone()` trong try-catch
- Line 4-6: Nếu `existingPhone.isPresent()` → phone đã tồn tại → return 409
- Line 7-9: Catch database errors → delegate to DatabaseErrorHandler

**Block 2: Check email duplicate**
- Line 1-2: Check email không null và không empty (email là optional field)
- Line 3-5: Gọi `findByEmail()` trong try-catch
- Line 6-8: Nếu tìm thấy → return 409 với message cụ thể
- Line 9-11: Catch database errors

**Lưu ý:** Code này đặt **TRƯỚC** dòng `// Save` trong method

---

### **NHIỆM VỤ 5: Cải thiện Frontend Error Handling**

#### **Bước 5A: Thêm Error Parser vào ProductCRUDController**

**File:** `oop_ui/src/main/java/org/example/oop/Control/Inventory/ProductCRUDController.java`

**Vị trí:** Thêm helper method vào cuối class (trước dấu `}` cuối cùng)

**Code cần thêm:**

```java
    /**
     * Parse error message from Exception
     * Extract JSON error response if available
     */
    private ErrorInfo parseError(Throwable error) {
        String rawMessage = error.getMessage();
        if (rawMessage == null) {
            return new ErrorInfo(0, "UNKNOWN_ERROR", "Unknown error occurred");
        }

        // Try to extract HTTP status code
        int statusCode = 0;
        if (rawMessage.matches(".*\\b(\\d{3})\\b.*")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(\\d{3})\\b");
            java.util.regex.Matcher matcher = pattern.matcher(rawMessage);
            if (matcher.find()) {
                statusCode = Integer.parseInt(matcher.group(1));
            }
        }

        // Try to extract JSON message
        String errorCode = "ERROR";
        String message = rawMessage;
        
        try {
            // Check if response contains JSON
            int jsonStart = rawMessage.indexOf("{");
            int jsonEnd = rawMessage.lastIndexOf("}");
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String json = rawMessage.substring(jsonStart, jsonEnd + 1);
                
                // Simple JSON parsing (without external library)
                if (json.contains("\"error\":")) {
                    int errorStart = json.indexOf("\"error\":\"") + 9;
                    int errorEnd = json.indexOf("\"", errorStart);
                    if (errorEnd > errorStart) {
                        errorCode = json.substring(errorStart, errorEnd);
                    }
                }
                
                if (json.contains("\"message\":")) {
                    int msgStart = json.indexOf("\"message\":\"") + 11;
                    int msgEnd = json.indexOf("\"", msgStart);
                    if (msgEnd > msgStart) {
                        message = json.substring(msgStart, msgEnd);
                    }
                }
            }
        } catch (Exception e) {
            // JSON parsing failed, use raw message
            System.err.println("⚠️ Failed to parse error JSON: " + e.getMessage());
        }

        return new ErrorInfo(statusCode, errorCode, message);
    }

    /**
     * Inner class to hold parsed error information
     */
    private static class ErrorInfo {
        final int statusCode;
        final String errorCode;
        final String message;

        ErrorInfo(int statusCode, String errorCode, String message) {
            this.statusCode = statusCode;
            this.errorCode = errorCode;
            this.message = message;
        }
    }
```

**Giải thích:**
- Method `parseError()` nhận `Throwable` và extract thông tin lỗi
- Dùng regex để tìm HTTP status code (400, 404, 409, v.v.)
- Parse JSON response từ backend để lấy `error` và `message` fields
- Return `ErrorInfo` object chứa statusCode, errorCode, message
- Nếu parse fail → fallback to raw message

#### **Bước 5B: Cập nhật Error Callbacks trong ProductCRUDController**

**Vị trí:** Method `createProductAsync()` (line 267-275)

**Tìm đoạn code hiện tại:**
```java
                    // Error
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         updateStatus("❌ Lỗi tạo sản phẩm: " + error.getMessage());
                         showError("Không thể tạo sản phẩm mới.\n\n" + error.getMessage());
                    });
```

**Thay thế bằng:**
```java
                    // Error - with detailed parsing
                    error -> {
                         showLoading(false);
                         disableButtons(false);
                         
                         ErrorInfo errorInfo = parseError(error);
                         
                         // Display user-friendly message based on error code
                         String title;
                         String message;
                         
                         switch (errorInfo.statusCode) {
                             case 409: // Conflict
                                 title = "❌ Dữ liệu bị trùng lặp";
                                 message = "SKU đã tồn tại trong hệ thống.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng sử dụng SKU khác hoặc cập nhật sản phẩm hiện có.";
                                 break;
                             
                             case 422: // Validation Failed
                                 title = "❌ Dữ liệu không hợp lệ";
                                 message = "Dữ liệu vi phạm quy tắc nghiệp vụ.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng kiểm tra:\n" +
                                          "- Số lượng phải >= 0\n" +
                                          "- Giá cost và retail phải >= 0\n" +
                                          "- Giá retail nên >= giá cost";
                                 break;
                             
                             case 400: // Bad Request
                                 title = "❌ Yêu cầu không hợp lệ";
                                 message = "Dữ liệu gửi lên không đúng định dạng.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng kiểm tra tất cả các trường bắt buộc.";
                                 break;
                             
                             case 503: // Service Unavailable
                                 title = "❌ Máy chủ không khả dụng";
                                 message = "Không thể kết nối đến cơ sở dữ liệu.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng:\n" +
                                          "- Kiểm tra kết nối mạng\n" +
                                          "- Thử lại sau 1-2 phút\n" +
                                          "- Liên hệ quản trị viên nếu vấn đề vẫn tiếp diễn";
                                 break;
                             
                             case 504: // Gateway Timeout
                                 title = "⏱️ Hết thời gian chờ";
                                 message = "Máy chủ xử lý quá lâu.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng:\n" +
                                          "- Thử lại ngay\n" +
                                          "- Kiểm tra tốc độ mạng\n" +
                                          "- Liên hệ IT nếu lỗi lặp lại";
                                 break;
                             
                             case 500: // Internal Server Error
                                 title = "❌ Lỗi máy chủ";
                                 message = "Đã xảy ra lỗi không mong muốn trên máy chủ.\n\n" +
                                          "Chi tiết: " + errorInfo.message + "\n\n" +
                                          "Vui lòng liên hệ quản trị viên.";
                                 break;
                             
                             default:
                                 title = "❌ Lỗi không xác định";
                                 message = "Không thể tạo sản phẩm.\n\n" +
                                          "Mã lỗi: " + errorInfo.statusCode + "\n" +
                                          "Chi tiết: " + errorInfo.message;
                         }
                         
                         updateStatus("❌ " + errorInfo.errorCode + ": " + errorInfo.message);
                         showError(title + "\n\n" + message);
                    });
```

**Giải thích từng case:**

**Case 409 (Conflict):**
- Hiển thị message rõ ràng: "SKU đã tồn tại"
- Hướng dẫn user: sử dụng SKU khác hoặc update sản phẩm cũ

**Case 422 (Validation Failed):**
- Giải thích: vi phạm quy tắc nghiệp vụ
- List các rules cần check: qty >= 0, price >= 0, retail >= cost

**Case 400 (Bad Request):**
- Thông báo: dữ liệu không đúng format
- Hướng dẫn: check các trường bắt buộc

**Case 503 (Service Unavailable):**
- Giải thích: database down
- Hướng dẫn troubleshooting: check mạng, retry, contact admin

**Case 504 (Gateway Timeout):**
- Thông báo: xử lý quá lâu
- Hướng dẫn: retry ngay, check network

**Case 500 (Internal Server Error):**
- Thông báo: lỗi server không mong muốn
- Hướng dẫn: contact admin

**Default:**
- Hiển thị status code và message raw

#### **Bước 5C: Tương tự cho updateProductAsync() và deleteProductAsync()**

Áp dụng cùng logic cho 2 methods còn lại:

**updateProductAsync()** (line 347-360):
- Thêm case 404 (Not Found): "Sản phẩm không tồn tại"
- Các cases khác giống createProductAsync()

**deleteProductAsync()** (line 395-408):
- Thêm case 404 (Not Found): "Sản phẩm không tồn tại"
- Thêm case 422 (Validation Failed): "Không thể xóa vì còn ràng buộc dữ liệu"
- Các cases khác giống trên

---

### **NHIỆM VỤ 6: Cải thiện ApiProductService Error Messages**

**File:** `oop_ui/src/main/java/org/example/oop/Service/ApiProductService.java`

**Vị trí:** Method `createProduct()` (lines 148-193)

**Tìm đoạn code:**
```java
        } else if (responseCode >= 500) {
            // ✅ Server error (500, 503, etc.)
            throw new Exception("Lỗi server (" + responseCode + "): " + responseBody +
                    "\n\nVui lòng kiểm tra:\n" +
                    "- Server backend có đang chạy?\n" +
                    "- Database connection có ổn định?\n" +
                    "- Xem logs của server để biết chi tiết");
        } else {
            // Client error (400, 404, etc.)
            throw new Exception("Lỗi tạo sản phẩm (" + responseCode + "): " + responseBody);
        }
```

**Thay thế bằng:**
```java
        } else {
            // Build detailed error message
            String errorMessage = "HTTP " + responseCode + ": ";
            
            // Try to parse JSON error response
            if (responseBody != null && responseBody.contains("{") && responseBody.contains("message")) {
                // Response có JSON format
                errorMessage += responseBody; // Keep full JSON for parsing in Controller
            } else {
                // Plain text response
                errorMessage += (responseBody != null ? responseBody : "Unknown error");
            }
            
            throw new Exception(errorMessage);
        }
```

**Giải thích:**
- Không phân loại error ở Service layer
- Throw Exception với format: "HTTP {code}: {JSON or text}"
- Controller sẽ parse và hiển thị message phù hợp
- Giữ nguyên JSON response để Controller extract được errorCode và message

---

### 2️⃣ **INVENTORY - UPDATE/DELETE (EditAndDeleteInventory.puml)**

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

## 📝 HƯỚNG DẪN TESTING CHI TIẾT

[Testing guide đã được thêm vào phần trước]

---

## ✅ CHECKLIST TRIỂN KHAI

[Checklist đã được thêm vào phần trước]

---

## 📚 TÀI LIỆU THAM KHẢO
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
