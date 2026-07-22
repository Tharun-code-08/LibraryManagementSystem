package com.university.lms.service.admin.impl;

import java.util.List;

import com.university.lms.dto.response.SettingDTO;
import com.university.lms.entity.Setting;
import com.university.lms.entity.User;
import com.university.lms.repository.SettingRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.admin.SettingsService;
import com.university.lms.service.auth.AuditLogService;

public final class SettingsServiceImpl implements SettingsService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public SettingsServiceImpl(SettingRepository settingRepository, UserRepository userRepository,
                                AuditLogService auditLogService) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<SettingDTO> listAll() {
        return settingRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public SettingDTO updateSetting(String key, String value, Long actorUserId) {
        User actor = actorUserId != null ? userRepository.findById(actorUserId).orElse(null) : null;
        Setting setting = settingRepository.findByKey(key).orElse(null);
        if (setting == null) {
            setting = new Setting(key, value, "GENERAL", actor);
        } else {
            setting.setValue(value);
            setting.setUpdatedBy(actor);
        }
        Setting saved = settingRepository.save(setting);
        auditLogService.log(actorUserId, "SETTING_CHANGED", "Setting", saved.getId());
        return toDto(saved);
    }

    private SettingDTO toDto(Setting setting) {
        return new SettingDTO(setting.getId(), setting.getKey(), setting.getValue(),
                setting.getCategory(), setting.getUpdatedAt());
    }
}
