package com.university.lms.database;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies pending Flyway migrations from {@code classpath:db/migration} against the pooled
 * {@link DataSource} before any repository/service code touches the database. Runs once at
 * application startup, ahead of {@link HibernateSessionFactoryProvider} construction.
 */
public final class FlywayMigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayMigrationRunner.class);

    private FlywayMigrationRunner() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        int applied = flyway.migrate().migrationsExecuted;
        log.info("Flyway migration complete: {} migration(s) applied", applied);
    }
}
