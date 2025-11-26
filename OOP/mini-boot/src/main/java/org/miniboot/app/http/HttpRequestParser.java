package org.miniboot.app.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miniboot.app.AppConfig;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.config.ErrorMessages;

/**
 * Lớp tiện ích để parse (phân tích cú pháp) HTTP Request từ InputStream
 */
public final class HttpRequestParser {
    
    /** Private constructor ngăn việc tạo instance - đây là utility class */
    private HttpRequestParser() {}

    /**
     * Phân tích cú pháp HTTP Request từ InputStream
     */
    public static HttpRequest parse(InputStream in) throws IOException {
        //  BƯỚC 1: PARSE REQUEST LINE 
        // Request line có format: "METHOD /path HTTP/1.1"
        String requestLine = readLine(in);
        
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }
        
        // Tách request line thành 3 phần: method, path, version
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_INVALID_REQUEST + ": " + requestLine);
        }
        String method = parts[0].trim();        // GET, POST, PUT, DELETE, etc.
        String target = parts[1].trim();        // /path?query=value
        String httpVersion = parts[2].trim();   // HTTP/1.1

        //  BƯỚC 2: PARSE HEADERS 
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
            headers.put(name, value);
        }

        //  BƯỚC 3: PARSE BODY 
        // Kiểm tra Transfer-Encoding - từ chối nếu không phải identity
        String te = getHeaderIgnoreCase(headers, "transfer-encoding");
        if (te != null && !te.isBlank() && !"identity".equalsIgnoreCase(te)) {
            throw new IOException("Transfer-Encoding not supported: " + te);
        }

        // Đọc body dựa trên Content-Length header
        byte[] body = new byte[0];
        String cl = getHeaderIgnoreCase(headers, HttpConstants.HEADER_CONTENT_LENGTH);
        if (cl != null) {
            int len = parseIntSafe(cl, 0);
            
            // Kiểm tra giới hạn kích thước body
            if (len < 0 || len > AppConfig.MAX_BODY_BYTES) {
                throw new IOException(ErrorMessages.ERROR_PAYLOAD_TOO_LARGE + ": " + cl);
            }
            
            // Đọc body nếu có độ dài > 0
            if (len > 0) {
                body = readExactly(in, len);
            }
        }

        // Trả về HttpRequest đã parse
        return new HttpRequest(method, target, httpVersion, headers, body);
    }

    //  PHƯƠNG THỨC HELPER (HỖ TRỢ) 

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean foundEOF = false;
        
        while (true) {
            int b = in.read();
            
            if (b == -1) {
                foundEOF = true;
                break;
            }
            
            if (b == '\n') {
                break;
            }
            
            if (b == '\r') {
                continue;
            }
            
            baos.write(b);
        }
        
        String result = baos.toString(StandardCharsets.UTF_8);
        
        // Nếu EOF và chưa đọc gì → return null
        if (foundEOF && baos.size() == 0) {
            return null;
        }
        
        return result;
    }

    /**
     * Đọc chính xác n bytes từ InputStream
     */
    private static byte[] readExactly(InputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        int total = 0;
        while (total < n) {
            int read = in.read(buf, total, n - total);
            if (read < 0) throw new EOFException("Unexpected EOF reading body");
            total += read;
        }
        return buf;
    }

    /**
     * Lấy header value không phân biệt hoa thường
     */
    private static String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Parse int an toàn, trả về defaultValue nếu không hợp lệ
     */
    private static int parseIntSafe(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
