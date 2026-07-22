package com.university.lms.service.auth;

import com.university.lms.dto.request.AuditLogSearchCriteria;
import com.university.lms.dto.response.AuditLogEntryDTO;
import com.university.lms.model.Page;

/** Writes append-only audit trail entries. Every service, across every module, depends on this. */
public interface AuditLogService {

    void log(Long actorUserId, String action, String entityType, Long entityId);

    /** Paginated, filterable view over the audit trail — backs the admin audit log viewer. */
    Page<AuditLogEntryDTO> search(AuditLogSearchCriteria criteria);
}
