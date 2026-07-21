-- V8: Password reset tokens, supporting the Forgot Password flow.
-- Each token is single-use (marked used_at once redeemed) and time-boxed.

CREATE TABLE password_reset_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    token      VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    used_at    DATETIME,
    UNIQUE KEY uk_password_reset_tokens_token (token),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_prt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
