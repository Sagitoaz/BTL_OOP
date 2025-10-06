# 📚 HƯỚNG DẪN CHI TIẾT SỬ DỤNG - NGÀY 1

## 🎯 TỔNG QUAN

File này giải thích chi tiết công dụng và cách sử dụng của **15 FILES** đã tạo trong NGÀY 1.

---

## 🏷️ PHẦN 1: ENUM CLASSES (5 files)

### **1. InventoryStatus.java - Trạng thái Sản phẩm**

**📌 Mục đích:**

- Định nghĩa các trạng thái chuẩn của sản phẩm trong kho
- Tránh hard-code string → dễ maintain, validate
- Hỗ trợ đa ngôn ngữ (code + displayName)

**📊 Các giá trị:**

```
ACTIVE          → Đang hoạt động (sản phẩm bán bình thường)
DISCONTINUED    → Ngừng kinh doanh (không nhập thêm nhưng vẫn bán hết tồn)
OUT_OF_STOCK    → Hết hàng hoàn toàn (quantity = 0)
LOW_STOCK       → Sắp hết hàng (quantity <= reorderLevel)
```

**💡 Cách sử dụng:**

```java
// 1. Gán trạng thái cho sản phẩm
Inventory product = new Inventory();
product.setStatus(InventoryStatus.ACTIVE.getCode());

// 2. Chuyển từ String sang Enum
String statusFromDB = "Active";
InventoryStatus status = InventoryStatus.fromCode(statusFromDB);

// 3. Hiển thị trong ComboBox (JavaFX)
ComboBox<InventoryStatus> statusCombo = new ComboBox<>();
statusCombo.getItems().addAll(InventoryStatus.values());
statusCombo.getSelectionModel().select(InventoryStatus.ACTIVE);

// 4. Kiểm tra trạng thái
if (status == InventoryStatus.LOW_STOCK) {
    System.out.println("⚠️ Cảnh báo: " + product.getName() + " sắp hết hàng!");
}

// 5. Lấy tên hiển thị
String displayName = status.getDisplayName(); // "Đang hoạt động"
String code = status.getCode();               // "Active"
```

---

### **2. Category.java - Danh mục Sản phẩm**

**📌 Mục đích:**

- Phân loại sản phẩm theo nhóm nghiệp vụ
- Hỗ trợ filtering, searching theo category
- Quản lý inventory theo từng nhóm

**📊 Các giá trị:**

```
MEDICATION      → Thuốc (Paracetamol, Amoxicillin...)
EQUIPMENT       → Thiết bị (Máy siêu âm, Máy X-quang...)
SUPPLIES        → Vật tư (Khẩu trang, Găng tay...)
CONSUMABLES     → Hàng tiêu hao (Kim tiêm, Băng gạc...)
```

**💡 Cách sử dụng:**

```java
// 1. Gán category cho sản phẩm
Inventory product = new Inventory();
product.setCategory(Category.MEDICATION.getCode());

// 2. Filter theo category
List<Inventory> medications = inventoryList.stream()
    .filter(p -> p.getCategory().equals(Category.MEDICATION.getCode()))
    .collect(Collectors.toList());

// 3. Populate ComboBox
ComboBox<Category> categoryBox = new ComboBox<>();
categoryBox.getItems().addAll(Category.values());

// 4. Validate category
String categoryInput = "Equipment";
try {
    Category cat = Category.fromCode(categoryInput);
    System.out.println("Valid category: " + cat.getDisplayName());
} catch (Exception e) {
    System.out.println("Invalid category!");
}

// 5. Group by category (báo cáo)
Map<String, List<Inventory>> grouped = inventoryList.stream()
    .collect(Collectors.groupingBy(Inventory::getCategory));
```

---

### **3. MovementType.java - Loại Giao dịch Kho**

**📌 Mục đích:**

- Định nghĩa các loại giao dịch xuất/nhập kho
- Tracking lịch sử thay đổi số lượng
- Audit trail cho inventory

**📊 Các giá trị:**

```
IN          → Nhập kho (từ nhà cung cấp, sản xuất...)
OUT         → Xuất kho (bán hàng, sử dụng nội bộ...)
ADJUSTMENT  → Điều chỉnh (kiểm kê phát hiện sai lệch, hư hỏng...)
RETURN      → Trả hàng (khách trả lại, trả nhà cung cấp...)
```

