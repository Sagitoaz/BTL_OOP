# 🌐 FRONTEND-BACKEND API INTEGRATION GUIDE

# Cách JavaFX UI gọi API từ Mini-Boot Backend

## 📋 OVERVIEW - Tổng quan

### 🏗️ **ARCHITECTURE:**

```
JavaFX Frontend (oop_ui)  ←→  REST API Backend (mini-boot)
     Port: Any                   Port: 8080
```

### 🔗 **CONNECTION FLOW:**

```
UI Controller → ApiClient → HTTP Request → Mini-Boot Server → Database → Response → UI Update
```

---

## 📂 1. API CLIENT LAYER

### 🛠️ **ApiClient.java** - HTTP Client Core

**📍 Location:** `oop_ui/src/main/java/org/example/oop/Utils/ApiClient.java`

```java
// Singleton pattern - Một instance cho toàn app
private static ApiClient instance;
private final HttpClient httpClient;

// Base URL configuration
private static final String BASE_URL = "http://localhost:8080";

// Khởi tạo HTTP client với timeout
this.httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();
```

### ⚡ **HTTP METHODS AVAILABLE:**

#### 🔍 **GET Request (Async):**

```java
public void getAsync(String endpoint, Consumer<ApiResponse<String>> onSuccess, Consumer<String> onError)

// Usage example:
apiClient.getAsync("/api/inventory", response -> {
    if (response.isSuccess()) {
        String jsonData = response.getData();
        // Parse JSON and update UI
    }
}, errorMessage -> {
    // Handle error
});
```

#### 📝 **POST Request (Async):**

```java
public void postAsync(String endpoint, String jsonBody, Consumer<ApiResponse<String>> onSuccess, Consumer<String> onError)

// Usage example:
String jsonBody = new ApiClient.JsonBuilder()
    .add("name", "Product Name")
    .add("price", 299.99)
    .build();

apiClient.postAsync("/api/inventory", jsonBody, response -> {
    // Handle success response
}, errorMessage -> {
    // Handle error
});
```

#### ✏️ **PUT Request (Async):**

```java
apiClient.putAsync("/api/inventory/1", jsonBody, response -> {
    // Handle update response
}, errorMessage -> {
    // Handle error
});
```

#### 🗑️ **DELETE Request (Async):**

```java
apiClient.deleteAsync("/api/inventory/1", response -> {
    // Handle delete response
}, errorMessage -> {
    // Handle error
});
```

---

## 📂 2. FRONTEND CONTROLLER IMPLEMENTATION

### 🎯 **AddInventoryController.java** - Main UI Controller

**📍 Location:** `oop_ui/src/main/java/org/example/oop/Control/AddInventoryController.java`

### 🔧 **INITIALIZATION:**

```java
public class AddInventoryController {
    // API Client instance
    private final ApiClient apiClient = ApiClient.getInstance();

    // Data storage
    private ObservableList<Inventory> allInventories;

    @FXML
    public void initialize() {
        loadData(); // Load data từ API khi controller khởi tạo
    }
}
```

### 📊 **1. LOAD DATA FROM BACKEND:**

```java
private void loadData() {
    updateStatus("🔄 Đang tải dữ liệu sản phẩm...");

    // GET request to load inventory
    apiClient.getAsync("/api/inventory", response -> {
        if (response.isSuccess()) {
            try {
                // Parse JSON response
                String jsonData = response.getData();
                allInventories = parseInventoryListFromJson(jsonData);

                // Update UI controls
                ObservableList<String> productNames = FXCollections.observableArrayList();
                for (Inventory inv : allInventories) {
                    productNames.add(inv.getName() + " (" + inv.getSku() + ")");
                }
                cbInitProduct.setItems(productNames);
                updateStatus("✅ Đã tải " + allInventories.size() + " sản phẩm");

            } catch (Exception e) {
                updateStatus("❌ Lỗi phân tích dữ liệu: " + e.getMessage());
            }
        } else {
            updateStatus("❌ Không thể tải dữ liệu: " + response.getErrorMessage());
        }
    }, errorMessage -> {
        updateStatus("❌ Lỗi kết nối API: " + errorMessage);
        // Fallback to empty list
        allInventories = FXCollections.observableArrayList();
    });
}
```

### 💾 **2. SAVE PRODUCT TO BACKEND:**

