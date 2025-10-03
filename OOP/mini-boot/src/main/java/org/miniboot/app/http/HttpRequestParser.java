package org.miniboot.app.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miniboot.app.AppConfig;

/**
 * Lớp tiện ích để parse (phân tích cú pháp) HTTP Request từ InputStream
 * 
 * Lớp này chịu tr책nhiệm đọc và phân tích cú pháp HTTP request theo chuẩn RFC 2616.
 * Quá trình parse bao gồm 3 phần chính:
 * 1. Request line (phương thức, đường dẫn, phiên bản HTTP)
 * 2. Headers (các thông tin meta)
 * 3. Body (nội dung request)
 * 
 * Lớp này được thiết kế như Utility class (chỉ có static methods),
 * không thể tạo instance (constructor private).
 * 
 * Hiện tại chỉ hỗ trợ Content-Length, chưa hỗ trợ Transfer-Encoding: chunked.
 * 
 * @author Ngũ hổ tướng
 */
public final class HttpRequestParser {
    
    /** Private constructor ngăn việc tạo instance - đây là utility class */
    private HttpRequestParser() {}

    /**
     * Phân tích cú pháp HTTP Request từ InputStream
     * 
     * Đây là method chính của parser, thực hiện 3 bước:
     * 1. Parse request line (method + path + HTTP version)
     * 2. Parse headers cho đến khi gặp dòng trống
     * 3. Parse body dựa trên Content-Length header
     * 
     * @param in InputStream chứa dữ liệu HTTP request thô
     * @return Đối tượng HttpRequest đã được parse
     * @throws IOException Nếu có lỗi khi đọc dữ liệu hoặc format không hợp lệ
     */
    public static HttpRequest parse(InputStream in) throws IOException {
        // === BƯỚC 1: PARSE REQUEST LINE ===
        // Request line có format: "METHOD /path HTTP/1.1"
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }
        
