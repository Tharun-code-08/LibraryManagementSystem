package com.university.lms.service.admin.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.dto.response.BackupDTO;
import com.university.lms.entity.Backup;
import com.university.lms.entity.BackupStatus;
import com.university.lms.entity.User;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.BackupRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.admin.BackupService;
import com.university.lms.service.admin.ProcessExecutor;
import com.university.lms.service.auth.AuditLogService;

public final class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final BackupRepository backupRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ProcessExecutor processExecutor;
    private final String host;
    private final int port;
    private final String database;
    private final String dbUsername;
    private final String dbPassword;
    private final Path backupDirectory;

    public BackupServiceImpl(BackupRepository backupRepository, UserRepository userRepository,
                              AuditLogService auditLogService, ProcessExecutor processExecutor,
                              String host, int port, String database, String dbUsername, String dbPassword,
                              Path backupDirectory) {
        this.backupRepository = backupRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.processExecutor = processExecutor;
        this.host = host;
        this.port = port;
        this.database = database;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.backupDirectory = backupDirectory;
    }

    @Override
    public List<BackupDTO> listRecent(int limit) {
        return backupRepository.findRecent(limit).stream().map(this::toDto).toList();
    }

    @Override
    public BackupDTO runBackup(Long actorUserId) {
        User actor = actorUserId != null ? userRepository.findById(actorUserId).orElse(null) : null;
        String fileName = "backup-" + java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".sql";
        Path outputPath = backupDirectory.resolve(fileName);
        Backup backup = backupRepository.save(new Backup(outputPath.toString(), actor));

        try {
            Files.createDirectories(backupDirectory);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump", "--host=" + host, "--port=" + port, "--user=" + dbUsername, database)
                    .redirectOutput(outputPath.toFile());
            processBuilder.environment().put("MYSQL_PWD", dbPassword);

            int exitCode = processExecutor.run(processBuilder);
            if (exitCode == 0) {
                backup.setSizeBytes(Files.size(outputPath));
                backup.setStatus(BackupStatus.SUCCESS);
            } else {
                backup.setStatus(BackupStatus.FAILED);
                log.warn("mysqldump exited with code {} for backup {}", exitCode, backup.getId());
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            backup.setStatus(BackupStatus.FAILED);
            log.warn("Backup run failed: {}", e.getMessage());
        }

        Backup saved = backupRepository.save(backup);
        auditLogService.log(actorUserId, "BACKUP_" + saved.getStatus(), "Backup", saved.getId());
        return toDto(saved);
    }

    @Override
    public boolean restoreBackup(Long backupId, Long actorUserId) {
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new ResourceNotFoundException("Backup", backupId));

        boolean success;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysql", "--host=" + host, "--port=" + port, "--user=" + dbUsername, database)
                    .redirectInput(Path.of(backup.getFilePath()).toFile());
            processBuilder.environment().put("MYSQL_PWD", dbPassword);

            success = processExecutor.run(processBuilder) == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Restore run failed: {}", e.getMessage());
            success = false;
        }

        auditLogService.log(actorUserId, success ? "BACKUP_RESTORED" : "BACKUP_RESTORE_FAILED", "Backup", backupId);
        return success;
    }

    private BackupDTO toDto(Backup backup) {
        return new BackupDTO(backup.getId(), backup.getFilePath(), backup.getSizeBytes(),
                backup.getCreatedAt(), backup.getStatus().name());
    }
}
