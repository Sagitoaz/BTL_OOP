package org.miniboot.app.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.miniboot.app.AppConfig;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.router.Router;

/**
 * HTTP Server - Lớp chính triển khai web server đơn giản
 * <p>
 * Lớp này cung cấp một HTTP server đa luồng (multi-threaded) đơn giản
 * có khả năng:
 * - Lắng nghe kết nối TCP trên một port cụ thể
 * - Xử lý đồng thời nhiều client connections sử dụng thread pool
 * - Parse HTTP requests và tạo HTTP responses
 * - Routing requests tới các handlers tương ứng
 * - Xử lý exceptions và trả về error responses phù hợp
 * <p>
 * Kiến trúc:
 * - Main thread: Accept connections và giao cho worker threads
 * - Worker threads: Parse request, route, generate response
 * - Thread pool: Quản lý số lượng threads để tối ưu performance
 *
 * @author Ngũ hổ tướng
 */
public class HttpServer {
    /**
     * Timeout cho client socket (10 giây)
     * Nếu client không gửi dữ liệu trong thời gian này, connection sẽ bị đóng
     * để tránh tình trạng hang threads
     */
    private static final int CLIENT_SO_TIMEOUT_MS = 10_000;

    /**
     * Port mà server sẽ lắng nghe (ví dụ: 8080, 3000)
     */
    private final int port;

    /**
     * Router chịu trách nhiệm điều hướng requests tới handlers phù hợp
     */
    private final Router router;

    /**
     * Flag đánh dấu server có đang chạy không
     * Sử dụng volatile để đảm bảo thread safety khi đọc/ghi từ nhiều threads
     */
    private volatile boolean running = false;

    /**
     * ServerSocket chính để accept connections
     */
    private ServerSocket socket;

    /**
     * Thread pool để xử lý client connections đồng thời
     * Kích thước pool = max(4, số CPU cores * 2) để tối ưu performance
     * Công thức này cân bằng giữa sử dụng tài nguyên và khả năng xử lý đồng thời
     */
    private final ExecutorService pool = Executors
            .newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

    /**
     * Constructor khởi tạo HttpServer
     *
     * @param port   Port để server lắng nghe (thường là 8080, 3000, 80, 443)
     * @param router Router để điều hướng requests tới handlers tương ứng
     */
    public HttpServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    /**
     * Constructor đơn giản chỉ với port, không sử dụng router
     * <p>
     * Tạo server với routing logic mặc định (ping, health, echo endpoints).
     * Phù hợp cho testing hoặc ứng dụng đơn giản.
     *
     * @param port Port để server lắng nghe
     */
    public HttpServer(int port) {
        this(port, null);
    }

    /**
     * Khởi động HTTP Server
     * <p>
     * Thực hiện các bước:
     * 1. Tạo và cấu hình ServerSocket
     * 2. Bind socket vào port chỉ định
     * 3. Chuyển sang chế độ lắng nghe
     * 4. Accept connections trong vòng lặp vô hạn
     * 5. Giao mỗi connection cho worker thread xử lý
     * <p>
     * Method này sẽ block cho đến khi server được stop()
     *
     * @throws IOException Nếu không thể bind port hoặc có lỗi network
     */
    public void start() throws IOException {
        // Tạo ServerSocket và cấu hình
        socket = new ServerSocket();
        socket.setReuseAddress(true); // Cho phép reuse địa chỉ ngay sau khi đóng
        
        // Bind vào 0.0.0.0 để accept connections từ mọi network interface
        // Điều này cần thiết cho deployment trên Render, Railway, Heroku, etc.
        socket.bind(new InetSocketAddress("0.0.0.0", port));

        running = true;
        System.out.println("[mini-boot] HTTP listening on: 0.0.0.0:" + port);
        System.out.println("[mini-boot] Server is ready to accept connections");
        System.out.println("[mini-boot] Waiting for incoming connections...");

        // Main server loop - accept connections liên tục
        while (!socket.isClosed()) {
            // Accept connection từ client (blocking call)
            Socket client = socket.accept();
            
            System.out.println("[mini-boot] 🔗 Connection accepted from: " + client.getInetAddress());

            // Đặt timeout cho client socket để tránh hang indefinitely
            client.setSoTimeout(CLIENT_SO_TIMEOUT_MS);

            // Giao client connection cho worker thread trong pool xử lý
            pool.execute(() -> handle(client));
        }
    }

    /**
     * Dừng HTTP Server một cách graceful
     * <p>
     * Thực hiện các bước cleanup:
     * 1. Đặt flag running = false để thoát khỏi accept loop
     * 2. Đóng ServerSocket để ngừng accept connections mới
     * 3. Shutdown thread pool và hủy các tasks đang pending
     * 4. In thông báo server đã dừng
     * <p>
     * Method này không block, shutdown sẽ diễn ra asynchronously
     */
    public void stop() {
        running = false; // Báo hiệu cho main loop dừng lại

        // Đóng ServerSocket để interrupt accept() call
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignore) {
            // Bỏ qua exceptions khi đóng socket - không quan trọng lúc này
        }

