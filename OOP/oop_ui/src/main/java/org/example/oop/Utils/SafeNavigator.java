package org.example.oop.Utils;

// dùng để xử lí lỗi khi điều hướng 

import javafx.application.Platform;

public class SafeNavigator {
     /**
      * Navigate to scene với error handling
      *
      * @param fxmlPath Đường dẫn FXML (dùng SceneConfig constants)
      * @param title    Tiêu đề cửa sổ
      */
     public static void navigate(String fxmlPath, String title) {
          navigate(fxmlPath, title, null);
     }

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

     // chuyển hướng với data
     public static void navigateWithData(String fxmlPath, String title, String[] keys, Object[] data) {
          try {
               System.err.println("Navigating with data to  " + fxmlPath);
               SceneManager.switchSceneWithData(fxmlPath, title, keys, data);
               System.out.println("navigation with data successful");
          } catch (Exception e) {
               System.err.println("Navigation with data fail " + e.getMessage());
               e.printStackTrace();
               Platform.runLater(() -> {
                    ErrorHandler.showCustomError(500, "Không thể mở màn hình " + title + "\n\nLỗi: " + e.getMessage());
               });
          }
     }

     // điều hướng với quyền

     public static void navigateWithPermissionCheck(String role, String module, String fxmlPath, String title) {
          if (!hasPermission(role, module)) {
               Platform.runLater(() -> {
                    ErrorHandler.showUserFriendlyError(403, "Bạn không có quyền try cập " + title);
               });
               return;
          }
          navigate(fxmlPath, title);
     }

     // mở cửa sổ và xử lí lỗi
     public static void openModal(String fxmlPath, String title, Runnable onClose) {
          try {
               System.out.println("Opening modal" + fxmlPath);
               SceneManager.openModalWindow(fxmlPath, title, onClose);
               System.out.println("modal opened successful");
          } catch (Exception e) {
               System.err.println("❌ Failed to open modal: " + e.getMessage());
               e.printStackTrace();

               Platform.runLater(() -> {
                    ErrorHandler.showCustomError(500,
                              "Không thể mở cửa sổ " + title + "\n\nLỗi: " + e.getMessage());
               });
          }
     }

     // xử lí lỗi khi quay lại
     public static void goBack() {
          try {
               SceneManager.goBack();
          } catch (Exception e) {
               System.err.println("❌ Cannot go back: " + e.getMessage());
               Platform.runLater(() -> {
                    ErrorHandler.showWarning("Không thể quay lại trang trước");
               });
          }
     }
     // xử lí lỗi với đi tiếp :v

     public static void goForward() {
          try {
               SceneManager.goForward();
          } catch (Exception e) {
               System.err.println("❌ Cannot go forward: " + e.getMessage());
               Platform.runLater(() -> {
                    ErrorHandler.showWarning("Không thể tiến tới trang tiếp theo");
               });
          }
     }

     /**
      * Reload với error handling
      */
     public static void reload() {
          try {
               SceneManager.reloadCurrentScene();
          } catch (Exception e) {
               System.err.println("❌ Cannot reload: " + e.getMessage());
               Platform.runLater(() -> {
                    ErrorHandler.showError("Không thể tải lại trang.\n\nLỗi: " + e.getMessage());
               });
          }
     }

     private static boolean hasPermission(String role, String module) {
          if (role == null)
               return false;
          switch (module) {
               case "EMPLOYEE_MANAGEMENT":
                    return "admin".equalsIgnoreCase(role);

               case "DOCTOR_SCHEDULE":
                    return "admin".equalsIgnoreCase(role) ||
                              "doctor".equalsIgnoreCase(role);

               case "INVENTORY":
                    return "admin".equalsIgnoreCase(role) ||
                              "nurse".equalsIgnoreCase(role);

               case "PAYMENT":
                    return "admin".equalsIgnoreCase(role) ||
                              "nurse".equalsIgnoreCase(role);

               case "CUSTOMER_HUB":
               case "APPOINTMENT_MANAGEMENT":
                    return true; // All roles can access

               default:
                    return false;
          }
     }
}
