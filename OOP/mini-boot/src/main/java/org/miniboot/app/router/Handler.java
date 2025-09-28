package org.miniboot.app.router;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;

@FunctionalInterface
public interface Handler {
    HttpResponse handle(HttpRequest req) throws Exception;
}
