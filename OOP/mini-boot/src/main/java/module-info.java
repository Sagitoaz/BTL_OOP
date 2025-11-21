module mini.boot {
    requires com.fasterxml.jackson.databind;
    requires transitive com.google.gson;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires bcrypt;
    requires transitive java.sql;


    // PostgreSQL, MS SQL Server không phải là proper Java modules
    // Chúng sẽ được load qua classpath tự động (automatic modules)

    // ========== Exports ==========
    exports org.miniboot.app;
    exports org.miniboot.app.auth;
    exports org.miniboot.app.controllers;
    exports org.miniboot.app.controllers.payment;
    exports org.miniboot.app.controllers.Inventory;
    exports org.miniboot.app.controllers.PatientAndPrescription;
    exports org.miniboot.app.domain.models;
    exports org.miniboot.app.domain.models.Inventory;
    exports org.miniboot.app.domain.models.Inventory.Enum;
    exports org.miniboot.app.domain.models.Payment;
    exports org.miniboot.app.domain.models.CustomerAndPrescription;
    exports org.miniboot.app.domain.repo;
    exports org.miniboot.app.domain.repo.Inventory;
    exports org.miniboot.app.domain.repo.Payment;
    exports org.miniboot.app.domain.repo.PatientAndPrescription;
    exports org.miniboot.app.domain.service;
    exports org.miniboot.app.config;
    exports org.miniboot.app.http;
    exports org.miniboot.app.router;
    exports org.miniboot.app.router.middleware;
    exports org.miniboot.app.util;
    exports org.miniboot.app.dao;
    exports org.miniboot.app.Service;

    // ========== Opens for Gson and Jackson Reflection ==========
    opens org.miniboot.app.domain.models to com.google.gson, com.fasterxml.jackson.databind;
    opens org.miniboot.app.domain.models.Inventory to com.google.gson, com.fasterxml.jackson.databind;
    opens org.miniboot.app.domain.models.Inventory.Enum to com.google.gson, com.fasterxml.jackson.databind;
    opens org.miniboot.app.domain.models.Payment to com.google.gson, com.fasterxml.jackson.databind;
    opens org.miniboot.app.domain.models.CustomerAndPrescription to com.google.gson, com.fasterxml.jackson.databind;
    opens org.miniboot.app.controllers.payment to com.fasterxml.jackson.databind, com.google.gson;
    opens org.miniboot.app.controllers.Inventory to com.fasterxml.jackson.databind, com.google.gson;
    opens org.miniboot.app.controllers.PatientAndPrescription to com.fasterxml.jackson.databind, com.google.gson;

    exports org.miniboot.app.util.errorvalidation;
}
