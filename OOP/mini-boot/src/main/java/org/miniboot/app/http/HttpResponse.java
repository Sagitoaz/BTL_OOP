package org.miniboot.app.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.util.Types;

/**
 * Lớp đại diện cho một HTTP Response (phản hồi HTTP)
 */
public class HttpResponse {
    public final int status;
    public final String contentType;
    public byte[] body;
    public final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(int status, String contentType, byte[] body) {
        this.status = status;
        
        // Sử dụng constants từ HttpConstants
        this.contentType = (contentType == null || contentType.isBlank())
            ? HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8 : contentType;

        this.body = body != null ? body : new byte[0];
        
        // Thiết lập các headers cơ bản sử dụng HttpConstants
        headers.put(HttpConstants.HEADER_CONTENT_TYPE, this.contentType);
        headers.put(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(this.body.length));
        headers.put(HttpConstants.HEADER_CONNECTION, HttpConstants.CONNECTION_CLOSE);
        headers.put(HttpConstants.HEADER_DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
        headers.put(HttpConstants.HEADER_SERVER, HttpConstants.SERVER_FULL_NAME);
    }

    /**
     * Factory method tạo HTTP response với content-type là JSON
     */
    public static HttpResponse json(int status, String jsonContent) {
        byte[] body = jsonContent != null
            ? jsonContent.getBytes(StandardCharsets.UTF_8)
            : new byte[0];
        return new HttpResponse(status, HttpConstants.CONTENT_TYPE_JSON_UTF8, body);
    }

    /**
     * Factory method tạo response text/plain
     */
    public static HttpResponse text(int status, String textContent) {
        byte[] body = textContent != null
            ? textContent.getBytes(StandardCharsets.UTF_8)
            : new byte[0];
        return new HttpResponse(status, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, body);
    }

    /**
     * Factory method tạo response với custom content type
     */
    public static HttpResponse of(int status, String contentType, byte[] body) {
        return new HttpResponse(status, contentType, body);
    }

    /**
     * Factory method tạo response với status code, sử dụng content type mặc định
     */
    public static HttpResponse of(int status) {
        return new HttpResponse(status, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, new byte[0]);
    }

    /**
     * Thêm hoặc cập nhật header
     */
    public HttpResponse header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Đặt body từ String
     */
    public HttpResponse body(String content) {
        this.body = content != null
            ? content.getBytes(StandardCharsets.UTF_8)
            : new byte[0];
        headers.put(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(this.body.length));
        return this;
    }

    /**
     * Đặt body từ byte array
     */
    public HttpResponse body(byte[] content) {
        this.body = content != null ? content : new byte[0];
        headers.put(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(this.body.length));
        return this;
    }

    /**
     * Ghi response ra OutputStream theo chuẩn HTTP
     */
    public void writeTo(OutputStream out) throws IOException {
        // Status line
        String statusLine = HttpConstants.HTTP_VERSION_1_1 + " " + status + " " + getReasonPhrase(status) + "\r\n";
        out.write(statusLine.getBytes(StandardCharsets.US_ASCII));

        // Headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.US_ASCII));
        }

        // Empty line
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));

        // Body
        if (body != null && body.length > 0) {
            out.write(body);
        }

        out.flush();
    }

    /**
     * Lấy reason phrase từ status code
     */
    private String getReasonPhrase(int statusCode) {
        return switch (statusCode) {
            case 200 -> HttpConstants.REASON_OK;
            case 201 -> HttpConstants.REASON_CREATED;
            case 204 -> HttpConstants.REASON_NO_CONTENT;
            case 301 -> HttpConstants.REASON_MOVED_PERMANENTLY;
            case 302 -> HttpConstants.REASON_FOUND;
            case 304 -> HttpConstants.REASON_NOT_MODIFIED;
            case 400 -> HttpConstants.REASON_BAD_REQUEST;
            case 401 -> HttpConstants.REASON_UNAUTHORIZED;
            case 403 -> HttpConstants.REASON_FORBIDDEN;
            case 404 -> HttpConstants.REASON_NOT_FOUND;
            case 405 -> HttpConstants.REASON_METHOD_NOT_ALLOWED;
            case 413 -> HttpConstants.REASON_PAYLOAD_TOO_LARGE;
            case 500 -> HttpConstants.REASON_INTERNAL_SERVER_ERROR;
            case 501 -> HttpConstants.REASON_NOT_IMPLEMENTED;
            case 503 -> HttpConstants.REASON_SERVICE_UNAVAILABLE;
            default -> "Unknown";
        };
    }
}
