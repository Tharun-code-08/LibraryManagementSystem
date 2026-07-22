package com.university.lms.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads and exposes the two externalized property files ({@code application.properties} and
 * {@code database.properties}). Both are read once at startup; an external file placed at
 * ~/.librarymanagementsystem/database.properties overrides the bundled defaults, allowing
 * the setup wizard to persist credentials across app updates.
 */
public final class ConfigurationManager {

    private final Properties applicationProperties = new Properties();
    private final Properties databaseProperties    = new Properties();

    public ConfigurationManager() {
        load("application.properties", applicationProperties);
        loadDatabase();
    }

    private void loadDatabase() {
        // Load bundled defaults first, then overlay the user-specific file if present.
        load("database.properties", databaseProperties);

        Path external = DatabaseSetupDialog.configDir().resolve("database.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                databaseProperties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load external database.properties: " + external, e);
            }
        }
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

    /** Returns true when a real password is present (either from bundled defaults or the external file). */
    public boolean isDatabaseConfigured() {
        String pw = databaseProperties.getProperty("db.password", "");
        return pw != null && !pw.isBlank();
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

    /** @throws IllegalStateException if {@code key} is missing or blank. */
    public String requireDb(String key) {
        String value = databaseProperties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required database configuration key '" + key + "' is missing or blank. "
                            + "Set it in database.properties or run the app to use the setup wizard.");
        }
        return value;
    }
}
