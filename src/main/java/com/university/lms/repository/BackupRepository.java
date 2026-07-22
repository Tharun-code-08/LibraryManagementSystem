package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Backup;

/** Persistence contract for {@link Backup}. */
public interface BackupRepository {

    Backup save(Backup backup);

    Optional<Backup> findById(Long id);

    List<Backup> findRecent(int limit);
}
