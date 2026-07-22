-- V4: Circulation (issue/return/reservation) and finance (fines/payments).

CREATE TABLE issues (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_copy_id  BIGINT NOT NULL,
    membership_id BIGINT NOT NULL,
    issued_by     BIGINT NOT NULL,
    issue_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date      DATETIME NOT NULL,
    status        ENUM('ISSUED','RETURNED','OVERDUE','LOST') NOT NULL DEFAULT 'ISSUED',
    -- NULL whenever the issue is closed; InnoDB unique indexes ignore NULLs, so this
    -- enforces "at most one OPEN (ISSUED/OVERDUE) issue per copy" without blocking history.
    open_copy_id  BIGINT AS (CASE WHEN status IN ('ISSUED','OVERDUE') THEN book_copy_id END) STORED,
    CONSTRAINT fk_issues_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id) ON DELETE RESTRICT,
    CONSTRAINT fk_issues_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE RESTRICT,
    CONSTRAINT fk_issues_issued_by FOREIGN KEY (issued_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_issues_dates CHECK (due_date > issue_date),
    UNIQUE KEY uk_issues_open_copy (open_copy_id),
    INDEX idx_issues_membership_status (membership_id, status),
    INDEX idx_issues_due_status (due_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE returns (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id            BIGINT NOT NULL,
    received_by         BIGINT NOT NULL,
    return_date         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    condition_on_return ENUM('GOOD','DAMAGED','LOST') NOT NULL DEFAULT 'GOOD',
    notes               VARCHAR(255),
    UNIQUE KEY uk_returns_issue (issue_id),
    CONSTRAINT fk_returns_issue FOREIGN KEY (issue_id) REFERENCES issues (id) ON DELETE RESTRICT,
    CONSTRAINT fk_returns_received_by FOREIGN KEY (received_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id         BIGINT NOT NULL,
    membership_id   BIGINT NOT NULL,
    requested_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    queue_position  INT NOT NULL,
    expires_at      DATETIME,
    status          ENUM('WAITING','READY','FULFILLED','EXPIRED','CANCELLED') NOT NULL DEFAULT 'WAITING',
    CONSTRAINT fk_reservations_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservations_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE RESTRICT,
    INDEX idx_reservations_book_status (book_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fines (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id   BIGINT NOT NULL,
    amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    reason     ENUM('OVERDUE','DAMAGE','LOST','MANUAL') NOT NULL,
    status     ENUM('PENDING','PAID','WAIVED','PARTIAL') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fines_issue FOREIGN KEY (issue_id) REFERENCES issues (id) ON DELETE RESTRICT,
    CONSTRAINT chk_fines_amount CHECK (amount >= 0),
    INDEX idx_fines_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fine_id         BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    method          ENUM('CASH','CARD','ONLINE') NOT NULL,
    paid_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    receipt_number  VARCHAR(50) NOT NULL,
    received_by     BIGINT NOT NULL,
    UNIQUE KEY uk_payments_receipt (receipt_number),
    CONSTRAINT fk_payments_fine FOREIGN KEY (fine_id) REFERENCES fines (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_received_by FOREIGN KEY (received_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_payments_amount CHECK (amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
