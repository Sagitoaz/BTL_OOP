package org.miniboot.app.router.middleware;

import java.nio.charset.StandardCharsets;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

public class ErrorHandle implements Middleware {
    public Handler apply(Handler next) {
        return req -> {
            try {
                HttpResponse response = next.handle(req);
                System.out.println("✅ Handler returned response successfully");
                return response;
            } catch (IllegalArgumentException e) {
                System.err.println("❌ ErrorHandle caught IllegalArgumentException: " + e.getMessage());
                return jsonError(400, HttpConstants.REASON_BAD_REQUEST, AppConfig.RESPONSE_REASON.get(400));
            } catch (HttpServer.MethodNotAllowed e) {
                System.err.println("❌ ErrorHandle caught MethodNotAllowed: " + e.getMessage());
                return jsonError(405, HttpConstants.REASON_METHOD_NOT_ALLOWED, AppConfig.RESPONSE_REASON.get(405));
            } catch (HttpServer.NotFound e) {
                System.err.println("❌ ErrorHandle caught NotFound: " + e.getMessage());
                return jsonError(404, HttpConstants.REASON_NOT_FOUND, AppConfig.RESPONSE_REASON.get(404));
            } catch (Exception e) {
                System.err.println("❌ ErrorHandle caught Exception:");
                System.err.println("   Type: " + e.getClass().getName());
                System.err.println("   Message: " + e.getMessage());
                e.printStackTrace();
                return jsonError(500, HttpConstants.REASON_INTERNAL_SERVER_ERROR, AppConfig.RESPONSE_REASON.get(500));
            }
        };
    }

    private static HttpResponse jsonError(int status, String code, String message) {
        String body = "{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}";
        return new HttpResponse(
                status,
                "application/json",
                body.getBytes(StandardCharsets.UTF_8));
    }
}
