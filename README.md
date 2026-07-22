# Library Management System

An enterprise-grade, multi-branch Library Management System desktop application built with
Java 21, JavaFX, Hibernate/JPA, HikariCP, and Flyway, following Clean Architecture.

## Documentation
Full design documentation (SRS, architecture, ER/class/sequence diagrams, database schema, UI
wireframes, color palette, and the phased implementation roadmap) lives in [`docs/`](docs/README.md).

## Getting Started

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- MySQL 8.x (running locally or reachable per `src/main/resources/database.properties`)

### Configure
Edit `src/main/resources/database.properties` (or supply an override on the classpath) with
your MySQL connection details. On first run, Flyway automatically creates and seeds the schema
(see `src/main/resources/db/migration/`), including a default administrator account:

- Username: `admin`
- Password: `Admin@123` (rotate immediately in any non-development environment)

### Build & Run
```bash
mvn clean compile
mvn javafx:run
```

### Test
```bash
mvn test
```

## Project Status
- **Phase 0 — Project Bootstrap**: complete. Maven project skeleton, layered package structure,
  Flyway-managed database schema, Hibernate + HikariCP wiring, the DI composition root
  (`AppContext`), global exception handling, and base theming (light/dark).
- **Phase 1 — Security & Authentication**: complete. `User`/`Role`/`Permission`/`UserSession`/
  `PasswordResetToken`/`AuditLog` entities and repositories; BCrypt password hashing;
  `AuthService` covering login (with account lockout after repeated failures), logout, session
  resume ("Remember Me"), change password, and forgot/reset password; `AuthContext` +
  `PermissionEvaluator` for role-based authorization; an append-only audit log for every auth
  event; and Login / Forgot Password / Reset Password / Change Password screens plus a
  role-aware authenticated shell with a client-side idle-session timeout.
- **Phase 2 — Catalog Core**: complete. `Author`/`Publisher`/`Category` (nested, self-referencing)/
  `Tag`/`Book`/`BookCopy` entities and repositories; `BookService` with create/update/soft-delete/
  restore, keyword+category+status search with pagination, QR-code generation for new books
  (ZXing) and Code128 barcode label generation for new physical copies, and CSV bulk import with
  a per-row rejected-rows report; `AuthorService`/`PublisherService`/`CategoryService` for
  reference-data CRUD. UI: a searchable/paginated Book Catalog screen, an Add/Edit Book form
  (inline author/publisher creation, nested-category picker), a nested Category management
  screen, and a Bulk Import screen — all reachable from the authenticated shell's "Book Catalog"
  quick action.
- **Phase 3 — People Management**: complete. `Student`/`Faculty` (1:1 `User` profile extensions)
  and `MembershipType`/`Membership` entities and repositories; `StudentService` (registration —
  creates a linked `User` with the STUDENT role plus an optional initial membership — update,
  status changes, keyword+department+year+status search with pagination, and Excel bulk import
  via Apache POI with a per-row rejected-rows report) and `FacultyService` (registration/update/
  listing); `MembershipService` (assign-or-renew, extending from the current expiry when still
  active) and `MembershipTypeService` for borrow-limit/loan-period/fine-rule reference data. UI:
  a searchable/paginated Student directory with an Add/Edit form (branch + membership-type
  pickers, photo upload), a Faculty directory with its own Add/Edit form, a Membership Types
  management screen, and a Student Bulk Import screen — all reachable from the authenticated
  shell's quick actions.
- **Phase 4 — Circulation**: complete. `Issue`/`Return`/`Reservation`/`Fine` entities and
  repositories. Business layer: `BorrowLimitValidator` (per-membership-type concurrent-borrow
  cap), `OverdueFineStrategy` (per-day overdue fine minus grace period, plus full/half book cost
  for lost/damaged copies), `MembershipHolderResolver` (resolves a scanned student/faculty ID to
  their active membership), and `ReservationQueueManager` (FIFO queueing and hold-expiry sweep).
  `IssueService` validates copy availability and the borrow limit before issuing; `ReturnService`
  computes and records any fine, updates copy condition/status, and auto-promotes the next queued
  reservation when a copy comes back available; `ReservationService` supports reserve/cancel and
  runs an hourly scheduled sweep that expires unclaimed holds. UI: barcode-scan-driven Issue and
  Return screens (typed or scanned IDs/barcodes, Enter-to-submit) and a Reserve Book screen with
  catalog search, all reachable from the authenticated shell.
