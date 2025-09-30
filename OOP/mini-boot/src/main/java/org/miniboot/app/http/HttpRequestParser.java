package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpRequestParser {
    private HttpRequestParser() {}

    public static HttpRequest parse(InputStream in) throws IOException {
        // 1) Request line
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            throw new IOException("Malformed request line: " + requestLine);
        }
        String method = parts[0].trim();
        String target = parts[1].trim();
        String httpVersion = parts[2].trim(); // expected: HTTP/1.1

        // 2) Headers
        Map<String, String> headers = new LinkedHashMap<>();
        while (true) {
            String line = readLine(in);
            if (line == null) throw new IOException("Unexpected EOF in headers");
            if (line.isEmpty()) break; // kết thúc header
            int colon = line.indexOf(':');
            if (colon <= 0) continue; // bỏ qua header xấu
            String name = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            headers.put(name, value); // normalization deferred to HttpRequest
        }

        // 3) Body (Content-Length only). Không hỗ trợ chunked.
        String te = getHeaderIgnoreCase(headers, "transfer-encoding");
        if (te != null && !te.isBlank() && !"identity".equalsIgnoreCase(te)) {
            // chưa hỗ trợ chunked → từ chối gọn
            throw new IOException("Transfer-Encoding not supported: " + te);
        }

        byte[] body = new byte[0];
        String cl = getHeaderIgnoreCase(headers, AppConfig.RES_CONTENT_LENGTH_KEY);
        if (cl != null) {
            int len = parseIntSafe(cl.trim(), 0);
            if (len < 0 || len > AppConfig.MAX_BODY_BYTES) {
                throw new IOException("Bad Content-Length: " + cl);
            }
            if (len > 0) {
                body = readFixedBytes(in, len);
            }
        }

        return new HttpRequest(method, target, httpVersion, headers, body);
    }

    // --- helpers ---

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
        int ch;
        boolean gotCR = false;
        while ((ch = in.read()) != -1) {
            if (gotCR) {
                if (ch == '\n') {
                    break; // hoàn tất CRLF
                } else {
                    buf.write('\r'); // \r đơn lẻ → ghi lại rồi tiếp tục
                    gotCR = false;
                }
            }
            if (ch == '\r') {
                gotCR = true;
            } else if (ch == '\n') {
                break; // chấp nhận LF đơn
            } else {
                buf.write(ch);
            }
        }
        if (ch == -1 && buf.size() == 0) return null;
        return buf.toString(StandardCharsets.UTF_8);
    }

    private static String getHeaderIgnoreCase(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static byte[] readFixedBytes(InputStream in, int len) throws IOException {
        byte[] buf = new byte[len];
        int off = 0;
        while (off < len) {
            int r = in.read(buf, off, len - off);
            if (r == -1) throw new EOFException("Unexpected EOF in body");
            off += r;
        }
        return buf;
    }
}
