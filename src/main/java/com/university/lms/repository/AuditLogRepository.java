package com.university.lms.repository;

import com.university.lms.entity.AuditLog;

/** Persistence contract for the append-only {@link AuditLog} trail. */
public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);
}
