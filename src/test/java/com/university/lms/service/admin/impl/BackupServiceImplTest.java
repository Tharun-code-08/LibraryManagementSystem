package com.university.lms.service.admin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.BackupDTO;
import com.university.lms.entity.Backup;
import com.university.lms.repository.BackupRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.admin.ProcessExecutor;
import com.university.lms.service.auth.AuditLogService;

@ExtendWith(MockitoExtension.class)
class BackupServiceImplTest {

    @Mock
    private BackupRepository backupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ProcessExecutor processExecutor;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    @TempDir
    private Path backupDirectory;

    private BackupServiceImpl backupService;

    @BeforeEach
    void setUp() {
        backupService = new BackupServiceImpl(backupRepository, userRepository, auditLogService, processExecutor,
                permissionEvaluator, "localhost", 3306, "library_management", "lms_app", "changeme", backupDirectory);
        lenient().when(userRepository.findById(100L)).thenReturn(Optional.empty());
        lenient().when(backupRepository.save(any(Backup.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void runBackupRecordsSuccessAndFileSize() throws Exception {
        when(processExecutor.run(any())).thenAnswer(invocation -> {
            ProcessBuilder processBuilder = invocation.getArgument(0);
            Path outputPath = processBuilder.redirectOutput().file().toPath();
            Files.writeString(outputPath, "-- dump contents --");
            return 0;
        });

        BackupDTO result = backupService.runBackup(100L);

        assertEquals("SUCCESS", result.status());
        assertTrue(result.sizeBytes() > 0);
        verify(auditLogService).log(100L, "BACKUP_SUCCESS", "Backup", null);
    }

    @Test
    void runBackupRecordsFailureOnNonZeroExit() throws Exception {
        when(processExecutor.run(any())).thenReturn(1);

        BackupDTO result = backupService.runBackup(100L);

        assertEquals("FAILED", result.status());
        verify(auditLogService).log(100L, "BACKUP_FAILED", "Backup", null);
    }

    @Test
    void runBackupRecordsFailureOnIOException() throws Exception {
        when(processExecutor.run(any())).thenThrow(new IOException("mysqldump not found"));

        BackupDTO result = backupService.runBackup(100L);

        assertEquals("FAILED", result.status());
    }

    @Test
    void restoreBackupReturnsTrueOnSuccess() throws Exception {
        Backup backup = new Backup(backupDirectory.resolve("backup-1.sql").toString(), null);
        when(backupRepository.findById(1L)).thenReturn(Optional.of(backup));
        when(processExecutor.run(any())).thenReturn(0);

        boolean success = backupService.restoreBackup(1L, 100L);

        assertTrue(success);
        verify(auditLogService).log(100L, "BACKUP_RESTORED", "Backup", 1L);
    }

    @Test
    void restoreBackupReturnsFalseOnNonZeroExit() throws Exception {
        Backup backup = new Backup(backupDirectory.resolve("backup-1.sql").toString(), null);
        when(backupRepository.findById(1L)).thenReturn(Optional.of(backup));
        when(processExecutor.run(any())).thenReturn(1);

        boolean success = backupService.restoreBackup(1L, 100L);

        assertFalse(success);
        verify(auditLogService).log(100L, "BACKUP_RESTORE_FAILED", "Backup", 1L);
    }

    @Test
    void listRecentMapsBackups() {
        Backup backup = new Backup("/backups/x.sql", null);
        when(backupRepository.findRecent(5)).thenReturn(List.of(backup));

        List<BackupDTO> result = backupService.listRecent(5);

        assertEquals(1, result.size());
    }
}