- **Phase 5 — Fines & Payments**: complete. `Payment` entity and repository (a fine may have
  several partial payments). `FineService` supports paginated status-filtered search, manual
  fine creation against an issue, and waiving a pending/partial fine (rejecting an already-settled
  one). `PaymentService` collects a full or partial payment — validating it against the fine's
  remaining balance — updates the fine's status (PARTIAL/PAID), and renders a one-page PDF
  receipt via a new PDFBox-based `ReceiptGenerator`. UI: a Fine Management dashboard (status
  filter, waive action, manual-fine form) and a Payment Collection screen that shows the fine
  summary, collects payment, and opens the generated receipt PDF — reachable from the
  authenticated shell's "Fines" quick action.
- **Phase 6 — Inventory, Suppliers & Purchase Management**: complete. `Supplier`,
  `PurchaseOrder`/`PurchaseOrderItem` (an aggregate — items cascade with their parent order),
  `Invoice`, and `InventoryAudit`/`InventoryAuditItem` entities and repositories.
  `PurchaseOrderService` drives the full approval workflow (DRAFT → PENDING_APPROVAL → APPROVED
  → RECEIVED, plus CANCELLED from any non-RECEIVED state), rejecting any transition attempted
  from the wrong status; `InvoiceService` records invoices only against approved/received orders;
  `SupplierService` is straightforward reference-data CRUD. `InventoryAuditService` runs the
  shelf-verification workflow: start an audit, scan each copy's barcode against its expected
  system status, and auto-correct the copy's status in place whenever a scan disagrees (e.g. a
  copy marked AVAILABLE that's actually LOST), then complete the audit. UI: Supplier management,
  a paginated/filterable Purchase Order list with a line-item entry form and a detail screen
  exposing the workflow actions plus invoice recording, and an Inventory Audit screen (start →
  scan → complete) — all reachable from the authenticated shell.
- **Phase 7 — Dashboard & Analytics**: complete. A new `DashboardRepository` backs live
  aggregation queries (active book/copy counts by status, open-overdue issues, active
  reservations, student/faculty headcounts, monthly issue/return trends via portable HQL
  `year()`/`month()` grouping, category distribution, and top-borrowed books), plus a
  `findRecent` query on the audit log for a recent-activity feed. `DashboardService` composes
  these into stat cards, chart series, and activity entries. The authenticated home screen now
  shows seven live stat cards, a monthly issues-vs-returns line chart, a category-distribution
  pie chart, a popular-books bar chart, and a recent-activity feed, above the existing quick
  actions.

