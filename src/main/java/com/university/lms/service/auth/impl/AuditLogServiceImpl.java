package com.university.lms.service.auth.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
