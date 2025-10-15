package org.example.oop.Control;

import java.util.Timer;
import java.util.TimerTask;

import org.example.oop.Model.Alert.Alert;
import org.example.oop.Utils.ApiClient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * 🚨 ALERT CONTROLLER - NGÀY 8 FRONTEND INTEGRATION
 * Controller cho alert panel trong JavaFX UI với real-time updates
 */
public class AlertController {

     @FXML
     private TableView<Alert> tblAlerts;

     @FXML
     private TableColumn<Alert, Integer> colId;

     @FXML
     private TableColumn<Alert, String> colProduct;

     @FXML
     private TableColumn<Alert, String> colType;

     @FXML
     private TableColumn<Alert, String> colPriority;

     @FXML
     private TableColumn<Alert, String> colMessage;

     @FXML
     private TableColumn<Alert, String> colCreated;

     @FXML
     private TableColumn<Alert, String> colStatus;

     @FXML
     private TableColumn<Alert, Void> colActions;

     @FXML
     private Button btnRefresh;

     @FXML
     private Button btnCheckAlerts;

     @FXML
     private Label lblAlertCount;

     @FXML
     private Label lblStatus;

     @FXML
     private ComboBox<String> cbPriority;

     @FXML
     private CheckBox chkShowResolved;

     // Data và Services
     private final ApiClient apiClient = ApiClient.getInstance();
     private final ObservableList<Alert> allAlerts = FXCollections.observableArrayList();
     private final ObservableList<Alert> filteredAlerts = FXCollections.observableArrayList();
     private Timer refreshTimer;

     @FXML
     public void initialize() {
          try {
               initializeTable();
               initializeControls();
               loadAlerts();
               startAutoRefresh();
               updateStatus("Alert system khởi tạo thành công");
          } catch (Exception e) {
               updateStatus("❌ Lỗi khởi tạo: " + e.getMessage());
               e.printStackTrace();
          }
     }

