-- V9: Phase 12 performance pass — indexes for query patterns added by later phases
-- (Phase 7 dashboard trends, Phase 8 date-range reports, Phase 9 notification dedup) that
-- weren't covered by the composite indexes already in place from V2/V4/V6.

-- Issues/Returns reports (ReportServiceImpl.issuesReport/returnsReport) and the dashboard's
-- monthly trend both range-filter on these date columns directly; V4 only indexed
-- (due_date, status) and (membership_id, status), neither of which a plain issue_date/
-- return_date range predicate can use as a leading column.
ALTER TABLE issues ADD INDEX idx_issues_issue_date (issue_date);
ALTER TABLE returns ADD INDEX idx_returns_return_date (return_date);

-- NotificationServiceImpl.runOverdueReminderSweep's per-user dedup check filters by
-- (user_id, category, created_at >= today) — V6 only indexed (user_id, is_read), which
-- doesn't cover category or the created_at range.
ALTER TABLE notifications ADD INDEX idx_notifications_user_category_created (user_id, category, created_at);
