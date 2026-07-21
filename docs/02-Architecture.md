# System Architecture

## 1. Architectural Style
Clean Architecture with strict unidirectional dependency flow. Inner layers know nothing about
outer layers; all cross-layer communication happens through interfaces, with concrete
implementations injected at composition-root time (a lightweight DI container / manual factory
wiring, since this is a desktop app without a Spring runtime).

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│   JavaFX Controllers (FXML) · ViewModels · UI Widgets · CSS       │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ calls (interfaces)
┌───────────────────────────────▼───────────────────────────────────┐
│                          Service Layer                            │
│   *Service interfaces + impl · DTO mapping · Transaction scripts   │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ calls (interfaces)
┌───────────────────────────────▼───────────────────────────────────┐
│                          Business Layer                           │
│  Domain rules: fine calc, borrow-limit validation, reservation     │
│  queueing, membership rules, purchase approval workflow            │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ calls (interfaces)
┌───────────────────────────────▼───────────────────────────────────┐
│                        Repository Layer                            │
│   Spring-Data-style repository interfaces + Hibernate/JPA impls    │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ JDBC (HikariCP pool)
┌───────────────────────────────▼───────────────────────────────────┐
│                         MySQL 8 Database                          │
│              (Flyway-versioned schema, 40+ tables)                │
└─────────────────────────────────────────────────────────────────┘
```

Cross-cutting concerns (`security`, `validation`, `exception`, `util`, `config`, `database`) are
accessible from every layer but depend on none of them (dependency inversion via interfaces defined
in the innermost layer they serve).

## 2. Layer Responsibilities

| Layer | Responsibility | Depends on |
|---|---|---|
| Presentation | Rendering, user input, navigation, binding to ViewModels | Service (interfaces only) |
| Service | Orchestration, DTO ⇄ Entity mapping, transaction boundaries | Business, Repository (interfaces) |
| Business | Domain rules, invariants, calculations, workflows | Repository (interfaces), Model |
| Repository | Persistence abstraction (CRUD + custom queries) | Entity, Database |
| Database | Hibernate session/EntityManager, HikariCP, Flyway | — |

## 3. Key Architectural Patterns

- **Repository Pattern** — `BookRepository`, `StudentRepository`, etc. define persistence contracts;
  `HibernateBookRepository` etc. implement them. Services depend only on the interface.
- **Service Pattern** — one service per aggregate/module (`BookService`, `CirculationService`,
  `FineService`...), each exposing a use-case-oriented API (not CRUD passthrough).
- **DTO Pattern** — entities never cross into the Presentation layer; `BookDTO`, `IssueRequestDTO`,
  `IssueResultDTO` etc. decouple UI from persistence schema.
- **Builder Pattern** — complex object construction (`Book.builder()...build()`, `IssueRecord`,
  report request objects with many optional filters).
- **Factory Pattern** — `ReportFactory` (produces PDF/Excel exporters), `NotificationFactory`
  (Email vs Desktop), `RepositoryFactory` (test doubles vs Hibernate impls for JUnit).
- **Strategy Pattern** — `FineCalculationStrategy` (per membership type / per branch rule set),
  `ThemeStrategy` (Dark/Light).
- **Observer Pattern** — JavaFX properties/bindings for reactive dashboard widgets; domain event bus
  for audit logging & notification triggers (e.g. `BookReturnedEvent` → fine calc + audit + email).
- **Facade Pattern** — `LibraryFacade`/module-level facades simplify multi-service orchestration for
  complex screens (e.g. Issue screen touches Book, Student, Membership, Circulation services).
- **Singleton (managed)** — `HikariDataSource`, `SessionManager`, `ConfigurationManager` — created
  once at the composition root, injected everywhere else (never accessed via static global state).

## 4. Dependency Injection Approach
No Spring Framework (keeps footprint desktop-appropriate). A lightweight composition root
(`AppContext`/`DIContainer` in `config/`) wires concrete implementations to interfaces at startup and
exposes typed getters. Controllers receive their services via constructor injection performed by an
`FXML` controller factory (`FXMLLoader.setControllerFactory(...)`), preserving testability (services
can be swapped in unit tests without a container).

## 5. Concurrency Model
- All DB/service calls run off the JavaFX Application Thread via `javafx.concurrent.Task` wrapped by
  a small `AsyncExecutor` util, with results marshalled back via `Platform.runLater`.
- A background `ScheduledExecutorService` drives nightly jobs: overdue-fine sweep, reservation
  expiry sweep, automatic backup, email reminder batch.

## 6. Deployment View
Single JAR/native-image per client (jpackage), connecting over TLS-enabled MySQL connection strings
to a shared branch-aware database. Configuration (`application.properties` /
`database.properties`) externalized outside the jar for per-branch DB endpoint overrides.

## 7. Cross-Cutting Concerns
- **security/**: `PasswordEncoder` (BCrypt), `SessionManager`, `PermissionEvaluator`, `AuthContext`
- **validation/**: Bean-Validation-style annotated DTOs + a `Validator` chain per form
- **exception/**: `GlobalExceptionHandler` (JavaFX `Thread.UncaughtExceptionHandler` +
  service-layer checked business exceptions such as `BookNotAvailableException`)
- **logging**: SLF4J + Logback, structured logs per layer, rolling file appenders, audit-specific
  logger routed additionally to the `audit_logs` table
