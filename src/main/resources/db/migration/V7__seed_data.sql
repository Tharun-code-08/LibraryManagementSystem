-- V7: Baseline reference/seed data required for the application to be usable on first boot.

INSERT INTO branches (name, code, address, phone) VALUES
    ('Main Campus Library', 'MAIN', 'University Main Campus', '000-000-0000');

INSERT INTO roles (name, description) VALUES
    ('ADMIN',      'Full system administrator'),
    ('LIBRARIAN',  'Day-to-day library operations staff'),
    ('STUDENT',    'Student borrower'),
    ('FACULTY',    'Faculty borrower'),
    ('GUEST',      'Unauthenticated / catalog-only access');

INSERT INTO permissions (code, description) VALUES
    ('BOOK_VIEW',           'View book catalog'),
    ('BOOK_MANAGE',         'Create/edit/delete/restore books'),
    ('PEOPLE_MANAGE',       'Manage student/faculty records'),
    ('CIRCULATION_MANAGE',  'Issue/return/reserve books'),
    ('FINE_MANAGE',         'Create, waive, and collect fines'),
    ('INVENTORY_MANAGE',    'Manage inventory audits and stock'),
    ('PROCUREMENT_MANAGE',  'Manage suppliers and purchase orders'),
    ('REPORT_VIEW',         'Generate and export reports'),
    ('USER_MANAGE',         'Manage users, roles, and permissions'),
    ('SETTINGS_MANAGE',     'Manage system settings and backups'),
    ('AUDIT_LOG_VIEW',      'View system audit logs');

-- ADMIN: every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON r.name = 'ADMIN';

-- LIBRARIAN: operational permissions (everything except user/role and settings management)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON r.name = 'LIBRARIAN'
   AND p.code IN ('BOOK_VIEW','BOOK_MANAGE','PEOPLE_MANAGE','CIRCULATION_MANAGE',
                  'FINE_MANAGE','INVENTORY_MANAGE','PROCUREMENT_MANAGE','REPORT_VIEW');

-- STUDENT / FACULTY: read-only catalog + implicit self-service (enforced in service layer)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON r.name IN ('STUDENT','FACULTY') AND p.code = 'BOOK_VIEW';

-- GUEST: catalog browsing only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON r.name = 'GUEST' AND p.code = 'BOOK_VIEW';

-- Default administrator account. Password: "Admin@123" (BCrypt, cost 12) — MUST be
-- rotated immediately after first login in any non-development environment.
INSERT INTO users (username, email, password_hash, status, branch_id)
SELECT 'admin', 'admin@library.local',
       '$2b$12$BvcnkssNLMcvLQ1M9oVbneIS6rqtGdLRgr2H3cDjJLmefzlEloUUK',
       'ACTIVE', b.id
FROM branches b WHERE b.code = 'MAIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON u.username = 'admin' AND r.name = 'ADMIN';

INSERT INTO membership_types (name, max_borrow_limit, loan_period_days, fine_per_day, grace_period_days, renewal_limit) VALUES
    ('STUDENT_STANDARD', 3, 14, 5.00, 1, 2),
    ('FACULTY_STANDARD', 10, 30, 5.00, 3, 5);

INSERT INTO settings (`key`, `value`, category) VALUES
    ('app.theme.default', 'light', 'UI'),
    ('app.session.idle-timeout-minutes', '15', 'SECURITY'),
    ('app.circulation.reservation-hold-days', '3', 'CIRCULATION'),
    ('app.backup.auto-enabled', 'true', 'BACKUP');
