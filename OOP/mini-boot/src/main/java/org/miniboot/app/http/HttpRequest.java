package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    public final String method;
    public final String path;
    public final String httpVersion;
    public final Map<String, String> headers;
    public final byte[] body;
    public final Map<String, List<String>> query;
    public final Map<String,String> tags = new LinkedHashMap<>();

    private HttpRequest(String method, String path, String httpVersion, Map<String, String> headers, byte[] body) {
        this.method = method;
        String[] split = path.split("\\?", 2);
        this.path = split[0];
        this.query = parseQuery(split.length == 2 ? split[1] : "");
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest parse(InputStream in,  int maxBody) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        String requestLine = readLine(bis);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new HttpServer.BadRequest();
        }
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) throw new HttpServer.BadRequest();

        String method = parts[0];
        String target = parts[1];
        String version = parts[2];

        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while ((line = readLine(bis)) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx <= 0) continue;
            String k = line.substring(0, idx).trim().toLowerCase();
            String v = line.substring(idx + 1).trim();
            headers.put(k, v);
        }
        int contentLength = 0;
        if (headers.containsKey(AppConfig.RES_CONTENT_LENGTH_KEY)) {
            try {
                contentLength = Integer.parseInt(headers.get(AppConfig.RES_CONTENT_LENGTH_KEY));
            } catch (NumberFormatException ignored) {}
        }
        if (contentLength < 0 || contentLength > maxBody) throw new HttpServer.BadRequest();

        byte[] body = new byte[contentLength];
        int read = 0;
        while (read < contentLength) {
            int r = bis.read(body, read, contentLength - read);
            if (r == -1) throw new EOFException();
            read += r;
        }
        return new HttpRequest(method, target, version, headers, body);
    }

    private static String readLine(BufferedInputStream bis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        int prev = -1;
        int cur;
        while ((cur = bis.read()) != -1) {
            if (prev == '\r' || prev == '\n') break;
            if (prev != -1) bos.write(prev);
            prev = cur;
            if (bos.size() > 8192) throw new HttpServer.BadRequest();
        }
        if (cur == -1 && prev == -1) return null;
        if (prev != -1 && !(prev == '\r')) bos.write(prev);
        return bos.toString(StandardCharsets.US_ASCII);
    }

    private static Map<String, List<String>> parseQuery(String q) {
        Map<String, List<String>> res = new LinkedHashMap<>();
        for (String kv : q.split("&")) {
            if (kv.isEmpty()) continue;
            String[] p = kv.split("=", 2);
            String k = urlDecode(p[0]);
            String v = p.length > 1 ? urlDecode(p[1]) : "";
            res.computeIfAbsent(k, key -> new ArrayList<>()).add(v);
        }
        return res;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    public String header(String name) {
        return headers.getOrDefault(name.toLowerCase(), null);
    }
    public String bodyText() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public static HttpRequest of(String method, String path, String httpVersion, Map<String, String> headers, byte[] body) {
        return new HttpRequest(method, path, httpVersion, headers, body);
    }
}
