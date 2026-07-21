# Database Schema (MySQL 8, Flyway-managed)

Full normalized schema (3NF), delivered later as versioned Flyway migrations
(`db/migration/V1__*.sql`, `V2__*.sql`, ...). Below is the authoritative table-level design.

## Security & Identity
- **roles** (id PK, name UNIQUE, description)
- **permissions** (id PK, code UNIQUE, description) — e.g. `BOOK_DELETE`, `FINE_WAIVE`
- **role_permissions** (role_id FK, permission_id FK, PK(role_id, permission_id))
- **users** (id PK, username UNIQUE, email UNIQUE, password_hash, status ENUM(ACTIVE,LOCKED,
  DISABLED), branch_id FK, failed_login_attempts, last_login_at, created_at, updated_at)
- **user_roles** (user_id FK, role_id FK, PK(user_id, role_id))
- **sessions** (id PK, user_id FK, token UNIQUE, created_at, expires_at, revoked_at, ip_address)

## Organization
- **branches** (id PK, name, code UNIQUE, address, phone)

## People
- **students** (id PK, user_id FK UNIQUE, student_id UNIQUE, roll_number UNIQUE, department,
  year, semester, photo_path, phone, address, guardian_name, guardian_phone, status, branch_id FK)
- **faculty** (id PK, user_id FK UNIQUE, faculty_id UNIQUE, department, designation, phone, office)
- **librarians** (id PK, user_id FK UNIQUE, employee_id UNIQUE, branch_id FK)

## Catalog
- **authors** (id PK, name, biography TEXT, nationality)
- **publishers** (id PK, name, address, phone, email)
- **categories** (id PK, name, parent_id FK NULLABLE self-ref, description)
- **books** (id PK, isbn UNIQUE, title, subtitle, edition, volume, language, publisher_id FK,
  category_id FK, cost DECIMAL(10,2), purchase_date, vendor, cover_image_path, pdf_path,
  qr_code_path, barcode_value UNIQUE, is_deleted BOOLEAN, created_at, updated_at)
- **book_authors** (book_id FK, author_id FK, PK(book_id, author_id)) — many-to-many
- **tags** (id PK, name UNIQUE)
- **book_tags** (book_id FK, tag_id FK, PK(book_id, tag_id))
- **book_copies** (id PK, book_id FK, branch_id FK, barcode UNIQUE, shelf, rack, row_label,
  condition ENUM(NEW,GOOD,WORN,DAMAGED,LOST), status ENUM(AVAILABLE,ISSUED,RESERVED,
  MAINTENANCE,LOST,RETIRED), acquired_at, created_at, updated_at)

## Membership & Circulation
- **membership_types** (id PK, name, max_borrow_limit, loan_period_days, fine_per_day DECIMAL,
  grace_period_days, renewal_limit)
- **memberships** (id PK, membership_type_id FK, holder_type ENUM(STUDENT,FACULTY),
  holder_id BIGINT, start_date, expiry_date, status ENUM(ACTIVE,EXPIRED,SUSPENDED))
- **issues** (id PK, book_copy_id FK, membership_id FK, issued_by FK→users, issue_date,
  due_date, status ENUM(ISSUED,RETURNED,OVERDUE,LOST), UNIQUE partial index enforcing one open
  issue per book_copy)
- **returns** (id PK, issue_id FK UNIQUE, received_by FK→users, return_date,
  condition_on_return, notes)
- **reservations** (id PK, book_id FK, membership_id FK, requested_at, queue_position,
  expires_at, status ENUM(WAITING,READY,FULFILLED,EXPIRED,CANCELLED))
- **fines** (id PK, issue_id FK, amount DECIMAL(10,2), reason ENUM(OVERDUE,DAMAGE,LOST,MANUAL),
  status ENUM(PENDING,PAID,WAIVED,PARTIAL), created_at)
- **payments** (id PK, fine_id FK, amount DECIMAL(10,2), method ENUM(CASH,CARD,ONLINE),
  paid_at, receipt_number UNIQUE, received_by FK→users)

## Inventory & Procurement
- **suppliers** (id PK, name, contact_person, phone, email, address)
- **purchase_orders** (id PK, supplier_id FK, ordered_by FK→users, order_date, status
  ENUM(DRAFT,PENDING_APPROVAL,APPROVED,RECEIVED,CANCELLED), budget_amount, approved_by FK→users)
- **purchase_order_items** (id PK, purchase_order_id FK, book_id FK NULLABLE, description,
  quantity, unit_cost)
- **invoices** (id PK, purchase_order_id FK, invoice_number, invoice_date, total_amount,
  file_path)
- **inventory_audits** (id PK, branch_id FK, conducted_by FK→users, started_at, completed_at,
  status)
- **inventory_audit_items** (id PK, inventory_audit_id FK, book_copy_id FK, expected_status,
  found_status, notes)

## Notifications, Logging, Settings
- **notifications** (id PK, user_id FK, type ENUM(EMAIL,DESKTOP), category ENUM(OVERDUE,
  RESERVATION_READY,FINE,GENERAL), message, is_read, created_at, sent_at)
- **audit_logs** (id PK, user_id FK, action, entity_type, entity_id, before_snapshot JSON,
  after_snapshot JSON, ip_address, created_at) — append-only, indexed on (entity_type,
  entity_id) and (user_id, created_at)
- **system_logs** (id PK, level, logger_name, message, thrown TEXT, created_at)
- **settings** (id PK, `key` UNIQUE, `value`, category, updated_by FK→users, updated_at)
- **backups** (id PK, file_path, size_bytes, created_at, created_by FK→users, status)

## Indexing & Constraints Strategy
- Every FK indexed; composite indexes for hot query paths:
  `books(title)` FULLTEXT, `books(isbn)`, `book_copies(barcode)`, `book_copies(status, branch_id)`,
  `issues(membership_id, status)`, `issues(due_date, status)` (overdue sweep),
  `audit_logs(entity_type, entity_id)`, `students(roll_number)`, `students(department, year)`.
- `ON DELETE RESTRICT` on FKs referencing catalog/people entities with historical transactions
  (books, students, users) to preserve audit integrity; soft-delete flags (`is_deleted`,
  `status = 'DISABLED'`) used instead of hard deletes for these.
- `ON DELETE CASCADE` only for pure child/detail rows (`book_authors`, `book_tags`,
  `purchase_order_items`, `role_permissions`, `user_roles`).
- CHECK constraints: `amount >= 0` on fines/payments; `expiry_date > start_date` on memberships;
  `due_date > issue_date` on issues.
