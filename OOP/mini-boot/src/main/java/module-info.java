module mini.boot {
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.sql;
    requires bcrypt;
    
    // PostgreSQL JDBC driver và MS SQL Server JDBC driver không phải là proper Java modules
    // Chúng sẽ được load qua classpath tự động (automatic modules)

    exports org.miniboot.app;
    exports org.miniboot.app.auth;
    exports org.miniboot.app.controllers;
    exports org.miniboot.app.domain.models;
    exports org.miniboot.app.domain.repo;
    exports org.miniboot.app.config;
    exports org.miniboot.app.http;
    exports org.miniboot.app.router;
    exports org.miniboot.app.util;
    exports org.miniboot.app.dao;

    // Opens packages for reflection access (Gson, Jackson serialization)
    opens org.miniboot.app.domain.models to com.google.gson, com.fasterxml.jackson.databind;
}
