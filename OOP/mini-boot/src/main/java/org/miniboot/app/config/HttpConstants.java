package org.miniboot.app.config;

/**
 * HttpConstants: Chứa tất cả các hằng số HTTP
 * Tập trung các giá trị HTTP methods, headers, content types, status codes
 */
public final class HttpConstants {

    // Private constructor để ngăn khởi tạo
    private HttpConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== HTTP METHODS ==========
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_HEAD = "HEAD";

    // ========== HTTP VERSIONS ==========
    public static final String HTTP_VERSION_1_0 = "HTTP/1.0";
    public static final String HTTP_VERSION_1_1 = "HTTP/1.1";
    public static final String HTTP_VERSION_2_0 = "HTTP/2.0";

    // ========== REQUEST HEADERS ==========
    public static final String HEADER_AUTHORIZATION = "authorization";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_CONTENT_LENGTH = "content-length";
    public static final String HEADER_ACCEPT = "accept";
    public static final String HEADER_USER_AGENT = "user-agent";
    public static final String HEADER_HOST = "host";
    public static final String HEADER_CONNECTION = "connection";
    public static final String HEADER_COOKIE = "cookie";
    public static final String HEADER_REFERER = "referer";

    // ========== RESPONSE HEADERS ==========
    public static final String HEADER_DATE = "date";
    public static final String HEADER_SERVER = "server";
    public static final String HEADER_LOCATION = "location";
    public static final String HEADER_SET_COOKIE = "set-cookie";
    public static final String HEADER_CACHE_CONTROL = "cache-control";
    public static final String HEADER_EXPIRES = "expires";
    public static final String HEADER_ETAG = "etag";

    // ========== CORS HEADERS ==========
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String HEADER_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String HEADER_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    // ========== CONTENT TYPES ==========
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_TEXT_HTML_UTF8 = "text/html; charset=utf-8";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    // ========== CONNECTION VALUES ==========
    public static final String CONNECTION_CLOSE = "close";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";

    // ========== AUTHORIZATION TYPES ==========
    public static final String AUTH_TYPE_BEARER = "Bearer";
    public static final String AUTH_TYPE_BASIC = "Basic";

    // ========== CHARSET ==========
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    // ========== SERVER INFO ==========
    public static final String SERVER_NAME = "mini-boot";
    public static final String SERVER_VERSION = "1.0.0";
    public static final String SERVER_FULL_NAME = SERVER_NAME + "/" + SERVER_VERSION;

    // ========== HTTP STATUS CODES ==========
    // 2xx Success
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_ACCEPTED = 202;
    public static final int STATUS_NO_CONTENT = 204;

    // 3xx Redirection
    public static final int STATUS_MOVED_PERMANENTLY = 301;
    public static final int STATUS_FOUND = 302;
    public static final int STATUS_NOT_MODIFIED = 304;
    public static final int STATUS_TEMPORARY_REDIRECT = 307;
    public static final int STATUS_PERMANENT_REDIRECT = 308;

    // 4xx Client Errors
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    public static final int STATUS_REQUEST_TIMEOUT = 408;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_PAYLOAD_TOO_LARGE = 413;
    public static final int STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    public static final int STATUS_TOO_MANY_REQUESTS = 429;

    // 5xx Server Errors
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final int STATUS_BAD_GATEWAY = 502;
    public static final int STATUS_SERVICE_UNAVAILABLE = 503;
    public static final int STATUS_GATEWAY_TIMEOUT = 504;

    // ========== HTTP STATUS REASON PHRASES ==========
    public static final String REASON_OK = "OK";
    public static final String REASON_CREATED = "Created";
    public static final String REASON_NO_CONTENT = "No Content";
    public static final String REASON_MOVED_PERMANENTLY = "Moved Permanently";
    public static final String REASON_FOUND = "Found";
    public static final String REASON_NOT_MODIFIED = "Not Modified";
    public static final String REASON_BAD_REQUEST = "Bad Request";
    public static final String REASON_UNAUTHORIZED = "Unauthorized";
    public static final String REASON_FORBIDDEN = "Forbidden";
    public static final String REASON_NOT_FOUND = "Not Found";
    public static final String REASON_METHOD_NOT_ALLOWED = "Method Not Allowed";
    public static final String REASON_PAYLOAD_TOO_LARGE = "Payload Too Large";
    public static final String REASON_INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String REASON_NOT_IMPLEMENTED = "Not Implemented";
    public static final String REASON_SERVICE_UNAVAILABLE = "Service Unavailable";

    // ========== COMMON VALUES ==========
    public static final String WILDCARD = "*";
    public static final String CRLF = "\r\n";
    public static final String SPACE = " ";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String QUESTION_MARK = "?";
    public static final String AMPERSAND = "&";
    public static final String EQUALS = "=";
}

