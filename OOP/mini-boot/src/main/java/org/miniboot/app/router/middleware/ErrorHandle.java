package org.miniboot.app.router.middleware;

import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

import java.nio.charset.StandardCharsets;


public class ErrorHandle implements Middleware {
    @Override
    public Handler apply(Handler next) {
        return req -> {
            try {
                return next.handle(req);
            } catch (IllegalArgumentException e) {
                return jsonError(400, AppConfig.RESPONSE_400, AppConfig.RESPONSE_REASON.get(400));
            } catch (Exception e) {
                return jsonError(500, AppConfig.RESPONSE_500, AppConfig.RESPONSE_REASON.get(500));
            }
        };
    }

    private static HttpResponse jsonError(int status, String code, String message) {
        String body = "{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}";
        return new HttpResponse(
                status,
                "application/json",
                body.getBytes(StandardCharsets.UTF_8)
        );
    }
}