**💡 Cách sử dụng:**

```java
// 1. Record stock movement khi nhập hàng
StockMovement movement = new StockMovement();
movement.setMovementType(MovementType.IN.getCode());
movement.setProductId(1);
movement.setQuantityBefore(100);
movement.setQuantityChange(50);
movement.setQuantityAfter(150);
movement.setReason("Nhập hàng từ PO-2025-001");

// 2. Xuất hàng khi bán
StockMovement saleMovement = new StockMovement();
saleMovement.setMovementType(MovementType.OUT.getCode());
saleMovement.setQuantityBefore(150);
saleMovement.setQuantityChange(30);
saleMovement.setQuantityAfter(120);
saleMovement.setReference("INV-12345");

// 3. Filter movements theo loại
List<StockMovement> imports = movementList.stream()
    .filter(m -> m.getMovementType().equals(MovementType.IN.getCode()))
    .collect(Collectors.toList());

// 4. Calculate total IN/OUT
int totalIn = movementList.stream()
    .filter(m -> m.getMovementType().equals(MovementType.IN.getCode()))
    .mapToInt(StockMovement::getQuantityChange)
    .sum();

int totalOut = movementList.stream()
    .filter(m -> m.getMovementType().equals(MovementType.OUT.getCode()))
    .mapToInt(StockMovement::getQuantityChange)
    .sum();
```

---

### **4. PurchaseOrderStatus.java - Trạng thái Đơn đặt hàng**

**📌 Mục đích:**

- Quản lý workflow của Purchase Order
- Track tiến độ từ tạo đơn → nhận hàng
- Hỗ trợ approval process

**📊 Các giá trị:**

```
DRAFT       → Bản nháp (chưa submit)
PENDING     → Chờ duyệt (đã submit, chờ manager approve)
APPROVED    → Đã duyệt (chờ gửi cho supplier)
RECEIVED    → Đã nhận hàng (hoàn thành)
CANCELLED   → Đã hủy
```

**💡 Cách sử dụng:**

```java
// 1. Workflow tạo PO
PurchaseOrder po = new PurchaseOrder();
po.setStatus(PurchaseOrderStatus.DRAFT.getCode());
po.setPoNumber("PO-2025-" + String.format("%03d", nextId));

// 2. Submit để chờ duyệt
if (validatePO(po)) {
    po.setStatus(PurchaseOrderStatus.PENDING.getCode());
    savePO(po);
    sendApprovalRequest(po);
}

// 3. Manager approve
if (isManager && po.getStatus().equals(PurchaseOrderStatus.PENDING.getCode())) {
    po.setStatus(PurchaseOrderStatus.APPROVED.getCode());
    sendToSupplier(po);
}

// 4. Nhận hàng → update stock
if (po.getStatus().equals(PurchaseOrderStatus.APPROVED.getCode())) {
    receiveGoods(po);
    po.setStatus(PurchaseOrderStatus.RECEIVED.getCode());
    po.setReceivedDate(LocalDate.now());

    // Update inventory
    for (PurchaseOrderItem item : po.getItems()) {
        updateStock(item.getProductId(), item.getQuantityReceived());
    }
}

// 5. Filter PO chờ duyệt
List<PurchaseOrder> pendingPOs = poList.stream()
    .filter(p -> p.getStatus().equals(PurchaseOrderStatus.PENDING.getCode()))
    .collect(Collectors.toList());
```

---

### **5. SupplierStatus.java - Trạng thái Nhà cung cấp**

**📌 Mục đích:**

- Quản lý trạng thái hoạt động của supplier
- Filter suppliers khi tạo PO
- Soft delete (không xóa hẳn record)

**📊 Các giá trị:**

```
ACTIVE      → Đang hợp tác (có thể tạo PO mới)
INACTIVE    → Ngừng hợp tác (không tạo PO mới, nhưng giữ lịch sử)
```

**💡 Cách sử dụng:**

