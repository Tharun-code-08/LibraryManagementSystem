package com.university.lms.config;

import java.nio.file.Path;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.database.FlywayMigrationRunner;
import com.university.lms.database.HibernateSessionFactoryProvider;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.repository.AuthorRepository;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.BranchRepository;
import com.university.lms.repository.CategoryRepository;
import com.university.lms.repository.PasswordResetTokenRepository;
import com.university.lms.repository.PublisherRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.SessionRepository;
import com.university.lms.repository.TagRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.repository.impl.HibernateAuditLogRepository;
import com.university.lms.repository.impl.HibernateAuthorRepository;
import com.university.lms.repository.impl.HibernateBookCopyRepository;
import com.university.lms.repository.impl.HibernateBookRepository;
import com.university.lms.repository.impl.HibernateBranchRepository;
import com.university.lms.repository.impl.HibernateCategoryRepository;
import com.university.lms.repository.impl.HibernatePasswordResetTokenRepository;
import com.university.lms.repository.impl.HibernatePublisherRepository;
import com.university.lms.repository.impl.HibernateRoleRepository;
import com.university.lms.repository.impl.HibernateSessionRepository;
import com.university.lms.repository.impl.HibernateTagRepository;
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
import com.university.lms.service.catalog.AuthorService;
import com.university.lms.service.catalog.BookService;
import com.university.lms.service.catalog.CategoryService;
import com.university.lms.service.catalog.PublisherService;
import com.university.lms.service.catalog.impl.AuthorServiceImpl;
import com.university.lms.service.catalog.impl.BookServiceImpl;
import com.university.lms.service.catalog.impl.CategoryServiceImpl;
import com.university.lms.service.catalog.impl.PublisherServiceImpl;
import com.university.lms.ui.navigation.ViewNavigator;
import com.university.lms.util.AsyncExecutor;
import com.university.lms.util.BarcodeGenerator;
import com.university.lms.util.QrCodeGenerator;

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
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BranchRepository branchRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthContext authContext;
    private final SessionManager sessionManager;
    private final PermissionEvaluator permissionEvaluator;
    private final RememberMeStore rememberMeStore;
    private final BarcodeGenerator barcodeGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    private final AuditLogService auditLogService;
    private final AuthService authService;
    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final CategoryService categoryService;
    private final BookService bookService;

    private ViewNavigator viewNavigator;
    private Object navigationParameter;

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
        this.authorRepository = new HibernateAuthorRepository(sessionFactory);
        this.publisherRepository = new HibernatePublisherRepository(sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(sessionFactory);
        this.tagRepository = new HibernateTagRepository(sessionFactory);
        this.bookRepository = new HibernateBookRepository(sessionFactory);
        this.bookCopyRepository = new HibernateBookCopyRepository(sessionFactory);
        this.branchRepository = new HibernateBranchRepository(sessionFactory);

        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authContext = new AuthContext();
        long idleTimeoutMinutes = Long.parseLong(configurationManager.app("app.session.idle-timeout-minutes", "15"));
        this.sessionManager = new SessionManager(sessionRepository, idleTimeoutMinutes);
        this.permissionEvaluator = new PermissionEvaluator(authContext);
        this.rememberMeStore = new RememberMeStore();
        this.barcodeGenerator = new BarcodeGenerator(Path.of(configurationManager.app("app.assets.barcodes-directory", "./generated/barcodes")));
        this.qrCodeGenerator = new QrCodeGenerator(Path.of(configurationManager.app("app.assets.qrcodes-directory", "./generated/qrcodes")));

        this.auditLogService = new AuditLogServiceImpl(auditLogRepository);
        this.authService = new AuthServiceImpl(
                userRepository, passwordResetTokenRepository, sessionManager,
                passwordEncoder, authContext, auditLogService);
        this.authorService = new AuthorServiceImpl(authorRepository, auditLogService, authContext);
        this.publisherService = new PublisherServiceImpl(publisherRepository, auditLogService, authContext);
        this.categoryService = new CategoryServiceImpl(categoryRepository, auditLogService, authContext);
        this.bookService = new BookServiceImpl(
                bookRepository, bookCopyRepository, authorRepository, publisherRepository,
                categoryRepository, tagRepository, branchRepository, barcodeGenerator,
                qrCodeGenerator, auditLogService, authContext);
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

    public AuthorService getAuthorService() {
        return authorService;
    }

    public PublisherService getPublisherService() {
        return publisherService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public BookService getBookService() {
        return bookService;
    }

    public BranchRepository getBranchRepository() {
        return branchRepository;
    }

    public ViewNavigator getViewNavigator() {
        return viewNavigator;
    }

    public void setViewNavigator(ViewNavigator viewNavigator) {
        this.viewNavigator = viewNavigator;
    }

    /**
     * A single slot for passing lightweight context to the next-navigated screen (e.g. "which
     * book id to edit"), since {@link ViewNavigator} otherwise only takes an FXML path. The
     * receiving controller must read and clear it immediately in its {@code initialize}.
     */
    public Object getNavigationParameter() {
        return navigationParameter;
    }

    public void setNavigationParameter(Object navigationParameter) {
        this.navigationParameter = navigationParameter;
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
