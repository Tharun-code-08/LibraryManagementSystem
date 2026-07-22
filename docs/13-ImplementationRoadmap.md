# Implementation Roadmap

Implementation proceeds in incremental, independently testable phases after this design phase is
approved. Each phase produces working, compiling code with tests — never a partial/broken state.

## Phase 0 — Project Bootstrap
- Maven project skeleton (Java 21, JavaFX 21 plugin, Hibernate, HikariCP, Flyway, BCrypt, SLF4J/
  Logback, JUnit 5, Mockito, ZXing, Apache POI, PDFBox dependencies)
- Base package structure exactly as in `06-FolderStructure.md`
- `application.properties` / `database.properties` + `logback.xml`
- Flyway `V1__init_schema.sql` covering security/identity tables + `V2__catalog.sql`, etc.
- `AppContext` composition root + `FxControllerFactory`
- Global exception handler + base theming (`base.css`, `theme-light.css`, `theme-dark.css`)

## Phase 1 — Security & Authentication
- `User`, `Role`, `Permission` entities + repositories
- `PasswordEncoder` (BCrypt), `AuthService`, `SessionManager`, `PermissionEvaluator`
- Login / Forgot Password / Change Password screens
- Role-based shell routing + `RoleGuard`
- Unit tests for auth service and password policy

## Phase 2 — Catalog Core (Books, Authors, Publishers, Categories)
- Entities + repositories + services + validators
- Book list/search/filter screen, Add/Edit Book form, nested category management
- QR/Barcode generation (ZXing) for new copies
- Bulk CSV/Excel import with rejected-rows report
- Tests: repository (Testcontainers) + service (Mockito)

## Phase 3 — People Management (Students, Faculty, Membership)
- Entities/repos/services, registration forms, photo upload
- Membership types, borrow limits, expiry/renewal logic
- Bulk student import from Excel

## Phase 4 — Circulation (Issue / Return / Reservation)
- `IssueService`, `ReturnService`, `ReservationQueueManager`, `BorrowLimitValidator`
- Barcode-scan-driven Issue/Return screens
- Reservation queueing + expiry sweep (scheduled job)
- Integration tests covering the full issue→return→fine lifecycle

## Phase 5 — Fines & Payments
- `FineCalculationStrategy` (+ configurable per-membership-type rules), `PaymentService`
- Fine dashboard, payment collection, waive workflow, receipt generation (PDF)

## Phase 6 — Inventory, Suppliers & Purchase Management
- Inventory audits + shelf verification workflow
- Supplier CRUD, Purchase Orders + approval workflow, Invoices

## Phase 7 — Dashboard & Analytics
- Stat cards wired to live aggregation queries
- Charts: monthly issues/returns, category distribution, popular books, department stats
- Recent activity feed sourced from `audit_logs`

## Phase 8 — Reports
- `ReportFactory` (PDF via PDFBox/iText, Excel via Apache POI)
- All report types from SRS §3 with department/year/date-range filters + print support

## Phase 9 — Notifications
- `NotificationService`, SMTP `EmailService`, desktop notifications
- Overdue reminders, reservation-ready alerts, issue/return receipts (event-driven via nightly job
  + real-time triggers)

## Phase 10 — Admin: Users, Roles, Permissions, Audit Logs, Settings, Backup
- Full RBAC management UI, permission matrix editor
- Audit log viewer with filtering/export
- Settings screen (theme, locale, borrow-rule config), automated backup/restore

## Phase 11 — Global Search & Polish
- Cross-entity global search (books/authors/students/faculty/ISBN) with instant results overlay
- Keyboard shortcuts, animations/transitions pass, empty/loading states audit
- Accessibility & contrast audit against `09-ColorPalette.md`

## Phase 12 — Hardening & QA
- Full JUnit 5 + Mockito suite for services/business rules; repository integration tests
- ArchUnit test enforcing the layer-dependency rule
- Performance pass on catalog search / dashboard aggregation queries (indexes, query plans)
- Packaging via `jpackage` for Windows/Linux/macOS installers

## Working Agreement
- Each phase is delivered as a separate, reviewable increment (own commit(s)), building and
  passing tests before moving to the next phase.
- No phase introduces UI screens backed by mock/fake data — every screen is wired to its real
  service layer as soon as it lands.
- Schema changes always arrive as new Flyway migrations, never edits to already-applied ones.

**Next step:** upon approval of this Phase-1 design set, implementation begins at **Phase 0 —
Project Bootstrap**.