```java
// 1. Tạo supplier mới
Supplier supplier = new Supplier();
supplier.setCode("SUP-" + String.format("%03d", nextId));
supplier.setName("Công ty ABC");
supplier.setStatus(SupplierStatus.ACTIVE.getCode());

// 2. Load danh sách active suppliers cho dropdown
List<Supplier> activeSuppliers = supplierList.stream()
    .filter(s -> s.getStatus().equals(SupplierStatus.ACTIVE.getCode()))
    .collect(Collectors.toList());

ComboBox<Supplier> supplierBox = new ComboBox<>();
supplierBox.getItems().addAll(activeSuppliers);

// 3. Deactivate supplier (soft delete)
public void deactivateSupplier(int supplierId) {
    Supplier supplier = findById(supplierId);
    supplier.setStatus(SupplierStatus.INACTIVE.getCode());
    save(supplier);
    // Không xóa record → giữ lịch sử PO
}

// 4. Validate khi tạo PO
if (!supplier.getStatus().equals(SupplierStatus.ACTIVE.getCode())) {
    throw new Exception("Không thể tạo PO cho supplier không hoạt động!");
}

// 5. Report theo status
long activeCount = supplierList.stream()
    .filter(s -> s.getStatus().equals(SupplierStatus.ACTIVE.getCode()))
    .count();
```

---

## 📦 PHẦN 2: MODEL CLASSES (6 files)

### **6. Inventory.java - Model Sản phẩm chính**

**📌 Mục đích:**

- Đại diện cho 1 sản phẩm trong kho
- Chứa đầy đủ thông tin sản phẩm
- Sử dụng cho business logic

**📊 Các trường quan trọng:**

```java
id              → ID duy nhất
sku             → Mã SKU (Stock Keeping Unit) - VD: MED-001
name            → Tên sản phẩm
category        → Danh mục (Medication, Equipment...)
quantity        → Số lượng tồn kho hiện tại
unitPrice       → Giá bán lẻ
price_cost      → Giá nhập (cost)
reorderLevel    → Ngưỡng cảnh báo (VD: 10 → cảnh báo khi còn ≤10)
reorderQuantity → Số lượng đặt lại mặc định
supplier        → Nhà cung cấp
status          → Trạng thái (Active, Discontinued...)
```

**💡 Cách sử dụng:**

```java
// 1. Tạo sản phẩm mới
Inventory product = new Inventory();
product.setId(generateNextId());
product.setSku("MED-" + String.format("%04d", product.getId()));
product.setName("Paracetamol 500mg");
product.setCategory(Category.MEDICATION.getCode());
product.setQuantity(100);
product.setUnit("tablet");
product.setUnitPrice(1500);
product.setPrice_cost(1000);
product.setReorderLevel(20);
product.setReorderQuantity(100);
product.setSupplier("Công ty Dược ABC");
product.setActive(true);
product.setCreatedAt(LocalDateTime.now());

// 2. Check low stock
public boolean isLowStock(Inventory product) {
    return product.getQuantity() <= (product.getReorderLevel() != null ? product.getReorderLevel() : 0);
}

// 3. Calculate profit margin
public double calculateProfit(Inventory product, int quantitySold) {
    double cost = product.getPrice_cost() != null ? product.getPrice_cost() : 0;
    double revenue = product.getUnitPrice() * quantitySold;
    double totalCost = cost * quantitySold;
    return revenue - totalCost;
}

// 4. Update quantity
public void updateQuantity(Inventory product, int change, String reason) {
    int oldQty = product.getQuantity();
    int newQty = oldQty + change;

    if (newQty < 0) {
        throw new Exception("Không đủ hàng trong kho!");
    }

    product.setQuantity(newQty);
    product.setLastUpdated(LocalDateTime.now());

    // Record movement
    recordStockMovement(product.getId(), oldQty, change, newQty, reason);
}

// 5. Check cần reorder không
public List<Inventory> getProductsNeedReorder() {
    return inventoryList.stream()
        .filter(p -> p.getQuantity() <= p.getReorderLevel())
        .filter(p -> p.isActive())
        .collect(Collectors.toList());
}
```

---

### **7. InventoryRow.java - Model hiển thị trong Table**

**📌 Mục đích:**

- Model đơn giản hóa cho TableView (JavaFX)
- Chỉ chứa fields cần hiển thị
- Có thêm computed fields (stockStatus)

**📊 Khác biệt với Inventory:**

```
Inventory       → Full model (business logic)
InventoryRow    → Display model (UI only)

InventoryRow có thêm:
- stockStatus   → "LOW_STOCK", "IN_STOCK", "OUT_OF_STOCK"
- Auto calculate stock status
```

**💡 Cách sử dụng:**

