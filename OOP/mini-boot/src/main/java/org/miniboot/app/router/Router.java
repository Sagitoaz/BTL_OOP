package org.miniboot.app.router;

import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.http.HttpServer;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class Router {
    private static class Route {
        private final String method;
        private final PathPattern path;
        private final Function<HttpRequest, HttpResponse> handler;
        private final boolean isProtected;

        public Route(String method, String path, Function<HttpRequest, HttpResponse> handler, boolean isProtected) {
            this.method = method;
            this.path = new PathPattern(path);
            this.handler = handler;
            this.isProtected = isProtected;

        }
    }

    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<Middleware> middlewares = new ArrayList<>();

    public ArrayList<Middleware> getMiddlewares() {
        return middlewares;
    }

    public void use(Middleware middleware) {
        middlewares.add(middleware);
    }

    public void get(String path, Function<HttpRequest, HttpResponse> handler, boolean isProtected) {
        routes.add(new Route(AppConfig.GET_KEY, path, handler, isProtected));
    }

    public void get(String path, Function<HttpRequest, HttpResponse> handler) {
        get(path, handler, false);
    }

    public void post(String path, Function<HttpRequest, HttpResponse> handler, boolean isProtected) {
        routes.add(new Route(AppConfig.POST_KEY, path, handler, isProtected));
    }

    public void post(String path, Function<HttpRequest, HttpResponse> handler) {
        post(path, handler, false);
    }

    public void put(String path, Function<HttpRequest, HttpResponse> handler, boolean isProtected) {
        routes.add(new Route(AppConfig.PUT_KEY, path, handler, isProtected));
    }

    public void put(String path, Function<HttpRequest, HttpResponse> handler) {
        put(path, handler, false);
    }

    public void delete(String path, Function<HttpRequest, HttpResponse> handler, boolean isProtected) {
        routes.add(new Route(AppConfig.DELETE_KEY, path, handler, isProtected));
    }

    public void delete(String path, Function<HttpRequest, HttpResponse> handler) {
        delete(path, handler, false);
    }

    public HttpResponse dispatch(HttpRequest request) throws Exception {
        Handler h = null;
        for (Route route : routes) {
            if (route.path.match(request.path)) {

                if (!route.method.equalsIgnoreCase(request.method)){
                    h = req ->{throw new HttpServer.MethodNotAllowed(); } ;
                }
                else{
                    request.tags.put("protected", String.valueOf(route.isProtected));
                    Map<String, String> params = route.path.extract(request.path);
                    request.tags.putAll(params);
                    h = req -> route.handler.apply(req);
                }


                break;
            }
        }
        if(h == null){
            h = req -> {throw new HttpServer.NotFound(); } ;
        }
        //Boc middleware tu cuoi ve dau
        for (int i = middlewares.size() - 1; i >= 0; --i) {
            h = middlewares.get(i).apply(h);
        }

        // (d) Gọi handler CUỐI (đã bọc middleware)
        return h.handle(request);
    }

}
