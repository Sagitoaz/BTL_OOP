package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Immutable-style request holder (body remains byte[]) focusing on readability & extensibility.
public class HttpRequest {
    public final String method;
    public final String path;
    public final String httpVersion;
    // All header names stored in lower-case for O(1) case‑insensitive lookup.
    public final Map<String, String> headers;
    public final byte[] body;
    public final Map<String, List<String>> query;

    public HttpRequest(String method, String path, String httpVersion, Map<String, String> rawHeaders, byte[] body) {
        this.method = method;
        String[] split = path.split("\\?", 2);
        this.path = split[0];
        this.query = parseQuery(split.length == 2 ? split[1] : "");
        this.httpVersion = httpVersion;
        // normalize headers to lower-case once
        Map<String,String> normalized = new LinkedHashMap<>();
        if (rawHeaders != null) {
            rawHeaders.forEach((k,v) -> {
                if (k != null && v != null) normalized.put(k.toLowerCase(), v);
            });
        }
        this.headers = Collections.unmodifiableMap(normalized);
        this.body = body != null ? body : new byte[0];
    }

    // --- Helpers / API ---

    public String header(String name) {
        if (name == null) return null;
        return headers.get(name.toLowerCase());
    }

    public String bodyText() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public List<String> queryValues(String name) {
        if (name == null) return List.of();
        return query.getOrDefault(name, List.of());
    }

    public String firstQueryValue(String name) {
        List<String> vs = queryValues(name);
        return vs.isEmpty() ? null : vs.get(0);
    }

    // Factory (kept)
    public static HttpRequest of(String method, String path, String httpVersion, Map<String, String> headers, byte[] body) {
        return new HttpRequest(method, path, httpVersion, headers, body);
    }

    // Legacy parser (kept for compatibility) now delegates to HttpRequestParser to avoid duplication.
    @Deprecated
    public static HttpRequest parse(InputStream in, int maxBody) throws IOException {
        // maxBody currently enforced inside HttpRequestParser via AppConfig.MAX_BODY_BYTES (ignored param here).
        return HttpRequestParser.parse(in);
    }

    // --- internal query parsing ---

    private static Map<String, List<String>> parseQuery(String q) {
        Map<String, List<String>> res = new LinkedHashMap<>();
        if (q == null || q.isEmpty()) return res;
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
}