```java
// 1. Convert Inventory → InventoryRow (cho display)
public InventoryRow toInventoryRow(Inventory inv) {
    return new InventoryRow(
        inv.getId(),
        inv.getSku(),
        inv.getName(),
        inv.getType(),
        inv.getCategory(),
        inv.getQuantity(),
        inv.getUnit(),
        inv.getUnitPrice(),
        inv.getLastUpdated().toLocalDate(),
        inv.getSupplier(),
        inv.isActive() ? "ACTIVE" : "INACTIVE",
        inv.getReorderLevel(),
        calculateStockStatus(inv)
    );
}

// 2. Display trong TableView
TableView<InventoryRow> table = new TableView<>();
TableColumn<InventoryRow, String> nameCol = new TableColumn<>("Name");
nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));

TableColumn<InventoryRow, Integer> qtyCol = new TableColumn<>("Quantity");
qtyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));

// 3. Color coding theo stock status
qtyCol.setCellFactory(column -> new TableCell<InventoryRow, Integer>() {
    @Override
    protected void updateItem(Integer quantity, boolean empty) {
        super.updateItem(quantity, empty);
        if (empty || quantity == null) {
            setText(null);
            setStyle("");
        } else {
            setText(quantity.toString());
            InventoryRow row = getTableRow().getItem();
            if (row != null) {
                switch (row.getStockStatus()) {
                    case "OUT_OF_STOCK":
                        setStyle("-fx-background-color: #ffcccc;"); // Red
                        break;
                    case "LOW_STOCK":
                        setStyle("-fx-background-color: #ffffcc;"); // Yellow
                        break;
                    case "IN_STOCK":
                        setStyle("-fx-background-color: #ccffcc;"); // Green
                        break;
                }
            }
        }
    }
});

// 4. Filter by stock status
FilteredList<InventoryRow> filtered = new FilteredList<>(inventoryRows);
filtered.setPredicate(row -> {
    if (showLowStockOnly) {
        return "LOW_STOCK".equals(row.getStockStatus());
    }
    return true;
});
table.setItems(filtered);
```

---

### **8. StockMovement.java - Lịch sử Giao dịch Kho**

**📌 Mục đích:**

- Audit trail cho mọi thay đổi về quantity
- Traceability (ai, khi nào, tại sao thay đổi)
- Báo cáo xuất/nhập kho

**📊 Các trường quan trọng:**

```java
productId        → Sản phẩm nào
movementType     → IN/OUT/ADJUSTMENT/RETURN
quantityBefore   → Số lượng trước khi thay đổi
quantityChange   → Số lượng thay đổi (+/-)
quantityAfter    → Số lượng sau khi thay đổi
reason           → Lý do (VD: "Nhập hàng", "Bán cho khách")
reference        → Mã tham chiếu (PO number, Invoice number)
movedAt          → Thời gian
movedBy          → User thực hiện
```

**💡 Cách sử dụng:**

```java
// 1. Record khi nhập hàng từ PO
public void receiveGoods(PurchaseOrder po) {
    for (PurchaseOrderItem item : po.getItems()) {
        Inventory product = findProductById(item.getProductId());

        StockMovement movement = new StockMovement();
        movement.setId(generateId());
        movement.setProductId(product.getId());
        movement.setMovementType(MovementType.IN.getCode());
        movement.setQuantityBefore(product.getQuantity());
        movement.setQuantityChange(item.getQuantityReceived());
        movement.setQuantityAfter(product.getQuantity() + item.getQuantityReceived());
        movement.setReason("Nhập hàng từ nhà cung cấp");
        movement.setReference(po.getPoNumber());
        movement.setMovedAt(LocalDateTime.now());
        movement.setMovedBy(getCurrentUser().getId());

        saveStockMovement(movement);

        // Update product quantity
        product.setQuantity(movement.getQuantityAfter());
        saveProduct(product);
    }
}

// 2. Record khi xuất bán
public void sellProduct(int productId, int quantity, String invoiceNumber) {
    Inventory product = findProductById(productId);

    if (product.getQuantity() < quantity) {
        throw new Exception("Không đủ hàng trong kho!");
    }

    StockMovement movement = new StockMovement();
    movement.setProductId(productId);
    movement.setMovementType(MovementType.OUT.getCode());
    movement.setQuantityBefore(product.getQuantity());
    movement.setQuantityChange(quantity);
    movement.setQuantityAfter(product.getQuantity() - quantity);
    movement.setReason("Bán cho khách hàng");
    movement.setReference(invoiceNumber);
    movement.setMovedAt(LocalDateTime.now());
    movement.setMovedBy(getCurrentUser().getId());

    saveStockMovement(movement);
    product.setQuantity(movement.getQuantityAfter());
    saveProduct(product);
}

// 3. Xem lịch sử 1 sản phẩm
public List<StockMovement> getProductHistory(int productId) {
    return stockMovements.stream()
        .filter(m -> m.getProductId() == productId)
        .sorted(Comparator.comparing(StockMovement::getMovedAt).reversed())
        .collect(Collectors.toList());
}

// 4. Báo cáo xuất nhập theo tháng
public Map<String, Integer> getMonthlyReport(int month, int year) {
    LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
    LocalDateTime endDate = startDate.plusMonths(1);

    int totalIn = stockMovements.stream()
        .filter(m -> m.getMovedAt().isAfter(startDate) && m.getMovedAt().isBefore(endDate))
        .filter(m -> m.getMovementType().equals(MovementType.IN.getCode()))
        .mapToInt(StockMovement::getQuantityChange)
        .sum();

    int totalOut = stockMovements.stream()
        .filter(m -> m.getMovedAt().isAfter(startDate) && m.getMovedAt().isBefore(endDate))
        .filter(m -> m.getMovementType().equals(MovementType.OUT.getCode()))
        .mapToInt(StockMovement::getQuantityChange)
        .sum();

    return Map.of("IN", totalIn, "OUT", totalOut);
}
```

