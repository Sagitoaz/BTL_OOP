// package org.example.oop.Control;
//
// import java.io.IOException;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
//
// import org.example.oop.Model.Inventory.Inventory;
// import org.example.oop.Model.Inventory.StockMovement;
// import org.example.oop.Repository.InventoryRepository;
// import org.example.oop.Repository.StockMovementRepository;
// import org.example.oop.Service.InventoryService;
// import org.example.oop.Service.StockMovementService;
// import org.example.oop.Utils.AppConfig;
//
// import javafx.animation.PauseTransition;
// import javafx.beans.property.ReadOnlyObjectWrapper;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.collections.transformation.FilteredList;
// import javafx.fxml.FXML;
// import javafx.geometry.Side;
// import javafx.scene.control.Button;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.ContextMenu;
// import javafx.scene.control.DatePicker;
// import javafx.scene.control.Label;
// import javafx.scene.control.MenuItem;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextField;
// import javafx.util.Duration;
//
// public class StockMovementController {
//
// // ===== Header & Filter Section =====
// @FXML
// private Label statsLabel;
// @FXML
// private ComboBox<String> filterProductBox;
// @FXML
// private ComboBox<String> filterMoveTypeBox;
// @FXML
// private DatePicker filterDateFrom;
// @FXML
// private DatePicker filterDateTo;
//
// // ===== Left Panel - Record New Movement =====
// @FXML
// private TextField productField; // ⬅️ thay cho ComboBox productBox
// @FXML
// private Label currentQtyLabel;
//
// @FXML
// private ComboBox<String> moveTypeBox;
// @FXML
// private TextField qtyField;
//
// @FXML
// private ComboBox<String> refTableBox;
// @FXML
// private TextField refIdField;
// @FXML
// private TextField noteField;
// @FXML
// private TextField batchNoField;
// @FXML
// private DatePicker expiryDatePicker;
//
// @FXML
// private TextField serialNoField;
// @FXML
// private TextField movedbyField1;
// @FXML
// private DatePicker movedatDatePicker1;
// @FXML
// private Button saveButton;
// @FXML
// private Button clearButton;
// @FXML
// private Button filterButton;
// @FXML
// private Button resetFilterButton;
//
// // ===== Edit Mode Controls =====
// @FXML
// private Label modeLabel;
//
// @FXML
// private Label statusLabel;
//
// // ===== Right Panel - Movement History Table =====
// @FXML
// private TableView<StockMovement> movementTable;
//
// @FXML
// private TableColumn<StockMovement, Integer> idColumn;
// @FXML
// private TableColumn<StockMovement, String> productNameColumn;
// @FXML
// private TableColumn<StockMovement, Integer> productIdColumn;
// @FXML
// private TableColumn<StockMovement, Integer> qtyColumn;
// @FXML
// private TableColumn<StockMovement, String> moveTypeColumn;
// @FXML
// private TableColumn<StockMovement, String> refTableColumn;
// @FXML
// private TableColumn<StockMovement, Integer> refIdColumn;
// @FXML
// private TableColumn<StockMovement, String> batchNoColumn;
// @FXML
// private TableColumn<StockMovement, String> expiryDateColumn;
// @FXML
// private TableColumn<StockMovement, String> serialNoColumn;
// @FXML
// private TableColumn<StockMovement, String> movedAtColumn;
// @FXML
// private TableColumn<StockMovement, String> movedByColumn;
// @FXML
// private TableColumn<StockMovement, String> noteColumn;
// @FXML
// private TableColumn<StockMovement, Void> actionsColumn;
//
// // ===== Bottom - Footer =====
// @FXML
// private Label footerStatusLabel;
// @FXML
// private Label totalMovementsLabel;
// @FXML
// private Label selectedProductLabel;
//
// private final StockMovementService movementService = new
// StockMovementService();
// private final InventoryService inventoryService = new InventoryService();
// private final StockMovementRepository movementRepo = new
// StockMovementRepository();
// private final InventoryRepository inventoryRepo = new InventoryRepository();
// private StockMovement selectMovement = null;
// private ObservableList<Inventory> inventoryList =
// FXCollections.observableArrayList();
//
// // ===== Edit Mode State =====
// private boolean isEditMode = false;
// private StockMovement editingMovement = null;
// private int originalQty = 0; // để tính delta khi update
//
// private final ObservableList<StockMovement> masterData =
// FXCollections.observableArrayList();
// private final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd
// HH:mm:ss");
//
// // ===== dữ liệu gợi ý tên sản phẩm =====
// private final ObservableList<String> productNames =
// FXCollections.observableArrayList();
// private final ContextMenu productSuggest = new ContextMenu();
// // Lọc lịch sử movement
// private FilteredList<StockMovement> filteredMovements;
// private final PauseTransition qtyDebounce = new
// PauseTransition(Duration.millis(180));
//
// // ====== Event Handlers (declared in FXML) ======
// @FXML
// private void onFilterButton() {
// updateFilter();
// }
//
// @FXML
// private void onResetFilterButton() {
// if (filterProductBox != null) {
// filterProductBox.getSelectionModel().clearSelection();
// if (filterProductBox.isEditable())
// filterProductBox.getEditor().clear();
// }
// if (filterMoveTypeBox != null)
// filterMoveTypeBox.getSelectionModel().clearSelection();
// if (filterDateFrom != null)
// filterDateFrom.setValue(null);
// if (filterDateTo != null)
// filterDateTo.setValue(null);
//
// updateFilter();
// }
//
// @FXML
// private void onSaveButton() {
// if (isEditMode) {
// updateMovement();
// } else {
// createNewMovement();
// }
// }
//
// @FXML
// private void onClearButton() {
// if (isEditMode) {
// cancelEdit();
// } else {
// clearForm();
// }
// }
//
// @FXML
// private void onRefreshButton() {
// try {
// loadData();
// } catch (IOException e) {
// e.printStackTrace();
// statusLabel.setText("Lỗi refresh dữ liệu: " + e.getMessage());
// }
// }
//
// @FXML
// private void onExportButton() {
// }
//
// // ====== Initialize ======
// @FXML
// public void initialize() {
// System.out.println("🚀 StockMovementController initializing...");
// try {
// initTable();
// loadProductNames(); // fill productNames
// initProductField(); // ⬅️ bật gợi ý
// wireProductQty(); // ⬅️ cập nhật tồn theo tên đang nhập
// setupFilters();
// loadData();
//
// // ✅ Thiết lập chế độ mặc định (Add Mode)
// initializeDefaultMode();
// } catch (Exception e) {
// System.err.println("❌ Initialization error: " + e.getMessage());
// e.printStackTrace();
// if (statusLabel != null)
// statusLabel.setText("Initialization failed: " + e.getMessage());
// }
// }
//
// private void initializeDefaultMode() {
// isEditMode = false;
// editingMovement = null;
// originalQty = 0;
//
// // ✅ Thiết lập UI cho Add Mode
// updateModeUI();
//
// // ✅ Thiết lập ngày mặc định
// if (movedatDatePicker1 != null) {
// movedatDatePicker1.setValue(LocalDate.now());
// }
//
// // ✅ Thiết lập ComboBox cho refTable
// if (refTableBox != null) {
// refTableBox.getItems().setAll("Payments", "Sales", "Purchases",
// "Adjustments", "Returns");
// }
// }
//
// private void setupFilters() {
// // 1) Bọc masterData bằng FilteredList
// filteredMovements = new FilteredList<>(masterData, m -> true);
// var sorted = new
// javafx.collections.transformation.SortedList<>(filteredMovements);
// sorted.comparatorProperty().bind(movementTable.comparatorProperty());
// movementTable.setItems(sorted); // ⬅️ CHỈ set 1 lần ở đây
//
// // 3) Cho phép gõ để lọc trong filterProductBox (không bắt buộc)
// if (filterProductBox != null) {
// filterProductBox.setEditable(true); // gõ để tìm nhanh
// // đã set items ở loadProductNames(); nếu muốn:
// //
// filterProductBox.setItems(FXCollections.observableArrayList(productNames));
// // Lắng nghe cả value lẫn text editor
// filterProductBox.valueProperty().addListener((o, ov, nv) -> updateFilter());
// filterProductBox.getEditor().textProperty().addListener((o, ov, nv) ->
// updateFilter());
// }
//
// if (filterMoveTypeBox != null) {
// filterMoveTypeBox.valueProperty().addListener((o, ov, nv) -> updateFilter());
// }
// if (filterDateFrom != null) {
// filterDateFrom.valueProperty().addListener((o, ov, nv) -> updateFilter());
// }
// if (filterDateTo != null) {
// filterDateTo.valueProperty().addListener((o, ov, nv) -> updateFilter());
// }
//
// // Khởi tạo lần đầu
// updateFilter();
// }
//
// private void updateFilter() {
// // Lấy input từ các filter
// String productQuery = null;
// if (filterProductBox != null) {
// String v = filterProductBox.getValue();
// String t = filterProductBox.isEditable() ?
// filterProductBox.getEditor().getText() : null;
// productQuery = (t != null && !t.isBlank()) ? t : v;
// }
// String moveTypeQuery = (filterMoveTypeBox != null) ?
// filterMoveTypeBox.getValue() : null;
// LocalDate from = (filterDateFrom != null) ? filterDateFrom.getValue() : null;
// LocalDate to = (filterDateTo != null) ? filterDateTo.getValue() : null;
//
// // Nếu người dùng chọn "to" < "from" → tự hoán đổi cho đỡ lỗi
// if (from != null && to != null && to.isBefore(from)) {
// LocalDate tmp = from;
// from = to;
// to = tmp;
// }
//
// final String pq = productQuery == null ? "" :
// productQuery.trim().toLowerCase();
// final String mt = moveTypeQuery == null ? "" :
// moveTypeQuery.trim().toLowerCase();
// final LocalDate f = from;
// final LocalDate tdate = to;
//
// filteredMovements.setPredicate(m -> {
// if (m == null)
// return false;
//
// // 1) Lọc theo Product name (contains, không phân biệt hoa thường)
// if (!pq.isBlank()) {
// String rowName = safeGetProductName(m.getProductId());
// if (rowName == null)
// rowName = "";
// if (!rowName.toLowerCase().contains(pq))
// return false;
// }
//
// // 2) Lọc theo Move Type (so sánh equalsIgnoreCase)
// if (!mt.isBlank()) {
// Object mv = m.getMoveType();
// String rowType = mv == null ? "" : (mv instanceof Enum<?> e ? e.name() :
// mv.toString());
// if (!rowType.toLowerCase().equals(mt))
// return false;
// }
//
// // 3) Lọc theo khoảng ngày (movedAt thuộc [from, to], inclusive)
// if (f != null || tdate != null) {
// if (m.getMovedAt() == null)
// return false;
// LocalDate d = m.getMovedAt().toLocalDate();
// if (f != null && d.isBefore(f))
// return false;
// if (tdate != null && d.isAfter(tdate))
// return false;
// }
//
// return true;
// });
//
// // Cập nhật thống kê gọn gàng
// if (statsLabel != null) {
// statsLabel.setText("Đang hiển thị: " + filteredMovements.size() + " / " +
// masterData.size());
// }
// }
//
// private void loadData() throws IOException {
// System.out.println("🔄 Loading stock movement data...");
// var moveTypes = FXCollections.observableArrayList(
// "purchase", "sale", "return_in", "return_out",
// "adjustment", "consume", "transfer");
// moveTypeBox.setItems(moveTypes);
// filterMoveTypeBox.setItems(moveTypes);
//
// try {
// masterData.clear();
// ObservableList<StockMovement> loadedData = movementRepo.loadAll();
// masterData.addAll(loadedData);
// System.out.println("✅ Loaded movements: " + masterData.size());
// } catch (Exception e) {
// System.err.println("❌ Error loading stock movements: " + e.getMessage());
// e.printStackTrace();
// if (statusLabel != null)
// statusLabel.setText("Error loading data: " + e.getMessage());
// }
//
// if (totalMovementsLabel != null)
// totalMovementsLabel.setText("Total: " + masterData.size());
// updateFilter(); // ⬅️ để predicate áp vào dữ liệu mới nạp
// System.out.println("📊 Table items set: " + movementTable.getItems().size());
// }
//
// private void initTable() {
// idColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(d.getValue().getId()));
// productIdColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(d.getValue().getProductId()));
// productNameColumn.setCellValueFactory(
// d -> new
// ReadOnlyObjectWrapper<>(safeGetProductName(d.getValue().getProductId())));
// qtyColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(d.getValue().getQty()));
// moveTypeColumn.setCellValueFactory(d -> {
// Object moveType = d.getValue().getMoveType();
// String s = moveType == null ? "" : (moveType instanceof Enum<?> e ? e.name()
// : moveType.toString());
// return new ReadOnlyObjectWrapper<>(s);
// });
// refTableColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(nz(d.getValue().getRefTable())));
// refIdColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(d.getValue().getRefId()));
// batchNoColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(nz(d.getValue().getBatchNo())));
// expiryDateColumn.setCellValueFactory(d -> {
// var date = d.getValue().getExpiryDate();
// String formatted = date != null ?
// date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
// return new ReadOnlyObjectWrapper<>(formatted);
// });
// serialNoColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(nz(d.getValue().getSerialNo())));
// movedAtColumn.setCellValueFactory(d -> {
// var t = d.getValue().getMovedAt();
// return new ReadOnlyObjectWrapper<>(t == null ? "" : t.format(DT));
// });
// movedByColumn
// .setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(String.valueOf(d.getValue().getMovedBy())));
// noteColumn.setCellValueFactory(d -> new
// ReadOnlyObjectWrapper<>(nz(d.getValue().getNote())));
//
// // ✅ Thêm cột Actions với nút Edit
// actionsColumn.setCellFactory(col -> {
// return new javafx.scene.control.TableCell<StockMovement, Void>() {
// private final Button editBtn = new Button("Edit");
//
// {
// editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;
// -fx-font-size: 10px;");
// editBtn.setOnAction(e -> {
// StockMovement movement = getTableView().getItems().get(getIndex());
// if (movement != null) {
// enterEditMode(movement);
// }
// });
// }
//
// @Override
// protected void updateItem(Void item, boolean empty) {
// super.updateItem(item, empty);
// if (empty) {
// setGraphic(null);
// } else {
// setGraphic(editBtn);
// }
// }
// };
// });
//
// // ✅ Thêm double-click để edit nhanh
// movementTable.setRowFactory(tv -> {
// javafx.scene.control.TableRow<StockMovement> row = new
// javafx.scene.control.TableRow<>();
// row.setOnMouseClicked(event -> {
// if (event.getClickCount() == 2 && !row.isEmpty()) {
// StockMovement movement = row.getItem();
// if (movement != null) {
// enterEditMode(movement);
// }
// }
// });
// return row;
// });
//
// movementTable.getSelectionModel().selectedItemProperty().addListener((obs, o,
// n) -> {
// selectMovement = n;
// if (selectedProductLabel != null)
// selectedProductLabel.setText(n == null ? "" : "Selected ID: " + n.getId());
// });
// }
//
// // ====== TextField gợi ý tên sản phẩm ======
// private void initProductField() {
// productField.textProperty().addListener((o, oldT, txt) -> {
// String q = txt == null ? "" : txt.trim().toLowerCase();
// if (q.isEmpty()) {
// productSuggest.hide();
// return;
// }
//
// var hits = productNames.stream()
// .filter(n -> n.toLowerCase().contains(q))
// .limit(10).toList();
//
// if (hits.isEmpty()) {
// productSuggest.hide();
// return;
// }
//
// var items = hits.stream().map(name -> {
// MenuItem mi = new MenuItem(name);
// mi.setOnAction(e -> {
// // ✅ FIX: Xóa text cũ trước khi set text mới
// productField.clear();
// productField.setText(name);
// productField.positionCaret(name.length());
// productSuggest.hide();
//
// // ✅ FIX: Cập nhật số lượng ngay khi chọn từ gợi ý
// updateQtyByName(name);
// });
// return mi;
// }).toList();
//
// productSuggest.getItems().setAll(items);
// if (!productSuggest.isShowing())
// productSuggest.show(productField, Side.BOTTOM, 0, 0);
// });
//
// productField.focusedProperty().addListener((o, was, f) -> {
// if (!f)
// productSuggest.hide();
// });
// }
//
// private void wireProductQty() {
// productField.textProperty().addListener((o, ov, nv) -> {
// // debounce: chờ người dùng ngừng gõ 180ms
// qtyDebounce.setOnFinished(e -> {
// updateQtyByName(nv);
// });
// qtyDebounce.playFromStart();
// });
// }
//
// private void updateQtyByName(String name) {
// if (currentQtyLabel == null)
// return;
// if (name == null || name.isBlank()) {
// currentQtyLabel.setText("Tồn kho: --");
// return;
// }
//
// // ✅ FIX: Hiển thị số lượng ngay cả khi chưa nhập đầy đủ tên
// // Tìm sản phẩm có tên chứa từ khóa đang nhập (không phân biệt hoa/thường)
// String partialMatch = productNames.stream()
// .filter(n -> n.toLowerCase().contains(name.trim().toLowerCase()))
// .findFirst().orElse(null);
//
// // Nếu có kết quả khớp một phần, hiển thị số lượng
// if (partialMatch != null) {
// int pid = getProductIdByName(partialMatch);
// if (pid != -1) {
// // Đọc an toàn (không để exception làm sập listener)
// int onHand;
// try {
// onHand = inventoryService.getOnHand(pid);
// // ✅ FIX: Hiển thị cả tên sản phẩm tìm thấy nếu chưa nhập chính xác
// if (partialMatch.equalsIgnoreCase(name.trim())) {
// currentQtyLabel.setText("Tồn kho: " + onHand);
// } else {
// currentQtyLabel.setText("Tồn kho (" + partialMatch + "): " + onHand);
// }
// } catch (Exception ex) {
// System.err.println("⚠️ getOnHand error for pid=" + pid + ": " +
// ex.getMessage());
// currentQtyLabel.setText("Tồn kho: 0");
// }
// return;
// }
// }
//
// // Không tìm thấy sản phẩm nào
// currentQtyLabel.setText("Tồn kho: --");
// }
//
// // ====== Helpers ======
// private static String nz(String s) {
// return s == null ? "" : s;
// }
//
// // ===== Edit Mode Methods =====
// private void createNewMovement() {
// try {
// String productName = productField.getText();
// if (productName == null || productName.isBlank()) {
// statusLabel.setText("❌ Vui lòng chọn sản phẩm");
// return;
// }
// boolean exists = productNames.stream().anyMatch(n ->
// n.equalsIgnoreCase(productName.trim()));
// if (!exists) {
// statusLabel.setText("❌ Sản phẩm không có trong inventory");
// return;
// }
//
// if (moveTypeBox.getValue() == null) {
// statusLabel.setText("❌ Vui lòng chọn loại giao dịch");
// return;
// }
// if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
// statusLabel.setText("❌ Vui lòng nhập số lượng");
// return;
// }
//
// int productId = getProductIdByName(productName);
// if (productId == -1) {
// statusLabel.setText("❌ Không tìm thấy sản phẩm: " + productName);
// return;
// }
//
// String moveType = moveTypeBox.getValue().toUpperCase();
// int qty = Integer.parseInt(qtyField.getText());
// String refTable = refTableBox.getValue();
// Integer refId = refIdField.getText().trim().isEmpty() ? null :
// Integer.parseInt(refIdField.getText());
// String batchNo = batchNoField.getText().trim().isEmpty() ? null :
// batchNoField.getText();
// LocalDate expiryDate = expiryDatePicker.getValue();
// String serialNo = serialNoField.getText().trim().isEmpty() ? null :
// serialNoField.getText();
// int movedBy = Integer.parseInt(movedbyField1.getText());
// LocalDate moveDate = movedatDatePicker1.getValue();
// String note = noteField.getText();
//
// StockMovement movement = movementService.recordMovementByType(
// productId, qty, moveType, refTable, refId,
// batchNo, expiryDate, serialNo, movedBy, note);
//
// statusLabel.setText("✅ Đã lưu movement ID: " + movement.getId());
// clearForm();
// loadData();
// } catch (NumberFormatException e) {
// statusLabel.setText("❌ Lỗi định dạng số: " + e.getMessage());
// } catch (Exception e) {
// statusLabel.setText("❌ Lỗi lưu dữ liệu: " + e.getMessage());
// e.printStackTrace();
// }
// }
//
// private void updateMovement() {
// try {
// if (editingMovement == null) {
// statusLabel.setText("❌ Không có movement để cập nhật");
// return;
// }
//
// String productName = productField.getText();
// if (productName == null || productName.isBlank()) {
// statusLabel.setText("❌ Vui lòng chọn sản phẩm");
// return;
// }
//
// if (moveTypeBox.getValue() == null) {
// statusLabel.setText("❌ Vui lòng chọn loại giao dịch");
// return;
// }
// if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
// statusLabel.setText("❌ Vui lòng nhập số lượng");
// return;
// }
//
// int productId = getProductIdByName(productName);
// if (productId == -1) {
// statusLabel.setText("❌ Không tìm thấy sản phẩm: " + productName);
// return;
// }
//
// String moveType = moveTypeBox.getValue().toUpperCase();
// int newQty = Integer.parseInt(qtyField.getText());
// String refTable = refTableBox.getValue();
// Integer refId = refIdField.getText().trim().isEmpty() ? null :
// Integer.parseInt(refIdField.getText());
// String batchNo = batchNoField.getText().trim().isEmpty() ? null :
// batchNoField.getText();
// LocalDate expiryDate = expiryDatePicker.getValue();
// String serialNo = serialNoField.getText().trim().isEmpty() ? null :
// serialNoField.getText();
// int movedBy = Integer.parseInt(movedbyField1.getText());
// LocalDate moveDate = movedatDatePicker1.getValue();
// String note = noteField.getText();
//
// // ✅ Tính delta để điều chỉnh tồn kho
// int deltaQty = calculateQtyDelta(editingMovement.getMoveType().toString(),
// originalQty, moveType,
// newQty);
//
// // ✅ Cập nhật movement trong database
// boolean updated = movementService.updateMovement(editingMovement.getId(),
// productId, newQty, moveType, refTable, refId,
// batchNo, expiryDate, serialNo, movedBy, note);
//
// if (updated) {
// // ✅ Điều chỉnh tồn kho nếu có thay đổi về số lượng
// if (deltaQty != 0) {
// inventoryService.applyDelta(productId, deltaQty, true);
// }
//
// statusLabel.setText("✅ Đã cập nhật movement ID: " + editingMovement.getId());
// exitEditMode();
// loadData();
// } else {
// statusLabel.setText("❌ Không thể cập nhật movement");
// }
//
// } catch (NumberFormatException e) {
// statusLabel.setText("❌ Lỗi định dạng số: " + e.getMessage());
// } catch (Exception e) {
// statusLabel.setText("❌ Lỗi cập nhật dữ liệu: " + e.getMessage());
// e.printStackTrace();
// }
// }
//
// private int calculateQtyDelta(String oldMoveType, int oldQty, String
// newMoveType, int newQty) {
// // Hoàn nguyên tác động cũ
// int revertDelta = 0;
// if ("PURCHASE".equalsIgnoreCase(oldMoveType) ||
// "RETURN_IN".equalsIgnoreCase(oldMoveType)) {
// revertDelta = -oldQty; // trừ lại số lượng đã cộng
// } else if ("SALE".equalsIgnoreCase(oldMoveType) ||
// "RETURN_OUT".equalsIgnoreCase(oldMoveType)
// || "CONSUME".equalsIgnoreCase(oldMoveType)) {
// revertDelta = oldQty; // cộng lại số lượng đã trừ
// }
//
// // Áp dụng tác động mới
// int newDelta = 0;
// if ("PURCHASE".equalsIgnoreCase(newMoveType) ||
// "RETURN_IN".equalsIgnoreCase(newMoveType)) {
// newDelta = newQty; // cộng số lượng mới
// } else if ("SALE".equalsIgnoreCase(newMoveType) ||
// "RETURN_OUT".equalsIgnoreCase(newMoveType)
// || "CONSUME".equalsIgnoreCase(newMoveType)) {
// newDelta = -newQty; // trừ số lượng mới
// }
//
// return revertDelta + newDelta;
// }
//
// private void enterEditMode(StockMovement movement) {
// isEditMode = true;
// editingMovement = movement;
// originalQty = movement.getQty();
//
// // ✅ Điền dữ liệu vào form
// populateFormWithMovement(movement);
//
// // ✅ Thay đổi UI
// updateModeUI();
//
// statusLabel.setText("📝 Đang chỉnh sửa movement ID: " + movement.getId());
// }
//
// private void exitEditMode() {
// isEditMode = false;
// editingMovement = null;
// originalQty = 0;
//
// // ✅ Đặt lại UI
// updateModeUI();
// clearForm();
//
// statusLabel.setText("✅ Đã thoát chế độ chỉnh sửa");
// }
//
// private void cancelEdit() {
// exitEditMode();
// statusLabel.setText("❌ Đã hủy chỉnh sửa");
// }
//
// private void populateFormWithMovement(StockMovement movement) {
// // ✅ Tìm tên sản phẩm
// String productName = safeGetProductName(movement.getProductId());
// productField.setText(productName);
//
// // ✅ Điền các trường khác
// moveTypeBox.setValue(movement.getMoveType().toString().toLowerCase());
// qtyField.setText(String.valueOf(movement.getQty()));
// refTableBox.setValue(movement.getRefTable());
// refIdField.setText(movement.getRefId() != null ?
// String.valueOf(movement.getRefId()) : "");
// batchNoField.setText(movement.getBatchNo() != null ? movement.getBatchNo() :
// "");
// expiryDatePicker.setValue(movement.getExpiryDate());
// serialNoField.setText(movement.getSerialNo() != null ? movement.getSerialNo()
// : "");
// movedbyField1.setText(String.valueOf(movement.getMovedBy()));
//
// if (movement.getMovedAt() != null) {
// movedatDatePicker1.setValue(movement.getMovedAt().toLocalDate());
// }
//
// noteField.setText(movement.getNote() != null ? movement.getNote() : "");
//
// // ✅ Cập nhật số lượng tồn kho
// updateQtyByName(productName);
// }
//
// private void updateModeUI() {
// if (modeLabel != null) {
// modeLabel.setText(isEditMode ? "📝 EDIT MODE" : "➕ ADD MODE");
// modeLabel.setStyle(isEditMode ? "-fx-text-fill: orange; -fx-font-weight:
// bold;"
// : "-fx-text-fill: green; -fx-font-weight: bold;");
// }
//
// if (saveButton != null) {
// saveButton.setText(isEditMode ? "Update Movement" : "Save Movement");
// }
//
// if (clearButton != null) {
// clearButton.setText(isEditMode ? "Cancel Edit" : "Clear");
// }
// }
//
// private void clearForm() {
// productField.clear();
// moveTypeBox.getSelectionModel().clearSelection();
// qtyField.clear();
// refTableBox.getSelectionModel().clearSelection();
// refIdField.clear();
// batchNoField.clear();
// expiryDatePicker.setValue(null);
// serialNoField.clear();
// movedbyField1.clear();
// movedatDatePicker1.setValue(LocalDate.now());
// noteField.clear();
//
// if (currentQtyLabel != null) {
// currentQtyLabel.setText("Tồn kho: --");
// }
// }
//
// private int getProductIdByName(String productName) {
// if (productName == null || productName.trim().isEmpty())
// return -1;
// // Tìm EXACT trước
// Inventory exact = inventoryList.stream()
// .filter(inv -> inv.getName() != null &&
// inv.getName().equalsIgnoreCase(productName.trim()))
// .findFirst().orElse(null);
// if (exact != null)
// return exact.getId();
//
// // Nếu bạn thật sự muốn fallback chứa từ khoá (không khuyến nghị):
// // Inventory partial = inventoryList.stream()
// // .filter(inv -> inv.getName() != null &&
// // inv.getName().toLowerCase().contains(productName.trim().toLowerCase()))
// // .findFirst().orElse(null);
// // return partial != null ? partial.getId() : -1;
//
// return -1;
// }
//
// private void loadProductNames() {
// try {
// inventoryList = inventoryRepo.loadInventory(AppConfig.TEST_DATA_TXT);
// productNames.setAll(inventoryList.stream().map(Inventory::getName).toList());
// filterProductBox.setItems(FXCollections.observableArrayList(productNames));
// System.out.println("✅ Loaded " + productNames.size() + " product names");
// } catch (IOException e) {
// System.err.println("❌ IOException while loading product names: " +
// e.getMessage());
// } catch (RuntimeException e) {
// System.err.println("❌ RuntimeException while loading product names: " +
// e.getMessage());
// }
// }
//
// private String safeGetProductName(int productId) {
// try {
// for (Inventory i : inventoryList)
// if (i.getId() == productId)
// return i.getName();
// return "Product #" + productId;
// } catch (Exception e) {
// return "";
// }
// }
// }
