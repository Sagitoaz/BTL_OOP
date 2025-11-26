package org.example.oop.Control.PaymentControl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import org.example.oop.Control.BaseController;
import org.example.oop.Utils.SceneManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FinancialReportController extends BaseController implements Initializable {
    
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button reloadButton;
    @FXML
    private DatePicker dpFrom;
    @FXML
    private DatePicker dpTo;
    @FXML
    private Button btnApply;
    @FXML
    private Button btnExportCsv;
    @FXML
    private Button btnExportPdf;
    
    // Charts
    @FXML
    private LineChart<String, Number> chartRevenue;
    @FXML
    private PieChart chartMethod;
    @FXML
    private LineChart<String, Number> chartDailyRevenue;
    @FXML
    private BarChart<String, Number> chartByMethod;
    
    // Tables
    @FXML
    private TableView<?> tableTopProducts;
    @FXML
    private TableView<?> tableStaffPerf;
    @FXML
    private TableView<?> tableDaily;
    @FXML
    private TableView<?> tableByMethod;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🔵 FinancialReportController: Initializing...");
        
        // Set default date range (last 30 days)
        if (dpTo != null) {
            dpTo.setValue(LocalDate.now());
        }
        if (dpFrom != null) {
            dpFrom.setValue(LocalDate.now().minusDays(30));
        }
        
        System.out.println("✅ FinancialReportController: Initialization complete");
    }
    
    @FXML
    private void handleBackButton() {
        SceneManager.goBack();
    }
    
    @FXML
    private void handleForwardButton() {
        SceneManager.goForward();
    }
    
    @FXML
    private void handleReloadButton() {
        SceneManager.reloadCurrentScene();
    }
    
    @FXML
    private void onApplyFilter() {
        LocalDate from = dpFrom != null ? dpFrom.getValue() : null;
        LocalDate to = dpTo != null ? dpTo.getValue() : null;
        
        if (from != null && to != null && from.isAfter(to)) {
            showWarning("Ngày bắt đầu phải trước ngày kết thúc!");
            return;
        }
        
        showSuccess("Đang tải dữ liệu báo cáo...");

    }
    
    @FXML
    private void onExportCsv() {
        try {
            // Prepare headers for financial report
            java.util.List<String> headers = java.util.Arrays.asList(
                "Ngày", "Số hóa đơn", "Doanh thu", "Giảm giá", "Thuế", "Lợi nhuận"
            );
            
            // Sample data (replace with actual data when available)
            java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>();
            LocalDate from = dpFrom != null ? dpFrom.getValue() : LocalDate.now().minusDays(30);
            LocalDate to = dpTo != null ? dpTo.getValue() : LocalDate.now();
            
            // Add sample row
            java.util.List<Object> row = java.util.Arrays.asList(
                from.toString() + " - " + to.toString(), 0, 0, 0, 0, 0
            );
            data.add(row);
            
            // Generate filename and path
            String directory = org.example.oop.Utils.ExcelExporter.getDocumentsPath();
            org.example.oop.Utils.ExcelExporter.ensureDirectoryExists(directory);
            String fileName = org.example.oop.Utils.ExcelExporter.generateFileName("BaoCaoTaiChinh");
            String fullPath = directory + fileName;
            
            // Export to Excel
            org.example.oop.Utils.ExcelExporter.exportToFile(fullPath, "Báo cáo tài chính", headers, data);
            
            showSuccess("Đã xuất báo cáo tài chính ra file:\n" + fileName + "\n\nVị trí: " + fullPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xuất file CSV: " + e.getMessage());
        }
    }
    
    @FXML
    private void onExportPdf() {
        try {
            // Prepare headers for financial report
            java.util.List<String> headers = java.util.Arrays.asList(
                "Ngày", "Số hóa đơn", "Doanh thu", "Giảm giá", "Thuế", "Lợi nhuận"
            );
            
            // Sample data (replace with actual data when available)
            java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>();
            LocalDate from = dpFrom != null ? dpFrom.getValue() : LocalDate.now().minusDays(30);
            LocalDate to = dpTo != null ? dpTo.getValue() : LocalDate.now();
            
            // Add sample row
            java.util.List<Object> row = java.util.Arrays.asList(
                from.toString() + " - " + to.toString(), 0, 0, 0, 0, 0
            );
            data.add(row);
            
            // Generate filename and path
            String directory = org.example.oop.Utils.PDFExporter.getDocumentsPath();
            org.example.oop.Utils.PDFExporter.ensureDirectoryExists(directory);
            String fileName = org.example.oop.Utils.PDFExporter.generateFileName("BaoCaoTaiChinh");
            String fullPath = directory + fileName;
            
            // Export to PDF
            String title = "BÁO CÁO TÀI CHÍNH\nTừ " + from.toString() + " đến " + to.toString();
            org.example.oop.Utils.PDFExporter.exportToFile(fullPath, title, headers, data);
            
            showSuccess("Đã xuất báo cáo tài chính ra file:\n" + fileName + "\n\nVị trí: " + fullPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xuất file PDF: " + e.getMessage());
        }
    }
}
