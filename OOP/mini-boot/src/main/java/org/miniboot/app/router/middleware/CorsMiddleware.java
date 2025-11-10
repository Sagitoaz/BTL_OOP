package org.miniboot.app.router.middleware;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

public class CorsMiddleware implements Middleware {
    public Handler apply(Handler next) {

        return req -> {

            // Gan Cors vao header cho response
            HttpResponse resp = next.handle(req);
            resp.headers.put(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, HttpConstants.WILDCARD);
            resp.headers.put(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                String.join(",", HttpConstants.HEADER_AUTHORIZATION, HttpConstants.HEADER_CONTENT_TYPE));
            return resp;
        };
    }
}
