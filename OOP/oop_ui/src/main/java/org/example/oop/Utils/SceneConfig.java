package org.example.oop.Utils;

public final class SceneConfig {

    // Private constructor to prevent instantiation
    private SceneConfig() {
    }

    // ========== Dashboard Scene ==========
    public static final String ADMIN_DASHBOARD_FXML = "/FXML/Dashboard/AdminDashboard.fxml";
   // public static final String EMPLOYEE_DASHBOARD_FXML = "/FXML/Dashboard/EmployeeDashboard.fxml";
    public static final String CUSTOMER_DASHBOARD_FXML = "/FXML/Dashboard/CustomerDashboard.fxml";
    public static final String DOCTOR_DASHBOARD_FXML = "/FXML/Dashboard/DoctorDashboard.fxml";

    // ========== AUTHENTICATION SCENES ==========
    public static final String LOGIN_FXML = "/FXML/Login.fxml";
    public static final String CHANGE_PASSWORD_FXML = "/FXML/ChangePassword.fxml";
    public static final String RESET_PASSWORD_FXML = "/FXML/ResetPassword.fxml";
    public static final String SIGNUP_FXML = "/FXML/Signup.fxml";

    // ========== INVENTORY SCENES ==========
    public static final String ADD_INVENTORY_VIEW_FXML = "/FXML/Inventory/AddInventoryView.fxml";
    public static final String PRODUCT_CRUD_VIEW_FXML = "/FXML/Inventory/ProductCRUDView.fxml";
    public static final String SEARCH_INVENTORY_VIEW_FXML = "/FXML/Inventory/SearchInventoryView.fxml";
    public static final String STOCK_MOVEMENT_VIEW_FXML = "/FXML/Inventory/StockMovementView.fxml";

    // ========== PATIENT AND PRESCRIPTION SCENES ==========
    public static final String ADD_CUSTOMER_VIEW_FXML = "/FXML/PatientAndPrescription/AddCustomerView.fxml";
    public static final String CUSTOMER_HUB_FXML = "/FXML/PatientAndPrescription/CustomerHub.fxml";
    public static final String PRESCRIPTION_EDITOR_FXML = "/FXML/PatientAndPrescription/PrescriptionEditor.fxml";
    public static final String CUSTOMER_DETAIL_FXML = "/FXML/PatientAndPrescription/CustomerDetailView.fxml";

    // ========== PAYMENT SCENES ==========
    public static final String PAYMENT_FXML = "/FXML/PaymentFXML/Payment.fxml";
    public static final String INVOICE_FXML = "/FXML/PaymentFXML/Invoice.fxml";
    public static final String RECEIPT_FXML = "/FXML/PaymentFXML/Receipt.fxml";
    public static final String PAYMENT_HISTORY_FXML = "/FXML/PaymentFXML/PaymentHistory.fxml";
    public static final String FINANCIAL_REPORT_FXML = "/FXML/PaymentFXML/FinancialReport.fxml";

    // ========== SCHEDULE SCENES ==========
    public static final String APPOINTMENT_BOOKING_FXML = "/FXML/Schedule/AppointmentBooking.fxml";
    public static final String APPOINTMENT_MANAGEMENT_FXML = "/FXML/Schedule/AppointmentManagement.fxml";
    public static final String CALENDAR_FXML = "/FXML/Schedule/Calendar.fxml";
    public static final String DOCTOR_SCHEDULE_FXML = "/FXML/Schedule/DoctorSchedule.fxml";

    // ========== EMPLOYEE SCENES ==========
    public static final String EMPLOYEE_MANAGEMENT_FXML = "/FXML/Employee/EmployeeManagement.fxml";
    public static final String EMPLOYEE_FORM_FXML = "/FXML/Employee/EmployeeForm.fxml";
    public static final String EMPLOYEE_EDIT_FORM_FXML = "/FXML/Employee/EmployeeEditForm.fxml";
    public static final String EMPLOYEE_DETAIL_FXML = "/FXML/Employee/EmployeeDetail.fxml";

    // ========== SCENE TITLES ==========
    public static final class Titles {
        private Titles() {
        }
        public static final String DASHBOARD = "Phòng kham mắt GOAT DŨNG - Eye Clinic";

        // Authentication
        public static final String LOGIN = "Đăng nhập - Eye Clinic";
        public static final String SIGNUP = "Đăng ký tài khoản - Eye Clinic";
        public static final String CHANGE_PASSWORD = "Đổi mật khẩu - Eye Clinic";
        public static final String RESET_PASSWORD = "Khôi phục mật khẩu - Eye Clinic";

        // Inventory
        public static final String ADD_INVENTORY = "Thêm hàng tồn kho - Eye Clinic";
        public static final String PRODUCT_CRUD = "Quản lý sản phẩm - Eye Clinic";
        public static final String SEARCH_INVENTORY = "Tìm kiếm kho - Eye Clinic";
        public static final String STOCK_MOVEMENT = "Xuất nhập kho - Eye Clinic";

        // Patient & Prescription
        public static final String ADD_CUSTOMER = "Thêm và Chỉnh sửa Hồ sơ bệnh nhân  - Eye Clinic";
        public static final String CUSTOMER_HUB = "Quản lý bệnh nhân - Eye Clinic";
        public static final String PRESCRIPTION_EDITOR = "Toa thuốc - Eye Clinic";
        public static final String CUSTOMER_DETAIL = "Chi tiết bệnh nhân - Eye Clinic";

        // Payment
        public static final String PAYMENT = "Thanh toán - Eye Clinic";
        public static final String INVOICE = "Hóa đơn - Eye Clinic";
        public static final String RECEIPT = "Biên lai - Eye Clinic";
        public static final String PAYMENT_HISTORY = "Lịch sử thanh toán - Eye Clinic";
        public static final String FINANCIAL_REPORT = "Báo cáo tài chính - Eye Clinic";

        // Schedule
        public static final String APPOINTMENT_BOOKING = "Đặt lịch hẹn - Eye Clinic";
        public static final String APPOINTMENT_MANAGEMENT = "Quản lý lịch hẹn - Eye Clinic";
        public static final String CALENDAR = "Lịch làm việc - Eye Clinic";
        public static final String DOCTOR_SCHEDULE = "Lịch bác sĩ - Eye Clinic";

        // Employee
        public static final String EMPLOYEE_MANAGEMENT = "Quản lý nhân viên - Eye Clinic";
        public static final String EMPLOYEE_FORM = "Thêm nhân viên - Eye Clinic";
        public static final String EMPLOYEE_EDIT_FORM = "Chỉnh sửa nhân viên - Eye Clinic";
        public static final String EMPLOYEE_DETAIL = "Chi tiết nhân viên - Eye Clinic";
    }
}
