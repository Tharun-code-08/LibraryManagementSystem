package com.university.lms.service.admin;

import java.util.List;

import com.university.lms.dto.response.BackupDTO;

public interface BackupService {

    List<BackupDTO> listRecent(int limit);

    /** Runs {@code mysqldump} against the configured database and records the outcome — never
     *  throws; a failed dump is returned as a {@code BackupDTO} with {@code status = "FAILED"}.
     *  Requires {@code SETTINGS_MANAGE} — this is the interactive, UI-triggered entry point. */
    BackupDTO runBackup(Long actorUserId);

    /** Identical to {@link #runBackup(Long)} but skips the permission check — the only caller
     *  is {@code AppContext}'s own scheduled nightly-backup sweep, which runs with no
     *  authenticated user in context and would otherwise always fail that check. Never call
     *  this from a UI-reachable code path. */
    BackupDTO runScheduledBackup();

    /** Restores the database from a previously recorded backup file via the {@code mysql} CLI.
     *  @return true if the restore process exited successfully. */
    boolean restoreBackup(Long backupId, Long actorUserId);
}
