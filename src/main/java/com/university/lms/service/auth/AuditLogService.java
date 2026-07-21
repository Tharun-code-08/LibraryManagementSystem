package com.university.lms.service.auth;

import com.university.lms.entity.User;

/** Writes append-only audit trail entries. Every service, across every module, depends on this. */
public interface AuditLogService {

    void log(User actor, String action, String entityType, Long entityId);
}
