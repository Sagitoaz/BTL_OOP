package org.example.oop.config;

/**
 * ApiConstants: Chứa các hằng số cho API Client (UI side)
 */
public final class ApiConstants {

    // Private constructor để ngăn khởi tạo
    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    //  HTTP HEADERS 
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    //  CONTENT TYPES 
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    //  HTTP METHODS 
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    //  TIMEOUT SETTINGS 
    public static final int CONNECTION_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;
    public static final int RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 1000;

    //  RESPONSE CODES 
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    //  API RESPONSE FIELDS 
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_ERROR = "error";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_TOTAL = "total";
    public static final String FIELD_PAGE = "page";
    public static final String FIELD_PAGE_SIZE = "pageSize";

    //  QUERY PARAMETERS 
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_ORDER = "order";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_FILTER = "filter";
    public static final String PARAM_ID = "id";

    //  ERROR MESSAGES 
    public static final String ERROR_CONNECTION_FAILED = "Không thể kết nối đến server";
    public static final String ERROR_TIMEOUT = "Yêu cầu quá hạn";
    public static final String ERROR_UNAUTHORIZED = "Chưa đăng nhập hoặc phiên đã hết hạn";
    public static final String ERROR_FORBIDDEN = "Không có quyền truy cập";
    public static final String ERROR_NOT_FOUND = "Không tìm thấy dữ liệu";
    public static final String ERROR_BAD_REQUEST = "Yêu cầu không hợp lệ";
    public static final String ERROR_SERVER = "Lỗi từ server";
    public static final String ERROR_PARSE_JSON = "Không thể phân tích dữ liệu JSON";
}
