package org.example.oop.Control;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 * Base Controller với Task utilities để xử lý background operations
 * Tất cả controllers khác extends từ class này để có các method tiện ích
 */
public abstract class BaseController {

     /**
      * Chạy task trong background thread với callback
      *
      * @param <T>          Kiểu dữ liệu trả về từ background task
      * @param taskSupplier Supplier cung cấp dữ liệu (chạy trong background thread)
      * @param onSuccess    Callback khi thành công (tự động chạy trên UI thread)
      * @param onError      Callback khi có lỗi (tự động chạy trên UI thread)
      *
      *                     CÁCH DÙNG:
      *                     executeAsync(
      *                     () -> apiService.getData(), // Background: gọi API
      *                     data -> table.setItems(data), // Success: update UI
      *                     error -> showError(error) // Error: hiện thông báo
      *                     );
      */
     protected <T> void executeAsync(
               Supplier<T> taskSupplier,
               Consumer<T> onSuccess,
               Consumer<Throwable> onError) {

          // Tạo JavaFX Task
          Task<T> task = new Task<>() {
               @Override
               protected T call() throws Exception {
                    // Phần này chạy trong BACKGROUND THREAD
                    System.out.println("🔄 Task running in thread: " + Thread.currentThread().getName());
                    return taskSupplier.get();
               }
          };

          // Xử lý khi task thành công
          task.setOnSucceeded(event -> {
               // Phần này TỰ ĐỘNG chạy trên JavaFX Application Thread
               System.out.println("✅ Task succeeded, updating UI in thread: " + Thread.currentThread().getName());
               try {
                    T result = task.getValue();
                    onSuccess.accept(result);
               } catch (Exception e) {
                    onError.accept(e);
               }
          });

          // Xử lý khi task thất bại
          task.setOnFailed(event -> {
               // Phần này TỰ ĐỘNG chạy trên JavaFX Application Thread
               System.err.println("❌ Task failed in thread: " + Thread.currentThread().getName());
               Throwable exception = task.getException();
               onError.accept(exception);
          });

          // Khởi chạy background thread
          Thread backgroundThread = new Thread(task);
          backgroundThread.setDaemon(true); // Daemon thread tự động tắt khi app đóng
          backgroundThread.setName("API-Worker-" + System.currentTimeMillis());
          backgroundThread.start();
     }

     /**
      * Chạy task đơn giản với default error handler
      * Dùng khi không cần custom error handling
      */
     protected <T> void executeAsync(
               Supplier<T> taskSupplier,
               Consumer<T> onSuccess) {
          executeAsync(taskSupplier, onSuccess, this::handleError);
     }

     /**
      * Chạy task không trả về giá trị (Runnable)
      * Dùng cho operations như delete, update không cần return value
      */
     protected void executeAsync(Runnable runnable, Runnable onSuccess) {
          executeAsync(
                    () -> {
                         runnable.run();
                         return null;
                    },
                    result -> onSuccess.run(),
                    this::handleError);
     }

     /**
      * Default error handler - có thể override trong subclass
      * Hiển thị alert với thông báo lỗi user-friendly
      */
     protected void handleError(Throwable throwable) {
          throwable.printStackTrace();

          Platform.runLater(() -> {
               showAlert(Alert.AlertType.ERROR, "Lỗi kết nối",
                         "Không thể kết nối đến server.\n\n" +
                                   "Chi tiết: " + throwable.getMessage() + "\n\n" +
                                   "Vui lòng kiểm tra:\n" +
                                   "- Server đang chạy trên http://localhost:8080\n" +
                                   "- Kết nối mạng ổn định");
          });
     }

     /**
      * Hiển thị alert (an toàn cho UI thread)
      * Tự động check và chuyển sang UI thread nếu cần
      */
     protected void showAlert(Alert.AlertType type, String title, String message) {
          if (Platform.isFxApplicationThread()) {
               // Đã ở UI thread -> hiển thị trực tiếp
               Alert alert = new Alert(type);
               alert.setTitle(title);
               alert.setHeaderText(null);
               alert.setContentText(message);
               alert.showAndWait();
          } else {
               // Đang ở background thread -> chuyển sang UI thread
               Platform.runLater(() -> showAlert(type, title, message));
          }
     }

     /**
      * Update UI component an toàn (ensure chạy trên UI thread)
      * Dùng khi cần update UI từ bất kỳ đâu
      */
     protected void runOnUIThread(Runnable action) {
          if (Platform.isFxApplicationThread()) {
               action.run();
          } else {
               Platform.runLater(action);
          }
     }

     /**
      * Show success message - shortcut method
      */
     protected void showSuccess(String message) {
          showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
     }

     /**
      * Show error message - shortcut method
      */
     protected void showError(String message) {
          showAlert(Alert.AlertType.ERROR, "Lỗi", message);
     }

     /**
      * Show warning message - shortcut method
      */
     protected void showWarning(String message) {
          showAlert(Alert.AlertType.WARNING, "Cảnh báo", message);
     }
}