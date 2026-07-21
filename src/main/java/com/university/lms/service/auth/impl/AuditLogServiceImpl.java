package com.university.lms.service.auth.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.entity.AuditLog;
import com.university.lms.entity.User;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.service.auth.AuditLogService;

public final class AuditLogServiceImpl implements AuditLogService {

    private static final Logger auditLogger = LoggerFactory.getLogger("com.university.lms.audit");

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(User actor, String action, String entityType, Long entityId) {
        AuditLog entry = new AuditLog(actor, action, entityType, entityId, null, null, null);
        auditLogRepository.save(entry);
        auditLogger.info("action={} entityType={} entityId={} actor={}",
                action, entityType, entityId, actor != null ? actor.getUsername() : "anonymous");
    }
}
