package com.university.lms.service.auth;

/** Writes append-only audit trail entries. Every service, across every module, depends on this. */
public interface AuditLogService {

    void log(Long actorUserId, String action, String entityType, Long entityId);
}
