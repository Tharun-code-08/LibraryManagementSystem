package com.university.lms.service.admin.impl;

import java.util.List;

import com.university.lms.dto.response.SettingDTO;
import com.university.lms.entity.Setting;
import com.university.lms.entity.User;
import com.university.lms.repository.SettingRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.admin.SettingsService;
import com.university.lms.service.auth.AuditLogService;

public final class SettingsServiceImpl implements SettingsService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PermissionEvaluator permissionEvaluator;

    public SettingsServiceImpl(SettingRepository settingRepository, UserRepository userRepository,
                                AuditLogService auditLogService, PermissionEvaluator permissionEvaluator) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public List<SettingDTO> listAll() {
        permissionEvaluator.requirePermission("SETTINGS_MANAGE");
        return settingRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public SettingDTO updateSetting(String key, String value, Long actorUserId) {
        permissionEvaluator.requirePermission("SETTINGS_MANAGE");
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
