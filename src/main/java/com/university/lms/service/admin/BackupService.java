package com.university.lms.service.admin;

import java.util.List;

import com.university.lms.dto.response.BackupDTO;

public interface BackupService {

    List<BackupDTO> listRecent(int limit);

    /** Runs {@code mysqldump} against the configured database and records the outcome — never
     *  throws; a failed dump is returned as a {@code BackupDTO} with {@code status = "FAILED"}. */
    BackupDTO runBackup(Long actorUserId);

    /** Restores the database from a previously recorded backup file via the {@code mysql} CLI.
     *  @return true if the restore process exited successfully. */
    boolean restoreBackup(Long backupId, Long actorUserId);
}
