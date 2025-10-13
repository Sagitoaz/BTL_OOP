module mini.boot {
    // Required modules for external dependencies
    requires com.google.gson;
    requires com.auth0.jwt;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // Export packages that will be used by other modules
    exports org.miniboot.app;
    exports org.miniboot.app.auth;
    exports org.miniboot.app.controllers;
    exports org.miniboot.app.domain;
    exports org.miniboot.app.http;
    exports org.miniboot.app.router;
    exports org.miniboot.app.util;
}