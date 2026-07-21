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

See [`docs/13-ImplementationRoadmap.md`](docs/13-ImplementationRoadmap.md) for what's next
(Phase 5 — Fines & Payments).
