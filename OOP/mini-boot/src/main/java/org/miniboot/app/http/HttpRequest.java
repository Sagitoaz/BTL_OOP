package org.miniboot.app.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp đại diện cho một HTTP Request (yêu cầu HTTP)
 * 
 * Đây là lớp bất biến (immutable) dùng để lưu trữ thông tin của một HTTP request
 * bao gồm method, đường dẫn, version HTTP, headers, body và query parameters.
 * 
 * Thiết kế immutable giúp đảm bảo an toàn thread và tính nhất quán của dữ liệu.
 * Body được lưu dưới dạng byte[] để hỗ trợ cả text và binary data.
 * 
 * @author Ngũ hổ tướng
 */
public class HttpRequest {
    /** Phương thức HTTP (GET, POST, PUT, DELETE, etc.) */
    public final String method;
    
    /** Đường dẫn của request (không bao gồm query parameters) */
    public final String path;
    
    /** Phiên bản HTTP (thường là "HTTP/1.1") */
    public final String httpVersion;
    
    /** 
     * Map chứa tất cả HTTP headers
     * Tên header được chuyển về chữ thường để tìm kiếm không phân biệt hoa thường (case-insensitive)
     * với độ phức tạp O(1)
     */
    public final Map<String, String> headers;
    
    /** Nội dung body của request dưới dạng mảng byte để hỗ trợ cả text và binary data */
    public final byte[] body;
    
    /** Map chứa các query parameters, mỗi parameter có thể có nhiều giá trị */
    public final Map<String, List<String>> query;

    /**
     * Constructor khởi tạo HttpRequest
     * 
     * @param method Phương thức HTTP (GET, POST, PUT, DELETE, etc.)
     * @param path Đường dẫn đầy đủ bao gồm cả query parameters
     * @param httpVersion Phiên bản HTTP (thường là "HTTP/1.1")
     * @param rawHeaders Map chứa headers chưa được chuẩn hóa tên
     * @param body Nội dung body dưới dạng byte array, có thể null
     */
    public HttpRequest(String method, String path, String httpVersion, Map<String, String> rawHeaders, byte[] body) {
        this.method = method;
        
        // Tách đường dẫn và query parameters
        // Ví dụ: "/users?name=john&age=25" -> path="/users", query="name=john&age=25"
        String[] split = path.split("\\?", 2);
        this.path = split[0];
        this.query = parseQuery(split.length == 2 ? split[1] : "");
        
        this.httpVersion = httpVersion;
        
        // Chuẩn hóa tên headers về chữ thường một lần để tối ưu tốc độ tìm kiếm
        // Điều này cho phép tìm kiếm header không phân biệt hoa thường với O(1)
        Map<String,String> normalized = new LinkedHashMap<>();
        if (rawHeaders != null) {
            rawHeaders.forEach((k,v) -> {
                if (k != null && v != null) normalized.put(k.toLowerCase(), v);
            });
        }
        this.headers = Collections.unmodifiableMap(normalized);
        
        // Đảm bảo body không bao giờ null, sử dụng mảng rỗng thay vì null
        this.body = body != null ? body : new byte[0];
    }

    // === METHODS HELPER VÀ API CÔNG KHAI ===

    /**
     * Lấy giá trị header theo tên (không phân biệt hoa thường)
     * 
     * @param name Tên header cần lấy
     * @return Giá trị header hoặc null nếu không tồn tại
     */
    public String header(String name) {
        if (name == null) return null;
        return headers.get(name.toLowerCase());
    }

    /**
     * Chuyển đổi body từ byte array sang String với encoding UTF-8
     * 
     * @return Nội dung body dưới dạng chuỗi UTF-8
     */
    public String bodyText() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * Lấy tất cả giá trị của một query parameter
     * Một parameter có thể có nhiều giá trị, ví dụ: ?tags=java&tags=web&tags=backend
     * 
     * @param name Tên query parameter
     * @return Danh sách các giá trị hoặc danh sách rỗng nếu không tồn tại
     */
    public List<String> queryValues(String name) {
        if (name == null) return List.of();
        return query.getOrDefault(name, List.of());
    }

