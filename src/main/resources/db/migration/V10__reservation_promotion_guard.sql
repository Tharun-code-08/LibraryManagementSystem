-- V10: closes a race the "pro level" hardening pass found — two concurrent returns for the
-- same book could each call ReservationQueueManager.promoteNextWaiting() and both promote a
-- (possibly different) WAITING reservation to READY, double-reserving a single freed copy.
-- Mirrors the existing uk_issues_open_copy pattern from V4: a generated column that is only
-- non-null while the row is in the state we want at-most-one-per-book, with a unique index on
-- it, so the loser's INSERT/UPDATE fails at the database level instead of silently succeeding.

ALTER TABLE reservations
    ADD COLUMN ready_book_id BIGINT AS (CASE WHEN status = 'READY' THEN book_id END) STORED,
    ADD UNIQUE KEY uk_reservations_ready_book (ready_book_id);