```java
@FXML
private void saveProduct() {
    try {
        if (!validateProductInput()) return;

        // Parse input values
        int priceCost = parseNonNegativeIntOrAlert(tfPriceCost, "Giá vốn", 0);
        int priceRetail = parseNonNegativeIntOrAlert(tfPriceRetail, "Giá bán lẻ", 0);

        // Build JSON payload
        String jsonBody = new ApiClient.JsonBuilder()
            .add("sku", tfSku.getText().trim())
            .add("name", tfName.getText().trim())
            .add("category", cbCategory.getValue())
            .add("unit", tfUnit.getText().trim())
            .add("priceCost", priceCost)
            .add("unitPrice", priceRetail)
            .add("minStock", 10)
            .add("maxStock", 1000)
            .add("description", taNote.getText().trim())
            .build();

        updateStatus("🔄 Đang lưu sản phẩm...");
        btnSaveProduct.setDisable(true);

        // POST request to save product
        apiClient.postAsync("/api/inventory", jsonBody, response -> {
            btnSaveProduct.setDisable(false);

            if (response.isSuccess()) {
                try {
                    // Parse response to get created product
                    Inventory inventory = parseInventoryFromJson(response.getData());
                    if (inventory != null) {
                        savedProduct = inventory;
                        tfId.setText(String.valueOf(inventory.getId()));

                        // Update local data and UI
                        allInventories.add(inventory);
                        String productDisplay = inventory.getName() + " (" + inventory.getSku() + ")";
                        cbInitProduct.getItems().add(productDisplay);
                        cbInitProduct.setValue(productDisplay);

                        updateStatus("✅ Đã lưu sản phẩm: " + inventory.getName() + " (ID: " + inventory.getId() + ")");
                    }
                } catch (Exception e) {
                    updateStatus("❌ Lỗi phân tích response: " + e.getMessage());
                }
            } else {
                updateStatus("❌ Không thể lưu sản phẩm: " + response.getErrorMessage());
            }
        }, errorMessage -> {
            btnSaveProduct.setDisable(false);
            updateStatus("❌ Lỗi kết nối: " + errorMessage);
        });

    } catch (Exception e) {
        updateStatus("❌ Lỗi: " + e.getMessage());
        btnSaveProduct.setDisable(false);
    }
}
```

### 📈 **3. SAVE INITIAL STOCK:**

```java
private void saveInitialStock() {
    try {
        if (!validateStockInput()) return;

        int productId = savedProduct.getId();
        int totalQuantity = calculateTotalQuantity();

        // Build JSON payload
        String jsonBody = new ApiClient.JsonBuilder()
            .add("quantity", totalQuantity)
            .add("note", "Nhập kho ban đầu từ UI")
            .build();

        String endpoint = "/api/inventory/" + productId + "/initial-stock";

        updateStatus("🔄 Đang lưu tồn kho ban đầu...");
        btnSaveInitStock.setDisable(true);

        // POST request to save initial stock
        apiClient.postAsync(endpoint, jsonBody, response -> {
            btnSaveInitStock.setDisable(false);

            if (response.isSuccess()) {
                updateStatus("✅ Đã lưu tồn kho ban đầu: " + totalQuantity + " " + savedProduct.getUnit());
                clearStockForm();
            } else {
                updateStatus("❌ Không thể lưu tồn kho: " + response.getErrorMessage());
            }
        }, errorMessage -> {
            btnSaveInitStock.setDisable(false);
            updateStatus("❌ Lỗi kết nối: " + errorMessage);
        });

    } catch (Exception e) {
        updateStatus("❌ Lỗi: " + e.getMessage());
        btnSaveInitStock.setDisable(false);
    }
}
```

---

## 📂 3. JSON HANDLING

### 🔨 **JSON BUILDER (Simple):**

```java
// Built-in JSON builder trong ApiClient
String jsonBody = new ApiClient.JsonBuilder()
    .add("name", "Product Name")
    .add("price", 299.99)
    .add("quantity", 50)
    .add("active", true)
    .build();

// Result: {"name":"Product Name","price":299.99,"quantity":50,"active":true}
```

### 🔍 **JSON PARSING (Manual):**

```java
private Inventory parseInventoryFromJson(String jsonData) {
    try {
        // Simple string parsing (no external JSON library)
        // Extract values using string manipulation
        int id = extractIntValue(jsonData, "id");
        String name = extractStringValue(jsonData, "name");
        String sku = extractStringValue(jsonData, "sku");
        // ... extract other fields

        return new Inventory(id, sku, name, category, ...);
    } catch (Exception e) {
        System.err.println("JSON parsing error: " + e.getMessage());
        return null;
    }
}
```

---

## 📂 4. THREADING & UI UPDATES

### ⚡ **JAVAFX PLATFORM THREADING:**

```java
// ApiClient automatically handles JavaFX threading
public void getAsync(String endpoint, Consumer<ApiResponse<String>> onSuccess, Consumer<String> onError) {
    CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    future.thenAccept(response -> {
        // Switch to JavaFX Application Thread for UI updates
        Platform.runLater(() -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                onSuccess.accept(ApiResponse.success(response.body()));
            } else {
                onError.accept("HTTP " + response.statusCode());
            }
        });
    }).exceptionally(throwable -> {
        Platform.runLater(() -> {
            onError.accept("Connection error: " + throwable.getMessage());
        });
        return null;
    });
}
```

### 🔄 **UI STATE MANAGEMENT:**

