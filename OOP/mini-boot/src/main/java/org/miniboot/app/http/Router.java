package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.util.ArrayList;
import java.util.function.Function;

public class Router {
    private static class Route {
        private final String method;
        private final String path;
        private final Function<HttpRequest, HttpResponse> handler;
        public Route(String method, String path, Function<HttpRequest, HttpResponse> handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }
    }

    private ArrayList<Route> routes = new ArrayList<>();

    public void get(String path, Function<HttpRequest, HttpResponse> handler) {
        routes.add(new Route(AppConfig.GET_KEY, path, handler));
    }

    public void post(String path, Function<HttpRequest, HttpResponse> handler) {
        routes.add(new Route(AppConfig.POST_KEY, path, handler));
    }

    public HttpResponse dispatch(HttpRequest request) {
        for (Route route : routes) {
            if (route.path.equals(request.path)) {
                if (!route.method.equalsIgnoreCase(request.method)) throw new HttpServer.MethodNotAllowed();
                return route.handler.apply(request);
            }
        }
        throw new HttpServer.NotFound();
    }
}
