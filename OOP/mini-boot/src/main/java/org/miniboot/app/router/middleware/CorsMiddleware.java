package org.miniboot.app.router.middleware;

import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

public class CorsMiddleware implements Middleware {
    public Handler apply(Handler next) {

        return req -> {

            // Gan Cors vao header cho response
            HttpResponse resp = next.handle(req);
            resp.headers.put("Access-Control-Allow-Origin", "*");
            resp.headers.put("Access-Control-Allow-Headers", "Authorization,Content-Type");
            return resp;
        };
    }
}