```java
// Disable controls during API calls
btnSaveProduct.setDisable(true);
updateStatus("🔄 Đang xử lý...");

// API call with callbacks
apiClient.postAsync(endpoint, data, response -> {
    // Re-enable controls on completion
    btnSaveProduct.setDisable(false);
    updateStatus("✅ Hoàn thành!");
}, error -> {
    // Re-enable controls on error
    btnSaveProduct.setDisable(false);
    updateStatus("❌ Lỗi: " + error);
});
```

---

## 📂 5. CONFIGURATION

### ⚙️ **ApiConfig.java:**

```java
public class ApiConfig {
    // Server configuration
    private static final String DEV_BASE_URL = "http://localhost:8080";
    private static final String PROD_BASE_URL = "http://production-server:8080";

    // Timeout settings
    public static final int CONNECTION_TIMEOUT = 10; // seconds
    public static final int REQUEST_TIMEOUT = 30;    // seconds

    public static String getBaseUrl() {
        return DEV_BASE_URL; // Switch for different environments
    }

    // Endpoint builders
    public static String inventoryEndpoint() { return "/api/inventory"; }
    public static String inventoryEndpoint(long id) { return "/api/inventory/" + id; }
    public static String stockMovementEndpoint() { return "/api/stock-movements"; }
    public static String alertsEndpoint() { return "/api/alerts"; }
}
```

---

## 📊 6. CURRENT API ENDPOINTS USAGE

### ✅ **WORKING ENDPOINTS:**

```java
// GET requests (working perfectly)
GET /api/inventory              → Load all products
GET /api/inventory/{id}         → Get product by ID
GET /api/stock-movements        → Load movements
GET /api/alerts                 → Load alerts
GET /api/alerts/stats           → Alert statistics

// POST endpoints (some working)
POST /api/alerts/check          → Generate alerts ✅
POST /api/inventory             → Create product ❌ (405 Method Not Allowed)
POST /api/stock-movements       → Create movement ❌ (405 Method Not Allowed)
POST /api/inventory/{id}/initial-stock → Set initial stock ❌ (405 Method Not Allowed)
```

### 🚫 **CURRENT ISSUE:**

- **GET methods**: 100% working
- **POST/PUT/DELETE methods**: 405 Method Not Allowed
- **Root cause**: Router configuration issue in mini-boot server

---

## 🎯 7. COMPLETE WORKFLOW EXAMPLE

### 📋 **TYPICAL USER WORKFLOW:**

```java
1. User opens AddInventoryController
   ↓
2. initialize() calls loadData()
   ↓
3. loadData() → GET /api/inventory → Updates product dropdown
   ↓
4. User fills product form and clicks Save
   ↓
5. saveProduct() → POST /api/inventory → Creates product
   ↓
6. User fills stock form and clicks Save Initial Stock
   ↓
7. saveInitialStock() → POST /api/inventory/{id}/initial-stock → Sets stock
   ↓
8. UI updates with success message and new data
```

### 💫 **SUCCESS CASE:**

```
🔄 Loading... → ✅ Loaded 25 products → User input → 🔄 Saving... → ✅ Product saved (ID: 26)
```

### ❌ **ERROR CASE (Current):**

```
🔄 Loading... → ✅ Loaded 0 products → User input → 🔄 Saving... → ❌ 405 Method Not Allowed
```

---

## 🔧 8. DEBUGGING & TESTING

### 🧪 **Test Individual API Calls:**

```java
// Test in initialize() method
private void testApiConnection() {
    apiClient.getAsync("/api/inventory", response -> {
        System.out.println("✅ GET working: " + response.getData());
    }, error -> {
        System.out.println("❌ GET failed: " + error);
    });

    String testJson = new ApiClient.JsonBuilder().add("test", "value").build();
    apiClient.postAsync("/api/inventory", testJson, response -> {
        System.out.println("✅ POST working: " + response.getData());
    }, error -> {
        System.out.println("❌ POST failed: " + error);
    });
}
```

### 🔍 **Monitor Network Traffic:**

```java
// Add logging to ApiClient
System.out.println("📤 Sending: " + request.method() + " " + request.uri());
System.out.println("📤 Body: " + requestBody);
System.out.println("📥 Response: " + response.statusCode() + " - " + response.body());
```

---

## 🎉 SUMMARY

### ✅ **WHAT'S WORKING:**

- **ApiClient**: Complete HTTP client with async operations
- **GET APIs**: Load inventory, movements, alerts perfectly
- **UI Integration**: Real-time updates, error handling, progress indicators
- **Threading**: Proper JavaFX Platform.runLater() usage

### 🔧 **WHAT NEEDS FIXING:**

- **POST/PUT/DELETE**: Method Not Allowed (405) error
- **Server Config**: Router mounting issue in mini-boot
- **Data Creation**: Cannot create new products/movements via UI

### 🚀 **ONCE FIXED:**

Frontend sẽ có **complete CRUD functionality** với backend qua REST APIs!

**🏆 Architecture is solid, just need to fix backend router configuration! 🏆**
