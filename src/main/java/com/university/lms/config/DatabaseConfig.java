package com.university.lms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Builds the single application-wide {@link HikariDataSource}, sized and timed out per
 * {@code database.properties}. Owned by {@link AppContext} for its entire lifetime and
 * closed on application shutdown.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static HikariDataSource buildDataSource(ConfigurationManager config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.requireDb("db.jdbc-url"));
        hikariConfig.setUsername(config.requireDb("db.username"));
        hikariConfig.setPassword(config.requireDb("db.password"));
        hikariConfig.setDriverClassName(config.requireDb("db.driver-class-name"));
        hikariConfig.setPoolName(config.db("db.pool.name", "LMS-HikariPool"));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(config.db("db.pool.maximum-pool-size", "20")));
        hikariConfig.setMinimumIdle(Integer.parseInt(config.db("db.pool.minimum-idle", "5")));
        hikariConfig.setConnectionTimeout(Long.parseLong(config.db("db.pool.connection-timeout-ms", "30000")));
        hikariConfig.setIdleTimeout(Long.parseLong(config.db("db.pool.idle-timeout-ms", "600000")));
        hikariConfig.setMaxLifetime(Long.parseLong(config.db("db.pool.max-lifetime-ms", "1800000")));
        hikariConfig.setLeakDetectionThreshold(Long.parseLong(config.db("db.pool.leak-detection-threshold-ms", "60000")));
        return new HikariDataSource(hikariConfig);
    }
}