- **Phase 8 — Reports**: complete. A generic tabular report model (`ReportType`,
  `ReportCriteriaDTO`, `ReportDTO`) backs ten report kinds — Books, Students, Faculty, Fines,
  Issues, Returns, Overdue, Inventory, Lost Books, and Popular Books — reusing existing
  service-layer search/list methods rather than duplicating queries, plus new
  `IssueRepository.findByIssueDateRange`, `ReturnRepository.findByReturnDateRange`, and
  `BookCopyRepository.findAll/findByStatus` queries for the reports that need them.
  `ReportServiceImpl` filters by department, year, and/or date range where applicable and
  resolves member names via the existing `MembershipHolderResolver`. A `ReportFactory` selects
  between a PDFBox-based `PdfReportExporter` (paginated landscape tables with automatic header
  repetition and cell truncation) and a POI-based `ExcelReportExporter` (single-sheet .xlsx) by
  `ExportFormat`. UI: a Reports screen with a report-type selector, department/year/date-range
  filters, an on-screen preview table, and Export PDF / Export Excel / Print actions (the last
  two opening or sending the generated file via the desktop's default handler) — reachable from
  the authenticated shell's "Reports" quick action.

- **Phase 9 — Notifications**: complete. A `Notification` entity/repository (email/desktop
  channel, category, read flag, sent timestamp) backs a `NotificationService` that dispatches
  through a `NotificationFactory` selecting between an `EmailNotifier` (SMTP via Jakarta Mail —
  `SmtpEmailServiceImpl`) and a `DesktopNotifierChannel` (OS system-tray balloon via
  `DesktopNotifier`, a safe no-op wherever the tray is unavailable). Every dispatch attempt is
  persisted regardless of delivery outcome, and delivery failures are caught and logged rather
  than surfaced, so a broken mail relay never breaks the underlying business action.
  `IssueService`/`ReturnService` now raise issue/return receipts and reservation-ready alerts as
  a side effect of issuing, returning, and auto-promoting a reservation; a new scheduled sweep
  (`NotificationService.runOverdueReminderSweep`, alongside the existing hourly reservation-expiry
  sweep) reminds every open-overdue borrower once daily, skipping anyone already reminded that
  day. UI: a "Notifications" quick action in the shell's top bar shows a live unread count and
  opens a Notification Center listing recent notifications with mark-as-read.

- **Phase 10 — Admin: Users, Roles, Permissions, Audit Logs, Settings, Backup**: complete.
  `UserManagementService` lists every account and lets an admin change status (active/locked/
  disabled) and reassign roles; `RoleService` is the permission-matrix editor — list roles/
  permissions and replace a role's permission set in one call. `AuditLogService` gained a
  filterable, paginated `search` (actor, entity type, date range) on top of its existing
  write-only `log`. New `Setting`/`Backup` entities (matching tables already defined by the
  Phase 0 `V6` migration but never mapped — along with `Notification` from Phase 9, now fixed)
  back `SettingsService` (upsert admin-editable key/value config) and `BackupService`, which
  shells out to `mysqldump`/`mysql` (host/port/database parsed from the JDBC URL via a new
  `JdbcUrlParser`) behind a swappable `ProcessExecutor` seam so it stays unit-testable; every
  backup attempt is recorded win or lose, restore is a separate explicit, confirmed action, and
  an optional nightly auto-backup sweep runs alongside the existing scheduled jobs. UI: five new
  admin screens — User Management, Role/Permission Matrix, Audit Log (with PDF/Excel export via
  the existing generic `ReportFactory`), Settings, and Backup & Restore — reachable from new
  quick actions in the authenticated shell.

- **Phase 11 — Global Search & Polish**: complete. `GlobalSearchService` composes the existing
  `BookService`/`AuthorService`/`StudentService`/`FacultyService` search methods into one
  cross-entity keyword search (books/ISBN/authors/students/faculty). UI: a `Ctrl+K` window-wide
  keyboard shortcut (or the shell's "Search" button) opens a `GlobalSearchOverlay` — an
  instant-results popup pushed onto the shared root `StackPane` on top of whatever screen is
  showing, debounced as you type, that jumps straight into the matched book/student/faculty's
  edit form (reusing the existing navigation-parameter pattern) or the book catalog for an
  author match; Escape or a backdrop click dismisses it. Accessibility/contrast audit against
  `09-ColorPalette.md`: found and fixed two WCAG-AA failures — `.error-label` was using
  `-color-accent` directly, ~2.8:1 against a white surface in the light theme (now a dedicated
  `-color-error-text` token, ~6.5:1); `.primary-button` had no background/text color rule at all
  (falling back to the default JavaFX gray button), now filled with `-color-primary-variant` +
  a new `-color-on-primary` (white) token, chosen because `-color-primary` itself is too light
  in the dark theme for white text to clear 4.5:1.

See [`docs/13-ImplementationRoadmap.md`](docs/13-ImplementationRoadmap.md) for what's next
(Phase 12).
