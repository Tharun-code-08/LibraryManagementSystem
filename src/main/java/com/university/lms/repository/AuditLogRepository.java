package com.university.lms.repository;

import java.util.List;

import com.university.lms.dto.request.AuditLogSearchCriteria;
import com.university.lms.entity.AuditLog;

/** Persistence contract for the append-only audit trail. */
public interface AuditLogRepository {

    void save(Long actorUserId, String action, String entityType, Long entityId);

    List<AuditLog> findRecent(int limit);

    List<AuditLog> search(AuditLogSearchCriteria criteria);

    long countSearchResults(AuditLogSearchCriteria criteria);
}
