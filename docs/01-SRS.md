# Software Requirement Specification (SRS)
## Library Management System (LMS) — Enterprise Desktop Edition

### 1. Introduction

#### 1.1 Purpose
This document specifies the functional and non-functional requirements for a production-grade,
multi-branch Library Management System (LMS) desktop application intended for deployment at a
university scale — thousands of students, hundreds of librarians, multiple branch libraries, and
millions of transaction records over the system's lifetime.

#### 1.2 Scope
The system will manage the full lifecycle of library operations: catalog management, membership,
circulation (issue/return/reserve), fines and payments, inventory/audit, procurement, reporting,
notifications, security/RBAC, and analytics. It is delivered as a JavaFX desktop client backed by a
MySQL 8 database, following Clean Architecture with a strict layered separation.

#### 1.3 Intended Audience
Architects, developers, QA engineers, and DevOps engineers implementing and maintaining the system;
librarians, students, faculty, and administrators as end users.

#### 1.4 Definitions
- **LMS** — Library Management System
- **RBAC** — Role Based Access Control
- **DTO** — Data Transfer Object
- **Copy** — A single physical instance of a Book title (barcode-tagged)
- **Circulation** — Issue / Return / Renew / Reserve operations

### 2. Overall Description

#### 2.1 Product Perspective
Standalone JavaFX desktop application (single codebase, multi-branch aware via a `branch_id`
partition key across operational tables), connecting to a centralized MySQL 8 instance. Designed for
LAN/WAN deployment across branches with a shared database tier.

#### 2.2 User Classes and Characteristics
| Role | Description | Typical Permissions |
|---|---|---|
| **Admin** | Full system control | All modules, user/role management, settings, backups |
| **Librarian** | Day-to-day operations | Catalog, circulation, fines, reports, inventory |
| **Student** | Borrower (self-service kiosk / portal) | Search, view own account, reserve, view fines |
| **Faculty** | Borrower with extended privileges | Search, extended borrow limits, reserve |
| **Guest** | Unauthenticated / limited | Catalog search only |

#### 2.3 Operating Environment
- Java 21 (LTS), JavaFX 21
- MySQL 8.x server (on-prem or cloud-hosted)
- Windows / Linux / macOS desktop clients
- Minimum 4 GB RAM client-side; server sized per branch transaction volume

#### 2.4 Design & Implementation Constraints
- Must follow Clean Architecture (Presentation → Service → Business → Repository → Database)
- Must follow SOLID, Repository, Service, DTO, Builder, and Factory patterns
- Must use Hibernate/JPA + HikariCP + Flyway
- Passwords hashed with BCrypt only; no plaintext credential storage
- All schema changes via Flyway versioned migrations — no manual DDL

#### 2.5 Assumptions and Dependencies
- Network connectivity to central MySQL instance is available during normal operation
- SMTP relay is available for email notifications
- Barcode/QR scanners emulate keyboard input (HID) or connect via a supported camera API

### 3. Functional Requirements (by module)

The system SHALL implement the following modules end-to-end (see task modules 1–30 for full detail):
Authentication & Session Management, Dashboard & Analytics, Book/Author/Publisher/Category
Management, Student & Faculty Management, Membership Management, Issue/Return/Reservation,
Fine & Payment Management, Inventory & Audits, Supplier & Purchase Management, Reporting
(PDF/Excel/Print), Notifications (Email/Desktop), Settings & Theming, User & Role Management,
Audit Logging, Backup & Restore, Global Search, QR/Barcode Generation & Scanning, File Management,
Validation, Exception Handling, and Logging.

Representative high-priority functional requirements:

- **FR-1**: The system shall authenticate users via username/email + password (BCrypt-verified) and
  issue a server-tracked session with configurable idle timeout.
- **FR-2**: The system shall enforce role-based authorization at the service layer for every
  operation (defense in depth beyond UI hiding).
- **FR-3**: The system shall prevent issuing a book copy that is not `AVAILABLE`, and shall enforce
  per-membership-type maximum concurrent borrow limits.
- **FR-4**: The system shall compute overdue fines automatically at return time (and via a nightly
  scheduled job for outstanding overdue items) based on configurable fine-rule tables.
- **FR-5**: The system shall maintain an immutable audit log entry for every create/update/delete/
  issue/return/login/permission-change action, including actor, timestamp, entity, and diff.
- **FR-6**: The system shall support reservation queues per title with FIFO notification when a copy
  becomes available, and automatic expiry after a configurable hold window.
- **FR-7**: The system shall generate and print barcode/QR labels for book copies and student/faculty
  ID cards, and shall accept scanner input as a fast-path for issue/return workflows.
- **FR-8**: The system shall generate reports (Books, Circulation, Overdue, Fines, Inventory,
  Department/Year-wise, Monthly/Quarterly/Yearly) exportable to PDF and Excel, and support direct
  printing.
- **FR-9**: The system shall support bulk import of books and students via CSV/Excel with row-level
  validation and a rejected-rows report.
- **FR-10**: The system shall support full CRUD + soft-delete/restore for Books, Authors, Publishers,
  Categories (nested), Students, Faculty, Suppliers.

### 4. Non-Functional Requirements

- **NFR-1 Performance**: Catalog search shall return results for a 1M+ row `books`/`book_copies`
  dataset in under 300ms (p95) using proper indexing; UI shall remain responsive via async
  service calls (JavaFX `Task`/`Service`) — no blocking calls on the FX Application Thread.
- **NFR-2 Scalability**: Schema and connection pool (HikariCP) sized to support hundreds of
  concurrent librarian sessions and millions of historical transaction rows via part422ioning-friendly
  design (indexed `branch_id`, date columns).
- **NFR-3 Security**: BCrypt (cost factor ≥ 12) password hashing; principle of least privilege for
  DB roles; parameterized queries only (Hibernate) — no string-concatenated SQL; session tokens
  invalidated on logout/timeout.
- **NFR-4 Reliability**: Automated nightly backups with restore verification; global exception
  handler ensures no unhandled exception crashes the UI thread.
- **NFR-5 Usability**: Material-Design-inspired UI, full dark/light theming, keyboard shortcuts for
  power users (librarians), WCAG-AA color contrast.
- **NFR-6 Maintainability**: Clean Architecture with strict layer boundaries enforced by module
  dependencies; ≥ 70% unit test coverage on service/business layers.
- **NFR-7 Auditability**: Every state-changing action traceable to a user, timestamp, and before/
  after snapshot.
- **NFR-8 Portability**: Runs unmodified on Windows, Linux, macOS given a Java 21 runtime.

### 5. External Interface Requirements
- **SMTP** — outbound email for receipts/reminders
- **Barcode/QR libraries** — ZXing for generation and scanning
- **PDF/Excel** — Apache PDFBox/iText and Apache POI for exports
- **MySQL 8** — primary data store over JDBC via Hibernate

### 6. Acceptance Criteria Summary
Each module is considered complete when: (a) its repository/service/controller layers exist with
interfaces, (b) unit tests cover core business rules, (c) UI screens are wired end-to-end against
real service calls (no mocked data), and (d) relevant audit log entries are emitted.
