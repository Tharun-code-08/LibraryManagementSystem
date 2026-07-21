package com.university.lms.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConfigurationManagerTest {

    @Test
    void loadsApplicationAndDatabasePropertiesFromClasspath() {
        ConfigurationManager config = new ConfigurationManager();

        assertEquals("Library Management System", config.app("app.name"));
        assertNotNull(config.db("db.jdbc-url"));
        assertEquals("com.mysql.cj.jdbc.Driver", config.db("db.driver-class-name"));
    }

    @Test
    void fallsBackToProvidedDefaultWhenKeyMissing() {
        ConfigurationManager config = new ConfigurationManager();

        assertEquals("fallback", config.app("app.does.not.exist", "fallback"));
    }
}