        // Tách request line thành 3 phần: method, path, version
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            // Sử dụng IllegalArgumentException thay vì IOException cho lỗi format
            // vì đây là lỗi về định dạng dữ liệu, không phải lỗi I/O
            throw new IllegalArgumentException("Malformed request line: " + requestLine);
        }
        String method = parts[0].trim();        // GET, POST, PUT, DELETE, etc.
        String target = parts[1].trim();        // /path?query=value
        String httpVersion = parts[2].trim();   // HTTP/1.1

        // === BƯỚC 2: PARSE HEADERS ===
        // Headers có format: "Header-Name: Header-Value"
        // Kết thúc bằng một dòng trống
        Map<String, String> headers = new LinkedHashMap<>();
        while (true) {
            String line = readLine(in);
            if (line == null) throw new IOException("Unexpected EOF in headers");
            if (line.isEmpty()) break; // Dòng trống đánh dấu kết thúc headers
            
            // Tách header name và value bằng dấu ':'
            int colon = line.indexOf(':');
            if (colon <= 0) continue; // Bỏ qua header không hợp lệ
            
            String name = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(name, value); // Việc normalize header name sẽ được làm trong HttpRequest
        }

        // === BƯỚC 3: PARSE BODY ===
        // Hiện tại chỉ hỗ trợ Content-Length, chưa hỗ trợ Transfer-Encoding: chunked
        
        // Kiểm tra Transfer-Encoding - từ chối nếu không phải identity
        String te = getHeaderIgnoreCase(headers, "transfer-encoding");
        if (te != null && !te.isBlank() && !"identity".equalsIgnoreCase(te)) {
            // Chưa hỗ trợ chunked encoding → từ chối request
            throw new IOException("Transfer-Encoding not supported: " + te);
        }

        // Đọc body dựa trên Content-Length header
        byte[] body = new byte[0];
        String cl = getHeaderIgnoreCase(headers, AppConfig.RES_CONTENT_LENGTH_KEY);
        if (cl != null) {
            int len = parseIntSafe(cl, 0);
            
            // Kiểm tra giới hạn kích thước body
            if (len < 0 || len > AppConfig.MAX_BODY_BYTES) {
                throw new IOException("Bad Content-Length: " + cl);
            }
            
            // Đọc body nếu có độ dài > 0
            if (len > 0) {
                body = readFixedBytes(in, len);
            }
        }

        return new HttpRequest(method, target, httpVersion, headers, body);
    }

    // === PHƯƠNG THỨC HELPER (HỖ TRỢ) ===

    /**
     * Đọc một dòng từ InputStream cho đến khi gặp CRLF hoặc LF
     * 
     * HTTP chuẩn sử dụng CRLF (\r\n) để kết thúc dòng, nhưng method này
     * cũng chấp nhận LF đơn (\n) để tương thích với các client không chuẩn.
     * 
     * @param in InputStream để đọc dữ liệu
     * @return Chuỗi đại diện cho một dòng (không bao gồm \r\n), hoặc null nếu EOF
     * @throws IOException Nếu có lỗi khi đọc dữ liệu
     */
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
        int ch;
        boolean gotCR = false; // Flag để track việc đã gặp \r chưa
        
        while ((ch = in.read()) != -1) {
            if (gotCR) {
                // Đã gặp \r ở ký tự trước
                if (ch == '\n') {
                    break; // Hoàn tất CRLF (\r\n) - kết thúc dòng
                } else {
                    // \r đơn lẻ (không theo sau bởi \n) 
                    // → ghi \r vào buffer và tiếp tục xử lý ký tự hiện tại
                    buf.write('\r');
                    gotCR = false;
                }
            }
            
            // Xử lý ký tự hiện tại
            switch (ch) {
                case '\r' -> gotCR = true; // Đánh dấu đã gặp \r, chờ kiểm tra ký tự tiếp theo
                case '\n' -> {
                    return buf.toString(StandardCharsets.UTF_8); // LF đơn - kết thúc dòng
                }
                default -> buf.write(ch); // Ký tự bình thường - thêm vào buffer
            }
        }
        
        // Đã đến EOF
        if (ch == -1 && buf.size() == 0) return null; // EOF và buffer rỗng
        return buf.toString(StandardCharsets.UTF_8);    // EOF nhưng buffer có dữ liệu
    }

    /**
     * Tìm kiếm header không phân biệt hoa thường
     * 
     * HTTP headers không phân biệt hoa thường theo RFC 2616,
     * vì vậy "Content-Length", "content-length", "CONTENT-LENGTH" đều giống nhau.
     * 
     * @param headers Map chứa tất cả headers
     * @param key Tên header cần tìm
     * @return Giá trị header hoặc null nếu không tìm thấy
     */
    private static String getHeaderIgnoreCase(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Parse chuỗi thành số nguyên một cách an toàn
     * 
     * Nếu parse thất bại (NumberFormatException), trả về giá trị mặc định
     * thay vì để exception lan truyền. Điều này hữu ích khi parse Content-Length
     * header có thể không hợp lệ.
     * 
     * @param s Chuỗi cần parse (ví dụ: "1024")
     * @param defaultValue Giá trị mặc định nếu parse thất bại
     * @return Số nguyên đã parse hoặc giá trị mặc định
     */
    private static int parseIntSafe(String s, int defaultValue) {
        if (s == null || s.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            // Log cảnh báo nếu cần thiết cho debugging
            // System.err.println("Warning: Invalid integer format: " + s);
            return defaultValue;
        }
    }

    /**
     * Đọc chính xác một số lượng byte cố định từ InputStream
     * 
     * Đảm bảo đọc đủ số byte yêu cầu, bằng cách lặp lại việc đọc
     * cho đến khi đủ hoặc gặp EOF. Điều này cần thiết vì InputStream.read()
     * có thể trả về ít hơn số byte yêu cầu trong một lần gọi.
     * 
     * @param in InputStream để đọc
     * @param len Số byte cần đọc
     * @return Mảng byte với độ dài chính xác = len
     * @throws EOFException Nếu gặp EOF trước khi đọc đủ len bytes
     * @throws IOException Nếu có lỗi I/O khác
     */
    private static byte[] readFixedBytes(InputStream in, int len) throws IOException {
        byte[] buf = new byte[len];
        int off = 0; // Offset hiện tại trong buffer
        
        // Lặp lại việc đọc cho đến khi đủ len bytes
        while (off < len) {
            int r = in.read(buf, off, len - off);
            if (r == -1) throw new EOFException("Unexpected EOF in body");
            off += r; // Cập nhật offset
        }
        return buf;
    }
}
