package com.university.lms.config;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.university.lms.business.BorrowLimitValidator;
import com.university.lms.business.FineCalculationStrategy;
import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.business.OverdueFineStrategy;
import com.university.lms.business.ReservationQueueManager;
import com.university.lms.repository.CategoryRepository;
import com.university.lms.repository.DashboardRepository;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.InventoryAuditItemRepository;
import com.university.lms.repository.InventoryAuditRepository;
import com.university.lms.repository.InvoiceRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.MembershipTypeRepository;
import com.university.lms.repository.NotificationRepository;
import com.university.lms.repository.PasswordResetTokenRepository;
import com.university.lms.repository.PaymentRepository;
import com.university.lms.repository.PermissionRepository;
import com.university.lms.repository.PublisherRepository;
import com.university.lms.repository.PurchaseOrderRepository;
import com.university.lms.repository.ReservationRepository;
import com.university.lms.repository.ReturnRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.SessionRepository;
import com.university.lms.repository.SettingRepository;
import com.university.lms.repository.BackupRepository;
import com.university.lms.repository.StudentRepository;
import com.university.lms.repository.SupplierRepository;
import com.university.lms.repository.TagRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.repository.impl.HibernateAuditLogRepository;
import com.university.lms.repository.impl.HibernateAuthorRepository;
import com.university.lms.repository.impl.HibernateBackupRepository;
import com.university.lms.repository.impl.HibernateBookCopyRepository;
import com.university.lms.repository.impl.HibernateBookRepository;
import com.university.lms.repository.impl.HibernateBranchRepository;
import com.university.lms.repository.impl.HibernateCategoryRepository;
import com.university.lms.repository.impl.HibernateDashboardRepository;
import com.university.lms.repository.impl.HibernateFacultyRepository;
import com.university.lms.repository.impl.HibernateFineRepository;
import com.university.lms.repository.impl.HibernateInventoryAuditItemRepository;
import com.university.lms.repository.impl.HibernateInventoryAuditRepository;
import com.university.lms.repository.impl.HibernateInvoiceRepository;
import com.university.lms.repository.impl.HibernateIssueRepository;
import com.university.lms.repository.impl.HibernateMembershipRepository;
import com.university.lms.repository.impl.HibernateMembershipTypeRepository;
import com.university.lms.repository.impl.HibernateNotificationRepository;
import com.university.lms.repository.impl.HibernatePasswordResetTokenRepository;
import com.university.lms.repository.impl.HibernatePaymentRepository;
import com.university.lms.repository.impl.HibernatePermissionRepository;
import com.university.lms.repository.impl.HibernatePublisherRepository;
import com.university.lms.repository.impl.HibernatePurchaseOrderRepository;
import com.university.lms.repository.impl.HibernateReservationRepository;
import com.university.lms.repository.impl.HibernateReturnRepository;
import com.university.lms.repository.impl.HibernateRoleRepository;
import com.university.lms.repository.impl.HibernateSessionRepository;
import com.university.lms.repository.impl.HibernateSettingRepository;
import com.university.lms.repository.impl.HibernateStudentRepository;
import com.university.lms.repository.impl.HibernateSupplierRepository;
import com.university.lms.repository.impl.HibernateTagRepository;
import com.university.lms.repository.impl.HibernateUserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.BCryptPasswordEncoder;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.security.RememberMeStore;
import com.university.lms.security.SessionManager;
import com.university.lms.service.admin.BackupService;
import com.university.lms.service.admin.ProcessExecutor;
import com.university.lms.service.admin.RoleService;
import com.university.lms.service.admin.SettingsService;
import com.university.lms.service.admin.UserManagementService;
import com.university.lms.service.admin.impl.BackupServiceImpl;
import com.university.lms.service.admin.impl.RoleServiceImpl;
import com.university.lms.service.admin.impl.SettingsServiceImpl;
import com.university.lms.service.admin.impl.SystemProcessExecutor;
import com.university.lms.service.admin.impl.UserManagementServiceImpl;
import com.university.lms.service.analytics.DashboardService;
import com.university.lms.service.analytics.impl.DashboardServiceImpl;
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
import com.university.lms.service.people.FacultyService;
import com.university.lms.service.people.MembershipService;
import com.university.lms.service.people.MembershipTypeService;
import com.university.lms.service.people.StudentService;
import com.university.lms.service.people.impl.FacultyServiceImpl;
import com.university.lms.service.people.impl.MembershipServiceImpl;
import com.university.lms.service.people.impl.MembershipTypeServiceImpl;
import com.university.lms.service.people.impl.StudentServiceImpl;
import com.university.lms.service.circulation.IssueService;
import com.university.lms.service.circulation.ReservationService;
import com.university.lms.service.circulation.ReturnService;
import com.university.lms.service.circulation.impl.IssueServiceImpl;
import com.university.lms.service.circulation.impl.ReservationServiceImpl;
import com.university.lms.service.circulation.impl.ReturnServiceImpl;
import com.university.lms.service.finance.FineService;
import com.university.lms.service.finance.PaymentService;
import com.university.lms.service.finance.impl.FineServiceImpl;
import com.university.lms.service.finance.impl.PaymentServiceImpl;
import com.university.lms.service.inventory.InventoryAuditService;
import com.university.lms.service.inventory.InvoiceService;
import com.university.lms.service.inventory.PurchaseOrderService;
import com.university.lms.service.inventory.SupplierService;
import com.university.lms.service.inventory.impl.InventoryAuditServiceImpl;
import com.university.lms.service.inventory.impl.InvoiceServiceImpl;
import com.university.lms.service.inventory.impl.PurchaseOrderServiceImpl;
import com.university.lms.service.inventory.impl.SupplierServiceImpl;
import com.university.lms.service.notification.EmailService;
import com.university.lms.service.notification.NotificationFactory;
import com.university.lms.service.notification.NotificationService;
import com.university.lms.service.notification.Notifier;
import com.university.lms.service.notification.impl.DesktopNotifierChannel;
import com.university.lms.service.notification.impl.EmailNotifier;
import com.university.lms.service.notification.impl.NotificationServiceImpl;
import com.university.lms.service.notification.impl.SmtpEmailServiceImpl;
import com.university.lms.service.report.ReportService;
import com.university.lms.service.report.impl.ReportServiceImpl;
import com.university.lms.ui.navigation.ViewNavigator;
import com.university.lms.util.AsyncExecutor;
import com.university.lms.util.BarcodeGenerator;
import com.university.lms.util.DesktopNotifier;
import com.university.lms.util.ExcelReportExporter;
import com.university.lms.util.FileStorageUtil;
import com.university.lms.util.JdbcUrlParser;
import com.university.lms.util.PdfReportExporter;
import com.university.lms.util.QrCodeGenerator;
import com.university.lms.util.ReceiptGenerator;
import com.university.lms.util.ReportFactory;

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
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final MembershipRepository membershipRepository;
    private final IssueRepository issueRepository;
    private final ReturnRepository returnRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final PaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryAuditRepository inventoryAuditRepository;
    private final InventoryAuditItemRepository inventoryAuditItemRepository;
    private final DashboardRepository dashboardRepository;
    private final NotificationRepository notificationRepository;
    private final PermissionRepository permissionRepository;
    private final SettingRepository settingRepository;
    private final BackupRepository backupRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthContext authContext;
    private final SessionManager sessionManager;
    private final PermissionEvaluator permissionEvaluator;
    private final RememberMeStore rememberMeStore;
    private final BarcodeGenerator barcodeGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final FileStorageUtil photoStorageUtil;
    private final ReceiptGenerator receiptGenerator;
    private final PdfReportExporter pdfReportExporter;
    private final ExcelReportExporter excelReportExporter;
    private final ReportFactory reportFactory;
    private final EmailService emailService;
    private final DesktopNotifier desktopNotifier;
    private final NotificationFactory notificationFactory;
    private final ProcessExecutor processExecutor;
    private final BorrowLimitValidator borrowLimitValidator;
    private final FineCalculationStrategy fineCalculationStrategy;
    private final MembershipHolderResolver membershipHolderResolver;
    private final ReservationQueueManager reservationQueueManager;
    private final ScheduledExecutorService scheduledExecutorService;

    private final AuditLogService auditLogService;
    private final AuthService authService;
    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final CategoryService categoryService;
    private final BookService bookService;
    private final MembershipTypeService membershipTypeService;
    private final MembershipService membershipService;
    private final StudentService studentService;
    private final FacultyService facultyService;
    private final IssueService issueService;
    private final ReturnService returnService;
    private final ReservationService reservationService;
    private final FineService fineService;
    private final PaymentService paymentService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;
    private final InvoiceService invoiceService;
    private final InventoryAuditService inventoryAuditService;
    private final DashboardService dashboardService;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final UserManagementService userManagementService;
    private final RoleService roleService;
    private final SettingsService settingsService;
    private final BackupService backupService;

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
        this.studentRepository = new HibernateStudentRepository(sessionFactory);
        this.facultyRepository = new HibernateFacultyRepository(sessionFactory);
        this.membershipTypeRepository = new HibernateMembershipTypeRepository(sessionFactory);
        this.membershipRepository = new HibernateMembershipRepository(sessionFactory);
        this.issueRepository = new HibernateIssueRepository(sessionFactory);
        this.returnRepository = new HibernateReturnRepository(sessionFactory);
        this.reservationRepository = new HibernateReservationRepository(sessionFactory);
        this.fineRepository = new HibernateFineRepository(sessionFactory);
        this.paymentRepository = new HibernatePaymentRepository(sessionFactory);
        this.supplierRepository = new HibernateSupplierRepository(sessionFactory);
        this.purchaseOrderRepository = new HibernatePurchaseOrderRepository(sessionFactory);
        this.invoiceRepository = new HibernateInvoiceRepository(sessionFactory);
        this.inventoryAuditRepository = new HibernateInventoryAuditRepository(sessionFactory);
        this.inventoryAuditItemRepository = new HibernateInventoryAuditItemRepository(sessionFactory);
        this.dashboardRepository = new HibernateDashboardRepository(sessionFactory);
        this.notificationRepository = new HibernateNotificationRepository(sessionFactory);
        this.permissionRepository = new HibernatePermissionRepository(sessionFactory);
        this.settingRepository = new HibernateSettingRepository(sessionFactory);
        this.backupRepository = new HibernateBackupRepository(sessionFactory);

        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authContext = new AuthContext();
        long idleTimeoutMinutes = Long.parseLong(configurationManager.app("app.session.idle-timeout-minutes", "15"));
        this.sessionManager = new SessionManager(sessionRepository, idleTimeoutMinutes);
        this.permissionEvaluator = new PermissionEvaluator(authContext);
        this.rememberMeStore = new RememberMeStore();
        this.barcodeGenerator = new BarcodeGenerator(Path.of(configurationManager.app("app.assets.barcodes-directory", "./generated/barcodes")));
        this.qrCodeGenerator = new QrCodeGenerator(Path.of(configurationManager.app("app.assets.qrcodes-directory", "./generated/qrcodes")));
        this.photoStorageUtil = new FileStorageUtil(Path.of(configurationManager.app("app.assets.photos-directory", "./generated/photos")));
        this.receiptGenerator = new ReceiptGenerator(Path.of(configurationManager.app("app.assets.receipts-directory", "./generated/receipts")));
        Path reportsDirectory = Path.of(configurationManager.app("app.assets.reports-directory", "./generated/reports"));
        this.pdfReportExporter = new PdfReportExporter(reportsDirectory);
        this.excelReportExporter = new ExcelReportExporter(reportsDirectory);
        this.reportFactory = new ReportFactory(pdfReportExporter, excelReportExporter);
        this.emailService = new SmtpEmailServiceImpl(
                configurationManager.app("app.smtp.host", "localhost"),
                Integer.parseInt(configurationManager.app("app.smtp.port", "587")),
                configurationManager.app("app.smtp.username", ""),
                configurationManager.app("app.smtp.password", ""),
                configurationManager.app("app.smtp.from-address", "library@university.edu"),
                Boolean.parseBoolean(configurationManager.app("app.smtp.auth-enabled", "false")),
                Boolean.parseBoolean(configurationManager.app("app.smtp.starttls-enabled", "true")));
        this.desktopNotifier = new DesktopNotifier();
        this.notificationFactory = new NotificationFactory(
                new EmailNotifier(emailService), new DesktopNotifierChannel(desktopNotifier));
        this.processExecutor = new SystemProcessExecutor();
        this.borrowLimitValidator = new BorrowLimitValidator();
        this.fineCalculationStrategy = new OverdueFineStrategy();
        this.membershipHolderResolver = new MembershipHolderResolver(studentRepository, facultyRepository);
        int reservationHoldDays = Integer.parseInt(configurationManager.app("app.circulation.reservation-hold-days", "3"));
        this.reservationQueueManager = new ReservationQueueManager(reservationRepository, reservationHoldDays);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "lms-scheduled-sweep");
            thread.setDaemon(true);
            return thread;
        });

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

        int defaultMembershipValidityDays = Integer.parseInt(configurationManager.app("app.membership.default-validity-days", "365"));
        this.membershipTypeService = new MembershipTypeServiceImpl(membershipTypeRepository, auditLogService, authContext);
        this.membershipService = new MembershipServiceImpl(membershipRepository, membershipTypeRepository, auditLogService, authContext);
        this.studentService = new StudentServiceImpl(
                studentRepository, userRepository, roleRepository, branchRepository, passwordEncoder,
                membershipService, auditLogService, authContext, defaultMembershipValidityDays);
        this.facultyService = new FacultyServiceImpl(
                facultyRepository, userRepository, roleRepository, passwordEncoder,
                membershipService, auditLogService, authContext, defaultMembershipValidityDays);

        this.notificationService = new NotificationServiceImpl(
                notificationRepository, issueRepository, membershipHolderResolver, notificationFactory);

        this.issueService = new IssueServiceImpl(
                issueRepository, bookCopyRepository, membershipRepository, userRepository,
                membershipHolderResolver, borrowLimitValidator, auditLogService, notificationService);
        this.returnService = new ReturnServiceImpl(
                issueRepository, bookCopyRepository, returnRepository, fineRepository, userRepository,
                fineCalculationStrategy, reservationQueueManager, membershipHolderResolver, auditLogService,
                notificationService);
        this.reservationService = new ReservationServiceImpl(
                reservationRepository, bookRepository, membershipRepository, membershipHolderResolver,
                reservationQueueManager, auditLogService, authContext);

        this.scheduledExecutorService.scheduleAtFixedRate(
                reservationService::expireStaleReservations, 1, 60, TimeUnit.MINUTES);

        int overdueSweepIntervalHours = Integer.parseInt(
                configurationManager.app("app.notifications.overdue-sweep-interval-hours", "24"));
        this.scheduledExecutorService.scheduleAtFixedRate(
                notificationService::runOverdueReminderSweep, 5, overdueSweepIntervalHours * 60L, TimeUnit.MINUTES);

        this.fineService = new FineServiceImpl(
                fineRepository, issueRepository, paymentRepository, membershipHolderResolver, auditLogService, authContext);
        this.paymentService = new PaymentServiceImpl(
                fineRepository, paymentRepository, userRepository, membershipHolderResolver,
                receiptGenerator, auditLogService);

        this.supplierService = new SupplierServiceImpl(supplierRepository, auditLogService, authContext);
        this.purchaseOrderService = new PurchaseOrderServiceImpl(
                purchaseOrderRepository, supplierRepository, bookRepository, userRepository, auditLogService, authContext);
        this.invoiceService = new InvoiceServiceImpl(invoiceRepository, purchaseOrderRepository, auditLogService, authContext);
        this.inventoryAuditService = new InventoryAuditServiceImpl(
                inventoryAuditRepository, inventoryAuditItemRepository, bookCopyRepository, branchRepository,
                userRepository, auditLogService, authContext);

        this.dashboardService = new DashboardServiceImpl(dashboardRepository, auditLogRepository);
        this.reportService = new ReportServiceImpl(
                bookService, studentService, facultyService, fineService, issueRepository, returnRepository,
                bookCopyRepository, dashboardService, membershipHolderResolver, reportFactory);

        this.userManagementService = new UserManagementServiceImpl(userRepository, roleRepository, auditLogService);
        this.roleService = new RoleServiceImpl(roleRepository, permissionRepository, auditLogService);
        this.settingsService = new SettingsServiceImpl(settingRepository, userRepository, auditLogService);

        JdbcUrlParser.ConnectionInfo dbConnectionInfo = JdbcUrlParser.parse(configurationManager.db("db.jdbc-url"));
        Path backupDirectory = Path.of(configurationManager.app("app.backup.directory", "./backups"));
        this.backupService = new BackupServiceImpl(
                backupRepository, userRepository, auditLogService, processExecutor,
                dbConnectionInfo.host(), dbConnectionInfo.port(), dbConnectionInfo.database(),
                configurationManager.db("db.username"), configurationManager.db("db.password"), backupDirectory);

        boolean backupAutoEnabled = Boolean.parseBoolean(configurationManager.app("app.backup.auto-enabled", "true"));
        if (backupAutoEnabled) {
            this.scheduledExecutorService.scheduleAtFixedRate(
                    () -> backupService.runBackup(null), 10, 24 * 60L, TimeUnit.MINUTES);
        }
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

    public MembershipTypeService getMembershipTypeService() {
        return membershipTypeService;
    }

    public MembershipService getMembershipService() {
        return membershipService;
    }

    public StudentService getStudentService() {
        return studentService;
    }

    public FacultyService getFacultyService() {
        return facultyService;
    }

    public FileStorageUtil getPhotoStorageUtil() {
        return photoStorageUtil;
    }

    public IssueService getIssueService() {
        return issueService;
    }

    public ReturnService getReturnService() {
        return returnService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public FineService getFineService() {
        return fineService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public SupplierService getSupplierService() {
        return supplierService;
    }

    public PurchaseOrderService getPurchaseOrderService() {
        return purchaseOrderService;
    }

    public InvoiceService getInvoiceService() {
        return invoiceService;
    }

    public InventoryAuditService getInventoryAuditService() {
        return inventoryAuditService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public UserManagementService getUserManagementService() {
        return userManagementService;
    }

    public RoleService getRoleService() {
        return roleService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public BackupService getBackupService() {
        return backupService;
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
        scheduledExecutorService.shutdownNow();
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
