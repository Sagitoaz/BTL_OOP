module mini.boot {
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires com.auth0.jwt;
    requires com.microsoft.sqlserver.jdbc;
    requires bcrypt;

    exports org.miniboot.app;
    exports org.miniboot.app.auth;
    exports org.miniboot.app.controllers;
    exports org.miniboot.app.domain;
    exports org.miniboot.app.http;
    exports org.miniboot.app.router;
    exports org.miniboot.app.util;
}
