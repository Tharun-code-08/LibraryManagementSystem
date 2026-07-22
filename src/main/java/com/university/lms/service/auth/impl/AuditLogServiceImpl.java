package com.university.lms.service.auth.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.dto.request.AuditLogSearchCriteria;
import com.university.lms.dto.response.AuditLogEntryDTO;
import com.university.lms.entity.AuditLog;
import com.university.lms.model.Page;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.service.auth.AuditLogService;

public final class AuditLogServiceImpl implements AuditLogService {

    private static final Logger auditLogger = LoggerFactory.getLogger("com.university.lms.audit");

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(Long actorUserId, String action, String entityType, Long entityId) {
        auditLogRepository.save(actorUserId, action, entityType, entityId);
        auditLogger.info("action={} entityType={} entityId={} actorUserId={}",
                action, entityType, entityId, actorUserId);
    }

    @Override
    public Page<AuditLogEntryDTO> search(AuditLogSearchCriteria criteria) {
        var entries = auditLogRepository.search(criteria).stream().map(this::toDto).toList();
        long total = auditLogRepository.countSearchResults(criteria);
        return new Page<>(entries, criteria.getPageNumber(), criteria.getPageSize(), total);
    }

    private AuditLogEntryDTO toDto(AuditLog auditLog) {
        String actorUsername = auditLog.getUser() != null ? auditLog.getUser().getUsername() : "System";
        return new AuditLogEntryDTO(auditLog.getId(), actorUsername, auditLog.getAction(),
                auditLog.getEntityType(), auditLog.getEntityId(), auditLog.getCreatedAt());
    }
}
