# Project Folder Structure

```
LibraryManagementSystem/
├── docs/                              # This design documentation set
├── pom.xml                            # Maven build (Java 21, JavaFX, Hibernate, Flyway...)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/university/lms/
│   │   │       ├── LibraryManagementApplication.java   # JavaFX Application entry point
│   │   │       │
│   │   │       ├── config/                # AppContext/DI wiring, HikariCP config, FX config
│   │   │       │   ├── AppContext.java
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   └── FxControllerFactory.java
│   │   │       │
│   │   │       ├── database/              # Hibernate SessionFactory, Flyway runner
│   │   │       │   ├── HibernateSessionFactoryProvider.java
│   │   │       │   └── FlywayMigrationRunner.java
│   │   │       │
│   │   │       ├── entity/                # JPA @Entity classes (one per DB table)
│   │   │       │   ├── User.java, Role.java, Permission.java
│   │   │       │   ├── Book.java, BookCopy.java, Author.java, Publisher.java, Category.java
│   │   │       │   ├── Student.java, Faculty.java, Librarian.java, Membership.java
│   │   │       │   ├── Issue.java, Return.java, Reservation.java, Fine.java, Payment.java
│   │   │       │   ├── Supplier.java, PurchaseOrder.java, PurchaseOrderItem.java, Invoice.java
│   │   │       │   ├── InventoryAudit.java, AuditLog.java, Notification.java, Setting.java
│   │   │       │
│   │   │       ├── model/                 # Plain domain/value objects not persisted as-is
│   │   │       │   ├── FineRule.java, BorrowPolicy.java, SearchCriteria.java
│   │   │       │
│   │   │       ├── dto/                   # Data Transfer Objects (+ Builders)
│   │   │       │   ├── request/  (IssueRequestDTO, BookCreateDTO, StudentImportRowDTO...)
│   │   │       │   └── response/ (BookDTO, IssueResultDTO, DashboardStatsDTO...)
│   │   │       │
│   │   │       ├── repository/            # Repository interfaces + Hibernate impls
│   │   │       │   ├── BookRepository.java / impl/HibernateBookRepository.java
│   │   │       │   ├── StudentRepository.java, IssueRepository.java, FineRepository.java, ...
│   │   │       │
│   │   │       ├── service/               # Service interfaces + impls (use-case oriented)
│   │   │       │   ├── auth/  AuthService, SessionService
│   │   │       │   ├── catalog/  BookService, AuthorService, CategoryService
│   │   │       │   ├── people/  StudentService, FacultyService, MembershipService
│   │   │       │   ├── circulation/  IssueService, ReturnService, ReservationService
│   │   │       │   ├── finance/  FineService, PaymentService
│   │   │       │   ├── inventory/  InventoryAuditService, SupplierService, PurchaseOrderService
│   │   │       │   ├── report/  ReportService (PDF/Excel factories)
│   │   │       │   ├── notification/  NotificationService, EmailService
│   │   │       │   └── admin/  UserService, RoleService, AuditLogService, SettingsService
│   │   │       │
│   │   │       ├── business/              # Domain rule engines (framework-agnostic)
│   │   │       │   ├── FineCalculationStrategy.java (+ impls)
│   │   │       │   ├── BorrowLimitValidator.java
│   │   │       │   ├── ReservationQueueManager.java
│   │   │       │   └── PurchaseApprovalWorkflow.java
│   │   │       │
│   │   │       ├── security/
│   │   │       │   ├── PasswordEncoder.java (BCrypt)
│   │   │       │   ├── SessionManager.java, AuthContext.java
│   │   │       │   └── PermissionEvaluator.java, RoleGuard.java
│   │   │       │
│   │   │       ├── validation/
│   │   │       │   ├── Validator.java (generic contract)
│   │   │       │   └── impl/ BookValidator, StudentValidator, IssueValidator...
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── BusinessException.java (+ subclasses: BookNotAvailableException,
│   │   │       │   │   BorrowLimitExceededException, InvalidFineStateException...)
│   │   │       │
│   │   │       ├── util/
│   │   │       │   ├── AsyncExecutor.java, DateUtils.java, BarcodeGenerator.java,
│   │   │       │   │   QrCodeGenerator.java, CsvImportUtil.java, ExcelUtil.java, PdfUtil.java
│   │   │       │
│   │   │       └── ui/
│   │   │           ├── controller/        # JavaFX controllers (Presentation)
│   │   │           │   ├── auth/ LoginController, ForgotPasswordController
│   │   │           │   ├── dashboard/ DashboardController
│   │   │           │   ├── catalog/ BookListController, BookFormController, ...
│   │   │           │   ├── people/ StudentListController, FacultyListController, ...
│   │   │           │   ├── circulation/ IssueController, ReturnController, ReservationController
│   │   │           │   ├── finance/ FineController, PaymentController
│   │   │           │   ├── reports/ ReportsController
│   │   │           │   ├── admin/ UserManagementController, RoleManagementController,
│   │   │           │   │   AuditLogController, SettingsController
│   │   │           │   └── common/ SidebarController, TopNavController, MainShellController
│   │   │           ├── component/          # Reusable custom controls (StatCard, DataTable...)
│   │   │           ├── viewmodel/          # JavaFX-bindable view state per screen
│   │   │           └── navigation/         # SceneRouter / ViewNavigator
│   │   │
│   │   └── resources/
│   │       ├── fxml/                       # mirrors ui/controller package structure
│   │       ├── css/                        # theme-light.css, theme-dark.css, base.css
│   │       ├── assets/
│   │       │   ├── icons/
│   │       │   ├── images/
│   │       │   └── fonts/
│   │       ├── db/migration/               # Flyway V1__init.sql, V2__seed_roles.sql, ...
│   │       ├── application.properties
│   │       ├── database.properties
│   │       └── logback.xml
│   │
│   └── test/
│       └── java/com/university/lms/
│           ├── repository/                 # Repository integration tests (Testcontainers MySQL)
│           ├── service/                    # Service unit tests (JUnit 5 + Mockito)
│           └── business/                   # Business rule unit tests
│
└── reports/                                # Generated report templates (Jasper/PDF templates)
```

## Package Dependency Rule (enforced by code review / ArchUnit test)
`ui → service → business → repository → entity`, with `security`, `validation`, `exception`,
`util`, `config`, `database` reachable from any layer but never depending on `ui` or `service`.
