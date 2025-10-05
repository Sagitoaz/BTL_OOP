package org.miniboot.auth;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.http.Router;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AuthMain = Server HTTP siêu gọn:
 * - Lắng nghe TCP (ServerSocket) trên 1 cổng (mặc định 8080)
 * - Mỗi kết nối client -> đưa vào thread-pool để xử lý (đa luồng)
 * - Đọc request (dòng đầu, header, body) -> tạo HttpRequest
 * - Đưa vào Router để chọn handler -> trả ra HttpResponse
 * - Ghi response theo đúng format HTTP/1.1 (status line, headers, CRLF, body)
 *
 * Tập trung vào: cách đọc/gửi dữ liệu theo chuẩn HTTP và tổ chức đa luồng.
 */
public class AuthMain {

    // Cổng server lắng nghe (mặc định 8080, có thể đổi qua tham số)
    private final int port;

    // Router định tuyến: nhận HttpRequest -> trả HttpResponse (handler mount vào Router ở nơi khác)
    private final Router router;

    // Cờ trạng thái chạy/dừng (volatile để các thread nhìn thấy thay đổi ngay)
    private volatile boolean running = false;

    // ServerSocket = socket “nghe” kết nối TCP đến
    private ServerSocket serverSocket;

    // Thread pool: giới hạn số luồng xử lý đồng thời (ở đây 16)
    private final ExecutorService threadPool;

    // Thời điểm server khởi động (để in banner/log)
    private final long startTime = System.currentTimeMillis();

    // Khởi tạo server với cổng cho trước, tạo Router và thread pool
    public AuthMain(int port) {
        this.port = port;
        this.router = new Router();
        this.threadPool = Executors.newFixedThreadPool(16);
    }

