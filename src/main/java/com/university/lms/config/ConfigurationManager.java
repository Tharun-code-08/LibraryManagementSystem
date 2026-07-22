package com.university.lms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and exposes the two externalized property files ({@code application.properties} and
 * {@code database.properties}). Both are read once at startup; an external file of the same
 * name placed next to the packaged jar (on the classpath) overrides the bundled defaults.
 */
public final class ConfigurationManager {

    private final Properties applicationProperties = new Properties();
    private final Properties databaseProperties = new Properties();

    public ConfigurationManager() {
        load("application.properties", applicationProperties);
        load("database.properties", databaseProperties);
    }

    private void load(String resourceName, Properties target) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Required configuration resource missing: " + resourceName);
            }
            target.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration resource: " + resourceName, e);
        }
    }

    public String app(String key) {
        return applicationProperties.getProperty(key);
    }

    public String app(String key, String defaultValue) {
        return applicationProperties.getProperty(key, defaultValue);
    }

    public String db(String key) {
        return databaseProperties.getProperty(key);
    }

    public String db(String key, String defaultValue) {
        return databaseProperties.getProperty(key, defaultValue);
    }

    /** @throws IllegalStateException if {@code key} is missing or blank in database.properties
     *  (or its external override) — fails fast at boot instead of surfacing as an obscure NPE
     *  deep inside HikariCP. */
    public String requireDb(String key) {
        String value = databaseProperties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required database configuration key '" + key + "' is missing or blank. "
                            + "Set it in database.properties or an external override on the classpath.");
        }
        return value;
    }
}
