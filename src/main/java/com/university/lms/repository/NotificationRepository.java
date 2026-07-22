package com.university.lms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Notification;
import com.university.lms.entity.NotificationCategory;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByUserId(Long userId, int limit);

    long countUnreadByUserId(Long userId);

    boolean existsByUserIdAndCategorySince(Long userId, NotificationCategory category, LocalDateTime since);

    void markRead(Long notificationId);
}
