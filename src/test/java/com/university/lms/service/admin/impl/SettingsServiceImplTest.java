package com.university.lms.service.admin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.SettingDTO;
import com.university.lms.entity.Setting;
import com.university.lms.entity.User;
import com.university.lms.repository.SettingRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.auth.AuditLogService;

@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private SettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SettingsServiceImpl(settingRepository, userRepository, auditLogService);
        lenient().when(settingRepository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(userRepository.findById(100L))
                .thenReturn(Optional.of(new User("admin", "admin@library.local", "hash", null)));
    }

    @Test
    void listAllMapsSettings() {
        when(settingRepository.findAll()).thenReturn(
                List.of(new Setting("app.theme.default", "light", "UI", null)));

        List<SettingDTO> result = service.listAll();

        assertEquals(1, result.size());
        assertEquals("app.theme.default", result.get(0).key());
    }

    @Test
    void updateSettingCreatesWhenAbsent() {
        when(settingRepository.findByKey("app.theme.default")).thenReturn(Optional.empty());

        SettingDTO result = service.updateSetting("app.theme.default", "dark", 100L);

        assertEquals("dark", result.value());
    }

    @Test
    void updateSettingUpdatesExistingValue() {
        Setting existing = new Setting("app.theme.default", "light", "UI", null);
        when(settingRepository.findByKey("app.theme.default")).thenReturn(Optional.of(existing));

        SettingDTO result = service.updateSetting("app.theme.default", "dark", 100L);

        assertEquals("dark", result.value());
        assertEquals("dark", existing.getValue());
    }
}
