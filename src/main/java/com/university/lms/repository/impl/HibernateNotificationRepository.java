package com.university.lms.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Notification;
import com.university.lms.entity.NotificationCategory;
import com.university.lms.repository.NotificationRepository;

public final class HibernateNotificationRepository implements NotificationRepository {

    private final SessionFactory sessionFactory;

    public HibernateNotificationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Notification save(Notification notification) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Notification merged = session.merge(notification);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public Optional<Notification> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Notification.class, id));
        }
    }

    @Override
    public List<Notification> findByUserId(Long userId, int limit) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                    "from Notification n where n.user.id = :userId order by n.createdAt desc", Notification.class);
            query.setParameter("userId", userId);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(n) from Notification n where n.user.id = :userId and n.read = false", Long.class);
            query.setParameter("userId", userId);
            return query.uniqueResult();
        }
    }

    @Override
    public boolean existsByUserIdAndCategorySince(Long userId, NotificationCategory category, LocalDateTime since) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(n) from Notification n where n.user.id = :userId and n.category = :category "
                            + "and n.createdAt >= :since", Long.class);
            query.setParameter("userId", userId);
            query.setParameter("category", category);
            query.setParameter("since", since);
            return query.uniqueResult() > 0;
        }
    }

    @Override
    public void markRead(Long notificationId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Notification notification = session.get(Notification.class, notificationId);
            if (notification != null) {
                notification.setRead(true);
            }
            session.getTransaction().commit();
        }
    }
}
