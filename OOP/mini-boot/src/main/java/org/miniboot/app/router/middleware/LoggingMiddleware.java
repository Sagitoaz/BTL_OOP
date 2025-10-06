package org.miniboot.app.router.middleware;

import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

import java.util.Date;

public class LoggingMiddleware implements Middleware {
    public Handler apply(Handler next) {

        return req -> {

            long timeStart = System.nanoTime();
            try{
                HttpResponse resp = next.handle(req);
                long time = (System.nanoTime() - timeStart)/1000000;
                System.out.println(req.method + " " + req.path + " -> " + resp.status + " " + time + "ms");
                return resp;

            }
            catch (Exception e) {
                long time = (System.nanoTime() - timeStart)/1000000;
                System.out.println(req.method + " " + req.path + " -> " + 500 + " " + time + "ms");
                throw e;

            }

        };
    }
}
