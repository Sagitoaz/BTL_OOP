package org.miniboot.app.util;

public final class Types {
    public enum HttpMethod { GET, POST, PUT, DELETE, OPTIONS }
    public enum Status {
        OK(200), CREATED(201), NO_CONTENT(204),
        BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403),
        NOT_FOUND(404), METHOD_NOT_ALLOWED(405), CONFLICT(409), ERROR(500);
        public final int code; Status(int c){ this.code = c; }
    }
    private Types(){}
}