---

### **9. Supplier.java - Nhà cung cấp**

**📌 Mục đích:**

- Quản lý thông tin nhà cung cấp
- Link với Purchase Order
- Track supplier performance

**📊 Các trường quan trọng:**

```java
code            → Mã supplier (SUP-001)
name            → Tên công ty
contactPerson   → Người liên hệ
email, phone    → Thông tin liên lạc
address         → Địa chỉ
status          → ACTIVE/INACTIVE
paymentTerms    → Điều khoản thanh toán (NET30, NET60)
notes           → Ghi chú
```

**💡 Cách sử dụng:**

```java
// 1. Tạo supplier mới
Supplier supplier = new Supplier();
supplier.setId(generateId());
supplier.setCode("SUP-" + String.format("%03d", supplier.getId()));
supplier.setName("Công ty Dược phẩm ABC");
supplier.setContactPerson("Nguyễn Văn A");
supplier.setEmail("a.nguyen@abc.com");
supplier.setPhone("0901234567");
supplier.setAddress("123 Lê Lợi, Q1, HCM");
supplier.setStatus(SupplierStatus.ACTIVE.getCode());
supplier.setPaymentTerms("NET30");
saveSupplier(supplier);

// 2. ComboBox cho PO
ComboBox<Supplier> supplierBox = new ComboBox<>();
List<Supplier> activeSuppliers = loadActiveSuppliers();
supplierBox.getItems().addAll(activeSuppliers);
supplierBox.setConverter(new StringConverter<Supplier>() {
    @Override
    public String toString(Supplier supplier) {
        return supplier != null ? supplier.getName() : "";
    }

    @Override
    public Supplier fromString(String string) {
        return null;
    }
});

// 3. Calculate payment due date
public LocalDate calculateDueDate(PurchaseOrder po, Supplier supplier) {
    String terms = supplier.getPaymentTerms();
    int days = Integer.parseInt(terms.replace("NET", ""));
    return po.getOrderDate().plusDays(days);
}

// 4. Supplier performance report
public Map<String, Object> getSupplierPerformance(int supplierId, int year) {
    List<PurchaseOrder> orders = poList.stream()
        .filter(po -> po.getSupplierId() == supplierId)
        .filter(po -> po.getOrderDate().getYear() == year)
        .collect(Collectors.toList());

    double totalAmount = orders.stream()
        .mapToDouble(PurchaseOrder::getTotalAmount)
        .sum();

    long onTimeDeliveries = orders.stream()
        .filter(po -> po.getReceivedDate() != null)
        .filter(po -> !po.getReceivedDate().isAfter(po.getExpectedDate()))
        .count();

    double onTimeRate = orders.isEmpty() ? 0 : (double)onTimeDeliveries / orders.size() * 100;

    return Map.of(
        "totalOrders", orders.size(),
        "totalAmount", totalAmount,
        "onTimeRate", onTimeRate
    );
}
```

