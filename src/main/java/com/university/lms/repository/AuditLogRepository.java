package com.university.lms.repository;

/** Persistence contract for the append-only audit trail. */
public interface AuditLogRepository {

    void save(Long actorUserId, String action, String entityType, Long entityId);
}
