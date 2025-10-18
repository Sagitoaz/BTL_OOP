package org.example.oop.Control.Inventory;

// package org.example.oop.Control;
//
// import java.io.IOException;
// import java.time.LocalDate;
//
// import org.example.oop.Model.Inventory.Inventory;
// import org.example.oop.Repository.InventoryRepository;
// import org.example.oop.Utils.AppConfig;
//
// import javafx.beans.property.ReadOnlyObjectWrapper;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.collections.transformation.FilteredList;
// import javafx.collections.transformation.SortedList;
// import javafx.event.ActionEvent;
// import javafx.fxml.FXML;
// import javafx.scene.control.Alert;
// import javafx.scene.control.Button;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Label;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextField;
//
// public class ProductCRUDController {
// // === FXML Fields - Table ===
// @FXML
// private TableView<Inventory> productTable;
// @FXML
// private TableColumn<Inventory, Integer> idColumn;
// @FXML
// private TableColumn<Inventory, String> skuColumn;
// @FXML
// private TableColumn<Inventory, String> nameColumn;
// @FXML // ✅ THÊM @FXML
// private TableColumn<Inventory, String> categoryColumn;
// @FXML // ✅ THÊM @FXML
// private TableColumn<Inventory, Integer> quantityColumn; // ✅ SỬA thành
// Integer
// @FXML // ✅ THÊM @FXML
// private TableColumn<Inventory, Integer> priceColumn; // ✅ SỬA thành Integer
// @FXML // ✅ THÊM @FXML
// private TableColumn<Inventory, String> statusColumn; // ✅ SỬA thành String
//
// // === FXML Fields - Form ===
// @FXML
// private TextField skuField;
// @FXML
// private TextField nameField;
// @FXML // ✅ THÊM @FXML
// private TextField quantityField;
// @FXML // ✅ THÊM @FXML
// private TextField unitField;
// @FXML // ✅ THÊM @FXML
// private TextField priceField;
// @FXML
// private TextField searchField;
//
// @FXML
// private ComboBox<String> categoryBox;
// @FXML
// private ComboBox<String> filterCategoryBox;
// @FXML
// private ComboBox<String> statusBox;
// // === FXML Fields - Buttons & Status ===
// @FXML
// private Label statusLabel;
// @FXML
// private Button saveButton;
// @FXML
// private Button deleteButton;
// @FXML
// private Button clearButton;
// @FXML
// private Button clearFilterButton;
// @FXML
// private Button exportButton;
// @FXML
// private Button importButton;
// @FXML
// private Button duplicateButton;
// @FXML
// private Button editButton;
// @FXML
// private Button addNewButton;
//
// // === Data ===
// private ObservableList<Inventory> productList =
// FXCollections.observableArrayList();
// private InventoryRepository inventoriesController = new
// InventoryRepository();
// private Inventory selectedProduct = null;
// private FilteredList<Inventory> filteredData;
// private SortedList<Inventory> sortedData;
//
// @FXML // ✅ THÊM @FXML cho initialize
// public void initialize() throws IOException {
// loadData(); // nạp vào productList
// initFilterPipe(); // tạo FilteredList + SortedList + gắn vào bảng
// initTable(); // chỉ set cellValueFactory + listener (ko setItems)
// initFormBoxes(); // gán dữ liệu cho combobox form
// }
//
// // === Event Handlers ===
// @FXML
// private void onExportButton(ActionEvent event) throws Exception {
//
// }
//
// @FXML
// private void onAddNewButton(ActionEvent event) throws Exception {
//
// }
//
// @FXML
// private void onImportButton(ActionEvent event) throws Exception {
//
// }
//
// @FXML
// private void onClearFilterButton(ActionEvent event) throws Exception {
// searchField.clear();
// filterCategoryBox.getSelectionModel().selectFirst();
// }
//
// @FXML
// private void onDuplicateButton(ActionEvent event) throws Exception {
//
// }
//
// @FXML
// private void onEditButton(ActionEvent event) throws Exception {
//
// }
//
// @FXML
// private void onSaveButton(ActionEvent event) throws Exception {
// if (!validateForm()) {
// showStatus("Please fill all required fields", true);
// return;
// }
// Inventory product = getFormData();
// if (selectedProduct == null) {
// inventoriesController.AddInventory(productList, product);
// showStatus("Product added successfully", false);
// } else {
// boolean success = inventoriesController.updateInventory(productList,
// selectedProduct.getId(), product);
// if (success) {
// showStatus("Product updated successfully", false);
// } else {
// showStatus("Failed to update product", true);
// }
// }
// inventoriesController.saveInventory(productList, AppConfig.TEST_DATA_TXT);
// clearForm();
// productTable.refresh();
// }
//
// @FXML
// private void onDeleteButton(ActionEvent event) throws Exception {
// if (selectedProduct == null) {
// showStatus("Please select a product to delete", true);
// return;
// }
//
// Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
// confirmAlert.setTitle("Confirm Delete");
// confirmAlert.setHeaderText("Delete Product?");
// confirmAlert.setContentText("Are you sure you want to delete: " +
// selectedProduct.getName() + "?");
//
// if (confirmAlert.showAndWait().get() == ButtonType.OK) {
// boolean success = inventoriesController.deleteInventory(productList,
// selectedProduct.getId());
//
// if (success) {
// inventoriesController.saveInventory(productList, AppConfig.TEST_DATA_TXT);
// clearForm();
// productTable.refresh();
// showStatus("Product deleted successfully", false);
// } else {
// showStatus("Failed to delete product", true);
// }
// }
// }
//
// @FXML
// private void onClearButton(ActionEvent event) {
// clearForm();
// }
//
// // === Helper Methods ===
// private void initTable() {
// System.out.println(
// "✅ ProductCRUDController.initTable(): Setting up table with " +
// productList.size() + " items");
// idColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getId()));
// skuColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getSku()));
// nameColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getName()));
// categoryColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getCategory()));
// quantityColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
// priceColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice()));
// statusColumn.setCellValueFactory(data -> new
// ReadOnlyObjectWrapper<>(data.getValue().getStockStatus()));
//
// productTable.getSelectionModel().selectedItemProperty().addListener((obs,
// oldSelection, newSelection) -> {
// if (newSelection != null) {
// selectedProduct = newSelection;
// populateForm(newSelection);
// }
// });
// // productTable.setItems(productList);
// System.out.println("✅ ProductCRUDController.initTable(): Table setup
// complete!");
// }
//
// private void initFormBoxes() {
//
// ObservableList<String> categories = FXCollections.observableArrayList(
// "Drug", "Lens", "Frame", "Tool", "Supply");
// categoryBox.setItems(categories);
// ObservableList<String> status = FXCollections.observableArrayList(
// "OUT_OF_STOCK", "LOW_STOCK", "IN_STOCK");
// statusBox.setItems(status);
// }
//
// private void loadData() throws IOException {
// productList.setAll(inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT));
// // ✅ thay setAll
// System.out.println("✅ Loaded " + productList.size() + " products");
// }
//
// private void populateForm(Inventory product) {
// if (product == null)
// return;
//
// skuField.setText(product.getSku());
// nameField.setText(product.getName());
// categoryBox.setValue(product.getCategory());
// quantityField.setText(String.valueOf(product.getQuantity()));
// unitField.setText(product.getUnit());
// priceField.setText(String.valueOf(product.getUnitPrice()));
// statusBox.setValue(product.getStockStatus());
//
// }
//
// private void clearForm() {
// skuField.clear();
// nameField.clear();
// categoryBox.getSelectionModel().clearSelection();
// quantityField.clear();
// unitField.clear();
// priceField.clear();
// statusBox.getSelectionModel().selectFirst();
// selectedProduct = null;
// }
//
// private Inventory getFormData() {
// Inventory inventory = new Inventory();
//
// inventory.setSku(skuField.getText());
// inventory.setName(nameField.getText());
// inventory.setCategory(categoryBox.getValue());
// inventory.setQuantity(Integer.parseInt(quantityField.getText()));
// inventory.setUnit(unitField.getText());
// inventory.setUnitPrice(Integer.parseInt(priceField.getText()));
// inventory.setLastUpdated(LocalDate.now());
// inventory.setStockStatus(statusBox.getSelectionModel().getSelectedItem());
// return inventory;
// }
//
// private boolean validateForm() {
// if (skuField.getText() == null || skuField.getText().trim().isEmpty()) {
// return false;
// }
// if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
// return false;
// }
// if (categoryBox.getValue() == null || categoryBox.getValue().isEmpty()) {
// return false;
// }
//
// try {
// Integer.parseInt(quantityField.getText());
// Integer.parseInt(priceField.getText());
// } catch (NumberFormatException e) {
// return false;
// }
//
// return true;
// }
//
// private void initFilterPipe() {
// ObservableList<String> category =
// FXCollections.observableArrayList("Category", "Drug", "Lens", "Frame",
// "Tool",
// "Supply");
// filterCategoryBox.setItems(category);
// filterCategoryBox.getSelectionModel().selectFirst();
// filteredData = new FilteredList<>(productList, p -> true);
// sortedData = new SortedList<>(filteredData);
// sortedData.comparatorProperty().bind(productTable.comparatorProperty());
// productTable.setItems(sortedData);
//
// // Ví dụ: filter theo searchField
// if (searchField != null) {
// searchField.textProperty().addListener((obs, o, n) -> applyFilter());
// }
// if (filterCategoryBox != null) {
// filterCategoryBox.valueProperty().addListener((obs, o, n) -> applyFilter());
// }
// applyFilter();
// }
//
// private void applyFilter() {
// String selected = filterCategoryBox.getSelectionModel().getSelectedItem();
// boolean all = (selected == null || selected.equalsIgnoreCase("Category"));
// String selectedNorm = norm(selected);
// String kw = norm(searchField != null ? searchField.getText() : "");
// filteredData.setPredicate(row -> {
// if (row == null)
// return false;
// boolean matchKw = kw.isBlank()
// || norm(row.getName()).contains(kw)
// || norm(row.getSku()).contains(kw)
// || norm(row.getCategory()).contains(kw);
//
// boolean matchCat = all
// || norm(row.getCategory()).equals(selectedNorm); // hoặc .contains(...) nếu
// bạn muốn “chứa”
//
// return matchKw && matchCat;
// });
// }
//
// private static String norm(String s) {
// if (s == null)
// return "";
// String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
// return n.replaceAll("\\p{M}+", "").toLowerCase().trim();
// }
//
// private void showStatus(String message, boolean isError) {
// if (statusLabel != null) {
// statusLabel.setText(message);
// statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill:
// green;");
// }
// }
// }
