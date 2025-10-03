package org.miniboot.app.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import org.miniboot.app.AppConfig;

/**
 * Lớp tiện ích để encode (mã hóa) HttpResponse thành định dạng HTTP chuẩn
 * 
 * Lớp này chịu trách nhiệm chuyển đổi đối tượng HttpResponse thành 
 * chuỗi byte tuân thủ chuẩn HTTP RFC 2616 để gửi qua network.
 * 
 * Cấu trúc HTTP Response:
 * 1. Status Line: "HTTP/1.1 200 OK"
 * 2. Headers: "Header-Name: Header-Value"
 * 3. Blank Line: "\r\n"
 * 4. Body: dữ liệu nhị phân
 * 
 * Lớp được thiết kế như Utility class (chỉ có static methods),
 * không thể tạo instance.
 * 
 * @author Ngũ hổ tướng
 */
public final class HttpResponseEncoder {
    /**
     * Formatter để định dạng thời gian theo chuẩn RFC-1123 cho HTTP Date header
     * Ví dụ: "Wed, 21 Oct 2015 07:28:00 GMT"
     * Sử dụng timezone GMT theo yêu cầu của HTTP specification
     */
    private static final DateTimeFormatter RFC_1123 =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    /**
     * Set chứa tên các headers đặc biệt được server tự động thiết lập
     * Các headers này sẽ không được ghi đè bởi custom headers từ user
     * để đảm bảo tính nhất quán và đúng chuẩn HTTP
     */
    private static final Set<String> RESERVED = Set.of(
            "date",              // Thời gian server tạo response
            "server",            // Thông tin server
            "content-type",      // Loại nội dung
            "content-length",    // Độ dài body
            "connection",        // Kiểu kết nối (keep-alive/close)
            "transfer-encoding"  // Cách mã hóa truyền tải (chunked/identity)
    );

    /** Private constructor ngăn việc tạo instance - đây là utility class */
    private HttpResponseEncoder() {}

    /**
     * Ghi HttpResponse ra OutputStream theo định dạng HTTP chuẩn
     * 
     * Đây là method convenience để ghi HttpResponse, nội bộ sẽ gọi
     * method write() chính với đầy đủ tham số.
     * 
     * @param out OutputStream để ghi dữ liệu (thường là socket output stream)
     * @param res Đối tượng HttpResponse cần encode
     * @throws IOException Nếu có lỗi khi ghi dữ liệu ra stream
     */
    public static void write(OutputStream out, HttpResponse res) throws IOException {
        write(out, res.status, res.contentType, res.body == null ? new byte[0] : res.body, res.headers);
    }

    /**
     * Method chính để ghi HTTP Response ra OutputStream theo định dạng chuẩn
     * 
     * Thực hiện encode HttpResponse theo cấu trúc HTTP RFC 2616:
     * 1. Status Line: "HTTP/1.1 {status} {reason}"
     * 2. Standard Headers: Date, Server, Content-Type, Content-Length, Connection
     * 3. Custom Headers: Các headers do user cung cấp (trừ reserved headers)
     * 4. Blank Line: Ngăn cách headers và body
     * 5. Body: Dữ liệu nhị phân
     * 
     * @param out OutputStream để ghi dữ liệu
     * @param status Mã trạng thái HTTP (200, 404, 500, etc.)
     * @param contentType Loại nội dung, nếu null sẽ dùng "text/plain; charset=UTF-8"
     * @param body Nội dung body, nếu null sẽ tạo mảng rỗng
     * @param headers Map chứa custom headers, có thể null
     * @throws IOException Nếu có lỗi khi ghi dữ liệu ra stream
     */
    public static void write(OutputStream out, int status, String contentType, byte[] body, Map<String, String> headers) throws IOException {
        // === CHUẨN BỊ DỮ LIỆU ===
        // Đảm bảo body không null
        if (body == null) body = new byte[0];
        
        // Đặt content-type mặc định nếu không được cung cấp
        if (contentType == null || contentType.isBlank()) contentType = AppConfig.TEXT_UTF_8_TYPE;
        
        // Lấy reason phrase tương ứng với status code (OK, Not Found, Internal Server Error, etc.)
        // Nếu không tìm thấy status code trong map, mặc định sử dụng "Unknown"
        String reason = AppConfig.RESPONSE_REASON.getOrDefault(status, "Unknown");

        // === XÂY DỰNG HTTP HEADERS ===
        StringBuilder head = new StringBuilder(256); // Pre-allocate với kích thước hợp lý
        
        // 1. Status Line theo format: "HTTP/1.1 200 OK"
        head.append(AppConfig.HTTP_TYPE).append(' ').append(status).append(' ').append(reason).append("\r\n");
        
        // 2. Standard Headers (bắt buộc theo HTTP specification)
        head.append(AppConfig.RES_DATE_KEY).append(": ").append(RFC_1123.format(Instant.now())).append("\r\n");
        head.append(AppConfig.RES_SERVER_KEY).append(": ").append(AppConfig.SERVER_NAME).append("\r\n");
        head.append(AppConfig.RES_CONTENT_TYPE_KEY).append(": ").append(contentType).append("\r\n");
        head.append(AppConfig.RES_CONTENT_LENGTH_KEY).append(": ").append(body.length).append("\r\n");
        head.append("Connection: close\r\n"); // Đóng kết nối sau khi gửi response

        // 3. Custom Headers do user cung cấp (nếu có)
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();
                
                // Kiểm tra tính hợp lệ của header
                if (headerName == null || headerValue == null) {
                    continue; // Bỏ qua headers có tên hoặc giá trị null
                }
                
                // Bảo vệ reserved headers khỏi bị ghi đè
                if (RESERVED.contains(headerName.toLowerCase())) {
                    continue; // Không cho phép user ghi đè các headers hệ thống
                }
                
                // Thêm header vào response
                head.append(headerName).append(": ").append(headerValue).append("\r\n");
            }
        }
        
        // 4. Blank Line đánh dấu kết thúc headers
        head.append("\r\n");

        // === GHI DỮ LIỆU RA STREAM ===
        // Ghi headers (chuyển StringBuilder thành bytes với UTF-8 encoding)
        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        
        // Ghi body (dữ liệu nhị phân)
        out.write(body);
        
        // Flush để đảm bảo dữ liệu được gửi ngay lập tức
        out.flush();
    }
}
