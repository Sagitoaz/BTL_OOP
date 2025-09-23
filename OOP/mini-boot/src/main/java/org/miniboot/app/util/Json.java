package org.miniboot.app.util;

import org.miniboot.app.http.HttpResponse;

import java.util.Map;

public class Json {
    public static HttpResponse ok(Object data) { return json(200, data); }
    public static HttpResponse created(Object data) { return json(201, data); }
    public static HttpResponse json(int status, Object data) { return HttpResponse.json(status, stringify(data)); }

    @SuppressWarnings("unchecked")
    private static String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof String s) return "\"" + escape(s) + "\"";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        if (v instanceof Map<?,?> m) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var e : m.entrySet()) {
                if (!first) sb.append(',');
                sb.append(stringify(e.getKey().toString())).append(':').append(stringify(e.getValue()));
                first = false;
            }
            return sb.append('}').toString();
        }
        if (v instanceof Iterable<?> it) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object e : it) {
                if (!first) sb.append(',');
                sb.append(stringify(e));
                first = false;
            }
            return sb.append(']').toString();
        }
        return "\"" + escape(String.valueOf(v)) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }
}
