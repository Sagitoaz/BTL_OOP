package org.miniboot.app.router;

@FunctionalInterface public interface Middleware {
    Handler apply(Handler next);
}