---

### **10. PurchaseOrder.java - Đơn đặt hàng**

**📌 Mục đích:**

- Quản lý đơn đặt hàng từ nhà cung cấp
- Track workflow từ draft → received
- Link với Inventory update

**📊 Các trường quan trọng:**

```java
poNumber        → Mã PO (PO-2025-001)
supplierId      → Nhà cung cấp
orderDate       → Ngày đặt hàng
expectedDate    → Ngày dự kiến nhận
receivedDate    → Ngày thực tế nhận
status          → DRAFT/PENDING/APPROVED/RECEIVED/CANCELLED
totalAmount     → Tổng tiền
items           → List các sản phẩm trong đơn
```

**💡 Cách sử dụng:**

```java
// 1. Tạo PO mới (DRAFT)
PurchaseOrder po = new PurchaseOrder();
po.setId(generateId());
po.setPoNumber("PO-2025-" + String.format("%03d", po.getId()));
po.setSupplierId(selectedSupplier.getId());
po.setSupplierName(selectedSupplier.getName());
po.setOrderDate(LocalDate.now());
po.setExpectedDate(LocalDate.now().plusDays(7));
po.setStatus(PurchaseOrderStatus.DRAFT.getCode());
po.setCurrency("VND");

// 2. Thêm items vào PO
PurchaseOrderItem item1 = new PurchaseOrderItem();
item1.setProductId(1);
item1.setProductName("Paracetamol 500mg");
item1.setQuantityOrdered(100);
item1.setUnitPrice(1500);
item1.calculateTotal();

po.addItem(item1); // Tự động calculate total
savePO(po);

// 3. Submit để duyệt
public void submitForApproval(PurchaseOrder po) {
    if (po.getItems().isEmpty()) {
        throw new Exception("PO phải có ít nhất 1 item!");
    }

    po.setStatus(PurchaseOrderStatus.PENDING.getCode());
    savePO(po);

    // Send notification to manager
    notifyManager("PO " + po.getPoNumber() + " chờ duyệt");
}

// 4. Approve PO
public void approvePO(PurchaseOrder po) {
    if (!isManager()) {
        throw new Exception("Chỉ manager mới được duyệt PO!");
    }

    po.setStatus(PurchaseOrderStatus.APPROVED.getCode());
    savePO(po);

    // Send to supplier
    sendEmailToSupplier(po);
}

// 5. Receive goods
public void receiveGoods(PurchaseOrder po, Map<Integer, Integer> receivedQuantities) {
    for (PurchaseOrderItem item : po.getItems()) {
        int receivedQty = receivedQuantities.getOrDefault(item.getProductId(), 0);
        item.setQuantityReceived(receivedQty);

        // Update inventory
        if (receivedQty > 0) {
            updateInventoryQuantity(item.getProductId(), receivedQty);
            recordStockMovement(item.getProductId(), receivedQty,
                MovementType.IN, po.getPoNumber());
        }
    }

    // Check nếu nhận đủ hết
    boolean allReceived = po.getItems().stream()
        .allMatch(i -> i.getQuantityReceived() >= i.getQuantityOrdered());

    if (allReceived) {
        po.setStatus(PurchaseOrderStatus.RECEIVED.getCode());
        po.setReceivedDate(LocalDate.now());
    }

    savePO(po);
}
```

---

### **11. PurchaseOrderItem.java - Chi tiết Đơn hàng**

**📌 Mục đích:**

- Đại diện cho 1 dòng trong PO
- Track quantity ordered vs received
- Calculate line total

**📊 Các trường quan trọng:**

```java
poId                → ID của PO
productId           → Sản phẩm
quantityOrdered     → Số lượng đặt
quantityReceived    → Số lượng thực nhận
unitPrice           → Giá nhập
totalPrice          → Thành tiền (auto calculate)
```

**💡 Cách sử dụng:**

