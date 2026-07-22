package com.university.lms.repository;

import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Setting;

/** Persistence contract for {@link Setting}. */
public interface SettingRepository {

    List<Setting> findAll();

    Optional<Setting> findByKey(String key);

    Setting save(Setting setting);
}
