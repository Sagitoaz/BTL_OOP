package org.miniboot.app.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miniboot.app.AppConfig;
import org.miniboot.app.util.Types;

/**
 * Lớp đại diện cho một HTTP Response (phản hồi HTTP)
 * 
 * Lớp này lưu trữ thông tin phản hồi từ server gửi về client bao gồm:
 * - Mã trạng thái HTTP (status code)
 * - Loại nội dung (content type)
 * - Nội dung body
 * - Các HTTP headers
 * 
 * Lớp này có thể thay đổi được (mutable) để cho phép điều chỉnh response
 * sau khi tạo, ví dụ thêm headers hoặc thay đổi body.
 * 
 * @author Ngũ hổ tướng
 */
public class HttpResponse {
    /** Mã trạng thái HTTP (200, 404, 500, etc.) - không thể thay đổi sau khi tạo */
    public final int status;
    
    /** Loại nội dung (Content-Type) như "application/json", "text/html" - không thể thay đổi */
    public final String contentType;
    
    /** Nội dung body của response - có thể thay đổi được */
    public byte[] body;
    
    /** Map chứa tất cả HTTP headers - sử dụng LinkedHashMap để giữ thứ tự */
    public final Map<String, String> headers = new LinkedHashMap<>();

    /**
     * Constructor khởi tạo HttpResponse
     * 
     * Tự động thiết lập các headers cơ bản theo chuẩn HTTP:
     * - Content-Type: loại nội dung
     * - Content-Length: độ dài body
     * - Connection: close (đóng kết nối sau khi gửi)
     * - Date: thời gian hiện tại theo chuẩn RFC-1123
     * - Server: tên server (từ AppConfig)
     * 
     * @param status Mã trạng thái HTTP (200, 404, 500, etc.)
     * @param contentType Loại nội dung, nếu null sẽ dùng mặc định "text/plain; charset=UTF-8"
     * @param body Nội dung response, nếu null sẽ tạo mảng rỗng
     */
    public HttpResponse(int status, String contentType, byte[] body) {
        this.status = status;
        
        // Đặt content-type mặc định nếu không được cung cấp
        this.contentType = (contentType == null || contentType.isBlank()) 
            ? AppConfig.TEXT_UTF_8_TYPE : contentType;
        
        // Đảm bảo body không null
        this.body = body != null ? body : new byte[0];
        
        // Thiết lập các headers cơ bản theo chuẩn HTTP
        headers.put(AppConfig.RES_CONTENT_TYPE_KEY, this.contentType);           // Content-Type
        headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(this.body.length)); // Content-Length
        headers.put(AppConfig.RES_CONNECTION_KEY, AppConfig.CONNECTION_CLOSE_KEY);        // Connection: close
        headers.put(AppConfig.RES_DATE_KEY, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())); // Date
        headers.put(AppConfig.RES_SERVER_KEY, AppConfig.SERVER_NAME);           // Server
    }

    /**
     * Factory method tạo HTTP response với content-type là JSON
     * 
     * Đây là phương thức tiện lợi để tạo response JSON, tự động:
     * - Đặt Content-Type là "application/json; charset=UTF-8"
     * - Chuyển đổi chuỗi JSON thành byte array với encoding UTF-8
     * 
     * @param status Mã trạng thái HTTP
     * @param json Chuỗi JSON, nếu null sẽ tạo body rỗng
     * @return Đối tượng HttpResponse với content-type JSON
     */
    public static HttpResponse json(int status, String json) {
        return new HttpResponse(status, AppConfig.JSON_UTF_8_TYPE,
                json == null ? new byte[0] : json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Factory method tạo HttpResponse với đầy đủ tham số
     * 
     * @param status Mã trạng thái HTTP
     * @param contentType Loại nội dung
     * @param body Nội dung body dưới dạng byte array
     * @return Đối tượng HttpResponse mới
     */
    public static HttpResponse of(int status, String contentType, byte[] body) {
        return new HttpResponse(status, contentType, body);
    }

    /**
     * Factory method tạo HttpResponse từ enum Status với body rỗng
     * 
     * Tiện lợi để tạo response chỉ có mã trạng thái mà không có nội dung,
     * ví dụ: 204 No Content, 404 Not Found
     * 
     * @param status Enum chứa mã trạng thái và message
     * @return Đối tượng HttpResponse với body rỗng
     */
    public static HttpResponse of(Types.Status status) {
        return new HttpResponse(status.code, AppConfig.TEXT_UTF_8_TYPE, new byte[0]);
    }

    /**
     * Thêm hoặc cập nhật một HTTP header
     * 
     * Phương thức này theo pattern "fluent interface" (trả về chính đối tượng)
     * để có thể gọi liên tiếp: response.header("X-Custom", "value").header("Cache-Control", "no-cache")
     * 
     * @param key Tên header, nếu null sẽ bỏ qua
     * @param value Giá trị header, nếu null sẽ bỏ qua
     * @return Chính đối tượng HttpResponse này để có thể chain method calls
     */
    public HttpResponse header(String key, String value) {
        if (key != null && value != null) {
            this.headers.put(key, value);
        }
        return this;
    }

    /**
     * Đặt nội dung body mới từ byte array
     * 
     * Tự động cập nhật header Content-Length để phản ánh độ dài body mới.
     * Điều này quan trọng để client biết chính xác kích thước dữ liệu cần đọc.
     * 
     * @param body Nội dung body mới, nếu null sẽ tạo mảng rỗng
     * @return Chính đối tượng HttpResponse để có thể chain method calls
     */
    public HttpResponse body(byte[] body) {
        this.body = body != null ? body : new byte[0];
        // Cập nhật Content-Length header để đồng bộ với body mới
        this.headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(this.body.length));
        return this;
    }

    /**
     * Đặt nội dung body mới từ chuỗi String
     * 
     * Tự động chuyển đổi String thành byte array với encoding UTF-8
     * và cập nhật Content-Length header tương ứng.
     * 
     * @param text Nội dung text, nếu null sẽ tạo body rỗng
     * @return Chính đối tượng HttpResponse để có thể chain method calls
     */
    public HttpResponse body(String text) {
        return body(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Ghi response này ra OutputStream theo định dạng HTTP chuẩn
     * 
     * Ủy quyền việc encode và ghi dữ liệu cho HttpResponseEncoder
     * để tách biệt logic encode với logic quản lý response.
     * 
     * @param out OutputStream để ghi dữ liệu (thường là socket output stream)
     * @throws IOException Nếu có lỗi khi ghi dữ liệu
     */
    public void writeTo(OutputStream out) throws IOException {
        HttpResponseEncoder.write(out, this);
    }
}