```java
// 1. Tạo item cho PO
PurchaseOrderItem item = new PurchaseOrderItem();
item.setPoId(po.getId());
item.setProductId(selectedProduct.getId());
item.setProductName(selectedProduct.getName());
item.setQuantityOrdered(100);
item.setUnitPrice(1500);
item.calculateTotal(); // totalPrice = 100 * 1500 = 150,000

// 2. Display trong TableView
TableView<PurchaseOrderItem> itemsTable = new TableView<>();
TableColumn<PurchaseOrderItem, String> nameCol = new TableColumn<>("Product");
nameCol.setCellValueFactory(c ->
    new ReadOnlyStringWrapper(c.getValue().getProductName()));

TableColumn<PurchaseOrderItem, Integer> qtyCol = new TableColumn<>("Qty");
qtyCol.setCellValueFactory(c ->
    new ReadOnlyObjectWrapper<>(c.getValue().getQuantityOrdered()));

TableColumn<PurchaseOrderItem, Double> totalCol = new TableColumn<>("Total");
totalCol.setCellValueFactory(c ->
    new ReadOnlyObjectWrapper<>(c.getValue().getTotalPrice()));

// 3. Editable quantity trong table
qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
qtyCol.setOnEditCommit(event -> {
    PurchaseOrderItem item = event.getRowValue();
    item.setQuantityOrdered(event.getNewValue());
    item.calculateTotal();
    po.calculateTotal(); // Update PO total
    itemsTable.refresh();
});

// 4. Add/Remove items
Button addBtn = new Button("Add Item");
addBtn.setOnAction(e -> {
    PurchaseOrderItem newItem = showProductSelector();
    if (newItem != null) {
        po.addItem(newItem); // Auto calculate total
        itemsTable.getItems().add(newItem);
    }
});

Button removeBtn = new Button("Remove");
removeBtn.setOnAction(e -> {
    PurchaseOrderItem selected = itemsTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
        po.removeItem(selected);
        itemsTable.getItems().remove(selected);
    }
});

// 5. Validate received quantity
public void validateReceiving(PurchaseOrderItem item, int receivedQty) {
    if (receivedQty > item.getQuantityOrdered()) {
        boolean confirm = showConfirmDialog(
            "Nhận nhiều hơn đặt. Bạn có chắc không?");
        if (!confirm) {
            throw new Exception("Cancelled by user");
        }
    }
    item.setQuantityReceived(receivedQty);
}
```

---

## 📄 PHẦN 3: DATA FILES (4 files)

### **12. suppliers.txt - Dữ liệu Nhà cung cấp**

**📌 Format:**

```
id|code|name|contactPerson|email|phone|address|status|paymentTerms|notes
```

**📊 Sample data:**

```
1|SUP-001|Công ty Dược phẩm ABC|Nguyễn Văn A|a.nguyen@abc.com|0901234567|123 Lê Lợi, Q1, HCM|ACTIVE|NET30|Nhà cung cấp thuốc chính
```

**💡 Cách sử dụng:**

```java
// 1. Read suppliers từ file
public List<Supplier> loadSuppliers() throws IOException {
    List<Supplier> suppliers = new ArrayList<>();
    String filePath = "TestData/suppliers.txt";

    try (BufferedReader reader = new BufferedReader(
            new FileReader(filePath))) {
        String line = reader.readLine(); // Skip header

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|");
            Supplier supplier = new Supplier(
                Integer.parseInt(parts[0]),  // id
                parts[1],                     // code
                parts[2],                     // name
                parts[3],                     // contactPerson
                parts[4],                     // email
                parts[5],                     // phone
                parts[6],                     // address
                parts[7],                     // status
                parts[8],                     // paymentTerms
                parts[9]                      // notes
            );
            suppliers.add(supplier);
        }
    }
    return suppliers;
}

// 2. Save supplier
public void saveSupplier(Supplier supplier) throws IOException {
    List<Supplier> suppliers = loadSuppliers();

    // Check if exists → update, else add
    boolean found = false;
    for (int i = 0; i < suppliers.size(); i++) {
        if (suppliers.get(i).getId() == supplier.getId()) {
            suppliers.set(i, supplier);
            found = true;
            break;
        }
    }

    if (!found) {
        suppliers.add(supplier);
    }

    // Write to file
    try (BufferedWriter writer = new BufferedWriter(
            new FileWriter("TestData/suppliers.txt"))) {
        writer.write("Format: id|code|name|contactPerson|email|phone|address|status|paymentTerms|notes\n");

        for (Supplier s : suppliers) {
            writer.write(String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s\n",
                s.getId(), s.getCode(), s.getName(), s.getContactPerson(),
                s.getEmail(), s.getPhone(), s.getAddress(), s.getStatus(),
                s.getPaymentTerms(), s.getNotes()));
        }
    }
}
```

---