     private void initializeTable() {
          // Setup columns
          colId.setCellValueFactory(new PropertyValueFactory<>("id"));
          colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
          colType.setCellValueFactory(new PropertyValueFactory<>("alertType"));
          colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
          colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
          colCreated.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedAt"));
          colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));

          // Priority column với colored indicator
          colPriority.setCellFactory(col -> new TableCell<Alert, String>() {
               @Override
               protected void updateItem(String priority, boolean empty) {
                    super.updateItem(priority, empty);
                    if (empty || priority == null) {
                         setText(null);
                         setGraphic(null);
                    } else {
                         setText(priority);

                         // Add colored circle indicator
                         Circle indicator = new Circle(5);
                         switch (priority.toUpperCase()) {
                              case "HIGH":
                                   indicator.setFill(Color.RED);
                                   break;
                              case "MEDIUM":
                                   indicator.setFill(Color.ORANGE);
                                   break;
                              case "LOW":
                              default:
                                   indicator.setFill(Color.YELLOW);
                                   break;
                         }

                         HBox container = new HBox(5);
                         container.getChildren().addAll(indicator, new Label(priority));
                         setGraphic(container);
                         setText(null);
                    }
               }
          });

          // Actions column với resolve button
          colActions.setCellFactory(col -> new TableCell<Alert, Void>() {
               private final Button resolveBtn = new Button("Giải quyết");

               {
                    resolveBtn.getStyleClass().add("btn-primary");
                    resolveBtn.setOnAction(e -> {
                         Alert alert = getTableView().getItems().get(getIndex());
                         resolveAlert(alert);
                    });
               }

               @Override
               protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                         setGraphic(null);
                    } else {
                         Alert alert = getTableView().getItems().get(getIndex());
                         if (alert.isResolved()) {
                              resolveBtn.setText("Đã giải quyết");
                              resolveBtn.setDisable(true);
                         } else {
                              resolveBtn.setText("Giải quyết");
                              resolveBtn.setDisable(false);
                         }
                         setGraphic(resolveBtn);
                    }
               }
          });

          tblAlerts.setItems(filteredAlerts);
          tblAlerts.setRowFactory(tv -> {
               TableRow<Alert> row = new TableRow<>();
               row.itemProperty().addListener((obs, oldAlert, newAlert) -> {
                    if (newAlert == null) {
                         row.setStyle("");
                    } else {
                         // Style based on priority
                         String style = "";
                         switch (newAlert.getPriority().toUpperCase()) {
                              case "HIGH":
                                   style = "-fx-background-color: #ffebee;";
                                   break;
                              case "MEDIUM":
                                   style = "-fx-background-color: #fff3e0;";
                                   break;
                              case "LOW":
                                   style = "-fx-background-color: #fffde7;";
                                   break;
                         }
                         if (newAlert.isResolved()) {
                              style += "-fx-opacity: 0.6;";
                         }
                         row.setStyle(style);
                    }
               });
               return row;
          });
     }

     private void initializeControls() {
          // Priority filter
          cbPriority.setItems(FXCollections.observableArrayList("Tất cả", "HIGH", "MEDIUM", "LOW"));
          cbPriority.setValue("Tất cả");
          cbPriority.setOnAction(e -> filterAlerts());

          // Show resolved checkbox
          chkShowResolved.setOnAction(e -> filterAlerts());

          // Buttons
          btnRefresh.setOnAction(e -> loadAlerts());
          btnCheckAlerts.setOnAction(e -> manualCheckAlerts());

          // Initial filter
          filterAlerts();
     }

     private void loadAlerts() {
          updateStatus("🔄 Đang tải alerts...");

          apiClient.getAsync("/api/alerts", response -> {
               if (response.isSuccess()) {
                    try {
                         allAlerts.clear();
                         allAlerts.addAll(parseAlertsFromJson(response.getData()));
                         filterAlerts();
                         updateAlertCount();
                         updateStatus("✅ Đã tải " + allAlerts.size() + " alerts");
                    } catch (Exception e) {
                         updateStatus("❌ Lỗi phân tích alerts: " + e.getMessage());
                    }
               } else {
                    updateStatus("❌ Lỗi tải alerts: " + response.getErrorMessage());
               }
          }, errorMessage -> {
               updateStatus("❌ Lỗi kết nối: " + errorMessage);
          });
     }

     private void manualCheckAlerts() {
          updateStatus("🔄 Đang kiểm tra alerts mới...");
          btnCheckAlerts.setDisable(true);

          apiClient.postAsync("/api/alerts/check", "", response -> {
               btnCheckAlerts.setDisable(false);
               if (response.isSuccess()) {
                    updateStatus("✅ Đã kiểm tra alerts, tự động tải lại...");
                    // Reload alerts after check
                    Platform.runLater(() -> loadAlerts());
               } else {
                    updateStatus("❌ Lỗi kiểm tra alerts: " + response.getErrorMessage());
               }
          }, errorMessage -> {
               btnCheckAlerts.setDisable(false);
               updateStatus("❌ Lỗi kết nối: " + errorMessage);
          });
     }

     private void resolveAlert(Alert alert) {
          if (alert.isResolved())
               return;

          updateStatus("🔄 Đang giải quyết alert...");

          String endpoint = "/api/alerts/" + alert.getId() + "/resolve";
          apiClient.putAsync(endpoint, "", response -> {
               if (response.isSuccess()) {
                    alert.setResolved(true);
                    tblAlerts.refresh();
                    updateAlertCount();
                    updateStatus("✅ Đã giải quyết alert: " + alert.getMessage());
               } else {
                    updateStatus("❌ Lỗi giải quyết alert: " + response.getErrorMessage());
               }
          }, errorMessage -> {
               updateStatus("❌ Lỗi kết nối: " + errorMessage);
          });
     }

     private void filterAlerts() {
          filteredAlerts.clear();

          String priorityFilter = cbPriority.getValue();
          boolean showResolved = chkShowResolved.isSelected();

          for (Alert alert : allAlerts) {
               // Priority filter
               if (!"Tất cả".equals(priorityFilter) && !priorityFilter.equals(alert.getPriority())) {
                    continue;
               }

               // Resolved filter
               if (!showResolved && alert.isResolved()) {
                    continue;
               }

               filteredAlerts.add(alert);
          }

          updateAlertCount();
     }

     private void updateAlertCount() {
          int totalAlerts = allAlerts.size();
          int activeAlerts = (int) allAlerts.stream().filter(a -> !a.isResolved()).count();
          int filteredCount = filteredAlerts.size();

          lblAlertCount.setText(String.format("%d/%d alerts (Hoạt động: %d)",
                    filteredCount, totalAlerts, activeAlerts));
     }

     private void updateStatus(String message) {
          lblStatus.setText(message);
          System.out.println("📝 Alert Status: " + message);
     }

     private void startAutoRefresh() {
          if (refreshTimer != null) {
               refreshTimer.cancel();
          }

          refreshTimer = new Timer(true); // Daemon timer
          refreshTimer.scheduleAtFixedRate(new TimerTask() {
               @Override
               public void run() {
                    Platform.runLater(() -> loadAlerts());
               }
          }, 30000, 30000); // Refresh every 30 seconds
     }

     public void stopAutoRefresh() {
          if (refreshTimer != null) {
               refreshTimer.cancel();
               refreshTimer = null;
          }
     }

     /**
      * Simple JSON parser for alerts list
      */
     private ObservableList<Alert> parseAlertsFromJson(String json) {
          ObservableList<Alert> alerts = FXCollections.observableArrayList();

          try {
               // Basic JSON parsing for alerts
               if (json.contains("\"alerts\"")) {
                    int alertsStart = json.indexOf("\"alerts\":[") + 10;
                    int alertsEnd = json.indexOf("]", alertsStart);

                    if (alertsStart > 9 && alertsEnd > alertsStart) {
                         String alertsJson = json.substring(alertsStart, alertsEnd);
                         String[] items = alertsJson.split("\\},\\s*\\{");

                         for (String item : items) {
                              if (!item.trim().isEmpty()) {
                                   Alert alert = parseAlertFromJson(item);
                                   if (alert != null) {
                                        alerts.add(alert);
                                   }
                              }
                         }
                    }
               }

          } catch (Exception e) {
               System.err.println("Error parsing alerts JSON: " + e.getMessage());
          }

          return alerts;
     }

     /**
      * Parse single alert from JSON
      */
     private Alert parseAlertFromJson(String json) {
          try {
               Alert alert = new Alert();

               alert.setId((int) extractLongField(json, "id", 0L));
               alert.setProductId((int) extractLongField(json, "productId", 0L));
               alert.setProductName(extractStringField(json, "productName", ""));
               alert.setAlertType(extractStringField(json, "alertType", ""));
               alert.setPriority(extractStringField(json, "priority", "LOW"));
               alert.setMessage(extractStringField(json, "message", ""));
               alert.setResolved(extractBooleanField(json, "isResolved", false));
               alert.setCurrentStock((int) extractLongField(json, "currentStock", 0L));
               alert.setMinStock((int) extractLongField(json, "minStock", 0L));

               return alert;

          } catch (Exception e) {
               System.err.println("Error parsing alert item: " + e.getMessage());
               return null;
          }
     }

     // JSON field extractors (reuse from other controllers)
     private String extractStringField(String json, String field, String defaultValue) {
          try {
               String pattern = "\"" + field + "\":\"";
               int start = json.indexOf(pattern);
               if (start == -1)
                    return defaultValue;

               start += pattern.length();
               int end = json.indexOf("\"", start);
               if (end == -1)
                    return defaultValue;

               return json.substring(start, end);
          } catch (Exception e) {
               return defaultValue;
          }
     }

     private long extractLongField(String json, String field, long defaultValue) {
          try {
               String pattern = "\"" + field + "\":";
               int start = json.indexOf(pattern);
               if (start == -1)
                    return defaultValue;

               start += pattern.length();
               int end = json.indexOf(",", start);
               if (end == -1)
                    end = json.indexOf("}", start);
               if (end == -1)
                    return defaultValue;

               String value = json.substring(start, end).trim();
               return Long.parseLong(value);
          } catch (Exception e) {
               return defaultValue;
          }
     }

     private boolean extractBooleanField(String json, String field, boolean defaultValue) {
          try {
               String pattern = "\"" + field + "\":";
               int start = json.indexOf(pattern);
               if (start == -1)
                    return defaultValue;

               start += pattern.length();
               int end = json.indexOf(",", start);
               if (end == -1)
                    end = json.indexOf("}", start);
               if (end == -1)
                    return defaultValue;

               String value = json.substring(start, end).trim();
               return Boolean.parseBoolean(value);
          } catch (Exception e) {
               return defaultValue;
          }
     }
}