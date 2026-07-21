package com.university.lms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.database.FlywayMigrationRunner;
import com.university.lms.database.HibernateSessionFactoryProvider;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.repository.PasswordResetTokenRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.SessionRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.repository.impl.HibernateAuditLogRepository;
import com.university.lms.repository.impl.HibernatePasswordResetTokenRepository;
import com.university.lms.repository.impl.HibernateRoleRepository;
import com.university.lms.repository.impl.HibernateSessionRepository;
import com.university.lms.repository.impl.HibernateUserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.BCryptPasswordEncoder;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.security.RememberMeStore;
import com.university.lms.security.SessionManager;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.auth.AuthService;
import com.university.lms.service.auth.impl.AuditLogServiceImpl;
import com.university.lms.service.auth.impl.AuthServiceImpl;
import com.university.lms.ui.navigation.ViewNavigator;
import com.university.lms.util.AsyncExecutor;

/**
 * Composition root of the application. Wires the configuration, connection pool, migration
 * runner, Hibernate session factory, repositories, security components, and services exactly
 * once at startup, and exposes them to the rest of the app via typed getters — the only place
 * in the codebase allowed to construct these singletons.
 *
 * <p>Controllers receive their dependencies through this context (via {@link FxControllerFactory})
 * rather than constructing them itself, keeping the {@code ui} layer free of infrastructure
 * knowledge.
 */
public final class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    private final ConfigurationManager configurationManager;
    private final HikariDataSource dataSource;
    private final SessionFactory sessionFactory;
    private final AsyncExecutor asyncExecutor;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogRepository auditLogRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthContext authContext;
    private final SessionManager sessionManager;
    private final PermissionEvaluator permissionEvaluator;
    private final RememberMeStore rememberMeStore;

    private final AuditLogService auditLogService;
    private final AuthService authService;

    private ViewNavigator viewNavigator;

    private AppContext(ConfigurationManager configurationManager,
                        HikariDataSource dataSource,
                        SessionFactory sessionFactory) {
        this.configurationManager = configurationManager;
        this.dataSource = dataSource;
        this.sessionFactory = sessionFactory;
        this.asyncExecutor = new AsyncExecutor();

        this.userRepository = new HibernateUserRepository(sessionFactory);
        this.roleRepository = new HibernateRoleRepository(sessionFactory);
        this.sessionRepository = new HibernateSessionRepository(sessionFactory);
        this.passwordResetTokenRepository = new HibernatePasswordResetTokenRepository(sessionFactory);
        this.auditLogRepository = new HibernateAuditLogRepository(sessionFactory);

        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authContext = new AuthContext();
        long idleTimeoutMinutes = Long.parseLong(configurationManager.app("app.session.idle-timeout-minutes", "15"));
        this.sessionManager = new SessionManager(sessionRepository, idleTimeoutMinutes);
        this.permissionEvaluator = new PermissionEvaluator(authContext);
        this.rememberMeStore = new RememberMeStore();

        this.auditLogService = new AuditLogServiceImpl(auditLogRepository);
        this.authService = new AuthServiceImpl(
                userRepository, passwordResetTokenRepository, sessionManager,
                passwordEncoder, authContext, auditLogService);
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

    public AsyncExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    public AuthContext getAuthContext() {
        return authContext;
    }

    public PermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    public RememberMeStore getRememberMeStore() {
        return rememberMeStore;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    public ViewNavigator getViewNavigator() {
        return viewNavigator;
    }

    public void setViewNavigator(ViewNavigator viewNavigator) {
        this.viewNavigator = viewNavigator;
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        log.info("AppContext shut down cleanly");
    }
}