### **13. stock_movements.txt - Lịch sử Giao dịch**

**📌 Format:**

```
id|productId|type|quantityBefore|quantityChange|quantityAfter|reason|reference|movedAt|movedBy|notes
```

**📊 Sample data:**

```
1|1|IN|0|100|100|Nhập hàng đầu kỳ|PO-2025-001|2025-10-01T08:00:00|admin|Nhập kho lần đầu
```

**💡 Cách sử dụng:**

```java
// 1. Load movements
public List<StockMovement> loadStockMovements() throws IOException {
    List<StockMovement> movements = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(
            new FileReader("TestData/stock_movements.txt"))) {
        reader.readLine(); // Skip header

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|");
            StockMovement movement = new StockMovement(
                Integer.parseInt(parts[0]),              // id
                Integer.parseInt(parts[1]),              // productId
                parts[2],                                 // type
                Integer.parseInt(parts[3]),              // quantityBefore
                Integer.parseInt(parts[4]),              // quantityChange
                Integer.parseInt(parts[5]),              // quantityAfter
                parts[6],                                 // reason
                parts[7],                                 // reference
                LocalDateTime.parse(parts[8]),           // movedAt
                parts[9],                                 // movedBy
                parts[10]                                 // notes
            );
            movements.add(movement);
        }
    }
    return movements;
}

// 2. Append new movement (không đọc lại toàn bộ file)
public void appendStockMovement(StockMovement movement) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(
            new FileWriter("TestData/stock_movements.txt", true))) { // append mode
        writer.write(String.format("%d|%d|%s|%d|%d|%d|%s|%s|%s|%s|%s\n",
            movement.getId(),
            movement.getProductId(),
            movement.getMovementType(),
            movement.getQuantityBefore(),
            movement.getQuantityChange(),
            movement.getQuantityAfter(),
            movement.getReason(),
            movement.getReference(),
            movement.getMovedAt(),
            movement.getMovedBy(),
            movement.getNotes()));
    }
}

// 3. Query movements
public List<StockMovement> getMovementsByProduct(int productId) {
    return loadStockMovements().stream()
        .filter(m -> m.getProductId() == productId)
        .sorted(Comparator.comparing(StockMovement::getMovedAt).reversed())
        .collect(Collectors.toList());
}
```

---

### **14. purchase_orders.txt - Đơn đặt hàng**

**📌 Format:**

```
id|poNumber|supplierId|supplierName|orderDate|expectedDate|receivedDate|status|totalAmount|currency|notes
```

**💡 Sử dụng tương tự suppliers.txt**

---

### **15. purchase_order_items.txt - Chi tiết Đơn hàng**

**📌 Format:**

```
id|poId|productId|productName|quantityOrdered|quantityReceived|unitPrice|totalPrice
```

**💡 Cách sử dụng:**

```java
// Load items for a PO
public List<PurchaseOrderItem> getItemsByPO(int poId) {
    return loadAllItems().stream()
        .filter(item -> item.getPoId() == poId)
        .collect(Collectors.toList());
}

// Load PO with items
public PurchaseOrder loadPOWithItems(int poId) {
    PurchaseOrder po = findPOById(poId);
    List<PurchaseOrderItem> items = getItemsByPO(poId);
    po.setItems(items);
    return po;
}
```

---

## 🎯 KẾT LUẬN

### **Mối quan hệ giữa các files:**

```
Supplier ──────┬──> PurchaseOrder ──> PurchaseOrderItem ──> Inventory
               │                                               │
               └───────────────────────────────────────────────┘
                                                               │
                                                               ▼
                                                        StockMovement
```

**Workflow cơ bản:**

1. Tạo Supplier
2. Tạo PurchaseOrder (chọn Supplier)
3. Add PurchaseOrderItem vào PO
4. Receive goods → Update Inventory + Record StockMovement

### **Các nguyên tắc quan trọng:**

✅ **Enums** → Dùng cho dropdowns, validation, avoid hard-code strings  
✅ **Models** → Business logic, data structure  
✅ **Data Files** → Temporary storage (sẽ migrate sang DB sau)  
✅ **Separation** → Inventory (full) vs InventoryRow (display only)  
✅ **Audit Trail** → StockMovement track mọi thay đổi

---

**🎓 Bạn đã hiểu rõ chưa? Sẵn sàng cho NGÀY 2 chưa? 🚀**
