-- V6: Notifications, append-only audit/system logs, settings, backup records.

CREATE TABLE notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    type       ENUM('EMAIL','DESKTOP') NOT NULL,
    category   ENUM('OVERDUE','RESERVATION_READY','FINE','GENERAL') NOT NULL,
    message    VARCHAR(500) NOT NULL,
    is_read    TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at    DATETIME,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notifications_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Append-only: no UPDATE/DELETE grants issued to the application role for this table.
CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    action          VARCHAR(80) NOT NULL,
    entity_type     VARCHAR(80) NOT NULL,
    entity_id       BIGINT,
    before_snapshot JSON,
    after_snapshot  JSON,
    ip_address      VARCHAR(45),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    level       VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255) NOT NULL,
    message     TEXT NOT NULL,
    thrown      TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_system_logs_level_created (level, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE settings (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `key`      VARCHAR(120) NOT NULL,
    `value`    VARCHAR(500),
    category   VARCHAR(80),
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settings_key (`key`),
    CONSTRAINT fk_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE backups (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_path  VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    status     ENUM('SUCCESS','FAILED','IN_PROGRESS') NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_backups_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
