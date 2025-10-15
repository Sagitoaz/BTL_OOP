module mini.boot {
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    // PostgreSQL JDBC driver không phải là proper Java module
    // Nó sẽ được load qua classpath tự động

    // Export packages that will be used by other modules
    // Using 'transitive' to make domain models accessible to clients
    requires com.microsoft.sqlserver.jdbc;
    requires bcrypt;

    exports org.miniboot.app;
    exports org.miniboot.app.auth;
    exports org.miniboot.app.controllers;
    exports org.miniboot.app.domain.models;
    exports org.miniboot.app.domain.repo;
    exports org.miniboot.app.config;
    exports org.miniboot.app.http;
    exports org.miniboot.app.router;
    exports org.miniboot.app.util;

    // Opens packages for reflection access (Gson, Jackson serialization)
    opens org.miniboot.app.domain.models to com.google.gson, com.fasterxml.jackson.databind;
}
