package org.example.oop.config;

/**
 * UIConstants: Chứa tất cả các hằng số cho UI (JavaFX)
 */
public final class UIConstants {

    // Private constructor để ngăn khởi tạo
    private UIConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    //  WINDOW SETTINGS 
    public static final String APP_TITLE = "Phòng khám mắt - Eye Clinic";
    public static final int DEFAULT_WINDOW_WIDTH = 1200;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    //  CSS STYLES 
    public static final String CSS_MAIN = "/css/main.css";
    public static final String CSS_DASHBOARD = "/css/dashboard.css";
    public static final String CSS_FORM = "/css/form.css";
    public static final String CSS_TABLE = "/css/table.css";

    //  IMAGES 
    public static final String IMAGE_LOGO = "/Image/logo.png";
    public static final String IMAGE_ICON = "/Image/icon.png";
    public static final String IMAGE_USER_DEFAULT = "/Image/user-default.png";
    public static final String IMAGE_DOCTOR = "/Image/doctor.png";
    public static final String IMAGE_NURSE = "/Image/nurse.png";

    //  COLORS 
    public static final String COLOR_PRIMARY = "#2196F3";
    public static final String COLOR_SUCCESS = "#4CAF50";
    public static final String COLOR_WARNING = "#FF9800";
    public static final String COLOR_ERROR = "#F44336";
    public static final String COLOR_INFO = "#00BCD4";

    //  BUTTON LABELS 
    public static final String BTN_SAVE = "Lưu";
    public static final String BTN_CANCEL = "Hủy";
    public static final String BTN_DELETE = "Xóa";
    public static final String BTN_EDIT = "Sửa";
    public static final String BTN_ADD = "Thêm";
    public static final String BTN_SEARCH = "Tìm kiếm";
    public static final String BTN_REFRESH = "Làm mới";
    public static final String BTN_BACK = "Quay lại";
    public static final String BTN_NEXT = "Tiếp theo";
    public static final String BTN_FINISH = "Hoàn thành";
    public static final String BTN_LOGIN = "Đăng nhập";
    public static final String BTN_LOGOUT = "Đăng xuất";
    public static final String BTN_SUBMIT = "Gửi";
    public static final String BTN_CLOSE = "Đóng";
    public static final String BTN_CONFIRM = "Xác nhận";

    //  VALIDATION MESSAGES 
    public static final String VALIDATION_REQUIRED = "Trường này không được để trống";
    public static final String VALIDATION_EMAIL = "Email không hợp lệ";
    public static final String VALIDATION_PHONE = "Số điện thoại không hợp lệ";
    public static final String VALIDATION_PASSWORD_MIN = "Mật khẩu phải có ít nhất 6 ký tự";
    public static final String VALIDATION_PASSWORD_MISMATCH = "Mật khẩu không khớp";
    public static final String VALIDATION_NUMBER = "Vui lòng nhập số hợp lệ";
    public static final String VALIDATION_DATE = "Ngày không hợp lệ";

    //  ALERT MESSAGES 
    public static final String ALERT_SUCCESS = "Thành công";
    public static final String ALERT_ERROR = "Lỗi";
    public static final String ALERT_WARNING = "Cảnh báo";
    public static final String ALERT_INFO = "Thông tin";
    public static final String ALERT_CONFIRM = "Xác nhận";

    //  DIALOG MESSAGES 
    public static final String DIALOG_DELETE_CONFIRM = "Bạn có chắc chắn muốn xóa?";
    public static final String DIALOG_SAVE_CONFIRM = "Bạn có chắc chắn muốn lưu?";
    public static final String DIALOG_CANCEL_CONFIRM = "Bạn có chắc chắn muốn hủy?";
    public static final String DIALOG_LOGOUT_CONFIRM = "Bạn có chắc chắn muốn đăng xuất?";
    public static final String DIALOG_EXIT_CONFIRM = "Bạn có chắc chắn muốn thoát?";

    //  TABLE HEADERS 
    public static final String TBL_ID = "ID";
    public static final String TBL_NAME = "Tên";
    public static final String TBL_EMAIL = "Email";
    public static final String TBL_PHONE = "Số điện thoại";
    public static final String TBL_ADDRESS = "Địa chỉ";
    public static final String TBL_DATE = "Ngày";
    public static final String TBL_TIME = "Giờ";
    public static final String TBL_STATUS = "Trạng thái";
    public static final String TBL_ACTION = "Hành động";
    public static final String TBL_CREATED_AT = "Ngày tạo";
    public static final String TBL_UPDATED_AT = "Ngày cập nhật";

    //  DATE/TIME FORMATS 
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";

    //  PAGINATION 
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int[] PAGE_SIZE_OPTIONS = {10, 25, 50, 100};
    public static final String PAGINATION_INFO = "Hiển thị %d-%d trong tổng số %d";

    //  STATUS VALUES 
    public static final String STATUS_ACTIVE = "Đang hoạt động";
    public static final String STATUS_INACTIVE = "Không hoạt động";
    public static final String STATUS_PENDING = "Đang chờ";
    public static final String STATUS_CONFIRMED = "Đã xác nhận";
    public static final String STATUS_CANCELLED = "Đã hủy";
    public static final String STATUS_COMPLETED = "Đã hoàn thành";

    //  LOADING MESSAGES 
    public static final String LOADING_DEFAULT = "Đang tải...";
    public static final String LOADING_SAVING = "Đang lưu...";
    public static final String LOADING_DELETING = "Đang xóa...";
    public static final String LOADING_LOADING_DATA = "Đang tải dữ liệu...";
    public static final String LOADING_PROCESSING = "Đang xử lý...";

    //  ERROR MESSAGES 
    public static final String ERROR_LOAD_SCENE = "Không thể tải màn hình";
    public static final String ERROR_LOAD_DATA = "Không thể tải dữ liệu";
    public static final String ERROR_SAVE_DATA = "Không thể lưu dữ liệu";
    public static final String ERROR_DELETE_DATA = "Không thể xóa dữ liệu";
    public static final String ERROR_CONNECTION = "Không thể kết nối đến server";
    public static final String ERROR_UNKNOWN = "Đã xảy ra lỗi không xác định";

    //  SUCCESS MESSAGES 
    public static final String SUCCESS_SAVE = "Lưu thành công";
    public static final String SUCCESS_DELETE = "Xóa thành công";
    public static final String SUCCESS_UPDATE = "Cập nhật thành công";
    public static final String SUCCESS_CREATE = "Tạo mới thành công";
    public static final String SUCCESS_LOGIN = "Đăng nhập thành công";
    public static final String SUCCESS_LOGOUT = "Đăng xuất thành công";
}