        // Shutdown thread pool ngay lập tức, hủy các tasks đang chờ
        pool.shutdownNow();
        System.out.println("[mini-boot] HTTP stopped.");
    }

    /**
     * Xử lý một client connection trong worker thread
     * <p>
     * Quy trình xử lý đã được tách thành các method nhỏ để dễ đọc và bảo trì:
     * 1. Thiết lập timeout cho client
     * 2. Parse HTTP request từ input stream
     * 3. Tạo response phù hợp (routing logic)
     * 4. Ghi HTTP response ra output stream
     * 5. Xử lý exceptions và log kết quả
     *
     * @param client Socket connection tới client
     */
    private void handle(Socket client) {
        setupClientTimeout(client);

        InputStream in;
        OutputStream out = null;

        try {
            in = client.getInputStream();
            out = client.getOutputStream();

            long startTime = System.nanoTime();

            // Bước 1: Parse HTTP request
            HttpRequest request = HttpRequestParser.parse(in);

            // Bước 2: Tạo HTTP response dựa trên request
            HttpResponse response = router.dispatch(request);

            System.out.println("📤 Writing response to client...");
            System.out
                    .println("   Response body size: " + (response.body != null ? response.body.length : 0) + " bytes");

            // Bước 3: Ghi response ra client
            HttpResponseEncoder.write(out, response);

            System.out.println("✅ Response written successfully");

            // Bước 4: Log kết quả xử lý
            logRequest(request, response, startTime);

        } catch (IllegalArgumentException | IOException e) {
            // Xử lý im lặng cho empty request lines (health checks, keep-alive pings)
            if (e.getMessage() != null && e.getMessage().contains("Empty request line")) {
                // Đây là health check hoặc keep-alive ping - không cần log
                return;
            }
            
            System.err.println("❌ Bad Request / IOException: " + e.getMessage());
            // Lỗi do request không hợp lệ hoặc lỗi I/O → HTTP 400 Bad Request
            handleBadRequestError(out, client);

        } catch (Exception e) {
            System.err.println("❌ CAUGHT EXCEPTION in handleClientRequest:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            // Lỗi bất ngờ khác → HTTP 500 Internal Server Error
            handleInternalError(out, client, e);
        } finally {
            // Đảm bảo đóng kết nối một cách sạch sẽ
            cleanupConnection(out, client);
        }
    }

    /**
     * Thiết lập timeout cho client socket để tránh treo connection
     *
     * @param client Socket connection tới client
     */
    private void setupClientTimeout(Socket client) {
        try {
            client.setSoTimeout(CLIENT_SO_TIMEOUT_MS);
        } catch (Exception ignore) {
            // Bỏ qua lỗi khi thiết lập timeout
        }
    }

    /**
     * Tạo HTTP response dựa trên HTTP request
     * <p>
     * Hiện tại đây là logic routing cơ bản với các endpoint mặc định:
     * - GET /ping → trả về "pong"
     * - GET /health → trả về JSON status
     * - POST /echo → echo lại request body
     * - Các trường hợp khác → 404 Not Found hoặc 405 Method Not Allowed
     * <p>
     * Logic này có thể được thay thế bằng Router trong tương lai.
     *
     * @param request HTTP request từ client
     * @return HTTP response tương ứng
     */
    private HttpResponse createResponse(HttpRequest request) {
        String method = request.method;
        String path = request.path;

        boolean isGet = "GET".equalsIgnoreCase(method);
        boolean isPost = "POST".equalsIgnoreCase(method);

        // Logic routing cơ bản
        if (isGet && "/ping".equals(path)) {
            return createPingResponse();
        }

        if (isGet && "/health".equals(path)) {
            return createHealthResponse();
        }

        if (isPost && "/echo".equals(path)) {
            return createEchoResponse(request);
        }

        // Xử lý các trường hợp lỗi
        if ("/ping".equals(path) || "/health".equals(path)) {
            return createMethodNotAllowedResponse();
        }

        return createNotFoundResponse();
    }

    /**
     * Tạo response cho endpoint /ping
     */
    private HttpResponse createPingResponse() {
        return HttpResponse.of(200, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, "pong".getBytes());
    }

    /**
     * Tạo response cho endpoint /health check
     */
    private HttpResponse createHealthResponse() {
        return HttpResponse.json(200, "{\"status\":\"UP\"}");
    }

    /**
     * Tạo response cho endpoint /echo - trả lại chính nội dung request
     *
     * @param request HTTP request chứa body cần echo
     */
    private HttpResponse createEchoResponse(HttpRequest request) {
        String contentType = request.headers.getOrDefault("content-type", "application/octet-stream");
        return HttpResponse.of(200, contentType, request.body);
    }

    /**
     * Tạo response cho lỗi 405 Method Not Allowed
     */
    private HttpResponse createMethodNotAllowedResponse() {
        return HttpResponse.of(405, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, "method not allowed".getBytes());
    }

    /**
     * Tạo response cho lỗi 404 Not Found
     */
    private HttpResponse createNotFoundResponse() {
        return HttpResponse.of(404, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, "not found".getBytes());
    }

    /**
     * Ghi log thông tin request và response với thời gian xử lý
     *
     * @param request   HTTP request
     * @param response  HTTP response
     * @param startTime Thời điểm bắt đầu xử lý (nanoseconds)
     */
    private void logRequest(HttpRequest request, HttpResponse response, long startTime) {
        long processingTimeMs = (System.nanoTime() - startTime) / 1_000_000;
        System.out.printf("[HTTP] %s %s -> %d (%dms)%n",
                request.method, request.path, response.status, processingTimeMs);
    }

    /**
     * Xử lý lỗi Bad Request (400) - request không hợp lệ hoặc lỗi I/O
     *
     * @param out    OutputStream để ghi response, có thể null
     * @param client Client socket để lấy OutputStream nếu cần
     */
    private void handleBadRequestError(OutputStream out, Socket client) {
        try {
            if (out == null)
                out = client.getOutputStream();
            HttpResponse errorResponse = HttpResponse.of(400, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, "bad request".getBytes());
            HttpResponseEncoder.write(out, errorResponse);
        } catch (Exception ignore) {
            // Best effort - nếu không gửi được error response thì thôi
        }
    }

    /**
     * Xử lý lỗi Internal Server Error (500) - lỗi không mong đợi
     *
     * @param out               OutputStream để ghi response, có thể null
     * @param client            Client socket để lấy OutputStream nếu cần
     * @param originalException Exception gốc gây ra lỗi (để log nếu cần)
     */
    private void handleInternalError(OutputStream out, Socket client, Exception originalException) {
        try {
            if (out == null)
                out = client.getOutputStream();
            HttpResponse errorResponse = HttpResponse.of(500, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8, "internal error".getBytes());
            HttpResponseEncoder.write(out, errorResponse);
        } catch (Exception ignore) {
            // Best effort - nếu không gửi được error response thì thôi
        }

        // Log lỗi để debug (có thể bật/tắt tùy theo môi trường)
        System.err.println("[ERROR] Internal server error: " + originalException.getMessage());
    }

    /**
     * Dọn dẹp và đóng connection một cách an toàn
     *
     * @param out    OutputStream cần flush và đóng
     * @param client Client socket cần đóng
     */
    private void cleanupConnection(OutputStream out, Socket client) {
        // Flush output stream để đảm bảo dữ liệu được gửi
        try {
            if (out != null)
                out.flush();
        } catch (Exception ignore) {
        }

        // Đóng client socket
        try {
            client.close();
        } catch (Exception ignore) {
        }
    }

    /**
     * Ghi error response dưới định dạng JSON cho client
     * <p>
     * Tạo JSON error response với format chuẩn:
     * {
     * "error": {
     * "code": 404,
     * "message": "Not Found"
     * }
     * }
     * <p>
     * Method này được sử dụng trong catch blocks để gửi error responses
     * khi xử lý request thất bại.
     *
     * @param client  Socket connection tới client
     * @param status  HTTP status code (400, 404, 405, 500)
     * @param message Error message tương ứng với status code
     */
    private void writeJsonError(Socket client, int status, String message) {
        try {
            OutputStream out = client.getOutputStream();
            // Tạo JSON error response theo format chuẩn
            String json = "{\"error\":{\"code\":" + status + ",\"message\":\"" + message + "\"}}";
            HttpResponse.json(status, json).writeTo(out);
        } catch (IOException ignore) {
            // Bỏ qua IOException khi ghi error response
            // Vì đây là best-effort attempt để thông báo lỗi cho client
            // Nếu không ghi được thì client sẽ thấy connection bị đóng
        }
    }

    // === INNER EXCEPTION CLASSES ===
    // Các exception classes này được sử dụng để phân loại và xử lý lỗi một cách có
    // cấu trúc

    /**
     * Exception cho HTTP 400 Bad Request - request không đúng định dạng
     */
    public static class BadRequest extends RuntimeException {
    }

    /**
     * Exception cho HTTP 404 Not Found - không tìm thấy route
     */
    public static class NotFound extends RuntimeException {
    }

    /**
     * Exception cho HTTP 405 Method Not Allowed - method không được hỗ trợ
     */
    public static class MethodNotAllowed extends RuntimeException {
    }
}
