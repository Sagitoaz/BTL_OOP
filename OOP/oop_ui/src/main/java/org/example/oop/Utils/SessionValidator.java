package org.example.oop.Utils;

import org.miniboot.app.domain.models.Employee;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.example.oop.Control.SessionStorage;

import javafx.application.Platform;

/**
 * SessionValidator - Kiểm tra tính hợp lệ của session
 * 
 * Sử dụng: Gọi ở đầu initialize() của mỗi Dashboard/Controller
 */
public class SessionValidator {
     /**
      * Validate employee session
      * 
      * @return true nếu valid, false nếu invalid (sẽ redirect về login)
      */
     public static boolean validateEmployeeSession() {
          Employee employee = SceneManager.getSceneData("accountData");
          if (employee == null) {
               System.err.println("❌ Employee Session invalid: accountData is null");
               Platform.runLater(() -> {
                    // Hiển thị thông báo lỗi
                    ErrorHandler.showCustomError(401, 
                         "Phiên đăng nhập đã hết hạn.\n\n" +
                         "Vui lòng đăng nhập lại để tiếp tục.");
                    
                    // Clear session data (KHÔNG cần clearHistory)
                    SceneManager.clearSceneData();
                    
                    // Redirect to login
                    SceneManager.switchScene(
                         SceneConfig.LOGIN_FXML,
                         SceneConfig.Titles.LOGIN);
               });
               return false;
          }
          String token = SceneManager.getSceneData("authToken");
          if (token == null || token.isEmpty()) {
               System.err.println("auth token missing");
          }
          return true;
     }

     /**
      * Validate customer session
      * 
      * @return true nếu session hợp lệ
      */
     public static boolean validateCustomerSession() {
          Customer customer = SceneManager.getSceneData("accountData");
          
          if (customer == null) {
               System.err.println("❌ Customer Session invalid: accountData is null");
               Platform.runLater(() -> {
                    ErrorHandler.showCustomError(401,
                         "Phiên đăng nhập đã hết hạn.\n\n" +
                         "Vui lòng đăng nhập lại để tiếp tục.");
                    
                    SceneManager.clearSceneData();
                    SceneManager.switchScene(
                         SceneConfig.LOGIN_FXML,
                         SceneConfig.Titles.LOGIN);
               });
               return false;
          }
          
          System.out.println("✅ Customer session valid: " + customer.getUsername());
          return true;
     }

     /**
      * Validate role-based permission
      * 
      * @param requiredRoles Các role được phép (VD: "ADMIN", "DOCTOR")
      * @return true nếu user có quyền
      */
     public static boolean validatePermission(String... requiredRoles) {
          Employee employee = SceneManager.getSceneData("accountData");
          
          if (employee == null) {
               System.err.println("❌ Permission check failed: No employee data");
               return false;
          }

          String userRole = employee.getRole().toUpperCase();
          
          for (String role : requiredRoles) {
               if (userRole.equals(role.toUpperCase())) {
                    System.out.println("✅ Permission granted: " + userRole + " can access " + role);
                    return true;
               }
          }

          System.err.println("❌ Permission denied: " + userRole + " cannot access " + String.join(", ", requiredRoles));
          
          Platform.runLater(() -> {
               ErrorHandler.showCustomError(403,
                    "Bạn không có quyền truy cập chức năng này.\n\n" +
                    "Vai trò của bạn: " + userRole + "\n" +
                    "Yêu cầu: " + String.join(" hoặc ", requiredRoles));
          });
          
          return false;
     }

     /**
      * Lấy employee data một cách an toàn
      * 
      * @return Employee hoặc null nếu không có
      */
     public static Employee getSafeEmployeeData() {
          try {
               return SceneManager.getSceneData("accountData");
          } catch (Exception e) {
               System.err.println("❌ Error getting employee data: " + e.getMessage());
               return null;
          }
     }

     /**
      * Lấy customer data một cách an toàn
      * 
      * @return Customer hoặc null nếu không có
      */
     public static Customer getSafeCustomerData() {
          try {
               return SceneManager.getSceneData("accountData");
          } catch (Exception e) {
               System.err.println("❌ Error getting customer data: " + e.getMessage());
               return null;
          }
     }

     /**
      * Kiểm tra xem có data trong SceneManager hay không
      * 
      * @param key Key cần kiểm tra
      * @return true nếu tồn tại và không null
      */
     public static boolean hasData(String key) {
          Object data = SceneManager.getSceneData(key);
          return data != null;
     }
}
