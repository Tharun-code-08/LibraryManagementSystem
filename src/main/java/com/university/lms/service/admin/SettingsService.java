package com.university.lms.service.admin;

import java.util.List;

import com.university.lms.dto.response.SettingDTO;

public interface SettingsService {

    List<SettingDTO> listAll();

    /** Creates the setting if it doesn't already exist (upsert), otherwise updates its value. */
    SettingDTO updateSetting(String key, String value, Long actorUserId);
}