    /**
     * Bắt đầu chạy server:
     * - Tạo ServerSocket lắng nghe cổng
     * - In banner endpoint
     * - Đăng ký shutdown hook để đóng tài nguyên khi JVM tắt
     * - Vòng lặp accept(): chấp nhận client và giao cho thread-pool xử lý
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        printBanner();

        // Khi tiến trình JVM tắt (Ctrl+C, kill...), gọi stop() để đóng socket & tắt thread-pool
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stop();
            } catch (IOException ignored) {}
        }));

        // Vòng lặp chính: nhận kết nối đến (blocking), mỗi client -> submit 1 task xử lý
        while (running) {
            try {
                Socket client = serverSocket.accept();      // chờ client kết nối
                client.setSoTimeout(10_000);                // timeout đọc (10s) để tránh treo
                threadPool.submit(() -> handleClient(client));
            } catch (IOException e) {
                // Nếu đang chạy bình thường mà accept lỗi -> log; nếu đã stop() thì bỏ qua
                if (running) {
                    System.err.println("[ACCEPT-ERR] " + e.getMessage());
                }
            }
        }
    }

    /**
     * Xử lý 1 kết nối client:
     * - Đọc request (request line + headers + body)
     * - Đưa sang router.route(req) để lấy response
     * - Ghi response ra socket theo chuẩn HTTP
     * - In log 1 dòng "<METHOD> <PATH> -> <STATUS>"
     */
    private void handleClient(Socket client) {
        // try-with-resources: tự đóng socket/stream khi xong
        try (Socket c = client;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
             OutputStream rawOut = c.getOutputStream();
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), false)) {

            // 1) Parse request từ luồng vào
            HttpRequest req = parseRequest(in);
            if (req == null) {
                // Request không hợp lệ -> trả 400
                sendRaw(out, 400, "application/json", "{\"error\":\"Bad request\"}");
                return;
            }

            // 2) Router xử lý -> trả HttpResponse
            HttpResponse resp;
            try {
                resp = router.route(req);
            } catch (Exception ex) {
                // Nếu handler ném lỗi -> trả 500 để client biết là lỗi server
                System.err.println("[ROUTE-ERR] " + ex.getMessage());
                resp = HttpResponse.json(500, "{\"error\":\"Internal server error\"}");
            }

            // 3) Ghi response chuẩn HTTP/1.1 ra socket
            writeResponse(out, resp);

            // 4) Log gọn: "GET /health -> 200"
            System.out.printf("%s %s -> %d%n", req.method, req.path, resp.status);

        } catch (Exception e) {
            // Lỗi khi đọc/ghi cho client này -> log lỗi nhẹ, không làm chết server
            System.err.println("[CLIENT-ERR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Đọc & phân tích HTTP request thủ công:
     * - Dòng đầu: "METHOD /path HTTP/1.1"
     * - Headers: đọc từng dòng "Key: Value" đến dòng trống
     * - Body: nếu có Content-Length > 0 thì đọc đúng số ký tự
     * - Cuối cùng tạo đối tượng HttpRequest (dùng reflection để gọi ctor private)
     */
    private HttpRequest parseRequest(BufferedReader in) throws IOException {
        // Ví dụ: "GET /health HTTP/1.1"
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isBlank()) return null;

        String[] parts = requestLine.split("\\s+");
        if (parts.length < 3) return null;

        String method = parts[0];  // GET/POST/...
        String path   = parts[1];  // /health, /auth/login, ...
        Map<String, String> headers = new HashMap<>();

        // Đọc headers tới dòng trống
        String line;
        int contentLength = 0;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String hName = line.substring(0, idx).trim();
                String hVal  = line.substring(idx + 1).trim();
                headers.put(hName, hVal);
                // Bắt Content-Length để biết cần đọc bao nhiêu body
                if ("content-length".equalsIgnoreCase(hName)) {
                    try { contentLength = Integer.parseInt(hVal); } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Nếu có body (VD: POST /auth/login gửi JSON), đọc đúng contentLength ký tự
        byte[] body = new byte[0];
        if (contentLength > 0) {
            char[] buf = new char[contentLength];
            int read = in.read(buf);
            if (read > 0) {
                // Chuyển chuỗi sang bytes UTF-8 để tương thích HttpResponse (dùng byte[])
                body = new String(buf, 0, read).getBytes(StandardCharsets.UTF_8);
            }
        }

        // Tạo HttpRequest qua reflection (vì ctor của HttpRequest là private/protected)
        try {
            var ctor = HttpRequest.class.getDeclaredConstructor(
                    String.class, String.class, String.class, Map.class, byte[].class);
            ctor.setAccessible(true);
            return (HttpRequest) ctor.newInstance(method, path, "HTTP/1.1", headers, body);
        } catch (Exception e) {
            throw new IOException("Failed to build HttpRequest: " + e.getMessage(), e);
        }
    }

    /**
     * Ghi HttpResponse ra socket theo chuẩn HTTP:
     * - Status line: "HTTP/1.1 <code> <text>"
     * - Headers (tự bổ sung Date/Content-Length nếu thiếu)
     * - Dòng trống
     * - Body (chuỗi từ r.body bytes)
     */
    private void writeResponse(PrintWriter out, HttpResponse r) {
        // 1) Status line
        out.printf("HTTP/1.1 %d %s\r\n", r.status, statusText(r.status));

        // 2) Một số header chuẩn (nếu chưa có)
        if (!r.headers.containsKey("Date")) {
            out.printf("Date: %s\r\n", Instant.now());
        }
        if (!r.headers.containsKey("Content-Length")) {
            out.printf("Content-Length: %d\r\n", r.body.length);
        }

        // 3) Ghi các header khác do handler đã set (Content-Type, etc.)
        for (Map.Entry<String, String> h : r.headers.entrySet()) {
            out.printf("%s: %s\r\n", h.getKey(), h.getValue());
        }

        // 4) Kết thúc phần header bằng 1 dòng trống
        out.print("\r\n");
        out.flush();

        // 5) Ghi body (r.body là bytes -> chuyển thành String UTF-8 để in ra)
        try {
            out.write(new String(r.body, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        out.flush();
    }

    /**
     * Tiện ích gửi response nhanh (khi lỗi parse request):
     * Tránh phải tự tạo HttpResponse.
     */
    private void sendRaw(PrintWriter out, int status, String ctype, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        out.printf("HTTP/1.1 %d %s\r\n", status, statusText(status));
        out.printf("Content-Type: %s\r\n", ctype);
        out.printf("Content-Length: %d\r\n", bytes.length);
        out.print("\r\n");
        out.print(body);
        out.flush();
    }

    // Map mã số HTTP -> chữ (200 -> "OK", 401 -> "Unauthorized", ...)
    private String statusText(int code) {
        switch (code) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default:  return "Unknown";
        }
    }

    /**
     * Dừng server:
     * - Tắt cờ running để thoát vòng accept
     * - Đóng ServerSocket
     * - Tắt thread-pool (await 5s, rồi shutdownNow nếu cần)
     */
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) threadPool.shutdownNow();
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Server stopped.");
    }

    // In “banner” khi khởi động để biết server chạy cổng nào, có endpoint nào
    private void printBanner() {
        System.out.println("MiniBoot Auth Server");
        System.out.println("Port: " + port);
        System.out.println("Endpoints:");
        System.out.println("  GET  /health");
        System.out.println("  POST /auth/login");
        System.out.println("  GET  /doctors (protected)");
        System.out.println("Started at: " + Instant.ofEpochMilli(startTime));
        System.out.println();
    }

    /**
     * Điểm vào chương trình:
     * - Đọc tham số cổng (nếu có), mặc định 8080
     * - Tạo AuthMain và start()
     */
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        AuthMain server = new AuthMain(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Startup error: " + e.getMessage());
        }
    }
}
