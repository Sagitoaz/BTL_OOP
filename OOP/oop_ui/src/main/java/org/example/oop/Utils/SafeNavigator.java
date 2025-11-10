package org.example.oop.Utils;

// dùng để xử lí lỗi khi điều hướng 

import javafx.application.Platform;

public class SafeNavigator {
     /**
      * Navigate to scene với error handling và callback
      *
      * @param fxmlPath Đường dẫn FXML
      * @param title    Tiêu đề cửa sổ
      * @param onError  Callback khi có lỗi (optional)
      */
     public static void navigate(String fxmlPath, String title, Runnable onError) {
          try {
               System.out.println("🔄 Navigating to: " + fxmlPath);
               SceneManager.switchScene(fxmlPath, title);
               System.out.println("✅ Navigation successful");
          } catch (Exception e) {
               System.err.println("Chuyển hướng thất bại :" + e.getMessage());
               e.printStackTrace();
               Platform.runLater(() -> {
                    ErrorHandler.showCustomError(500, "Không thể mở màn hình " + title + "\n\n" +
                              "Chi tiết lỗi: " + e.getMessage() + "\n\n" +
                              "Vui lòng thử lại hoặc liên hệ quản trị viên.");
                    if (onError == null) {
                         onError.run();
                    }
               });
          }
     }
}
