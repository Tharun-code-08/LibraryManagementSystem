package com.university.lms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.database.FlywayMigrationRunner;
import com.university.lms.database.HibernateSessionFactoryProvider;

/**
 * Composition root of the application. Wires the configuration, connection pool, migration
 * runner, and Hibernate session factory exactly once at startup, and exposes them to the rest
 * of the app via typed getters — the only place in the codebase allowed to construct these
 * infrastructure singletons.
 *
 * <p>As services are introduced module-by-module, their single implementations are registered
 * and exposed here too, so every controller receives its dependencies through this context
 * (via {@link FxControllerFactory}) rather than constructing them itself.
 */
public final class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    private final ConfigurationManager configurationManager;
    private final HikariDataSource dataSource;
    private final SessionFactory sessionFactory;

    private AppContext(ConfigurationManager configurationManager,
                        HikariDataSource dataSource,
                        SessionFactory sessionFactory) {
        this.configurationManager = configurationManager;
        this.dataSource = dataSource;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Builds the full application context: loads configuration, opens the connection pool,
     * applies pending Flyway migrations, then boots Hibernate on top of the same pool.
     */
    public static AppContext bootstrap() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        HikariDataSource dataSource = DatabaseConfig.buildDataSource(configurationManager);
        log.info("Database connection pool '{}' initialized", dataSource.getPoolName());

        FlywayMigrationRunner.migrate(dataSource);

        SessionFactory sessionFactory = HibernateSessionFactoryProvider.build(configurationManager, dataSource);
        log.info("Hibernate SessionFactory initialized");

        return new AppContext(configurationManager, dataSource, sessionFactory);
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        log.info("AppContext shut down cleanly");
    }
}