    /**
     * Lấy giá trị đầu tiên của một query parameter
     * Hữu ích khi chỉ cần một giá trị duy nhất, ví dụ: ?id=123
     * 
     * @param name Tên query parameter
     * @return Giá trị đầu tiên hoặc null nếu không tồn tại
     */
    public String firstQueryValue(String name) {
        List<String> vs = queryValues(name);
        return vs.isEmpty() ? null : vs.get(0);
    }

    // === FACTORY METHODS (PHƯƠNG THỨC TẠO ĐỐI TƯỢNG) ===
    
    /**
     * Factory method tạo HttpRequest (cách tạo khuyến nghị)
     * 
     * @param method Phương thức HTTP
     * @param path Đường dẫn bao gồm query parameters
     * @param httpVersion Phiên bản HTTP
     * @param headers Map chứa headers
     * @param body Nội dung body
     * @return Đối tượng HttpRequest mới
     */
    public static HttpRequest of(String method, String path, String httpVersion, Map<String, String> headers, byte[] body) {
        return new HttpRequest(method, path, httpVersion, headers, body);
    }

    /**
     * Phương thức parse cũ (đã lỗi thời - deprecated)
     * Hiện tại chỉ ủy quyền cho HttpRequestParser để tránh trùng lặp code
     * 
     * @param in InputStream chứa dữ liệu HTTP request
     * @param maxBody Kích thước tối đa của body (tham số bị bỏ qua, sử dụng AppConfig.MAX_BODY_BYTES)
     * @return Đối tượng HttpRequest đã parse
     * @throws IOException Nếu có lỗi khi đọc dữ liệu
     * @deprecated Sử dụng HttpRequestParser.parse(InputStream) thay thế
     */
    @Deprecated
    public static HttpRequest parse(InputStream in, int maxBody) throws IOException {
        // Tham số maxBody hiện tại được kiểm tra bên trong HttpRequestParser 
        // thông qua AppConfig.MAX_BODY_BYTES (tham số maxBody ở đây bị bỏ qua)
        return HttpRequestParser.parse(in);
    }

    // === PHƯƠNG THỨC NỘI BỘ CHO VIỆC PARSE QUERY PARAMETERS ===

    /**
     * Parse chuỗi query parameters thành Map
     * 
     * Ví dụ: "name=john&age=25&tags=java&tags=web" 
     * -> {name: [john], age: [25], tags: [java, web]}
     * 
     * @param q Chuỗi query parameters (không bao gồm dấu ?)
     * @return Map chứa các parameter và danh sách giá trị tương ứng
     */
    private static Map<String, List<String>> parseQuery(String q) {
        Map<String, List<String>> res = new LinkedHashMap<>();
        
        // Nếu không có query string thì trả về map rỗng
        if (q == null || q.isEmpty()) return res;
        
        // Tách từng cặp key=value bằng dấu &
        for (String kv : q.split("&")) {
            if (kv.isEmpty()) continue; // Bỏ qua các cặp rỗng
            
            // Tách key và value bằng dấu =, tối đa 2 phần
            String[] p = kv.split("=", 2);
            String k = urlDecode(p[0]);                    // Decode key
            String v = p.length > 1 ? urlDecode(p[1]) : ""; // Decode value (rỗng nếu không có)
            
            // Thêm giá trị vào danh sách của key tương ứng
            // computeIfAbsent tự động tạo ArrayList mới nếu key chưa tồn tại
            res.computeIfAbsent(k, key -> new ArrayList<>()).add(v);
        }
        return res;
    }

    /**
     * Decode URL-encoded string về dạng bình thường
     * 
     * Ví dụ: "hello%20world" -> "hello world"
     * 
     * @param s Chuỗi đã được URL encode
     * @return Chuỗi đã decode hoặc chuỗi gốc nếu decode thất bại
     */
    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Nếu decode thất bại, trả về chuỗi gốc để tránh crash
            return s;
        }
    }
}
